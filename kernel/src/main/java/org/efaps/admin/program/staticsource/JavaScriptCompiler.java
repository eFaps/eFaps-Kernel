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

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

import org.efaps.db.Checkout;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class JavaScriptCompiler extends AbstractSourceCompiler {

  private static final Logger LOG =
      LoggerFactory.getLogger(JavaScriptCompiler.class);

  /**
   * UUID of the CSS type.
   */
  private static final UUID TYPE_JAVASCRIPT =
      UUID.fromString("1c9ce325-7e4f-401f-aeb8-74e2e0c9e224");

  /**
   * UUID of the CompiledCSS type.
   */
  public static final UUID TYPE_COMPILED =
      UUID.fromString("5ed4d346-c82e-4f4e-b52e-a4d5afa0e284");

  private static final UUID TYPE_JAVASCRIPT2JAVASCRIPT =
      UUID.fromString("2d24e861-580c-43ad-a59c-3266021ea190");

  @Override
  public UUID getUUID4Type() {
    return TYPE_JAVASCRIPT;
  }

  @Override
  public UUID getUUID4TypeCompiled() {
    return TYPE_COMPILED;
  }

  @Override
  public UUID getUUID4Type2Type() {
    return TYPE_JAVASCRIPT2JAVASCRIPT;
  }

  @Override
  protected String getCompiledString(final String _oid) {
    final Checkout checkout = new Checkout(_oid);
    BufferedReader in;
    ByteArrayOutputStream byteout = null;
    try {
      in = new BufferedReader(new InputStreamReader(checkout.execute()));

      final JavaScriptCompressor compressor =
          new JavaScriptCompressor(in, new ErrorReporter() {

            public void error(String arg0, String arg1, int arg2, String arg3,
                              int arg4) {
              LOG.error(arg0);
            }

            public EvaluatorException runtimeError(String arg0, String arg1,
                                                   int arg2, String arg3,
                                                   int arg4) {
              return null;
            }

            public void warning(String arg0, String arg1, int arg2,
                                String arg3, int arg4) {
              // TODO use a systemproperty to determine if warning or not
              LOG.warn(arg0);
            }
          });

      in.close();
      checkout.close();
      byteout = new ByteArrayOutputStream();
      final OutputStreamWriter out = new OutputStreamWriter(byteout);
      compressor.compress(out, 2000, false, true, false, true);
      out.flush();
    } catch (final EFapsException e) {
      LOG.error("error during checkout of Instance with oid:" + _oid, e);
      e.printStackTrace();
    } catch (final EvaluatorException e) {
      LOG.error(
          "error during the evaluation of the JavaScript of Instance with oid:"
              + _oid, e);
    } catch (final IOException e) {
      LOG.error("error during reqding of the Inputstram of Instance with oid:"
          + _oid, e);
    }
    String ret = byteout.toString();
    ret += "\n";
    return ret;
  }

  @Override
  public AbstractSource getNewSource(String _name, String _oid, long _id) {
    return new OneJavaScript(_name, _oid, _id);
  }

  /**
   * TODO description
   *
   * @author jmox
   * @version $Id$
   *
   */
  protected class OneJavaScript extends AbstractSource {

    public OneJavaScript(final String _name, final String _oid, final long _id) {
      super(_name, _oid, _id);
    }

  }
}
