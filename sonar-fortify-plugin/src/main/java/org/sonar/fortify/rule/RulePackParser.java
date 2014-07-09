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

import com.google.common.io.Closeables;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.fortify.base.FortifyConstants;
import org.sonar.fortify.base.FortifyParseException;
import org.sonar.fortify.rule.element.RulePack;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RulePackParser {

  private static final Logger LOG = LoggerFactory.getLogger(RulePackParser.class);

  private final Settings settings;

  public RulePackParser(Settings settings) {
    this.settings = settings;
  }

  public List<RulePack> parse() {
    List<File> files = new ArrayList<File>();
    for (String location : this.settings.getStringArray(FortifyConstants.RULEPACK_PATHS_PROPERTY)) {
      File file = new File(location);
      if (file.isDirectory()) {
        files.addAll(FileUtils.listFiles(file, new String[] {"xml"}, false));
      } else if (file.exists()) {
        files.add(file);
      } else {
        LOG.warn("Ignore rulepack location: \"{}\", file is not found.", file);
      }
    }

    List<RulePack> rulePacks = new ArrayList<RulePack>();
    for (File file : files) {
      InputStream stream = null;
      try {
        stream = new FileInputStream(file);
        rulePacks.add(new RulePackSAXParser().parse(stream));
      } catch (IOException e) {
        LOG.error("Unexpected error during the parse of " + file + ".", e);
      } catch (SAXException e) {
        LOG.error("Unexpected error during the parse of " + file + ".", e);
      } catch (ParserConfigurationException e) {
        LOG.error("Unexpected error during the parse of " + file + ".", e);
      } catch (FortifyParseException e) {
        LOG.error("Unexpected error during the parse of " + file + ".", e);
      } finally {
        Closeables.closeQuietly(stream);
      }
    }
    return rulePacks;
  }
}
