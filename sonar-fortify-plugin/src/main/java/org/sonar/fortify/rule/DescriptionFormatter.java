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
package org.sonar.fortify.rule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class DescriptionFormatter {
  private enum State {
    TEXT, CODE, IGNORE
  }

  private Queue<State> states;
  private Queue<String> tags;
  private State currentState;
  private String currentTag;
  private String currentLine;
  private StringBuilder builder;
  private BufferedReader reader;

  private void switchState(String tag, State newState, String newTag) {
    this.currentLine = this.currentLine.substring(tag.length());
    this.tags.add(this.currentTag);
    this.states.add(this.currentState);
    this.currentState = newState;
    this.currentTag = newTag;
  }

  private void handleCode() throws IOException {
    int i = this.currentLine.indexOf("</pre>");
    if (i >= 0) {
      String code = this.currentLine.substring(0, i + 6);
      this.currentLine = this.currentLine.substring(i + 6);
      this.builder.append(code).append("\n");
      this.currentState = this.states.remove();
      this.currentTag = this.tags.remove();
    } else {
      this.builder.append(this.currentLine).append("\n");
      this.currentLine = this.reader.readLine();
    }
  }

  private void handleIgnore() throws IOException {
    int i = this.currentLine.indexOf('<');
    if (i >= 0) {
      this.currentLine = this.currentLine.substring(i);
      if (this.currentLine.startsWith("<AltParagraph>")) {
        switchState("<AltParagraph>", State.TEXT, "</AltParagraph>");
      } else if (this.currentLine.startsWith(this.currentTag)) {
        this.currentLine = this.currentLine.substring(this.currentTag.length());
        this.currentState = this.states.remove();
        this.currentTag = this.tags.remove();
      } else if (this.currentLine.length() > 0) {
        this.currentLine = this.currentLine.substring(1);
      } else {
        this.currentLine = this.reader.readLine();
      }
    } else {
      this.currentLine = this.reader.readLine();
    }
  }

  private void handleTextTags(int i) throws IOException {
    this.builder.append(this.currentLine.substring(0, i));
    this.currentLine = this.currentLine.substring(i);
    if (this.currentLine.startsWith("<pre>")) {
      this.builder.append("<pre>");
      switchState("<pre>", State.CODE, "</pre>");
    } else if (this.currentLine.startsWith("<Paragraph>")) {
      switchState("<Paragraph>", State.IGNORE, "</Paragraph>");
    } else if (this.currentLine.startsWith("<IfNotDef var=\"ConditionalDescriptions\">")) {
      switchState("<IfNotDef var=\"ConditionalDescriptions\">", State.TEXT, "</IfNotDef>");
    } else if (this.currentLine.startsWith("<IfDef var=\"ConditionalDescriptions\">")) {
      switchState("<IfDef var=\"ConditionalDescriptions\">", State.IGNORE, "</IfDef>");
    } else if (this.currentTag != null && this.currentLine.startsWith(this.currentTag)) {
      this.currentLine = this.currentLine.substring(this.currentTag.length());
      this.currentState = this.states.remove();
      this.currentTag = this.tags.remove();
    } else if (this.currentLine.length() > 0) {
      this.builder.append('<');
      this.currentLine = this.currentLine.substring(1);
    } else {
      this.builder.append("</p><p>");
      this.currentLine = this.reader.readLine();
    }
  }

  private void handleText() throws IOException {
    int i = this.currentLine.indexOf('<');
    if (i >= 0) {
      handleTextTags(i);
    } else {
      this.builder.append(this.currentLine).append("</p>");
      this.currentLine = this.reader.readLine();
      while (this.currentLine != null && this.currentLine.isEmpty()) {
        this.currentLine = this.reader.readLine();
      }
      if (this.currentLine != null) {
        this.builder.append("<p>");
      }
    }
  }

  protected String format(BufferedReader input) throws IOException {
    this.reader = input;
    this.states = Collections.asLifoQueue(new LinkedList<State>());
    this.tags = Collections.asLifoQueue(new LinkedList<String>());
    this.currentState = State.TEXT;
    this.currentTag = null;
    this.builder = new StringBuilder().append("<p>");

    this.currentLine = this.reader.readLine();
    while (this.currentLine != null) {
      switch (this.currentState) {
        case CODE:
          handleCode();
          break;
        case IGNORE:
          handleIgnore();
          break;
        case TEXT:
          handleText();
          break;
        default:
          break;
      }
    }
    this.reader.close();

    return this.builder.toString();
  }

  public String format(String input) {
    try {
      return format(new BufferedReader(new StringReader(input)));
    } catch (IOException e) {
      return input;
    }
  }
}
