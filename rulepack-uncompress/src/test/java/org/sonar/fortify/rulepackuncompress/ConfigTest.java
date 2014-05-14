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

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class ConfigTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void guess_xml_filename() throws Exception {
    File inputDir = temp.newFolder();
    File outputDir = temp.newFolder();

    Config config = new Config(inputDir, outputDir);
    File xmlFile = config.outputXmlFile(new File(inputDir, "rulepack1.bin"));
    assertThat(xmlFile.getName()).isEqualTo("rulepack1.xml");
    assertThat(xmlFile.getParentFile().getCanonicalPath()).isEqualTo(config.outputDir().getCanonicalPath());
  }

  @Test
  public void list_bin_files() throws Exception {
    File inputDir = temp.newFolder();
    FileUtils.touch(new File(inputDir, "rulepack1.bin"));
    FileUtils.touch(new File(inputDir, "rulepack2.BIN"));
    FileUtils.touch(new File(inputDir, "rulepack3.xml"));
    FileUtils.touch(new File(inputDir, "various.txt"));

    Config config = new Config(inputDir, temp.newFolder());
    assertThat(config.inputDir().getCanonicalPath()).isEqualTo(inputDir.getCanonicalPath());

    File[] binFiles = config.inputBinFiles();
    assertThat(binFiles).hasSize(2);
    assertThat(binFiles[0].getName()).isEqualTo("rulepack1.bin");
    assertThat(binFiles[1].getName()).isEqualTo("rulepack2.BIN");
  }

  @Test
  public void fail_if_input_dir_does_not_exist() throws Exception {
    File inputDir = temp.newFolder();
    inputDir.delete();
    try {
      new Config(inputDir, temp.newFolder());
      fail();
    } catch (MessageException e) {
      assertThat(e).hasMessage("Input directory does not exist: " + inputDir.getAbsolutePath());
    }
  }

  @Test
  public void fail_if_output_dir_does_not_exist() throws Exception {
    File outputDir = temp.newFolder();
    outputDir.delete();
    try {
      new Config(temp.newFolder(), outputDir);
      fail();
    } catch (MessageException e) {
      assertThat(e).hasMessage("Output directory does not exist: " + outputDir.getAbsolutePath());
    }
  }
}
