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
package org.sonar.fortify.fvdl.handler;

import org.sonar.fortify.base.handler.AbstractHandler;

import org.sonar.fortify.fvdl.element.ReplacementDefinition;
import org.xml.sax.Attributes;

import java.util.Set;

public class ReplacementDefinitionsHandler extends AbstractHandler<Set<ReplacementDefinition>> {

  private final ReplacementDefinitionHandler replacementDefinitionHandler;

  ReplacementDefinitionsHandler() {
    super("ReplacementDefinitions");
    this.replacementDefinitionHandler = new ReplacementDefinitionHandler();
    setChildren(this.replacementDefinitionHandler);
  }

  @Override
  protected void start(Attributes attributes) {
    this.replacementDefinitionHandler.reset();
  }

  @Override
  protected void end() {
    setResult(this.replacementDefinitionHandler.getResult());
  }
}
