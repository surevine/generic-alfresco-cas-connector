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
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.log4j.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Connect to an Alfresco instance.
 */
public class AlfrescoConnector {

    /**
     * Different types of roles for a member of an Alfresco site.
     */
    public static enum RoleType {
        /**
         * Collaborator.
         */
        COLLABORATOR,
        /**
         * Manager.
         */
        MANAGER
    };

    /**
     * Initial size of hash sets (based on typical group size).
     */
    private static final int  HASH_SIZE = 128;

    /**
     * Starting value of HTTP error codes.
     */
    private static final int  HTTP_ERROR_CODE = 400;


    /**
     * Client for connecting to websites.
     */
    private DefaultHttpClient client;

    /**
     * URL for Alfresco API for manipulating sites.
     */
    private String            alfrescoUrlSites;

    /**
     * URL for Alfresco API for updating profiles.
     */
    private String            alfrescoUrlProfile;

    /**
     * URL for Alfresco API for manipulating Records Management Constraints.
     */
    private String            alfrescoUrlRmConstraint;
    
    /**
     * Base URL of the alfresco REST api
     */
    private String 			  alfrescoUrlBase;

    /**
     * Logging instance.
     */
    private static final Logger LOGGER = Logger.getLogger(AlfrescoConnector.class);

    /**
     * @param properties Contains configuration for the Alfresco server to connect to
     * @param auth A mechanism for authenticating the connection to Alfresco
     * @throws AlfrescoException If required properties are not present or if authentication fails
     */
    public AlfrescoConnector(final PropertyWrapper properties, final Authenticator auth)
            throws AlfrescoException {
        try {
            // read and save properties needed elsewhere
            alfrescoUrlBase = properties.getProperty("alfresco.url.base");
            alfrescoUrlSites = alfrescoUrlBase+"/api/sites/";
            alfrescoUrlRmConstraint = alfrescoUrlBase+"/api/rma/admin/rmconstraints/";
            alfrescoUrlProfile = alfrescoUrlBase+"/sv-theme/user-profile/internal-profile";
        } catch (PropertyException e) {
            throw new AlfrescoException("Cannot find a required property", e);
        }

        // create HTTP client
        client = new DefaultHttpClient();

        // login
        if (!auth.authenticate(client)) {
            throw new AlfrescoException("Cannot log into Alfresco");
        }
    }

    /**
     * Add a member to an Alfresco site.
     * @param site The name of the site (must already exist in Alfresco)
     * @param username The name of the user (must already exist in Alfresco)
     * @param role The role to assign the user within the site
     * @throws AlfrescoException On any Alfresco error
     */
    public void addMemberToSite(final String site, final String username, final RoleType role)
            throws AlfrescoException {
        JSONObject request = new JSONObject();

        try {
            JSONObject person = new JSONObject();
            person.put("userName", username);
            request.put("person", person);

            if (role == RoleType.MANAGER) {
                request.put("role", "SiteManager");
            } else {
                request.put("role", "SiteCollaborator");
            }
        } catch (JSONException e) {
            throw new AlfrescoException("Cannot add a member", e);
        }

        doHttpPost(alfrescoUrlSites + site + "/memberships", request);
    }
    
    /**
     * Sent a REST call to add the given user to the given group.  If the specified user does not exist,
     * an error will be returned.  If the specified group does not exist, nothing will happen (this inconsistent
     * behaviour is a consuequence of the API)
     * @param userName Name of a user within alfresco
     * @param groupName Name of a group within alfresco, without the "GROUP_" prefix
     */
    public void addMemberToGroup(final String userName, final String groupName) throws AlfrescoException
    {
        JSONObject request = new JSONObject();

        //First, try and remove the user from the group, but ignore any errors.  We need to do this as Alfresco doesn't like us
        //adding a user to a group they're already a member of
        try {
        	removeMemberFromGroupIfPresent(userName, groupName);
        }
        catch (Exception e) {
        	LOGGER.warn("Exception thrown removing "+userName+" from "+groupName+": "+e, e);
        }
        
        try {
            JSONArray groupsList = new JSONArray();
            groupsList.put("GROUP_"+groupName);
            request.put("addGroups", groupsList);

        } catch (JSONException e) {
            throw new AlfrescoException("Cannot Create JSON to add "+userName+" to the group "+groupName, e);
        }

        doHttpPut(alfrescoUrlBase +"/api/people/"+userName, request);
    }
    
