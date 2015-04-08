/*
 * Copyright 2003 - 2013 The eFaps Team
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Context;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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
    private static final Logger LOG = LoggerFactory.getLogger(Install.class);

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
     * Evaluate the profiles from SystemConfiguration.
     */
    private final boolean evaluateProfiles;

    /**
     * Standard Constructor.
     */
    public Install()
    {
        this(false);
    }

    /**
     * @param _evaluateProfiles evaluate profiles from systemconfiguration
     */
    public Install(final boolean _evaluateProfiles)
    {
        this.evaluateProfiles = _evaluateProfiles;
    }

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
     * @param _profiles         profiles to be applied
     * @param _ignoredSteps     set of ignored life cycle steps which are not
     *                          executed
     * @throws InstallationException on error
     * @see org.efaps.db.databases.AbstractDatabase#supportsBigTransactions()
     */
    public void install(final Long _number,
                        final Long _latestNumber,
                        final Set<Profile> _profiles,
                        final Set<UpdateLifecycle> _ignoredSteps)
        throws InstallationException
    {
        final boolean bigTrans = Context.getDbType().supportsBigTransactions();
        final String user;
        try  {
            user = Context.getThreadContext().getPerson() != null
                   ? Context.getThreadContext().getPerson().getName()
                   : null;
        } catch (final EFapsException e)  {
            throw new InstallationException("No context in this thread defined!", e);
        }

        // initialize cache
        initialise();

        // initialize JexlContext (used to evaluate version)
        final JexlContext jexlContext = new MapContext();
        if (_number != null) {
            jexlContext.set("version", _number);
        }
        if (_latestNumber != null) {
            jexlContext.set("latest", _latestNumber);
        }

        // loop through all life cycle steps
        for (final UpdateLifecycle step : getUpdateLifecycles())  {
            if (!_ignoredSteps.contains(step)) {
                if (Install.LOG.isInfoEnabled())  {
                    Install.LOG.info("..Running Lifecycle step " + step);
                }
                for (final Map.Entry<Class<? extends IUpdate>, List<IUpdate>> entry : this.cache.entrySet()) {
                    final List<IUpdate> updates = entry.getValue();
                    Collections.sort(updates, new Comparator<IUpdate>()
                    {
                        @Override
                        public int compare(final IUpdate _update0,
                                           final IUpdate _update1)
                        {
                            return String.valueOf(_update0.getInstallFile().getUrl()).compareTo(
                                            String.valueOf(_update1.getInstallFile().getUrl()));
                        }
                    });
                    for (final IUpdate update : updates) {
                        try {
                            update.updateInDB(jexlContext, step,
                                            evaluateProfiles(update.getFileApplication(), _profiles));
                            if (!bigTrans) {
                                Context.commit();
                                Context.begin(user);
                            }
                        } catch (final EFapsException e) {
                            throw new InstallationException("Transaction start failed", e);
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

            @Override
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
     * @param _profiles set of profiles to be used
     * @throws InstallationException if update failed
     */
    public void updateLatest(final Set<Profile> _profiles)
        throws InstallationException
    {
        final boolean bigTrans = Context.getDbType().supportsBigTransactions();
        final String user;
        try  {
            user = Context.getThreadContext().getPerson() != null
                   ? Context.getThreadContext().getPerson().getName()
                   : null;
        } catch (final EFapsException e)  {
            throw new InstallationException("No context in this thread defined!", e);
        }

        // initialize cache
        initialise();

        // get for all applications the latest version
        final Map<String, Integer> versions = getLatestVersions();

        // loop through all life cycle steps
        for (final UpdateLifecycle step : getUpdateLifecycles()) {
            if (Install.LOG.isInfoEnabled()) {
                Install.LOG.info("..Running Lifecycle step " + step);
            }
            for (final Map.Entry<Class<? extends IUpdate>, List<IUpdate>> entry : this.cache.entrySet()) {
                for (final IUpdate update : entry.getValue()) {
                    final Integer latestVersion = versions.get(update.getFileApplication());
                    // initialize JexlContext (used to evaluate version)
                    final JexlContext jexlContext = new MapContext();
                    if (latestVersion != null) {
                        jexlContext.set("version", latestVersion);
                        jexlContext.set("latest", latestVersion);
                    }
                    try {
                        // and create
                        update.updateInDB(jexlContext, step, evaluateProfiles(update.getFileApplication(), _profiles));
                        if (!bigTrans) {
                            Context.commit();
                            Context.begin(user);
                        }
                    } catch (final EFapsException e) {
                        throw new InstallationException("Transaction start failed", e);
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
    public Map<String, Integer> getLatestVersions()
        throws InstallationException
    {
        final Map<String, Integer> versions = new HashMap<String, Integer>();
        try {
            if (Context.getDbType().existsView(Context.getThreadContext().getConnection(), "V_ADMINTYPE")
                            && CIAdminCommon.Application.getType() != null) {
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.ApplicationVersion);
                final MultiPrintQuery multi = queryBldr.getPrint();
                final SelectBuilder selName = SelectBuilder.get()
                                .linkto(CIAdminCommon.ApplicationVersion.ApplicationLink)
                                .attribute(CIAdminCommon.Application.Name);
                multi.addSelect(selName);
                multi.addAttribute(CIAdminCommon.ApplicationVersion.Revision);
                multi.executeWithoutAccessCheck();
                while (multi.next()) {
                    final String name = multi.<String>getSelect(selName);
                    final Integer revision = multi.<Integer>getAttribute(CIAdminCommon.ApplicationVersion.Revision);
                    if (!versions.containsKey(name) || versions.get(name) < revision) {
                        versions.put(name, revision);
                    }
                }
            }
        } catch (final EFapsException e) {
            throw new InstallationException("Latest version could not be found", e);
        } catch (final SQLException e) {
            throw new InstallationException("Latest version could not be found", e);
        }
        return versions;
    }

    /**
     * Reads all XML update files and parses them.
     *
     * @see #initialised
     * @throws InstallationException on error
     */
    protected void initialise()
        throws InstallationException
    {

        if (!this.initialised) {
            this.initialised = true;
            this.cache.clear();
            AppDependency.initialise();
            for (final FileType fileType : FileType.values()) {

                if (fileType == FileType.XML) {
                    for (final InstallFile file : this.files) {
                        if (file.getType() == fileType) {
                            final SaxHandler handler = new SaxHandler();
                            try {
                                final IUpdate elem = handler.parse(file);
                                List<IUpdate> list = this.cache.get(elem.getClass());
                                if (list == null) {
                                    list = new ArrayList<IUpdate>();
                                    this.cache.put(elem.getClass(), list);
                                }
                                list.add(handler.getUpdate());
                            } catch (final SAXException e) {
                                throw new InstallationException("initialise()", e);
                            } catch (final IOException e) {
                                throw new InstallationException("initialise()", e);
                            }
                        }
                    }
                } else {
                    for (final Class<? extends AbstractUpdate> updateClass : fileType.getClazzes()) {

                        final List<IUpdate> list = new ArrayList<IUpdate>();
                        this.cache.put(updateClass, list);

                        Method method = null;
                        try {
                            method = updateClass.getMethod("readFile", InstallFile.class);
                        } catch (final SecurityException e) {
                            throw new InstallationException("initialise()", e);
                        } catch (final NoSuchMethodException e) {
                            throw new InstallationException("initialise()", e);
                        }
                        for (final InstallFile file : this.files) {
                            if (file.getType() == fileType) {
                                Object obj = null;
                                try {
                                    obj = method.invoke(null, file);
                                } catch (final IllegalArgumentException e) {
                                    throw new InstallationException("initialise()", e);
                                } catch (final IllegalAccessException e) {
                                    throw new InstallationException("initialise()", e);
                                } catch (final InvocationTargetException e) {
                                    throw new InstallationException("initialise()", e);
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
     * @param _installFile InstallFile to add
     */
    public void addFile(final InstallFile _installFile)
    {
        this.files.add(_installFile);
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
     * Getter method for the instance variable {@link #evaluateProfiles}.
     *
     * @return value of instance variable {@link #evaluateProfiles}
     */
    protected boolean isEvaluateProfiles()
    {
        return this.evaluateProfiles;
    }

    /**
     * @param _application  application used as key for SystemConfiguration
     * @param _profile      profiles
     * @return set of profiles
     * @throws EFapsException on error
     */
    private Set<Profile> evaluateProfiles(final String _application,
                                          final Set<Profile> _profile)
        throws EFapsException
    {
        final Set<Profile> ret;
        if (_profile == null && _application != null) {
            ret = new HashSet<Profile>();
            final Properties props = EFapsSystemConfiguration.get().getAttributeValueAsProperties(
                            KernelSettings.PROFILES4UPDATE, true);
            if (props.containsKey(_application)) {
                final String[] profileNames = props.getProperty(_application).split(";");
                for (final String name : profileNames) {
                    ret.add(Profile.getProfile(name));
                }
            }
            if (ret.isEmpty()) {
                ret.add(Profile.getDefaultProfile());
            }
        } else {
            ret = _profile;
        }
        Install.LOG.debug("Applying profiles: {}", ret);
        return ret;
    }

    /**
     * Returns a string representation with values of all instance variables.
     *
     * @return string representation of this Application
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("urls", this.files)
            .toString();
    }

    /**
     * Class is used as a container for one file that must be installed.
     */
    public static class InstallFile
    {
        /**
         * URL to the file.
         */
        private URL url;

        /**
         * Type of the file.
         */
        private FileType type;

        private String revision;

        private String name;

        private DateTime date;

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
         * Setter method for instance variable {@link #url}.
         *
         * @param _revision value for instance variable {@link #url}
         */
        public InstallFile setURL(final URL _url)
        {
            this.url = _url;
            return this;
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

        /**
         * Getter method for the instance variable {@link #revision}.
         *
         * @return value of instance variable {@link #revision}
         */
        public String getRevision()
        {
            return this.revision == null ? "UNKNOWN" : this.revision;
        }

        /**
         * Setter method for instance variable {@link #revision}.
         *
         * @param _revision value for instance variable {@link #revision}
         */
        public InstallFile setRevision(final String _revision)
        {
            this.revision = _revision;
            return this;
        }

        /**
         * Setter method for instance variable {@link #type}.
         *
         * @param _type value for instance variable {@link #type}
         */
        public InstallFile setType(final String _type)
        {
            this.type = FileType.getFileTypeByType(_type);
            return this;
        }

        /**
         * Getter method for the instance variable {@link #name}.
         *
         * @return value of instance variable {@link #name}
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * Setter method for instance variable {@link #name}.
         *
         * @param _name value for instance variable {@link #name}
         */
        public InstallFile setName(final String _name)
        {
            this.name = _name;
            return this;
        }

        /**
         * Getter method for the instance variable {@link #date}.
         *
         * @return value of instance variable {@link #date}
         */
        public DateTime getDate()
        {
            return this.date == null ? new DateTime() : this.date;
        }

        /**
         * Setter method for instance variable {@link #date}.
         *
         * @param _date value for instance variable {@link #date}
         */
        public InstallFile setDate(final DateTime _date)
        {
            this.date = _date;
            return this;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
