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
package org.sonar.fortify.rule;

import com.google.common.io.Closeables;
import org.junit.Test;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.fortify.base.FortifyParseException;

import java.io.InputStream;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class RulePackParserTest {
  @Test
  public void test() {
    Collection<Rule> rules = parse("rulepack/dummy-rulepack.xml");
    assertThat(rules.size()).isEqualTo(4);
    for (Rule rule : rules) {
      String key = rule.getKey();
      if ("1".equals(key)) {
        assertRule(rule, "Dummy cat: Dummy subcat", RulePriority.MAJOR, "");
      } else if ("3".equals(key)) {
        assertRule(rule, "Dummy cat", RulePriority.CRITICAL,
          "<h2>ABSTRACT</h2><p>Dummy abstract</p><h2>EXPLANATION</h2><p>Dummy explanation</p><h2>REFERENCES</h2><p>[1] Dummy reference 1</p><p>[2] Dummy reference 2 - Dummy author</p>");
      } else if ("4".equals(key)) {
        assertRule(rule, "Dummy cat", RulePriority.BLOCKER, "");
      } else if ("5".equals(key)) {
        assertRule(rule, "Dummy cat", RulePriority.INFO, "");
      } else {
        fail("Rule " + key + " is not expected");
      }
    }
  }

  public void assertRule(Rule rule, String name, RulePriority priority, String description) {
    assertThat(rule.getName()).isEqualTo(name);
    assertThat(rule.getRepositoryKey()).isEqualTo("fortify-java");
    assertThat(rule.getLanguage()).isEqualTo("java");
    assertThat(rule.getSeverity()).isEqualTo(priority);
    assertThat(rule.getDescription()).isEqualTo(description);
  }

  @Test
  public void testLanguage() {
    parse("rulepack/dummy2-rulepack.xml");
    assertThat(parse("rulepack/dummy2-rulepack.xml").size()).isEqualTo(1);
  }

  @Test
  public void testOtherLanguage() {
    assertThat(parse("rulepack/other-rulepack.xml").size()).isEqualTo(0);
  }

  private Collection<Rule> parse(String rulePack) {
    RulePackParser parser = new RulePackParser();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(rulePack);
    try {
      return parser.parse(inputStream, "java");
    } catch (FortifyParseException e) {
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
  }
}
