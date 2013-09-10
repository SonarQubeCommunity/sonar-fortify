/*
 * SonarQube Fortify Plugin
 * Copyright (C) 2012 SonarSource
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
package org.sonar.plugins.fortify.batch;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.plugins.fortify.base.FortifyMetrics;
import org.sonar.plugins.fortify.client.FortifyClient;
import xmlns.www_fortifysoftware_com.schema.wstypes.MeasurementHistory;
import xmlns.www_fortifysoftware_com.schema.wstypes.VariableHistory;

import java.util.List;
import java.util.Map;

public class PerformanceIndicatorSensor implements Sensor {

  private final FortifyClient client;
  private final FortifyProject fortifyProject;

  public PerformanceIndicatorSensor(FortifyClient client, FortifyProject fortifyProject) {
    this.client = client;
    this.fortifyProject = fortifyProject;
  }

  public boolean shouldExecuteOnProject(Project project) {
    return ResourceUtils.isRootProject(project) && fortifyProject.exists();
  }

  public void analyse(Project project, SensorContext sensorContext) {
    importPerformanceIndicators(sensorContext);
    importVariables(sensorContext);
  }

  private void importPerformanceIndicators(SensorContext sensorContext) {
    Map<String, Metric> mapping = measureKeyToMetrics();
    List<MeasurementHistory> indicators = client.getPerformanceIndicators(fortifyProject.getVersionId(), Lists.newArrayList(mapping.keySet()));
    for (MeasurementHistory indicator : indicators) {
      Metric metric = mapping.get(indicator.getMeasurementGuid());
      sensorContext.saveMeasure(metric, (double) indicator.getMeasurementValue());
    }
  }

  private void importVariables(SensorContext sensorContext) {
    Map<String, Metric> mapping = variableKeyToMetrics();
    List<VariableHistory> variables = client.getVariables(fortifyProject.getVersionId(), Lists.newArrayList(mapping.keySet()));
    for (VariableHistory v : variables) {
      Metric metric = mapping.get(v.getVariable().getVariable());
      sensorContext.saveMeasure(metric, (double) v.getVariableValue());
    }
  }

  private Map<String, Metric> measureKeyToMetrics() {
    // List here the indicators to download
    return ImmutableMap.of("FortifySecurityRating", FortifyMetrics.SECURITY_RATING);
  }

  private Map<String, Metric> variableKeyToMetrics() {
    // List here the variables to download
    return ImmutableMap.of(
      "CFPO", FortifyMetrics.CFPO,
      "HFPO", FortifyMetrics.HFPO,
      "LFPO", FortifyMetrics.LFPO,
      "MFPO", FortifyMetrics.MFPO
    );
  }

  @Override
  public String toString() {
    return "Fortify Performance Indicators";
  }
}
