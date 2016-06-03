/*
 * Copyright 2003 - 2016 The eFaps Team
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


package org.efaps.admin.program.esjp;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.efaps.rest.EFapsResourceConfig;
import org.efaps.rest.EFapsResourceConfig.EFapsResourceFinder;
import org.efaps.util.EFapsException;
import org.glassfish.jersey.server.internal.scanning.AnnotationAcceptingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public final class Listener
{
    /**
     * Singleton instance.
     */
    private static Listener LISTENER = new Listener();

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Listener.class);

    /**
     * Classes found by the scanner.
     */
    private final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

    /**
     * Is the instance initialized.
     */
    private boolean initialized = false;

    /**
     * Constructor.
     */
    private Listener()
    {
        // Singelton
    }

    /**
     * Initialize and scan for root resource and provider classes using a scanner.
     *
     * @throws EFapsException on error
     */
    private void init()
        throws EFapsException
    {
        if (!this.initialized) {
            @SuppressWarnings("unchecked")
            final AnnotationAcceptingListener asl = new AnnotationAcceptingListener(EFapsClassLoader.getInstance(),
                            EFapsListener.class);
            try (final EFapsResourceFinder resourceFinder = new EFapsResourceConfig.EFapsResourceFinder()) {
                while (resourceFinder.hasNext()) {
                    final String next = resourceFinder.next();
                    if (asl.accept(next)) {
                        final InputStream in = resourceFinder.open();
                        try {
                            Listener.LOG.debug("Scanning '{}' for annotations.", next);
                            asl.process(next, in);
                        } catch (final IOException e) {
                            Listener.LOG.warn("Cannot process '{}'", next);
                        } finally {
                            try {
                                in.close();
                            } catch (final IOException ex) {
                                Listener.LOG.trace("Error closing resource stream.", ex);
                            }
                        }
                    }
                }
                this.classes.clear();
                this.classes.addAll(asl.getAnnotatedClasses());

                if (Listener.LOG.isInfoEnabled() && !this.classes.isEmpty()) {
                    logClasses("Listener classes found:", this.classes);
                }
                this.initialized = true;
            }
        }
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
        Listener.LOG.info(b.toString());
    }

    /**
     * @return the singleton JmsResourceConfig instance
     */
    public static Listener get()
    {
        return Listener.LISTENER;
    }

    /**
     * @param _class interface class
     * @param <T> class extending the interface
     * @return lst of classes found
     * @throws EFapsException on error
     */
    public <T extends IEsjpListener> List<T> invoke(final Class<? extends IEsjpListener> _class)
        throws EFapsException
    {
        init();
        final List<T> ret = new ArrayList<T>();
        for (final Class<?> clazz : this.classes) {
            if (_class.isAssignableFrom(clazz)) {
                try {
                    boolean hasConst = false;
                    final Constructor<?>[] constructors = clazz.getConstructors();
                    for (final Constructor<?> constructor : constructors) {
                        if (constructor.getParameterTypes().length == 0) {
                            hasConst = true;
                        }
                    }
                    if (hasConst) {
                        @SuppressWarnings("unchecked")
                        final T obj = (T) clazz.newInstance();
                        ret.add(obj);
                        Listener.LOG.debug("Instancated class: {}", obj);
                    } else {
                        @SuppressWarnings("unchecked")
                        final T obj = (T) clazz.getMethod("get").invoke(null);
                        ret.add(obj);
                        Listener.LOG.debug("Usted static get for class: {}", obj);
                    }
                } catch (final InstantiationException | IllegalAccessException e) {
                    throw new EFapsException("Could not get.", e);
                }  catch (final SecurityException e) {
                    throw new EFapsException("Could not get.", e);
                } catch (final IllegalArgumentException e) {
                    throw new EFapsException("Could not get.", e);
                } catch (final InvocationTargetException e) {
                    throw new EFapsException("Could not get.", e);
                } catch (final NoSuchMethodException e) {
                    throw new EFapsException("Could not get.", e);
                }
            }
        }
        Collections.sort(ret, new Comparator<IEsjpListener>()
        {
            @Override
            public int compare(final IEsjpListener _o1,
                               final IEsjpListener _o2)
            {
                return Integer.compare(_o1.getWeight(), _o2.getWeight());
            }
        });
        return Collections.unmodifiableList(ret);
    }
}