    public void removeMemberFromGroupIfPresent(final String userName, final String groupName) throws AlfrescoException
    {
    	JSONObject request = new JSONObject();

        try {
            JSONArray groupsList = new JSONArray();
            groupsList.put("GROUP_"+groupName);
            request.put("removeGroups", groupsList);

        } catch (JSONException e) {
            throw new AlfrescoException("Cannot Create JSON to remove "+userName+" from the group "+groupName, e);
        }

        doHttpPut(alfrescoUrlBase +"/api/people/"+userName, request);
    }
    
    public Collection<String> getMembershipOfGroup(final String groupName) throws AlfrescoException {
    	
    	JSONObject jsonObj = doHttpGet(alfrescoUrlBase + "/api/groups/" + groupName + "/children?authorityType=USER");
        Collection<String> memberList = new HashSet<String>(HASH_SIZE);
        
    	JSONArray jsonArray;
		try {
			jsonArray = jsonObj.getJSONArray("data");

	    	
	        for (int x = 0; x < jsonArray.length(); x++) {
	            JSONObject member = jsonArray.optJSONObject(x);

	            if (member != null) {
	            	memberList.add(member.getString("shortName"));
	            }
	        }
		} catch (JSONException e) {
			throw new AlfrescoException("Failed to get members of group" + groupName, e);
		}

		return memberList;
    }

    /**
     * Remove a member from an Alfresco site.
     * @param site The name of the site (must exist in Alfresco)
     * @param username The name of the user (must already be a member of the site)
     * @throws AlfrescoException On any Alfresco error
     */
    public void removeMemberFromSite(final String site, final String username)
            throws AlfrescoException {
        doHttpDelete(alfrescoUrlSites + site + "/memberships/" + username);
    }
    
    /** 
     * Remove a member from an Alfresco site, but first check that they are a member of that site
     * @param site The name of the site (must exist in Alfresco)
     * @param username The name of the user
     * @throws AlfrescoException On any Alfresco error
     */
    public void removeMemberFromSiteIfPresent(final String site, final String username) throws AlfrescoException {
    	JSONArray jsonArray = doHttpGetArray(alfrescoUrlBase + "/api/people/" + username + "/sites");
    	
    	for (int i = 0; i < jsonArray.length(); i++) {
    		JSONObject jsonSite;
			try {
				jsonSite = jsonArray.getJSONObject(i);
	    		
	    		if (jsonSite != null) {
	    			if (site.equalsIgnoreCase(jsonSite.getString("shortName"))) {
	    				// the member does exist in the site, so remove them.
	    				removeMemberFromSite(site, username);
	    			}
	    		}
			} catch (JSONException e) {
				throw new AlfrescoException("Error parsing JSON when getting site access for " + username, e);
			}

    	}
    }

    /**
     * Returns a list of all the current members of an Alfresco site.
     * @param site The name of the site (must exist in Alfresco)
     * @return A list of usernames of the members
     * @throws AlfrescoException On any Alfresco error
     */
    public Collection<String> getSiteMemberList(final String site)
            throws AlfrescoException {
        Collection<String> memberList = new HashSet<String>(HASH_SIZE);

        JSONArray jsonArray = doHttpGetArray(alfrescoUrlSites + site
                + "/memberships?authorityType=USER");

        for (int x = 0; x < jsonArray.length(); x++) {
            JSONObject member = jsonArray.optJSONObject(x);

            if (member != null) {
                JSONObject authority = member.optJSONObject("authority");

                if (authority != null) {
                    String username = authority.optString("userName");

                    if (username.length() > 0) {
                        memberList.add(username);
                    }
                }
            }
        }

        return memberList;
    }


