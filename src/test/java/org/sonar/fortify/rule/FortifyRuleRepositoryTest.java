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

import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.rules.Rule;
import org.sonar.fortify.base.FortifyConstants;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class FortifyRuleRepositoryTest {
  @Test
  public void create_rules() {
    Settings settings = new Settings();
    settings.setProperty(FortifyConstants.RULEPACK_LOCATION_PROPERTY, "src/test/resources/rulepack,src/test/resources/rulepack/other-rulepack.xml");
    List<Rule> rules = new FortifyRuleRepository(settings, "java").createRules();
    // TODO replace by isNotEmpty()
    assertThat(rules).isNotNull();
  }

  @Test
  public void test_characteristics() {
    FortifyRuleRepository repository = new FortifyRuleRepository(new Settings(), "java");

    assertThat(repository.getKey()).isEqualTo("fortify-java");
    assertThat(repository.getName()).isEqualTo("Fortify");
    assertThat(repository.getLanguage()).isEqualTo("java");
  }
}
