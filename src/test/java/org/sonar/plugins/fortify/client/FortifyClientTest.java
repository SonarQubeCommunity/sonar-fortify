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

import com.fortify.manager.schema.IssueListing;
import com.fortify.manager.schema.Project;
import com.fortify.manager.schema.ProjectVersionLite;
import com.fortify.manager.schema.Status;
import com.fortify.ws.client.*;
import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;
import org.sonar.plugins.fortify.base.FortifyConstants;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class FortifyClientTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

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

  @Test
  public void instantiate_fortify_clients() {
    FortifyClient.ClientFactory factory = new FortifyClient.ClientFactory();
    ContextTemplateProvider templateProvider = new ContextTemplateProvider();
    Credential credential = Credential.forToken("ABCDE");

    assertThat(factory.newClient(AuditClient.class, templateProvider, credential).getClass()).isEqualTo(AuditClient.class);
    assertThat(factory.newClient(ProjectClient.class, templateProvider, credential).getClass()).isEqualTo(ProjectClient.class);
    assertThat(factory.newClient(ProjectVersionClient.class, templateProvider, credential).getClass()).isEqualTo(ProjectVersionClient.class);
    assertThat(factory.newClient(MeasurementClient.class, templateProvider, credential).getClass()).isEqualTo(MeasurementClient.class);
  }

  @Test
  public void get_projects() throws Exception {
    ProjectClient projectClient = mock(ProjectClient.class);
    FortifyClient.ClientFactory clientFactory = mock(FortifyClient.ClientFactory.class);
    when(clientFactory.newClient(eq(ProjectClient.class), any(ContextTemplateProvider.class), any(Credential.class))).thenReturn(projectClient);

    List<Project> projects = new FortifyClient(new Settings(), clientFactory).getProjects();

    verify(projectClient).getProjects();
    assertThat(projects).isNotNull();
  }

  @Test
  public void fail_to_get_projects() throws Exception {
    thrown.expect(RuntimeException.class);

    ProjectClient projectClient = mock(ProjectClient.class);
    when(projectClient.getProjects()).thenThrow(new FortifyWebServiceException(new Status()));
    FortifyClient.ClientFactory clientFactory = mock(FortifyClient.ClientFactory.class);
    when(clientFactory.newClient(eq(ProjectClient.class), any(ContextTemplateProvider.class), any(Credential.class))).thenReturn(projectClient);

    new FortifyClient(new Settings(), clientFactory).getProjects();
  }

  @Test
  public void get_project_versions() throws Exception {
    ProjectVersionClient vClient = mock(ProjectVersionClient.class);
    when(vClient.getProjectVersions()).thenReturn(Lists.<ProjectVersionLite>newArrayList());
    FortifyClient.ClientFactory clientFactory = mock(FortifyClient.ClientFactory.class);
    when(clientFactory.newClient(eq(ProjectVersionClient.class), any(ContextTemplateProvider.class), any(Credential.class))).thenReturn(vClient);

    List<ProjectVersionLite> projectVersions = new FortifyClient(new Settings(), clientFactory).getProjectVersions();

    verify(vClient).getProjectVersions();
    assertThat(projectVersions).isNotNull();
  }

  @Test
  public void fail_to_get_project_versions() throws Exception {
    thrown.expect(RuntimeException.class);

    ProjectVersionClient vClient = mock(ProjectVersionClient.class);
    when(vClient.getProjectVersions()).thenThrow(new FortifyWebServiceException(new Status()));
    FortifyClient.ClientFactory clientFactory = mock(FortifyClient.ClientFactory.class);
    when(clientFactory.newClient(eq(ProjectVersionClient.class), any(ContextTemplateProvider.class), any(Credential.class))).thenReturn(vClient);

    new FortifyClient(new Settings(), clientFactory).getProjectVersions();
  }

  @Test
  public void get_issues() throws Exception {
    AuditClient auditClient = mock(AuditClient.class);
    IssueListing issueListing = new IssueListing();
    issueListing.setIssues(new IssueListing.Issues());
    when(auditClient.listIssues()).thenReturn(issueListing);
    FortifyClient.ClientFactory clientFactory = mock(FortifyClient.ClientFactory.class);
    when(clientFactory.newClient(eq(AuditClient.class), any(ContextTemplateProvider.class), any(Credential.class))).thenReturn(auditClient);

    new FortifyClient(new Settings(), clientFactory).getIssues(3L);

    verify(auditClient).startSession(3L);
    verify(auditClient).listIssues();
    verify(auditClient).endSession();
  }

  @Test
  public void fail_to_get_issues() throws Exception {
    thrown.expect(RuntimeException.class);

    AuditClient auditClient = mock(AuditClient.class);
    when(auditClient.listIssues()).thenThrow(new FortifyWebServiceException(new Status()));
    FortifyClient.ClientFactory clientFactory = mock(FortifyClient.ClientFactory.class);
    when(clientFactory.newClient(eq(AuditClient.class), any(ContextTemplateProvider.class), any(Credential.class))).thenReturn(auditClient);

    new FortifyClient(new Settings(), clientFactory).getIssues(3L);
  }
}
