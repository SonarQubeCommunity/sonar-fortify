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
package org.sonar.fortify.base.metrics;

import org.junit.Test;
import org.sonar.api.measures.FormulaContext;
import org.sonar.api.measures.FormulaData;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SecurityRatingFormulaTest {
  @Test
  public void returnNull() {
    SecurityRatingFormula formula = new SecurityRatingFormula();

    assertThat(formula.calculate(mock(FormulaData.class), mock(FormulaContext.class))).isNull();
    assertThat(formula.dependsUponMetrics().size()).isEqualTo(4);
  }

  @Test
  public void testCritical() {
    test(2.0, 3.0, 4.0, 5.0, 1.0);
  }

  @Test
  public void testHigh() {
    test(0.0, 3.0, 4.0, 5.0, 2.0);
  }

  @Test
  public void testMedium() {
    test(0.0, 0.0, 4.0, 5.0, 3.0);
  }

  @Test
  public void testLow() {
    test(0.0, 0.0, 0.0, 5.0, 4.0);
  }

  @Test
  public void testNone() {
    test(0.0, 0.0, 0.0, 0.0, 5.0);
  }

  private void test(double critical, double high, double medium, double low, double expected) {
    SecurityRatingFormula formula = new SecurityRatingFormula();

    FormulaData data = mock(FormulaData.class);
    mockMeasure(data, FortifyMetrics.CFPO, critical);
    mockMeasure(data, FortifyMetrics.HFPO, high);
    mockMeasure(data, FortifyMetrics.MFPO, medium);
    mockMeasure(data, FortifyMetrics.LFPO, low);

    FormulaContext context = mock(FormulaContext.class);
    when(context.getTargetMetric()).thenReturn(FortifyMetrics.SECURITY_RATING);

    Measure measure = formula.calculate(data, context);

    assertThat(measure).isNotNull();
    assertThat(measure.getMetric()).isEqualTo(FortifyMetrics.SECURITY_RATING);
    assertThat(measure.getValue()).isEqualTo(expected);
  }

  private void mockMeasure(FormulaData data, Metric metric, Double value) {
    Measure measure = mock(Measure.class);
    when(measure.getValue()).thenReturn(value);
    when(data.getMeasure(metric)).thenReturn(measure);
  }
}
