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

import com.fortify.ws.core.WSAuthenticationProvider;
import com.fortify.ws.core.WSTemplateProvider;
import org.springframework.ws.client.core.WebServiceTemplate;

public class ContextTemplateProvider implements WSTemplateProvider {

  private WebServiceTemplate webServiceTemplate;
  private String uri;

  public ContextTemplateProvider() {
  }

  public WSAuthenticationProvider getAuthenicationProvider() {
    return null;
  }

  public WebServiceTemplate getTemplate() {
    return webServiceTemplate;
  }

  /**
   * Used by spring
   */
  public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
    this.webServiceTemplate = webServiceTemplate;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String s) {
    this.uri = s;
    if (webServiceTemplate != null) {
      webServiceTemplate.setDefaultUri(s);
    }
  }
}
