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

package org.efaps.jms;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.rest.EFapsResourceConfig;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.spi.scanning.Scanner;
import com.sun.jersey.spi.scanning.AnnotationScannerListener;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JmsResourceConfig
{
    /**
     * Singleton instance.
     */
    private static JmsResourceConfig CONFIG = new JmsResourceConfig();

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JmsResourceConfig.class);

    /**
     * Classes found by the scanner.
     */
    private final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

    /**
     * Constructor.
     */
    private JmsResourceConfig()
    {
        // Singelton
    }

    /**
     * Getter method for the instance variable {@link #classes}.
     *
     * @return value of instance variable {@link #classes}
     */
    public Set<Class<?>> getClasses()
    {
        return this.classes;
    }

    /**
     * Initialize and scan for root resource and provider classes using a scanner.
     *
     * @throws EFapsException on error
     */
    protected void init()
        throws EFapsException
    {
        @SuppressWarnings("unchecked")
        final AnnotationScannerListener asl = new AnnotationScannerListener(
                        new EFapsClassLoader(this.getClass().getClassLoader()),
                            XmlAccessorType.class,
                            XmlType.class,
                            XmlElementWrapper.class,
                            XmlElementRef.class,
                            XmlRootElement.class,
                            XmlAttribute.class);
        try {
            final Scanner scanner = EFapsResourceConfig.EfapsResourceScanner.class.newInstance();
            scanner.scan(asl);
        } catch (final InstantiationException e) {
            throw new EFapsException("InstantiationException", e);
        } catch (final IllegalAccessException e) {
            throw new EFapsException("IllegalAccessException", e);
        }
        this.classes.clear();
        this.classes.addAll(asl.getAnnotatedClasses());

        if (JmsResourceConfig.LOG.isInfoEnabled() && !getClasses().isEmpty()) {
            logClasses("Jms classes found:", getClasses());
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
        JmsResourceConfig.LOG.info(b.toString());
    }

    /**
     * @return the singleton JmsResourceConfig instance
     */
    public static JmsResourceConfig getResourceConfig()
    {
        return JmsResourceConfig.CONFIG;
    }
}
