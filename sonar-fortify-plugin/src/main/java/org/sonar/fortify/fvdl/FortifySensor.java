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
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.Severity;
import org.sonar.api.utils.TimeProfiler;
import org.sonar.fortify.base.FortifyConstants;
import org.sonar.fortify.base.metrics.FortifyMetrics;
import org.sonar.fortify.fvdl.element.Fvdl;
import org.sonar.fortify.fvdl.element.Vulnerability;

import javax.annotation.CheckForNull;

import java.io.InputStream;

public class FortifySensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(FortifySensor.class);

  private static final double BLOCKER_SECURITY_RATING_LEVEL = 1.0;
  private static final double CRITICAL_SECURITY_RATING_LEVEL = 2.0;
  private static final double MAJOR_SECURITY_RATING_LEVEL = 3.0;
  private static final double MINOR_SECURITY_RATING_LEVEL = 4.0;
  private static final double DEFAULT_SECURITY_RATING_LEVEL = 5.0;

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

  private void addIssue(InputFile inputFile, Fvdl fvdl, Vulnerability vulnerability, ActiveRule activeRule) {
    Issuable issuable = this.resourcePerspectives.as(Issuable.class, inputFile);
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
      this.blockerIssuesCount++;
    } else if (Severity.CRITICAL.equals(severity)) {
      this.criticalIssuesCount++;
    } else if (Severity.MAJOR.equals(severity)) {
      this.majorIssuesCount++;
    } else if (Severity.MINOR.equals(severity)) {
      this.minorIssuesCount++;
    }
  }

  private void addIssues(SensorContext context, Project project, Fvdl fvdl) {
    String sourceBasePath = fvdl.getBuild().getSourceBasePath();
    for (Vulnerability vulnerability : fvdl.getVulnerabilities()) {
      InputFile inputFile = resourceOf(context, sourceBasePath, vulnerability, project);
      if (inputFile != null) {
        String ruleKey = FortifyConstants.fortifySQRuleKey(vulnerability.getKingdom(), vulnerability.getType(), vulnerability.getSubtype());
        if (ruleKey == null) {
          LOG.debug("Unable to find rule for vulnerability " + vulnerability);
          continue;
        }
        ActiveRule activeRule = getRule(ruleKey, inputFile.language());
        if (activeRule == null) {
          FortifySensor.LOG.debug("Fortify rule '{}' is not active in quality profiles of your project.", ruleKey);
        } else {
          addIssue(inputFile, fvdl, vulnerability, activeRule);
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
      throw new IllegalStateException("Can not process Fortify report", e);
    } finally {
      profiler.stop();
    }
    saveMeasures(context);
  }

  private void saveMeasures(SensorContext context) {
    context.saveMeasure(FortifyMetrics.CFPO, Double.valueOf(this.blockerIssuesCount));
    context.saveMeasure(FortifyMetrics.HFPO, Double.valueOf(this.criticalIssuesCount));
    context.saveMeasure(FortifyMetrics.MFPO, Double.valueOf(this.majorIssuesCount));
    context.saveMeasure(FortifyMetrics.LFPO, Double.valueOf(this.minorIssuesCount));
    if (this.blockerIssuesCount > 0) {
      context.saveMeasure(FortifyMetrics.SECURITY_RATING, FortifySensor.BLOCKER_SECURITY_RATING_LEVEL);
    } else if (this.criticalIssuesCount > 0) {
      context.saveMeasure(FortifyMetrics.SECURITY_RATING, FortifySensor.CRITICAL_SECURITY_RATING_LEVEL);
    } else if (this.majorIssuesCount > 0) {
      context.saveMeasure(FortifyMetrics.SECURITY_RATING, FortifySensor.MAJOR_SECURITY_RATING_LEVEL);
    } else if (this.minorIssuesCount > 0) {
      context.saveMeasure(FortifyMetrics.SECURITY_RATING, FortifySensor.MINOR_SECURITY_RATING_LEVEL);
    } else {
      context.saveMeasure(FortifyMetrics.SECURITY_RATING, FortifySensor.DEFAULT_SECURITY_RATING_LEVEL);
    }
  }

  @CheckForNull
  private ActiveRule getRule(String ruleKey, String fileLanguage) {
    // Search in priority the same language as the file
    ActiveRule rule = activeRules.find(RuleKey.of(FortifyConstants.fortifyRepositoryKey(fileLanguage), ruleKey));
    if (rule != null) {
      return rule;
    }
    // Search rule in other languages
    for (String language : this.fileSystem.languages()) {
      String repositoryKey = FortifyConstants.fortifyRepositoryKey(language);
      rule = this.activeRules.find(RuleKey.of(repositoryKey, ruleKey));
      if (rule != null) {
        return rule;
      }
    }
    return null;
  }

  @CheckForNull
  private InputFile resourceOf(SensorContext context, String sourceBasePath, Vulnerability vulnerability, Project project) {
    java.io.File file = new java.io.File(sourceBasePath, vulnerability.getPath());
    if (file.exists()) {
      InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().is(file));
      if (inputFile == null) {
        LOG.debug("File \"{}\" is not under module basedir or is not indexed. Skip it.", vulnerability.getPath());
        return null;
      }
      return inputFile;
    }
    LOG.debug("Unable to find \"{}\". Trying relative path.", file);
    file = new java.io.File(this.fileSystem.baseDir(), vulnerability.getPath());
    if (file.exists()) {
      InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().is(file));
      if (inputFile == null) {
        LOG.debug("File \"{}\" is not indexed. Skip it.", vulnerability.getPath());
        return null;
      }
      return inputFile;
    }
    LOG.debug("Unable to find \"{}\". Your Fortify analysis was probably started from a different location than current SonarQube analysis.", file);
    return null;
  }

  @Override
  public String toString() {
    return "Fortify sensor";
  }
}
