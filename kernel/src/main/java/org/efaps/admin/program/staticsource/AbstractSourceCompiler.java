/*
 * Copyright 2003 - 2009 The eFaps Team
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
import java.io.UnsupportedEncodingException;
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

  /**
   * Static Method that executes the method compile for the SubClasses
   * CSSCompiler and JavaScriptCompiler
   *
   * @throws EFapsException
   */
  public static void compileAll() throws EFapsException {
    (new CSSCompiler()).compile();
    (new JavaScriptCompiler()).compile();
  }

  /**
   * this method is doig the actual compiling in the following steps
   * <li>delete all existing compiled source from the eFaps-DataBase</li>
   * <li>read all sources from the eFaps-DataBase</li>
   * <li>compile all sources (including the ecteding of any super type) and
   * insert it into the eFaps-DataBase</li>
   *
   * @throws EFapsException
   */
  public void compile() throws EFapsException {
    removeAllCompiled();

    final List<AbstractSource> allsource = readSources();

    for (final AbstractSource onecss : allsource) {

      if (LOG.isInfoEnabled()) {
        LOG.info("compiling " + onecss.getName());
      }

      final List<String> supers = getSuper(onecss.getOid());
      final StringBuilder builder = new StringBuilder();
      while (!supers.isEmpty()) {
        builder.append(getCompiledString(supers.get(supers.size() - 1)));
        supers.remove(supers.size() - 1);
      }
      builder.append(getCompiledString(onecss.getOid()));

      final Insert insert = new Insert(Type.get(getUUID4TypeCompiled()));
      insert.add("Name", onecss.getName());
      insert.add("ProgramLink", "" + onecss.getId());
      insert.executeWithoutAccessCheck();
      final Instance instance = insert.getInstance();
      insert.close();

      byte[] mybytes = null;
      try {
        mybytes = builder.toString().getBytes("UTF-8");
      } catch (final UnsupportedEncodingException e) {
        LOG.error("error in reading Bytes from String using UTF-8", e);
      }
      final ByteArrayInputStream str = new ByteArrayInputStream(mybytes);
      String name =
          onecss.getName().substring(0, onecss.getName().lastIndexOf("."));

      name =
          name.substring(name.lastIndexOf(".") + 1)
              + onecss.getName().substring(onecss.getName().lastIndexOf("."));

      final Checkin checkin = new Checkin(instance);
      checkin.executeWithoutAccessCheck(name, str, mybytes.length);
    }

  }

  /**
   * get the UUID for the CompiledType
   *
   * @return UUID for the CompiledType
   */
  protected abstract UUID getUUID4TypeCompiled();

  /**
   * get the UUID for the Type
   *
   * @return UUID for the Type
   */
  protected abstract UUID getUUID4Type();

  /**
   * get the UUID for the Type2Type
   *
   * @return UUID for the Type2Type
   */
  protected abstract UUID getUUID4Type2Type();

  /**
   * get a new AbstractSource to instanciate
   *
   * @see #AbstractSource
   * @see #readSources()
   * @param _name
   * @param _oid
   * @param _id
   * @return
   */
  protected abstract AbstractSource getNewSource(final String _name,
                                                 final String _oid,
                                                 final long _id);

  /**
   * get the compiled String for the Instance with OID _oid
   *
   * @param _oid
   *                oid of the instance the compiled STrign will be returned
   * @return a compiled String of the Instance Oid
   */
  protected abstract String getCompiledString(final String _oid);

  /**
   * This method removes all compiled Types from the eFapas-DataBase
   *
   * @throws EFapsException
   */
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

  /**
   * this method reads all Sources from the eFapsDataBase and returns for each
   * Source a Instance od AbstractSource in a List
   *
   * @return List with AbstractSources
   * @throws EFapsException
   */
  protected List<AbstractSource> readSources() throws EFapsException {
    final List<AbstractSource> ret = new ArrayList<AbstractSource>();
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
      ret.add(getNewSource(name, oid, id));
    }
    return ret;
  }

  /**
   * recursive method that searches the SuperSource for the current Instance
   * identified by the oid
   *
   * @param _oid
   *                OId of the Instance the Super Instance will be searched
   * @return List of SuperSources in reverse order
   * @throws EFapsException
   */
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

  /**
   * TODO description
   *
   * @author jmox
   * @version $Id$
   */
  protected abstract class AbstractSource {

    /**
     * stores the name of this source
     */
    private final String name;

    /**
     * stores the oid of this source
     */
    private final String oid;

    /**
     * stores the id of this source
     */
    private final long id;

    public AbstractSource(final String _name, final String _oid, final long _id) {
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
