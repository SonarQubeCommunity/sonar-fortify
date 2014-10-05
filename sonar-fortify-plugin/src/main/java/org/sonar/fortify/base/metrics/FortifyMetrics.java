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

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;
import org.sonar.api.measures.SumChildValuesFormula;

import java.util.Arrays;
import java.util.List;

public final class FortifyMetrics implements Metrics {

  public static final String DOMAIN = "Fortify";

  public static final String SECURITY_RATING_KEY = "fortify-security-rating";
  public static final Metric SECURITY_RATING = new Metric.Builder(FortifyMetrics.SECURITY_RATING_KEY, "Fortify Security Rating", Metric.ValueType.INT)
    .setDescription("Fortify Security Rating")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(5.0)
    .setFormula(new SecurityRatingFormula())
    .create();

  /**
   * The following metrics are used for the chart Impact versus Likelihood
   */
  public static final Metric CFPO = new Metric.Builder("fortify-cfpo", "Fortify Critical Severity Issues", Metric.ValueType.INT)
    .setDescription("Fortify Critical Priority Issues")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .setHidden(true)
    .setFormula(new SumChildValuesFormula(false))
    .create();

  public static final Metric HFPO = new Metric.Builder("fortify-hfpo", "Fortify High Severity Issues", Metric.ValueType.INT)
    .setDescription("Fortify High Priority Issues")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .setHidden(true)
    .setFormula(new SumChildValuesFormula(false))
    .create();

  public static final Metric MFPO = new Metric.Builder("fortify-mfpo", "Fortify Medium Severity Issues", Metric.ValueType.INT)
    .setDescription("Fortify Medium Priority Issues")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .setHidden(true)
    .setFormula(new SumChildValuesFormula(false))
    .create();

  public static final Metric LFPO = new Metric.Builder("fortify-lfpo", "Fortify Low Severity Issues", Metric.ValueType.INT)
    .setDescription("Fortify Low Priority Issues")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .setHidden(true)
    .setFormula(new SumChildValuesFormula(false))
    .create();

  /**
   * The following metrics are the current state of the audit.
   */
  public static final Metric AUDIT_PERCENTAGE = new Metric.Builder("fortify-audit-percentage", "Fortify Audit Percentage", Metric.ValueType.PERCENT)
    .setDescription("Fortify Audit Percentage")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(100.0)
    .setFormula(new AuditGlobalPercentageFormula())
    .create();

  public static final Metric CRITICAL_NOT_AUDITED_ISSUES = new Metric.Builder("fortify-critical-not-audited-issues", "Fortify Critical Not Audited Issues", Metric.ValueType.INT)
    .setDescription("Fortify Critical Not Audited Issue")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .setFormula(new SumChildValuesFormula(false))
    .setHidden(true)
    .create();

  public static final Metric AUDIT_CRITICAL_PERCENTAGE = new Metric.Builder("fortify-audit-critical-percentage", "Fortify Audit Critical Percentage", Metric.ValueType.PERCENT)
    .setDescription("Fortify Audit Critical Percentage")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(100.0)
    .setFormula(new AuditPercentageFormula(FortifyMetrics.CFPO, FortifyMetrics.CRITICAL_NOT_AUDITED_ISSUES))
    .create();

  public static final Metric HIGH_NOT_AUDITED_ISSUES = new Metric.Builder("fortify-high-not-audited-issues", "Fortify High Not Audited Issues", Metric.ValueType.INT)
    .setDescription("Fortify High Not Audited Issue")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .setFormula(new SumChildValuesFormula(false))
    .setHidden(true)
    .create();

  public static final Metric AUDIT_HIGH_PERCENTAGE = new Metric.Builder("fortify-audit-high-percentage", "Fortify Audit High Percentage", Metric.ValueType.PERCENT)
    .setDescription("Fortify Audit High Percentage")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(100.0)
    .setFormula(new AuditPercentageFormula(FortifyMetrics.HFPO, FortifyMetrics.HIGH_NOT_AUDITED_ISSUES))
    .create();

  public static final Metric MEDIUM_NOT_AUDITED_ISSUES = new Metric.Builder("fortify-medium-not-audited-issues", "Fortify Medium Not Audited Issues", Metric.ValueType.INT)
    .setDescription("Fortify Medium Not Audited Issue")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .setFormula(new SumChildValuesFormula(false))
    .setHidden(true)
    .create();

  public static final Metric AUDIT_MEDIUM_PERCENTAGE = new Metric.Builder("fortify-audit-medium-percentage", "Fortify Audit Medium Percentage", Metric.ValueType.PERCENT)
    .setDescription("Fortify Audit Medium Percentage")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(100.0)
    .setFormula(new AuditPercentageFormula(FortifyMetrics.MFPO, FortifyMetrics.MEDIUM_NOT_AUDITED_ISSUES))
    .create();

