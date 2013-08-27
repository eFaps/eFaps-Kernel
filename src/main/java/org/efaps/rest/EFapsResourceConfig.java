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

package org.efaps.rest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.core.spi.scanning.Scanner;
import com.sun.jersey.core.spi.scanning.ScannerListener;
import com.sun.jersey.core.util.Closing;
import com.sun.jersey.spi.container.ReloadListener;
import com.sun.jersey.spi.scanning.AnnotationScannerListener;
import com.sun.jersey.spi.scanning.PathProviderScannerListener;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: EFapsResourceConfig.java 9319 2013-04-30 18:30:17Z
 *          jan@moxter.net $
 */
public class EFapsResourceConfig
    extends DefaultResourceConfig
    implements ReloadListener
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsResourceConfig.class);

    /**
     * Scanner for the class files.
     */
    private final Scanner scanner = new EfapsResourceScanner();

    /**
     * Cached classes.
     */
    private final Set<Class<?>> cachedClasses = new HashSet<Class<?>>();

    /**
     * Constructor.
     */
    public EFapsResourceConfig()
    {
        init();
    }

    /**
     * Initialize and scan for root resource and provider classes using a
     * scanner.
     */
    public void init()
    {
        final AnnotationScannerListener asl = new PathProviderScannerListener(EFapsClassLoader.getInstance());
        this.scanner.scan(asl);
        getClasses().addAll(asl.getAnnotatedClasses());
        getClasses().add(Compile.class);
        getClasses().add(Update.class);
        if (EFapsResourceConfig.LOG.isInfoEnabled() && !getClasses().isEmpty()) {
            final Set<Class<?>> rootResourceClasses = get(Path.class);
            if (rootResourceClasses.isEmpty()) {
                EFapsResourceConfig.LOG.info("No root resource classes found.");
            } else {
                logClasses("Root resource classes found:", rootResourceClasses);
            }

            final Set<Class<?>> providerClasses = get(Provider.class);
            if (providerClasses.isEmpty()) {
                EFapsResourceConfig.LOG.info("No provider classes found.");
            } else {
                logClasses("Provider classes found:", providerClasses);
            }
        }
        this.cachedClasses.clear();
        this.cachedClasses.addAll(getClasses());
    }

    /**
     * Perform a new search for resource classes and provider classes.
     */
    @Override
    public void onReload()
    {
        final Set<Class<?>> classesToRemove = new HashSet<Class<?>>();
        final Set<Class<?>> classesToAdd = new HashSet<Class<?>>();

        for (final Class<?> c : getClasses()) {
            if (!this.cachedClasses.contains(c)) {
                classesToAdd.add(c);
            }
        }

        for (final Class<?> c : this.cachedClasses) {
            if (!getClasses().contains(c)) {
                classesToRemove.add(c);
            }
        }

        getClasses().clear();

        init();

        getClasses().addAll(classesToAdd);
        getClasses().removeAll(classesToRemove);
    }

    /**
     * @param _annoclass Annotation class
     * @return set of classes implementing Annotation class
     */
    private Set<Class<?>> get(final Class<? extends Annotation> _annoclass)
    {
        final Set<Class<?>> s = new HashSet<Class<?>>();
        for (final Class<?> c : getClasses()) {
            if (c.isAnnotationPresent(_annoclass)) {
                s.add(c);
            }
        }
        return s;
    }

    /**
     * @param _text text to log
     * @param _classes classes to log
     */
    private void logClasses(final String _text,
                            final Set<Class<?>> _classes)
    {
        final StringBuilder b = new StringBuilder();
        b.append(_text);
        for (final Class<?> c : _classes) {
            b.append('\n').append("  ").append(c);
        }
        EFapsResourceConfig.LOG.info(b.toString());
    }

    /**
     * Scanner for esjps.
     */
    public static class EfapsResourceScanner
        implements Scanner
    {

        /**
         * Scan the esjps for annotations.
         *
         * @param _sl scan listener
         */
        @Override
        public void scan(final ScannerListener _sl)
        {
            try {
                // in case of jboss the transaction filter is not executed
                // before the
                // init method is called therefore a Context must be opened
                boolean contextStarted = false;
                if (!Context.isThreadActive()) {
                    Context.begin(null, false);
                    contextStarted = true;
                }
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.JavaClass);
                final InstanceQuery query = queryBldr.getQuery();
                query.executeWithoutAccessCheck();
                while (query.next()) {
                    final Checkout checkout = new Checkout(query.getCurrentValue());
                    final InputStream in = checkout.execute();
                    final String fileName = checkout.getFileName();
                    new Closing(new BufferedInputStream(in)).f(new Closing.Closure()
                    {
                        @Override
                        public void f(final InputStream _in)
                        {
                            EFapsResourceConfig.LOG.debug("Scanning '{}' for annotations.", fileName);
                            try {
                                _sl.onProcess(fileName, _in);
                            } catch (final IOException e) {
                               EFapsResourceConfig.LOG.error("Error on reading file {}", fileName);
                            }
                        }
                    });
                }
                if (contextStarted) {
                    Context.rollback();
                }
            } catch (final IOException e) {
                EFapsResourceConfig.LOG.error("IO error when scanning file ", e);
            } catch (final EFapsException e) {
                EFapsResourceConfig.LOG.error("EFapsException when scanning file ", e);
            }
        }
    }
}
