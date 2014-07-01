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

import static org.sonar.fortify.base.DomUtils.getAtMostOneElementByTagName;
import static org.sonar.fortify.base.DomUtils.getSingleElementByTagName;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.rules.Rule;
import org.sonar.fortify.base.FortifyConstants;
import org.sonar.fortify.base.FortifyParseException;
import org.sonar.fortify.base.FortifyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;

/**
 *  Rule packs schema is defined in Fortify documentation: Docs/HP_Fortify_SCA_Custom_Rules_Guide_3.90.zip/rulesXMLschema/index.html
 */
class RulePackParser {
  private static final Logger LOG = LoggerFactory.getLogger(RulePackParser.class);

  @Nullable
  private String rulePackName = null;
  @Nullable
  private String rulePackLanguage = null;
  private final Map<String, Rule> rules = new HashMap<String, Rule>();
  private final Map<String, String> ruleVersions = new HashMap<String, String>();
  private final Map<String, String> descriptions = new HashMap<String, String>();

  private static final Collection<String> INTERNAL_RULE_NAMES = Sets.newHashSet(
    "AliasRule", "AllocationRule", "BufferCopyRule", "CharacterizationRule", "ControlflowActionPrototype",
    "ControlflowTransition", "CustomDescriptionRule", "GlobalClassRule", "MapRule", "NonReturningRule",
    "ResultFilterRule", "ScriptedCallGraphRule", "StringLengthRule", "SuppressionRule", "DataflowCleanseRule",
    "DataflowEntryPointRule", "DataflowPassthroughRule", "DataflowSourceRule", "DeprecationRule", "GlobalFieldRule",
    "StatisticalRule", "InputSetRule");

  private static final Collection<String> REAL_RULE_NAMES = Sets.newHashSet(
    "ConfigurationRule", "ContentRule", "ControlflowRule", "DataflowSinkRule", "SemanticRule", "StructuralRule",
    "InternalRule");

  private Rule createRule(String language, String ruleID, String vulnCategory, String vulnSubcategory, String defaultSeverity, String description, String formatVersion) {
    String name = vulnCategory;
    if (vulnSubcategory != null) {
      name += ": " + vulnSubcategory;
    }
    Rule rule = Rule.create(FortifyConstants.fortifyRepositoryKey(language), ruleID, name);
    rule.setDescription(description);
    rule.setLanguage(language);
    rule.setSeverity(org.sonar.api.rules.RulePriority.valueOf(FortifyUtils.fortifyToSonarQubeSeverity(defaultSeverity)));

    return rule;
  }

  private void handleRule(String language, Element element)
    throws FortifyParseException {
    String ruleLanguage = element.getAttribute("language");
    String formatVersion = element.getAttribute("formatVersion");
    String ruleID = getSingleElementByTagName(element, "RuleID").getTextContent();
    if (ruleLanguage.length() == 0 || ruleLanguage.equals(language)) {
      String vulnCategory = getSingleElementByTagName(element, "VulnCategory").getTextContent();
      Element vulnSubcategoryElement = getAtMostOneElementByTagName(element, "VulnSubcategory");
      String vulnSubcategory = null;
      if (vulnSubcategoryElement != null) {
        vulnSubcategory = vulnSubcategoryElement.getTextContent();
      }
      String defaultSeverity = getSingleElementByTagName(element, "DefaultSeverity").getTextContent();
      Element description = getSingleElementByTagName(element, "Description");
      String descriptionKey = description.getAttribute("ref");
      String ruleDescription;
      if (descriptionKey.length() == 0) {
        ruleDescription = handleDescription(null, description).toHTML();
      } else {
        ruleDescription = this.descriptions.get(descriptionKey);
      }
      String previousFormatVersion = this.ruleVersions.get(ruleID);
      if (previousFormatVersion == null) {
        this.rules.put(ruleID, createRule(language, ruleID, vulnCategory, vulnSubcategory, defaultSeverity, ruleDescription, formatVersion));
        this.ruleVersions.put(ruleID, formatVersion);
      } else if (compareFormatVersion(previousFormatVersion, formatVersion)) {
        RulePackParser.LOG.debug("The rule {} was already added in formatVersion {}, ignoring one with formatVersion {}.", ruleID, previousFormatVersion, formatVersion);
      } else {
        RulePackParser.LOG.debug("The rule {} was already added in formatVersion {}, replace it by the one with formatVersion {}.", ruleID, previousFormatVersion, formatVersion);
        this.rules.put(ruleID, createRule(language, ruleID, vulnCategory, vulnSubcategory, defaultSeverity, ruleDescription, formatVersion));
        this.ruleVersions.put(ruleID, formatVersion);
      }
    } else {
      RulePackParser.LOG.info("Ignore rule {} as it is for {} language.", ruleID, ruleLanguage);
    }
  }

