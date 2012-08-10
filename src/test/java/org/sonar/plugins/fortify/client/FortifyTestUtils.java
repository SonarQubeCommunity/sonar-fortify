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

import org.sonar.api.utils.DateUtils;
import xmlns.www_fortifysoftware_com.schema.activitytemplate.EquationVariable;
import xmlns.www_fortifysoftware_com.schema.wstypes.MeasurementHistory;
import xmlns.www_fortifysoftware_com.schema.wstypes.Snapshot;
import xmlns.www_fortifysoftware_com.schema.wstypes.VariableHistory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

public final class FortifyTestUtils {
  private FortifyTestUtils() {
  }

  public static VariableHistory newVariable(String key, String date) throws DatatypeConfigurationException {
    VariableHistory result = new VariableHistory();
    EquationVariable v = new EquationVariable();
    v.setVariable(key);
    result.setVariable(v);
    Snapshot snapshot = new Snapshot();
    snapshot.setDate(newDate(date));
    result.setSnapshot(snapshot);
    return result;
  }

  public static MeasurementHistory newMeasure(String key, String date) throws DatatypeConfigurationException {
    MeasurementHistory measure = new MeasurementHistory();
    measure.setMeasurementGuid(key);
    Snapshot snapshot = new Snapshot();
    snapshot.setDate(FortifyTestUtils.newDate(date));
    measure.setSnapshot(snapshot);
    return measure;
  }

  public static XMLGregorianCalendar newDate(String s) throws DatatypeConfigurationException {
    GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
    cal.setTime(DateUtils.parseDate(s));
    return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
  }
}
