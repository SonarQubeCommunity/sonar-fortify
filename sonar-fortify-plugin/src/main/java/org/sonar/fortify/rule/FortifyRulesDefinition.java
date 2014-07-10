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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Languages;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.fortify.base.FortifyConstants;
import org.sonar.fortify.rule.element.FormatVersion;
import org.sonar.fortify.rule.element.FortifyRule;
import org.sonar.fortify.rule.element.RulePack;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FortifyRulesDefinition implements RulesDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(FortifyRulesDefinition.class);

  private static final Map<String, String> FORTIFY_TO_SQ = ImmutableMap.<String, String>builder()
    .put("actionscript", "flex")
    .put("javascript", "js")
    .put("dotnet", "cs")
    .put("configuration", "xml")
    .put("content", "web")
    .put("jsp", "web")
    .put("python", "py")
    .build();

  private final Languages languages;
  private final RulePackParser rulePackParser;

  private final Map<String, NewRepository> newRepositories = new HashMap<String, NewRepository>();
  private final Map<String, FormatVersion> addedRulesVersions = new HashMap<String, FormatVersion>();
  private final Map<String, Map<String, Set<String>>> addedRuleIdsByLanguageAndName = new HashMap<String, Map<String, Set<String>>>();

  public FortifyRulesDefinition(Settings settings, Languages languages) {
    this(new RulePackParser(settings), languages);
  }

  @VisibleForTesting
  FortifyRulesDefinition(RulePackParser rulePackParser, Languages languages) {
    this.rulePackParser = rulePackParser;
    this.languages = languages;
  }

  @Override
  public void define(Context context) {
    List<RulePack> rulePacks = rulePackParser.parse();
    for (NewRepository newRepository : parseRulePacks(context, rulePacks)) {
      newRepository.done();
    }
  }

  private Collection<NewRepository> parseRulePacks(Context context, List<RulePack> rulePacks) {
    for (RulePack rulePack : rulePacks) {
      for (FortifyRule rule : rulePack.getRules()) {
        String sqLanguageKey = convertToSQ(rulePack.getRuleLanguage(rule));
        if (languages.get(sqLanguageKey) != null && isAnInterestingRule(rulePack, rule)) {
          processRule(context, rulePack, rule, sqLanguageKey);
        }
      }
    }

    return newRepositories.values();
  }

  private void processRule(Context context, RulePack rulePack, FortifyRule rule, String sqLanguageKey) {
    NewRepository repo = getRepository(context, sqLanguageKey);
    String htmlDescription = rulePack.getHTMLDescription(rule.getDescription());
    NewRule newRule = repo.rule(rule.getRuleID());
    if (newRule == null) {
      newRule = repo
        .createRule(rule.getRuleID());
    }
    String name = rule.getName();
    if (!addedRuleIdsByLanguageAndName.containsKey(sqLanguageKey)) {
      addedRuleIdsByLanguageAndName.put(sqLanguageKey, new HashMap<String, Set<String>>());
    }
    Map<String, Set<String>> addedRuleIdsByName = addedRuleIdsByLanguageAndName.get(sqLanguageKey);
    if (addedRuleIdsByName.containsKey(name)) {
      Set<String> ruleIds = addedRuleIdsByName.get(name);
      if (ruleIds.size() == 1) {
        NewRule alreadyAdded = repo.rule(ruleIds.iterator().next());
        alreadyAdded.setName(name + " - #1");
      }
      ruleIds.add(rule.getRuleID());
      newRule
        .setName(name + " - #" + ruleIds.size());
    } else {
      Set<String> ruleIds = new HashSet<String>();
      ruleIds.add(rule.getRuleID());
      addedRuleIdsByName.put(name, ruleIds);
      newRule
        .setName(name);
    }
    newRule
      .setHtmlDescription(StringUtils.isNotBlank(htmlDescription) ? htmlDescription : "No description available")
      .setSeverity(rule.getDefaultSeverity())
      .setTags(rule.getTags());
    this.addedRulesVersions.put(rule.getRuleID(), rule.getFormatVersion());
  }

  private boolean isAnInterestingRule(RulePack rulePack, org.sonar.fortify.rule.element.FortifyRule rule) {
    boolean isInteresting;
    FormatVersion previousVersion = this.addedRulesVersions.get(rule.getRuleID());
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

    return isInteresting;
  }

  private NewRepository getRepository(Context context, String sqLanguageKey) {
    String repoKey = FortifyConstants.fortifyRepositoryKey(sqLanguageKey);
    if (!newRepositories.containsKey(repoKey)) {
      newRepositories.put(repoKey, context.createRepository(repoKey, sqLanguageKey).setName("Fortify"));
    }
    return newRepositories.get(repoKey);
  }

  private String convertToSQ(String ruleLanguage) {
    if (FORTIFY_TO_SQ.containsKey(ruleLanguage)) {
      return FORTIFY_TO_SQ.get(ruleLanguage);
    }
    return ruleLanguage;
  }

}
