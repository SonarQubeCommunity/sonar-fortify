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

import static org.fest.assertions.Assertions.assertThat;

public class CredentialTest {
  @Test
  public void test_login() {
    Credential credential = Credential.forLogin("teddyrinner", "jo2012");
    assertThat(credential.isUsingTokenAuth()).isFalse();
    assertThat(credential.getUserName()).isEqualTo("teddyrinner");
    assertThat(credential.getPassword()).isEqualTo("jo2012");
    assertThat(credential.getToken()).isNull();
  }

  @Test
  public void test_token() {
    Credential credential = Credential.forToken("ABCDE");
    assertThat(credential.isUsingTokenAuth()).isTrue();
    assertThat(credential.getUserName()).isNull();
    assertThat(credential.getPassword()).isNull();
    assertThat(credential.getToken()).isEqualTo("ABCDE");
  }
}
