/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.update.version;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.Context;
import org.efaps.update.Install;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines one version of the application to install.
 *
 * @author The eFaps Team
 * @version $Id$
 * TODO: in case of a script: it must be possible to deactivate the context
 */
public class ApplicationVersion
    implements Comparable<ApplicationVersion>
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationVersion.class);

    /**
     * The number of the version is stored in this instance variable.
     *
     * @see #setNumber
     * @see #getNumber
     */
    private long number = 0;

    /**
     * Store the information weather a compile must be done after installing
     * this version.
     *
     * @see #setCompile(boolean)
     */
    private boolean compile = false;

    /**
     * Is a login for this version needed? This means if a new transaction is
     * started, a login with given user is made. The default value is
     * <i>true</i>.
     *
     * @see #setLoginNeeded(boolean)
     */
    private boolean loginNeeded = true;

    /**
     * Is a reload cache for this version needed? This means before the
     * installation of this version starts, a reload cache is done. The default
     * value is <i>true</i>.
     *
     * @see #setReloadCacheNeeded
     */
    private boolean reloadCacheNeeded = true;

    /**
     * List of all scripts for this version.
     *
     * @see #addScript(String, String, String, String)
     */
    private final List<AbstractScript> scripts = new ArrayList<AbstractScript>();

    /**
     * Description of this version.
     *
     * @see #appendDescription(String)
     * @see #getDescription()
     */
    private final StringBuilder description = new StringBuilder();

    /**
     * Set of ignored life cycle steps.
     *
     * @see #addIgnoredStep(String)
     */
    private final Set<UpdateLifecycle> ignoredSteps = new HashSet<UpdateLifecycle>();

    /**
     * Application this version belongs to.
     */
    private Application application;

    /**
     * Installs the XML update scripts of the schema definitions for this
     * version defined in {@link #number}.
     *
     * @param _install install instance with all cached XML definitions
     * @param _latestVersionNumber latest version number (defined in the
     *            version.xml file)
     * @param _userName name of logged in user
     * @param _password password of logged in user
     * @throws InstallationException on error
     */
    public void install(final Install _install,
                        final long _latestVersionNumber,
                        final String _userName,
                        final String _password)
        throws InstallationException
    {
        // reload cache if needed
        if (this.reloadCacheNeeded) {
            this.application.reloadCache();
        }
        try {
            // start transaction (with user name if needed)
            if (this.loginNeeded) {
                Context.begin(_userName);
            } else {
                Context.begin();
            }

            _install.install(this.number, _latestVersionNumber, this.ignoredSteps);

            // commit transaction
            Context.commit();

            // execute all scripts
            for (final AbstractScript script : this.scripts) {
                script.execute(_userName, _password);
            }

            // Compile esjp's in the database (if the compile flag is set).
            if (this.compile) {
                this.application.compileAll(_userName, true);
            }
        } catch (final EFapsException e) {
            throw new InstallationException("error in Context", e);
        }
    }

    /**
     * Adds a new Script to this version.
     *
     * @param _code     code of script to execute
     * @param _type     type of the code, groovy, rhino
     * @param _name     file name of the script
     * @param _function name of function which is called
     */
    public void addScript(final String _code,
                          final String _type,
                          final String _name,
                          final String _function)
    {
        AbstractScript script = null;
        if ("rhino".equalsIgnoreCase(_type)) {
            script = new RhinoScript(_code, _name, _function);
        } else if ("groovy".equalsIgnoreCase(_type)) {
            script = new GroovyScript(_code, _name, _function);
        }
        if (script != null) {
            this.scripts.add(script);
        }
    }

    /**
     * Append a description for this version.
     *
     * @param _desc text of description to append
     * @see #description
     */
    public void appendDescription(final String _desc)
    {
        if (_desc != null) {
            this.description.append(_desc.trim()).append("\n");
        }
    }

    /**
     * @param _appl Application
     */
    public void setApplication(final Application _appl)
    {
        this.application = _appl;
    }

    /**
     * The description for this version is returned. If no description exists, a
     * zero length description is returned.
     *
     * @return string value of instance variable {@link #description}
     * @see #description
     */
    public String getDescription()
    {
        return this.description.toString().trim();
    }

    /**
     * Appends a step which is ignored within the installation of this version.
     *
     * @param _step ignored step
     * @see #ignoredSteps
     */
    public void addIgnoredStep(final String _step)
    {
        this.ignoredSteps.add(UpdateLifecycle.valueOf(_step.toUpperCase()));
    }

    /**
     * Compares this application version with the specified application version.<br/>
     * The method compares the version number of the application version. To do
     * this, the method {@link java.lang.Long#compareTo} is called.
     *
     * @param _compareTo application version instance to compare to
     * @return a negative integer, zero, or a positive integer as this
     *         application version is less than, equal to, or greater than the
     *         specified application version
     * @see java.lang.Long#compareTo
     * @see java.lang.Comparable#compareTo
     */
    public int compareTo(final ApplicationVersion _compareTo)
    {
        return new Long(this.number).compareTo(_compareTo.number);
    }

    /**
     * This is the setter method for instance variable {@link #number}.
     *
     * @param _number new value for instance variable {@link #number}
     * @see #number
     * @see #getNumber
     */
    public void setNumber(final long _number)
    {
        this.number = _number;
    }

    /**
     * This is the getter method for instance variable {@link #number}.
     *
     * @return value of instance variable {@link #number}
     * @see #number
     * @see #setNumber
     */
    public Long getNumber()
    {
        return this.number;
    }

    /**
     * This is the setter method for instance variable {@link #compile}.
     *
     * @param _compile new value for instance variable {@link #compile}
     * @see #compile
     */
    public void setCompile(final boolean _compile)
    {
        this.compile = _compile;
    }

    /**
     * This is the setter method for instance variable {@link #loginNeeded}.
     *
     * @param _loginNeeded      <i>true</i> means that a login is needed for
     *                          this version
     * @see #loginNeeded
     */
    public void setLoginNeeded(final boolean _loginNeeded)
    {
        this.loginNeeded = _loginNeeded;
    }

    /**
     * This is the setter method for instance variable
     * {@link #reloadCacheNeeded}.
     *
     * @param _reloadCacheNeeded    <i>true</i> means that the cache must be
     *                              reloaded
     * @see #reloadCacheNeeded
     */
    public void setReloadCacheNeeded(final boolean _reloadCacheNeeded)
    {
        this.reloadCacheNeeded = _reloadCacheNeeded;
    }

    /**
     * Returns the complete root URL so that resources in the installation
     * package could be fetched. If an installation from the source directory
     * is done, the {@link Application#getRootUrl() root URL} is directly
     * returned, in the case that an installation is done from a JAR container
     * the {@link Application#getRootUrl() root URL} is appended with the name
     * of the {@link Application#getRootPackageName() root package name}.
     *
     * @return complete root URL
     * @throws InstallationException if complete root URL could not be prepared
     */
    protected URL getCompleteRootUrl()
        throws InstallationException
    {
        try {
            final URL url;
            if (this.application.getRootPackageName() == null) {
                url = this.application.getRootUrl();
            } else {
                url = new URL(this.application.getRootUrl(), this.application.getRootPackageName());
            }
            return url;
        } catch (final MalformedURLException e)  {
            throw new InstallationException("Root url could not be prepared", e);
        }
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
            .append("number", this.number)
            .toString();
    }

    /**
     * Class used to store information of needed called scripts within an
     * application version.
     */
    private abstract class AbstractScript
    {

        /**
         * Script code to execute.
         */
        private final String code;

        /**
         * File name of the script (within the class path).
         */
        private final String fileName;

        /**
         * Name of called function.
         */
        private final String function;

        /**
         * Constructor to initialize a script.
         *
         * @param _code         script code
         * @param _fileName     script file name
         * @param _function     called function name
         */
        private AbstractScript(final String _code,
                               final String _fileName,
                               final String _function)
        {
            this.code = (_code == null) || ("".equals(_code.trim())) ? null : _code.trim();
            this.fileName = _fileName;
            this.function = _function;
        }

        /**
         * Executes this script.
         *
         * @param _userName name of logged in user
         * @param _password password of logged in user
         * @throws InstallationException on error
         */
        public abstract void execute(final String _userName,
                                     final String _password)
            throws InstallationException;

        /**
         * Getter method for instance variable {@link #code}.
         *
         * @return value of instance variable {@link #code}
         */
        public String getCode()
        {
            return this.code;
        }

        /**
         * Getter method for instance variable {@link #fileName}.
         *
         * @return value of instance variable {@link #fileName}
         */
        public String getFileName()
        {
            return this.fileName;
        }

        /**
         * Getter method for instance variable {@link #function}.
         *
         * @return value of instance variable {@link #function}
         */
        public String getFunction()
        {
            return this.function;
        }
    }

    /**
     *Script for groovy.
     */
    private final class GroovyScript
        extends ApplicationVersion.AbstractScript
    {
        /**
         * Constructor.
         * @param _code         code
         * @param _fileName     filename
         * @param _function     function
         */
        private GroovyScript(final String _code,
                             final String _fileName,
                             final String _function)
        {
            super(_code, _fileName, _function);
        }

        /**
         * {@inheritDoc}
         * @throws InstallationException if installation failed
         * TODO: it must be able to deactivate the CONTEXT
         */
        @Override
        public void execute(final String _userName,
                            final String _password)
            throws InstallationException
        {
            boolean commit = false;
            try {
                try {
                    Context.begin(_userName);
                } catch (final EFapsException e) {
                    throw new InstallationException("Tranaction could not be started", e);
                }
                final ClassLoader parent = getClass().getClassLoader();
                final EFapsClassLoader efapsClassLoader = new EFapsClassLoader(parent);
                final CompilerConfiguration config = new CompilerConfiguration();
                config.setClasspathList(ApplicationVersion.this.application.getClassPathElements());
                final GroovyClassLoader loader = new GroovyClassLoader(efapsClassLoader, config);
                if (getCode() != null) {
                    final Class<?> clazz = loader.parseClass(getCode());
                    groovy.lang.Script go;
                    try {
                        go = (groovy.lang.Script) clazz.newInstance();

                        final Binding binding = new Binding();
                        binding.setVariable("EFAPS_LOGGER", ApplicationVersion.LOG);
                        binding.setVariable("EFAPS_USERNAME", _userName);
                        binding.setVariable("EFAPS_PASSWORD", _userName);
                        binding.setVariable("EFAPS_ROOTURL", getCompleteRootUrl());
                        go.setBinding(binding);

                        final Object[] args = {};
                        go.invokeMethod("run", args);

                    } catch (final InstantiationException e) {
                        throw new InstallationException("InstantiationException in Groovy", e);
                    } catch (final IllegalAccessException e) {
                        throw new InstallationException("IllegalAccessException in Groovy", e);
                    }
                }
                try  {
                    Context.commit();
                } catch (final EFapsException e) {
                    throw new InstallationException("Tranaction could not be commited", e);
                }
                commit = true;
            } finally {
                if (!commit)  {
                    try {
                        Context.rollback();
                    } catch (final EFapsException e) {
                        throw new InstallationException("Tranaction could not be aborted", e);
                    }
                }
            }
        }
    }

    /**
     * Script for mozilla rhino (Javascript).
     */
    private final class RhinoScript
        extends ApplicationVersion.AbstractScript
    {
        /**
         * Constructor.
         *
         * @param _code         code
         * @param _fileName     filename
         * @param _function     function
         */
        private RhinoScript(final String _code,
                             final String _fileName,
                             final String _function)
        {
            super(_code, _fileName, _function);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void execute(final String _userName,
                            final String _password)
            throws InstallationException
        {
            try {
                // create new javascript context
                final org.mozilla.javascript.Context javaScriptContext = org.mozilla.javascript.Context.enter();

                final Scriptable scope = new ImporterTopLevel(javaScriptContext);

                // define the context javascript property
                ScriptableObject.putProperty(scope, "javaScriptContext", javaScriptContext);

                // define the scope javascript property
                ScriptableObject.putProperty(scope, "javaScriptScope", scope);

                ScriptableObject.putProperty(scope, "EFAPS_LOGGER",
                                org.mozilla.javascript.Context.javaToJS(ApplicationVersion.LOG, scope));
                ScriptableObject.putProperty(scope, "EFAPS_USERNAME",
                                org.mozilla.javascript.Context.javaToJS(_userName, scope));
                ScriptableObject.putProperty(scope, "EFAPS_PASSWORD",
                                org.mozilla.javascript.Context.javaToJS(_userName, scope));
                ScriptableObject.putProperty(scope,
                                             "EFAPS_ROOTURL",
                                             org.mozilla.javascript.Context.javaToJS(getCompleteRootUrl(), scope));

                // evaluate java script file (if defined)
                if (getFileName() != null) {
                    if (ApplicationVersion.LOG.isInfoEnabled()) {
                        ApplicationVersion.LOG.info("Execute script file '" + getFileName() + "'");
                    }
                    final Reader in = new InputStreamReader(
                            new URL(getCompleteRootUrl(), getFileName()).openStream());
                    javaScriptContext.evaluateReader(scope, in, getFileName(), 1, null);
                    in.close();
                }

                // evaluate script code (if defined)
                if (getCode() != null) {
                    javaScriptContext.evaluateReader(scope, new StringReader(getCode()),
                                    "Executing script code of version " + ApplicationVersion.this.number, 1, null);
                }

                // evaluate script defined through the reader
                if (getFunction() != null) {
                    if (ApplicationVersion.LOG.isInfoEnabled()) {
                        ApplicationVersion.LOG.info("Execute script function '" + getFunction() + "'");
                    }
                    javaScriptContext.evaluateReader(scope, new StringReader(getFunction()), getFunction(), 1, null);
                }
            } catch (final IOException e) {
                throw new InstallationException("IOException in RhinoScript", e);
            }
        }
    }
}
