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
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.plugins.fortify.client.FortifyClient;

import javax.annotation.Nullable;
import java.util.List;

public class FortifyProject implements BatchExtension {

  private static final Logger LOG = LoggerFactory.getLogger(FortifyProject.class);

  private final FortifyClient client;
  private final org.sonar.api.resources.Project sonarProject;
  private Long versionId = null;

  public FortifyProject(FortifyClient client, org.sonar.api.resources.Project sonarProject) {
    this.client = client;
    this.sonarProject = sonarProject;
  }

  public void start() {
    if (sonarProject.isRoot() && client.isEnabled()) {
      initProjectVersionId();
    }
  }

  boolean exists() {
    return versionId != null;
  }

  Long getVersionId() {
    return versionId;
  }

  /**
   * <p>The current algorithm is really simple : [sonar project name, sonar project version] must match [fortify project name, fortify project version].</p>
   */
  private void initProjectVersionId() {
    // Fortify webservices do not allow to request a specific project. All the projects must be loaded.
    List<Project> projects = client.getProjects();
    final Project project = Iterables.find(projects, new Predicate<Project>() {
      public boolean apply(@Nullable Project project) {
        return project != null && sonarProject.getName().equals(project.getName());
      }
    }, null);
    ProjectVersionLite pv = null;
    if (project != null) {
      pv = Iterables.find(client.getProjectVersions(), new Predicate<ProjectVersionLite>() {
        public boolean apply(@Nullable ProjectVersionLite pv) {
          return pv != null && pv.getProjectId() == project.getId() && sonarProject.getAnalysisVersion().equals(pv.getName());
        }
      }, null);
    }
    if (pv != null) {
      versionId = pv.getId();
      LOG.info("Fortify SSC Project: ");
    } else {
      LoggerFactory.getLogger(FortifyProject.class).info("Fortify SSC not found");
    }
  }
}
