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

public class FortifyLibTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void uncompress() throws Exception {
    File binFile = temp.newFile();
    FileUtils.write(binFile, "compressed content");
    File toFile = temp.newFile();

    FortifyLib lib = new FortifyLib(FakeCryptoUtil.class.getName());
    lib.uncompress(binFile, toFile);
    assertThat(FileUtils.readFileToString(toFile)).isEqualTo("uncompressed content");
  }

  @Test
  public void fail_if_class_not_found() throws Exception {
    File binFile = temp.newFile();
    File toFile = temp.newFile();

    FortifyLib lib = new FortifyLib("does.not.Exist");
    try {
      lib.uncompress(binFile, toFile);
      fail();
    } catch (MessageException e) {
      assertThat(e).hasMessage("fortify-crypto.jar is not available in classpath");
    }
  }

  @Test
  public void fail_to_uncompress() throws Exception {
    File binFile = temp.newFile();
    File toFile = temp.newFile();

    FortifyLib lib = new FortifyLib(FailingCryptoUtil.class.getName());
    try {
      lib.uncompress(binFile, toFile);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("Fail to uncompress " + binFile.getAbsolutePath());
      assertThat(e.getCause()).isInstanceOf(IllegalStateException.class);
    }
  }
}

