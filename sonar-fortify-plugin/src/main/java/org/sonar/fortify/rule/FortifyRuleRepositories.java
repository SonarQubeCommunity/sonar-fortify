/*
 * Fortify Plugin for SonarQube
 * Copyright (C) 2014 Vivien HENRIET and SonarSource
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
/*
* SonarQube Fortify Plugin
* Copyright (C) 2014 Vivien HENRIET and SonarSource
* dev@sonar.codehaus.org
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 3 of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02
*/
package org.sonar.fortify.rule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.sonar.api.ExtensionProvider;
import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;

import java.util.List;

public final class FortifyRuleRepositories extends ExtensionProvider implements ServerExtension {

  // List of supported languages as declared at http://www.hpenterprisesecurity.com/vulncat/en/vulncat/index.html
  public static final List<String> SUPPORTED_LANGUAGES = ImmutableList.of(
    "abap", "c", "cobol", "cpp", "cs", "flex", "java", "js", "php", "plsql", "py", "sql", "vb", "vbnet", "xml");

  private final Settings settings;

  public FortifyRuleRepositories(Settings settings) {
    this.settings = settings;
  }

  @Override
  public List<FortifyRuleRepository> provide() {
    List<FortifyRuleRepository> repositories = Lists.newArrayList();
    for (String language : FortifyRuleRepositories.SUPPORTED_LANGUAGES) {
      repositories.add(new FortifyRuleRepository(this.settings, language));
    }
    return repositories;
  }
}
