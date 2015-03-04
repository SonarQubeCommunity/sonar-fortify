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
import org.sonar.fortify.base.AnalysisState;
import org.sonar.fortify.base.FortifyConstants;
import org.sonar.fortify.base.metrics.FortifyMetrics;
import org.sonar.fortify.fvdl.element.Fvdl;
import org.sonar.fortify.fvdl.element.Vulnerability;
import org.xml.sax.SAXException;

import javax.annotation.CheckForNull;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public class FortifySensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(FortifySensor.class);

  private final FortifySensorConfiguration configuration;
  private final ResourcePerspectives resourcePerspectives;
  private final FileSystem fileSystem;
  private final ActiveRules activeRules;
  private final FortifyReportFile report;

  private final int[] blockerIssuesCounts = new int[] {0, 0, 0, 0, 0, 0};
  private final int[] criticalIssuesCounts = new int[] {0, 0, 0, 0, 0, 0};
  private final int[] majorIssuesCounts = new int[] {0, 0, 0, 0, 0, 0};
  private final int[] minorIssuesCounts = new int[] {0, 0, 0, 0, 0, 0};
  private final int[] infoIssuesCounts = new int[] {0, 0, 0, 0, 0, 0};

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
        incrementCount(vulnerability, severity);
      }
    }
  }

  private void incrementCount(Vulnerability vulnerability, String severity) {
    if (Severity.BLOCKER.equals(severity)) {
      this.blockerIssuesCounts[vulnerability.getAnalysisState().ordinal()]++;
    } else if (Severity.CRITICAL.equals(severity)) {
      this.criticalIssuesCounts[vulnerability.getAnalysisState().ordinal()]++;
    } else if (Severity.MAJOR.equals(severity)) {
      this.majorIssuesCounts[vulnerability.getAnalysisState().ordinal()]++;
    } else if (Severity.MINOR.equals(severity)) {
      this.minorIssuesCounts[vulnerability.getAnalysisState().ordinal()]++;
    } else {
      this.infoIssuesCounts[vulnerability.getAnalysisState().ordinal()]++;
    }
  }

  private void addIssues(SensorContext context, Project project, Fvdl fvdl) {
    String sourceBasePath = fvdl.getBuild().getSourceBasePath();
    for (Vulnerability vulnerability : fvdl.getVulnerabilities()) {
      InputFile inputFile = resourceOf(context, sourceBasePath, vulnerability, project);
      if (inputFile != null) {
        ActiveRule activeRule = getRule(vulnerability, inputFile.language());
        if (activeRule == null) {
          FortifySensor.LOG.debug(
            "Fortify rule '{}' is not active in quality profiles of your project.", vulnerability.getClassID());
        } else {
          addIssue(inputFile, fvdl, vulnerability, activeRule);
        }
      }
    }
  }

  private Fvdl parseFvdl() throws IOException, ParserConfigurationException, SAXException {
    InputStream stream = this.report.getFvdlInputStream();
    try {
      return new FvdlStAXParser().parse(stream);
    } finally {
      stream.close();
    }
  }

  private Map<String, AnalysisState> parseAudit() throws IOException, ParserConfigurationException, SAXException {
    InputStream stream = this.report.getAuditInputStream();
    if (stream == null) {
      return Collections.emptyMap();
    } else {
      try {
        return new AuditStAXParser().parse(stream);
      } finally {
        stream.close();
      }
    }
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    TimeProfiler profiler = new TimeProfiler().start("Process Fortify report");
    try {
      Fvdl fvdl = parseFvdl();
      Map<String, AnalysisState> analysisStates = parseAudit();
      for (Vulnerability vulnerability : fvdl.getVulnerabilities()) {
        AnalysisState analysisState = analysisStates.get(vulnerability.getInstanceID());
        if (analysisState != null) {
          vulnerability.setAnalysisState(analysisState);
        }
      }
      addIssues(context, project, fvdl);
    } catch (Exception e) {
      throw new IllegalStateException("Can not process Fortify report", e);
    } finally {
      profiler.stop();
    }
    saveMeasures(context);
  }

  private void saveMeasures(SensorContext context) {
    int blockerIssuesCount = sumOf(this.blockerIssuesCounts);
    int criticalIssuesCount = sumOf(this.criticalIssuesCounts);
    int majorIssuesCount = sumOf(this.majorIssuesCounts);
    int minorIssuesCount = sumOf(this.minorIssuesCounts);
    int badPracticeCount = sumOf(AnalysisState.BAD_PRACTICE);
    int exploitableCount = sumOf(AnalysisState.EXPLOITABLE);
    int notAnIssueCount = sumOf(AnalysisState.NOT_AN_ISSUE);
    int notAuditedCount = sumOf(AnalysisState.NOT_AUDITED);
    int reliabilityIssueCount = sumOf(AnalysisState.RELIABILITY_ISSUE);
    int suspiciousCount = sumOf(AnalysisState.SUSPICIOUS);

    context.saveMeasure(FortifyMetrics.CFPO, Double.valueOf(blockerIssuesCount));
    context.saveMeasure(FortifyMetrics.HFPO, Double.valueOf(criticalIssuesCount));
    context.saveMeasure(FortifyMetrics.MFPO, Double.valueOf(majorIssuesCount));
    context.saveMeasure(FortifyMetrics.LFPO, Double.valueOf(minorIssuesCount));

    context.saveMeasure(FortifyMetrics.AUDIT_BAD_PRACTICE, Double.valueOf(badPracticeCount));
    context.saveMeasure(FortifyMetrics.AUDIT_EXPLOITABLE, Double.valueOf(exploitableCount));
    context.saveMeasure(FortifyMetrics.AUDIT_NOT_AN_ISSUE, Double.valueOf(notAnIssueCount));
    context.saveMeasure(FortifyMetrics.AUDIT_NOT_AUDITED, Double.valueOf(notAuditedCount));
    context.saveMeasure(FortifyMetrics.AUDIT_RELIABILITY_ISSUE, Double.valueOf(reliabilityIssueCount));
    context.saveMeasure(FortifyMetrics.AUDIT_SUSPICIOUS, Double.valueOf(suspiciousCount));

    context.saveMeasure(FortifyMetrics.CRITICAL_NOT_AUDITED_ISSUES, Double.valueOf(this.blockerIssuesCounts[AnalysisState.NOT_AUDITED.ordinal()]));
    context.saveMeasure(FortifyMetrics.HIGH_NOT_AUDITED_ISSUES, Double.valueOf(this.criticalIssuesCounts[AnalysisState.NOT_AUDITED.ordinal()]));
    context.saveMeasure(FortifyMetrics.MEDIUM_NOT_AUDITED_ISSUES, Double.valueOf(this.majorIssuesCounts[AnalysisState.NOT_AUDITED.ordinal()]));
    context.saveMeasure(FortifyMetrics.LOW_NOT_AUDITED_ISSUES, Double.valueOf(this.minorIssuesCounts[AnalysisState.NOT_AUDITED.ordinal()]));
  }

  private int sumOf(int[] counts) {
    int sum = 0;
    for (int i : counts) {
      sum += i;
    }
    return sum;
  }

  private int sumOf(AnalysisState analysisState) {
    int i = analysisState.ordinal();
    return this.blockerIssuesCounts[i] + this.criticalIssuesCounts[i] + this.majorIssuesCounts[i] + this.minorIssuesCounts[i] + this.infoIssuesCounts[i];
  }

  @CheckForNull
  private ActiveRule getRule(Vulnerability vulnerability, String fileLanguage) {
    String ruleKey = FortifyConstants.fortifySQRuleKey(vulnerability.getKingdom(), vulnerability.getType(), vulnerability.getSubtype());
    if (ruleKey != null) {
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
    }
    LOG.debug("Unable to find rule for vulnerability " + vulnerability);
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
