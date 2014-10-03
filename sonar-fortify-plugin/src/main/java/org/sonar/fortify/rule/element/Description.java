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

import java.util.ArrayList;
import java.util.Collection;

public class Description {
  private String id;
  private String ref;
  private String descriptionAbstract;
  private String explanation;
  private String recommendations;
  private final Collection<Reference> references = new ArrayList<Reference>();
  private final Collection<String> tips = new ArrayList<String>();

  public String getId() {
    return this.id;
  }

  public Description setId(String id) {
    this.id = id;
    return this;
  }

  public String getRef() {
    return this.ref;
  }

  public Description setRef(String ref) {
    this.ref = ref;
    return this;
  }

  public Description setDescriptionAbstract(String descriptionAbstract) {
    this.descriptionAbstract = descriptionAbstract;
    return this;
  }

  public Description setExplanation(String explanation) {
    this.explanation = explanation;
    return this;
  }

  public Description setRecommendations(String recommendations) {
    this.recommendations = recommendations;
    return this;
  }

  public String getRecommendations() {
    return this.recommendations;
  }

  public Description addReference(Reference reference) {
    this.references.add(reference);
    return this;
  }

  public Description addTip(String tip) {
    this.tips.add(tip);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (this.descriptionAbstract != null) {
      builder.append("<h2>ABSTRACT</h2><p>").append(this.descriptionAbstract).append("</p>");
    }
    if (this.explanation != null) {
      builder.append("<h2>EXPLANATION</h2><p>").append(this.explanation).append("</p>");
    }
    if (this.recommendations != null) {
      builder.append("<h2>RECOMMENDATIONS</h2><p>").append(this.recommendations).append("</p>");
    }
    if (!this.tips.isEmpty()) {
      builder.append("<h2>TIPS</h2><p><ul>");
      for (String tip : this.tips) {
        builder.append("<li>").append(tip).append("</li>");
      }
      builder.append("</ul></p>");
    }
    if (!this.references.isEmpty()) {
      builder.append("<h2>REFERENCES</h2>");
      int index = 0;
      for (Reference reference : this.references) {
        builder.append("<p>[").append(index + 1).append("] ").append(reference.getTitle());
        if (reference.getAuthor() != null) {
          builder.append(" - ").append(reference.getAuthor());
        }
        builder.append("</p>");
        index++;
      }
    }

    return builder.toString();
  }
}
