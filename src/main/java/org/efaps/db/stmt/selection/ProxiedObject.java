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

package org.efaps.db.stmt.selection;

import org.efaps.db.stmt.selection.elements.IProxy;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ProxiedObject.
 */
public class ProxiedObject
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProxiedObject.class);

    /** The object. */
    private Object object;

    /** The proxy. */
    private IProxy proxy;

    /**
     * Sets the object.
     *
     * @param _object the object
     * @return the proxied object
     */
    public ProxiedObject setObject(final Object _object)
    {
        this.object = _object;
        return this;
    }

    /**
     * Sets the proxy.
     *
     * @param _proxy the proxy
     * @return the proxied object
     */
    public ProxiedObject setProxy(final IProxy _proxy)
    {
        this.proxy = _proxy;
        return this;
    }

    /**
     * Gets the object.
     *
     * @return the object
     */
    public Object getObject()
    {
        Object ret = null;
        try {
            ret =  this.proxy.getValue(this.object);
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }
        return ret;
    }
}
