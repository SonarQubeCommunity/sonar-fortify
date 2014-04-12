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
package org.sonar.plugins.fortify;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.fortify.base.FortifyConstants;
import org.sonar.plugins.fortify.base.FortifyMetrics;
import org.sonar.plugins.fortify.base.FortifyRuleRepositories;
import org.sonar.plugins.fortify.base.FortifySensor;
import org.sonar.plugins.fortify.base.FortifySensorConfiguration;
import org.sonar.plugins.fortify.base.FortifyServerConfiguration;
import org.sonar.plugins.fortify.ui.FortifyWidget;

import java.util.Arrays;
import java.util.List;

@Properties({
  @Property(
    key = FortifyConstants.ENABLE_PROPERTY,
    type = PropertyType.BOOLEAN,
    defaultValue = "true",
    name = "Active Fortify analisys",
    description = "Active Fortify analisys.",
    project = true, module = true, global = true),
  @Property(
    key = FortifyConstants.REPORT_PATH_PROPERTY,
    name = "Report path",
    description = "Absolute or relative path to FPR or FVDL report file.",
    project = true, global = true),
  @Property(
    key = FortifyConstants.RULEPACK_LOCATION_PROPERTY,
    type = PropertyType.STRING,
    name = "Fortify rulespack locations",
    description = "Fortify rulespack locations.",
    project = false, module = false, global = true, multiValues = true),

})
public final class FortifyPlugin extends SonarPlugin {

  @Override
  public List getExtensions() {
    return Arrays.<Class<?>>asList(FortifyMetrics.class, FortifyWidget.class, FortifyRuleRepositories.class,
      FortifyServerConfiguration.class, FortifySensor.class, FortifySensorConfiguration.class);
  }
}
