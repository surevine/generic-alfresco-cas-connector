/*
 * Copyright (C) 2010 Surevine Ltd.
 *
 * All rights reserved.
 */

package com.surevine.alfresco.connector;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.surevine.alfresco.AlfrescoException;
import com.surevine.alfresco.Authenticator;
import com.surevine.alfresco.PropertyException;
import com.surevine.alfresco.PropertyWrapper;

/**
 * Connects to alfresco using the RESTful http services.
 * 
 * Should be largely ignorant of the underlying HTTP library.
 */
public class AlfrescoHttpConnector extends BaseAlfrescoHttpConnector implements IAlfrescoConnector
{
	
	private final Logger LOG = Logger.getLogger(AlfrescoHttpConnector.class);

	/**
	 * URL for Alfresco API for manipulating sites.
	 */
	protected final String alfrescoServiceBaseUrl;

	/**
	 * URL for Alfresco API for manipulating Records Management Constraints.
	 */
	protected final String alfrescoShareServiceBaseUrl;

	/**
	 * {@inheritDoc}
	 */
	public AlfrescoHttpConnector(final PropertyWrapper properties,
			final Authenticator auth, final HttpClient httpClient)
			throws AlfrescoException
	{
		super(properties, auth, httpClient);
		
		try {
			// read and save properties needed elsewhere
			alfrescoServiceBaseUrl = properties.getProperty("alfresco.url.service");
			alfrescoShareServiceBaseUrl = properties
					.getProperty("alfresco.share.url.service");
		} catch (final PropertyException e) {
			throw new AlfrescoException("Cannot find a required property", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public JSONObject doDelete(final String service) throws AlfrescoException
	{
		return doHttpDelete(createAlfrescoServiceUrl(service, null)).asJsonObject();
	}

	/**
	 * {@inheritDoc}
	 */
	public JSONObject doGet(final String service) throws AlfrescoException
	{
		return doHttpGet(createAlfrescoServiceUrl(service, null)).asJsonObject();
	}

	/**
	 * {@inheritDoc}
	 */
	public JSONObject doGet(final String service, final Map<String, String> parameters)
			throws AlfrescoException
	{
		return doHttpGet(createAlfrescoServiceUrl(service, parameters)).asJsonObject();
	}

	/**
	 * {@inheritDoc}
	 */
	public JSONArray doGetArray(final String service) throws AlfrescoException
	{
		return doHttpGetArray(createAlfrescoServiceUrl(service, null)).asJsonArray();
	}

	/**
	 * {@inheritDoc}
	 */
	public JSONArray doGetArray(final String service, final Map<String, String> parameters)
			throws AlfrescoException
	{
		return doHttpGetArray(createAlfrescoServiceUrl(service, parameters)).asJsonArray();
	}

	/**
	 * {@inheritDoc}
	 */
	public JSONObject doPost(final String service, final JSONObject json)
			throws AlfrescoException
	{
		return doHttpPost(createAlfrescoServiceUrl(service, null), json);
	}

	/**
	 * {@inheritDoc}
	 */
	public JSONObject doShareDelete(final String service) throws AlfrescoException
	{
		return doHttpDelete(createShareServiceUrl(service, null)).asJsonObject();
	}

	/**
	 * {@inheritDoc}
	 */
	public JSONObject doShareGet(final String service) throws AlfrescoException
	{
		return doHttpGet(createShareServiceUrl(service, null)).asJsonObject();
	}

	/**
	 * {@inheritDoc}
	 */
	public JSONObject doShareGet(final String service, final Map<String, String> parameters)
			throws AlfrescoException
	{
		return doHttpGet(createShareServiceUrl(service, parameters)).asJsonObject();
	}

	/**
	 * {@inheritDoc}
	 */
	public JSONArray doShareGetArray(final String service) throws AlfrescoException
	{
		return doHttpGetArray(createShareServiceUrl(service, null)).asJsonArray();
	}

	/**
	 * {@inheritDoc}
	 */
	public JSONArray doShareGetArray(final String service,
			final Map<String, String> parameters) throws AlfrescoException
	{
		return doHttpGetArray(createShareServiceUrl(service, parameters)).asJsonArray();
	}

	/**
	 * {@inheritDoc}
	 */
	public JSONObject doSharePost(final String service, final JSONObject json)
			throws AlfrescoException
	{
		return doHttpPost(createShareServiceUrl(service, null), json);
	}	
	
	/**
	 * Creates the full url to access the given alfresco service
	 * @param service the service to access.
	 * @param parameters the url parameters.
	 * @return the url.
	 */
	private String createAlfrescoServiceUrl(final String service, final Map<String,String> parameters) {
		return addUrlParameters(alfrescoServiceBaseUrl + service, parameters);
	}
	
	/**
	 * Creates the full url to access the given alfresco share service
	 * @param service the service to access.
	 * @param parameters the url parameters.
	 * @return the url.
	 */
	private String createShareServiceUrl(final String service, final Map<String,String> parameters) {
		return addUrlParameters(alfrescoShareServiceBaseUrl + service, parameters);
	}
	
	/**
	 * Adds the get paramters to a string url
	 */
	private String addUrlParameters(final String url, final Map<String,String> parameters) {
		if(parameters == null) {
			return url;
		}
		
		boolean questionMark = !url.contains("?");
		
		final StringBuilder output = new StringBuilder(url);
		
		for(final Entry<String,String> entry : parameters.entrySet()) {
			if(questionMark) {
				output.append("?");
				questionMark = false;
			} else {
				output.append("&");
			}
			
			try {
				output.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				output.append("=");
				output.append(entry.getValue());
			} catch (final UnsupportedEncodingException eUE) {
				LOG.error(eUE.getMessage(), eUE);
			}
		}
		
		return output.toString();
	}
	
	/**
	 * POST a JSON object to a URL and parse out a JSON object from the response.
	 * 
	 * @param url
	 *          URL to post to
	 * @param json
	 *          The JSON object to POST
	 * @return The JSON response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	private JSONObject doHttpPost(final String url, final JSONObject json)
			throws AlfrescoException
	{
		StringEntity jsonEnt;

		try {
			jsonEnt = new StringEntity(json.toString());
		} catch (final UnsupportedEncodingException e) {
			throw new AlfrescoException("Failed on HTTP POST", e);
		}

		return doHttpPost(url, jsonEnt).asJsonObject();
	}
}
