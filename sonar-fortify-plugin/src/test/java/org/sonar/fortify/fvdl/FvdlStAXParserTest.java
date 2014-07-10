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
import org.sonar.fortify.fvdl.element.Fvdl;
import org.sonar.fortify.fvdl.element.Vulnerability;

import java.io.InputStream;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class FvdlStAXParserTest {
  @Test
  public void parse_dummy_report() throws Exception {
    FvdlStAXParser parser = new FvdlStAXParser();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("report/dummy-report.xml");
    Fvdl fvdl = parser.parse(inputStream);
    assertThat(fvdl.getBuild().getSourceBasePath()).isEqualTo("/a/dummy/path");
    Collection<Vulnerability> vulnerabilities = fvdl.getVulnerabilities();
    assertThat(vulnerabilities.size()).isEqualTo(3);
    for (Vulnerability vulnerability : vulnerabilities) {
      if ("1".equals(vulnerability.getClassID())) {
        assertThat(vulnerability.getPath()).isEqualTo("dummy.file");
        assertThat(vulnerability.getInstanceID()).isEqualTo("1");
        assertThat(vulnerability.getLine()).isEqualTo(1);
        assertThat(vulnerability.getReplacementDefinitions().size()).isEqualTo(1);
      } else if ("2".equals(vulnerability.getClassID())) {
        assertThat(vulnerability.getPath()).isNull();
        assertThat(vulnerability.getInstanceID()).isEqualTo("2");
        assertThat(vulnerability.getLine()).isNull();
        assertThat(vulnerability.getReplacementDefinitions().size()).isEqualTo(0);
      } else if ("3".equals(vulnerability.getClassID())) {
        assertThat(vulnerability.getPath()).isNull();
        assertThat(vulnerability.getInstanceID()).isEqualTo("3");
        assertThat(vulnerability.getLine()).isNull();
        assertThat(vulnerability.getReplacementDefinitions().size()).isEqualTo(0);
      } else {
        fail("Vulnerability with classID=" + vulnerability.getClassID() + " is not expected!");
      }
    }
  }

  @Test
  public void parse_full_report() throws Exception {
    FvdlStAXParser parser = new FvdlStAXParser();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("report/audit.fvdl");
    Collection<Vulnerability> vulnerabilities = parser.parse(inputStream).getVulnerabilities();
    assertThat(vulnerabilities.size()).isEqualTo(1048);
    for (Vulnerability vulnerability : vulnerabilities) {
      assertThat(vulnerability.getClassID()).isNotNull();
      assertThat(vulnerability.getPath()).isNotNull();
      assertThat(vulnerability.getInstanceID()).isNotNull();
      assertThat(vulnerability.getLine()).isNotNull();
      assertThat(vulnerability.getReplacementDefinitions()).isNotNull();
    }
  }

}
