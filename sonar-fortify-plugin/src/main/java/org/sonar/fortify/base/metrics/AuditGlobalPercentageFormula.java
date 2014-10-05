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

public class AuditGlobalPercentageFormula implements Formula {

  @Override
  public List<Metric> dependsUponMetrics() {
    return Arrays.asList(FortifyMetrics.AUDIT_BAD_PRACTICE, FortifyMetrics.AUDIT_EXPLOITABLE, FortifyMetrics.AUDIT_NOT_AN_ISSUE,
      FortifyMetrics.AUDIT_NOT_AUDITED, FortifyMetrics.AUDIT_RELIABILITY_ISSUE, FortifyMetrics.AUDIT_SUSPICIOUS);
  }

  @Override
  public Measure calculate(FormulaData data, FormulaContext context) {
    Double badPractice = MeasureUtils.getValue(data.getMeasure(FortifyMetrics.AUDIT_BAD_PRACTICE), null);
    Double exploitable = MeasureUtils.getValue(data.getMeasure(FortifyMetrics.AUDIT_EXPLOITABLE), null);
    Double notAnIssue = MeasureUtils.getValue(data.getMeasure(FortifyMetrics.AUDIT_NOT_AN_ISSUE), null);
    Double notAudited = MeasureUtils.getValue(data.getMeasure(FortifyMetrics.AUDIT_NOT_AUDITED), null);
    Double reliability = MeasureUtils.getValue(data.getMeasure(FortifyMetrics.AUDIT_RELIABILITY_ISSUE), null);
    Double suspicious = MeasureUtils.getValue(data.getMeasure(FortifyMetrics.AUDIT_SUSPICIOUS), null);

    if (areNonNull(badPractice, exploitable, notAnIssue, notAudited, reliability, suspicious)) {
      Double total = badPractice + exploitable + notAnIssue + notAudited + reliability + suspicious;
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

  private boolean areNonNull(Double... values) {
    for (Double d : values) {
      if (d == null) {
        return false;
      }
    }
    return true;
  }
}
