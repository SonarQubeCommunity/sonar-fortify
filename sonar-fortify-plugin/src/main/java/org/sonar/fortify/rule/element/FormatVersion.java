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
package org.sonar.fortify.rule.element;

public class FormatVersion implements Comparable<FormatVersion> {
  private final String versionString;
  private final String[] versionParts;

  FormatVersion(String versionString) {
    this.versionString = versionString;
    this.versionParts = versionString.split("\\.");
  }

  @Override
  public int compareTo(FormatVersion o) {
    int length = Math.max(this.versionParts.length, o.versionParts.length);
    for (int i = 0; i < length; i++) {
      int version1 = i < this.versionParts.length ?
        Integer.valueOf(this.versionParts[i]).intValue() : 0;
      int version2 = i < o.versionParts.length ?
        Integer.valueOf(o.versionParts[i]).intValue() : 0;
      if (version1 < version2) {
        return -1;
      }
      if (version1 > version2) {
        return 1;
      }
    }
    return 0;
  }

  @Override
  public String toString() {
    return this.versionString;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof FormatVersion && this.versionString.equals(((FormatVersion) o).versionString);
  }

  @Override
  public int hashCode() {
    return this.versionString.hashCode();
  }
}
