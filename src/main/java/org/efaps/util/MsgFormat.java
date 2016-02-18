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

package org.efaps.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.text.ExtendedMessageFormat;
import org.apache.commons.lang3.text.FormatFactory;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.program.esjp.EFapsFormatFactory;
import org.efaps.db.Context;
import org.efaps.rest.EFapsResourceConfig;
import org.efaps.rest.EFapsResourceConfig.EFapsResourceFinder;
import org.glassfish.jersey.server.internal.scanning.AnnotationAcceptingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class MsgFormat
{

    /**
     * Singleton instance.
     */
    private static final MsgFormat MSGFORMAT = new MsgFormat();

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MsgFormat.class);

    /**
     * Is the instance initialized.
     */
    private boolean initialized = false;

    /**
     * Registry to be used for Extending the MsgFormat.
     */
    private final Map<String, FormatFactory> registry = new HashMap<>();

    /**
     * Singelton wanted.
     */
    private MsgFormat()
    {
    }

    /**
     * Initialize and scan for root resource and provider classes using a
     * scanner.
     *
     * @throws EFapsException on error
     * @return MsgFormat instance
     */
    private MsgFormat init()
        throws EFapsException
    {
        if (!this.initialized) {
            @SuppressWarnings("unchecked")
            final AnnotationAcceptingListener asl = new AnnotationAcceptingListener(EFapsClassLoader.getInstance(),
                            EFapsFormatFactory.class);
            final EFapsResourceFinder resourceFinder = new EFapsResourceConfig.EFapsResourceFinder();
            while (resourceFinder.hasNext()) {
                final String next = resourceFinder.next();
                if (asl.accept(next)) {
                    final InputStream in = resourceFinder.open();
                    try {
                        MsgFormat.LOG.debug("Scanning '{}' for annotations.", next);
                        asl.process(next, in);
                    } catch (final IOException e) {
                        MsgFormat.LOG.warn("Cannot process '{}'", next);
                    } finally {
                        try {
                            in.close();
                        } catch (final IOException ex) {
                            MsgFormat.LOG.trace("Error closing resource stream.", ex);
                        }
                    }
                }
            }
            for (final Class<?> clazz : asl.getAnnotatedClasses()) {
                try {
                    final FormatFactory factory = (FormatFactory) clazz.newInstance();
                    final EFapsFormatFactory ano = clazz.getAnnotation(EFapsFormatFactory.class);
                    this.registry.put(ano.name(), factory);
                } catch (final InstantiationException | IllegalAccessException e) {
                    LOG.error("Catched error on instantiotion", e);
                }
            }
            LOG.info("registered FormatFactories: {}", this.registry);
            this.initialized = true;
        }
        return this;
    }

    /**
     * Getter method for the instance variable {@link #registry}.
     *
     * @return value of instance variable {@link #registry}
     */
    public Map<String, FormatFactory> getRegistry()
    {
        return MapUtils.unmodifiableMap(this.registry);
    }

    /**
     * Get the MsgFormat.
     * @return MesgFormat
     * @throws EFapsException on error
     */
    public static MsgFormat get()
        throws EFapsException
    {
        return MsgFormat.MSGFORMAT.init();
    }

    /**
     * @param _pattern pattern to apply
     * @return ExtendedMessageFormat
     * @throws EFapsException on error
     */
    public static ExtendedMessageFormat getFormat(final String _pattern)
        throws EFapsException
    {
        return getFormat(_pattern, Context.getThreadContext().getLocale());
    }

    /**
     * @param _pattern pattern to apply
     * @param _locale Locale
     * @return ExtendedMessageFormat
     * @throws EFapsException on error
     */
    public static ExtendedMessageFormat getFormat(final String _pattern,
                                                  final Locale _locale)
        throws EFapsException
    {
        return new ExtendedMessageFormat(_pattern, _locale, get().registry);
    }
}
