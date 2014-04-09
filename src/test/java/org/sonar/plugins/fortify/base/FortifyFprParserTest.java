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

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;

public class FortifyFprParserTest {
  @Test
  public void parseTest() throws IOException, ParserConfigurationException, SAXException, FortifyParseException {
    FortifyFprParser parser = new FortifyFprParser();
    InputStream inputStream = getClass().getClassLoader()
      .getResourceAsStream("report/dummy-report.xml");
    Collection<FortifyVulnerability> vulnerabilities = parser.parse(inputStream);
    assertThat(vulnerabilities.size()).isEqualTo(1);
    FortifyVulnerability vulnerability = vulnerabilities.iterator().next();
    assertThat(vulnerability.getClassID()).isEqualTo("1");
    assertThat(vulnerability.getFile()).isEqualTo("/a/dummy/path/dummy.file");
    assertThat(vulnerability.getInstanceID()).isEqualTo("1");
    assertThat(vulnerability.getLine()).isEqualTo(1);
    assertThat(vulnerability.getMessage()).isEqualTo("Dummy");

  }

}
