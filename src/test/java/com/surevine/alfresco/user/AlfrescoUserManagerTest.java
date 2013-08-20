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
package com.surevine.alfresco.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.surevine.alfresco.AlfrescoException;
import com.surevine.alfresco.connector.IAlfrescoConnector;
import com.surevine.alfresco.dashboard.DashboardDefinition;
import com.surevine.alfresco.dashboard.DashboardDefinitionTest;

/**
 * Test case for the {@link AlfrescoUserManager} class
 */
public class AlfrescoUserManagerTest {
	/**
	 * The expected service for the setUserDashboard call
	 */
	static String SET_USER_DASHBOARD_SERVICE = "sv-theme/set-dashboard";

	/**
	 * The class under test
	 */
	AlfrescoUserManager alfrescoUserManager;

	/**
	 * The (mocked) {@link IAlfrescoConnector}
	 */
	@Mock
	IAlfrescoConnector alfrescoConnector;

	/**
	 * Initialise everything
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		alfrescoUserManager = new AlfrescoUserManager(alfrescoConnector);
	}

	/**
	 * Tear everything down
	 * 
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		alfrescoUserManager = null;
		alfrescoConnector = null;
	}

	/**
	 * Test the constructor
	 */
	@Test
	public void testConstructor() {
		alfrescoUserManager = new AlfrescoUserManager(alfrescoConnector);

		assertSame("Connector not initialised correctly", alfrescoConnector,
				alfrescoUserManager.connector);
	}

	/**
	 * Test the {@link AlfrescoUserManager#getAllPeople()} method
	 * 
	 * @throws AlfrescoException
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetPeopleWithFilter() throws AlfrescoException,
			JSONException {
		String testFilter = "test_filter";
		String expectedService = "api/people";

		// This will capture the parameters
		ArgumentCaptor<Map> parametersCaptor = ArgumentCaptor
				.forClass(Map.class);

		// This is the JSONObject which will be returned from the api call
		JSONObject result = new JSONObject();
		JSONArray expected = new JSONArray();
		result.put("people", expected);

		// When the connector call is made, return the json array
		when(alfrescoConnector.doGet(eq(expectedService), Matchers.anyMap()))
				.thenReturn(result);

		// Do the actual test call
		JSONArray actual = alfrescoUserManager.getPeople(testFilter);

		// Check that the correct connector method is called
		verify(alfrescoConnector).doGet(eq(expectedService),
				parametersCaptor.capture());

		Map parameters = parametersCaptor.getValue();

		assertTrue("Filter parameter not sent through", parameters
				.containsKey("filter"));
		assertEquals("Filter parameter not correct", testFilter, parameters
				.get("filter"));

		assertEquals("JSON array not sent through correctly", expected, actual);
	}

	/**
	 * Test the {@link AlfrescoUserManager#getAllPeople()} method
	 * 
	 * @throws AlfrescoException
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetPeopleWithoutFilter() throws AlfrescoException,
			JSONException {
		String expectedService = "api/people";

		// This will capture the parameters
		ArgumentCaptor<Map> parametersCaptor = ArgumentCaptor
				.forClass(Map.class);

		// This is the JSONObject which will be returned from the api call
		JSONObject result = new JSONObject();
		JSONArray expected = new JSONArray();
		result.put("people", expected);

		// When the connector call is made, return the json array
		when(alfrescoConnector.doGet(eq(expectedService), Matchers.anyMap()))
				.thenReturn(result);

		// Do the actual test call
		JSONArray actual = alfrescoUserManager.getPeople(null);

		// Check that the correct connector method is called
		verify(alfrescoConnector).doGet(eq(expectedService),
				parametersCaptor.capture());

		Map parameters = parametersCaptor.getValue();

		assertFalse("Filter parameter sent through when there is no filter",
				parameters.containsKey("filter"));

		assertEquals("JSON array not sent through correctly", expected, actual);
	}

	/**
	 * Test the {@link AlfrescoUserManager#getAllPeople()} method. It should
	 * pass on the AlfrescoException.
	 * 
	 * @throws AlfrescoException
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = AlfrescoException.class)
	public void testGetPeopleWithAlfrescoException() throws AlfrescoException,
			JSONException {
		String expectedService = "api/people";

		// When the connector call is made, throw an exception
		when(alfrescoConnector.doGet(eq(expectedService), Matchers.anyMap()))
				.thenThrow(new AlfrescoException("Alfresco call failed"));

		// Do the actual test call
		alfrescoUserManager.getPeople(null);
	}

	/**
	 * Test the {@link AlfrescoUserManager#getAllPeople()} method. It should
	 * raise a JSONException.
	 * 
	 * @throws AlfrescoException
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = JSONException.class)
	public void testGetPeopleWithIncorrectJSON() throws AlfrescoException,
			JSONException {
		String expectedService = "api/people";

		// This is the JSONObject which will be returned from the api call
		JSONObject result = new JSONObject();

		// When the connector call is made, return the json array
		when(alfrescoConnector.doGet(eq(expectedService), Matchers.anyMap()))
				.thenReturn(result);

		// Do the actual test call
		alfrescoUserManager.getPeople(null);
	}

	/**
	 * Test the
	 * {@link AlfrescoUserManager#setUserDashboard(String, com.surevine.alfresco.dashboard.DashboardDefinition)}
	 * method with a successful call
	 * 
	 * @throws JSONException
	 * @throws AlfrescoException
	 */
	@Test
	public void testSetUserDashboardSuccess() throws AlfrescoException,
			JSONException {
		final String testTemplateId = "test_template_id";
		final String testUsername = "test_username";

		// Construct a mocked dashboard definition
		DashboardDefinition dashboardDef = Mockito
				.mock(DashboardDefinition.class);

		int[] rowCounts = { 2, 0, 4 };

		Map<String, List<String>> dashlets = DashboardDefinitionTest
				.createTestDashlets(rowCounts);

		when(dashboardDef.getDashlets()).thenReturn(dashlets);
		when(dashboardDef.getTemplateId()).thenReturn(testTemplateId);
		
		JSONObject successJSON = new JSONObject();
		successJSON.put("success", true);

		when(
				alfrescoConnector.doSharePost(eq(SET_USER_DASHBOARD_SERVICE),
						Matchers.any(JSONObject.class)))
				.thenReturn(successJSON);

		// This will capture the parameters sent to the http service
		ArgumentCaptor<JSONObject> jsonCaptor = ArgumentCaptor
				.forClass(JSONObject.class);

		// Do that actual test call
		alfrescoUserManager.setUserDashboard(testUsername, dashboardDef);

		// Verify various stuff
		verify(dashboardDef).getDashlets();

		verify(alfrescoConnector).doSharePost(eq(SET_USER_DASHBOARD_SERVICE),
				jsonCaptor.capture());

		JSONObject jsonResult = jsonCaptor.getValue();

		assertEquals("Template ID is wrong", testTemplateId, jsonResult
				.getString("templateId"));
		assertEquals("dashboardPage", "user/" + testUsername + "/dashboard",
				jsonResult.getString("dashboardPage"));

		JSONArray resultDashlets = jsonResult.getJSONArray("dashlets");
		
		// This will be a map of all the expected dashlet urls which should be in the post
		HashMap<String,String> expectedDashlets = new HashMap<String,String>();

		for(Entry<String,List<String>> entry : dashlets.entrySet()) {
			int col = Integer.valueOf(entry.getKey()) + 1;
			for(int i = 0; i < entry.getValue().size(); ++i) {
				expectedDashlets.put("component-" + col + "-" + (i + 1), entry.getValue().get(i));
			}
		}
		
		// We firstly check that all the resulting dashlets are contained in the original dashboard definition
		for(int i = 0; i < resultDashlets.length(); ++i) {
			JSONObject resultDashlet = resultDashlets.getJSONObject(i);
			
			String regionId = resultDashlet.getString("regionId");
			String url = resultDashlet.getString("url");
			
			assertTrue("Unexpected dashlet regionId: " + regionId, expectedDashlets.containsKey(regionId));
			assertEquals("Wrong url for regionId: " + regionId, expectedDashlets.get(regionId), url);

			// We will remove it from the map, then at the end if there are any left over we know something has gone wrong!
			expectedDashlets.remove(regionId);
		}
		
		// If there are expected dashlets left then they were not all included in the post
		assertTrue("Dashlets were expected but not delivered", expectedDashlets.size() == 0);
	}
	
