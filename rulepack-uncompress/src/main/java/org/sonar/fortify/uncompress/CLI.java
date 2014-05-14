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
package org.sonar.fortify.uncompress;

import java.io.File;

public class CLI {

  public static void main(String[] args) {
    new CLI().execute(new FortifyLib(), args);
  }

  void execute(FortifyLib fortifyLib, String[] args) {
    if (args.length != 2) {
      throw new MessageException("Missing parameters. Please set paths to input and output directories.");
    }
    Config config = new Config(new File(args[0]), new File(args[1]));
    System.out.println("Input dir: " + config.inputDir().getAbsolutePath());
    System.out.println("Output dir: " + config.outputDir().getAbsolutePath());

    for (File binFile : config.inputBinFiles()) {
      File xmlFile = config.outputXmlFile(binFile);
      System.out.println("Uncompress " + binFile.getName() + " to " + xmlFile.getName());
      fortifyLib.uncompress(binFile, xmlFile);
    }
  }
}
