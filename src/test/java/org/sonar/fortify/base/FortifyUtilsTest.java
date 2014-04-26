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
package org.sonar.fortify.base;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sonar.api.rule.Severity;

import java.util.Arrays;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(Parameterized.class)
public class FortifyUtilsTest {

  private final String fortifySeverity;
  private final String expectedSeverity;

  public FortifyUtilsTest(String fortifySeverity, String expectedSeverity) {
    this.fortifySeverity = fortifySeverity;
    this.expectedSeverity = expectedSeverity;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> severities() {
    return Arrays.asList(new Object[][] {
      {"4.5", Severity.BLOCKER},
      {"3.0", Severity.CRITICAL},
      {"2.0", Severity.MAJOR},
      {"1.0", Severity.MINOR},
      {"0", Severity.INFO}
    });
  }

  @Test
  public void testGetRulePriorityFromFortifySeverity() {
    assertThat(FortifyUtils.getRulePriorityFromFortifySeverity(this.fortifySeverity)).isEqualTo(this.expectedSeverity);
  }
}
