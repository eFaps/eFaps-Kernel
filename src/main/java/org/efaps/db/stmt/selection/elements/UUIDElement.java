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
package org.efaps.db.stmt.selection.elements;

import org.efaps.admin.datamodel.Type;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UUIDElement
    extends AbstractElement<UUIDElement>
    implements IAuxillary
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(UUIDElement.class);

    @Override
    public UUIDElement getThis()
    {
        return this;
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        Object object = _row == null ? null : _row[0];
        if (object != null) {
            if (object instanceof Type) {
                object = ((Type) object).getUUID();
            } else {
                LOG.warn("UUIDElement was called with unexpected Object: {}", object);
            }
        }
        return object;
    }
}
