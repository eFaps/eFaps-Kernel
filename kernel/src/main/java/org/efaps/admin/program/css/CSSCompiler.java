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

package org.efaps.admin.program.css;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkin;
import org.efaps.db.Checkout;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.update.program.CSSUpdate;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
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
  public static final UUID TYPE_COMPILED =
      UUID.fromString("0607ea90-b48f-4b76-96f5-67cab19bd7b1");

  public void compile() throws EFapsException {
    removeAllCompiled();

    final List<OneCSS> allcss = readCSS();

    for (final OneCSS onecss : allcss) {

      if (LOG.isInfoEnabled()) {
        LOG.info("compiling " + onecss.getName());
      }

      final List<String> supers = getSuper(onecss.getOid());
      String ttl = "";
      while (!supers.isEmpty()) {
        ttl += getCompiledString(supers.get(supers.size() - 1));
        supers.remove(supers.size() - 1);
      }
      ttl += getCompiledString(onecss.getOid());

      Instance instance;
      final Insert insert = new Insert(Type.get(TYPE_COMPILED));
      insert.add("Name", onecss.getName());
      insert.add("ProgramLink", "" + onecss.getId());
      insert.executeWithoutAccessCheck();
      instance = insert.getInstance();
      insert.close();

      // TODO check character encoding!!
      final ByteArrayInputStream str = new ByteArrayInputStream(ttl.getBytes());
      String name =
          onecss.getName().substring(0, onecss.getName().lastIndexOf("."));

      name =
          name.substring(name.lastIndexOf(".") + 1)
              + onecss.getName().substring(onecss.getName().lastIndexOf("."));

      final Checkin checkin = new Checkin(instance);
      checkin.executeWithoutAccessCheck(name, str, ttl.getBytes().length);
    }
    // query.setExpand(_oid, _expand)

  }

  protected List<OneCSS> readCSS() throws EFapsException {
    final List<OneCSS> ret = new ArrayList<OneCSS>();
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(Type.get(TYPE_CSS).getName());
    query.addSelect("ID");
    query.addSelect("OID");
    query.addSelect("Name");
    query.executeWithoutAccessCheck();
    while (query.next()) {
      final String name = (String) query.get("Name");
      final String oid = (String) query.get("OID");
      final Long id = (Long) query.get("ID");
      ret.add(new OneCSS(name, oid, id));
    }
    return ret;
  }

  private String getCompiledString(final String _oid) {
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

  private List<String> getSuper(final String _oid) throws EFapsException {
    final List<String> ret = new ArrayList<String>();
    final SearchQuery query = new SearchQuery();
    query.setExpand(_oid, "Admin_Program_CSS2CSS\\From");
    query.addSelect("To");
    query.execute();
    if (query.next()) {
      final String tooid = Type.get(TYPE_CSS).getId() + "." + query.get("To");
      ret.add(tooid);
      ret.addAll(getSuper(tooid));
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

  private class OneCSS {

    private final String name;

    private final String oid;

    private final long id;

    public OneCSS(final String _name, final String _oid, final long _id) {
      this.name = _name;
      this.oid = _oid;
      this.id = _id;
    }

    /**
     * This is the getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    public String getName() {
      return this.name;
    }

    /**
     * This is the getter method for the instance variable {@link #oid}.
     *
     * @return value of instance variable {@link #oid}
     */
    public String getOid() {
      return this.oid;
    }

    /**
     * This is the getter method for the instance variable {@link #id}.
     *
     * @return value of instance variable {@link #id}
     */
    public long getId() {
      return this.id;
    }

  }

}
