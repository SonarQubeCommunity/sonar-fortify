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

import com.google.common.io.Closeables;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.fest.assertions.Assertions.assertThat;

public class DescriptionFormatterTest {
  @Test
  public void formatTest() throws IOException {
    DescriptionFormatter desc = new DescriptionFormatter();
    InputStream in = null;
    try {
      in = this.getClass().getClassLoader().getResourceAsStream("description.txt");
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));

      String formattedDescription = desc.format(reader);
      assertThat(formattedDescription)
        .isEqualTo(
          "<p>Some text that will be included</p><p></p><p>Some text that will be included. </p><p>Some text that will be included</p><p></p><p><b>Example 1:</b> Included text</p><p><pre>\n  formated\n    text\n</pre>\n</p><p></p><p></p>");
    } finally {
      Closeables.closeQuietly(in);
    }
  }
}
