/*
 * Copyright (C) 2008-2010 Surevine Limited.
 *   
 * Although intended for deployment and use alongside Alfresco this module should
 * be considered 'Not a Contribution' as defined in Alfresco'sstandard contribution agreement, see
 * http://www.alfresco.org/resource/AlfrescoContributionAgreementv2.pdf
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package com.surevine.alfresco;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.apache.log4j.Logger;


/**
 * Authenticates an HTTP connection using a CAS service.
 */
public class CasAuthenticator implements Authenticator {

    /**
     * HTTP success code.
     */
    private static final int  HTTP_SUCCESS_CODE = 200;

    /**
     * Starting value of HTTP error codes.
     */
    private static final int  HTTP_ERROR_CODE = 400;

    /**
     * URL for CAS login page.
     */
    private String urlLogin;

    /**
     * URL for a page protected by CAS (required to complete authentication).
     */
    private String urlPostlogin;

    /**
     * CAS username.
     */
    private String username;

    /**
     * CAS password.
     */
    private String password;

    /**
     * Logging instance.
     */
    private static final Logger LOGGER = Logger.getLogger(CasAuthenticator.class);

    /**
     * @param properties Contains configuration for the CAS server to connect to
     * @throws PropertyException If required properties are not present
     */
    public CasAuthenticator(final PropertyWrapper properties)
            throws PropertyException {
        urlLogin = properties.getProperty("cas.url.login");
        urlPostlogin = properties.getProperty("cas.url.postlogin");
        username = properties.getProperty("alfresco.username");
        password = properties.getProperty("alfresco.password");
    }

    /**
     * Attempt to authenticate the client.
     * @param client The HTTP client to authenticate
     * @return True if authentication was successful, otherwise false
     */
    public boolean authenticate(final HttpClient client) {
        String loginTicket = getLoginTicket(client, urlLogin);

        if (loginTicket == null) {
            return false;
        }

        if (!login(client, urlLogin, loginTicket)) {
            return false;
        }

        // CAS authentication seems to fail on POSTs unless you have previously
        // done a GET on a page which requires authentication. I don't know why
        // this is so - my best guess is that the authentication token is only
        // passed on GETs not POSTs? We visit a protected page with a GET
        // immediately after authentication to work around this problem.
        if (!visitPage(client, urlPostlogin)) {
            return false;
        }

        // got this far, so authenticated ok
        return true;
    }

    /**
     * Get the CAS login ticket from the CAS login page (it's a required field to post to the login form).
     * @param client The HTTP client
     * @param url URL of the login page
     * @return The login ticket or null if it couldn't be found
     */
    private String getLoginTicket(final HttpClient client, final String url) {
        // get login form and parse out logging ticket ("lt")
        HttpGet get = new HttpGet(url);

        HttpResponse getResponse;

        try {
            getResponse = client.execute(get);
        } catch (ClientProtocolException e) {
            logError("Failed to execute HTTP GET", e);
             return null;
        } catch (IOException e) {
            logError("Failed to execute HTTP GET", e);
            return null;
        }

        StatusLine getStatus = getResponse.getStatusLine();

        if (getStatus.getStatusCode() >= HTTP_ERROR_CODE) {
            logError("Cannot get CAS login form: " + getStatus.getStatusCode()
                     + " : " + getStatus.getReasonPhrase());
            return null;
        }

        HttpEntity getEnt = getResponse.getEntity();

        String loginTicket = null;

        // we've got a response so look inside it for the "lt" field
        if (getEnt != null) {
            String text;

            // get the text of the response
            try {
                text = EntityUtils.toString(getEnt);
            } catch (ParseException e) {
                logError("Cannot get CAS login ticket", e);
                return null;
            } catch (IOException e) {
                logError("Cannot get CAS login ticket", e);
                return null;
            }

            if (text != null) {
                // look for "lt"
                int namePos = text.indexOf("name=\"lt\"");

                if (namePos >= 0) {
                    // we've found "lt" so now look for the value parameter
                    final String valueField = "value=\"";
                    int valuePos = text.indexOf(valueField, namePos);

                    if (valuePos >= 0) {
                        // found it, skip past 'value="'
                        valuePos += valueField.length();

                        int valueEnd = text.indexOf('"', valuePos);

                        if (valueEnd >= 0) {
                            // got the login ticket value
                            loginTicket = text.substring(valuePos, valueEnd);
                        }
                    }
                }
            }

            // make sure response buffer is flushed so we can use the client
            // again
            if (!flushBuffer(getEnt)) {
                return null;
            }
        }

        return loginTicket;
    }

