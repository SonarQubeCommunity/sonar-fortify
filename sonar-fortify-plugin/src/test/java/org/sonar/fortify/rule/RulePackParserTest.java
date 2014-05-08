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
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.server.rule.RulesDefinition.Context;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.fortify.base.FortifyConstants;
import org.sonar.fortify.base.FortifyParseException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RulePackParserTest {
  @Test
  public void test() {
    Collection<NewRule> newRules = new ArrayList<NewRule>();
    Context context = mock(Context.class);
    NewRepository newRepository = mock(NewRepository.class);
    when(context.createRepository(FortifyConstants.fortifyRepositoryKey("java"), "java")).thenReturn(newRepository);

    newRules.add(mockNewRule(newRepository, "1", "Dummy cat: Dummy subcat", "MINOR", ""));
    newRules.add(mockNewRule(newRepository, "2", "Dummy cat: Dummy subcat", "MAJOR", ""));
    newRules
      .add(mockNewRule(newRepository, "3", "Dummy cat", "CRITICAL",
        "<h2>ABSTRACT</h2><p>Dummy abstract</p><h2>EXPLANATION</h2><p>Dummy explanation</p><h2>REFERENCES</h2><p>[1] Dummy reference 1</p><p>[2] Dummy reference 2 - Dummy author</p>"));
    newRules.add(mockNewRule(newRepository, "4", "Dummy cat", "BLOCKER", ""));
    newRules.add(mockNewRule(newRepository, "5", "Dummy cat", "INFO", ""));

    parse(context, "rulepack/dummy-rulepack.xml");

    for (NewRule newRule : newRules) {
      verify(newRule, times(1)).setName(anyString());
    }
  }

  private NewRule mockNewRule(NewRepository newRepository, final String key, String name, String severity, String description) {
    NewRule newRule = mock(NewRule.class, new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        String method = invocation.getMethod().getName() + "(";
        for (Object object : invocation.getArguments()) {
          method += object + ", ";
        }
        method += ")";
        fail("Invocation of method " + method + " is not expected (rule=" + key + ")");
        return null;
      }

    });

    when(newRepository.createRule(key)).thenReturn(newRule);
    doReturn(newRule).when(newRule).setName(name);
    doReturn(newRule).when(newRule).setHtmlDescription(description);
    doReturn(newRule).when(newRule).setSeverity(severity);

    return newRule;
  }

  @Test
  public void testLanguage() {
    Context context = mock(Context.class);
    NewRepository newRepository = mock(NewRepository.class);
    NewRule newRule = mock(NewRule.class);
    when(context.createRepository(anyString(), anyString())).thenReturn(newRepository);
    when(newRepository.createRule(anyString())).thenReturn(newRule);
    when(newRule.setName(anyString())).thenReturn(newRule);
    when(newRule.setHtmlDescription(anyString())).thenReturn(newRule);
    when(newRule.setSeverity(anyString())).thenReturn(newRule);

    parse(context, "rulepack/dummy2-rulepack.xml");
    verify(context, times(1)).createRepository(FortifyConstants.fortifyRepositoryKey("java"), "java");
  }

  @Test
  public void testNoLanguage() {
    Context context = mock(Context.class);
    NewRepository newRepository = mock(NewRepository.class, Mockito.RETURNS_MOCKS);
    when(context.createRepository(anyString(), anyString())).thenReturn(newRepository);

    parse(context, "rulepack/dummy3-rulepack.xml");
    verify(context, never()).createRepository(anyString(), anyString());
  }

  private void parse(Context context, String rulePack) {
    Map<String, NewRepository> newRepositories = new HashMap<String, NewRepository>();
    RulePackParser parser = new RulePackParser(context, newRepositories);
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(rulePack);
    try {
      parser.parse(inputStream);
    } catch (FortifyParseException e) {
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
  }
}
