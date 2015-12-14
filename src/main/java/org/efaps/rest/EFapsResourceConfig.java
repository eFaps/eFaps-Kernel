/*
 * Copyright 2003 - 2015 The eFaps Team
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
 */

package org.efaps.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ResourceFinder;
import org.glassfish.jersey.server.internal.scanning.AnnotationAcceptingListener;
import org.glassfish.jersey.server.spi.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class EFapsResourceConfig
    extends ResourceConfig
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsResourceConfig.class);

    /**
     * ResourceFinder for the class files.
     */
    private final ResourceFinder resourceFinder = new EFapsResourceFinder();

    /**
     * Cached classes.
     */
    private final Set<Class<?>> cachedClasses = new HashSet<Class<?>>();

    /**
     * Constructor.
     */
    public EFapsResourceConfig()
    {
        super(MultiPartFeature.class);
        init();
    }

    /**
     * Initialize and scan for root resource and provider classes using a
     * scanner.
     */
    public void init()
    {
        final AnnotationAcceptingListener asl = AnnotationAcceptingListener
                        .newJaxrsResourceAndProviderListener(EFapsClassLoader.getInstance());
        while (this.resourceFinder.hasNext()) {
            final String next = this.resourceFinder.next();
            if (asl.accept(next)) {
                final InputStream in = this.resourceFinder.open();
                try {
                    EFapsResourceConfig.LOG.debug("Scanning '{}' for annotations.", next);
                    asl.process(next, in);
                } catch (final IOException e) {
                    EFapsResourceConfig.LOG.warn("Cannot process '{}'", next);
                } finally {
                    try {
                        in.close();
                    } catch (final IOException ex) {
                        EFapsResourceConfig.LOG.trace("Error closing resource stream.", ex);
                    }
                }
            }
        }
        registerClasses(asl.getAnnotatedClasses());
        registerClasses(Compile.class);
        registerClasses(Update.class);
        registerClasses(RestEQLInvoker.class);
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
     * Perform a new search for resource classes and provider classes.
     * @param _container Container
     */
    public void onReload(final Container _container)
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
     * Finder for esjps.
     */
    public static class EFapsResourceFinder
        implements ResourceFinder
    {
        /**
         * underlying Iterator.
         */
        private Iterator<Instance> iter;

        /**
         * Current inputStream.
         */
        private InputStream in;

        /**
         * Scan the esjps for annotations.
         */
        private void init()
        {
            final List<Instance> instances = new ArrayList<Instance>();
            try {
                // in case of jboss the transaction filter is not executed
                // before the
                // init method is called therefore a Context must be opened
                boolean contextStarted = false;
                if (!Context.isThreadActive()) {
                    Context.begin(null, Context.Inheritance.Local);
                    contextStarted = true;
                }
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.JavaClass);
                final InstanceQuery query = queryBldr.getQuery();
                query.executeWithoutAccessCheck();
                while (query.next()) {
                    instances.add(query.getCurrentValue());
                }
                if (contextStarted) {
                    Context.rollback();
                }
            } catch (final EFapsException e) {
                EFapsResourceConfig.LOG.error("EFapsException when scanning file ", e);
            }
            this.iter = instances.iterator();
        }

        /**
         * @param _instance instacne of be read.
         * @return the name of the class file
         */
        private String readCurrent(final Instance _instance)
        {
            String ret = "";
            try {
                // in case of jboss the transaction filter is not executed
                // before the
                // init method is called therefore a Context must be opened
                boolean contextStarted = false;
                if (!Context.isThreadActive()) {
                    Context.begin(null, Context.Inheritance.Local);
                    contextStarted = true;
                }

                final Checkout checkout = new Checkout(_instance);
                this.in = checkout.execute();
                ret = checkout.getFileName() + ".class";
                if (contextStarted) {
                    Context.rollback();
                }
            } catch (final EFapsException e) {
                EFapsResourceConfig.LOG.error("EFapsException when scanning file ", e);
            }
            return ret;
        }

        @Override
        public boolean hasNext()
        {
            if (this.iter == null) {
                init();
            }
            return this.iter.hasNext();
        }

        @Override
        public String next()
        {
            return readCurrent(this.iter.next());
        }

        @Override
        public InputStream open()
        {
            return this.in;
        }

        @Override
        public void reset()
        {
            init();
        }

        @Override
        public void remove()
        {
            // nothing to do here
        }

        @Override
        public void close()
        {
            // nothing to do here
        }
    }
}
