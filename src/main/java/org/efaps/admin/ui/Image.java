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

package org.efaps.admin.ui;

import java.util.HashMap;
import java.util.Map;
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
 * @version $Id$ TODO:
 *          description
 */
public class Image
    extends AbstractUserInterfaceObject
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Menu.class);

    /**
     * Stores the mapping from type to tree menu.
     */
    private static final Map<Type, Image> TYPE2IMAGE = new HashMap<Type, Image>();

    /**
     * Stores all instances of class {@link Image}.
     *
     * @see #getCache
     */
    private static ImageCache CACHE = new ImageCache();

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
     * Sets the link properties for this object.
     *
     * @param _linkType type of the link property
     * @param _toId to id
     * @param _toType to type
     * @param _toName to name
     * @throws EFapsException on error
     */
    @Override
    protected void setLinkProperty(final Type _linkType,
                                   final long _toId,
                                   final Type _toType,
                                   final String _toName)
        throws EFapsException
    {
        if (_linkType.isKindOf(CIAdminUserInterface.LinkIsTypeIconFor.getType())) {
            final Type type = Type.get(_toId);
            if (type == null) {
                Image.LOG.error("Form '" + getName() + "' could not defined as type form for type '" + _toName
                                + "'! Type does not " + "exists!");
            } else {
                Image.TYPE2IMAGE.put(type, this);
            }
        } else {
            super.setLinkProperty(_linkType, _toId, _toType, _toName);
        }

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
        return Image.CACHE.get(_id);
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
        return Image.CACHE.get(_name);
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
        return Image.CACHE.get(_uuid);
    }

    /**
     * Returns for given type the type tree menu. If no type tree menu is
     * defined for the type, it is searched if for parent type a menu is
     * defined.
     *
     * @param _type type for which the type tree menu is searched
     * @return Image for type tree menu for given type if found; otherwise
     *         <code>null</code>.
     */
    public static Image getTypeIcon(final Type _type)
    {
        Image ret = Image.TYPE2IMAGE.get(_type);
        if ((ret == null) && (_type != null) && (_type.getParentType() != null)) {
            ret = Image.getTypeIcon(_type.getParentType());
        }
        return ret;
    }

    /**
     * Static getter method for the type hashtable {@link #CACHE}.
     *
     * @return value of static variable {@link #CACHE}
     */
    protected static AbstractUserInterfaceObjectCache<Image> getCache()
    {
        return Image.CACHE;
    }

    /**
     * Cache for Images.
     */
    private static class ImageCache
        extends AbstractUserInterfaceObjectCache<Image>
    {

        /**
         * Constructor.
         */
        protected ImageCache()
        {
            super(Image.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Type getType()
            throws EFapsException
        {
            return CIAdminUserInterface.Image.getType();
        }
    }
}
