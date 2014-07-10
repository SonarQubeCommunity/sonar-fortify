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

public class RulePack {
  private String name;
  private String language;
  private final Map<String, String> descriptions = new HashMap<String, String>();
  private final Collection<FortifyRule> rules = new ArrayList<FortifyRule>();

  public String name() {
    return this.name;
  }

  public RulePack setName(String name) {
    this.name = name;
    return this;
  }

  public String language() {
    return this.language;
  }

  public RulePack setLanguage(String language) {
    this.language = language;
    return this;
  }

  public RulePack addDescription(Description newDescription) {
    this.descriptions.put(newDescription.getId(), newDescription.toString());
    return this;
  }

  public Collection<FortifyRule> getRules() {
    return this.rules;
  }

  public RulePack addRule(FortifyRule rule) {
    this.rules.add(rule);
    return this;
  }

  public String getRuleLanguage(FortifyRule rule) {
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

}
