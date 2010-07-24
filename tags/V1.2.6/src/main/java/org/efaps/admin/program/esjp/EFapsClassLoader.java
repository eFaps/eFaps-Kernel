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

package org.efaps.admin.program.esjp;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class extends the ClassLoader of java, to be able to load Classes on
 * demand from the eFaps Database.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EFapsClassLoader
    extends ClassLoader
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsClassLoader.class);

    /**
     * should the Class be kept in a local Cache.
     */
    private static boolean HOLDCLASSESINCACHE = false;

    /**
     * holds all allready loaded Classes.
     */
    private static final Map<String, byte[]> LOADEDCLASSES = new HashMap<String, byte[]>();

    /**
     * Type instance of compile EJSP program.
     */
    private final Type classType;

    /**
     * Constructor setting the Parent of the EFapsClassLoader in ClassLoader.
     *
     * @param _parentClassLoader the Parent of the this EFapsClassLoader
     */
    public EFapsClassLoader(final ClassLoader _parentClassLoader)
    {
        super(_parentClassLoader);
        this.classType = CIAdminProgram.JavaClass.getType();
    }

    /**
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     * @param _name name of the class
     * @return Class
     * @throws ClassNotFoundException if class was not found
     */
    @Override()
    public Class<?> findClass(final String _name)
        throws ClassNotFoundException
    {
        byte[] b = getLoadedClasse(_name);
        if (b == null) {
            b = loadClassData(_name);
        }
        if (b == null) {
            throw new ClassNotFoundException(_name);
        }
        return defineClass(_name, b, 0, b.length);
    }

    /**
     * Loads the wanted Resource with the EFapsResourceStore into a byte-Array
     * to pass it on to findClass.
     *
     * @param _resourceName name of the Resource to load
     * @return byte[] containing the compiled javaclass
     */
    public byte[] loadClassData(final String _resourceName)
    {
        if (EFapsClassLoader.LOG.isDebugEnabled()) {
            EFapsClassLoader.LOG.debug("Loading Class '" + _resourceName + "' from Database");
        }
        final byte[] x = read(_resourceName);

        if (x != null && EFapsClassLoader.HOLDCLASSESINCACHE) {
            EFapsClassLoader.LOADEDCLASSES.put(_resourceName, x);
        }
        return x;
    }

    /**
     * The compiled class is received from the eFaps database (checked out)
     * using the name <code>_resourceName</code>.
     *
     * @param _resourceName     name of the resource to be received (ESJP class
     *                          name)
     * @return byte array containing the compiled ESJP class
     */
    public byte[] read(final String _resourceName)
    {
        byte[] ret = null;

        if (EFapsClassLoader.LOG.isDebugEnabled()) {
            EFapsClassLoader.LOG.debug("read '" + _resourceName + "'");
        }

        try {
            final QueryBuilder queryBuilder = new QueryBuilder(this.classType);
            queryBuilder.addWhereAttrEqValue("Name", _resourceName);
            final InstanceQuery query = queryBuilder.getQuery();
            query.execute();
            if (query.next()) {
                final Checkout checkout = new Checkout(query.getCurrentValue());
                final InputStream is = checkout.executeWithoutAccessCheck();

                ret = new byte[is.available()];
                is.read(ret);
                is.close();
            }
        } catch (final EFapsException e) {
            EFapsClassLoader.LOG.error("could not access the Database for reading '" + _resourceName + "'", e);
        } catch (final IOException e) {
            EFapsClassLoader.LOG.error("could not read the Javaclass '" + _resourceName + "'", e);
        }
        return ret;
    }

    /**
     * Get the Binary Class stored in the local Cache.
     *
     * @param _resourceName Name of the Class
     * @return binary Class, null if not in Cache
     */
    public byte[] getLoadedClasse(final String _resourceName)
    {
        return EFapsClassLoader.LOADEDCLASSES.get(_resourceName);
    }
}
