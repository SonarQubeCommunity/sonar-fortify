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
import org.sonar.fortify.base.FortifyConstants;
import org.sonar.fortify.fvdl.element.Fvdl;
import org.sonar.fortify.fvdl.element.Vulnerability;
import org.xml.sax.SAXException;

import javax.annotation.CheckForNull;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;

public class FortifySensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(FortifySensor.class);

  private final FortifySensorConfiguration configuration;
  private final ResourcePerspectives resourcePerspectives;
  private final FileSystem fileSystem;
  private final ActiveRules activeRules;
  private final FortifyReportFile report;

  public FortifySensor(
    FortifySensorConfiguration configuration,
    ResourcePerspectives resourcePerspectives,
    FileSystem fileSystem,
    ActiveRules activeRules) {
    this.configuration = configuration;
    this.resourcePerspectives = resourcePerspectives;
    this.fileSystem = fileSystem;
    this.activeRules = activeRules;
    this.report = new FortifyReportFile(configuration, fileSystem);
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return this.configuration.isActive(this.fileSystem.languages()) && this.report.exist();
  }

  private void addIssue(Resource resource, Fvdl fvdl, Vulnerability vulnerability, ActiveRule activeRule) {
    Issuable issuable = this.resourcePerspectives.as(Issuable.class, resource);
    if (issuable != null) {
      String severity = vulnerability.getInstanceSeverity();
      if (severity == null) {
        severity = activeRule.severity();
      }
      Issue issue = issuable.newIssueBuilder()
        .ruleKey(activeRule.ruleKey())
        .line(vulnerability.getLine())
        .message(fvdl.getDescription(vulnerability))
        .severity(severity)
        .build();
      issuable.addIssue(issue);
    }
  }

  private void addIssues(SensorContext context, Project project, Fvdl fvdl) {
    String sourceBasePath = fvdl.getBuild().getSourceBasePath();
    for (Vulnerability vulnerability : fvdl.getVulnerabilities()) {
      Resource resource = resourceOf(context, sourceBasePath, vulnerability, project);
      if (resource != null) {
        ActiveRule activeRule = getRule(vulnerability);
        if (activeRule == null) {
          FortifySensor.LOG.warn(
            "Fortify rule '{}' is not active in Sonar.", vulnerability.getClassID());
        } else {
          addIssue(resource, fvdl, vulnerability, activeRule);
        }
      }
    }
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    TimeProfiler profiler = new TimeProfiler().start("Execute Fortify");
    try {
      InputStream stream = this.report.getInputStream();
      try {
        Fvdl fvdl = new FvdlSAXParser().parse(stream);
        addIssues(context, project, fvdl);
      } finally {
        stream.close();
      }
    } catch (IOException e) {
      throw new SonarException("Can not execute Fortify", e);
    } catch (SAXException e) {
      throw new SonarException("Can not execute Fortify", e);
    } catch (ParserConfigurationException e) {
      throw new SonarException("Can not execute Fortify", e);
    } finally {
      profiler.stop();
    }
  }

  @CheckForNull
  private ActiveRule getRule(Vulnerability vulnerability) {
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
  private Resource resourceOf(SensorContext context, String sourceBasePath, Vulnerability vulnerability, Project project) {
    java.io.File file = new java.io.File(fileSystem.baseDir(), vulnerability.getPath());
    Resource resource = File.fromIOFile(file, project);
    if (resource == null || context.getResource(resource) == null) {
      LOG.debug("File \"{}\" is not under module basedir or is not indexed. Trying absolute path.", vulnerability.getPath());
      file = new java.io.File(sourceBasePath, vulnerability.getPath());
      if (file.exists()) {
        resource = File.fromIOFile(file, project);
        if (resource == null || context.getResource(resource) == null) {
          LOG.debug("File \"{}\" is not under module basedir or is not indexed.", file);
        }
      } else {
        LOG.debug("Unable to find \"{}\".", file);
      }
    }
    return resource;
  }

  @Override
  public String toString() {
    return "Fortify sensor";
  }
}
