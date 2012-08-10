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

import com.fortify.schema.fws.*;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.sonar.api.config.Settings;
import org.sonar.plugins.fortify.base.FortifyConstants;
import xmlns.www_fortifysoftware_com.schema.wstypes.MeasurementHistory;
import xmlns.www_fortifysoftware_com.schema.wstypes.Project;
import xmlns.www_fortifysoftware_com.schema.wstypes.ProjectVersionLite;
import xmlns.www_fortifysoftware_com.schema.wstypes.VariableHistory;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class FortifyClientTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void should_init_cxf() {
    JaxWsProxyFactoryBean factoryBean = FortifyClient.initCxf("http://localhost:8081/ssc", "admin", "passwd");

    assertThat(factoryBean.getAddress()).isEqualTo("http://localhost:8081/ssc/fm-ws/services");

    // ws-security is used instead of standard basic auth
    assertThat(factoryBean.getUsername()).isNull();
    assertThat(factoryBean.getPassword()).isNull();
    WSS4JOutInterceptor wsSecurityInterceptor = (WSS4JOutInterceptor) factoryBean.getOutInterceptors().get(0);
    PasswordCallback passwordCallback = (PasswordCallback) wsSecurityInterceptor.getProperties().get(WSHandlerConstants.PW_CALLBACK_REF);
    assertThat(passwordCallback.getPassword()).isEqualTo("passwd");

  }

  @Test
  public void should_be_disabled_if_no_url() {
    FortifyClient client = new FortifyClient(new Settings());
    client.start();

    assertThat(client.isEnabled()).isFalse();
    assertThat(client.getServices()).isNull();
  }

  @Test
  public void test_login_password_credential() {
    Settings settings = new Settings();
    settings.setProperty(FortifyConstants.PROPERTY_URL, "http://localhost:8081/ssc");
    settings.setProperty(FortifyConstants.PROPERTY_LOGIN, "admin");
    settings.setProperty(FortifyConstants.PROPERTY_PASSWORD, "<password>");

    JaxWsProxyFactoryBean cxf = FortifyClient.initCxf("http://localhost:8081/ssc", "admin", "<passwd>");

    WSS4JOutInterceptor wss = (WSS4JOutInterceptor) cxf.getOutInterceptors().get(0);
    assertThat(wss.getProperties().get(WSHandlerConstants.USER)).isEqualTo("admin");

    PasswordCallback passwordCallback = (PasswordCallback) wss.getProperties().get(WSHandlerConstants.PW_CALLBACK_REF);
    assertThat(passwordCallback).isNotNull();
    assertThat(passwordCallback.getPassword()).isEqualTo("<passwd>");
  }

  @Test
  public void get_projects() throws Exception {
    Services services = mockValidServices();

    List<Project> projects = new FortifyClient(new Settings(), services).getProjects();

    verify(services).projectList("");
    assertThat(projects).isNotNull();
  }

  private Services mockValidServices() {
    return mock(Services.class, Mockito.withSettings().defaultAnswer(Mockito.RETURNS_MOCKS));
  }

  @Test
  public void fail_to_get_projects() throws Exception {
    thrown.expect(RuntimeException.class);

    Services services = mock(Services.class);
    when(services.projectList("")).thenThrow(new IllegalStateException());

    new FortifyClient(new Settings(), services).getProjects();
  }

  @Test
  public void get_project_versions() throws Exception {
    Services services = mockValidServices();
    List<ProjectVersionLite> versions = new FortifyClient(new Settings(), services).getProjectVersions();

    verify(services).activeProjectVersionList("");
    assertThat(versions).isNotNull();
  }

  @Test
  public void fail_to_get_project_versions() throws Exception {
    thrown.expect(RuntimeException.class);

    Services services = mock(Services.class);
    when(services.activeProjectVersionList("")).thenThrow(new IllegalStateException());

    new FortifyClient(new Settings(), services).getProjectVersions();
  }

  @Test
  public void get_issues() throws Exception {
    Services services = mockValidServices();
    Collection<IssueWrapper> issues = new FortifyClient(new Settings(), services).getIssues(123L);

    verify(services).createAuditSession(any(CreateAuditSessionRequest.class));
    verify(services).issueList(any(IssueListRequest.class));
    verify(services).invalidateAuditSession(any(InvalidateAuditSessionRequest.class));
    assertThat(issues).isNotNull();
  }

  @Test
  public void fail_quietly_to_close_issues_session() throws Exception {
    Services services = mockValidServices();
    when(services.invalidateAuditSession(any(InvalidateAuditSessionRequest.class))).thenThrow(new IllegalStateException());

    Collection<IssueWrapper> issues = new FortifyClient(new Settings(), services).getIssues(123L);

    verify(services).createAuditSession(any(CreateAuditSessionRequest.class));
    verify(services).issueList(any(IssueListRequest.class));
    verify(services).invalidateAuditSession(any(InvalidateAuditSessionRequest.class));
    assertThat(issues).isNotNull();
  }

  @Test
  public void should_fail_to_get_issues() throws Exception {
    thrown.expect(RuntimeException.class);

    Services services = mock(Services.class);
    when(services.issueList(any(IssueListRequest.class))).thenThrow(IllegalStateException.class);

    new FortifyClient(new Settings(), services).getIssues(3L);
  }

  @Test
  public void should_get_variables() throws DatatypeConfigurationException {
    VariableHistoryListResponse response = new VariableHistoryListResponse();
    response.getVariableHistories().add(FortifyTestUtils.newVariable("CFPO", "2010-01-01"));
    response.getVariableHistories().add(FortifyTestUtils.newVariable("CFPO", "2012-01-01"));
    response.getVariableHistories().add(FortifyTestUtils.newVariable("HFPO", "2012-01-01"));

    Services services = mockValidServices();
    when(services.variableHistoryList(argThat(new BaseMatcher<VariableHistoryListRequest>() {
      public boolean matches(Object o) {
        VariableHistoryListRequest req = (VariableHistoryListRequest) o;
        return req.getProjectVersionIDs().contains(3L) && req.getVariableGuids().containsAll(Arrays.asList("CFPO", "HFPO"));
      }

      public void describeTo(Description description) {
      }
    }))).thenReturn(response);


    List<VariableHistory> variables = new FortifyClient(new Settings(), services).getVariables(3L, Arrays.asList("CFPO", "HFPO"));

    assertThat(variables).hasSize(2);
    assertThat(variables).onProperty("variable.variable").containsOnly("CFPO", "HFPO");

    // only most recent variables must be kept
    for (VariableHistory variable : variables) {
      assertThat(variable.getSnapshot().getDate().getYear()).isEqualTo(2012);
    }
  }

  @Test
  public void should_get_measures() throws DatatypeConfigurationException {
    MeasurementHistoryListResponse response = new MeasurementHistoryListResponse();
    response.getMeasurementHistories().add(FortifyTestUtils.newMeasure("Perf1", "2010-01-01"));
    response.getMeasurementHistories().add(FortifyTestUtils.newMeasure("Perf1", "2012-01-01"));
    response.getMeasurementHistories().add(FortifyTestUtils.newMeasure("Perf2", "2012-01-01"));

    Services services = mockValidServices();
    when(services.measurementHistoryList(argThat(new BaseMatcher<MeasurementHistoryListRequest>() {
      public boolean matches(Object o) {
        MeasurementHistoryListRequest req = (MeasurementHistoryListRequest) o;
        return req.getProjectVersionIDs().contains(3L) && req.getMeasurementGuids().containsAll(Arrays.asList("Perf1", "Perf2"));
      }

      public void describeTo(Description description) {
      }
    }))).thenReturn(response);


    List<MeasurementHistory> measures = new FortifyClient(new Settings(), services).getPerformanceIndicators(3L, Arrays.asList("Perf1", "Perf2"));

    assertThat(measures).hasSize(2);
    assertThat(measures).onProperty("measurementGuid").containsOnly("Perf1", "Perf2");

    // only most recent measures must be kept
    for (MeasurementHistory measure : measures) {
      assertThat(measure.getSnapshot().getDate().getYear()).isEqualTo(2012);
    }
  }

}
