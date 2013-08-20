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
package com.surevine.alfresco.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class DashboardDefinition {
	/**
	 * The template id
	 */
	final String templateId;

	/**
	 * The dashlets. The outer map is the columns, the inner list is the rows in
	 * each column
	 */
	final Map<String, List<String>> dashlets;

	/**
	 * Construct a new {@link DashboardDefinition} using the specified tempate
	 * ID and number of columns
	 * 
	 * @param templateId
	 *            the template ID for the dashboard.
	 * @param numColumns
	 *            the number of columns for the dashboard.
	 */
	public DashboardDefinition(final String templateId, final int numColumns) {
		if (templateId == null) {
			throw new NullPointerException("templateId cannot be null");
		}

		if (numColumns < 1) {
			throw new IllegalArgumentException(
					"numColumns cannot be zero or negative");
		}

		this.templateId = templateId;

		// Initialise the dashlet container
		dashlets = new HashMap<String, List<String>>(numColumns);

		for (int i = 0; i < numColumns; ++i) {
			dashlets.put(String.valueOf(i), new ArrayList<String>());
		}
	}

	/**
	 * Adds a dashlet to the specified column
	 * 
	 * @param column
	 *            the column to add the dashlet to
	 * @param dashlet
	 *            the dashlet url
	 */
	public void addDashlet(final int column, final String dashlet) {
		List<String> rows = dashlets.get(String.valueOf(column));

		if (rows == null) {
			throw new IndexOutOfBoundsException("Column " + column
					+ "does not exist");
		}

		rows.add(dashlet);
	}

	/**
	 * Gets the current dashlets Map of Lists
	 */
	public Map<String, List<String>> getDashlets() {
		return dashlets;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("templateId", templateId);
		builder.append("dashlets", dashlets);
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (!(o instanceof DashboardDefinition)) {
			return false;
		}

		DashboardDefinition rhs = (DashboardDefinition) o;
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(templateId, rhs.templateId);
		builder.append(dashlets, rhs.dashlets);
		return builder.isEquals();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(97, 193);
		builder.append(templateId);
		builder.append(dashlets);
		return builder.toHashCode();
	}
	
	/**
	 * Gets the template ID for the dashboard
	 */
	public String getTemplateId() {
		return templateId;
	}
}
