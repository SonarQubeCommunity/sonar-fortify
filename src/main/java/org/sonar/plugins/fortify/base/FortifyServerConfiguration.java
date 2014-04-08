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
package org.sonar.plugins.fortify.base;

import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;

import java.util.Arrays;
import java.util.Collection;

public class FortifyServerConfiguration implements ServerExtension {
  private final Settings settings;

  public FortifyServerConfiguration(Settings settings) {
    this.settings = settings;
  }

  public Collection<String> getRulePackLocations() {
    return Arrays.asList(this.settings.getStringArray(FortifyConstants.RULEPACK_LOCATION_PROPERTY));
  }

}
