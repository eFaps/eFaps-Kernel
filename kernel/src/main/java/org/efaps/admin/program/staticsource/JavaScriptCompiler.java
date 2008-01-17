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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import org.efaps.db.Checkout;
import org.efaps.update.program.JavaScriptUpdate;
import org.efaps.util.EFapsException;

public class JavaScriptCompiler extends AbstractSourceCompiler {

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
    String ret = "";
    try {
      final Checkout checkout = new Checkout(_oid);
      // TODO check character encoding!!UTF-8
      final BufferedReader in =
          new BufferedReader(new InputStreamReader(checkout.execute()));

      final StringBuffer buffer = new StringBuffer();

      String thisLine;
      while ((thisLine = in.readLine()) != null) {
        if (!thisLine.contains(JavaScriptUpdate.ANNOTATION_VERSION)
            && !thisLine.contains(JavaScriptUpdate.ANNOTATION_EXTENDS)) {
          buffer.append(thisLine);
        }
      }

      int start = 0;
      while ((start = buffer.indexOf("/*")) >= 0) {
        final int end = buffer.indexOf("*/", start + 2);
        if (end >= start + 2)
          buffer.delete(start, end + 2);
      }

      ret = buffer.toString();
      in.close();
      checkout.close();
      ret = ret.replaceAll("\\s+", " ");
      ret = ret.replaceAll("([!{}:;>+\\(\\[,])\\s+", "$1");
      ret += "\n";

    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return ret;

  }

  @Override
  public OneSource getNewOneSource(String _name, String _oid, long _id) {
    return new OneJavaScript(_name, _oid, _id);
  }

  protected class OneJavaScript extends OneSource {

    public OneJavaScript(final String _name, final String _oid, final long _id) {
      super(_name, _oid, _id);
    }

  }
}
