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

import com.fortify.manager.schema.*;
import com.fortify.ws.client.*;
import com.fortify.ws.core.WSAuthenticationProvider;
import com.fortify.ws.core.WSTemplateProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.config.Settings;
import org.sonar.plugins.fortify.base.FortifyConstants;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public class FortifyClient implements BatchExtension {

  private final Settings settings;
  private final ClientFactory clientFactory;
  private ContextTemplateProvider templateProvider;
  private Credential credential;

  public FortifyClient(Settings settings) {
    this(settings, new ClientFactory());
  }

  @VisibleForTesting
  FortifyClient(Settings settings, ClientFactory clientFactory) {
    this.settings = settings;
    this.clientFactory = clientFactory;
  }

  public void start() {
    String url = settings.getString(FortifyConstants.PROPERTY_URL);
    if (!Strings.isNullOrEmpty(url)) {
      String token = settings.getString(FortifyConstants.PROPERTY_TOKEN);
      String login = settings.getString(FortifyConstants.PROPERTY_LOGIN);
      String password = settings.getString(FortifyConstants.PROPERTY_PASSWORD);
      Credential c = (Strings.isNullOrEmpty(token) ? Credential.forLogin(login, password) : Credential.forToken(token));
      init(url, c);
    }
  }

  private void init(String rootUri, Credential credential) {
    InputStream input = FortifyClient.class.getResourceAsStream("/org/sonar/plugins/fortify/client/fortify-spring-wsclient-config.xml");
    try {
      GenericApplicationContext ctx = new GenericApplicationContext();
      XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
      xmlReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
      xmlReader.loadBeanDefinitions(new InputStreamResource(input));
      ctx.refresh();
      templateProvider = (ContextTemplateProvider) ctx.getBean("templateProvider");
      templateProvider.setUri(rootUri + "/fm-ws/services");
      this.credential = credential;
    } finally {
      Closeables.closeQuietly(input);
    }
  }

  @VisibleForTesting
  ContextTemplateProvider getTemplateProvider() {
    return templateProvider;
  }

  @VisibleForTesting
  Credential getCredential() {
    return credential;
  }

  public boolean isEnabled() {
    return templateProvider != null;
  }

  public List<Project> getProjects() {
    try {
      return clientFactory.newClient(ProjectClient.class, templateProvider, credential).getProjects();
    } catch (FortifyWebServiceException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * The versions of all the projects. Unfortunately it's not possible to get the versions of a give project.
   */
  public List<ProjectVersionLite> getProjectVersions() {
    try {
      return clientFactory.newClient(ProjectVersionClient.class, templateProvider, credential).getProjectVersions();
    } catch (FortifyWebServiceException e) {
      throw Throwables.propagate(e);
    }
  }

  public List<IssueInstance> getIssues(long projectVersionId) {
    AuditClient auditClient = clientFactory.newClient(AuditClient.class, templateProvider, credential);
    try {
      auditClient.startSession(projectVersionId);
      return auditClient.listIssues().getIssues().getIssue();

    } catch (Exception e) {
      throw Throwables.propagate(e);
    } finally {
      try {
        auditClient.endSession();
      } catch (Exception e) {
        LoggerFactory.getLogger(FortifyClient.class).error("Fail to end Fortify session on project version " + projectVersionId, e);
      }
    }
  }

  /**
   * Example of indicator keys : "CFPO", "FILES", "OWASP2004A1"
   */
  public List<VariableHistory> getVariables(long projectVersionId, List<String> variableKeys) {
    try {
      MeasurementClient measureClient = clientFactory.newClient(MeasurementClient.class, templateProvider, credential);
      return measureClient.getMostRecentVariableHistories(Arrays.asList(projectVersionId), variableKeys);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Example of indicator keys : "FortifySecurityRating", "TotalRemediationEffort", "PercentCriticalPriorityIssuesAudited"
   */
  public List<MeasurementHistory> getPerformanceIndicators(long projectVersionId, List<String> indicatorKeys) {
    try {
      MeasurementClient measureClient = clientFactory.newClient(MeasurementClient.class, templateProvider, credential);
      return measureClient.getMostRecentMeasurementHistories(Arrays.asList(projectVersionId), indicatorKeys);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  static class ClientFactory {
    <T extends AbstractWSClient> T newClient(Class<T> clazz, ContextTemplateProvider templateProvider, Credential credential) {
      try {
        Constructor<T> constructor = clazz.getConstructor(WSTemplateProvider.class, WSAuthenticationProvider.class, String.class);
        return constructor.newInstance(templateProvider, credential, null);
      } catch (Exception e) {
        throw new IllegalStateException("Fail to instantiate Fortify component: " + clazz, e);
      }
    }
  }
}