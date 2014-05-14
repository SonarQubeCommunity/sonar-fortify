/*
 * Fortify Rulepack Uncompress
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
package org.sonar.fortify.rulepackuncompress;

import java.io.File;
import java.io.FilenameFilter;

class Config {

  private final File inputDir, outputDir;

  Config(File inputDir, File outputDir) {
    if (!inputDir.isDirectory() || !inputDir.exists()) {
      throw new MessageException("Input directory does not exist: " + inputDir.getAbsolutePath());
    }
    this.inputDir = inputDir;

    if (!outputDir.isDirectory() || !outputDir.exists()) {
      throw new MessageException("Output directory does not exist: " + outputDir.getAbsolutePath());
    }
    this.outputDir = outputDir;
  }

  File inputDir() {
    return inputDir;
  }

  File outputDir() {
    return outputDir;
  }

  File[] inputBinFiles() {
    return inputDir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".bin");
      }
    });
  }

  File outputXmlFile(File binFile) {
    String xmlFilename = binFile.getName().substring(0, binFile.getName().length() - 4) + ".xml";
    return new File(outputDir, xmlFilename);
  }
}
