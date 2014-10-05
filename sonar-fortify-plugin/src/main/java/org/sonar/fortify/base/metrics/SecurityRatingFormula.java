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

public class SecurityRatingFormula implements Formula {

  private static final double BLOCKER_SECURITY_RATING_LEVEL = 1.0;
  private static final double CRITICAL_SECURITY_RATING_LEVEL = 2.0;
  private static final double MAJOR_SECURITY_RATING_LEVEL = 3.0;
  private static final double MINOR_SECURITY_RATING_LEVEL = 4.0;
  private static final double DEFAULT_SECURITY_RATING_LEVEL = 5.0;

  @Override
  public List<Metric> dependsUponMetrics() {
    return Arrays.asList(FortifyMetrics.CFPO, FortifyMetrics.HFPO, FortifyMetrics.MFPO, FortifyMetrics.LFPO);
  }

  @Override
  public Measure calculate(FormulaData data, FormulaContext context) {

    Double criticalCount = MeasureUtils.getValue(data.getMeasure(FortifyMetrics.CFPO), null);
    Double highCount = MeasureUtils.getValue(data.getMeasure(FortifyMetrics.HFPO), null);
    Double mediumCount = MeasureUtils.getValue(data.getMeasure(FortifyMetrics.MFPO), null);
    Double lowCount = MeasureUtils.getValue(data.getMeasure(FortifyMetrics.LFPO), null);

    if (criticalCount != null && highCount != null && mediumCount != null && lowCount != null) {
      Double securityRatingLevel;
      if (criticalCount > 0) {
        securityRatingLevel = SecurityRatingFormula.BLOCKER_SECURITY_RATING_LEVEL;
      } else if (highCount > 0) {
        securityRatingLevel = SecurityRatingFormula.CRITICAL_SECURITY_RATING_LEVEL;
      } else if (mediumCount > 0) {
        securityRatingLevel = SecurityRatingFormula.MAJOR_SECURITY_RATING_LEVEL;
      } else if (lowCount > 0) {
        securityRatingLevel = SecurityRatingFormula.MINOR_SECURITY_RATING_LEVEL;
      } else {
        securityRatingLevel = SecurityRatingFormula.DEFAULT_SECURITY_RATING_LEVEL;
      }

      return new Measure(context.getTargetMetric(), securityRatingLevel);
    }
    return null;
  }

}
