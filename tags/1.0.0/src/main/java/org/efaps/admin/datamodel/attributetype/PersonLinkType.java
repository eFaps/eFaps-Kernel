/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.admin.datamodel.attributetype;

import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.db.query.CachedResult;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 * TODO: description
 */
public class PersonLinkType
    extends AbstractLinkType
{
    /**
     * @param _rs
     * @param _index
     */
    @Override()
    public Object readValue(final Attribute _attribute,
                            final CachedResult _rs,
                            final List<Integer> _indexes)
    {
        Object ret = null;

        final Object userId = super.readValue(_attribute, _rs, _indexes);
        if (userId != null) {
            try {
                long id = 0;
                if (userId instanceof Number) {
                    id = ((Number) userId).longValue();
                } else if (userId != null) {
                    id = Long.parseLong(userId.toString());
                }
                ret = Person.get(id);
                if (ret == null) {
                    ret = Role.get(id);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#readValue(java.util.List)
     * @param _objectList List of Objects
     * @return DateTime
     * @throws EFapsException on error
     * TODO: throw error if more than one value is given
     */
    @Override()
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
        throws EFapsException
    {
        Object ret = null;
        final Object obj = _objectList.get(0);
        if (obj != null) {
            long id = 0;
            if (obj instanceof Number) {
                id = ((Number) obj).longValue();
            } else if (obj != null) {
                id = Long.parseLong(obj.toString());
            }
            ret = Person.get(id);
            if (ret == null) {
                ret = Role.get(id);
            }
        }
        return ret;
    }
}
