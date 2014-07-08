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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02
 */
package org.sonar.fortify.rule;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.resources.Languages;
import org.sonar.fortify.rule.element.Description;
import org.sonar.fortify.rule.element.Rule;
import org.sonar.fortify.rule.element.RulePack;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FortifyRuleRepositoriesTest {

  private RulePackParser parser;

  @Before
  public void prepare() {
    parser = mock(RulePackParser.class);
  }

  @Test
  public void provide_rule_repositories_for_supported_fortify_languages() {
    Languages languages = mock(Languages.class);
    when(languages.get(anyString())).thenReturn(new AbstractLanguage("foo") {
      @Override
      public String[] getFileSuffixes() {
        return null;
      }
    });

    when(parser.parse()).thenReturn(Arrays.asList(new RulePack().setLanguage("java")
      .addRules(Arrays.asList(new Rule()
        .setDescription(new Description())
        .setDefaultSeverity("MAJOR")))));

    FortifyRuleRepositories provider = new FortifyRuleRepositories(parser, languages);

    List<FortifyRuleRepository> repositories = provider.provide();
    assertThat(repositories).isNotEmpty();
    assertThat(repositories).onProperty("language").containsExactly("java");
  }

  @Test
  public void provide_rule_repositories_only_for_installed_languages() {
    when(parser.parse()).thenReturn(Arrays.asList(new RulePack().setLanguage("java")
      .addRules(Arrays.asList(new Rule()
        .setDescription(new Description())
        .setDefaultSeverity("MAJOR"),
        new Rule()
          .setLanguage("cpp")
          .setDescription(new Description())
          .setDefaultSeverity("MAJOR"),
        new Rule()
          .setLanguage("php")
          .setDescription(new Description())
          .setDefaultSeverity("MAJOR")))));

    Languages languages = mock(Languages.class);
    when(languages.get("cpp")).thenReturn(new AbstractLanguage("cpp") {
      @Override
      public String[] getFileSuffixes() {
        return null;
      }
    });
    when(languages.get("java")).thenReturn(new AbstractLanguage("java") {
      @Override
      public String[] getFileSuffixes() {
        return null;
      }
    });

    FortifyRuleRepositories provider = new FortifyRuleRepositories(parser, languages);

    List<FortifyRuleRepository> repositories = provider.provide();
    assertThat(repositories).isNotEmpty();
    assertThat(repositories).onProperty("language").containsExactly("cpp", "java");
  }

  @Test
  public void provide_rule_repositories_only_for_non_empty_fortify_repo() {
    when(parser.parse()).thenReturn(Arrays.asList(new RulePack().setLanguage("java")
      .addRules(Arrays.asList(new Rule()
        .setDescription(new Description())
        .setDefaultSeverity("MAJOR")))));

    Languages languages = mock(Languages.class);
    when(languages.get("cpp")).thenReturn(new AbstractLanguage("cpp") {
      @Override
      public String[] getFileSuffixes() {
        return null;
      }
    });
    when(languages.get("java")).thenReturn(new AbstractLanguage("java") {
      @Override
      public String[] getFileSuffixes() {
        return null;
      }
    });

    FortifyRuleRepositories provider = new FortifyRuleRepositories(parser, languages);

    List<FortifyRuleRepository> repositories = provider.provide();
    assertThat(repositories).isNotEmpty();
    assertThat(repositories).onProperty("language").containsExactly("java");
  }

}
