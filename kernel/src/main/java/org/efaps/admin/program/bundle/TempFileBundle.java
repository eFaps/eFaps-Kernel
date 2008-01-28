/*
 * Copyright 2003-2008 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.admin.program.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.efaps.db.Checkout;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class TempFileBundle implements BundleInterface {

  private static File TMPFOLDER;

  private File file = null;

  private File gzipFile = null;

  private final long created;

  private String contentType = "text/plain";

  private List<String> oids;

  private String key;

  public TempFileBundle() {
    this.created = System.currentTimeMillis();
  }

  public synchronized InputStream getInputStream(final boolean _gziped)
                                                                       throws EFapsException {
    InputStream ret = null;
    try {
      if (_gziped) {
        if (this.gzipFile == null) {
          this.gzipFile = setFile(true);
        }
        ret = new FileInputStream(this.gzipFile);
      } else {
        if (this.file == null) {
          this.file = setFile(false);
        }
        ret = new FileInputStream(this.file);
      }
    } catch (final FileNotFoundException e) {
      throw new EFapsException(this.getClass(), "getInputStream", e);
    }
    return ret;
  }

  public long getCreationTime() {
    return this.created;
  }

  /**
   * This is the getter method for the instance variable {@link #contentType}.
   *
   * @return value of instance variable {@link #contentType}
   */
  public String getContentType() {
    return this.contentType;
  }

  /**
   * This is the setter method for the instance variable {@link #contentType}.
   *
   * @param _contentType
   *                the contentType to set
   */
  public void setContentType(final String _contentType) {
    this.contentType = _contentType;
  }

  /**
   * This is the getter method for the instance variable {@link #oids}.
   *
   * @return value of instance variable {@link #oids}
   */
  public List<String> getOids() {
    return this.oids;
  }

  private File setFile(final boolean _gziped) throws EFapsException {
    final String filename = (_gziped ? this.key + "GZIP" : this.key);
    final File ret = new File(getTempFolder(), filename);

    try {
      final FileOutputStream out = new FileOutputStream(ret);
      final byte[] buffer = new byte[1024];
      int bytesRead;
      if (_gziped) {
        final GZIPOutputStream zout = new GZIPOutputStream(out);
        for (final String oid : this.oids) {
          final Checkout checkout = new Checkout(oid);
          final InputStream bis = checkout.execute();
          while ((bytesRead = bis.read(buffer)) != -1) {
            zout.write(buffer, 0, bytesRead);
          }
        }
        zout.close();
      } else {
        for (final String oid : this.oids) {
          final Checkout checkout = new Checkout(oid);
          final InputStream bis = checkout.execute();
          while ((bytesRead = bis.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
          }
        }
      }
      out.close();
    } catch (final IOException e) {
      throw new EFapsException(this.getClass(), "setFile", e, filename);
    }
    return ret;
  }

  public static File getTempFolder() throws EFapsException {
    try {
      if (TMPFOLDER == null) {
        final File tmp = File.createTempFile("eFapsTemp", null).getParentFile();
        TMPFOLDER = new File(tmp.getAbsolutePath() + "/eFapsTemp");
        TMPFOLDER.mkdir();
      }
    } catch (final IOException e) {
      throw new EFapsException(TempFileBundle.class, "getTempFolder", e);
    }
    return TMPFOLDER;
  }

  public void setKey(final String _key, final List<String> _oids) {
    this.key = _key;
    this.oids = _oids;
  }
}
