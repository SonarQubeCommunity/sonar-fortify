/*
 * SonarQube Fortify Plugin
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

import org.junit.Test;
import xmlns.www_fortify_com.schema.issuemanagement.InstanceInfo;
import xmlns.www_fortify_com.schema.issuemanagement.IssueInstance;
import xmlns.www_fortify_com.schema.issuemanagement.IssueLocation;

import static org.fest.assertions.Assertions.assertThat;

public class IssueWrapperTest {
  @Test
  public void test_create() {
    IssueInstance fortifyIssue = new IssueInstance();
    InstanceInfo instanceInfo = new InstanceInfo();
    IssueLocation location = new IssueLocation();
    location.setLineNumber(3);
    location.setSourceFilePath("src/main/java/foo/Caller.java");
    location.setFilePath("src/main/java/foo/Callee.java");
    location.setPackage("foo");
    location.setClassName("Callee");
    instanceInfo.setIssueLocation(location);
    fortifyIssue.setGroupName("SQL Injection");
    fortifyIssue.setInstanceInfo(instanceInfo);

    IssueWrapper issue = IssueWrapper.create(fortifyIssue);
    assertThat(issue.getHtmlAbstract()).isNull();
    assertThat(issue.getTextAbstract()).isNull();
    assertThat(issue.getLine()).isEqualTo(3);
    assertThat(issue.getRuleConfigKey()).isEqualTo("SQL Injection");
    assertThat(issue.getFilePath()).isEqualTo("src/main/java/foo/Callee.java");
    assertThat(issue.getPackageName()).isEqualTo("foo");
    assertThat(issue.getClassName()).isEqualTo("Callee");
  }

  @Test
  public void test_abstract() {
    IssueInstance fortifyIssue = new IssueInstance();
    InstanceInfo instanceInfo = new InstanceInfo();
    IssueLocation location = new IssueLocation();
    location.setLineNumber(3);
    location.setSourceFilePath("src/main/java/foo/Caller.java");
    location.setFilePath("src/main/java/foo/Callee.java");
    instanceInfo.setIssueLocation(location);
    fortifyIssue.setInstanceInfo(instanceInfo);
    IssueWrapper issue = IssueWrapper.create(fortifyIssue);

    issue.setHtmlAbstract("this is the html abstract");
    assertThat(issue.getHtmlAbstract()).isEqualTo("this is the html abstract");
    assertThat(issue.getTextAbstract()).isEqualTo(issue.getHtmlAbstract());

    issue.setHtmlAbstract("this is the <font color='#ddd'>html</font> abstract");
    assertThat(issue.getHtmlAbstract()).isEqualTo("this is the <font color='#ddd'>html</font> abstract");
    assertThat(issue.getTextAbstract()).isEqualTo("this is the html abstract");
  }

  @Test
  public void should_sanitize_html_abstract() {
    assertThat(IssueWrapper.sanitizeHtml(null)).isEqualTo(null);
    assertThat(IssueWrapper.sanitizeHtml("")).isEqualTo("");
    assertThat(IssueWrapper.sanitizeHtml("foo")).isEqualTo("foo");
    assertThat(IssueWrapper.sanitizeHtml("The method <font color='#669FD5'>_jspService()</font> in <font color='#669FD5'>test.jsp</font> sends unvalidated data to a web browser"))
      .isEqualTo("The method _jspService() in test.jsp sends unvalidated data to a web browser");
  }
}
