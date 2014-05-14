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

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class CLITest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void fail_if_missing_arguments() throws Exception {
    try {
      new CLI().execute(new FortifyLib(), new String[0]);
      fail();
    } catch (MessageException e) {
      assertThat(e).hasMessage("Missing parameters. Please set the path to the directory containing bin files.");
    }
  }

  @Test
  public void uncompress() throws Exception {
    File dir = temp.newFolder();
    FileUtils.write(new File(dir, "rulepack1.bin"), "compressed content");
    FileUtils.write(new File(dir, "rulepack2.bin"), "compressed content");
    FileUtils.write(new File(dir, "various.txt"), "something else");

    FortifyLib lib = new FortifyLib(FakeCryptoUtil.class.getName());
    new CLI().execute(lib, new String[]{dir.getAbsolutePath()});

    assertThat(FileUtils.listFiles(dir, new String[]{"xml"}, false)).hasSize(2);
    assertThat(new File(dir, "rulepack1.xml")).isFile().exists();
    assertThat(new File(dir, "rulepack2.xml")).isFile().exists();
  }

  @Test
  public void uncompress_in_another_directory() throws Exception {
    File inputDir = temp.newFolder();
    File outputDir = temp.newFolder();
    FileUtils.write(new File(inputDir, "rulepack1.bin"), "compressed content");
    FileUtils.write(new File(inputDir, "rulepack2.bin"), "compressed content");
    FileUtils.write(new File(inputDir, "various.txt"), "something else");

    FortifyLib lib = new FortifyLib(FakeCryptoUtil.class.getName());
    new CLI().execute(lib, new String[]{inputDir.getAbsolutePath(), outputDir.getAbsolutePath()});

    assertThat(FileUtils.listFiles(inputDir, new String[]{"xml"}, false)).isEmpty();
    assertThat(FileUtils.listFiles(outputDir, new String[]{"xml"}, false)).hasSize(2);
    assertThat(new File(outputDir, "rulepack1.xml")).isFile().exists();
    assertThat(new File(outputDir, "rulepack2.xml")).isFile().exists();
  }
}
