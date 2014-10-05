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

public class AuditPercentageFormulaTest {
  @Test
  public void testNull() {
    AuditPercentageFormula formula = new AuditPercentageFormula(null, null);

    assertThat(formula.calculate(mock(FormulaData.class), mock(FormulaContext.class))).isNull();
    assertThat(formula.dependsUponMetrics().size()).isEqualTo(2);
  }

  @Test
  public void testZero() {
    test(0.0, 0.0, 100.0);
  }

  @Test
  public void testCount() {
    test(2.0, 1.0, 50.0);
    test(2.0, 2.0, 0.0);
    test(2.0, 0.0, 100.0);
  }

  private void test(double totalValue, double notAuditedValue, double expected) {
    Metric total = mockMetric("total");
    Metric notAudited = mockMetric("notAudited");

    AuditPercentageFormula formula = new AuditPercentageFormula(total, notAudited);

    FormulaData data = mock(FormulaData.class);
    mockMeasure(data, total, totalValue);
    mockMeasure(data, notAudited, notAuditedValue);

    FormulaContext context = mock(FormulaContext.class);
    when(context.getTargetMetric()).thenReturn(FortifyMetrics.AUDIT_PERCENTAGE);

    Measure measure = formula.calculate(data, context);

    assertThat(measure).isNotNull();
    assertThat(measure.getMetric()).isEqualTo(FortifyMetrics.AUDIT_PERCENTAGE);
    assertThat(measure.getValue()).isEqualTo(expected);
  }

  private Metric mockMetric(String key) {
    Metric metric = mock(Metric.class);
    when(metric.getKey()).thenReturn(key);

    return metric;
  }

  private void mockMeasure(FormulaData data, Metric metric, Double value) {
    Measure measure = mock(Measure.class);
    when(measure.getValue()).thenReturn(value);
    when(data.getMeasure(metric)).thenReturn(measure);
  }
}
