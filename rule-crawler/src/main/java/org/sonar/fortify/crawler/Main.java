/*
 * Crawler for HP Fortify Web site to extract rule description
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
package org.sonar.fortify.crawler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.fortify.base.FortifyConstants;

import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.Normalizer;
import java.util.*;

public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  private static final String BASE_URL = "http://www.hpenterprisesecurity.com/vulncat/en/vulncat/";

  private static final Map<String, String> FORTIFY_TO_SQ = ImmutableMap.<String, String>builder()
    .put("ABAP", "abap")
    .put("ActionScript", "flex")
    .put("COBOL", "cobol")
    .put("C/C++", "c")
    .put("C#/VB.NET/ASP.NET", "cs")
    .put("HTML", "web")
    .put("Java/JSP", "java")
    .put("Javascript", "js")
    .put("Objective-C", "objc")
    .put("PHP", "php")
    .put("Python", "py")
    .put("PLSQL/TSQL", "plsql")
    .put("VisualBasic/VBScript/ASP", "vb")
    .put("Webservices", "xml")
    .put("XML", "xml")
    .build();

  public static void main(String... args) {
    try {
      new Main(new File(".")).extractRules();
    } catch (Exception e) {
      LOG.error("Unable to get Fortify rules", e);
      System.exit(1);
    }
  }

  private String currentLanguage;
  private int currentLanguageId;
  private String currentKingdom;
  private int currentKingdomId;
  private Map<String, List<Rule>> rulesByLanguage = new HashMap<>();
  private final File outputBaseDir;

  public Main(File output) {
    this.outputBaseDir = output;
  }

  @VisibleForTesting
  void extractRules() throws IOException, ScriptException {
    String pageSrc = getPageSource();

    String script = extractDTreeJSCode(pageSrc);

    executeJSCode(script);

    writeRulesXmlFiles();
  }

  private void writeRulesXmlFiles() throws IOException {
    for (String language : rulesByLanguage.keySet()) {
      String sonarLanguage = FORTIFY_TO_SQ.get(language);
      if (sonarLanguage == null) {
        LOG.warn("No SonarQube language equivalent to Fortify {}. Rules will not be imported.", language);
        continue;
      }
      File file = new File(outputBaseDir, "src/main/resources/rules/rules-" + sonarLanguage + ".xml");
      file.getParentFile().mkdirs();
      try (OutputStream out = new FileOutputStream(file);
        Writer writer = new OutputStreamWriter(out, Charsets.UTF_8)) {
        writer.append("<rules>").append("\n");
        writer.append("  <!-- see names and descriptions in org/sonar/l10n/ -->").append("\n");
        for (Rule rule : rulesByLanguage.get(language)) {
          writer.append("  <rule>").append("\n");
          writer.append("    <key>" + rule.getKey() + "</key>").append("\n");
          // Intenal key is not used but will keep track of unmodified labels
          writer.append("    <internalKey>" + rule.getInternalKey() + "</internalKey>").append("\n");
          writer.append("    <name>" + rule.getName() + "</name>").append("\n");
          writer.append("    <description><![CDATA[" + rule.getHtmlDescription() + "]]></description>").append("\n");
          writer.append("    <severity>MAJOR</severity>").append("\n");
          writer.append("    <tag>" + slugifyForTags(rule.getKingdom()) + "</tag>").append("\n");
          writer.append("  </rule>").append("\n");
        }
        writer.append("</rules>").append("\n");
      }
    }
  }

  private void executeJSCode(String pageSrc) throws ScriptException {
    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName("JavaScript");
    engine.put("d", this);
    engine.eval(pageSrc);
  }

  private String extractDTreeJSCode(String pageSrc) {
    String startScript = "d = new dTree('d');";
    String scriptCode = pageSrc.substring(pageSrc.indexOf(startScript) + startScript.length());
    scriptCode = scriptCode.substring(0, scriptCode.indexOf("document.write(d);"));
    return scriptCode;
  }

  private String getPageSource() throws IOException {
    URL url = new URL(BASE_URL + "all.html");
    return download(url);
  }

  @VisibleForTesting
  String download(URL url) throws IOException {
    LOG.info("Download: " + url);
    URLConnection openConnection = url.openConnection();
    return IOUtils.toString(openConnection.getInputStream(), openConnection.getContentEncoding());
  }

  // Called by JavaScript code
  public void add(int id, int parent, String label) {
    this.add(id, parent, label, null);
  }

  public void add(int id, int parent, String label, @Nullable String url) {
    if (parent == -1) {
      // Root node
    } else if (parent == 0) {
      // Language
      this.currentLanguage = label;
      this.currentLanguageId = id;
      this.rulesByLanguage.put(label, new ArrayList<Rule>());
    } else if (parent == currentLanguageId) {
      // Kingdom
      this.currentKingdom = label;
      this.currentKingdomId = id;
    } else if (parent == currentKingdomId) {
      // Rule
      this.rulesByLanguage.get(currentLanguage).add(new Rule(currentKingdom, label, url));
    } else {
      throw new IllegalStateException("Unexpected case. Crawler need an update.");
    }
  }

  public class Rule {
    private final String kingdom;
    private final String category;
    private final String subcategory;
    private final String name;
    private final String url;

    public Rule(String kingdom, String name, String url) {
      this.kingdom = kingdom.trim();
      this.name = name;
      int indexOfColon = name.indexOf(":");
      if (indexOfColon >= 0) {
        this.category = name.substring(0, indexOfColon).trim();
        this.subcategory = name.substring(indexOfColon + 1, name.length()).trim();
      } else {
        this.category = name.trim();
        this.subcategory = null;
      }
      this.url = url;
    }

    public String getKingdom() {
      return this.kingdom;
    }

    public String getHtmlDescription() throws IOException {
      URL urlDescription = new URL(BASE_URL + url);
      String description = download(urlDescription);
      description = description.substring(description.indexOf("</h1>") + "</h1>".length());
      description = description.substring(0, description.indexOf("<div id=\"theFooter\">"));
      return clean(description);
    }

    private String clean(String description) {
      return description.trim();
    }

    public String getName() {
      return name;
    }

    public String getKey() {
      return FortifyConstants.fortifySQRuleKey(kingdom, category, subcategory);
    }

    public String getInternalKey() {
      StringBuilder sb = new StringBuilder();
      sb.append(kingdom).append("/").append(category);
      if (subcategory != null) {
        sb.append("/").append(subcategory);
      }
      return sb.toString();
    }
  }

  private static String slugifyForTags(String s) {
    return Normalizer.normalize(s, Normalizer.Form.NFD)
      .replaceAll("[^\\p{ASCII}]", "")
      .replaceAll("[^\\w+]", "-")
      .replaceAll("\\s+", "-")
      .replaceAll("_", "-")
      .replaceAll("[-]+", "-")
      .replaceAll("^-", "")
      .replaceAll("-$", "").toLowerCase(Locale.ENGLISH);
  }

}
