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

package org.efaps.update;

import static org.efaps.admin.EFapsClassNames.ADMIN_COMMON_VERSION;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.importer.DataImport;
import org.efaps.update.dbproperty.DBPropertiesUpdate;
import org.efaps.util.EFapsException;

/**
 * TODO description.
 *
 * @author The eFasp Team
 *
 * @version $Id$
 */
public class Install
{
    /**
     * List of all import classes. The order is also used for the import order.
     *
     * @see #importData()
     */
    private final Map<Class<? extends ImportInterface>, FileType> importClasses
                                                = new LinkedHashMap<Class<? extends ImportInterface>, FileType>();
    {
        if (this.importClasses.size() == 0) {
            this.importClasses.put(DataImport.class, FileType.XML);
            this.importClasses.put(DBPropertiesUpdate.class, FileType.XML);
        }
    }

    /**
     * All defined file urls which are updated.
     *
     * @see #addFile(URL, String)
     */
    private final List<InstallFile> files = new ArrayList<InstallFile>();

    /**
     * Flag to store that the cache is initialised.
     *
     * @see #initialise
     * @see #addURL
     */
    private boolean initialised = false;

    /**
     * Cache with all update instances (loaded from the list of {@link #urls}).
     *
     * @see #initialise
     * @see #install
     */
    private final Map<Class<? extends AbstractUpdate>, List<AbstractUpdate>> cache
                                            = new HashMap<Class<? extends AbstractUpdate>, List<AbstractUpdate>>();

    /**
     * Installs the XML update scripts of the schema definitions for this
     * version defined in {@link #number}. The install itself is done for given
     * version normally in one big transaction. If the database does not support
     * to big transactions (method
     * {@link AbstractDatabase#supportsBigTransactions}, each modification of
     * one update is commited within small single transactions.
     *
     * @param _number number to install
     * @param _latestNumber latest version number to install (e..g. defined in
     *            the version.xml file)
     * @see AbstractDatabase#supportsBigTransactions is used to get information
     *      about the support of very big transactions from the database
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public void install(final Long _number, final Long _latestNumber) throws EFapsException
    {
        final boolean bigTrans = Context.getDbType().supportsBigTransactions();
        final String user = (org.efaps.db.Context.getThreadContext().getPerson() != null) ? org.efaps.db.Context
                        .getThreadContext().getPerson().getName() : null;

        // initialize cache
        initialise();

        // initialize JexlContext (used to evaluate version)
        final JexlContext jexlContext = JexlHelper.createContext();
        if (_number != null) {
            jexlContext.getVars().put("version", _number);
        }
        if (_latestNumber != null) {
            jexlContext.getVars().put("latest", _latestNumber);
        }

        // create all objects
        for (final Map.Entry<Class<? extends AbstractUpdate>, List<AbstractUpdate>> entry : this.cache.entrySet()) {
            for (final AbstractUpdate update : entry.getValue()) {
                update.createInDB(jexlContext);
                if (!bigTrans) {
                    Context.commit();
                    Context.begin(user);
                }
            }
        }

        // and update them
        for (final Map.Entry<Class<? extends AbstractUpdate>, List<AbstractUpdate>> entry : this.cache.entrySet()) {
            for (final AbstractUpdate update : entry.getValue()) {
                update.updateInDB(jexlContext);
                if (!bigTrans) {
                    Context.commit();
                    Context.begin(user);
                }
            }
        }
    }

    /**
     * All installation files are updated. For each file, the installation and
     * latest version is evaluated depending from all installed version and the
     * defined application in the XML update file. The installation version is
     * the same as the latest version of the application.
     *
     * @throws EFapsException if update failed
     */
    @SuppressWarnings("unchecked")
    public void updateLatest() throws EFapsException
    {
        final boolean bigTrans = Context.getDbType().supportsBigTransactions();
        final String user = (org.efaps.db.Context.getThreadContext().getPerson() != null) ? org.efaps.db.Context
                        .getThreadContext().getPerson().getName() : null;

        // initialize cache
        initialise();

        // get for all applications the latest version
        final Map<String, Long> versions = getLatestVersions();

        // create all objects
        for (final Map.Entry<Class<? extends AbstractUpdate>, List<AbstractUpdate>> entry : this.cache.entrySet()) {
            for (final AbstractUpdate update : entry.getValue()) {
                final Long latestVersion = versions.get(update.getFileApplication());
                // initialize JexlContext (used to evaluate version)
                final JexlContext jexlContext = JexlHelper.createContext();
                if (latestVersion != null) {
                    jexlContext.getVars().put("version", latestVersion);
                    jexlContext.getVars().put("latest", latestVersion);
                }
                // and create
                update.createInDB(jexlContext);
                if (!bigTrans) {
                    Context.commit();
                    Context.begin(user);
                }
            }
        }

        // and update them
        for (final Map.Entry<Class<? extends AbstractUpdate>, List<AbstractUpdate>> entry : this.cache.entrySet()) {
            for (final AbstractUpdate update : entry.getValue()) {
                final Long latestVersion = versions.get(update.getFileApplication());
                // initialize JexlContext (used to evaluate version)
                final JexlContext jexlContext = JexlHelper.createContext();
                if (latestVersion != null) {
                    jexlContext.getVars().put("version", latestVersion);
                    jexlContext.getVars().put("latest", latestVersion);
                }
                update.updateInDB(jexlContext);
                if (!bigTrans) {
                    Context.commit();
                    Context.begin(user);
                }
            }
        }
    }

