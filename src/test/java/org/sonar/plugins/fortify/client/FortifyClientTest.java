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

    assertThat(client.isEnabled()).isTrue();
    assertThat(client.getClientFactory()).isNotNull();
    assertThat(client.getClientFactory().templateProvider.getUri()).isEqualTo("http://localhost:8081/ssc/fm-ws/services");
    assertThat(client.getClientFactory().templateProvider.getTemplate()).isNotNull();
    assertThat(client.getClientFactory().templateProvider.getAuthenicationProvider()).isNull();
    assertThat(client.getClientFactory().credential.getToken()).isEqualTo("ABCDE");
  }

  @Test
  public void should_be_disabled_if_no_url() {
    FortifyClient client = new FortifyClient(new Settings());
    client.start();

    assertThat(client.isEnabled()).isFalse();
    assertThat(client.getClientFactory().templateProvider).isNull();
    assertThat(client.getClientFactory().credential).isNull();
  }

  @Test
  public void test_login_password_credential() {
    Settings settings = new Settings();
    settings.setProperty(FortifyConstants.PROPERTY_URL, "http://localhost:8081/ssc");
    settings.setProperty(FortifyConstants.PROPERTY_LOGIN, "admin");
    settings.setProperty(FortifyConstants.PROPERTY_PASSWORD, "<password>");

    FortifyClient client = new FortifyClient(settings);
    client.start();

    assertThat(client.isEnabled()).isTrue();
    assertThat(client.getClientFactory().templateProvider).isNotNull();
    Credential credential = client.getClientFactory().credential;
    assertThat(credential.getToken()).isNull();
    assertThat(credential.getUserName()).isEqualTo("admin");
    assertThat(credential.getPassword()).isEqualTo("<password>");
  }

  @Test
  public void instantiate_fortify_clients() {
    ContextTemplateProvider templateProvider = new ContextTemplateProvider();
    Credential credential = Credential.forToken("ABCDE");
    FortifyClient.ClientFactory factory = new FortifyClient.ClientFactory().init(templateProvider, credential);

    assertThat(factory.newClient(AuditClient.class).getClass()).isEqualTo(AuditClient.class);
    assertThat(factory.newClient(ProjectClient.class).getClass()).isEqualTo(ProjectClient.class);
    assertThat(factory.newClient(ProjectVersionClient.class).getClass()).isEqualTo(ProjectVersionClient.class);
    assertThat(factory.newClient(MeasurementClient.class).getClass()).isEqualTo(MeasurementClient.class);
  }

  @Test
  public void get_projects() throws Exception {
    ProjectClient projectClient = mock(ProjectClient.class);
    FortifyClient.ClientFactory clientFactory = mock(FortifyClient.ClientFactory.class);
    when(clientFactory.newClient(ProjectClient.class)).thenReturn(projectClient);

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
    when(clientFactory.newClient(ProjectClient.class)).thenReturn(projectClient);

    new FortifyClient(new Settings(), clientFactory).getProjects();
  }

  @Test
  public void get_project_versions() throws Exception {
    ProjectVersionClient vClient = mock(ProjectVersionClient.class);
    when(vClient.getProjectVersions()).thenReturn(Lists.<ProjectVersionLite>newArrayList());
    FortifyClient.ClientFactory clientFactory = mock(FortifyClient.ClientFactory.class);
    when(clientFactory.newClient(ProjectVersionClient.class)).thenReturn(vClient);

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
    when(clientFactory.newClient(ProjectVersionClient.class)).thenReturn(vClient);

    new FortifyClient(new Settings(), clientFactory).getProjectVersions();
  }

  @Test
  public void get_issues() throws Exception {
    AuditClient auditClient = mock(AuditClient.class);
    IssueListing issueListing = new IssueListing();
    issueListing.setIssues(new IssueListing.Issues());
    when(auditClient.listIssues()).thenReturn(issueListing);
    FortifyClient.ClientFactory clientFactory = mock(FortifyClient.ClientFactory.class);
    when(clientFactory.newClient(AuditClient.class)).thenReturn(auditClient);

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
    when(clientFactory.newClient(AuditClient.class)).thenReturn(auditClient);

    new FortifyClient(new Settings(), clientFactory).getIssues(3L);
  }


}
