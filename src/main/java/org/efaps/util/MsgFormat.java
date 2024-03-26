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
package org.efaps.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.text.ExtendedMessageFormat;
import org.apache.commons.text.FormatFactory;
import org.efaps.admin.program.esjp.EFapsFormatFactory;
import org.efaps.admin.program.esjp.EsjpScanner;
import org.efaps.db.Context;
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
            for (final Class<?> clazz : new EsjpScanner().scan(EFapsFormatFactory.class)) {
                try {
                    final FormatFactory factory = (FormatFactory) clazz.newInstance();
                    final EFapsFormatFactory ano = clazz.getAnnotation(EFapsFormatFactory.class);
                    this.registry.put(ano.name(), factory);
                } catch (final InstantiationException | IllegalAccessException e) {
                    MsgFormat.LOG.error("Catched error on instantiotion", e);
                }
            }
            MsgFormat.LOG.info("registered FormatFactories: {}", this.registry);
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
        return MsgFormat.getFormat(_pattern, Context.getThreadContext().getLocale());
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
        return new ExtendedMessageFormat(_pattern, _locale, MsgFormat.get().registry);
    }
}
