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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.fortify.base.FortifyParseException;
import org.sonar.fortify.base.handler.StartHandler;
import org.sonar.fortify.rule.element.RulePack;
import org.sonar.fortify.rule.handler.RulePackHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.IOException;
import java.io.InputStream;

public class RulePackSAXParser {
  private static final Logger LOG = LoggerFactory.getLogger(RulePackSAXParser.class);

  RulePack parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException, FortifyParseException {
    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

    RulePackHandler rulePackhandler = new RulePackHandler();
    StartHandler<RulePack> handler = new StartHandler<RulePack>(rulePackhandler);
    parser.parse(inputStream, handler);
    RulePack rulePack = handler.getResult();
    if (rulePack == null) {
      throw new FortifyParseException("Malformed RulePack");
    }
    RulePackSAXParser.LOG.debug(rulePack.getName() + " - " + rulePack.getLanguage() + " - " + rulePack.getRules().size());
    return rulePack;
  }
}