  public static final Metric LOW_NOT_AUDITED_ISSUES = new Metric.Builder("fortify-low-not-audited-issues", "Fortify Low Not Audited Issues", Metric.ValueType.INT)
    .setDescription("Fortify Low Not Audited Issue")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .setFormula(new SumChildValuesFormula(false))
    .setHidden(true)
    .create();

  public static final Metric AUDIT_LOW_PERCENTAGE = new Metric.Builder("fortify-audit-low-percentage", "Fortify Audit Low Percentage", Metric.ValueType.PERCENT)
    .setDescription("Fortify Audit Low Percentage")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(100.0)
    .setFormula(new AuditPercentageFormula(FortifyMetrics.LFPO, FortifyMetrics.LOW_NOT_AUDITED_ISSUES))
    .create();

  public static final Metric AUDIT_NOT_AN_ISSUE = new Metric.Builder("fortify-audit-not-an-issue-issues", "Fortify Not An Issue Issues", Metric.ValueType.INT)
    .setDescription("Fortify Not An Issue Issues")
    .setDirection(Metric.DIRECTION_NONE)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setFormula(new SumChildValuesFormula(false))
    .create();

  public static final Metric AUDIT_RELIABILITY_ISSUE = new Metric.Builder("fortify-audit-reliability-issues", "Fortify Reliability Issues", Metric.ValueType.INT)
    .setDescription("Fortify Reliability Issues")
    .setDirection(Metric.DIRECTION_NONE)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setFormula(new SumChildValuesFormula(false))
    .create();

  public static final Metric AUDIT_BAD_PRACTICE = new Metric.Builder("fortify-audit-bad-practice-issues", "Fortify Bad Practice Issues", Metric.ValueType.INT)
    .setDescription("Fortify Bad Practice Issues")
    .setDirection(Metric.DIRECTION_NONE)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setFormula(new SumChildValuesFormula(false))
    .create();

  public static final Metric AUDIT_SUSPICIOUS = new Metric.Builder("fortify-audit-suspicious-issues", "Fortify Suspicious Issues", Metric.ValueType.INT)
    .setDescription("Fortify Suspicious Issues")
    .setDirection(Metric.DIRECTION_NONE)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setFormula(new SumChildValuesFormula(false))
    .create();

  public static final Metric AUDIT_EXPLOITABLE = new Metric.Builder("fortify-audit-exploitable-issues", "Fortify Exploitable Issues", Metric.ValueType.INT)
    .setDescription("Fortify Exploitable Issues")
    .setDirection(Metric.DIRECTION_NONE)
    .setQualitative(false)
    .setDomain(FortifyMetrics.DOMAIN)
    .setFormula(new SumChildValuesFormula(false))
    .create();

  public static final Metric AUDIT_NOT_AUDITED = new Metric.Builder("fortify-audit-not-audited-issues", "Fortify Not Audited Issues", Metric.ValueType.INT)
    .setDescription("Fortify Not Audited Issues")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(true)
    .setDomain(FortifyMetrics.DOMAIN)
    .setBestValue(0.0)
    .setFormula(new SumChildValuesFormula(false))
    .create();

  @Override
  public List<Metric> getMetrics() {
    return Arrays.asList(FortifyMetrics.SECURITY_RATING, FortifyMetrics.CFPO, FortifyMetrics.HFPO, FortifyMetrics.MFPO,
      FortifyMetrics.LFPO, FortifyMetrics.AUDIT_BAD_PRACTICE, FortifyMetrics.AUDIT_EXPLOITABLE, FortifyMetrics.AUDIT_NOT_AN_ISSUE, FortifyMetrics.AUDIT_NOT_AUDITED,
      FortifyMetrics.AUDIT_PERCENTAGE, FortifyMetrics.AUDIT_RELIABILITY_ISSUE, FortifyMetrics.AUDIT_SUSPICIOUS, FortifyMetrics.AUDIT_HIGH_PERCENTAGE,
      FortifyMetrics.AUDIT_LOW_PERCENTAGE, FortifyMetrics.AUDIT_MEDIUM_PERCENTAGE, FortifyMetrics.AUDIT_CRITICAL_PERCENTAGE, FortifyMetrics.CRITICAL_NOT_AUDITED_ISSUES,
      FortifyMetrics.HIGH_NOT_AUDITED_ISSUES, FortifyMetrics.MEDIUM_NOT_AUDITED_ISSUES, FortifyMetrics.LOW_NOT_AUDITED_ISSUES);
  }
}
