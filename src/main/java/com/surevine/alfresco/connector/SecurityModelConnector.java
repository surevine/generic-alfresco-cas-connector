/**
 * 
 */
package com.surevine.alfresco.connector;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.surevine.alfresco.AlfrescoException;
import com.surevine.alfresco.Authenticator;
import com.surevine.alfresco.PropertyException;
import com.surevine.alfresco.PropertyWrapper;
import com.surevine.alfresco.connector.model.AlfrescoHttpResponse;

/**
 * @author richardm
 */
public class SecurityModelConnector extends BaseAlfrescoHttpConnector {

	private final Logger LOG = Logger.getLogger(SecurityModelConnector.class);
	
	private String alfrescoUrlBase;
	
	public SecurityModelConnector(PropertyWrapper properties, Authenticator auth)
			throws AlfrescoException {
		super(properties, auth, new DefaultHttpClient());
		
		try {
			this.alfrescoUrlBase = properties.getProperty("alfresco.url.base");
		} catch (final PropertyException e) {
			throw new AlfrescoException("Cannot find a required property", e);
		}
	}

    public AlfrescoHttpResponse getSecurityModel() throws AlfrescoException {
    	return doHttpGet(alfrescoUrlBase + "/wcs/surevine/security-model");
    }
    
    public void setSecurityModel(final String securityModel) throws AlfrescoException {
    	final Map<String, ContentBody> parts = new HashMap<String, ContentBody>();
    	
    	try {
			parts.put("updateNodeRef", new StringBody("workspace://SpacesStore/enhanced_security_custom_model"));
	    	parts.put("filedata", new InputStreamBody(new ByteArrayInputStream(securityModel.getBytes()),
	    			"text/xml", "enhancedSecurityCustomModel.xml"));
		} catch (final UnsupportedEncodingException e) {
			LOG.error("Failed to populate multipart form for updating security model.", e);
		}
    	
    	final AlfrescoHttpResponse response = doHttpPost(alfrescoUrlBase +"/wcservice/api/upload.html", parts);
    	
    	if (response.getHttpResponse().getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
    		throw new AlfrescoException("Update of security model failed with status code "
    				+ response.getHttpResponse().getStatusLine().getStatusCode());
    	}
    }
}
