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
package org.sonar.fortify.rule.handler;

import org.sonar.fortify.base.handler.AbstractSetHandler;
import org.sonar.fortify.base.handler.StringHandler;
import org.sonar.fortify.rule.element.Description;
import org.sonar.fortify.rule.element.Reference;
import org.xml.sax.Attributes;

import java.util.Collection;

public class DescriptionHandler extends AbstractSetHandler<Description> {
  private final StringHandler abstractHandler;
  private final StringHandler explanationHandler;
  private final StringHandler recommendationsHandler;
  private final ReferencesHandler referencesHandler;
  private Description description;

  DescriptionHandler() {
    super("Description");
    this.abstractHandler = new StringHandler("Abstract");
    this.explanationHandler = new StringHandler("Explanation");
    this.recommendationsHandler = new StringHandler("Recommendations");
    this.referencesHandler = new ReferencesHandler();
    setChildren(this.abstractHandler, this.explanationHandler, this.recommendationsHandler, this.referencesHandler);
  }

  @Override
  public void start(Attributes attributes) {
    this.description = new Description();
    this.description.setId(attributes.getValue("id"));
    this.description.setRef(attributes.getValue("ref"));
  }

  @Override
  public void end() {
    this.description.setDescriptionAbstract(this.abstractHandler.getResult());
    this.description.setExplanation(this.explanationHandler.getResult());
    this.description.setRecommendations(this.recommendationsHandler.getResult());
    Collection<Reference> references = this.referencesHandler.getResult();
    if (references != null) {
      this.description.setReferences(references);
    }
    add(this.description);
  }
}
