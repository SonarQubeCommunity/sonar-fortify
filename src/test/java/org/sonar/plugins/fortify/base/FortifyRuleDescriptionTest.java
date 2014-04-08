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

import static org.fest.assertions.Assertions.assertThat;

public class FortifyRuleDescriptionTest {
  @Test
  public void testGetId() {
    FortifyRuleDescription description = new FortifyRuleDescription("id", "descriptionAbstract", "explanation", "recommendations");
    assertThat(description.getId()).isEqualTo("id");
  }

  @Test
  public void testToHTML() {
    FortifyRuleDescription description = new FortifyRuleDescription(null, null, null, null);
    assertThat(description.toHTML()).isEqualTo("");

    description = new FortifyRuleDescription("id", "descriptionAbstract", "explanation", "recommendations");
    assertThat(description.toHTML()).isEqualTo("<h2>ABSTRACT</h2><p>descriptionAbstract</p><h2>EXPLANATION</h2><p>explanation</p>");

    description = new FortifyRuleDescription("id", "descriptionAbstract", "explanation", "recommendations");
    description.addReference("refTitle", null);
    assertThat(description.toHTML()).isEqualTo("<h2>ABSTRACT</h2><p>descriptionAbstract</p><h2>EXPLANATION</h2><p>explanation</p><h2>REFERENCES</h2><p>[1] refTitle</p>");

    description = new FortifyRuleDescription("id", "descriptionAbstract", "explanation", "recommendations");
    description.addReference("refTitle", "refAuthor");
    assertThat(description.toHTML()).isEqualTo(
      "<h2>ABSTRACT</h2><p>descriptionAbstract</p><h2>EXPLANATION</h2><p>explanation</p><h2>REFERENCES</h2><p>[1] refTitle - refAuthor</p>");
  }
}
