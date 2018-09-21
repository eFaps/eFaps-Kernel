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

package org.efaps.admin.program.esjp;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.Vfs.Dir;
import org.reflections.vfs.Vfs.File;
import org.reflections.vfs.Vfs.UrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to scan esjps for annotations.
 */
public class EsjpScanner
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EsjpScanner.class);

    static {
        Vfs.setDefaultURLTypes(Collections.singletonList(new UrlType()
        {
            @Override
            public boolean matches(final URL _url)
                throws Exception
            {
                return true;
            }

            @Override
            public Dir createDir(final URL _url)
                throws Exception
            {
                return new EsjpDir();
            }
        }));
    }

    @SafeVarargs
    public final Set<Class<?>> scan(final Class<? extends Annotation>... _annotations)
        throws EFapsException
    {
        final Set<Class<?>> ret = new HashSet<>();
        try {
            final ConfigurationBuilder configuration = new ConfigurationBuilder()
                            .setUrls(new URL("file://"))
                            .addClassLoader(EFapsClassLoader.getInstance())
                            .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner());
            // in case of jboss the transaction filter is not executed
            // before the method is called therefore a Context must be opened
            boolean contextStarted = false;
            if (!Context.isThreadActive()) {
                Context.begin(null, Context.Inheritance.Local);
                contextStarted = true;
            }
            final Reflections reflections = new Reflections(configuration);
            for (final Class<? extends Annotation> annotation : _annotations) {
                ret.addAll(reflections.getTypesAnnotatedWith(annotation));
            }
            if (contextStarted) {
                Context.rollback();
            }
        } catch (final MalformedURLException e) {
            LOG.error("Catched MalformedURLException", e);
        }
        return ret;
    }

    public static class EsjpDir
        implements Vfs.Dir
    {

        @Override
        public String getPath()
        {
            return "";
        }

        @Override
        public Iterable<File> getFiles()
        {
            final Set<File> files = new HashSet<>();
            try {
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.JavaClass);
                final InstanceQuery query = queryBldr.getQuery();
                query.executeWithoutAccessCheck();
                while (query.next()) {
                    files.add(new EsjpFile(query.getCurrentValue()));
                }
            } catch (final EFapsException e) {
                e.printStackTrace();
            }
            return files;
        }

        @Override
        public void close()
        {
            // not needed
        }
    }

    public static class EsjpFile
        implements Vfs.File
    {

        private final Instance instance;
        private InputStream in;
        private String name;

        public EsjpFile(final Instance _instance)
        {
            this.instance = _instance;
        }

        private void init()
        {
            if (this.name == null) {
                try {
                    final Checkout checkout = new Checkout(this.instance);
                    this.in = checkout.execute();
                    this.name = checkout.getFileName() + ".class";
                    LOG.debug("Scanned: {}", this.name );
                } catch (final EFapsException e) {
                    LOG.error("Catchec EFapsException", e);
                }
            }
        }

        @Override
        public String getName()
        {
            init();
            return this.name;
        }

        @Override
        public String getRelativePath()
        {
            init();
            return this.name;
        }

        @Override
        public InputStream openInputStream()
            throws IOException
        {
            init();
            return this.in;
        }
    }
}
