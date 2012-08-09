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
package org.sonar.plugins.fortify.base;

import org.junit.Test;
import org.sonar.api.measures.Metric;

import static org.fest.assertions.Assertions.assertThat;

public class FortifyMetricsTest {
  @Test
  public void test_getMetrics() {
    assertThat(new FortifyMetrics().getMetrics()).isNotNull();
  }

  @Test
  public void keys_should_be_prefixed_with_fortify() {
    for (Metric metric : new FortifyMetrics().getMetrics()) {
      assertThat(metric.getKey()).startsWith("fortify-");
    }
  }

  @Test
  public void domain_should_be_specific_to_fortify() {
    for (Metric metric : new FortifyMetrics().getMetrics()) {
      assertThat(metric.getDomain()).isEqualTo(FortifyMetrics.DOMAIN);
    }
  }
}
