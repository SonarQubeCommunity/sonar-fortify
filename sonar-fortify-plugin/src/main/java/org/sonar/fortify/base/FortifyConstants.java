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
package org.sonar.fortify.base;

import org.apache.commons.lang.StringUtils;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import java.text.Normalizer;
import java.util.Locale;

public class FortifyConstants {

  public static final String AUDIT_FVDL_FILE = "audit.fvdl";

  public static final String REPORT_PATH_PROPERTY = "sonar.fortify.reportPath";
  public static final String RULEPACK_PATHS_PROPERTY = "sonar.fortify.rulepackPaths";

  private FortifyConstants() {
    // only static stuff
  }

  public static String fortifyRepositoryKey(String language) {
    return "fortify-" + StringUtils.lowerCase(language);
  }

  @CheckForNull
  public static String fortifySQRuleKey(@Nullable String kingdom, @Nullable String category, @Nullable String subcategory) {
    StringBuilder sb = new StringBuilder();
    if (StringUtils.isNotBlank(kingdom)) {
      sb.append(slugifyForKey(kingdom)).append("_");
    }
    if (StringUtils.isNotBlank(category)) {
      sb.append(slugifyForKey(category));
    }
    if (StringUtils.isNotBlank(subcategory)) {
      sb.append("_");
      sb.append(slugifyForKey(subcategory));
    }
    return sb.length() > 0 ? sb.toString() : null;
  }

  private static String slugifyForKey(String s) {
    return Normalizer.normalize(s, Normalizer.Form.NFD)
      .replaceAll("[^\\p{ASCII}]", "")
      .replaceAll("[^\\w+]", "_")
      .replaceAll("\\s+", "_")
      .replaceAll("[-]+", "_")
      .replaceAll("^_", "")
      .replaceAll("_$", "").toLowerCase(Locale.ENGLISH);
  }
}
