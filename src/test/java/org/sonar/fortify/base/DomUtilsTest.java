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
package org.sonar.fortify.base;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DomUtilsTest {

  @Test
  public void testGetSingleElementByTagName_fromDocument() throws FortifyParseException {
    Element element = mock(Element.class);
    NodeList nodeList = mock(NodeList.class);
    when(element.getElementsByTagName("toto")).thenReturn(nodeList);
    when(nodeList.getLength()).thenReturn(1);
    Node node = mock(Element.class);
    when(nodeList.item(0)).thenReturn(node);
    when(node.getNodeType()).thenReturn(Node.ELEMENT_NODE);
    Document document = mock(Document.class);
    when(document.getElementsByTagName("toto")).thenReturn(nodeList);
    assertThat(DomUtils.getSingleElementByTagName(document, "toto")).isEqualTo(node);
  }

  @Test
  public void testGetSingleElementByTagName_fromDocument_withNoElement() throws FortifyParseException {
    Element element = mock(Element.class);
    NodeList nodeList = mock(NodeList.class);
    when(element.getElementsByTagName("toto")).thenReturn(nodeList);
    when(nodeList.getLength()).thenReturn(0);
    Document document = mock(Document.class);
    when(document.getElementsByTagName("toto")).thenReturn(nodeList);
    try {
      DomUtils.getSingleElementByTagName(document, "toto");
      fail("FortifyParseException is expected");
    } catch (FortifyParseException e) {
      assertThat(e.getCause()).isNull();
      assertThat(e.getMessage()).contains("'toto' is not found.");
    }
  }

  @Test
  public void testGetAtMostOneElementByTagName_fromDocument() throws FortifyParseException {
    Element element = mock(Element.class);
    NodeList nodeList = mock(NodeList.class);
    when(element.getElementsByTagName("toto")).thenReturn(nodeList);
    when(nodeList.getLength()).thenReturn(1);
    Node node = mock(Element.class);
    when(nodeList.item(0)).thenReturn(node);
    when(node.getNodeType()).thenReturn(Node.ELEMENT_NODE);
    Document document = mock(Document.class);
    when(document.getElementsByTagName("toto")).thenReturn(nodeList);
    assertThat(DomUtils.getAtMostOneElementByTagName(document, "toto")).isEqualTo(node);
  }

  @Test
  public void testGetAtMostOneElementByTagName() throws FortifyParseException {
    Element element = mock(Element.class);
    NodeList nodeList = mock(NodeList.class);
    when(element.getElementsByTagName("toto")).thenReturn(nodeList);
    when(nodeList.getLength()).thenReturn(1);
    Node node = mock(Element.class);
    when(nodeList.item(0)).thenReturn(node);
    when(node.getNodeType()).thenReturn(Node.ELEMENT_NODE);
    assertThat(DomUtils.getAtMostOneElementByTagName(element, "toto")).isEqualTo(node);
  }

  @Test
  public void testGetAtLeastOneElementByTagName() throws FortifyParseException {
    Element element = mock(Element.class);
    NodeList nodeList = mock(NodeList.class);
    when(element.getElementsByTagName("toto")).thenReturn(nodeList);
    when(nodeList.getLength()).thenReturn(1);
    Node node = mock(Element.class);
    when(nodeList.item(0)).thenReturn(node);
    when(node.getNodeType()).thenReturn(Node.ELEMENT_NODE);
    assertThat(DomUtils.getAtLeastOneElementByTagName(element, "toto")).isEqualTo(node);
  }

  @Test
  public void testGetAtLeastOneElementByTagName_wrongNodeType() {
    Element element = mock(Element.class);
    NodeList nodeList = mock(NodeList.class);
    when(element.getElementsByTagName("toto")).thenReturn(nodeList);
    when(nodeList.getLength()).thenReturn(1);
    Node node = mock(Text.class);
    when(nodeList.item(0)).thenReturn(node);
    when(node.getNodeType()).thenReturn(Node.TEXT_NODE);

    try {
      DomUtils.getAtLeastOneElementByTagName(element, "toto");
      fail("FortifyParseException is expected");
    } catch (FortifyParseException e) {
      assertThat(e.getCause()).isNull();
      assertThat(e.getMessage()).contains("Unexpected type " + Node.TEXT_NODE + " for node 'toto'.");
    }
  }

  @Test
  public void testGetAtLeastOneElementByTagName_withNoElement() throws FortifyParseException {
    Element element = mock(Element.class);
    NodeList nodeList = mock(NodeList.class);
    when(element.getElementsByTagName("toto")).thenReturn(nodeList);
    when(nodeList.getLength()).thenReturn(0);
    try {
      DomUtils.getAtLeastOneElementByTagName(element, "toto");
      fail("FortifyParseException is expected");
    } catch (FortifyParseException e) {
      assertThat(e.getCause()).isNull();
      assertThat(e.getMessage()).contains("'toto' is not found.");
    }
  }

  @Test
  public void testGetSingleElementByTagName() throws FortifyParseException {
    Element element = mock(Element.class);
    NodeList nodeList = mock(NodeList.class);
    when(element.getElementsByTagName("toto")).thenReturn(nodeList);
    when(nodeList.getLength()).thenReturn(1);
    Node node = mock(Element.class);
    when(nodeList.item(0)).thenReturn(node);
    when(node.getNodeType()).thenReturn(Node.ELEMENT_NODE);
    assertThat(DomUtils.getSingleElementByTagName(element, "toto")).isEqualTo(node);
  }

  @Test
  public void testGetSingleElementByTagName_withMoreElements() {
    Element element = mock(Element.class);
    NodeList nodeList = mock(NodeList.class);
    when(element.getElementsByTagName("toto")).thenReturn(nodeList);
    when(nodeList.getLength()).thenReturn(2);

    try {
      DomUtils.getSingleElementByTagName(element, "toto");
      fail("FortifyParseException is expected");
    } catch (FortifyParseException e) {
      assertThat(e.getCause()).isNull();
      assertThat(e.getMessage()).contains("Got 2 nodes matching 'toto', but only one is expected.");
    }
  }

  @Test
  public void testGetSingleElementByTagName_wrongNodeType() {
    Element element = mock(Element.class);
    NodeList nodeList = mock(NodeList.class);
    when(element.getElementsByTagName("toto")).thenReturn(nodeList);
    when(nodeList.getLength()).thenReturn(1);
    Node node = mock(Text.class);
    when(nodeList.item(0)).thenReturn(node);
    when(node.getNodeType()).thenReturn(Node.TEXT_NODE);

    try {
      DomUtils.getSingleElementByTagName(element, "toto");
      fail("FortifyParseException is expected");
    } catch (FortifyParseException e) {
      assertThat(e.getCause()).isNull();
      assertThat(e.getMessage()).contains("Unexpected type " + Node.TEXT_NODE + " for node 'toto'.");
    }
  }

  @Test
  public void testGetSingleElementByTagName_withNoElement() throws FortifyParseException {
    Element element = mock(Element.class);
    NodeList nodeList = mock(NodeList.class);
    when(element.getElementsByTagName("toto")).thenReturn(nodeList);
    when(nodeList.getLength()).thenReturn(0);
    try {
      DomUtils.getSingleElementByTagName(element, "toto");
      fail("FortifyParseException is expected");
    } catch (FortifyParseException e) {
      assertThat(e.getCause()).isNull();
      assertThat(e.getMessage()).contains("'toto' is not found.");
    }
  }

}
