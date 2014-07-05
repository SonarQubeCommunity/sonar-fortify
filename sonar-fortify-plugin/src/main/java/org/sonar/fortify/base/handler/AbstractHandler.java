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
import org.xml.sax.helpers.DefaultHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public abstract class AbstractHandler<T> extends DefaultHandler {
  private final String name;
  private Collection<? extends AbstractHandler<?>> children = Collections.emptyList();
  private AbstractHandler<?> currentChild = null;
  private T result;

  public AbstractHandler(String name) {
    super();
    this.name = name;
  }

  protected void setChildren(AbstractHandler<?>... children) {
    this.children = Arrays.asList(children);
  }

  protected boolean isStart(String qName) {
    return this.name.equals(qName);
  }

  protected boolean isEnd(String qName) {
    return this.name.equals(qName);
  }

  protected void start(@SuppressWarnings("unused") Attributes attributes) {
    // Can be override
  }

  protected void end() {
    // Can be override
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (this.currentChild == null) {
      for (AbstractHandler<?> child : this.children) {
        if (child.isStart(qName)) {
          this.currentChild = child;
          this.currentChild.start(attributes);
        }
      }
      if (this.currentChild == null) {
        this.currentChild = new LeafHandler();
      }
    } else {
      this.currentChild.startElement(uri, localName, qName, attributes);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (this.currentChild != null) {
      if (this.currentChild.isEnd(qName)) {
        this.currentChild.end();
        this.currentChild = null;
      } else {
        this.currentChild.endElement(uri, localName, qName);
      }
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (this.currentChild != null) {
      this.currentChild.characters(ch, start, length);
    }
  }

  public T getResult() {
    T ret = this.result;
    this.result = null;
    return ret;
  }

  public void setResult(T result) {
    this.result = result;
  }
}
