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

package org.efaps.admin.ui;

import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.util.EFapsException;
import org.efaps.util.RequestHandler;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * TODO:
 *          description
 */
public class Image
    extends AbstractUserInterfaceObject
{
    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(Image.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor to set the id and name of the command object.
     *
     * @param _id id of the command to set
     * @param _uuid uuid of the command to set
     * @param _name name of the command to set
     */
    public Image(final Long _id,
                 final String _uuid,
                 final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * Returns the URL of this image.
     *
     * @return URL of this image
     */
    public String getUrl()
    {
        return RequestHandler.replaceMacrosInUrl(RequestHandler.URL_IMAGE + getName());
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Image}.
     *
     * @param _id id to search in the cache
     * @return instance of class {@link Image}
     * @throws CacheReloadException on error
     * @see #getCache
     */
    public static Image get(final long _id)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Image>get(_id, Image.class, CIAdminUserInterface.Image.getType());
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Image}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link Image}
     * @throws CacheReloadException on error
     * @see #getCache
     */
    public static Image get(final String _name)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Image>get(_name, Image.class, CIAdminUserInterface.Image.getType());
    }

    /**
     * Returns for given parameter <i>UUID</i> the instance of class
     * {@link Image}.
     *
     * @param _uuid UUID to search in the cache
     * @return instance of class {@link Image}
     * @throws CacheReloadException on error
     * @see #getCache
     */
    public static Image get(final UUID _uuid)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Image>get(_uuid, Image.class, CIAdminUserInterface.Image.getType());
    }

    /**
     * Returns for given type the type tree menu. If no type tree menu is
     * defined for the type, it is searched if for parent type a menu is
     * defined.
     *
     * @param _type type for which the type tree menu is searched
     * @return Image for type tree menu for given type if found; otherwise
     *         <code>null</code>.
     * @throws EFapsException on error
     */
    public static Image getTypeIcon(final Type _type)
        throws EFapsException
    {
        Image ret = null;
        if (_type != null) {
            ret = _type.getTypeIcon();
        }
        return ret;
    }

    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof Image) {
            ret = ((Image) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return  Long.valueOf(getId()).intValue();
    }
}
