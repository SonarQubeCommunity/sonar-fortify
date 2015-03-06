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
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Languages;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.fortify.base.FortifyConstants;
import org.sonar.fortify.rule.element.FormatVersion;
import org.sonar.fortify.rule.element.FortifyRule;
import org.sonar.fortify.rule.element.RulePack;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private final RulesDefinitionXmlLoader xmlLoader;

  private final Map<String, NewRepository> newRepositories = new HashMap<String, NewRepository>();
  private final Map<String, FormatVersion> addedRulesVersions = new HashMap<String, FormatVersion>();

  public FortifyRulesDefinition(Settings settings, Languages languages, RulesDefinitionXmlLoader xmlLoader) {
    this(new RulePackParser(settings), languages, xmlLoader);
  }

  @VisibleForTesting
  FortifyRulesDefinition(RulePackParser rulePackParser, Languages languages, RulesDefinitionXmlLoader xmlLoader) {
    this.rulePackParser = rulePackParser;
    this.languages = languages;
    this.xmlLoader = xmlLoader;
  }

  @Override
  public void define(Context context) {
    List<RulePack> rulePacks = this.rulePackParser.parse();
    parseXml(context);
    parseRulePacks(context, rulePacks);
    for (NewRepository newRepository : newRepositories.values()) {
      newRepository.done();
    }
  }

  private void parseXml(Context context) {
    for (Language supportedLanguage : languages.all()) {
      InputStream rulesXml = this.getClass().getResourceAsStream("/rules/rules-" + supportedLanguage.getKey() + ".xml");
      if (rulesXml != null) {
        NewRepository repository = getRepository(context, supportedLanguage.getKey());
        xmlLoader.load(repository, rulesXml, Charsets.UTF_8.name());
      }
    }
  }

  private void parseRulePacks(Context context, List<RulePack> rulePacks) {
    for (RulePack rulePack : rulePacks) {
      for (FortifyRule rule : rulePack.getRules()) {
        String sqLanguageKey = convertToSQ(rulePack.getRuleLanguage(rule));
        if (this.languages.get(sqLanguageKey) != null && isAnInterestingRule(rule)) {
          processRule(context, rulePack, rule, sqLanguageKey);
        }
      }
    }
  }

  private void processRule(Context context, RulePack rulePack, FortifyRule rule, String sqLanguageKey) {
    NewRepository repo = getRepository(context, sqLanguageKey);
    String htmlDescription = rulePack.getHTMLDescription(rule.getDescription());
    String sonarKey = rule.getSonarKey();
    if (sonarKey == null) {
      LOG.debug("Unable to determine rule key for " + rule + ". Ignoring it.");
      return;
    }
    NewRule newRule = repo.rule(sonarKey);
    if (newRule == null) {
      newRule = repo.createRule(rule.getSonarKey());
    }
    String name = rule.getName();
    if (name == null) {
      LOG.debug("Ignoring Fortify rule " + rule.getRuleID());
      return;
    }
    newRule
      .setName(name)
      .setHtmlDescription(StringUtils.isNotBlank(htmlDescription) ? htmlDescription : "No description available")
      .setSeverity(rule.getDefaultSeverity())
      .setTags(rule.getTags());
    this.addedRulesVersions.put(rule.getRuleID(), rule.getFormatVersion());
  }

  private boolean isAnInterestingRule(FortifyRule rule) {
    boolean isInteresting;
    FormatVersion previousVersion = this.addedRulesVersions.get(rule.getRuleID());
    if (previousVersion == null) {
      isInteresting = true;
    } else if (previousVersion.compareTo(rule.getFormatVersion()) > 0) {
      FortifyRulesDefinition.LOG.debug("The rule {} was already added in formatVersion {}, ignoring one with formatVersion {}.", rule.getRuleID(), previousVersion,
        rule.getFormatVersion());
      isInteresting = false;
    } else if (previousVersion.compareTo(rule.getFormatVersion()) == 0) {
      FortifyRulesDefinition.LOG.debug("The rule {} was already added in formatVersion {}.", rule.getRuleID(), previousVersion);
      isInteresting = false;
    } else {
      FortifyRulesDefinition.LOG.debug("The rule {} was already added in formatVersion {}, replace it by the one with formatVersion {}.", rule.getRuleID(), previousVersion,
        rule.getFormatVersion());
      isInteresting = true;
    }

    return isInteresting;
  }

  private NewRepository getRepository(Context context, String sqLanguageKey) {
    String repoKey = FortifyConstants.fortifyRepositoryKey(sqLanguageKey);
    if (!this.newRepositories.containsKey(repoKey)) {
      this.newRepositories.put(repoKey, context.createRepository(repoKey, sqLanguageKey).setName("Fortify"));
    }
    return this.newRepositories.get(repoKey);
  }

  private String convertToSQ(String ruleLanguage) {
    if (FortifyRulesDefinition.FORTIFY_TO_SQ.containsKey(ruleLanguage)) {
      return FortifyRulesDefinition.FORTIFY_TO_SQ.get(ruleLanguage);
    }
    return ruleLanguage;
  }

}
