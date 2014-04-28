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
package org.sonar.fortify.fvdl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.utils.MessageException;
import org.sonar.fortify.base.FortifyConstants;

import javax.annotation.CheckForNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

class FortifyReportFile {
  private final FortifySensorConfiguration configuration;
  private final FileSystem fileSystem;

  FortifyReportFile(FortifySensorConfiguration configuration, FileSystem fileSystem) {
    this.configuration = configuration;
    this.fileSystem = fileSystem;
  }

  /**
   * Report file, null if the property is not set.
   * @throws org.sonar.api.utils.MessageException if the property relates to a directory or a non-existing file.
   */
  @CheckForNull
  private File getReportFromProperty() {
    String path = this.configuration.getReportPath();
    if (StringUtils.isNotBlank(path)) {
      File report = new File(path);
      if (!report.isAbsolute()) {
        report = new File(this.fileSystem.baseDir(), path);
      }
      if (report.exists() && report.isFile()) {
        return report;
      }
      throw MessageException.of("Fortify report does not exist. Please check property " +
          FortifyConstants.REPORT_PATH_PROPERTY + ": " + path);
    }
    return null;
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

  InputStream getInputStream() throws IOException {
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

  boolean exist() {
    File report = getReportFromProperty();
    return report != null;
  }
}
