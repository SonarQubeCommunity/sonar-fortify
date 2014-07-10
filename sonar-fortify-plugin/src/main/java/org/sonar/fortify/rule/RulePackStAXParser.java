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

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.fortify.base.FortifyParseException;
import org.sonar.fortify.base.FortifyUtils;
import org.sonar.fortify.rule.element.Description;
import org.sonar.fortify.rule.element.FortifyRule;
import org.sonar.fortify.rule.element.Reference;
import org.sonar.fortify.rule.element.RulePack;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class RulePackStAXParser {
  private static final Logger LOG = LoggerFactory.getLogger(RulePackStAXParser.class);

  private static final Collection<String> INTERNAL_RULE_NAMES = Sets.newHashSet(
    "AliasRule", "AllocationRule", "BufferCopyRule", "CharacterizationRule", "ControlflowActionPrototype",
    "ControlflowTransition", "CustomDescriptionRule", "GlobalClassRule", "MapRule", "NonReturningRule",
    "ResultFilterRule", "ScriptedCallGraphRule", "StringLengthRule", "SuppressionRule", "DataflowCleanseRule",
    "DataflowEntryPointRule", "DataflowPassthroughRule", "DataflowSourceRule", "DeprecationRule", "GlobalFieldRule",
    "StatisticalRule", "InputSetRule");

  private static final Collection<String> REAL_RULE_NAMES = Sets.newHashSet(
    "ConfigurationRule", "ContentRule", "ControlflowRule", "DataflowSinkRule", "SemanticRule", "StructuralRule",
    "InternalRule");

  RulePack parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException, FortifyParseException {

    SMInputFactory inputFactory = FortifyUtils.newStaxParser();
    try {
      SMHierarchicCursor rootC = inputFactory.rootElementCursor(inputStream);
      rootC.advance(); // <RulePack>

      SMInputCursor childCursor = rootC.childCursor();

      RulePack rulePack = new RulePack();

      while (childCursor.getNext() != null) {
        String nodeName = childCursor.getLocalName();

        if ("Name".equals(nodeName)) {
          rulePack.setName(StringUtils.trim(childCursor.collectDescendantText(false)));
        } else if ("Language".equals(nodeName)) {
          rulePack.setLanguage(StringUtils.trim(childCursor.collectDescendantText(false)));
        } else if ("Rules".equals(nodeName)) {
          processRules(childCursor, rulePack);
        }
      }

      LOG.debug(rulePack.name() + " - " + rulePack.language() + " - " + rulePack.getRules().size());
      return rulePack;

    } catch (XMLStreamException e) {
      throw new IllegalStateException("XML is not valid", e);
    }
  }

  private void processRules(SMInputCursor rulesCursor, RulePack rulePack) throws XMLStreamException {
    SMInputCursor childCursor = rulesCursor.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();

      if ("RuleDefinitions".equals(nodeName)) {
        processRuleDefinitions(childCursor, rulePack);
      } else if ("Descriptions".equals(nodeName)) {
        processDescriptions(childCursor, rulePack);
      }
    }
  }

  private void processDescriptions(SMInputCursor descriptionsCursor, RulePack rulePack) throws XMLStreamException {
    SMInputCursor descCursor = descriptionsCursor.childElementCursor("Description");
    while (descCursor.getNext() != null) {
      Description desc = processDescription(descCursor);
      rulePack.addDescription(desc);
    }

  }

  private Description processDescription(SMInputCursor descCursor) throws XMLStreamException {
    Description desc = new Description()
      .setId(descCursor.getAttrValue("id"))
      .setRef(descCursor.getAttrValue("ref"));
    SMInputCursor childCursor = descCursor.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();

      if ("Abstract".equals(nodeName)) {
        desc.setDescriptionAbstract(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("Explanation".equals(nodeName)) {
        desc.setExplanation(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("Recommendations".equals(nodeName)) {
        desc.setRecommendations(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("References".equals(nodeName)) {
        processReference(desc, childCursor);
      }
    }
    return desc;
  }

  private void processReference(Description desc, SMInputCursor childCursor) throws XMLStreamException {
    SMInputCursor refCursor = childCursor.childElementCursor("Reference");
    while (refCursor.getNext() != null) {
      Reference reference = new Reference();
      SMInputCursor refChildCursor = refCursor.childCursor();
      while (refChildCursor.getNext() != null) {
        String refNodeName = refChildCursor.getLocalName();
        if ("Title".equals(refNodeName)) {
          reference.setTitle(StringUtils.trim(refChildCursor.collectDescendantText(false)));
        } else if ("Author".equals(refNodeName)) {
          reference.setAuthor(StringUtils.trim(refChildCursor.collectDescendantText(false)));
        }
      }
      desc.addReference(reference);
    }
  }

  private void processRuleDefinitions(SMInputCursor ruleDefsCursor, RulePack rulePack) throws XMLStreamException {
    SMInputCursor childCursor = ruleDefsCursor.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();
      if (INTERNAL_RULE_NAMES.contains(nodeName)) {
        // Ignore
      } else if (REAL_RULE_NAMES.contains(nodeName)) {
        processRule(childCursor, rulePack);
      }
    }

  }

  private void processRule(SMInputCursor ruleCursor, RulePack rulePack) throws XMLStreamException {
    FortifyRule rule = new FortifyRule();
    rule.setLanguage(ruleCursor.getAttrValue("language"));
    rule.setFormatVersion(ruleCursor.getAttrValue("formatVersion"));

    SMInputCursor childCursor = ruleCursor.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();
      if ("RuleID".equals(nodeName)) {
        rule.setRuleID(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("Notes".equals(nodeName)) {
        rule.setNotes(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("VulnKingdom".equals(nodeName)) {
        rule.setVulnKingdom(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("VulnCategory".equals(nodeName)) {
        rule.setVulnCategory(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("VulnSubcategory".equals(nodeName)) {
        rule.setVulnSubcategory(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("DefaultSeverity".equals(nodeName)) {
        rule.setDefaultSeverity(FortifyUtils.fortifyToSonarQubeSeverity(StringUtils.trim(childCursor.collectDescendantText(false))));
      } else if ("Description".equals(nodeName)) {
        Description desc = processDescription(childCursor);
        rule.setDescription(desc);
      }
    }

    rulePack.addRule(rule);
  }
}
