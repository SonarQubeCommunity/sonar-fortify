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
import org.mockito.Mockito;
import org.sonar.api.config.Settings;
import org.sonar.api.server.rule.RulesDefinition.Context;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.fortify.base.FortifyConstants;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FortifyRulesDefinitionTest {
  @Test
  public void test() {
    Settings settings = new Settings();
    settings.setProperty(FortifyConstants.RULEPACK_PATHS_PROPERTY, "src/test/resources/rulepack,src/test/resources/rulepack/other-rulepack.xml");

    FortifyRulesDefinition fortifyRulesDefinition = new FortifyRulesDefinition(settings);

    Context context = mock(Context.class);
    NewRepository newRepository = mock(NewRepository.class, Mockito.RETURNS_MOCKS);
    when(context.createRepository(FortifyConstants.fortifyRepositoryKey("java"), "java")).thenReturn(newRepository);

    fortifyRulesDefinition.define(context);

    verify(newRepository, times(1)).setName("Fortify");
  }

  @Test
  public void testMissingFile() {
    Settings settings = new Settings();
    settings.setProperty(FortifyConstants.RULEPACK_PATHS_PROPERTY, "do/not/exist/file");

    FortifyRulesDefinition fortifyRulesDefinition = new FortifyRulesDefinition(settings);

    Context context = mock(Context.class);
    fortifyRulesDefinition.define(context);

    verify(context, never()).createRepository(anyString(), anyString());
  }
}
