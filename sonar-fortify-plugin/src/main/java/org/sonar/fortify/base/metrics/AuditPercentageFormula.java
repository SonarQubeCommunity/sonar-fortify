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

import org.sonar.api.measures.Formula;
import org.sonar.api.measures.FormulaContext;
import org.sonar.api.measures.FormulaData;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.MeasureUtils;
import org.sonar.api.measures.Metric;

import java.util.Arrays;
import java.util.List;

public class AuditPercentageFormula implements Formula {

  private final Metric totalCount;
  private final Metric notAuditedCount;

  public AuditPercentageFormula(Metric totalCount, Metric notAuditedCount) {
    this.totalCount = totalCount;
    this.notAuditedCount = notAuditedCount;
  }

  @Override
  public List<Metric> dependsUponMetrics() {
    return Arrays.asList(this.totalCount, this.notAuditedCount);
  }

  @Override
  public Measure calculate(FormulaData data, FormulaContext context) {
    Double total = MeasureUtils.getValue(data.getMeasure(this.totalCount), null);
    Double notAudited = MeasureUtils.getValue(data.getMeasure(this.notAuditedCount), null);

    if (total != null && notAudited != null) {
      Double percentage;
      if (total > 0) {
        percentage = Double.valueOf((total - notAudited) / total * 100);
      } else {
        percentage = Double.valueOf(100.0);
      }
      return new Measure(context.getTargetMetric(), percentage);
    }

    return null;
  }
}
