/*
 * Copyright (C) 2010 Surevine Ltd.
 *
 * All rights reserved.
 */

package com.surevine.alfresco.connector;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.surevine.alfresco.AlfrescoException;
import com.surevine.alfresco.Authenticator;
import com.surevine.alfresco.PropertyException;
import com.surevine.alfresco.PropertyWrapper;

/**
 * Connects to alfresco using the RESTful http services
 */
public class SimpleAlfrescoHttpConnector
{
	private final Logger LOG = Logger.getLogger(AlfrescoHttpConnector.class);

	/**
	 * Starting value of HTTP error codes.
	 */
	private static final int HTTP_ERROR_CODE = 400;

	/**
	 * Client for connecting to websites.
	 */
	private final HttpClient client;

	/**
	 * URL for Alfresco API for manipulating sites.
	 */
	private final String alfrescoServiceBaseUrl;

	/**
	 * URL for Alfresco API for manipulating Records Management Constraints.
	 */
	private final String alfrescoShareServiceBaseUrl;

	/**
	 * @param properties
	 *          Contains configuration for the Alfresco server to connect to
	 * @param auth
	 *          A mechanism for authenticating the connection to Alfresco
	 * @param httpClient
	 *          the {@link HttpClient} to use for communication
	 * @throws AlfrescoException
	 *           If required properties are not present or if authentication fails
	 */
	public SimpleAlfrescoHttpConnector(final PropertyWrapper properties,
			final Authenticator auth, final HttpClient httpClient)
			throws AlfrescoException
	{
		try {
			// read and save properties needed elsewhere
			alfrescoServiceBaseUrl = properties.getProperty("alfresco.url.service");
			alfrescoShareServiceBaseUrl = properties
					.getProperty("alfresco.share.url.service");
		} catch (final PropertyException e) {
			throw new AlfrescoException("Cannot find a required property", e);
		}

		// set the HTTP client
		client = httpClient;

		// login
		if (!auth.authenticate(client)) {
			throw new AlfrescoException("Cannot log into Alfresco");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void doDelete(final String service) throws AlfrescoException
	{
		doHttpDelete(createAlfrescoServiceUrl(service, null));
	}

	/**
	 * {@inheritDoc}
	 */
	public void doGet(final String service) throws AlfrescoException
	{
		doHttpGet(createAlfrescoServiceUrl(service, null));
	}

	/**
	 * {@inheritDoc}
	 */
	public void doGet(final String service, final Map<String, String> parameters)
			throws AlfrescoException
	{
		doHttpGet(createAlfrescoServiceUrl(service, parameters));
	}

	/**
	 * {@inheritDoc}
	 */
	public void doGetArray(final String service) throws AlfrescoException
	{
		doHttpGetArray(createAlfrescoServiceUrl(service, null));
	}

	/**
	 * {@inheritDoc}
	 */
	public void doGetArray(final String service, final Map<String, String> parameters)
			throws AlfrescoException
	{
		doHttpGetArray(createAlfrescoServiceUrl(service, parameters));
	}

	/**
	 * {@inheritDoc}
	 */
	public void doPost(final String service, final JSONObject json)
			throws AlfrescoException
	{
		doHttpPost(createAlfrescoServiceUrl(service, null), json);
	}

	/**
	 * {@inheritDoc}
	 */
	public void doShareDelete(final String service) throws AlfrescoException
	{
		doHttpDelete(createShareServiceUrl(service, null));
	}

	/**
	 * {@inheritDoc}
	 */
	public void doShareGet(final String service) throws AlfrescoException
	{
		doHttpGet(createShareServiceUrl(service, null));
	}

	/**
	 * {@inheritDoc}
	 */
	public void doShareGet(final String service, final Map<String, String> parameters)
			throws AlfrescoException
	{
		doHttpGet(createShareServiceUrl(service, parameters));
	}

	/**
	 * {@inheritDoc}
	 */
	public void doShareGetArray(final String service) throws AlfrescoException
	{
		doHttpGetArray(createShareServiceUrl(service, null));
	}

	/**
	 * {@inheritDoc}
	 */
	public void doShareGetArray(final String service,
			final Map<String, String> parameters) throws AlfrescoException
	{
		doHttpGetArray(createShareServiceUrl(service, parameters));
	}

	/**
	 * {@inheritDoc}
	 */
	public void doSharePost(final String service, final JSONObject json)
			throws AlfrescoException
	{
		 doHttpGet(createShareServiceUrl(service, null));
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
		
		for(final String key : parameters.keySet()) {
			if(questionMark) {
				output.append("?");
				questionMark = false;
			} else {
				output.append("&");
			}
			
			try {
				output.append(URLEncoder.encode(key, "UTF-8"));
				output.append("=");
				output.append(parameters.get(key));
			} catch (final UnsupportedEncodingException eUE) {
				LOG.error(eUE.getMessage(), eUE);
			}
		}
		
		return output.toString();
	}
	
	/**
	 * Visit a URL using an HTTP GET and parse out a JSON array from the response.
	 * 
	 * @param url
	 *          URL to visit
	 * @return The JSON array response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	private void doHttpGetArray(final String url) throws AlfrescoException
	{
		final HttpGet get = new HttpGet(url);

		HttpResponse response;

		try {
			response = client.execute(get);
		} catch (final ClientProtocolException e) {
			throw new AlfrescoException("Failed on HTTP GET", e);
		} catch (final IOException e) {
			throw new AlfrescoException("Failed on HTTP GET", e);
		}

		return;
	}
	
	/**
	 * Visit a URL using an HTTP GET and parse out a JSON object from the response.
	 * 
	 * @param url
	 *          URL to visit
	 * @return The JSON object response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	private void doHttpGet(final String url) throws AlfrescoException
	{
		final HttpGet get = new HttpGet(url);

		HttpResponse response;

		try {
			response = client.execute(get);
		} catch (final ClientProtocolException e) {
			throw new AlfrescoException("Failed on HTTP GET", e);
		} catch (final IOException e) {
			throw new AlfrescoException("Failed on HTTP GET", e);
		}

		return;
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
	private void doHttpPost(final String url, final JSONObject json)
			throws AlfrescoException
	{
		StringEntity jsonEnt;

		try {
			jsonEnt = new StringEntity(json.toString());
		} catch (final UnsupportedEncodingException e) {
			throw new AlfrescoException("Failed on HTTP POST", e);
		}

		doHttpPost(url, jsonEnt);
	}

	/**
	 * POST name value pairs to a URL using JSON encoding and parse out a JSON
	 * object from the response.
	 * 
	 * @param url
	 *          URL to post to
	 * @param jsonEnt
	 *          The name value pairs to post
	 * @return The JSON response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	private void doHttpPost(final String url, final StringEntity jsonEnt)
			throws AlfrescoException
	{
		final HttpPost post = new HttpPost(url);
		post.setEntity(jsonEnt);
		post.setHeader("Accept", "application/json");
		post.setHeader("Content-type", "application/json");

		// post it
		HttpResponse response;
		try {
			response = client.execute(post);
		} catch (final ClientProtocolException e) {
			throw new AlfrescoException("Failed on HTTP POST", e);
		} catch (final IOException e) {
			throw new AlfrescoException("Failed on HTTP POST", e);
		}

		// check status code and content of response
		return;
	}

	/**
	 * Visit a URL using an HTTP DELETE and parse out a JSON object from the
	 * response.
	 * 
	 * @param url
	 *          URL to delete
	 * @return The JSON response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	private void doHttpDelete(final String url) throws AlfrescoException
	{
		// construct entity to post
		final HttpDelete delete = new HttpDelete(url);

		// send it
		HttpResponse response;
		try {
			response = client.execute(delete);
		} catch (final ClientProtocolException e) {
			throw new AlfrescoException("Failed on HTTP DELETE", e);
		} catch (final IOException e) {
			throw new AlfrescoException("Failed on HTTP DELETE", e);
		}

		// check status code and content of response
		return;
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
