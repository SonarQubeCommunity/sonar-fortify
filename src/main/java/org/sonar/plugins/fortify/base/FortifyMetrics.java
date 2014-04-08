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
package org.sonar.plugins.fortify.base;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import java.util.Arrays;
import java.util.List;

public final class FortifyMetrics implements Metrics {

  public static final String DOMAIN = "Fortify";

  public static final String SECURITY_RATING_KEY = "fortify-security-rating";
  public static final Metric SECURITY_RATING = new Metric.Builder(FortifyMetrics.SECURITY_RATING_KEY, "Fortify Security Rating", Metric.ValueType.FLOAT)
    .setDescription("Fortify Security Rating")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(5.0)
    .create();

  public static final String AUDIT_CONTEXT_KEY = "fortify-audit-context";
  public static final Metric AUDIT_CONTEXT = new Metric.Builder(FortifyMetrics.AUDIT_CONTEXT_KEY, "Fortify Audit Context", Metric.ValueType.DATA)
    .setHidden(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .create();

  /**
   * The following metrics are used for the chart Impact versus Likelihood
   */
  public static final Metric CFPO = new Metric.Builder("fortify-cfpo", "Fortify Critical Priority Issues", Metric.ValueType.INT)
    .setDescription("Fortify Critical Priority Issues")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .create();

  public static final Metric HFPO = new Metric.Builder("fortify-hfpo", "Fortify High Priority Issues", Metric.ValueType.INT)
    .setDescription("Fortify High Priority Issues")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .create();

  public static final Metric MFPO = new Metric.Builder("fortify-mfpo", "Fortify Medium Priority Issues", Metric.ValueType.INT)
    .setDescription("Fortify Medium Priority Issues")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .create();

  public static final Metric LFPO = new Metric.Builder("fortify-lfpo", "Fortify Low Priority Issues", Metric.ValueType.INT)
    .setDescription("Fortify Low Priority Issues")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .create();

  @Override
  public List<Metric> getMetrics() {
    return Arrays.asList(FortifyMetrics.SECURITY_RATING, FortifyMetrics.AUDIT_CONTEXT, FortifyMetrics.CFPO, FortifyMetrics.HFPO, FortifyMetrics.MFPO, FortifyMetrics.LFPO);
  }
}
