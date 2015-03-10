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

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.fortify.base.metrics.FortifyMetrics;

import java.io.File;
import java.net.URISyntaxException;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FortifySensorTest {
  private FortifySensorConfiguration configuration;
  private ResourcePerspectives resourcePerspectives;
  private DefaultFileSystem fileSystem;
  private ActiveRules activeRules;
  private FortifySensor sensor;

  @Before
  public void init() {
    this.configuration = mock(FortifySensorConfiguration.class);
    this.resourcePerspectives = mock(ResourcePerspectives.class);
    this.fileSystem = new DefaultFileSystem();
    this.activeRules = mock(ActiveRules.class);
    this.sensor = new FortifySensor(this.configuration, this.resourcePerspectives, this.fileSystem, this.activeRules);
  }

  @Test
  public void shouldExecuteOnProjectTest() {
    when(this.configuration.isActive(anyListOf(String.class))).thenReturn(false);
    assertThat(this.sensor.shouldExecuteOnProject(null)).isFalse();

    when(this.configuration.isActive(anyListOf(String.class))).thenReturn(true);
    assertThat(this.sensor.shouldExecuteOnProject(null)).isFalse();
  }

  @Test
  public void toStringTest() {
    assertThat(this.sensor.toString()).isEqualTo("Fortify sensor");
  }

  @Test
  public void shouldAnalyse() throws URISyntaxException {
    when(this.configuration.getReportPath()).thenReturn("audit-simple.fvdl");
    Project project = new Project("foo");
    File baseDir = new File(this.getClass().getResource("/project/placeholder.txt").toURI()).getParentFile();
    fileSystem.setBaseDir(baseDir);
    fileSystem.addLanguages("web");
    ActiveRule activeRule = mock(ActiveRule.class);
    RuleKey ruleKey = RuleKey.of("fortify-web", "code_quality_unreleased_resource_database");
    when(activeRule.ruleKey()).thenReturn(ruleKey);
    when(this.activeRules.find(ruleKey)).thenReturn(activeRule);
    SensorContext context = mock(SensorContext.class);
    DefaultInputFile inputFile = new DefaultInputFile("WebContent/main.jsp").setFile(new File(baseDir, "WebContent/main.jsp"));
    fileSystem.add(inputFile);
    Issuable issuable = mock(Issuable.class);
    when(this.resourcePerspectives.as(Issuable.class, inputFile)).thenReturn(issuable);
    MockIssueBuilder mockIssueBuilder = new MockIssueBuilder();
    when(issuable.newIssueBuilder()).thenReturn(mockIssueBuilder);
    when(issuable.addIssue(any(Issue.class))).thenReturn(true);

    this.sensor.analyse(project, context);

    assertThat(mockIssueBuilder.ruleKey).isEqualTo(RuleKey.of("fortify-web", "code_quality_unreleased_resource_database"));
    assertThat(mockIssueBuilder.line).isEqualTo(163);
    assertThat(mockIssueBuilder.message)
      .isEqualTo(
        "The method _jspService() in main.jsp sends unvalidated data to a web browser on line 163, which can result in the browser executing malicious code.Sending unvalidated data to a web browser can result in the browser executing malicious code.");
    assertThat(mockIssueBuilder.severity).isEqualTo("BLOCKER");

    verify(context).saveMeasure(FortifyMetrics.CFPO, 1.0);
    verify(context).saveMeasure(FortifyMetrics.HFPO, 0.0);
    verify(context).saveMeasure(FortifyMetrics.MFPO, 0.0);
    verify(context).saveMeasure(FortifyMetrics.LFPO, 0.0);
    verify(context).saveMeasure(FortifyMetrics.SECURITY_RATING, 1.0);
  }

  private class MockIssueBuilder implements IssueBuilder {

    private RuleKey ruleKey;
    private Integer line;
    private String message;
    private String severity;

    @Override
    public IssueBuilder ruleKey(RuleKey ruleKey) {
      this.ruleKey = ruleKey;
      return this;
    }

    @Override
    public IssueBuilder line(Integer line) {
      this.line = line;
      return this;
    }

    @Override
    public IssueBuilder message(String message) {
      this.message = message;
      return this;
    }

    @Override
    public IssueBuilder severity(String severity) {
      this.severity = severity;
      return this;
    }

    @Override
    public IssueBuilder reporter(String reporter) {
      return this;
    }

    @Override
    public IssueBuilder effortToFix(Double d) {
      return this;
    }

    @Override
    public IssueBuilder attribute(String key, String value) {
      return this;
    }

    @Override
    public Issue build() {
      return null;
    }

  }
}
