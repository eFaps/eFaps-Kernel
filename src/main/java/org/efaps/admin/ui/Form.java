/*
 * Copyright 2003 - 2012 The eFaps Team
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps TEam
 * @version $Id$ TODO:
 *          description
 */
public class Form
    extends AbstractCollection
{

    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(Form.class);

    /**
     * Stores the mapping from type to tree menu.
     */
    private static final Map<Type, Form> TYPE2FORMS = new HashMap<Type, Form>();


    /**
     * @param _id       id
     * @param _uuid     UUID
     * @param _name     name
     */
    public Form(final Long _id,
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
        if (_linkType.isKindOf(CIAdminUserInterface.LinkIsTypeFormFor.getType())) {
            final Type type = Type.get(_toId);
            if (type == null) {
                Form.LOG.error("Form '" + getName() + "' could not defined as type form for type '" + _toName
                                + "'! Type does not " + "exists!");
            } else {
                Form.TYPE2FORMS.put(type, this);
            }
        } else {
            super.setLinkProperty(_linkType, _toId, _toType, _toName);
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class {@link Form}
     * .
     *
     * @param _id id to search in the cache
     * @return instance of class {@link Form}
     * @see #getCache
     */
    public static Form get(final long _id)
    {
        return AbstractUserInterfaceObject.<Form>get(_id, Form.class, CIAdminUserInterface.Form.getType());
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Form}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link Form}
     * @see #getCache
     */
    public static Form get(final String _name)
    {
        return AbstractUserInterfaceObject.<Form>get(_name, Form.class, CIAdminUserInterface.Form.getType());
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Form}.
     *
     * @param _uuid UUID to search in the cache
     * @return instance of class {@link Form}
     * @see #getCache
     */
    public static Form get(final UUID _uuid)
    {
        return AbstractUserInterfaceObject.<Form>get(_uuid, Form.class, CIAdminUserInterface.Form.getType());
    }

    /**
     * Returns for given type the type form. If no type form is defined for the
     * type, it is searched if for parent type a menu is defined.
     *
     * @param _type type for which the type form is searched
     * @return type form for given type if found; otherwise <code>null</code>.
     */
    public static Form getTypeForm(final Type _type)
        throws EFapsException
    {
        return _type.getTypeForm();
    }
}
