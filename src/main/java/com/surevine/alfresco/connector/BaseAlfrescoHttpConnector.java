/**
 * 
 */
package com.surevine.alfresco.connector;

import java.io.IOException;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.surevine.alfresco.AlfrescoException;
import com.surevine.alfresco.Authenticator;
import com.surevine.alfresco.CasAuthenticator;
import com.surevine.alfresco.PropertyWrapper;
import com.surevine.alfresco.connector.model.AlfrescoHttpResponse;


/**
 * Handles HTTP connections to Alfresco.
 * 
 * This includes CAS logins but is ignorant of the resulting format.
 * 
 * It does however return a {@link AlfrescoHttpResponse} instances from which
 * you can request the response in your desired format model.
 * 
 * @author richardm
 */
public abstract class BaseAlfrescoHttpConnector {

	private final Logger LOG = Logger.getLogger(BaseAlfrescoHttpConnector.class);
	
	/**
	 * Client for connecting to websites.
	 */
	private final HttpClient client;

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
	public BaseAlfrescoHttpConnector(final PropertyWrapper properties,
			final Authenticator auth, final HttpClient httpClient)
			throws AlfrescoException
	{
		// set the HTTP client
		client = httpClient;

		// login
		if (!auth.authenticate(client)) {
			throw new AlfrescoException("Cannot log into Alfresco");
		}
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
	protected AlfrescoHttpResponse doHttpGet(final String url) throws AlfrescoException
	{
		final HttpGet request = new HttpGet(url);

		return fetch(request);
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
	protected AlfrescoHttpResponse doHttpGetArray(final String url) throws AlfrescoException
	{
		final HttpGet request = new HttpGet(url);

		return fetch(request);
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
	protected AlfrescoHttpResponse doHttpDelete(final String url) throws AlfrescoException
	{
		// construct entity to post
		final HttpDelete request = new HttpDelete(url);

		return fetch(request);
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
	protected AlfrescoHttpResponse doHttpPost(final String url, final StringEntity jsonEnt)
			throws AlfrescoException
	{
		final HttpPost request = new HttpPost(url);
		request.setEntity(jsonEnt);
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-type", "application/json");

		return fetch(request);
	}

	/**
	 * POST multipart form parts to a URL using JSON encoding and parse out a JSON
	 * object from the response.
	 * 
	 * @param url
	 *          URL to post to
	 * @param content
	 *          The String value of the post request
	 * @return The JSON response
	 * @throws AlfrescoException
	 *           On any HTTP error
	 */
	protected AlfrescoHttpResponse doHttpPost(final String url, final Map<String, ContentBody> parts)
			throws AlfrescoException
	{
		final HttpPost request = new HttpPost(url);
		final MultipartEntity entity = new MultipartEntity();
		
		for (final String name : parts.keySet()) {
			entity.addPart(name, parts.get(name));
		}
		
		request.setEntity(entity);

		return fetch(request);
	}
	
	/**
	 * This should be the only place our connectors exercise an {@link HttpUriRequest}.
	 * 
	 * @param request The URI we want to retrieve.
	 * @return A wrapped HttpResponse allowing us to request Strings, JSON Objects, Arrays, etc.
	 * @throws AlfrescoException On error.
	 */
	private AlfrescoHttpResponse fetch(final HttpUriRequest request) throws AlfrescoException {
		try {
			return new AlfrescoHttpResponse(client.execute(request));
		} catch (final ClientProtocolException e) {
			throw new AlfrescoException("Failed on HTTP " + request.getMethod(), e);
		} catch (final IOException e) {
			throw new AlfrescoException("Failed on HTTP " + request.getMethod(), e);
		}
	}
}
