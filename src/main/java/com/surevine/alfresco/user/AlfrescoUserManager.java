package com.surevine.alfresco.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.surevine.alfresco.AlfrescoException;
import com.surevine.alfresco.connector.IAlfrescoConnector;
import com.surevine.alfresco.dashboard.DashboardDefinition;

/**
 * A facade for the user management scripts in alfresco
 */
public class AlfrescoUserManager {
	private static final String SERVICE_API_PEOPLE = "api/people";
	private static final String SERVICE_SHARE_SET_DASHBOARD = "sv-theme/set-dashboard";

	/**
	 * The AlfrescoConnector to use for communication
	 */
	final protected IAlfrescoConnector connector;

	/**
	 * Constructor
	 * 
	 * @param connector
	 *            the {@link IAlfrescoConnector} to use for communication
	 */
	public AlfrescoUserManager(final IAlfrescoConnector connector) {
		this.connector = connector;
	}

	/**
	 * Returns a {@link JSONObject} containing an array of all the usernames
	 * within the database
	 * 
	 * @param filter
	 *            an optional string filter
	 * @return the {@link JSONObject} of the array of usernames
	 * @throws AlfrescoException
	 * @throws JSONException
	 */
	public JSONArray getPeople(final String filter) throws AlfrescoException,
			JSONException {
		final HashMap<String, String> parameters = new HashMap<String, String>();

		if (filter != null) {
			parameters.put("filter", filter);
		}

		JSONObject result = connector.doGet(SERVICE_API_PEOPLE, parameters);

		return result.getJSONArray("people");
	}

	/**
	 * Updates the user's dashboard to the supplied definition.
	 * 
	 * @param username
	 *            the username of the user to update.
	 * @param dashboardDef
	 *            the dashboard definition to update the user's dashboard to.
	 * @throws JSONException 
	 */
	public void setUserDashboard(final String username,
			final DashboardDefinition dashboardDef) throws AlfrescoException {
		
		// Construct the request JSON
		JSONObject request = new JSONObject();
		
		try {
			request.put("dashboardPage", "user/" + username + "/dashboard");
			request.put("templateId", dashboardDef.getTemplateId());
			
			Map<String, List<String>> dashletDefs = dashboardDef.getDashlets();
			
			for(Entry<String,List<String>> entry : dashletDefs.entrySet()) {
				int column = Integer.valueOf(entry.getKey()) + 1;
				
				int row = 0;
				
				for( String dashletUrl : entry.getValue()) {
					++row;
					
					JSONObject dashlet = new JSONObject();
					
					dashlet.put("regionId", "component-" + column + "-" + row);
					dashlet.put("url", dashletUrl);
					
					request.append("dashlets", dashlet);
				}
			}
		} catch(JSONException eJSON) {
			throw new AlfrescoException("An error occurred creating the json request", eJSON);
		}
		
		JSONObject result = connector.doSharePost(SERVICE_SHARE_SET_DASHBOARD, request);
		
		try {
			if(!result.getBoolean("success")) {
				throw new AlfrescoException(SERVICE_SHARE_SET_DASHBOARD + " did not return success");
			}
		} catch( JSONException eJSON) {
			throw new AlfrescoException(SERVICE_SHARE_SET_DASHBOARD + " did not return a valid response", eJSON);
		}
	}
}
