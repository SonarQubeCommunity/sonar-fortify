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

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.fortify.base.AnalysisState;
import org.sonar.fortify.base.FortifyUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AuditStAXParser {
  Map<String, AnalysisState> parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {

    Map<String, AnalysisState> analysisStates = new HashMap<String, AnalysisState>();
    SMInputFactory inputFactory = FortifyUtils.newStaxParser();
    try {
      SMHierarchicCursor rootC = inputFactory.rootElementCursor(inputStream);
      rootC.advance(); // <Audit>

      SMInputCursor issueListCursor = rootC.childElementCursor("ns2:IssueList");
      while (issueListCursor.getNext() != null) {
        SMInputCursor issueCursor = issueListCursor.childElementCursor("ns2:Issue");
        while (issueCursor.getNext() != null) {
          String instanceId = issueCursor.getAttrValue("instanceId");
          SMInputCursor tagCursor = issueCursor.childElementCursor("ns2:Tag");
          while (tagCursor.getNext() != null) {
            if ("87f2364f-dcd4-49e6-861d-f8d3f351686b".equals(tagCursor.getAttrValue("id"))) {
              AnalysisState analysisState = processValue(tagCursor.childElementCursor("ns2:Value"));
              analysisStates.put(instanceId, analysisState);
            }
          }
        }
      }

      return analysisStates;

    } catch (XMLStreamException e) {
      throw new IllegalStateException("XML is not valid", e);
    }
  }

  private AnalysisState processValue(SMInputCursor valueCursor) throws XMLStreamException {
    AnalysisState analysisState = AnalysisState.NOT_AUDITED;

    while (valueCursor.getNext() != null) {
      String value = valueCursor.collectDescendantText(false);
      if ("Not an Issue".equals(value)) {
        analysisState = AnalysisState.NOT_AN_ISSUE;
      } else if ("Reliability Issue".equals(value)) {
        analysisState = AnalysisState.RELIABILITY_ISSUE;
      } else if ("Bad Practice".equals(value)) {
        analysisState = AnalysisState.BAD_PRACTICE;
      } else if ("Suspicious".equals(value)) {
        analysisState = AnalysisState.SUSPICIOUS;
      } else if ("Exploitable".equals(value)) {
        analysisState = AnalysisState.EXPLOITABLE;
      }
    }

    return analysisState;
  }
}
