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
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.List;

public class ProjectKeyConverter {

  private FortifyClient client;

  public ProjectKeyConverter(FortifyClient client) {
    this.client = client;
  }

  /**
   * <p>The current algorithm is really simple : [sonar project key, sonar project version] must match [fortify project name, fortify project version].</p>
   */
  public Long getProjectVersionId(final String fortifyProjectName, final String version) {
    // Fortify webservices do not allow to request a specific project. All the projects must be loaded.
    List<Project> projects = client.getProjects();
    final Project project = Iterables.find(projects, new Predicate<Project>() {
      public boolean apply(@Nullable Project project) {
        return project != null && fortifyProjectName.equals(project.getName());
      }
    }, null);
    ProjectVersionLite pv = null;
    if (project != null) {
      pv = Iterables.find(client.getProjectVersions(), new Predicate<ProjectVersionLite>() {
        public boolean apply(@Nullable ProjectVersionLite pv) {
          return pv != null && pv.getProjectId() == project.getId() && version.equals(pv.getName());
        }
      }, null);
    }
    return pv != null ? pv.getId() : null;
  }
}
