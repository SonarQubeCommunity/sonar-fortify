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
package org.sonar.plugins.fortify.client;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.fest.assertions.Assertions.assertThat;

public class FortifyClientTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test_spring_configuration() {
    FortifyClient client = FortifyClient.create("http://localhost:8081/ssc/fm-ws/services", Credential.forToken("ABCDE"));

    assertThat(client.getTemplateProvider()).isNotNull();
    assertThat(client.getCredential().getToken()).isEqualTo("ABCDE");
  }

  @Test
  public void uri_is_mandatory() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Fortify SCA URL must be set");
    FortifyClient.create("", Credential.forToken("ABCDE"));
  }

  @Test
  public void credential_is_mandatory() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("Fortify credentials must be set");
    FortifyClient.create("http://localhost:8081/ssc/fm-ws/services", null);
  }
}
