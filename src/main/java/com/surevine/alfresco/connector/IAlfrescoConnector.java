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
