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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.CheckForNull;

public final class DomUtils {
  private DomUtils() {
    // only static stuff
  }

  public static Element getSingleElementByTagName(Document document, String tagName) throws FortifyParseException {
    Element element = getAtMostOneElementByTagName(document, tagName);
    if (element == null) {
      throw new FortifyParseException("'" + tagName + "' is not found.");
    }
    return element;
  }

  @CheckForNull
  public static Element getAtMostOneElementByTagName(Document document, String tagName) throws FortifyParseException {
    return getAtMostOneElementByTagName(document.getElementsByTagName(tagName), tagName);
  }

  @CheckForNull
  public static Element getAtMostOneElementByTagName(Element element, String tagName) throws FortifyParseException {
    return getAtMostOneElementByTagName(element.getElementsByTagName(tagName), tagName);
  }

  public static Element getAtLeastOneElementByTagName(Element element, String tagName) throws FortifyParseException {
    return getAtLeastOneElementByTagName(element.getElementsByTagName(tagName), tagName);
  }

  public static Element getSingleElementByTagName(Element element, String tagName) throws FortifyParseException {
    Element subElement = getAtMostOneElementByTagName(element.getElementsByTagName(tagName), tagName);
    if (subElement == null) {
      throw new FortifyParseException("'" + tagName + "' is not found.");
    }
    return subElement;
  }

  private static Element getAtLeastOneElementByTagName(NodeList nodeList, String tagName) throws FortifyParseException {
    Element element;
    if (nodeList.getLength() > 0) {
      Node node = nodeList.item(0);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        element = (Element) node;
      } else {
        throw new FortifyParseException("Unexpected type " + node.getNodeType() + " for node '" + tagName + "'.");
      }
    } else {
      throw new FortifyParseException("'" + tagName + "' is not found.");
    }

    return element;
  }

  @CheckForNull
  private static Element getAtMostOneElementByTagName(NodeList nodeList, String tagName) throws FortifyParseException {
    Element element = null;
    if (nodeList.getLength() == 1) {
      Node node = nodeList.item(0);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        element = (Element) node;
      } else {
        throw new FortifyParseException("Unexpected type " + node.getNodeType() + " for node '" + tagName + "'.");
      }
    } else if (nodeList.getLength() > 1) {
      throw new FortifyParseException("Got " + nodeList.getLength()
        + " nodes matching '" + tagName
        + "', but only one is expected.");
    }

    return element;
  }

}
