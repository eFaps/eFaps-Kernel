/*
 * Copyright 2003-2007 The eFaps Team
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

package org.efaps.admin.program.css;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkin;
import org.efaps.db.Checkout;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

public class CSSCompiler {

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
  private static final UUID TYPE_COMPILED =
      UUID.fromString("0607ea90-b48f-4b76-96f5-67cab19bd7b1");

  public void compile() throws EFapsException {
    removeAllCompiled();

    final Map<String, Long> allcss = readCSS();

    for (final Entry<String, Long> entry : allcss.entrySet()) {

      if (LOG.isInfoEnabled()) {
        LOG.info("compiling " + entry.getKey());
      }

      final Checkout checkout =
          new Checkout(Type.get(TYPE_CSS).getId() + "." + entry.getValue());
      // TODO check character encoding!!
      final BufferedReader in =
          new BufferedReader(new InputStreamReader(checkout.execute()));
      final StringBuffer buffer = new StringBuffer();
      try {
        int pos;
        while ((pos = in.read()) != -1) {
          buffer.append((char) pos);
        }
        int start = 0;
        while ((start = buffer.indexOf("/*")) >= 0) {
          final int end = buffer.indexOf("*/", start + 2);
          if (end >= start + 2)
            buffer.delete(start, end + 2);
        }

        String css = buffer.toString();
        in.close();
        css = css.replaceAll("\\s+", " ");
        css = css.replaceAll("([!{}:;>+\\(\\[,])\\s+", "$1");

        Instance instance;
        final Insert insert = new Insert(Type.get(TYPE_COMPILED));
        insert.add("Name", entry.getKey());
        insert.add("ProgramLink", "" + entry.getValue());
        insert.executeWithoutAccessCheck();
        instance = insert.getInstance();
        insert.close();

        // TODO check character encoding!!
        final ByteArrayInputStream str =
            new ByteArrayInputStream(css.getBytes());
        String name =
            entry.getKey().substring(0, entry.getKey().lastIndexOf("."));

        name =
            name.substring(name.lastIndexOf(".") + 1)
                + entry.getKey().substring(entry.getKey().lastIndexOf("."));

        final Checkin checkin = new Checkin(instance);
        checkin.executeWithoutAccessCheck(name, str, css.getBytes().length);

      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    // query.setExpand(_oid, _expand)

  }

  protected Map<String, Long> readCSS() throws EFapsException {
    final Map<String, Long> ret = new HashMap<String, Long>();
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(Type.get(TYPE_CSS).getName());
    query.addSelect("ID");
    query.addSelect("Name");
    query.executeWithoutAccessCheck();
    while (query.next()) {
      final String name = (String) query.get("Name");
      final Long id = (Long) query.get("ID");
      ret.put(name, id);
    }
    return ret;
  }

  private void removeAllCompiled() throws EFapsException {

    final SearchQuery query = new SearchQuery();

    query.setQueryTypes(Type.get(TYPE_COMPILED).getName());
    query.addSelect("OID");
    query.executeWithoutAccessCheck();
    while (query.next()) {
      final String oid = (String) query.get("OID");
      final Delete del = new Delete(oid);
      del.executeWithoutAccessCheck();
    }
    query.close();
  }

}
