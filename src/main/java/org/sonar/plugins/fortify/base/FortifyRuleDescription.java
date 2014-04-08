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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.fortify.base;

import javax.annotation.CheckForNull;

import java.util.ArrayList;
import java.util.List;

public class FortifyRuleDescription {
  @CheckForNull
  private final String id;
  @CheckForNull
  private final String descriptionAbstract;
  @CheckForNull
  private final String explanation;
  @CheckForNull
  private final String recommendations;
  private final List<Reference> references = new ArrayList<Reference>();

  public FortifyRuleDescription(@CheckForNull String id, @CheckForNull String descriptionAbstract,
    @CheckForNull String explanation, @CheckForNull String recommendations) {
    this.id = id;
    this.descriptionAbstract = descriptionAbstract;
    this.explanation = explanation;
    this.recommendations = recommendations;
  }

  public void addReference(String title, @CheckForNull String author) {
    this.references.add(new Reference(title, author));
  }

  @CheckForNull
  public String getId() {
    return this.id;
  }

  public String toHTML() {
    StringBuilder builder = new StringBuilder();
    if (this.descriptionAbstract != null) {
      builder.append("<h2>ABSTRACT</h2><p>").append(this.descriptionAbstract).append("</p>");
    }
    if (this.explanation != null) {
      builder.append("<h2>EXPLANATION</h2><p>").append(this.explanation).append("</p>");
    }
    if (!this.references.isEmpty()) {
      builder.append("<h2>REFERENCES</h2>");
      for (int index = 0; index < this.references.size(); index++) {
        Reference reference = this.references.get(index);
        builder.append("<p>[").append(index + 1).append("] ").append(reference.getTitle());
        if (reference.getAuthor() != null) {
          builder.append(" - ").append(reference.getAuthor());
        }
        builder.append("</p>");
      }
    }

    return builder.toString();
  }

  private static class Reference {
    private final String title;
    @CheckForNull
    private final String author;

    public Reference(String title, @CheckForNull String author) {
      this.title = title;
      this.author = author;
    }

    public String getTitle() {
      return this.title;
    }

    @CheckForNull
    public String getAuthor() {
      return this.author;
    }
  }
}
