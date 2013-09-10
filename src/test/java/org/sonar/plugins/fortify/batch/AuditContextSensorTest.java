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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.plugins.fortify.base.FortifyMetrics;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

public class AuditContextSensorTest {
  @Test
  public void should_be_executed_if_existing_fortify_project() {
    FortifyProject fortifyProject = mock(FortifyProject.class);
    when(fortifyProject.exists()).thenReturn(true);

    assertThat(new AuditContextSensor(fortifyProject).shouldExecuteOnProject(new Project("foo"))).isTrue();
  }

  @Test
  public void should_not_be_executed_if_no_fortify_project() {
    FortifyProject fortifyProject = mock(FortifyProject.class);
    when(fortifyProject.exists()).thenReturn(false);

    assertThat(new AuditContextSensor(fortifyProject).shouldExecuteOnProject(new Project("foo"))).isFalse();
  }

  @Test
  public void should_save_context() {
    FortifyProject fortifyProject = mock(FortifyProject.class);
    when(fortifyProject.getName()).thenReturn("Logistics");
    when(fortifyProject.getVersion()).thenReturn("2.5");

    AuditContextSensor sensor = new AuditContextSensor(fortifyProject);
    SensorContext sensorContext = mock(SensorContext.class);
    sensor.analyse(new Project("foo"), sensorContext);

    verify(sensorContext).saveMeasure(argThat(new BaseMatcher<Measure>() {
      public boolean matches(Object o) {
        Measure m = (Measure) o;
        return m.getMetricKey().equals(FortifyMetrics.AUDIT_CONTEXT_KEY) && m.getData().equals("name=Logistics;version=2.5");
      }

      public void describeTo(Description description) {
      }
    }));
  }

  @Test
  public void test_to_string() {
    AuditContextSensor sensor = new AuditContextSensor(mock(FortifyProject.class));
    // overridden, not something like "org.sonar.plugins.fortify.batch.AuditContextSensor@276bab54"
    assertThat(sensor.toString()).doesNotContain("@");
  }
}
