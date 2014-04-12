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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.TimeProfiler;

import javax.annotation.CheckForNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.zip.ZipFile;

public class FortifySensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(FortifySensor.class);

  private final FortifySensorConfiguration configuration;
  private final ResourcePerspectives resourcePerspectives;
  private final FileSystem fileSystem;
  private final ActiveRules activeRules;

  public FortifySensor(
    FortifySensorConfiguration configuration,
    ResourcePerspectives resourcePerspectives,
    FileSystem fileSystem,
    ActiveRules activeRules) {
    this.configuration = configuration;
    this.resourcePerspectives = resourcePerspectives;
    this.fileSystem = fileSystem;
    this.activeRules = activeRules;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return this.configuration.isActive(this.fileSystem.languages()) && reportExists(getReportFromProperty());
  }

  private void addIssue(Resource resource, FortifyVulnerability vulnerability, ActiveRule activeRule) {
    Issuable issuable = this.resourcePerspectives.as(Issuable.class, resource);
    if (issuable == null) {
      FortifySensor.LOG.warn("Resource {} is not issuable.", resource);
    } else {
      Issue issue = issuable.newIssueBuilder()
        .ruleKey(activeRule.ruleKey())
        .line(vulnerability.getLine())
        .message(vulnerability.getMessage())
        .build();
      issuable.addIssue(issue);
    }
  }

  private void addIssues(Project project, Collection<FortifyVulnerability> vulnerabilities) {
    for (FortifyVulnerability vulnerability : vulnerabilities) {
      Resource resource = resourceOf(vulnerability, project);
      if (resource != null) {
        ActiveRule activeRule = getRule(vulnerability);
        if (activeRule == null) {
          FortifySensor.LOG.warn(
            "Fortify rule '{}' is not active in Sonar.", vulnerability.getClassID());
        } else {
          addIssue(resource, vulnerability, activeRule);
        }
      }
    }
  }

  private Collection<FortifyVulnerability> parseReport(java.io.File report) throws IOException, FortifyParseException {
    ZipFile fprFile = new ZipFile(report);
    try {
      InputStream inputStream = fprFile.getInputStream(fprFile.getEntry(FortifyConstants.AUDIT_FVDL_FILE));
      try {
        return new FortifyFprParser().parse(inputStream);
      } finally {
        inputStream.close();
      }
    } finally {
      fprFile.close();
    }
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    TimeProfiler profiler = new TimeProfiler().start("Execute Fortify");
    try {
      addIssues(project, parseReport(getReportFromProperty()));
    } catch (IOException e) {
      throw new SonarException("Can not execute Fortify", e);
    } catch (FortifyParseException e) {
      throw new SonarException("Can not execute Fortify", e);
    } finally {
      profiler.stop();
    }
  }

  @CheckForNull
  private ActiveRule getRule(FortifyVulnerability vulnerability) {
    ActiveRule rule = null;
    for (String language : this.fileSystem.languages()) {
      String repositoryKey = FortifyConstants.fortifyRepositoryKey(language);
      rule = this.activeRules.find(RuleKey.of(repositoryKey, vulnerability.getClassID()));
      if (rule != null) {
        return rule;
      }
    }
    return rule;
  }

  @CheckForNull
  private Resource resourceOf(FortifyVulnerability vulnerability, Project project) {
    java.io.File file = new java.io.File(vulnerability.getFile());
    Resource resource = File.fromIOFile(file, project);
    if (resource == null) {
      FortifySensor.LOG.warn("The file \"{}\" is not under module base dir.", file);
    }
    return resource;
  }

  @CheckForNull
  private java.io.File getReportFromProperty() {
    String path = this.configuration.getReportPath();
    if (path != null && path.length() > 0) {
      java.io.File report = new java.io.File(this.fileSystem.baseDir(), path);
      if (!reportExists(report)) {
        report = new java.io.File(path);
      }
      return report;
    }
    return null;
  }

  private boolean reportExists(java.io.File report) {
    return report != null && report.exists() && report.isFile();
  }

  @Override
  public String toString() {
    return "Fortify sensor";
  }
}
