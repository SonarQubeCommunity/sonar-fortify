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

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class FortifyLib {

  private final String uncompressClassname;

  /**
   * Visible for testing
   */
  FortifyLib(String classname) {
    this.uncompressClassname = classname;
  }

  FortifyLib() {
    this("com.fortify.util.CryptoUtil");
  }

  void uncompress(File binFile, File toFile) {
    InputStream binInput = null;
    OutputStream xmlOutput = null;
    try {
      binInput = new FileInputStream(binFile);
      Method uncompressMethod = introspectMethod();
      InputStream uncompressedInput = (InputStream) uncompressMethod.invoke(null, binInput, null);
      xmlOutput = new FileOutputStream(toFile);
      copy(uncompressedInput, xmlOutput);

    } catch (ClassNotFoundException e) {
      throw new MessageException("fortify-crypto.jar is not available in classpath");

    } catch (InvocationTargetException e) {
      throw new IllegalStateException("Fail to uncompress " + binFile.getAbsolutePath(), e.getTargetException());

    } catch (Exception e) {
      throw new IllegalStateException("Fail to uncompress " + binFile.getAbsolutePath(), e);

    } finally {
      close(binInput);
      close(xmlOutput);
    }
  }

  private Method introspectMethod() throws NoSuchMethodException, ClassNotFoundException {
    Class utilClass = Class.forName(uncompressClassname);
    return utilClass.getMethod("decryptCompressed", InputStream.class, String.class);
  }

  private void copy(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[1024];
    int length;
    while ((length = input.read(buffer)) >= 0) {
      output.write(buffer, 0, length);
    }
  }

  private void close(@Nullable Closeable c) {
    if (c != null) {
      try {
        c.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
