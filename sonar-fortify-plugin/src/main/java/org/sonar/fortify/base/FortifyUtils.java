/*
 * Fortify Plugin for SonarQube
 * Copyright (C) 2014 Vivien HENRIET and SonarSource
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
package org.sonar.fortify.base;

import org.codehaus.staxmate.SMInputFactory;
import org.sonar.api.rule.Severity;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;

public final class FortifyUtils {
  private FortifyUtils() {
    // only static stuff
  }

  private static final double BLOCKER_SEVERITY_THRESHOLD = 4.0;
  private static final double CRITICAL_SEVERITY_THRESHOLD = 3.0;
  private static final double MAJOR_SEVERITY_THRESHOLD = 2.0;
  private static final double MINOR_SEVERITY_THRESHOLD = 1.0;

  public static SMInputFactory newStaxParser() throws FactoryConfigurationError {
    XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    // just so it won't try to load DTD in if there's DOCTYPE
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    return new SMInputFactory(xmlFactory);
  }

  public static String fortifyToSonarQubeSeverity(String fortifySeverity) {
    String severity;
    Double level = Double.valueOf(fortifySeverity);
    if (level >= FortifyUtils.BLOCKER_SEVERITY_THRESHOLD) {
      severity = Severity.BLOCKER;
    } else if (level >= FortifyUtils.CRITICAL_SEVERITY_THRESHOLD) {
      severity = Severity.CRITICAL;
    } else if (level >= FortifyUtils.MAJOR_SEVERITY_THRESHOLD) {
      severity = Severity.MAJOR;
    } else if (level >= FortifyUtils.MINOR_SEVERITY_THRESHOLD) {
      severity = Severity.MINOR;
    } else {
      severity = Severity.INFO;
    }
    return severity;

  }
}
