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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class FortifyClient {

  private ContextTemplateProvider templateProvider;
  private Credential credential;

  private FortifyClient() {
  }

  private FortifyClient init(String uri, Credential credential) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(uri), "Fortify SCA URL must be set");
    Preconditions.checkNotNull(credential, "Fortify credentials must be set");

    InputStream input = getClass().getResourceAsStream("/fortify-spring-wsclient-config.xml");
    try {
      GenericApplicationContext ctx = new GenericApplicationContext();
      XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
      xmlReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
      xmlReader.loadBeanDefinitions(new InputStreamResource(input));
      ctx.refresh();
      templateProvider = (ContextTemplateProvider) ctx.getBean("templateProvider");
      templateProvider.setUri(uri);
      this.credential = credential;
      return this;
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

  public List<Project> getProjects() {
    try {
      return new ProjectClient(templateProvider, credential, null).getProjects();
    } catch (FortifyWebServiceException e) {
      throw Throwables.propagate(e);
    }
  }

  public List<ProjectVersionLite> getProjectVersions() {
    try {
      return new ProjectVersionClient(templateProvider, credential, null).getProjectVersions();
    } catch (FortifyWebServiceException e) {
      throw Throwables.propagate(e);
    }
  }

  public List<IssueInstance> getIssues(long projectVersionId) {
    AuditClient auditClient = new AuditClient(templateProvider, credential, null);
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
      MeasurementClient measureClient = new MeasurementClient(templateProvider, credential, null);
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
      MeasurementClient measureClient = new MeasurementClient(templateProvider, credential, null);
      return measureClient.getMostRecentMeasurementHistories(Arrays.asList(projectVersionId), indicatorKeys);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public static FortifyClient create(String uri, Credential credential) {
    return new FortifyClient().init(uri, credential);
  }
}