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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RulePack {
  private String name;
  private String language;
  private final Map<String, String> descriptions = new HashMap<String, String>();
  private final Collection<Rule> rules = new ArrayList<Rule>();

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLanguage() {
    return this.language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void addDescriptions(Collection<Description> newDescriptions) {
    if (newDescriptions != null) {
      for (Description description : newDescriptions) {
        this.descriptions.put(description.getId(), description.toString());
      }
    }
  }

  public Collection<Rule> getRules() {
    return this.rules;
  }

  public void addRules(Collection<Rule> rules) {
    if (rules != null) {
      this.rules.addAll(rules);
    }
  }

  public String getRuleLanguage(Rule rule) {
    String ruleLanguage = rule.getLanguage();
    if (ruleLanguage == null) {
      ruleLanguage = this.language;
    }

    return ruleLanguage;
  }

  public String getHTMLDescription(Description description) {
    String htmlDescription;
    String ref = description.getRef();
    if (ref == null) {
      htmlDescription = description.toString();
    } else {
      htmlDescription = this.descriptions.get(ref);
    }

    return htmlDescription;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[RulePack\n\t");
    builder.append("name=").append(this.name).append("\n\t");
    builder.append("language=").append(this.language).append("\n\t");
    builder.append("descriptions.size()=").append(this.descriptions.size()).append("\n\t");
    builder.append("descriptions=[\n\t\t");
    for (Entry<String, String> description : this.descriptions.entrySet()) {
      builder.append(description).append(",\n\t\t");
    }
    builder.append("\n\t]");
    builder.append("rules.size()=").append(this.rules.size()).append("\n\t");
    builder.append("rules=[\n\t\t");
    int i = 0;
    for (Rule rule : this.rules) {
      i++;
      builder.append(rule).append(",\n\t\t");
      if (i > 10) {
        break;
      }
    }
    builder.append("\n\t]");
    builder.append("\n]");
    return builder.toString();
  }
}