    /**
     * Update a profile.
     * @param username The username of the user to update
     * @param fields JSON encoded fields to update
     * @throws AlfrescoException On any Alfresco error
     */
    public void updateProfile(final String username, final JSONObject fields)
            throws AlfrescoException {

        // encode username and add to JSON

        try {
        	fields.put("userName", username);
        } catch (JSONException e) {
            throw new AlfrescoException("Cannot update profile", e);
        }

        doHttpPost(alfrescoUrlProfile, fields);
    }

    /**
     * Update a Records Management Constraint in Alfresco.
     * @param constraint The name of the constraint type
     * @param group The name of the constraint group
     * @param users List of usernames to put in the constraint group (overwriting the current contents)
     * @throws AlfrescoException On any Alfresco error
     */
    public void updateRmConstraint(final String constraint, final String group,
            final Collection<String> users) throws AlfrescoException {
        String url = alfrescoUrlRmConstraint + constraint + "/values";

        JSONArray userNames = new JSONArray();

        Iterator<String> userIter = users.iterator();

        while (userIter.hasNext()) {
            String user = userIter.next();
            userNames.put(user);
        }

        JSONObject request = new JSONObject();

        try {
            JSONObject obj = new JSONObject();
            obj.put("value", group);
            obj.put("authorities", userNames);

            JSONArray values = new JSONArray();
            values.put(obj);

            request.put("values", values);
        } catch (JSONException e) {
            throw new AlfrescoException("Cannot update RM constraints", e);
        }

        doHttpPost(url, request);
    }

    /**
     * Visit a URL using an HTTP GET and parse out a JSON array from the response.
     * @param url URL to visit
     * @return The JSON array response
     * @throws AlfrescoException On any HTTP error
     */
    private JSONArray doHttpGetArray(final String url) throws AlfrescoException {
        HttpGet get = new HttpGet(url);

        HttpResponse response;

        try {
            response = client.execute(get);
        } catch (ClientProtocolException e) {
            throw new AlfrescoException("Failed on HTTP GET", e);
        } catch (IOException e) {
            throw new AlfrescoException("Failed on HTTP GET", e);
        }

        return getJsonArrayFromResponse(response);
    }
    
    private JSONObject doHttpGet(final String url) throws AlfrescoException {
    	HttpGet get = new HttpGet(url);
    	
    	HttpResponse response;
    	
        try {
            response = client.execute(get);
        } catch (ClientProtocolException e) {
            throw new AlfrescoException("Failed on HTTP GET", e);
        } catch (IOException e) {
            throw new AlfrescoException("Failed on HTTP GET", e);
        }

        return getJsonObjectFromResponse(response);
    }

    /**
     * POST a JSON object to a URL and parse out a JSON object from the response.
     * @param url URL to post to
     * @param json The JSON object to POST
     * @return The JSON response
     * @throws AlfrescoException On any HTTP error
     */
    private JSONObject doHttpPost(final String url, final JSONObject json)
            throws AlfrescoException {

        // debugging
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Posting JSON to "+url);

            try {
                final int indent = 4;
                LOGGER.debug(json.toString(indent));
            } catch (JSONException e) {
                LOGGER.debug("Can't output JSON");
            }
        }

        StringEntity jsonEnt;

        try {
            jsonEnt = new StringEntity(json.toString());
        } catch (UnsupportedEncodingException e) {
            throw new AlfrescoException("Failed on HTTP POST", e);
        }

