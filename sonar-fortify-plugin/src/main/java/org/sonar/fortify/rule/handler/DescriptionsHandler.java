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

import org.sonar.fortify.base.handler.AbstractHandler;
import org.sonar.fortify.rule.element.Description;
import org.xml.sax.Attributes;

import java.util.Collection;
import java.util.Collections;

public class DescriptionsHandler extends AbstractHandler<Collection<Description>> {
  private final DescriptionHandler descriptionHandler;

  DescriptionsHandler() {
    super("Descriptions");
    this.descriptionHandler = new DescriptionHandler();
    setChildren(this.descriptionHandler);
  }

  @Override
  protected void start(Attributes attributes) {
    this.descriptionHandler.reset();
  }

  @Override
  public void end() {
    Collection<Description> descriptions = this.descriptionHandler.getResult();
    if (descriptions == null) {
      setResult(Collections.<Description>emptyList());
    } else {
      setResult(descriptions);
    }
  }
}
