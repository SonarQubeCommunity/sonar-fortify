/*
 * SonarQube Fortify Plugin
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

import org.apache.commons.lang.StringUtils;

public class FortifyConstants {
  /* sonar.junit.reportsPath=[baseDir]/myReports/myExecutionReports */
  public static final String AUDIT_FVDL_FILE = "audit.fvdl";
  public static final String REPORT_PATH_PROPERTY = "fortify.reportPath";
  public static final String ENABLE_PROPERTY = "fortify.enable";
  public static final String RULEPACK_LOCATION_PROPERTY = "fortify.rulepack.location";

  private FortifyConstants() {
  }

  public static String fortifyRepositoryKey(String language) {
    return "fortify-" + StringUtils.lowerCase(language);
  }
}
