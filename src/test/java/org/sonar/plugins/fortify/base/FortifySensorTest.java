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

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FortifySensorTest {
  private FortifySensorConfiguration configuration;
  private ResourcePerspectives resourcePerspectives;
  private FileSystem fileSystem;
  private ActiveRules activeRules;
  private FortifySensor sensor;

  @Before
  public void init() {
    this.configuration = mock(FortifySensorConfiguration.class);
    this.resourcePerspectives = mock(ResourcePerspectives.class);
    this.fileSystem = mock(FileSystem.class);
    this.activeRules = mock(ActiveRules.class);
    this.sensor = new FortifySensor(this.configuration, this.resourcePerspectives, this.fileSystem, this.activeRules);
  }

  @Test
  public void shouldExecuteOnProjectTest() {
    when(this.configuration.isActive(anyListOf(String.class))).thenReturn(false);
    assertThat(this.sensor.shouldExecuteOnProject(null)).isFalse();

    when(this.configuration.isActive(anyListOf(String.class))).thenReturn(true);
    assertThat(this.sensor.shouldExecuteOnProject(null)).isFalse();
  }

  @Test
  public void toStringTest() {
    assertThat(this.sensor.toString()).isEqualTo("Fortify sensor");
  }
}
