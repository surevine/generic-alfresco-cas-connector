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
package com.surevine.alfresco.connector.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.surevine.alfresco.AlfrescoException;

/**
 * @author richardm
 */
public class AlfrescoHttpResponse {
	
	/**
	 * Logger.
	 */
	private final Logger LOG = Logger.getLogger(AlfrescoHttpResponse.class);

	/**
	 * Starting value of HTTP error codes.
	 */
	private static final int HTTP_ERROR_CODE = 400;

	/**
	 * An HTTP response as provided by the underlying Apache library.
	 */
	private HttpResponse response;
	
	/**
	 * @return The underlying {@link HttpResponse} element.
	 */
	public HttpResponse getHttpResponse() {
		return response;
	}
	
	/**
	 * @return The status code of the underlying HttpResponse.
	 */
	public int getStatusCode() {
		return getHttpResponse().getStatusLine().getStatusCode();
	}
	
	/**
	 * Our constructor.
	 * 
	 * Attempts to provide an immutable wrapper.
	 * 
	 * @param response The HTTP response provided by the underlying Apache library.
	 */
	public AlfrescoHttpResponse(final HttpResponse response) {
		this.response = response;
	}
	
	/**
	 * Parse out a JSON array from an HTTP response.
	 * 
	 * @param response
	 *          The response from an HTTP account
	 * @return The JSON response
	 * @throws AlfrescoException
	 *           If the HTTP response had an error or did not contain valid JSON
	 */
	public JSONArray asJsonArray()
			throws AlfrescoException
	{
		// check status code and content of response
		final StatusLine status = response.getStatusLine();
		final HttpEntity responseEnt = response.getEntity();

		JSONArray jsonResponse = null;

		if (responseEnt != null) {
			try {
				jsonResponse = new JSONArray(new JSONTokener(new InputStreamReader(
						responseEnt.getContent())));
			} catch (final JSONException e) {
				jsonResponse = null;
			} catch (final IOException e) {
				throw new AlfrescoException("Failed parsing HTTP response", e);
			}

			// make sure response buffer is flushed so we can use the client
			// again
			flushBuffer(responseEnt);

			// debugging
			if(LOG.isDebugEnabled()) {
				LOG.debug("Alfresco response: " + jsonResponse);
			}
		}

		// if any errors throw an exception with error message

		if (status == null) {
			throw new AlfrescoException("Alfresco action failed to return a status");
		} else if (jsonResponse == null
				|| status.getStatusCode() >= HTTP_ERROR_CODE) {
			throw new AlfrescoException("Alfresco action failed: "
					+ status.getStatusCode() + ": " + status.getReasonPhrase());
		}

		return jsonResponse;
	}

	/**
	 * Parse out a JSON object from an HTTP response.
	 * 
	 * @return The JSON response
	 * @throws AlfrescoException
	 *           If the HTTP response had an error or did not contain valid JSON
	 */
	public JSONObject asJsonObject()
			throws AlfrescoException
	{
		// check status code and content of response

		final StatusLine status = response.getStatusLine();
		final HttpEntity responseEnt = response.getEntity();

		JSONObject jsonResponse = null;

		if (responseEnt != null) {
			try {
				jsonResponse = new JSONObject(new JSONTokener(new InputStreamReader(
						responseEnt.getContent())));
			} catch (final JSONException e) {
				jsonResponse = null;
			} catch (final IOException e) {
				throw new AlfrescoException("Failed parsing HTTP response", e);
			}

			// make sure response buffer is flushed so we can use the client
			// again
			flushBuffer(responseEnt);

			// debugging
			if(LOG.isDebugEnabled()) {
				LOG.debug("Alfresco response: " + jsonResponse);
			}
		}

		// if any errors throw an exception with error message

		if (status == null) {
			throw new AlfrescoException("Alfresco action failed to return a status");
		} else if (jsonResponse == null) {
			throw new AlfrescoException("Alfresco action failed: "
					+ status.getStatusCode() + ": " + status.getReasonPhrase());
		} else if (status.getStatusCode() >= HTTP_ERROR_CODE) {
			// if the action failed then Alfresco may have given us an error
			// message
			final String message = jsonResponse.optString("message");

			throw new AlfrescoException("Alfresco action failed: "
					+ status.getStatusCode() + ": " + status.getReasonPhrase() + " \""
					+ message + "\"");
		}

		return jsonResponse;
	}
	
	public String asString() throws AlfrescoException {
		final HttpEntity responseEnt = response.getEntity();
		final StatusLine status = response.getStatusLine();
		
		if (status == null) {
			throw new AlfrescoException("Alfresco action failed to return a status");
		} else if (status.getStatusCode() >= HTTP_ERROR_CODE) {
			throw new AlfrescoException("Alfresco action failed: "
					+ status.getStatusCode() + ": " + status.getReasonPhrase() + "\"");
		}
		
		final StringBuilder sb = new StringBuilder();

		try {
			if (responseEnt != null) {
				String line;
				BufferedReader reader;
					reader = new BufferedReader(new InputStreamReader(responseEnt.getContent()));
		
					while ((line = reader.readLine()) != null) {
						sb.append(line);
						sb.append('\n');
					}
			}
		} catch (final IllegalStateException e) {
			throw new AlfrescoException("Alfresco failed to read the response", e);
		} catch (final IOException e) {
			throw new AlfrescoException("Alfresco failed to read the response", e);
		}

		return sb.toString();
	}
	
	/**
	 * Flush out any unused content in an HTTP entity (otherwise the HTTP Client
	 * cannot be re-used).
	 * 
	 * @param ent
	 *          The HTTP entity to flush
	 * @throws AlfrescoException
	 *           If the flush failed
	 */
	private void flushBuffer(final HttpEntity ent) throws AlfrescoException
	{
		try {
			ent.consumeContent();
		} catch (final IOException e) {
			throw new AlfrescoException("Cannot flush HTTP buffer", e);
		}
	}
}
