/*
 * Copyright 2003 - 2018 The eFaps Team
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

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.efaps.admin.program.esjp.EsjpScanner;
import org.efaps.util.EFapsException;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
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
     * Cached classes.
     */
    private final Set<Class<?>> cachedClasses = new HashSet<>();

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
        LOG.info("Scanning esjps for REST implementations");
        try {
            registerClasses(new EsjpScanner().scan(Path.class, Provider.class));
        } catch (final EFapsException e) {
            LOG.error("Catched EFapsException", e);
        }
        registerClasses(Compile.class);
        registerClasses(Update.class);
        registerClasses(RestEQLInvoker.class);
        registerClasses(RestContext.class);
        registerClasses(Search.class);
        registerClasses(ObjectMapperResolver.class);
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
        final Set<Class<?>> s = new HashSet<>();
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
        final Set<Class<?>> classesToRemove = new HashSet<>();
        final Set<Class<?>> classesToAdd = new HashSet<>();

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
}
