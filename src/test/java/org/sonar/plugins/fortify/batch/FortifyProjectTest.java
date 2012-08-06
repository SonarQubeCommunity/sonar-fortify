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
package org.sonar.plugins.fortify.batch;

import com.fortify.manager.schema.Project;
import com.fortify.manager.schema.ProjectVersionLite;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.plugins.fortify.base.FortifyConstants;
import org.sonar.plugins.fortify.client.FortifyClient;

import java.util.Arrays;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FortifyProjectTest {

  @Test
  public void should_match_project_name_and_version() {
    org.sonar.api.resources.Project sonarProject = new org.sonar.api.resources.Project("org.codehaus.sonar:sonar");
    sonarProject.setName("Sonar");
    sonarProject.setAnalysisVersion("3.3");

    FortifyProject fortifyProject = new FortifyProject(mockClient(), sonarProject, new Settings());
    fortifyProject.start();

    assertThat(fortifyProject.exists()).isTrue();
    assertThat(fortifyProject.getVersionId()).isEqualTo(20002L);
  }

  @Test
  public void could_not_find_version() {
    org.sonar.api.resources.Project sonarProject = new org.sonar.api.resources.Project("org.codehaus.sonar:sonar");
    sonarProject.setName("Sonar");
    sonarProject.setAnalysisVersion("5.0");

    FortifyProject fortifyProject = new FortifyProject(mockClient(), sonarProject, new Settings());
    fortifyProject.start();

    assertThat(fortifyProject.exists()).isFalse();
    assertThat(fortifyProject.getVersionId()).isNull();
  }

  @Test
  public void could_not_find_name() {
    org.sonar.api.resources.Project sonarProject = new org.sonar.api.resources.Project("commons-lang:commons-lang");
    sonarProject.setName("Commons Lang");
    sonarProject.setAnalysisVersion("2.0");

    FortifyProject fortifyProject = new FortifyProject(mockClient(), sonarProject, new Settings());
    fortifyProject.start();

    assertThat(fortifyProject.exists()).isFalse();
    assertThat(fortifyProject.getVersionId()).isNull();
  }

  @Test
  public void should_not_match_modules() {
    org.sonar.api.resources.Project sonarProject = new org.sonar.api.resources.Project("org.codehaus.sonar:sonar-parent");
    sonarProject.setName("Sonar Parent");
    sonarProject.setAnalysisVersion("3.3");

    org.sonar.api.resources.Project sonarModule = new org.sonar.api.resources.Project("org.codehaus.sonar:sonar");
    sonarModule.setName("Sonar");
    sonarModule.setAnalysisVersion("3.3");
    sonarModule.setParent(sonarProject);

    FortifyProject fortifyProject = new FortifyProject(mockClient(), sonarModule, new Settings());
    fortifyProject.start();

    assertThat(fortifyProject.exists()).isFalse();
    assertThat(fortifyProject.getVersionId()).isNull();
  }

  @Test
  public void name_and_version_could_be_overridden() {
    org.sonar.api.resources.Project sonarProject = new org.sonar.api.resources.Project("org.codehaus.sonar:sonar");
    sonarProject.setName("Something");
    sonarProject.setAnalysisVersion("1.0");


    Settings settings = new Settings();
    settings.setProperty(FortifyConstants.PROPERTY_PROJECT_NAME, "Sonar");
    settings.setProperty(FortifyConstants.PROPERTY_PROJECT_VERSION, "3.3");

    FortifyProject fortifyProject = new FortifyProject(mockClient(), sonarProject, settings);
    fortifyProject.start();

    assertThat(fortifyProject.exists()).isTrue();
    assertThat(fortifyProject.getVersionId()).isEqualTo(20002L);
  }

  private FortifyClient mockClient() {
    ListMultimap<Project, ProjectVersionLite> data = ArrayListMultimap.create();
    data.putAll(newProject(100L, "Struts"), Arrays.asList(newVersion(1001L, "1.3.9"), newVersion(10002L, "1.4")));
    data.putAll(newProject(200L, "Sonar"), Arrays.asList(newVersion(2001L, "3.2"), newVersion(20002L, "3.3")));

    for (Map.Entry<Project, ProjectVersionLite> entry : data.entries()) {
      entry.getValue().setProjectId(entry.getKey().getId());
    }

    FortifyClient client = mock(FortifyClient.class);
    when(client.isEnabled()).thenReturn(true);
    when(client.getProjects()).thenReturn(Lists.newArrayList(data.keySet()));
    when(client.getProjectVersions()).thenReturn(Lists.newArrayList(data.values()));
    return client;
  }

  private Project newProject(long projectId, String projectName) {
    Project project = new Project();
    project.setId(projectId);
    project.setName(projectName);
    return project;
  }

  private ProjectVersionLite newVersion(long versionId, String version) {
    ProjectVersionLite result = new ProjectVersionLite();
    result.setId(versionId);
    result.setName(version);
    return result;
  }
}