        return doHttpPost(url, jsonEnt);
    }

    /**
     * POST a JSON array to a URL and parse out a JSON object from the response.
     * @param url URL to post to
     * @param json The JSON object to POST
     * @return The JSON response
     * @throws AlfrescoException On any HTTP error
     */
    private JSONObject doHttpPost(final String url, final JSONArray json)
            throws AlfrescoException {

        // debugging
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Posting JSON to "+url);

            try {
                final int indent = 4;
                LOGGER.debug(json.toString(indent));
            } catch (JSONException e) {
                LOGGER.debug("Can't output JSON");
            }
        }

        StringEntity jsonEnt;

        try {
            jsonEnt = new StringEntity(json.toString());
        } catch (UnsupportedEncodingException e) {
            throw new AlfrescoException("Failed on HTTP POST", e);
        }

        return doHttpPost(url, jsonEnt);
    }

    /**
     * POST name value pairs to a URL using JSON encoding and parse out a JSON object from the response.
     * @param url URL to post to
     * @param jsonEnt The name value pairs to post
     * @return The JSON response
     * @throws AlfrescoException On any HTTP error
     */
    private JSONObject doHttpPost(final String url, final StringEntity jsonEnt)
            throws AlfrescoException {
        HttpPost post = new HttpPost(url);
        post.setEntity(jsonEnt);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");

        // post it
        HttpResponse response;
        try {
            response = client.execute(post);
        } catch (ClientProtocolException e) {
            throw new AlfrescoException("Failed on HTTP POST", e);
        } catch (IOException e) {
            throw new AlfrescoException("Failed on HTTP POST", e);
        }

        // check status code and content of response
        return getJsonObjectFromResponse(response);
    }
    
    /**
     * PUT name value pairs to a URL using JSON encoding and parse out a JSON object from the response.
     * @param url URL to PUT to
     * @param jsonEnt The name value pairs to PUT
     * @return The JSON response
     * @throws AlfrescoException On any HTTP error
     */
    private JSONObject doHttpPut(final String url, final StringEntity jsonEnt)
            throws AlfrescoException {
        HttpPut put = new HttpPut(url);
        put.setEntity(jsonEnt);
        put.setHeader("Accept", "application/json");
        put.setHeader("Content-type", "application/json");

        // post it
        HttpResponse response;
        try {
            response = client.execute(put);
        } catch (ClientProtocolException e) {
            throw new AlfrescoException("Failed on HTTP PUT", e);
        } catch (IOException e) {
            throw new AlfrescoException("Failed on HTTP PUT", e);
        }

        // check status code and content of response
        return getJsonObjectFromResponse(response);
    }
    
    /**
     * PUT a JSON object to a URL and parse out a JSON object from the response.
     * @param url URL to post to
     * @param json The JSON object to PUT
     * @return The JSON response
     * @throws AlfrescoException On any HTTP error
     */
    private JSONObject doHttpPut(final String url, final JSONObject json)
            throws AlfrescoException {

        // debugging
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("PUTting JSON to "+url);

            try {
                final int indent = 4;
                LOGGER.debug(json.toString(indent));
            } catch (JSONException e) {
                LOGGER.debug("Can't output JSON");
            }
        }

        StringEntity jsonEnt;

        try {
            jsonEnt = new StringEntity(json.toString());
        } catch (UnsupportedEncodingException e) {
            throw new AlfrescoException("Failed on HTTP PUT", e);
        }

        return doHttpPut(url, jsonEnt);
    }

    /**
     * Visit a URL using an HTTP DELETE and parse out a JSON object from the response.
     * @param url URL to delete
     * @return The JSON response
     * @throws AlfrescoException On any HTTP error
     */
    private JSONObject doHttpDelete(final String url) throws AlfrescoException {
        // construct entity to post
        HttpDelete delete = new HttpDelete(url);

        // send it
        HttpResponse response;
        try {
            response = client.execute(delete);
        } catch (ClientProtocolException e) {
            throw new AlfrescoException("Failed on HTTP DELETE", e);
        } catch (IOException e) {
            throw new AlfrescoException("Failed on HTTP DELETE", e);
        }

        // check status code and content of response
        return getJsonObjectFromResponse(response);
    }

    /**
     * Parse out a JSON object from an HTTP response.
     * @param response The response from an HTTP account
     * @return The JSON response
     * @throws AlfrescoException If the HTTP response had an error or did not contain valid JSON
     */
    private JSONObject getJsonObjectFromResponse(final HttpResponse response)
            throws AlfrescoException {
        // check status code and content of response

        StatusLine status = response.getStatusLine();
        HttpEntity responseEnt = response.getEntity();

        JSONObject jsonResponse = null;

        if (responseEnt != null) {
            try {
                jsonResponse = new JSONObject(new JSONTokener(
                        new InputStreamReader(responseEnt.getContent())));
            } catch (JSONException e) {
                jsonResponse = null;
            } catch (IOException e) {
                throw new AlfrescoException("Failed parsing HTTP response", e);
            }

            // make sure response buffer is flushed so we can use the client
            // again
            flushBuffer(responseEnt);

            // debugging
            if (jsonResponse != null && LOGGER.isDebugEnabled()) {
                LOGGER.debug("Alfresco response:");

                try {
                    final int indent = 4;
                    LOGGER.debug(jsonResponse.toString(indent));
                } catch (JSONException e) {
                    LOGGER.debug("Can't output JSON");
                }
            }
        }

        // if any errors throw an exception with error message

        if (status == null) {
            throw new AlfrescoException(
                    "Alfresco action failed to return a status");
        } else if (jsonResponse == null) {
            throw new AlfrescoException("Alfresco action failed: "
                    + status.getStatusCode() + ": " + status.getReasonPhrase());
        } else if (status.getStatusCode() >= HTTP_ERROR_CODE) {
            // if the action failed then Alfresco may have given us an error
            // message
            String message = jsonResponse.optString("message");

            throw new AlfrescoException("Alfresco action failed: "
                    + status.getStatusCode() + ": " + status.getReasonPhrase()
                    + " \"" + message + "\"");
        }

        return jsonResponse;
    }

    /**
     * Parse out a JSON array from an HTTP response.
     * @param response The response from an HTTP account
     * @return The JSON response
     * @throws AlfrescoException If the HTTP response had an error or did not contain valid JSON
     */
    private JSONArray getJsonArrayFromResponse(final HttpResponse response)
            throws AlfrescoException {
        // check status code and content of response

        StatusLine status = response.getStatusLine();
        HttpEntity responseEnt = response.getEntity();

        JSONArray jsonResponse = null;

        if (responseEnt != null) {
            try {
                jsonResponse = new JSONArray(new JSONTokener(
                        new InputStreamReader(responseEnt.getContent())));
            } catch (JSONException e) {
                jsonResponse = null;
            } catch (IOException e) {
                throw new AlfrescoException("Failed parsing HTTP response", e);
            }

            // make sure response buffer is flushed so we can use the client
            // again
            flushBuffer(responseEnt);

            // debugging
            if (jsonResponse != null && LOGGER.isDebugEnabled()) {
                LOGGER.debug("Alfresco response:");

                try {
                    final int indent = 4;
                    LOGGER.debug(jsonResponse.toString(indent));
                } catch (JSONException e) {
                    LOGGER.debug("Can't output JSON");
                }
            }
        }

        // if any errors throw an exception with error message

        if (status == null) {
            throw new AlfrescoException(
                    "Alfresco action failed to return a status");
        } else if (jsonResponse == null || status.getStatusCode() >= HTTP_ERROR_CODE) {
            throw new AlfrescoException("Alfresco action failed: "
                    + status.getStatusCode() + ": " + status.getReasonPhrase());
        }

        return jsonResponse;
    }

    /**
     * Flush out any unused content in an HTTP entity (otherwise the HTTP Client cannot be re-used).
     * @param ent The HTTP entity to flush
     * @throws AlfrescoException If the flush failed
     */
    private void flushBuffer(final HttpEntity ent) throws AlfrescoException {
        try {
            ent.consumeContent();
        } catch (IOException e) {
            throw new AlfrescoException("Cannot flush HTTP buffer", e);
        }
    }
}
