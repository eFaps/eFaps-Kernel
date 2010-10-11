/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.update.schema.program.staticsource;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkin;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.update.schema.program.jasperreport.JasperReportCompiler;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to compile JavaScript and style sheets.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractStaticSourceCompiler
{

    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractStaticSourceCompiler.class);

    /**
     * Static Method that executes the method compile for the SubClasses
     * CSSCompiler and JavaScriptCompiler.
     * @param _classPathElements elements on the classpath
     * @throws EFapsException on error
     */
    public static void compileAll(final List<String> _classPathElements)
        throws EFapsException
    {
        (new JasperReportCompiler(_classPathElements)).compile();
        (new CSSCompiler()).compile();
        (new JavaScriptCompiler()).compile();
        (new WikiCompiler()).compile();
    }

    /**
     * This method is doing the actual compiling in the following steps. <li>
     * read all existing compiled source from the eFaps-DataBase</li> <li>read
     * all sources from the eFaps-DataBase</li> <li>compile all sources
     * (including the extending of any super type) and insert it into the
     * eFaps-DataBase</li>
     *
     * @throws EFapsException on error
     */
    public void compile() throws EFapsException
    {
        final Map<String, String> compiled = readCompiledSources();

        final List<AbstractSource> allsource = readSources();

        for (final AbstractSource onesource : allsource) {

            if (AbstractStaticSourceCompiler.LOG.isInfoEnabled()) {
                AbstractStaticSourceCompiler.LOG.info("compiling " + onesource.getName());
            }

            final List<String> supers = getSuper(onesource.getOid());
            final StringBuilder builder = new StringBuilder();
            while (!supers.isEmpty()) {
                builder.append(getCompiledString(supers.get(supers.size() - 1)));
                supers.remove(supers.size() - 1);
            }
            builder.append(getCompiledString(onesource.getOid()));
            final Update update;
            if (compiled.containsKey(onesource.getName())) {
                update = new Update(compiled.get(onesource.getName()));
            } else {
                update = new Insert(Type.get(getClassName4TypeCompiled()));
            }
            update.add("Name", onesource.getName());
            update.add("ProgramLink", "" + onesource.getId());
            update.executeWithoutAccessCheck();
            final Instance instance = update.getInstance();
            update.close();

            byte[] mybytes = null;
            try {
                mybytes = builder.toString().getBytes("UTF-8");
            } catch (final UnsupportedEncodingException e) {
                AbstractStaticSourceCompiler.LOG.error("error in reading Bytes from String using UTF-8", e);
            }
            final ByteArrayInputStream str = new ByteArrayInputStream(mybytes);
            String name = onesource.getName().substring(0, onesource.getName().lastIndexOf("."));
            name = name.substring(name.lastIndexOf(".") + 1)
                            + onesource.getName().substring(onesource.getName().lastIndexOf("."));

            final Checkin checkin = new Checkin(instance);
            checkin.executeWithoutAccessCheck(name, str, mybytes.length);
        }

    }

    /**
     * Get the UUID for the CompiledType.
     *
     * @return UUID for the CompiledType
     */
    protected abstract EFapsClassNames getClassName4TypeCompiled();

    /**
     * Get the UUID for the Type.
     *
     * @return UUID for the Type
     */
    protected abstract EFapsClassNames getClassName4Type();

    /**
     * Get the UUID for the Type2Type.
     *
     * @return UUID for the Type2Type
     */
    protected abstract EFapsClassNames getClassName4Type2Type();

    /**
     * Get a new AbstractSource to instantiate.
     *
     * @see #AbstractSource
     * @see #readSources()
     * @param _name name of the source
     * @param _oid oid of the source
     * @param _id id of the source
     * @return AbstractSource
     */
    protected abstract AbstractSource getNewSource(final String _name, final String _oid, final long _id);

    /**
     * Get the compiled String for the Instance with OID _oid.
     *
     * @param _oid oid of the instance the compiled String will be returned
     * @return a compiled String of the Instance Oid
     */
    protected abstract String getCompiledString(final String _oid);

    /**
     * This method reads all compiled Sources from the eFaps-DataBase and
     * returns a map with name to oid relation.
     *
     * @return Map with name to oid of the compiled source
     * @throws EFapsException on error
     */
    protected Map<String, String> readCompiledSources() throws EFapsException
    {
        final Map<String, String> ret = new HashMap<String, String>();
        final SearchQuery query = new SearchQuery();
        query.setQueryTypes(Type.get(getClassName4TypeCompiled()).getName());
        query.addSelect("OID");
        query.addSelect("Name");
        query.executeWithoutAccessCheck();
        while (query.next()) {
            final String name = (String) query.get("Name");
            final String oid = (String) query.get("OID");
            ret.put(name, oid);
        }
        return ret;
    }

    /**
     * This method reads all Sources from the eFapsDataBase and returns for each
     * Source a Instance of AbstractSource in a List.
     *
     * @return List with AbstractSources
     * @throws EFapsException on error
     */
    protected List<AbstractSource> readSources() throws EFapsException
    {
        final List<AbstractSource> ret = new ArrayList<AbstractSource>();
        final SearchQuery query = new SearchQuery();
        query.setQueryTypes(Type.get(getClassName4Type()).getName());
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
     * Recursive method that searches the SuperSource for the current Instance
     * identified by the oid.
     *
     * @param _oid OId of the Instance the Super Instance will be searched
     * @return List of SuperSources in reverse order
     * @throws EFapsException error
     */
    protected List<String> getSuper(final String _oid) throws EFapsException
    {
        final List<String> ret = new ArrayList<String>();
        final SearchQuery query = new SearchQuery();
        query.setExpand(_oid, Type.get(getClassName4Type2Type()).getName() + "\\From");
        query.addSelect("To");
        query.execute();
        if (query.next()) {
            final String tooid = Type.get(getClassName4Type()).getId() + "." + query.get("To");
            ret.add(tooid);
            ret.addAll(getSuper(tooid));
        }
        return ret;
    }

    /**
     * Class to access one source.
     */
    protected abstract class AbstractSource
    {

        /**
         * Stores the name of this source.
         */
        private final String name;

        /**
         * Stores the oid of this source.
         */
        private final String oid;

        /**
         * Stores the id of this source.
         */
        private final long id;

        /**
         * Constructor setting all instance variables.
         *
         * @param _name name of the source
         * @param _oid oid of the source
         * @param _id id of the source
         */
        public AbstractSource(final String _name, final String _oid, final long _id)
        {
            this.name = _name;
            this.oid = _oid;
            this.id = _id;
        }

        /**
         * This is the getter method for the instance variable {@link #name}.
         *
         * @return value of instance variable {@link #name}
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * This is the getter method for the instance variable {@link #oid}.
         *
         * @return value of instance variable {@link #oid}
         */
        public String getOid()
        {
            return this.oid;
        }

        /**
         * This is the getter method for the instance variable {@link #id}.
         *
         * @return value of instance variable {@link #id}
         */
        public long getId()
        {
            return this.id;
        }
    }
}
