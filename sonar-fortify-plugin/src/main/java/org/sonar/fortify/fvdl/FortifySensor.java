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
import org.sonar.api.rule.Severity;
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.TimeProfiler;
import org.sonar.fortify.base.FortifyConstants;
import org.sonar.fortify.base.FortifyMetrics;
import org.sonar.fortify.fvdl.element.Fvdl;
import org.sonar.fortify.fvdl.element.Vulnerability;

import javax.annotation.CheckForNull;

import java.io.InputStream;

public class FortifySensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(FortifySensor.class);

  private final FortifySensorConfiguration configuration;
  private final ResourcePerspectives resourcePerspectives;
  private final FileSystem fileSystem;
  private final ActiveRules activeRules;
  private final FortifyReportFile report;

  private int blockerIssuesCount = 0;
  private int criticalIssuesCount = 0;
  private int majorIssuesCount = 0;
  private int minorIssuesCount = 0;

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
      if (issuable.addIssue(issue)) {
        incrementCount(severity);
      }
    }
  }

  private void incrementCount(String severity) {
    if (Severity.BLOCKER.equals(severity)) {
      blockerIssuesCount++;
    } else if (Severity.CRITICAL.equals(severity)) {
      criticalIssuesCount++;
    } else if (Severity.MAJOR.equals(severity)) {
      majorIssuesCount++;
    } else if (Severity.MINOR.equals(severity)) {
      minorIssuesCount++;
    }
  }

  private void addIssues(SensorContext context, Project project, Fvdl fvdl) {
    String sourceBasePath = fvdl.getBuild().getSourceBasePath();
    for (Vulnerability vulnerability : fvdl.getVulnerabilities()) {
      Resource resource = resourceOf(context, sourceBasePath, vulnerability, project);
      if (resource != null) {
        ActiveRule activeRule = getRule(vulnerability);
        if (activeRule == null) {
          FortifySensor.LOG.debug(
            "Fortify rule '{}' is not active in quality profiles of your project.", vulnerability.getClassID());
        } else {
          addIssue(resource, fvdl, vulnerability, activeRule);
        }
      }
    }
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    TimeProfiler profiler = new TimeProfiler().start("Process Fortify report");
    try {
      InputStream stream = this.report.getInputStream();
      try {
        Fvdl fvdl = new FvdlStAXParser().parse(stream);
        addIssues(context, project, fvdl);
      } finally {
        stream.close();
      }
    } catch (Exception e) {
      throw new SonarException("Can not process Fortify report", e);
    } finally {
      profiler.stop();
    }
    saveMeasures(context);
  }

  private void saveMeasures(SensorContext context) {
    context.saveMeasure(FortifyMetrics.CFPO, Double.valueOf(blockerIssuesCount));
    context.saveMeasure(FortifyMetrics.HFPO, Double.valueOf(criticalIssuesCount));
    context.saveMeasure(FortifyMetrics.MFPO, Double.valueOf(majorIssuesCount));
    context.saveMeasure(FortifyMetrics.LFPO, Double.valueOf(minorIssuesCount));
    if (blockerIssuesCount > 0) {
      context.saveMeasure(FortifyMetrics.SECURITY_RATING, 1.0);
    } else if (criticalIssuesCount > 0) {
      context.saveMeasure(FortifyMetrics.SECURITY_RATING, 2.0);
    } else if (majorIssuesCount > 0) {
      context.saveMeasure(FortifyMetrics.SECURITY_RATING, 3.0);
    } else if (minorIssuesCount > 0) {
      context.saveMeasure(FortifyMetrics.SECURITY_RATING, 4.0);
    } else {
      context.saveMeasure(FortifyMetrics.SECURITY_RATING, 1.0);
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
    java.io.File file = new java.io.File(sourceBasePath, vulnerability.getPath());
    if (file.exists()) {
      Resource resource = File.fromIOFile(file, project);
      if (resource == null || context.getResource(resource) == null) {
        LOG.debug("File \"{}\" is not under module basedir or is not indexed. Skip it.", vulnerability.getPath());
        return null;
      }
      return resource;
    }
    LOG.debug("Unable to find \"{}\". Trying relative path.", file);
    file = new java.io.File(fileSystem.baseDir(), vulnerability.getPath());
    if (file.exists()) {
      Resource resource = File.fromIOFile(file, project);
      if (resource == null || context.getResource(resource) == null) {
        LOG.debug("File \"{}\" is not indexed. Skip it.", vulnerability.getPath());
        return null;
      }
      return resource;
    }
    LOG.debug("Unable to find \"{}\". Your Fortify analysis was probably started from a different location than current SonarQube analysis.", file);
    return null;
  }

  @Override
  public String toString() {
    return "Fortify sensor";
  }
}
