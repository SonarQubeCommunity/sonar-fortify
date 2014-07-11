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

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rule.RuleKey;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;

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

  @Test
  public void shouldAnalyse() throws URISyntaxException {
    when(configuration.getReportPath()).thenReturn("audit-simple.fvdl");
    Project project = new Project("foo");
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    project.setFileSystem(fs);
    File baseDir = new File(this.getClass().getResource("/project/placeholder.txt").toURI()).getParentFile();
    when(fs.getBasedir()).thenReturn(baseDir);
    when(fileSystem.baseDir()).thenReturn(baseDir);
    when(fileSystem.languages()).thenReturn(Sets.newTreeSet(Arrays.asList("web")));
    ActiveRule activeRule = mock(ActiveRule.class);
    when(activeRules.find(RuleKey.of("fortify-web", "45BF957F-1A34-4E28-9B34-FEB83EC96792"))).thenReturn(activeRule);
    SensorContext context = mock(SensorContext.class);
    when(context.getResource(org.sonar.api.resources.File.create("WebContent/main.jsp"))).thenReturn(org.sonar.api.resources.File.create("WebContent/main.jsp"));
    sensor.analyse(project, context);
  }
}
