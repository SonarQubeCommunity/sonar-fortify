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
package org.sonar.fortify.rule.handler;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.fortify.base.FortifyUtils;
import org.sonar.fortify.base.handler.AbstractSetHandler;
import org.sonar.fortify.base.handler.StringHandler;
import org.sonar.fortify.rule.element.Description;
import org.sonar.fortify.rule.element.Rule;
import org.xml.sax.Attributes;

import java.util.Collection;

public class RuleHandler extends AbstractSetHandler<Rule> {
  private static final Logger LOG = LoggerFactory.getLogger(RuleHandler.class);

  private static final Collection<String> INTERNAL_RULE_NAMES = Sets.newHashSet(
    "AliasRule", "AllocationRule", "BufferCopyRule", "CharacterizationRule", "ControlflowActionPrototype",
    "ControlflowTransition", "CustomDescriptionRule", "GlobalClassRule", "MapRule", "NonReturningRule",
    "ResultFilterRule", "ScriptedCallGraphRule", "StringLengthRule", "SuppressionRule", "DataflowCleanseRule",
    "DataflowEntryPointRule", "DataflowPassthroughRule", "DataflowSourceRule", "DeprecationRule", "GlobalFieldRule",
    "StatisticalRule", "InputSetRule");

  private static final Collection<String> REAL_RULE_NAMES = Sets.newHashSet(
    "ConfigurationRule", "ContentRule", "ControlflowRule", "DataflowSinkRule", "SemanticRule", "StructuralRule",
    "InternalRule");

  private final StringHandler ruleIDHandler;
  private final StringHandler vulnCategoryHandler;
  private final StringHandler vulnSubcategoryHandler;
  private final StringHandler defaultSeverityHandler;
  private final DescriptionHandler descriptionHandler;

  private String currentRuleType;
  private Rule rule;

  RuleHandler() {
    super("dummy");
    this.ruleIDHandler = new StringHandler("RuleID");
    this.vulnCategoryHandler = new StringHandler("VulnCategory");
    this.vulnSubcategoryHandler = new StringHandler("VulnSubcategory");
    this.defaultSeverityHandler = new StringHandler("DefaultSeverity");
    this.descriptionHandler = new DescriptionHandler();
    setChildren(this.ruleIDHandler, this.vulnCategoryHandler, this.vulnSubcategoryHandler, this.defaultSeverityHandler, this.descriptionHandler);
  }

  @Override
  protected boolean isStart(String qName) {
    boolean isStart = false;
    if (RuleHandler.INTERNAL_RULE_NAMES.contains(qName)) {
      // Flow analysis rules: ignore
    } else if (RuleHandler.REAL_RULE_NAMES.contains(qName)) {
      isStart = true;
      this.currentRuleType = qName;
    } else {
      RuleHandler.LOG.error("Rule of type: {} is unknown!", qName);
    }
    return isStart;
  }

  @Override
  protected boolean isEnd(String qName) {
    return this.currentRuleType != null && this.currentRuleType.equals(qName);
  }

  @Override
  protected void start(Attributes attributes) {
    if (this.currentRuleType != null) {
      this.descriptionHandler.reset();
      this.rule = new Rule();
      this.rule.setLanguage(attributes.getValue("language"));
      this.rule.setFormatVersion(attributes.getValue("formatVersion"));
    }
  }

  @Override
  protected void end() {
    if (this.currentRuleType != null) {
      this.rule.setRuleID(this.ruleIDHandler.getResult());
      this.rule.setVulnCategory(this.vulnCategoryHandler.getResult());
      this.rule.setVulnSubcategory(this.vulnSubcategoryHandler.getResult());
      String defaultSeverity = this.defaultSeverityHandler.getResult();
      if (defaultSeverity != null) {
        this.rule.setDefaultSeverity(FortifyUtils.fortifyToSonarQubeSeverity(defaultSeverity));
      }
      Collection<Description> descriptions = this.descriptionHandler.getResult();
      if (!descriptions.isEmpty()) {
        this.rule.setDescription(descriptions.iterator().next());
      }
      add(this.rule);
      this.currentRuleType = null;
    }
  }
}
