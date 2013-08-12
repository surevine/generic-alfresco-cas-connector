/**
 * 
 */
package com.surevine.alfresco.dashboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ash
 * 
 */
public class DashboardDefinitionTest {
	/**
	 * The default test template id
	 */
	static final String TEST_TEMPLATE_ID = "template_id";

	/**
	 * The default test number of columns
	 */
	static final int TEST_NUM_COLUMNS = 3;

	/**
	 * The class under test
	 */
	DashboardDefinition dashboardDefinition;

	/**
	 * Set up our test class
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		dashboardDefinition = new DashboardDefinition(TEST_TEMPLATE_ID,
				TEST_NUM_COLUMNS);
	}

	/**
	 * Clean up after ourselves
	 * 
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		dashboardDefinition = null;
	}

	/**
	 * Test method for
	 * {@link com.surevine.alfresco.dashboard.DashboardDefinition#DashboardDefinition(java.lang.String, int)}
	 * .
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructorWithNullTemplate() {
		dashboardDefinition = new DashboardDefinition(null, TEST_NUM_COLUMNS);
	}

	/**
	 * Test method for
	 * {@link com.surevine.alfresco.dashboard.DashboardDefinition#DashboardDefinition(java.lang.String, int)}
	 * .
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWith0Columns() {
		dashboardDefinition = new DashboardDefinition(TEST_TEMPLATE_ID, 0);
	}

	/**
	 * Test method for
	 * {@link com.surevine.alfresco.dashboard.DashboardDefinition#DashboardDefinition(java.lang.String, int)}
	 * .
	 */
	@Test
	public void testConstructor() {
		assertSame("Template ID not initialised correctly", TEST_TEMPLATE_ID,
				dashboardDefinition.templateId);

		final int[] rowCounts = new int[TEST_NUM_COLUMNS];

		for (int i = 0; i < TEST_NUM_COLUMNS; ++i) {
			rowCounts[i] = 0;
		}

		final Map<String, List<String>> expected = createTestDashlets(rowCounts);

		assertNotNull("Dashlets is null", dashboardDefinition.dashlets);
		assertEquals("Dashlets not correctly initialised", expected,
				dashboardDefinition.dashlets);
	}

	/**
	 * Test method for
	 * {@link com.surevine.alfresco.dashboard.DashboardDefinition#addDashlet(int, java.lang.String)}
	 * .
	 */
	@Test
	public void testAddAndGetDashlets() {
		final int[] rowCounts = { 3, 0, 2 };

		final Map<String, List<String>> expected = createTestDashlets(rowCounts);

		for (int col = 0; col < rowCounts.length; ++col) {
			for (final String dashlet : expected.get(String.valueOf(col))) {
				dashboardDefinition.addDashlet(col, dashlet);
			}
		}

		assertEquals("Dashlets not correct", expected, dashboardDefinition
				.getDashlets());
	}

	/**
	 * Test method for
	 * {@link com.surevine.alfresco.dashboard.DashboardDefinition#addDashlet(int, java.lang.String)}
	 * .
	 */
	@Test(expected = IndexOutOfBoundsException.class)
	public void testAddDashletsFailsWithNegativeColumn() {
		dashboardDefinition.addDashlet(-1, "test");
	}

	/**
	 * Test method for
	 * {@link com.surevine.alfresco.dashboard.DashboardDefinition#addDashlet(int, java.lang.String)}
	 * .
	 */
	@Test(expected = IndexOutOfBoundsException.class)
	public void testAddDashletsFailsWithFourthColumn() {
		dashboardDefinition.addDashlet(3, "test");
	}

	/**
	 * Test the equals and hashCode contracts.
	 */
	@Test
	public void testEqualsAndHashCode() {
		EqualsVerifier.forClass(DashboardDefinition.class).verify();
	}

	/**
	 * Test the toString method. Let's just make sure it doesn't error and comes back with something!
	 */
	@Test
	public void testToString() {
		String result = dashboardDefinition.toString();
		assertFalse("toString is empty", result.equals(""));
	}

	/**
	 * Test the getTemplateId method.
	 */
	@Test
	public void testGetTemplateId() {
		assertEquals("TemplateId not returned correctly", TEST_TEMPLATE_ID, dashboardDefinition.getTemplateId());
	}
	
	/**
	 * Creates a test dashlet map
	 * 
	 * @param rowCounts
	 *            array of ints denoting how may dashlets to create in each
	 *            column
	 * @return
	 */
	public static Map<String, List<String>> createTestDashlets(
			final int[] rowCounts) {
		final Map<String, List<String>> testDashlets = new HashMap<String, List<String>>(
				3);

		for (int col = 0; col < rowCounts.length; ++col) {
			final ArrayList<String> column = new ArrayList<String>(
					rowCounts[col]);

			for (int row = 0; row < rowCounts[col]; ++row) {
				column.add("col" + col + "row" + row);
			}

			testDashlets.put(String.valueOf(col), column);
		}

		return testDashlets;
	}
}
