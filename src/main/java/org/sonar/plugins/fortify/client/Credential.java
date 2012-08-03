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
import com.google.common.base.Strings;

public final class Credential implements WSAuthenticationProvider {
  private String username;
  private String password;
  private String token;

  private Credential(String username, String password, String token) {
    this.username = username;
    this.password = password;
    this.token = token;
  }

  public String getUserName() {
    return isUsingTokenAuth() ? null : username;
  }

  public String getPassword() {
    return isUsingTokenAuth() ? null : password;
  }

  public boolean isUsingTokenAuth() {
    return !Strings.isNullOrEmpty(token);
  }

  public String getToken() {
    return token;
  }

  public static Credential forLogin(String login, String password) {
    return new Credential(login, password, null);
  }

  public static Credential forToken(String token) {
    return new Credential(null, null, token);
  }
}