    /**
     * Attempt to login into CAS.
     * @param client The HTTP client
     * @param url URL to post to
     * @param loginTicket The login ticket from the login page
     * @return True if authenticate succeeded, otherwise false
     */
    private boolean login(final HttpClient client, final String url, final String loginTicket) {
        // submit login form

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("lt", loginTicket));
        params.add(new BasicNameValuePair("_eventId", "submit"));

        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logError("Failed to initialise CAS parameters", e);
            return false;
        }

        HttpPost postLogin = new HttpPost(url);

        postLogin.setEntity(entity);

        HttpResponse postResponse;
        try {
            postResponse = client.execute(postLogin);
        } catch (ClientProtocolException e) {
            logError("Failed to execute HTTP POST", e);
              return false;
        } catch (IOException e) {
            logError("Failed to execute HTTP POST", e);
            return false;
        }
        StatusLine postStatus = postResponse.getStatusLine();

        if (postStatus.getStatusCode() >= HTTP_ERROR_CODE) {
            logError("Cannot authenticate with CAS: " + postStatus.getStatusCode()
                     + " : " + postStatus.getReasonPhrase());
            return false;
        }

        HttpEntity postEnt = postResponse.getEntity();

        if (postEnt == null) {
            logError("Empty response from CAS: " + postStatus.getStatusCode()
                     + " : " + postStatus.getReasonPhrase());
            return false;
        }

        String text;
        try {
            text = EntityUtils.toString(postEnt);
        } catch (ParseException e) {
            logError("Failed to parse CAS response", e);
            return false;
        } catch (IOException e) {
            logError("Failed to parse CAS response", e);
            return false;
        }

        if (!text.contains("class=\"success\"")) {
            logError("CAS login did not return success");
            return false;
        }

        // make sure response buffer is flushed so we can use the client again
        if (!flushBuffer(postEnt)) {
            return false;
        }

        return true;
    }

    /**
     * Visit a page by doing an HTTP GET.
     * @param client The HTTP client
     * @param url The URL to visit
     * @return True if the GET succeeded, otherwise false
     */
    private boolean visitPage(final HttpClient client, final String url) {
        HttpGet get = new HttpGet(url);
        HttpResponse response;

        try {
            response = client.execute(get);
        } catch (ClientProtocolException e) {
            logError("Failed to execute HTTP GET", e);
            return false;
        } catch (IOException e) {
            logError("Failed to execute HTTP GET", e);
            return false;
        }

        StatusLine status = response.getStatusLine();
        HttpEntity responseEnt = response.getEntity();

        if (responseEnt == null || status.getStatusCode() != HTTP_SUCCESS_CODE) {
            logError("Failed to visit URL \"" + url + "\": "
                     + status.getStatusCode() + ": " + status.getReasonPhrase());
            return false;
        }

        if (!flushBuffer(responseEnt)) {
            return false;
        }

        return true;
    }

    /**
     * Flush out any unused content in an HTTP entity (otherwise the HTTP Client cannot be re-used).
     * @param ent The HTTP entity to flush
     * @return True if the flush succeeded, otherwise false
     */
    private boolean flushBuffer(final HttpEntity ent) {
        try {
            ent.consumeContent();
        } catch (IOException e) {
            logError("Cannot flush HTTP buffer", e);
            return false;
        }

        return true;
    }

    /**
     * Output an error message to the log file.
     * @param error The error message
     */
    private void logError(final String error) {
        LOGGER.error(error);
    }

    /**
     * Output an error message and an exception's stack trace to the log file.
     * @param error The error message
     * @param e The exception
     */
    private void logError(final String error, final Exception e) {
        LOGGER.error(error);
        LOGGER.error(getStackTrace(e));
    }

    /**
     * Get an exception's stack trace.
     * @param e The exception
     * @return The stack trace
     */
    private String getStackTrace(final Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e2) {
            return "stack trace unavailable";
        }
    }
}
