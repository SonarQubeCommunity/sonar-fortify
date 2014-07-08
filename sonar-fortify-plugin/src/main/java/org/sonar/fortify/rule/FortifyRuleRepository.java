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
package org.sonar.fortify.rule;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.fortify.base.FortifyConstants;
import org.sonar.fortify.rule.element.FormatVersion;
import org.sonar.fortify.rule.element.RulePack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FortifyRuleRepository extends RuleRepository {
  private static final Logger LOG = LoggerFactory.getLogger(FortifyRuleRepository.class);

  private static final Map<String, String> FORTIFY_TO_SQ = ImmutableMap.of(
    "actionscript", "flex",
    "javascript", "js",
    "dotnet", "cs"
    );

  private final List<RulePack> rulePacks;
  private List<Rule> rules;
  private final String language;
  private final Map<String, FormatVersion> addedRules = new HashMap<String, FormatVersion>();

  FortifyRuleRepository(List<RulePack> rulePacks, String language) {
    super(FortifyConstants.fortifyRepositoryKey(language), language);
    setName("Fortify");
    this.rulePacks = rulePacks;
    this.language = language;
  }

  @Override
  public List<Rule> createRules() {
    if (rules == null) {
      rules = createRules(rulePacks);
    }
    return rules;
  }

  private List<Rule> createRules(List<RulePack> rulePacks) {
    Map<String, Rule> rules = new HashMap<String, Rule>();

    for (RulePack rulePack : rulePacks) {
      for (org.sonar.fortify.rule.element.Rule rule : rulePack.getRules()) {
        if (isAnInterestingRule(rulePack, rule)) {
          rules.put(rule.getRuleID(), createRule(this.language, rulePack, rule));
          this.addedRules.put(rule.getRuleID(), rule.getFormatVersion());
        }
      }
    }
    return new ArrayList<Rule>(rules.values());
  }

  private boolean isAnInterestingRule(RulePack rulePack, org.sonar.fortify.rule.element.Rule rule) {
    boolean isInteresting;
    if (this.language.equals(convertToSQ(rulePack.getRuleLanguage(rule)))) {
      FormatVersion previousVersion = this.addedRules.get(rule.getRuleID());
      if (previousVersion == null) {
        isInteresting = true;
      } else if (previousVersion.compareTo(rule.getFormatVersion()) > 0) {
        LOG.debug("The rule {} was already added in formatVersion {}, ignoring one with formatVersion {}.", rule.getRuleID(), previousVersion,
          rule.getFormatVersion());
        isInteresting = false;
      } else if (previousVersion.compareTo(rule.getFormatVersion()) == 0) {
        LOG.debug("The rule {} was already added in formatVersion {}.", rule.getRuleID(), previousVersion);
        isInteresting = false;
      } else {
        LOG.debug("The rule {} was already added in formatVersion {}, replace it by the one with formatVersion {}.", rule.getRuleID(), previousVersion,
          rule.getFormatVersion());
        isInteresting = true;
      }
    } else {
      isInteresting = false;
    }

    return isInteresting;
  }

  private String convertToSQ(String ruleLanguage) {
    if (FORTIFY_TO_SQ.containsKey(ruleLanguage)) {
      return FORTIFY_TO_SQ.get(ruleLanguage);
    }
    return ruleLanguage;
  }

  private Rule createRule(String language, RulePack rulePack, org.sonar.fortify.rule.element.Rule fortifyRule) {
    Rule rule = Rule.create(FortifyConstants.fortifyRepositoryKey(language), fortifyRule.getRuleID(), fortifyRule.getName());
    rule.setDescription(rulePack.getHTMLDescription(fortifyRule.getDescription()));
    rule.setLanguage(language);
    rule.setSeverity(org.sonar.api.rules.RulePriority.valueOf(fortifyRule.getDefaultSeverity()));

    return rule;
  }
}
