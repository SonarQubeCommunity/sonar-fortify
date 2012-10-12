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

import com.google.common.collect.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.JavaFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.fortify.client.FortifyClient;
import org.sonar.plugins.fortify.client.IssueWrapper;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IssueSensorTest {
  @Test
  public void should_execute_if_fortify_project_exists_and_rules_are_enabled() {
    RulesProfile profile = new RulesProfile();
    profile.activateRule(Rule.create("fortify-java", "SQL Injection"), RulePriority.MAJOR);

    FortifyProject fortifyProject = mock(FortifyProject.class);
    when(fortifyProject.exists()).thenReturn(true);

    IssueSensor sensor = new IssueSensor(mock(FortifyClient.class), fortifyProject, profile);

    assertThat(sensor.shouldExecuteOnProject(newJavaProject())).isTrue();
  }

  @Test
  public void should_not_be_executed_if_no_fortify_project() {
    RulesProfile profile = new RulesProfile();
    profile.activateRule(Rule.create("fortify-java", "SQL Injection"), RulePriority.MAJOR);

    FortifyProject fortifyProject = mock(FortifyProject.class);
    when(fortifyProject.exists()).thenReturn(false);

    IssueSensor sensor = new IssueSensor(mock(FortifyClient.class), fortifyProject, profile);

    assertThat(sensor.shouldExecuteOnProject(newJavaProject())).isFalse();
  }

  @Test
  public void should_not_be_executed_if_rules_disabled() {
    RulesProfile profile = new RulesProfile();
    FortifyProject fortifyProject = mock(FortifyProject.class);
    when(fortifyProject.exists()).thenReturn(true);

    IssueSensor sensor = new IssueSensor(mock(FortifyClient.class), fortifyProject, profile);

    assertThat(sensor.shouldExecuteOnProject(newJavaProject())).isFalse();
  }

  private Project newJavaProject() {
    return new Project("foo").setLanguageKey("java");
  }

  @Test
  public void test_to_string() {
    IssueSensor sensor = new IssueSensor(mock(FortifyClient.class), mock(FortifyProject.class), new RulesProfile());
    // overridden, not something like "org.sonar.plugins.fortify.batch.IssueSensor@276bab54"
    assertThat(sensor.toString()).doesNotContain("@");
  }

  @Test
  public void should_create_violations_on_enabled_rules() {
    RulesProfile profile = new RulesProfile();
    profile.activateRule(Rule.create("fortify-java", "SQL Injection").setConfigKey("SQL"), RulePriority.MAJOR);
    FortifyClient client = mock(FortifyClient.class);
    when(client.getIssues(3L)).thenReturn(Lists.newArrayList(
      new IssueWrapper().setRuleConfigKey("SQL").setFilePath("src/main/java/Foo.java").setLine(40).setHtmlAbstract("message"),
      new IssueWrapper().setRuleConfigKey("SQL").setFilePath("src/main/java/Bar.java").setLine(50).setHtmlAbstract("another message"),
      new IssueWrapper().setRuleConfigKey("XSS").setFilePath("src/main/java/Bar.java").setLine(20).setHtmlAbstract("this rule is disabled")
    ));

    FortifyProject fortifyProject = mock(FortifyProject.class);
    when(fortifyProject.getVersionId()).thenReturn(3L);
    IssueSensor.ResourceMatcher resourceMatcher = mock(IssueSensor.ResourceMatcher.class, Mockito.withSettings().defaultAnswer(Mockito.RETURNS_SMART_NULLS));
    IssueSensor sensor = new IssueSensor(client, fortifyProject, profile, resourceMatcher);
    SensorContext context = mock(SensorContext.class);
    when(context.isIndexed(any(Resource.class), eq(false))).thenReturn(true);

    sensor.analyse(newJavaProject(), context);

    verify(context).saveViolation(argThat(new ViolationMatcher("SQL Injection", 40, "message")));
    verify(context).saveViolation(argThat(new ViolationMatcher("SQL Injection", 50, "another message")));
  }

  @Test
  public void should_not_create_violations_on_unknown_files() {
    RulesProfile profile = new RulesProfile();
    profile.activateRule(Rule.create("fortify-java", "SQL Injection").setConfigKey("SQL"), RulePriority.MAJOR);
    FortifyClient client = mock(FortifyClient.class);
    when(client.getIssues(3L)).thenReturn(Lists.newArrayList(
      new IssueWrapper().setRuleConfigKey("SQL").setFilePath("src/main/java/Foo.java").setLine(40).setHtmlAbstract("message")
    ));

    FortifyProject fortifyProject = mock(FortifyProject.class);
    when(fortifyProject.getVersionId()).thenReturn(3L);
    IssueSensor.ResourceMatcher resourceMatcher = mock(IssueSensor.ResourceMatcher.class, Mockito.withSettings().defaultAnswer(Mockito.RETURNS_SMART_NULLS));
    IssueSensor sensor = new IssueSensor(client, fortifyProject, profile, resourceMatcher);
    SensorContext context = mock(SensorContext.class);

    // file is not indexed
    when(context.isIndexed(any(Resource.class), eq(false))).thenReturn(false);

    sensor.analyse(newJavaProject(), context);

    verify(context, never()).saveViolation(any(Violation.class));
  }

  @Test
  public void should_match_java_file() {
    IssueWrapper issue = new IssueWrapper().setFilePath("src/main/java/foo/Hello.java").setPackageName("foo").setClassName("Hello");
    ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
    when(fileSystem.getBasedir()).thenReturn(new File("."));
    Resource resource = new IssueSensor.ResourceMatcher().resourceOf(issue, fileSystem);

    assertThat(resource).isInstanceOf(JavaFile.class);
    assertThat(resource.getKey()).isEqualTo("foo.Hello");
    assertThat(((JavaFile) resource).getParent().getKey()).isEqualTo("foo");
  }

  static class ViolationMatcher extends BaseMatcher<Violation> {
    private String ruleKey;
    private int line;
    private String message;

    ViolationMatcher(String ruleKey, int line, String message) {
      this.ruleKey = ruleKey;
      this.line = line;
      this.message = message;
    }

    public boolean matches(Object o) {
      Violation v = (Violation) o;
      return ruleKey.equals(v.getRule().getKey()) && v.getLineId() == line && message.equals(v.getMessage());
    }

    public void describeTo(Description description) {
    }
  }
}
