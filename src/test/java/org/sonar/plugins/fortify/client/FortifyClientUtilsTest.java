/*
 * Sonar Fortify Plugin
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.fortify.client;

import com.fortify.schema.fws.MeasurementHistoryListResponse;
import com.fortify.schema.fws.VariableHistoryListResponse;
import org.junit.Test;
import org.sonar.api.utils.DateUtils;
import xmlns.www_fortifysoftware_com.schema.activitytemplate.EquationVariable;
import xmlns.www_fortifysoftware_com.schema.wstypes.MeasurementHistory;
import xmlns.www_fortifysoftware_com.schema.wstypes.Snapshot;
import xmlns.www_fortifysoftware_com.schema.wstypes.VariableHistory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class FortifyClientUtilsTest {
  @Test
  public void keep_more_recent_measures() throws DatatypeConfigurationException {
    MeasurementHistoryListResponse response = new MeasurementHistoryListResponse();
    response.getMeasurementHistories().add(FortifyTestUtils.newMeasure("FortifySecurityRating", "2012-01-01"));
    response.getMeasurementHistories().add(FortifyTestUtils.newMeasure("FortifySecurityRating", "2010-01-01"));
    response.getMeasurementHistories().add(FortifyTestUtils.newMeasure("FortifySecurityRating", "2011-01-01"));
    response.getMeasurementHistories().add(FortifyTestUtils.newMeasure("Other", "2011-01-01"));
    response.getMeasurementHistories().add(FortifyTestUtils.newMeasure("Other", "2012-01-01"));

    List<MeasurementHistory> sorted = FortifyClientUtils.keepMoreRecent(response);

    assertThat(sorted).hasSize(2);
    assertThat(sorted).onProperty("measurementGuid").contains("FortifySecurityRating", "Other");
    for (MeasurementHistory m : sorted) {
      assertThat(m.getSnapshot().getDate().getYear()).isEqualTo(2012);
    }
  }

  @Test
  public void keep_more_recent_variables() throws DatatypeConfigurationException {
    VariableHistoryListResponse response = new VariableHistoryListResponse();
    response.getVariableHistories().add(FortifyTestUtils.newVariable("CFPO", "2012-01-01"));
    response.getVariableHistories().add(FortifyTestUtils.newVariable("CFPO", "2010-01-01"));
    response.getVariableHistories().add(FortifyTestUtils.newVariable("CFPO", "2011-01-01"));
    response.getVariableHistories().add(FortifyTestUtils.newVariable("FILES", "2011-01-01"));
    response.getVariableHistories().add(FortifyTestUtils.newVariable("FILES", "2012-01-01"));

    List<VariableHistory> sorted = FortifyClientUtils.keepMoreRecent(response);

    assertThat(sorted).hasSize(2);
    assertThat(sorted).onProperty("variable").onProperty("variable").contains("CFPO", "FILES");
    for (VariableHistory v : sorted) {
      assertThat(v.getSnapshot().getDate().getYear()).isEqualTo(2012);
    }
  }


}