	/**
	 * Test the
	 * {@link AlfrescoUserManager#setUserDashboard(String, com.surevine.alfresco.dashboard.DashboardDefinition)}
	 * method with a failed call
	 * 
	 * @throws JSONException
	 * @throws AlfrescoException
	 */
	@Test(expected=AlfrescoException.class)
	public void testSetUserDashboardFailure() throws AlfrescoException,
			JSONException {
		final String testTemplateId = "test_template_id";
		final String testUsername = "test_username";

		// Construct a blank dashboard definition
		DashboardDefinition dashboardDef = new DashboardDefinition(testTemplateId, 3);

		JSONObject successJSON = new JSONObject();
		successJSON.put("success", false);

		when(
				alfrescoConnector.doSharePost(eq(SET_USER_DASHBOARD_SERVICE),
						Matchers.any(JSONObject.class)))
				.thenReturn(successJSON);

		// Do that actual test call
		alfrescoUserManager.setUserDashboard(testUsername, dashboardDef);
	}
	
	/**
	 * Test the
	 * {@link AlfrescoUserManager#setUserDashboard(String, com.surevine.alfresco.dashboard.DashboardDefinition)}
	 * method with a successful call
	 * 
	 * @throws JSONException
	 * @throws AlfrescoException
	 */
	@Test(expected = AlfrescoException.class)
	public void testSetUserDashboardWithAlfrescoException()
			throws AlfrescoException, JSONException {
		final String testTemplateId = "test_template_id";
		final String testUsername = "test_username";

		DashboardDefinition dashboardDef = new DashboardDefinition(testTemplateId, 3);

		when(
				alfrescoConnector.doSharePost(eq(SET_USER_DASHBOARD_SERVICE),
						Matchers.any(JSONObject.class))).thenThrow(
				new AlfrescoException("An error has occurred"));

		// Do that actual test call
		alfrescoUserManager.setUserDashboard(testUsername, dashboardDef);
	}
}
