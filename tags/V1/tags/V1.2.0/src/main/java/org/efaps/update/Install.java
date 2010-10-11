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

package org.efaps.update;

import static org.efaps.admin.EFapsClassNames.ADMIN_COMMON_VERSION;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.update.schema.datamodel.SQLTableUpdate;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO description.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Install
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SQLTableUpdate.class);

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
    private final Map<Class<? extends IUpdate>, List<IUpdate>> cache
        = new HashMap<Class<? extends IUpdate>, List<IUpdate>>();

    /**
     * Installs the XML update scripts of the schema definitions for this
     * version defined in {@link #number}. The install itself is done for given
     * version normally in one big transaction. If the database does not support
     * to big transactions (method
     * {@link org.efaps.db.databases.AbstractDatabase#supportsBigTransactions()},
     * each modification of one update is committed within small single
     * transactions.
     *
     * @param _number           number to install
     * @param _latestNumber     latest version number to install (e..g. defined
     *                          in the version.xml file)
     * @param _ignoredSteps     set of ignored life cycle steps which are not
     *                          executed
     * @throws InstallationException on error
     * @see org.efaps.db.databases.AbstractDatabase#supportsBigTransactions()
     */
    @SuppressWarnings("unchecked")
    public void install(final Long _number,
                        final Long _latestNumber,
                        final Set<UpdateLifecycle> _ignoredSteps)
        throws InstallationException
    {
        final boolean bigTrans = Context.getDbType().supportsBigTransactions();
        final String user;
        try  {
            user = (Context.getThreadContext().getPerson() != null)
                   ? Context.getThreadContext().getPerson().getName()
                   : null;
        } catch (final EFapsException e)  {
            throw new InstallationException("No context in this thread defined!", e);
        }

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

        // loop through all life cycle steps
        for (final UpdateLifecycle step : getUpdateLifecycles())  {
            if (!_ignoredSteps.contains(step)) {
                if (Install.LOG.isInfoEnabled())  {
                    Install.LOG.info("..Running Lifecycle step " + step);
                }
                for (final Map.Entry<Class<? extends IUpdate>, List<IUpdate>> entry : this.cache.entrySet()) {
                    for (final IUpdate update : entry.getValue()) {
                        update.updateInDB(jexlContext, step);
                        if (!bigTrans) {
                            try {
                                Context.commit();
                            } catch (final EFapsException e) {
                                throw new InstallationException("Transaction commit failed", e);
                            }
                            try {
                                Context.begin(user);
                            } catch (final EFapsException e) {
                                throw new InstallationException("Transaction start failed", e);
                            }
                        }
                    }
                }
            } else if (Install.LOG.isInfoEnabled())  {
                Install.LOG.info("..Skipped Lifecycle step " + step);
            }
        }
    }

    /**
     * Method to get all UpdateLifecycle in an ordered List.
     * @return ordered List of all UpdateLifecycle
     */
    private List<UpdateLifecycle> getUpdateLifecycles()
    {
        final List<UpdateLifecycle> ret = new ArrayList<UpdateLifecycle>();
        for (final UpdateLifecycle cycle : UpdateLifecycle.values()) {
            ret.add(cycle);
        }
        Collections.sort(ret, new Comparator<UpdateLifecycle>() {

            public int compare(final UpdateLifecycle _cycle1,
                               final UpdateLifecycle _cycle2)
            {
                return _cycle1.getOrder().compareTo(_cycle2.getOrder());
            }
        });

        return ret;
    }

    /**
     * All installation files are updated. For each file, the installation and
     * latest version is evaluated depending from all installed version and the
     * defined application in the XML update file. The installation version is
     * the same as the latest version of the application.
     *
     * @throws InstallationException if update failed
     */
    @SuppressWarnings("unchecked")
    public void updateLatest()
        throws InstallationException
    {
        final boolean bigTrans = Context.getDbType().supportsBigTransactions();
        final String user;
        try  {
            user = (Context.getThreadContext().getPerson() != null)
                   ? Context.getThreadContext().getPerson().getName()
                   : null;
        } catch (final EFapsException e)  {
            throw new InstallationException("No context in this thread defined!", e);
        }

        // initialize cache
        initialise();

        // get for all applications the latest version
        final Map<String, Long> versions = getLatestVersions();

        // loop through all life cycle steps
        for (final UpdateLifecycle step : getUpdateLifecycles()) {
            if (Install.LOG.isInfoEnabled()) {
                Install.LOG.info("..Running Lifecycle step " + step);
            }
            for (final Map.Entry<Class<? extends IUpdate>, List<IUpdate>> entry
                            : this.cache.entrySet()) {
                for (final IUpdate update : entry.getValue()) {
                    final Long latestVersion = versions.get(update.getFileApplication());
                    // initialize JexlContext (used to evaluate version)
                    final JexlContext jexlContext = JexlHelper.createContext();
                    if (latestVersion != null) {
                        jexlContext.getVars().put("version", latestVersion);
                        jexlContext.getVars().put("latest", latestVersion);
                    }
                    // and create
                    update.updateInDB(jexlContext, step);
                    if (!bigTrans) {
                        if (!bigTrans) {
                            try {
                                Context.commit();
                            } catch (final EFapsException e) {
                                throw new InstallationException("Transaction commit failed", e);
                            }
                            try {
                                Context.begin(user);
                            } catch (final EFapsException e) {
                                throw new InstallationException("Transaction start failed", e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Load the already installed versions for this application from eFaps. The
     * method must be called within a Context begin and commit (it is not done
     * itself in this method!
     * @return Map containing the versions
     * @throws InstallationException on error
     */
    public Map<String, Long> getLatestVersions()
        throws InstallationException
    {
        final Map<String, Long> versions = new HashMap<String, Long>();
        final Type versionType = Type.get(ADMIN_COMMON_VERSION);
        if (versionType != null) {
            try  {
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
            } catch (final EFapsException e)  {
                throw new InstallationException("Latest version could not be found", e);
            }
        }
        return versions;
    }

    /**
     * Reads all XML update files and parses them.
     *
     * @see #initialised
     */
    protected void initialise()
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
                                final IUpdate elem = handler.parse(file.getUrl());

                                List<IUpdate> list = this.cache.get(elem.getClass());
                                if (list == null) {
                                    list = new ArrayList<IUpdate>();
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
                    for (final Class<? extends AbstractUpdate> updateClass : fileType.getClazzes()) {

                        final List<IUpdate> list = new ArrayList<IUpdate>();
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
    public void addFile(final URL _url,
                        final String _type)
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
    public void addFile(final URL _url,
                        final FileType _fileType)
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
    @Override()
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("urls", this.files)
            .toString();
    }

    /**
     * Class is used as a container for one file that must be installed.
     */
    private class InstallFile
    {
        /**
         * URL to the file.
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
        public InstallFile(final URL _url,
                           final FileType _type)
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
}
