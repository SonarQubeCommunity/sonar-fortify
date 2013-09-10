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

import com.google.common.collect.Maps;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.KeyValueFormat;
import org.sonar.plugins.fortify.base.FortifyMetrics;

import java.util.Map;

public class AuditContextSensor implements Sensor {

  private final FortifyProject fortifyProject;

  public AuditContextSensor(FortifyProject fortifyProject) {
    this.fortifyProject = fortifyProject;
  }

  public boolean shouldExecuteOnProject(Project project) {
    return fortifyProject.exists();
  }

  public void analyse(Project project, SensorContext sensorContext) {
    Map<String,String> context = Maps.newTreeMap();
    context.put("name", fortifyProject.getName());
    context.put("version", fortifyProject.getVersion());
    sensorContext.saveMeasure(new Measure(FortifyMetrics.AUDIT_CONTEXT, KeyValueFormat.format(context)));
  }

  @Override
  public String toString() {
    return "Fortify Audit Context";
  }
}
