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
package org.sonar.plugins.fortify.client;

import xmlns.www_fortify_com.schema.issuemanagement.IssueInstance;

public class IssueWrapper {
  private String ruleConfigKey;
  private String htmlAbstract;
  private int line;
  private String filePath;
  private String packageName;
  private String className;

  public String getRuleConfigKey() {
    return ruleConfigKey;
  }

  public IssueWrapper setRuleConfigKey(String ruleConfigKey) {
    this.ruleConfigKey = ruleConfigKey;
    return this;
  }

  public String getHtmlAbstract() {
    return htmlAbstract;
  }

  public IssueWrapper setHtmlAbstract(String htmlAbstract) {
    this.htmlAbstract = htmlAbstract;
    return this;
  }

  public int getLine() {
    return line;
  }

  public IssueWrapper setLine(int line) {
    this.line = line;
    return this;
  }

  public String getFilePath() {
    return filePath;
  }

  public IssueWrapper setFilePath(String filePath) {
    this.filePath = filePath;
    return this;
  }

  public String getPackageName() {
    return packageName;
  }

  public IssueWrapper setPackageName(String s) {
    this.packageName = s;
    return this;
  }

  public String getClassName() {
    return className;
  }

  public IssueWrapper setClassName(String s) {
    this.className = s;
    return this;
  }

  static IssueWrapper create(IssueInstance fortifyIssue) {
    return new IssueWrapper()
      .setLine(fortifyIssue.getInstanceInfo().getIssueLocation().getLineNumber())
      .setFilePath(fortifyIssue.getInstanceInfo().getIssueLocation().getFilePath())
      .setRuleConfigKey(fortifyIssue.getGroupName())
      .setPackageName(fortifyIssue.getInstanceInfo().getIssueLocation().getPackage())
      .setClassName(fortifyIssue.getInstanceInfo().getIssueLocation().getClassName());
  }
}
