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
package org.sonar.fortify.base.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class LeafHandler extends AbstractHandler<Void> {
  private int level = 0;

  public LeafHandler() {
    super("Leaf");
  }

  @Override
  protected boolean isEnd(String qName) {
    return this.level == 0;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    this.level++;
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    this.level--;
  }
}
