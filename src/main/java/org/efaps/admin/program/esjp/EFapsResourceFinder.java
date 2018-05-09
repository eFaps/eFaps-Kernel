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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.glassfish.jersey.server.ResourceFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EFapsResourceFinder
    implements ResourceFinder
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsResourceFinder.class);

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
        final List<Instance> instances = new ArrayList<>();
        try {
            // in case of jboss the transaction filter is not executed
            // before the
            // init method is called therefore a Context must be opened
            boolean contextStarted = false;
            if (!Context.isThreadActive()) {
                Context.begin(null, Context.Inheritance.Local);
                contextStarted = true;
            }
            if (CIAdminProgram.JavaClass.getType() != null) {
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.JavaClass);
                final InstanceQuery query = queryBldr.getQuery();
                query.executeWithoutAccessCheck();
                while (query.next()) {
                    instances.add(query.getCurrentValue());
                }
            }
            if (contextStarted) {
                Context.rollback();
            }
        } catch (final EFapsException e) {
            EFapsResourceFinder.LOG.error("EFapsException when scanning file ", e);
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
            EFapsResourceFinder.LOG.error("EFapsException when scanning file ", e);
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
