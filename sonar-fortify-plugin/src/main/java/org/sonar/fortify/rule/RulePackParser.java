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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.rule.RulesDefinition.Context;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.fortify.base.FortifyConstants;
import org.sonar.fortify.base.FortifyParseException;
import org.sonar.fortify.base.FortifyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.sonar.fortify.base.DomUtils.getAtMostOneElementByTagName;
import static org.sonar.fortify.base.DomUtils.getSingleElementByTagName;

/**
 *  Rule packs schema is defined in Fortify documentation: Docs/HP_Fortify_SCA_Custom_Rules_Guide_3.90.zip/rulesXMLschema/index.html
 */
class RulePackParser {
  private static final Logger LOG = LoggerFactory.getLogger(RulePackParser.class);

  private final Context context;
  private final Map<String, NewRepository> newRepositories;

  @Nullable
  private String rulePackName = null;
  @Nullable
  private String rulePackLanguage = null;
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

  RulePackParser(Context context, Map<String, NewRepository> newRepositories) {
    this.context = context;
    this.newRepositories = newRepositories;
  }

  private NewRepository getNewRepository(String language) {
    NewRepository newRepository = this.newRepositories.get(language);
    if (newRepository == null) {
      newRepository = this.context.createRepository(FortifyConstants.fortifyRepositoryKey(language), language);
      newRepository.setName("Fortify");
      this.newRepositories.put(language, newRepository);
    }
    return newRepository;
  }

  private void createRule(String language, String ruleID, String vulnCategory, String vulnSubcategory, String defaultSeverity, String description) {
    String name = vulnCategory;
    if (vulnSubcategory != null) {
      name += ": " + vulnSubcategory;
    }

    NewRepository newRepository = getNewRepository(language);
    newRepository.createRule(ruleID)
      .setName(name)
      .setHtmlDescription(description)
      // .setTags(vulnKingdom)
      .setSeverity(FortifyUtils.fortifyToSonarQubeSeverity(defaultSeverity));
  }

  private void handleRule(Element element)
    throws FortifyParseException {
    String ruleLanguage = element.getAttribute("language");
    String ruleID = getSingleElementByTagName(element, "RuleID").getTextContent();
    String language = null;
    if (StringUtils.isNotBlank(ruleLanguage)) {
      language = ruleLanguage;
    } else if (StringUtils.isNotBlank(this.rulePackLanguage)) {
      language = this.rulePackLanguage;
    } else {
      RulePackParser.LOG.info("Ignore rule {} as language is nor defined in rule definition neither in rulePack.", ruleID);
    }
    if (language != null) {
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
      createRule(language, ruleID, vulnCategory, vulnSubcategory, defaultSeverity, ruleDescription);
    }
  }

  private void handleRuleDefinition(Element element)
    throws FortifyParseException {
    String name = element.getNodeName();

    if (RulePackParser.INTERNAL_RULE_NAMES.contains(name)) {
      // Flow analysis rules: ignore
    } else if (RulePackParser.REAL_RULE_NAMES.contains(name)) {
      handleRule(element);
    } else {
      RulePackParser.LOG.error("Rule of type: {} is unknown!", name);
    }

  }

  private void handleRuleDefinitions(Element element)
    throws FortifyParseException {
    NodeList ruleDefinitions = element.getChildNodes();
    for (int i = 0; i < ruleDefinitions.getLength(); i++) {
      Node node = ruleDefinitions.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        handleRuleDefinition((Element) node);
      }
    }
  }

  private void handleRules(Element element)
    throws FortifyParseException {
    handleRuleDefinitions(getSingleElementByTagName(element, "RuleDefinitions"));
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
    if (languageElement == null) {
      this.rulePackLanguage = null;
    } else {
      this.rulePackLanguage = languageElement.getTextContent();
    }
  }

  void parse(InputStream inputStream) throws FortifyParseException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      Document document = documentBuilder.parse(inputStream);

      handleRulePack(getSingleElementByTagName(document, "RulePack"));
      Element descriptionsElement = getAtMostOneElementByTagName(document, "Descriptions");
      if (descriptionsElement != null) {
        handleDescriptions(descriptionsElement);
      }
      handleRules(getSingleElementByTagName(document, "Rules"));
    } catch (ParserConfigurationException e) {
      throw new FortifyParseException(e);
    } catch (SAXException e) {
      throw new FortifyParseException(e);
    } catch (IOException e) {
      throw new FortifyParseException(e);
    }
  }
}
