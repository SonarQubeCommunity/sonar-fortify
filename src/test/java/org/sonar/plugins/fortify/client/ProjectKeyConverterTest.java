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

import com.fortify.manager.schema.Project;
import com.fortify.manager.schema.ProjectVersionLite;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectKeyConverterTest {
  @Test
  public void should_match_project_name_and_version() {
    ListMultimap data = ArrayListMultimap.create();
    data.putAll(newProject(100L, "org.apache.struts:struts"), Arrays.asList(newVersion(1001L, "1.3.9"), newVersion(10002L, "1.4")));
    data.putAll(newProject(200L, "org.codehaus.sonar:sonar"), Arrays.asList(newVersion(2001L, "3.2"), newVersion(20002L, "3.3")));

    FortifyClient fortify = mockClient(data);
    ProjectKeyConverter converter = new ProjectKeyConverter(fortify);

    assertThat(converter.getProjectVersionId("unknown-project", "1.0")).isNull();
    assertThat(converter.getProjectVersionId("org.codehaus.sonar:sonar", "5.0")).isNull();
    assertThat(converter.getProjectVersionId("org.codehaus.sonar:sonar", "3.3")).isEqualTo(20002L);
  }

  private FortifyClient mockClient(ListMultimap<Project, ProjectVersionLite> data) {
    for (Map.Entry<Project, ProjectVersionLite> entry : data.entries()) {
      entry.getValue().setProjectId(entry.getKey().getId());
    }

    FortifyClient fortify = mock(FortifyClient.class);
    when(fortify.getProjects()).thenReturn(Lists.newArrayList(data.keySet()));
    when(fortify.getProjectVersions()).thenReturn(Lists.newArrayList(data.values()));
    return fortify;
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
