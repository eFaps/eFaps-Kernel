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
public class Bundle {

  private File file = null;

  private File gzipFile = null;

  private final long created;

  private String contentType = "text/plain";

  private final List<String> oids;

  private final String key;

  public Bundle(final String _key, final List<String> _oids) {
    this.key = _key;
    this.created = System.currentTimeMillis();
    this.oids = _oids;

  }

  public synchronized InputStream getInputStream(boolean _gziped) {
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
      // TODO Auto-generated catch block
      e.printStackTrace();
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
   * @param contentType
   *                the contentType to set
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * This is the getter method for the instance variable {@link #oids}.
   *
   * @return value of instance variable {@link #oids}
   */
  public List<String> getOids() {
    return this.oids;
  }

  private File setFile(boolean _gziped) {
    final String filename = (_gziped ? this.key + "GZIP" : this.key);
    final File ret = new File(BundleMaker.getTempFolder(), filename);;

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
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return ret;
  }

}
