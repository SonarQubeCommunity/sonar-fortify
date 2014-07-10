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

import org.apache.commons.lang.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FortifyRule {
  // According to Fortify documentation 3.90, language is not in the XSD, but it's in their RulePack :/
  private String language;
  private FormatVersion formatVersion;
  private String ruleID;
  private String notes;
  private String vulnKingdom;
  private String vulnCategory;
  private String vulnSubcategory;
  private String defaultSeverity;
  private Description description;

  public String getLanguage() {
    return this.language;
  }

  public FortifyRule setLanguage(String language) {
    this.language = language;
    return this;
  }

  public FormatVersion getFormatVersion() {
    return this.formatVersion;
  }

  public FortifyRule setFormatVersion(String formatVersion) {
    this.formatVersion = new FormatVersion(formatVersion);
    return this;
  }

  public String getRuleID() {
    return this.ruleID;
  }

  public FortifyRule setRuleID(String ruleID) {
    this.ruleID = ruleID;
    return this;
  }

  public FortifyRule setNotes(String notes) {
    this.notes = notes;
    return this;
  }

  public FortifyRule setVulnCategory(String vulnCategory) {
    this.vulnCategory = vulnCategory;
    return this;
  }

  public FortifyRule setVulnKingdom(String vulnKingdom) {
    this.vulnKingdom = vulnKingdom;
    return this;
  }

  public FortifyRule setVulnSubcategory(String vulnSubcategory) {
    this.vulnSubcategory = vulnSubcategory;
    return this;
  }

  public String getDefaultSeverity() {
    return this.defaultSeverity;
  }

  public FortifyRule setDefaultSeverity(String defaultSeverity) {
    this.defaultSeverity = defaultSeverity;
    return this;
  }

  public Description getDescription() {
    return this.description;
  }

  public FortifyRule setDescription(Description description) {
    this.description = description;
    return this;
  }

  public String getName() {
    if (StringUtils.isNotBlank(vulnCategory)) {
      StringBuilder sb = new StringBuilder();
      sb.append(vulnCategory);
      if (StringUtils.isNotBlank(vulnSubcategory)) {
        sb.append(": ");
        sb.append(vulnSubcategory);
      }
      return sb.toString();
    } else if (StringUtils.isNotBlank(vulnSubcategory)) {
      return vulnSubcategory;
    }
    return ruleID;
  }

  @Override
  public String toString() {
    return "[Rule language=" + this.language + ", formatVersion=" + this.formatVersion + ", ruleID=" + this.ruleID + ", vulnCategory=" + this.vulnCategory + ", vulnSubcategory="
      + this.vulnSubcategory + ", defaultSeverity=" + this.defaultSeverity + ", description=" + this.description + "]";
  }

  public String[] getTags() {
    List<String> tags = new ArrayList<String>();
    if (StringUtils.isNotBlank(vulnKingdom)) {
      tags.add(slugify(vulnKingdom));
    }
    if (StringUtils.isNotBlank(vulnCategory)) {
      tags.add(slugify(vulnCategory));
    }
    if (StringUtils.isNotBlank(vulnSubcategory)) {
      tags.add(slugify(vulnSubcategory));
    }
    return tags.toArray(new String[tags.size()]);
  }

  private static String slugify(String s) {
    return Normalizer.normalize(s, Normalizer.Form.NFD)
      .replaceAll("[^\\p{ASCII}]", "")
      .replaceAll("[^\\w+]", "-")
      .replaceAll("\\s+", "-")
      .replaceAll("_", "-")
      .replaceAll("[-]+", "-")
      .replaceAll("^-", "")
      .replaceAll("-$", "").toLowerCase(Locale.ENGLISH);
  }

}
