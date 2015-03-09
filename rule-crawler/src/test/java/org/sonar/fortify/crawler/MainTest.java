/*
 * Crawler for HP Fortify Web site to extract rule description
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
package org.sonar.fortify.crawler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class MainTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void exportRules() throws Exception {
    File output = temp.newFolder();
    Main main = spy(new Main(output));
    doThrow(new IOException("404")).when(main).download(any(URL.class));
    doReturn(IOUtils.toString(MainTest.class.getResourceAsStream("/all.html"))).when(main).download(new URL("http://www.hpenterprisesecurity.com/vulncat/en/vulncat/all.html"));
    doReturn(IOUtils.toString(MainTest.class.getResourceAsStream("/abap/sql_bad_practices_direct_update.html"))).when(main).download(
      new URL("http://www.hpenterprisesecurity.com/vulncat/en/vulncat/abap/sql_bad_practices_direct_update.html"));
    doReturn(IOUtils.toString(MainTest.class.getResourceAsStream("/abap/system_field_overwrite.html"))).when(main).download(
      new URL("http://www.hpenterprisesecurity.com/vulncat/en/vulncat/abap/system_field_overwrite.html"));
    doReturn(IOUtils.toString(MainTest.class.getResourceAsStream("/abap/obsolete.html"))).when(main).download(
      new URL("http://www.hpenterprisesecurity.com/vulncat/en/vulncat/abap/obsolete.html"));
    doReturn(IOUtils.toString(MainTest.class.getResourceAsStream("/actionscript/dangerous_function_asnative.html"))).when(main).download(
      new URL("http://www.hpenterprisesecurity.com/vulncat/en/vulncat/actionscript/dangerous_function_asnative.html"));
    main.extractRules();

    String abapRules = FileUtils.readFileToString(new File(output, "src/main/resources/rules/rules-abap.xml"));
    assertThat(abapRules).contains("<key>api_abuse_sql_bad_practices_direct_update</key>");
    assertThat(abapRules).contains("<internalKey>API Abuse/SQL Bad Practices/Direct Update</internalKey>");
    assertThat(abapRules).contains("<key>api_abuse_system_field_overwrite</key>");
    assertThat(abapRules).contains("<key>code_quality_obsolete</key>");
    assertThat(abapRules).contains("<internalKey>Code Quality/Obsolete</internalKey>");
    assertThat(abapRules).contains("<tag>api-abuse</tag>");
  }

}
