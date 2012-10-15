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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.config.Settings;
import org.sonar.plugins.fortify.base.FortifyConstants;
import org.sonar.plugins.fortify.client.FortifyClient;
import xmlns.www_fortifysoftware_com.schema.wstypes.Project;
import xmlns.www_fortifysoftware_com.schema.wstypes.ProjectVersionLite;

import javax.annotation.Nullable;

import java.util.List;

public class FortifyProject implements BatchExtension {

  private static final Logger LOG = LoggerFactory.getLogger(FortifyProject.class);

  private final FortifyClient client;
  private final org.sonar.api.resources.Project sonarProject;
  private final Settings settings;

  private String name = null;
  private String version = null;
  private Long versionId = null;

  public FortifyProject(FortifyClient client, org.sonar.api.resources.Project sonarProject, Settings settings) {
    this.client = client;
    this.sonarProject = sonarProject;
    this.settings = settings;
  }

  public void start() {
    if (client.isEnabled()) {
      name = StringUtils.defaultIfBlank(settings.getString(FortifyConstants.PROPERTY_PROJECT_NAME), sonarProject.getName());
      version = StringUtils.defaultIfBlank(settings.getString(FortifyConstants.PROPERTY_PROJECT_VERSION), sonarProject.getAnalysisVersion());
      versionId = initProjectVersionId(client, name, version);
    }
  }

  boolean exists() {
    return versionId != null;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public Long getVersionId() {
    return versionId;
  }

  private static Long initProjectVersionId(FortifyClient client, final String fortifyName, final String fortifyVersion) {
    // Fortify webservices do not allow to request a specific project. All the projects must be loaded then filtered.
    List<Project> projects = client.getProjects();
    final Project project = Iterables.find(projects, new Predicate<Project>() {
      public boolean apply(@Nullable Project project) {
        return project != null && fortifyName.equals(project.getName());
      }
    }, null);
    ProjectVersionLite pv = null;
    if (project != null) {
      pv = Iterables.find(client.getProjectVersions(), new Predicate<ProjectVersionLite>() {
        public boolean apply(@Nullable ProjectVersionLite pv) {
          return pv != null && pv.getProjectId() == project.getId() && fortifyVersion.equals(pv.getName());
        }
      }, null);
    }

    Long versionId = null;
    if (pv != null) {
      versionId = pv.getId();
      LOG.info("Fortify SSC Project: " + fortifyName + ", version: " + fortifyVersion);
    } else {
      LoggerFactory.getLogger(FortifyProject.class).info("Fortify SSC Project does not exist");
    }
    return versionId;
  }
}
