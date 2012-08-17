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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.config.Settings;
import org.sonar.plugins.fortify.base.FortifyConstants;
import xmlns.www_fortify_com.schema.issuemanagement.IssueInstance;
import xmlns.www_fortify_com.schema.issuemanagement.IssueListDescription;
import xmlns.www_fortifysoftware_com.schema.wstypes.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class FortifyClient implements BatchExtension {

  private static final Logger LOG = LoggerFactory.getLogger(FortifyClient.class);
  private final Settings settings;
  private Services services;

  public FortifyClient(Settings settings) {
    this(settings, null);
  }

  @VisibleForTesting
  FortifyClient(Settings settings, @Nullable Services services) {
    this.settings = settings;
    this.services = services;
  }

  public void start() {
    if (!settings.getBoolean(FortifyConstants.PROPERTY_ENABLE)) {
      LOG.info("Import of Fortify report is disabled (see " + FortifyConstants.PROPERTY_ENABLE + ")");
    } else {
      String url = settings.getString(FortifyConstants.PROPERTY_URL);
      if (Strings.isNullOrEmpty(url)) {
        LOG.info("Fortify SSC Server URL is missing. Please check the property " + FortifyConstants.PROPERTY_URL);
      } else {
        LOG.info("Import of Fortify report is enabled. SSC Server is: " + url);
        String login = settings.getString(FortifyConstants.PROPERTY_LOGIN);
        String password = settings.getString(FortifyConstants.PROPERTY_PASSWORD);
        JaxWsProxyFactoryBean factory = initCxf(url, login, password);
        services = factory.create(Services.class);
      }
    }
  }

  @VisibleForTesting
  static JaxWsProxyFactoryBean initCxf(String rootUri, String login, String password) {
    JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(Services.class);
    factory.setAddress(rootUri + "/fm-ws/services");

    Map<String, Object> outProps = Maps.newHashMap();
    outProps.put(WSHandlerConstants.ACTION, "UsernameToken Timestamp");
    outProps.put(WSHandlerConstants.USER, login);
    outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
    outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new PasswordCallback(password));
    factory.getOutInterceptors().add(new WSS4JOutInterceptor(outProps));

    Map<String, Object> inProps = Maps.newHashMap();
    inProps.put(WSHandlerConstants.ACTION, "Timestamp");
    factory.getInInterceptors().add(new WSS4JInInterceptor(inProps));
    return factory;
  }

  @VisibleForTesting
  Services getServices() {
    return services;
  }

  public boolean isEnabled() {
    return services != null;
  }

  public List<Project> getProjects() {
    return services.projectList("").getProject();
  }

  /**
   * The versions of all the projects. Unfortunately it's not possible to get the versions of a give project.
   */
  public List<ProjectVersionLite> getProjectVersions() {
    return services.activeProjectVersionList("").getProjectVersion();
  }

  public List<IssueWrapper> getIssues(long projectVersionId) {
    String sessionId = createAuditSession(projectVersionId);
    try {
      ProjectIdentifier pid = new ProjectIdentifier();
      pid.setProjectVersionId(projectVersionId);

      IssueListRequest req = new IssueListRequest();
      req.setSessionId(sessionId);
      req.setProjectIdentifier(pid);

      IssueListDescription listDescription = new IssueListDescription();
      listDescription.setIncludeRemoved(false);
      listDescription.setIncludeSuppressed(false);
      req.setIssueListDescription(listDescription);

      List<IssueWrapper> result = Lists.newArrayList();
      for (IssueInstance issueInstance : services.issueList(req).getIssueList().getIssues().getIssue()) {
        IssueWrapper issue = IssueWrapper.create(issueInstance);
        result.add(issue);
        DescriptionAndRecommendationRequest detailReq = new DescriptionAndRecommendationRequest();
        detailReq.setProjectIdentifier(pid);
        detailReq.setIssueId(issueInstance.getInstanceId());
        detailReq.setSessionId(sessionId);
        DescriptionAndRecommendationResponse recommendation = services.descriptionAndRecommendation(detailReq);
        if (recommendation != null) {
          issue.setHtmlAbstract(recommendation.getAbstract());
        }
      }
      return result;

    } finally {
      closeAuditSession(sessionId);
    }
  }

  private String createAuditSession(long projectVersionId) {
    CreateAuditSessionRequest req = new CreateAuditSessionRequest();
    req.setProjectVersionId(projectVersionId);
    return services.createAuditSession(req).getSessionId();
  }

  private void closeAuditSession(@Nullable String sessionId) {
    if (sessionId != null) {
      try {
        InvalidateAuditSessionRequest req = new InvalidateAuditSessionRequest();
        req.setSessionId(sessionId);
        services.invalidateAuditSession(req);
      } catch (Exception e) {
        LoggerFactory.getLogger(FortifyClient.class).error("Fail to close audit session " + sessionId, e);
      }
    }
  }

  /**
   * Example of indicator keys : "CFPO", "FILES", "OWASP2004A1"
   */
  public List<VariableHistory> getVariables(long projectVersionId, List<String> variableKeys) {
    VariableHistoryListRequest req = new VariableHistoryListRequest();
    req.getProjectVersionIDs().add(projectVersionId);
    req.getVariableGuids().addAll(variableKeys);
    return FortifyClientUtils.keepMoreRecent(services.variableHistoryList(req));
  }

  /**
   * Example of indicator keys : "FortifySecurityRating", "TotalRemediationEffort", "PercentCriticalPriorityIssuesAudited"
   */
  public List<MeasurementHistory> getPerformanceIndicators(long projectVersionId, List<String> indicatorKeys) {
    MeasurementHistoryListRequest req = new MeasurementHistoryListRequest();
    req.getProjectVersionIDs().add(projectVersionId);
    req.getMeasurementGuids().addAll(indicatorKeys);
    return FortifyClientUtils.keepMoreRecent(services.measurementHistoryList(req));
  }
}