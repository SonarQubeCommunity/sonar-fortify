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
package org.sonar.fortify.fvdl;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.fortify.base.FortifyUtils;
import org.sonar.fortify.fvdl.element.Build;
import org.sonar.fortify.fvdl.element.Description;
import org.sonar.fortify.fvdl.element.Fvdl;
import org.sonar.fortify.fvdl.element.ReplacementDefinition;
import org.sonar.fortify.fvdl.element.Vulnerability;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class FvdlStAXParser {
  Fvdl parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {

    XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    // just so it won't try to load DTD in if there's DOCTYPE
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    SMInputFactory inputFactory = new SMInputFactory(xmlFactory);
    try {
      SMHierarchicCursor rootC = inputFactory.rootElementCursor(inputStream);
      rootC.advance(); // <FVDL>

      SMInputCursor childCursor = rootC.childCursor();

      Build build = null;
      Collection<Description> descriptions = new ArrayList<Description>();
      Collection<Vulnerability> vulnerabilities = null;

      while (childCursor.getNext() != null) {
        String nodeName = childCursor.getLocalName();

        if ("Build".equals(nodeName)) {
          build = processBuild(childCursor);
        } else if ("Description".equals(nodeName)) {
          descriptions.add(processDescription(childCursor));
        } else if ("Vulnerabilities".equals(nodeName)) {
          vulnerabilities = processVulnerabilities(childCursor);
        }
      }

      return new Fvdl(build, descriptions, vulnerabilities);

    } catch (XMLStreamException e) {
      throw new IllegalStateException("XML is not valid", e);
    }
  }

  private Collection<Vulnerability> processVulnerabilities(SMInputCursor vulnsC) throws XMLStreamException {
    Collection<Vulnerability> vulnerabilities = new ArrayList<Vulnerability>();
    SMInputCursor vulnCursor = vulnsC.childElementCursor("Vulnerability");
    while (vulnCursor.getNext() != null) {
      vulnerabilities.add(processVulnerability(vulnCursor));
    }
    return vulnerabilities;
  }

  private Vulnerability processVulnerability(SMInputCursor vulnCursor) throws XMLStreamException {
    Vulnerability vulnerability = new Vulnerability();
    SMInputCursor childCursor = vulnCursor.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();

      if ("ClassInfo".equals(nodeName)) {
        processClassInfo(childCursor, vulnerability);
      } else if ("InstanceInfo".equals(nodeName)) {
        processInstanceInfo(childCursor, vulnerability);
      } else if ("AnalysisInfo".equals(nodeName)) {
        processAnalysisInfo(childCursor, vulnerability);
      }
    }
    return vulnerability;
  }

  private void processAnalysisInfo(SMInputCursor paCursor, Vulnerability vulnerability) throws XMLStreamException {
    SMInputCursor unifiedCursor = paCursor.childElementCursor("Unified");
    if (unifiedCursor.getNext() != null) {
      SMInputCursor childCursor = unifiedCursor.childCursor();
      while (childCursor.getNext() != null) {
        String nodeName = childCursor.getLocalName();

        if ("Trace".equals(nodeName)) {
          processTrace(childCursor, vulnerability);
        } else if ("ReplacementDefinitions".equals(nodeName)) {
          processReplacementDefinitions(childCursor, vulnerability);
        }
      }
    }
  }

  private void processReplacementDefinitions(SMInputCursor repDefsCursor, Vulnerability vulnerability) throws XMLStreamException {
    SMInputCursor repDefCursor = repDefsCursor.childElementCursor("Def");
    while (repDefCursor.getNext() != null) {
      vulnerability.addReplacementDefinitions(new ReplacementDefinition(repDefCursor.getAttrValue("key"), repDefCursor.getAttrValue("value")));
    }

  }

  private void processTrace(SMInputCursor traceCursor, Vulnerability vulnerability) throws XMLStreamException {
    SMInputCursor primaryCursor = traceCursor.childElementCursor("Primary");
    if (primaryCursor.getNext() != null) {
      SMInputCursor entryCursor = primaryCursor.childElementCursor("Entry");
      while (entryCursor.getNext() != null) {
        SMInputCursor nodeCursor = entryCursor.childElementCursor("Node");
        if (nodeCursor.getNext() != null) {
          if ("true".equals(nodeCursor.getAttrValue("isDefault"))) {
            SMInputCursor sourceLocationCursor = nodeCursor.childElementCursor("SourceLocation");
            if (sourceLocationCursor.getNext() != null) {
              vulnerability.setPath(sourceLocationCursor.getAttrValue("path"));
              vulnerability.setLine(Integer.valueOf(sourceLocationCursor.getAttrValue("line")));
            }
          }
        }
      }
    }
  }

  private void processInstanceInfo(SMInputCursor instanceInfoCursor, Vulnerability vulnerability) throws XMLStreamException {
    SMInputCursor childCursor = instanceInfoCursor.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();

      if ("InstanceID".equals(nodeName)) {
        vulnerability.setInstanceID(StringUtils.trim(childCursor.collectDescendantText(false)));
      } else if ("InstanceSeverity".equals(nodeName)) {
        vulnerability.setInstanceSeverity(FortifyUtils.fortifyToSonarQubeSeverity(StringUtils.trim(childCursor.collectDescendantText(false))));
      }
    }
  }

  private void processClassInfo(SMInputCursor classInfoCursor, Vulnerability vulnerability) throws XMLStreamException {
    SMInputCursor classIDCursor = classInfoCursor.childElementCursor("ClassID");
    if (classIDCursor.getNext() != null) {
      vulnerability.setClassID(StringUtils.trim(classIDCursor.collectDescendantText(false)));
    }
  }

  private Description processDescription(SMInputCursor descC) throws XMLStreamException {
    Description description = new Description();
    description.setClassID(descC.getAttrValue("classID"));
    SMInputCursor abstractCursor = descC.childElementCursor("Abstract");
    if (abstractCursor.getNext() != null) {
      description.setAbstract(StringUtils.trim(abstractCursor.collectDescendantText(false)));
    }
    return description;
  }

  private Build processBuild(SMInputCursor buildC) throws XMLStreamException {
    SMInputCursor childCursor = buildC.childCursor();
    while (childCursor.getNext() != null) {
      String nodeName = childCursor.getLocalName();

      if (StringUtils.equalsIgnoreCase("SourceBasePath", nodeName)) {
        String sourceBasePath = StringUtils.trim(childCursor.collectDescendantText(false));
        return new Build(sourceBasePath);
      }
    }

    return null;
  }
}
