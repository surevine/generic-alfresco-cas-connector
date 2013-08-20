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
package com.surevine.alfresco.connector;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.surevine.alfresco.AlfrescoException;

/**
 * Basic interface for classes which allow communication to Alfresco
 */
public interface IAlfrescoConnector
{
	/**
	 * Calls an alfresco service using an HTTP GET and parse out a JSON object
	 * from the response.
	 * 
	 * @param service
	 *          the service to call (e.g. "api/people")
	 * @return The JSON object response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	JSONObject doGet(String service) throws AlfrescoException;

	/**
	 * Calls an alfresco service using an HTTP GET and parse out a JSON object
	 * from the response.
	 * 
	 * @param service
	 *          the service to call (e.g. "api/people")
	 * @param parameters
	 *          the name/value pairs of parameters to pass to the webscript
	 * @return The JSON object response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	JSONObject doGet(String service, Map<String, String> parameters)
			throws AlfrescoException;

	/**
	 * Calls an alfresco service using an HTTP GET and parse out a JSON array from the response.
	 * 
	 * @param service
	 *          the service to call (e.g. "api/people")
	 * @return The JSON array response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	JSONArray doGetArray(String service) throws AlfrescoException;

	/**
	 * Calls an alfresco service using an HTTP GET and parse out a JSON array from the response.
	 * 
	 * @param service
	 *          the service to call (e.g. "api/people")
	 * @param parameters
	 *          the name/value pairs of parameters to pass to the webscript
	 * @return The JSON array response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	JSONArray doGetArray(String service, Map<String, String> parameters)
			throws AlfrescoException;

	/**
	 * POST a JSON object to an alfresco service and parse out a JSON object from the response.
	 * 
	 * @param service
	 *          the service to post to (e.g. "api/people")
	 * @param json
	 *          The JSON object to POST
	 * @return The JSON response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	JSONObject doPost(String service, JSONObject json) throws AlfrescoException;

	/**
	 * Calls an alfresco service using an HTTP DELETE and parse out a JSON object from the
	 * response.
	 * 
	 * @param service
	 *          the service to post to (e.g. "api/people")
	 * @return The JSON response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	JSONObject doDelete(String service) throws AlfrescoException;

	/**
	 * Calls an alfresco service using an HTTP GET and parse out a JSON object
	 * from the response.
	 * 
	 * @param service
	 *          the service to call (e.g. "api/people")
	 * @return The JSON object response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	JSONObject doShareGet(String service) throws AlfrescoException;

	/**
	 * Calls an alfresco service using an HTTP GET and parse out a JSON object
	 * from the response.
	 * 
	 * @param service
	 *          the service to call (e.g. "api/people")
	 * @param parameters
	 *          the name/value pairs of parameters to pass to the webscript
	 * @return The JSON object response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	JSONObject doShareGet(String service, Map<String, String> parameters)
			throws AlfrescoException;

	/**
	 * Calls an alfresco service using an HTTP GET and parse out a JSON array from the response.
	 * 
	 * @param service
	 *          the service to call (e.g. "api/people")
	 * @return The JSON array response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	JSONArray doShareGetArray(String service) throws AlfrescoException;

	/**
	 * Calls an alfresco service using an HTTP GET and parse out a JSON array from the response.
	 * 
	 * @param service
	 *          the service to call (e.g. "api/people")
	 * @param parameters
	 *          the name/value pairs of parameters to pass to the webscript
	 * @return The JSON array response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	JSONArray doShareGetArray(String service, Map<String, String> parameters)
			throws AlfrescoException;

	/**
	 * POST a JSON object to an alfresco share service and parse out a JSON object from the response.
	 * 
	 * @param service
	 *          the service to post to (e.g. "api/people")
	 * @param json
	 *          The JSON object to POST
	 * @return The JSON response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	JSONObject doSharePost(String service, final JSONObject json)
			throws AlfrescoException;

	/**
	 * Calls an alfresco service using an HTTP DELETE and parse out a JSON object from the
	 * response.
	 * 
	 * @param service
	 *          the service to post to (e.g. "api/people")
	 * @return The JSON response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	JSONObject doShareDelete(String service) throws AlfrescoException;
}
