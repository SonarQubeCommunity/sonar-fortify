/*
 * SonarQube Fortify Plugin
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
package org.sonar.plugins.fortify.base;

import org.apache.commons.io.FilenameUtils;
import org.sonar.api.batch.fs.FileSystem;

import javax.annotation.CheckForNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class FortifyReportFile {
  private final FortifySensorConfiguration configuration;
  private final FileSystem fileSystem;

  public FortifyReportFile(FortifySensorConfiguration configuration, FileSystem fileSystem) {
    this.configuration = configuration;
    this.fileSystem = fileSystem;
  }

  @CheckForNull
  private File getReportFromProperty() {
    String path = this.configuration.getReportPath();
    if (path != null && path.length() > 0) {
      File report = new File(this.fileSystem.baseDir(), path);
      if (!reportExists(report)) {
        report = new File(path);
      }
      return report;
    }
    return null;
  }

  private boolean reportExists(File report) {
    return report != null && report.exists() && report.isFile();
  }

  private InputStream getInputStreamFromFprFile(File file) throws IOException {
    final ZipFile fprFile = new ZipFile(file);
    try {
      final InputStream reportStream = fprFile.getInputStream(fprFile.getEntry(FortifyConstants.AUDIT_FVDL_FILE));
      return new InputStream() {
        @Override
        public int read() throws IOException {
          return reportStream.read();
        }

        @Override
        public void close() throws IOException {
          try {
            reportStream.close();
          } finally {
            fprFile.close();
          }
        }
      };
    } catch (IOException e) {
      fprFile.close();
      throw e;
    }
  }

  private InputStream getInputStreamFromFVDLFile(File file) throws FileNotFoundException {
    return new FileInputStream(file);
  }

  public InputStream getInputStream() throws IOException {
    File file = getReportFromProperty();
    if (file == null) {
      throw new FileNotFoundException();
    }
    String fileExtension = FilenameUtils.getExtension(file.getName());
    if ("fpr".equalsIgnoreCase(fileExtension)) {
      return getInputStreamFromFprFile(file);
    } else {
      return getInputStreamFromFVDLFile(file);
    }
  }

  public boolean exist() {
    File report = getReportFromProperty();
    return report != null && report.exists() && report.isFile();
  }
}
