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
import org.sonar.fortify.rule.element.Description;
import org.sonar.fortify.rule.element.FortifyRule;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class RulePackStAXParserTest {
  @Test
  public void test() {
    Collection<FortifyRule> rules = parse("rulepack/dummy-rulepack.xml");
    assertThat(rules.size()).isEqualTo(6);
    for (FortifyRule rule : rules) {
      String key = rule.getRuleID();
      if ("1".equals(key) && "3.3".equals(rule.getFormatVersion().toString())) {
        assertRule(rule, "Dummy cat: Dummy subcat", "java", "MAJOR", "");
      } else if ("1".equals(key)) {
        assertRule(rule, "Dummy cat: Dummy subcat", "java", "MINOR", "");
      } else if ("2".equals(key)) {
        assertRule(rule, "2", "other", null, "");
      } else if ("3".equals(key)) {
        assertRule(
          rule,
          "Dummy cat",
          "java",
          "CRITICAL",
          "<h2>ABSTRACT</h2><p>Dummy abstract</p><h2>EXPLANATION</h2><p>Dummy explanation</p><h2>RECOMMENDATIONS</h2><p>Dummy recommendations</p><h2>REFERENCES</h2><p>\\[1\\] Dummy reference . - Dummy author .</p><p>\\[2\\] Dummy reference . - Dummy author .</p>");
      } else if ("4".equals(key)) {
        assertRule(rule, "Dummy cat", null, "BLOCKER", "");
      } else if ("5".equals(key)) {
        assertRule(rule, "Dummy cat", null, "INFO", "");
      } else {
        fail("Rule " + key + " is not expected");
      }
    }
  }

  public void assertRule(FortifyRule rule, String name, String language, String priority, String description) {
    assertThat(rule.getName()).isEqualTo(name);
    assertThat(rule.getLanguage()).isEqualTo(language);
    assertThat(rule.getDefaultSeverity()).isEqualTo(priority);
    Description desc = rule.getDescription();
    if (desc != null) {
      assertThat(rule.getDescription().toString()).matches(description);
    }
  }

  @Test
  public void testLanguage() {
    assertThat(parse("rulepack/dummy2-rulepack.xml").size()).isEqualTo(1);
  }

  @Test
  public void testOtherLanguage() {
    assertThat(parse("rulepack/other-rulepack.xml").size()).isEqualTo(0);
  }

  private Collection<FortifyRule> parse(String rulePack) {
    RulePackStAXParser parser = new RulePackStAXParser();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(rulePack);
    try {
      return parser.parse(inputStream).getRules();
    } catch (SAXException e) {
      throw new RuntimeException(e);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
  }
}
