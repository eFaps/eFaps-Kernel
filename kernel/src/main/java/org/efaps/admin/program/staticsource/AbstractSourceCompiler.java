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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkin;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractSourceCompiler {

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG =
      LoggerFactory.getLogger(AbstractSourceCompiler.class);

  public static void compileAll() throws EFapsException {
    (new CSSCompiler()).compile();
    (new JavaScriptCompiler()).compile();
  }

  public void compile() throws EFapsException {
    removeAllCompiled();

    final List<OneSource> allsource = readSources();

    for (final OneSource onecss : allsource) {

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
      final Insert insert = new Insert(Type.get(getUUID4TypeCompiled()));
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

  public abstract UUID getUUID4TypeCompiled();

  public abstract UUID getUUID4Type();

  public abstract UUID getUUID4Type2Type();

  public abstract OneSource getNewOneSource(final String _name,
                                            final String _oid, final long _id);

  protected abstract String getCompiledString(final String _oid);

  protected void removeAllCompiled() throws EFapsException {

    final SearchQuery query = new SearchQuery();

    query.setQueryTypes(Type.get(getUUID4TypeCompiled()).getName());
    query.addSelect("OID");
    query.executeWithoutAccessCheck();
    while (query.next()) {
      final String oid = (String) query.get("OID");
      final Delete del = new Delete(oid);
      del.executeWithoutAccessCheck();
    }
    query.close();
  }

  protected List<OneSource> readSources() throws EFapsException {
    final List<OneSource> ret = new ArrayList<OneSource>();
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(Type.get(getUUID4Type()).getName());
    query.addSelect("ID");
    query.addSelect("OID");
    query.addSelect("Name");
    query.executeWithoutAccessCheck();
    while (query.next()) {
      final String name = (String) query.get("Name");
      final String oid = (String) query.get("OID");
      final Long id = (Long) query.get("ID");
      ret.add(getNewOneSource(name, oid, id));
    }
    return ret;
  }

  protected List<String> getSuper(final String _oid) throws EFapsException {
    final List<String> ret = new ArrayList<String>();
    final SearchQuery query = new SearchQuery();
    query.setExpand(_oid, Type.get(getUUID4Type2Type()).getName() + "\\From");
    query.addSelect("To");
    query.execute();
    if (query.next()) {
      final String tooid =
          Type.get(getUUID4Type()).getId() + "." + query.get("To");
      ret.add(tooid);
      ret.addAll(getSuper(tooid));
    }
    return ret;
  }

  protected abstract class OneSource {

    private final String name;

    private final String oid;

    private final long id;

    public OneSource(final String _name, final String _oid, final long _id) {
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