    /**
     * Load the already installed versions for this application from eFaps. The
     * method must be called within a Context begin and commit (it is not done
     * itself in this method!
     * @return Map containing the versions
     * @throws EFapsException on error
     */
    public Map<String, Long> getLatestVersions() throws EFapsException
    {
        final Map<String, Long> versions = new HashMap<String, Long>();
        final Type versionType = Type.get(ADMIN_COMMON_VERSION);
        if (versionType != null) {
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes(versionType.getName());
            query.addSelect("Name");
            query.addSelect("Revision");
            query.executeWithoutAccessCheck();
            while (query.next()) {
                final String name = (String) query.get("Name");
                final Long revision = (Long) query.get("Revision");
                if (!versions.containsKey(name) || (versions.get(name) < revision)) {
                    versions.put(name, revision);
                }
            }
            query.close();
        }
        return versions;
    }

    /**
     * Reads all XML update files and parses them.
     *
     * @throws SAXException
     * @throws IOException
     * @throws IOException
     * @throws FileNotFoundException
     * @see #initialised
     * @throws EFapsException on error
     */
    protected void initialise() throws EFapsException
    {

        if (!this.initialised) {
            this.initialised = true;
            this.cache.clear();

            for (final FileType fileType : FileType.values()) {

                if (fileType == FileType.XML) {
                    for (final InstallFile file : this.files) {
                        if (file.getType() == fileType) {
                            try {
                                final SaxHandler handler = new SaxHandler();
                                final AbstractUpdate elem = handler.parse(file.getUrl());

                                List<AbstractUpdate> list = this.cache.get(elem.getClass());
                                if (list == null) {
                                    list = new ArrayList<AbstractUpdate>();
                                    this.cache.put(elem.getClass(), list);
                                }
                                list.add(handler.getUpdate());
                            } catch (final Exception e) {
                                e.printStackTrace();
                                throw new Error(e);
                            }
                        }
                    }
                } else {
                    for (final Class<? extends AbstractUpdate> updateClass : fileType.clazzes) {

                        final List<AbstractUpdate> list = new ArrayList<AbstractUpdate>();
                        this.cache.put(updateClass, list);

                        Method method = null;
                        try {
                            method = updateClass.getMethod("readFile", URL.class);
                        } catch (final SecurityException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (final NoSuchMethodException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        for (final InstallFile file : this.files) {
                            if (file.getType() == fileType) {
                                Object obj = null;
                                try {
                                    obj = method.invoke(null, file.getUrl());
                                } catch (final IllegalArgumentException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                } catch (final IllegalAccessException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                } catch (final InvocationTargetException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                if (obj != null) {
                                    list.add((AbstractUpdate) obj);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to import the date.
     * @throws Exception on error
     */
    public void importData() throws Exception
    {
        for (final Entry<Class<? extends ImportInterface>, FileType> entry : this.importClasses.entrySet()) {
            final Method method = entry.getKey().getMethod("readFile", URL.class);

            for (final InstallFile file : this.files) {
                if (file.getType() == entry.getValue()) {
                    final Object obj = method.invoke(null, file.getUrl());
                    if (obj != null) {
                        ((ImportInterface) obj).updateInDB();
                    }
                }
            }
        }
    }

    /**
     * Appends a new file defined through an URL and the string representation
     * of the file type.
     *
     * @param _url URL of the file to append
     * @param _type type of the file
     * @see #files
     * @see #initialised
     * @see #addFile(URL, FileType) method called to add the URL after convert
     *      the string representation of the type to a file type instance
     */
    public void addFile(final URL _url, final String _type)
    {
        addFile(_url, FileType.getFileTypeByType(_type));
    }

    /**
     * Appends a new file defined through an URL. The initialized flag
     * {@link #initialized} is automatically reseted.
     *
     * @param _url URL of the file to add
     * @param _fileType file type of the file to add
     */
    public void addFile(final URL _url, final FileType _fileType)
    {
        this.files.add(new InstallFile(_url, _fileType));
        this.initialised = false;
    }

    /**
     * This is the getter method for the instance variable {@link #files}.
     *
     * @return value of instance variable {@link #files}
     */
    public List<InstallFile> getFiles()
    {
        return this.files;
    }

    /**
     * Returns a string representation with values of all instance variables.
     *
     * @return string representation of this Application
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("urls", this.files).toString();
    }

    /**
     * Class is used as a container for one file that must be installed.
     */
    private class InstallFile
    {
        /**
         * Url to the file.
         */
        private final URL url;

        /**
         * Type of the file.
         */
        private final FileType type;

        /**
         * @param _url      Url to the file
         * @param _type     Type of the file.
         */
        public InstallFile(final URL _url, final FileType _type)
        {
            this.url = _url;
            this.type = _type;
        }

        /**
         * This is the getter method for the instance variable {@link #url}.
         *
         * @return value of instance variable {@link #url}
         */
        public URL getUrl()
        {
            return this.url;
        }

        /**
         * This is the getter method for the instance variable {@link #type}.
         *
         * @return value of instance variable {@link #type}
         */
        public FileType getType()
        {
            return this.type;
        }
    }

    /**
     * This interface is used in {@link #org.efaps.update.Install.importData()}.
     *
     * @see #importClasses
     */
    public interface ImportInterface
    {
        /**
         * Method executes the actual update of the database.
         * @throws EFapsException on error
         */
        void updateInDB() throws EFapsException;
    }
}
