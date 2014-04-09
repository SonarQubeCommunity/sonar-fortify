/*
 * SonarQube Fortify Plugin
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
package org.sonar.plugins.fortify.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.CheckForNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static org.sonar.plugins.fortify.base.FortifyParserUtils.getAtLeastOneElementByTagName;
import static org.sonar.plugins.fortify.base.FortifyParserUtils.getAtMostOneElementByTagName;
import static org.sonar.plugins.fortify.base.FortifyParserUtils.getSingleElementByTagName;

public class FortifyFprParser {
  private static final Logger LOG = LoggerFactory.getLogger(FortifyFprParser.class);

  @CheckForNull
  private String sourceBasePath = null;
  private final List<FortifyVulnerability> vulnerabilities = new ArrayList<FortifyVulnerability>();
  private final Map<String, String> descriptions = new HashMap<String, String>();

  private void handleDescription(Element description) throws FortifyParseException {
    try {
      String classID = description.getAttribute("classID");
      Element abstractElement = getSingleElementByTagName(description, "Abstract");
      String htmlDescription = abstractElement.getTextContent();
      this.descriptions.put(classID, htmlDescription);
    } catch (FortifyParseException e) {
      FortifyFprParser.LOG.error("Cannot parse description! ({}), description={}",
        e.getMessage(), description);
    }
  }

  private void handleDescriptions(NodeList descriptions) throws FortifyParseException {
    FortifyFprParser.LOG.info("Got {} descriptions", descriptions.getLength());
    for (int i = 0; i < descriptions.getLength(); i++) {
      handleDescription((Element) descriptions.item(i));
    }
  }

  private Collection<ReplacementDefinition> handleVulnerabilityReplacementDefinitions(
    @CheckForNull Element replacementDefinitionElements)
    throws FortifyParseException {
    Collection<ReplacementDefinition> replacementDefinitions = new ArrayList<ReplacementDefinition>();
    if (replacementDefinitionElements != null) {
      NodeList defs = replacementDefinitionElements.getElementsByTagName("Def");
      for (int i = 0; i < defs.getLength(); i++) {
        Node defNode = defs.item(i);
        if (defNode.getNodeType() != Node.ELEMENT_NODE) {
          throw new FortifyParseException("Unexpected type " + defNode.getNodeType() + " for node 'Def'.");
        }
        Element def = (Element) defNode;
        replacementDefinitions.add(new ReplacementDefinition(def.getAttribute("key"), def.getAttribute("value")));
      }
    }
    return replacementDefinitions;
  }

  private Location handleVulnerabilityEntries(NodeList entries) throws FortifyParseException {
    Location location = null;
    for (int i = 0; i < entries.getLength(); i++) {
      Node entryNode = entries.item(i);
      if (entryNode.getNodeType() != Node.ELEMENT_NODE) {
        throw new FortifyParseException("Unexpected type " + entryNode.getNodeType() + " for node 'Entry'.");
      }
      Element entry = (Element) entryNode;
      Element node = getAtMostOneElementByTagName(entry, "Node");
      if (node != null) {
        String isDefault = node.getAttribute("isDefault");
        if (Boolean.valueOf(isDefault)) {
          Element sourceLocation = getSingleElementByTagName(node, "SourceLocation");
          String file = this.sourceBasePath + "/" + sourceLocation.getAttribute("path");
          Integer line = Integer.valueOf(sourceLocation.getAttribute("line"));
          location = new Location(file, line);
        }
      }
    }
    if (location == null) {
      throw new FortifyParseException("Cannot parse file location");
    }
    return location;
  }

  private void handleVulnerability(Element vulnerabilityElement) {
    try {
      String classID;
      String instanceInfo;

      Element classInfoElement = getSingleElementByTagName(vulnerabilityElement, "ClassInfo");
      classID = getSingleElementByTagName(classInfoElement, "ClassID").getTextContent();

      Element instanceInfoElement = getSingleElementByTagName(vulnerabilityElement, "InstanceInfo");
      instanceInfo = getSingleElementByTagName(instanceInfoElement, "InstanceID").getTextContent();

      Element analysisInfo = getSingleElementByTagName(vulnerabilityElement, "AnalysisInfo");
      Element unified = getSingleElementByTagName(analysisInfo, "Unified");

      Collection<ReplacementDefinition> replacementDefinitions =
        handleVulnerabilityReplacementDefinitions(getAtMostOneElementByTagName(unified, "ReplacementDefinitions"));
      String description = this.descriptions.get(classID);
      String message = "No message found";
      if (description == null) {
        FortifyFprParser.LOG.warn("Message not found for classID={}", classID);
      } else {
        message = description;
        for (ReplacementDefinition replacementDefinition : replacementDefinitions) {
          String key = replacementDefinition.getKey();
          String value = replacementDefinition.getValue();
          String regex = "<Replace key=\"" + Matcher.quoteReplacement(key) + "\"/>";
          value = Matcher.quoteReplacement(value);
          message = message.replaceAll(regex, value);
        }
        message = message.replaceAll("\\<[^>]*>", "");
      }

      Element trace = getAtLeastOneElementByTagName(unified, "Trace");
      Element primary = getSingleElementByTagName(trace, "Primary");
      Location location = handleVulnerabilityEntries(primary.getElementsByTagName("Entry"));
      FortifyVulnerability vulnerability = new FortifyVulnerability(location.getFile(), location.getLine(), classID, instanceInfo, message);

      this.vulnerabilities.add(vulnerability);
    } catch (FortifyParseException e) {
      FortifyFprParser.LOG.error("Cannot parse vulnerability! ({})", e.getMessage());
    }
  }

  private void handleVulnerabilities(Element element) {
    NodeList vulnerabilitieNodes = element.getElementsByTagName("Vulnerability");
    FortifyFprParser.LOG.info("Got {} vulnerabilities.", vulnerabilitieNodes.getLength());
    for (int i = 0; i < vulnerabilitieNodes.getLength(); i++) {
      handleVulnerability((Element) vulnerabilitieNodes.item(i));
    }
  }

  private void handleBuild(Element element)
    throws FortifyParseException {
    this.sourceBasePath = getSingleElementByTagName(element, "SourceBasePath").getTextContent();
  }

  public Collection<FortifyVulnerability> parse(InputStream inputStream) throws FortifyParseException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(inputStream);

      handleBuild(getSingleElementByTagName(document, "Build"));
      handleDescriptions(document.getElementsByTagName("Description"));
      handleVulnerabilities(getSingleElementByTagName(document, "Vulnerabilities"));
    } catch (ParserConfigurationException e) {
      throw new FortifyParseException(e);
    } catch (SAXException e) {
      throw new FortifyParseException(e);
    } catch (IOException e) {
      throw new FortifyParseException(e);
    }

    return this.vulnerabilities;
  }

  private static class ReplacementDefinition {
    private final String key;
    private final String value;

    public ReplacementDefinition(String key, String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return this.key;
    }

    public String getValue() {
      return this.value;
    }
  }

  private static class Location {
    private final String file;
    private final Integer line;

    public Location(String file, Integer line) {
      this.file = file;
      this.line = line;
    }

    public String getFile() {
      return this.file;
    }

    public Integer getLine() {
      return this.line;
    }
  }
}
