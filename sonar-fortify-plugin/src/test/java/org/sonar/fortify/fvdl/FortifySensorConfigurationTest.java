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
package org.sonar.fortify.fvdl;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.fortify.base.FortifyConstants;

import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FortifySensorConfigurationTest {
  private RulesProfile profile;
  private Settings settings;
  private FortifySensorConfiguration fortifySensorConfiguration;

  @Before
  public void init() {
    this.profile = mock(RulesProfile.class);
    this.settings = mock(Settings.class);
    this.fortifySensorConfiguration = new FortifySensorConfiguration(this.profile, this.settings);
  }

  @Test
  public void testIsActive() {
    when(this.profile.getActiveRulesByRepository(anyString())).thenReturn(Collections.<ActiveRule>emptyList());
    assertThat(this.fortifySensorConfiguration.isActive(Collections.singletonList("java"))).isFalse();

    when(this.profile.getActiveRulesByRepository(anyString())).thenReturn(Collections.singletonList((ActiveRule) null));
    assertThat(this.fortifySensorConfiguration.isActive(Collections.singletonList("java"))).isTrue();
  }

  @Test
  public void testGetReportPath() {
    when(this.settings.getString(FortifyConstants.REPORT_PATH_PROPERTY)).thenReturn("location");
    assertThat(this.fortifySensorConfiguration.getReportPath()).isEqualTo("location");
  }
}
