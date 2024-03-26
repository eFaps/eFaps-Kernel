/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.admin.program.esjp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.efaps.admin.AppConfigHandler;
import org.efaps.ci.CIAdminProgram;
import org.efaps.ci.CIType;
import org.efaps.db.Checkout;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class extends the ClassLoader of java, to be able to load Classes on
 * demand from the eFaps Database.
 *
 * @author The eFaps Team
 *
 */
public final class EFapsClassLoader
    extends ClassLoader
{

    /**
     * Classloader to be used for singleton.
     */
    private static EFapsClassLoader CLASSLOADER;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsClassLoader.class);

    /**
     * Temporary folder.
     */
    private static File TMPFOLDER;

    /**
     * Type instance of compile EJSP program.
     */
    private final CIType classType;

    /**
     * Is the classloader permitted to load from the eFaps DataBase.
     */
    private final boolean offline;

    /**
     * Constructor setting the Parent of the EFapsClassLoader in ClassLoader.
     *
     * @param _parentClassLoader the Parent of the this EFapsClassLoader
     * @param _offline has the classloader tehr rigth to access the dataBase
     */
    private EFapsClassLoader(final ClassLoader _parentClassLoader,
                             final boolean _offline)
    {
        super(_parentClassLoader);
        this.offline = _offline;
        this.classType = CIAdminProgram.JavaClass;
    }

    /**
     * Checks if is is the classloader permitted to load from the eFaps DataBase.
     *
     * @return the is the classloader permitted to load from the eFaps DataBase
     */
    public boolean isOffline()
    {
        return this.offline;
    }

    /**
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     * @param _name name of the class
     * @return Class
     * @throws ClassNotFoundException if class was not found
     */
    @Override
    public Class<?> findClass(final String _name)
        throws ClassNotFoundException
    {
        Class<?> ret = null;
        if (this.offline) {
            throw new ClassNotFoundException(_name);
        } else {
            final byte[] b = loadClassData(_name);
            if (b != null) {
                ret = defineClass(_name, b, 0, b.length);
            } else {
                throw new ClassNotFoundException(_name);
            }
        }
        return ret;
    }

    /**
     * In case of jbpm this is necessary for compiling,
     * because they search the classes with URL.
     * @param _name filename as url
     * @return URL if found
     */
    @Override
    public URL findResource(final String _name)
    {
        URL ret = null;
        final String name = _name.replaceAll(System.getProperty("file.separator"), ".").replaceAll(".class", "");
        final byte[] data = loadClassData(name);
        if (data != null && data.length > 0) {
            final File file = FileUtils.getFile(EFapsClassLoader.getTempFolder(), name);
            try {
                if (!file.exists() || FileUtils.isFileOlder(file, new DateTime().minusHours(1).toDate())) {
                    FileUtils.writeByteArrayToFile(file, data);
                }
                ret = file.toURI().toURL();
            } catch (final IOException e) {
                EFapsClassLoader.LOG.error("Could not geneate File for reading from URL: {}", name);
            }
        }
        return ret;
    }

    /**
     * Loads the wanted Resource with the EFapsResourceStore into a byte-Array
     * to pass it on to findClass.
     *
     * @param _resourceName name of the Resource to load
     * @return byte[] containing the compiled javaclass
     */
    protected byte[] loadClassData(final String _resourceName)
    {
        EFapsClassLoader.LOG.debug("Loading Class '{}' from Database.", _resourceName);
        final byte[] x = read(_resourceName);
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
        EFapsClassLoader.LOG.debug("read ''", _resourceName);
        try {
            final QueryBuilder queryBuilder = new QueryBuilder(this.classType);
            queryBuilder.addWhereAttrEqValue("Name", _resourceName);
            final InstanceQuery query = queryBuilder.getCachedQuery("esjp");
            query.execute();
            if (query.next()) {
                final Checkout checkout = new Checkout(query.getCurrentValue());
                final InputStream is = checkout.executeWithoutAccessCheck();

                ret = new byte[is.available()];
                is.read(ret);
                is.close();
            }
        } catch (final EFapsException e) {
            EFapsClassLoader.LOG.error("could not access the Database for reading '{}' - {}", _resourceName, e);
        } catch (final IOException e) {
            EFapsClassLoader.LOG.error("could not read the Javaclass '{}' - {}", _resourceName, e);
        }
        return ret;
    }

    /**
     * @return the tmpfolder
     */
    private static File getTempFolder()
    {
        if (EFapsClassLoader.TMPFOLDER == null || !EFapsClassLoader.TMPFOLDER.exists()) {
            File tmpfld = AppConfigHandler.get().getTempFolder();
            if (tmpfld == null) {
                final File temp;
                try {
                    temp = File.createTempFile("eFaps", ".tmp");
                    tmpfld = temp.getParentFile();
                    temp.delete();
                } catch (final IOException e) {
                    EFapsClassLoader.LOG.error("Cannot create temp file", e);
                }
            }
            EFapsClassLoader.TMPFOLDER = new File(tmpfld, "eFaps-ClassFiles");
            if (!EFapsClassLoader.TMPFOLDER.exists()) {
                final boolean mkdir = EFapsClassLoader.TMPFOLDER.mkdir();
                if (!mkdir) {
                    EFapsClassLoader.LOG.error("Temp folder was not created");
                }
            }
        }
        return EFapsClassLoader.TMPFOLDER;
    }

    /**
     * Get the current EFapsClassLoader.
     * This static method is used to provide a way to use the same classloader
     * in different threads, due to the reason that using different classloader
     * instances might bring the problem of "instanceof" return unexpected results.
     *
     * @return the current EFapsClassLoader
     */
    public static synchronized EFapsClassLoader getInstance()
    {
        if (EFapsClassLoader.CLASSLOADER == null) {
            EFapsClassLoader.CLASSLOADER = new EFapsClassLoader(EFapsClassLoader.class.getClassLoader(), false);
        }
        return EFapsClassLoader.CLASSLOADER;
    }

    /**
     * Get the current EFapsClassLoader.
     * This static method is used to provide a way to use the same classloader
     * in different threads, due to the reason that using different classloader
     * instances might bring the problem of "instanceof" return unexpected results.
     * @param _parent parent classloader
     * @return the current EFapsClassLoader
     */
    public static synchronized EFapsClassLoader getOfflineInstance(final ClassLoader _parent)
    {
        if (EFapsClassLoader.CLASSLOADER == null) {
            EFapsClassLoader.CLASSLOADER = new EFapsClassLoader(_parent, true);
        }
        return EFapsClassLoader.CLASSLOADER;
    }

    /**
     * To be able to know if it is the first time the Classloader is wanted.
     * @return is the static Class loader initialized.
     */
    public static boolean isInitialized()
    {
        return EFapsClassLoader.CLASSLOADER != null;
    }
}
