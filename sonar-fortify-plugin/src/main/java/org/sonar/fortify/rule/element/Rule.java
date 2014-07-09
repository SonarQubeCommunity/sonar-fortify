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
package org.sonar.fortify.rule.element;

public class Rule {
  // According to Fortify documentation 3.90, language is not in the XSD, but it's in their RulePack :/
  private String language;
  private FormatVersion formatVersion;
  private String ruleID;
  private String notes;
  private String vulnCategory;
  private String vulnSubcategory;
  private String defaultSeverity;
  private Description description;

  public String getLanguage() {
    return this.language;
  }

  public Rule setLanguage(String language) {
    this.language = language;
    return this;
  }

  public FormatVersion getFormatVersion() {
    return this.formatVersion;
  }

  public Rule setFormatVersion(String formatVersion) {
    this.formatVersion = new FormatVersion(formatVersion);
    return this;
  }

  public String getRuleID() {
    return this.ruleID;
  }

  public Rule setRuleID(String ruleID) {
    this.ruleID = ruleID;
    return this;
  }

  public Rule setNotes(String notes) {
    this.notes = notes;
    return this;
  }

  public Rule setVulnCategory(String vulnCategory) {
    this.vulnCategory = vulnCategory;
    return this;
  }

  public Rule setVulnSubcategory(String vulnSubcategory) {
    this.vulnSubcategory = vulnSubcategory;
    return this;
  }

  public String getDefaultSeverity() {
    return this.defaultSeverity;
  }

  public Rule setDefaultSeverity(String defaultSeverity) {
    this.defaultSeverity = defaultSeverity;
    return this;
  }

  public Description getDescription() {
    return this.description;
  }

  public Rule setDescription(Description description) {
    this.description = description;
    return this;
  }

  public String getName() {
    return ruleID;
  }

  @Override
  public String toString() {
    return "[Rule language=" + this.language + ", formatVersion=" + this.formatVersion + ", ruleID=" + this.ruleID + ", vulnCategory=" + this.vulnCategory + ", vulnSubcategory="
      + this.vulnSubcategory + ", defaultSeverity=" + this.defaultSeverity + ", description=" + this.description + "]";
  }

}
