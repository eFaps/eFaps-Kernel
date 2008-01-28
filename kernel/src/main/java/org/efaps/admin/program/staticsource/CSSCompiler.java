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

package org.efaps.admin.program.staticsource;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.platform.yui.compressor.CssCompressor;

import org.efaps.db.Checkout;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class CSSCompiler extends AbstractSourceCompiler {

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CSSCompiler.class);

  /**
   * UUID of the CSS type.
   */
  private static final UUID TYPE_CSS =
      UUID.fromString("f5a5bcf6-3cc7-4530-a5a0-7808a392381b");

  /**
   * UUID of the CompiledCSS type.
   */
  public static final UUID TYPE_COMPILED =
      UUID.fromString("0607ea90-b48f-4b76-96f5-67cab19bd7b1");

  private static final UUID TYPE_CSS2CSS =
      UUID.fromString("9d69ef63-b248-4f50-9130-5f33d64d81f0");

  @Override
  public UUID getUUID4Type() {
    return TYPE_CSS;
  }

  @Override
  public UUID getUUID4TypeCompiled() {
    return TYPE_COMPILED;
  }

  @Override
  public UUID getUUID4Type2Type() {
    return TYPE_CSS2CSS;
  }

  @Override
  protected String getCompiledString(final String _oid) {
    String ret = "";
    try {
      final Checkout checkout = new Checkout(_oid);
      final BufferedReader in =
          new BufferedReader(new InputStreamReader(checkout.execute(), "UTF-8"));

      final CssCompressor compressor = new CssCompressor(in);
      in.close();
      checkout.close();
      final ByteArrayOutputStream byteout = new ByteArrayOutputStream();
      final OutputStreamWriter out = new OutputStreamWriter(byteout);
      compressor.compress(out, 2000);
      out.flush();

      ret = byteout.toString();
      ret += "\n";

    } catch (final EFapsException e) {
      LOG.error("error during checkout of Instance with oid:" + _oid, e);
      e.printStackTrace();
    } catch (final IOException e) {
      LOG.error("error during reqding of the Inputstram of Instance with oid:"
          + _oid, e);
    }
    return ret;

  }

  @Override
  public AbstractSource getNewSource(String _name, String _oid, long _id) {
    return new OneCSS(_name, _oid, _id);
  }

  /**
   * TODO description
   *
   * @author jmox
   * @version $Id$
   */
  protected class OneCSS extends AbstractSource {

    public OneCSS(final String _name, final String _oid, final long _id) {
      super(_name, _oid, _id);
    }

  }

}
