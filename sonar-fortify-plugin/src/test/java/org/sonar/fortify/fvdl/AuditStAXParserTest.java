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

import org.junit.Test;
import org.sonar.fortify.base.AnalysisState;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

public class AuditStAXParserTest {
  @Test
  public void test() throws ParserConfigurationException, SAXException, IOException {
    AuditStAXParser parser = new AuditStAXParser();

    InputStream in = getClass().getClassLoader().getResourceAsStream("audit/audit.xml");
    Map<String, AnalysisState> result = parser.parse(in);

    int[] results = new int[] {0, 0, 0, 0, 0, 0};
    for (AnalysisState analysisState : result.values()) {
      results[analysisState.ordinal()]++;
    }

    assertThat(result.size()).isEqualTo(10);
    assertThat(results[AnalysisState.BAD_PRACTICE.ordinal()]).isEqualTo(2);
    assertThat(results[AnalysisState.EXPLOITABLE.ordinal()]).isEqualTo(2);
    assertThat(results[AnalysisState.NOT_AN_ISSUE.ordinal()]).isEqualTo(3);
    assertThat(results[AnalysisState.NOT_AUDITED.ordinal()]).isEqualTo(0);
    assertThat(results[AnalysisState.RELIABILITY_ISSUE.ordinal()]).isEqualTo(2);
    assertThat(results[AnalysisState.SUSPICIOUS.ordinal()]).isEqualTo(1);
  }
}
