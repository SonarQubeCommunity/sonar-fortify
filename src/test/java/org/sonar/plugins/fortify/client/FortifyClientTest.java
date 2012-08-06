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

import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.plugins.fortify.base.FortifyConstants;

import static org.fest.assertions.Assertions.assertThat;

public class FortifyClientTest {

  @Test
  public void test_spring_configuration() {
    Settings settings = new Settings();
    settings.setProperty(FortifyConstants.PROPERTY_URL, "http://localhost:8081/ssc");
    settings.setProperty(FortifyConstants.PROPERTY_TOKEN, "ABCDE");

    FortifyClient client = new FortifyClient(settings);
    client.start();

    assertThat(client.getTemplateProvider()).isNotNull();
    assertThat(client.getTemplateProvider().getUri()).isEqualTo("http://localhost:8081/ssc/fm-ws/services");
    assertThat(client.getTemplateProvider().getTemplate()).isNotNull();
    assertThat(client.getTemplateProvider().getAuthenicationProvider()).isNull();
    assertThat(client.getCredential().getToken()).isEqualTo("ABCDE");
    assertThat(client.isEnabled()).isTrue();
  }

  @Test
  public void should_be_disabled_if_no_url() {
    FortifyClient client = new FortifyClient(new Settings());
    client.start();

    assertThat(client.isEnabled()).isFalse();
    assertThat(client.getTemplateProvider()).isNull();
    assertThat(client.getCredential()).isNull();
  }

  @Test
  public void test_login_password_credential() {
    Settings settings = new Settings();
    settings.setProperty(FortifyConstants.PROPERTY_URL, "http://localhost:8081/ssc");
    settings.setProperty(FortifyConstants.PROPERTY_LOGIN, "admin");
    settings.setProperty(FortifyConstants.PROPERTY_PASSWORD, "<password>");

    FortifyClient client = new FortifyClient(settings);
    client.start();

    assertThat(client.getTemplateProvider()).isNotNull();
    assertThat(client.getCredential().getToken()).isNull();
    assertThat(client.getCredential().getUserName()).isEqualTo("admin");
    assertThat(client.getCredential().getPassword()).isEqualTo("<password>");
    assertThat(client.isEnabled()).isTrue();
  }
}
