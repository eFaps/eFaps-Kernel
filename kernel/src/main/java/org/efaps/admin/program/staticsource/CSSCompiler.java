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
import org.efaps.update.program.CSSUpdate;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class CSSCompiler extends AbstractSourceCompiler {

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
      // TODO check character encoding!!UTF-8
      final BufferedReader in =
          new BufferedReader(new InputStreamReader(checkout.execute()));

      final StringBuffer buffer = new StringBuffer();

      String thisLine;
      while ((thisLine = in.readLine()) != null) {
        if (!thisLine.contains(CSSUpdate.ANNOTATION_VERSION)
            && !thisLine.contains(CSSUpdate.ANNOTATION_EXTENDS)) {
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
    return new OneCSS(_name, _oid, _id);
  }

  protected class OneCSS extends OneSource {

    public OneCSS(final String _name, final String _oid, final long _id) {
      super(_name, _oid, _id);
    }

  }

}
