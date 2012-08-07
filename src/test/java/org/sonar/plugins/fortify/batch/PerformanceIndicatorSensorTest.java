/*
 * Sonar Fortify Plugin
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

import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.fortify.base.FortifyMetrics;
import org.sonar.plugins.fortify.client.FortifyClient;
import xmlns.www_fortifysoftware_com.schema.wstypes.MeasurementHistory;

import java.util.Arrays;
import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PerformanceIndicatorSensorTest {

  private Project sonarProject = new Project("foo");

  @Test
  public void should_be_enabled_if_fortify_project_exists() {
    FortifyClient client = mock(FortifyClient.class);
    FortifyProject fortifyProject = mock(FortifyProject.class);
    when(fortifyProject.exists()).thenReturn(true);

    PerformanceIndicatorSensor sensor = new PerformanceIndicatorSensor(client, fortifyProject);

    assertThat(sensor.shouldExecuteOnProject(sonarProject)).isTrue();
  }

  @Test
  public void should_be_disabled_if_fortify_project_does_not_exist() {
    FortifyClient client = mock(FortifyClient.class);
    FortifyProject fortifyProject = mock(FortifyProject.class);
    when(fortifyProject.exists()).thenReturn(false);

    PerformanceIndicatorSensor sensor = new PerformanceIndicatorSensor(client, fortifyProject);

    assertThat(sensor.shouldExecuteOnProject(sonarProject)).isFalse();
  }

  @Test
  public void test_to_string() {
    PerformanceIndicatorSensor sensor = new PerformanceIndicatorSensor(mock(FortifyClient.class), mock(FortifyProject.class));
    // overridden, not something like "org.sonar.plugins.fortify.batch.PerformanceIndicatorSensor@276bab54"
    assertThat(sensor.toString()).doesNotContain("@");
  }

  @Test
  public void should_load_performance_indicators() {
    FortifyClient client = mock(FortifyClient.class);
    when(client.getPerformanceIndicators(3L, Arrays.asList("FortifySecurityRating"))).thenReturn(Arrays.asList(
      newIndicator("FortifySecurityRating", 123.4f)
    ));
    FortifyProject fortifyProject = mock(FortifyProject.class);
    when(fortifyProject.getVersionId()).thenReturn(3L);
    SensorContext sensorContext = mock(SensorContext.class);

    PerformanceIndicatorSensor sensor = new PerformanceIndicatorSensor(client, fortifyProject);
    sensor.analyse(sonarProject, sensorContext);

    verify(sensorContext).saveMeasure(eq(FortifyMetrics.SECURITY_RATING), AdditionalMatchers.eq(123.4, 0.01));
  }

  @Test
  public void performance_indicators_are_not_available() {
    FortifyClient client = mock(FortifyClient.class);
    when(client.getPerformanceIndicators(3L, Arrays.asList("FortifySecurityRating"))).thenReturn(Collections.<MeasurementHistory>emptyList());
    FortifyProject fortifyProject = mock(FortifyProject.class);
    when(fortifyProject.getVersionId()).thenReturn(3L);
    SensorContext sensorContext = mock(SensorContext.class);

    PerformanceIndicatorSensor sensor = new PerformanceIndicatorSensor(client, fortifyProject);
    sensor.analyse(sonarProject, sensorContext);

    verifyZeroInteractions(sensorContext);
  }

  private MeasurementHistory newIndicator(String key, float val) {
    MeasurementHistory mh = new MeasurementHistory();
    mh.setMeasurementGuid(key);
    mh.setMeasurementValue(val);
    return mh;
  }
}