  private void handleRuleDefinition(String language, Element element)
    throws FortifyParseException {
    String name = element.getNodeName();

    if (RulePackParser.INTERNAL_RULE_NAMES.contains(name)) {
      // Flow analysis rules: ignore
    } else if (RulePackParser.REAL_RULE_NAMES.contains(name)) {
      handleRule(language, element);
    } else {
      RulePackParser.LOG.error("Rule of type: {} is unknown!", name);
    }

  }

  private void handleRuleDefinitions(String language, Element element)
    throws FortifyParseException {
    NodeList ruleDefinitions = element.getChildNodes();
    for (int i = 0; i < ruleDefinitions.getLength(); i++) {
      Node node = ruleDefinitions.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        handleRuleDefinition(language, (Element) node);
      }
    }
  }

  private void handleRules(String language, Element element)
    throws FortifyParseException {
    handleRuleDefinitions(language, getSingleElementByTagName(element, "RuleDefinitions"));
  }

  private void handleDescriptionReferences(FortifyRuleDescription description, Element references)
    throws FortifyParseException {
    NodeList referenceNodes = references.getElementsByTagName("Reference");
    for (int i = 0; i < referenceNodes.getLength(); i++) {
      Node node = referenceNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element reference = (Element) node;
        String title = getSingleElementByTagName(reference, "Title").getTextContent();
        String author = null;
        Element authorElement = getAtMostOneElementByTagName(reference, "Author");
        if (authorElement != null) {
          author = authorElement.getTextContent();
        }
        description.addReference(title, author);
      }
    }
  }

  private FortifyRuleDescription handleDescription(@Nullable String id, Element element)
    throws FortifyParseException {
    Element abstractElement = getAtMostOneElementByTagName(element, "Abstract");
    String descriptionAbstract = null;
    if (abstractElement != null) {
      descriptionAbstract = abstractElement.getTextContent();
    }
    Element explanationElement = getAtMostOneElementByTagName(element, "Explanation");
    String explanation = null;
    if (explanationElement != null) {
      explanation = explanationElement.getTextContent();
    }
    Element recommendationsElement = getAtMostOneElementByTagName(element, "Recommendations");
    String recommendations = null;
    if (recommendationsElement != null) {
      recommendations = recommendationsElement.getTextContent();
    }
    FortifyRuleDescription description = new FortifyRuleDescription(id, descriptionAbstract, explanation, recommendations);
    Element references = getAtMostOneElementByTagName(element, "References");
    if (references != null) {
      handleDescriptionReferences(description, references);
    }
    return description;
  }

  private void handleDescriptionReference(Element element) throws FortifyParseException {
    String id = element.getAttribute("id");
    FortifyRuleDescription description = handleDescription(id, element);
    this.descriptions.put(description.getId(), description.toHTML());
  }

  private void handleDescriptions(Element element) throws FortifyParseException {
    NodeList descriptionNodes = element.getChildNodes();
    for (int i = 0; i < descriptionNodes.getLength(); i++) {
      Node node = descriptionNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        handleDescriptionReference((Element) node);
      }
    }
  }

  private void handleRulePack(Element element) throws FortifyParseException {
    this.rulePackName = getSingleElementByTagName(element, "Name").getTextContent();
    Element languageElement = getAtMostOneElementByTagName(element, "Language");
    if (languageElement != null) {
      this.rulePackLanguage = languageElement.getTextContent();
    }
  }

  Collection<Rule> parse(InputStream inputStream, String language) throws FortifyParseException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      Document document = documentBuilder.parse(inputStream);

      handleRulePack(getSingleElementByTagName(document, "RulePack"));
      if (this.rulePackLanguage == null || this.rulePackLanguage.equals(language)) {
        Element descriptionsElement = getAtMostOneElementByTagName(document, "Descriptions");
        if (descriptionsElement != null) {
          handleDescriptions(descriptionsElement);
        }
        handleRules(language, getSingleElementByTagName(document, "Rules"));
      } else {
        RulePackParser.LOG.info("Ignore rulepack \"{}\" as it is for {} language.", this.rulePackName, this.rulePackLanguage);
      }
    } catch (ParserConfigurationException e) {
      throw new FortifyParseException(e);
    } catch (SAXException e) {
      throw new FortifyParseException(e);
    } catch (IOException e) {
      throw new FortifyParseException(e);
    }
    return this.rules.values();
  }

  /**
   * @return true is formatVersion1 is greater than formatVersion2
   */
  protected static boolean compareFormatVersion(String formatVersion1, String formatVersion2) {
    String[] version1Parts = formatVersion1.split("\\.");
    String[] version2Parts = formatVersion2.split("\\.");
    int length = Math.max(version1Parts.length, version2Parts.length);
    for (int i = 0; i < length; i++) {
      int version1 = i < version1Parts.length ?
        Integer.valueOf(version1Parts[i]) : 0;
      int version2 = i < version2Parts.length ?
        Integer.valueOf(version2Parts[i]) : 0;
      if (version1 < version2) {
        return false;
      }
      if (version1 > version2) {
        return true;
      }
    }
    return false;
  }
}
