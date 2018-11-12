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

package org.efaps.db.stmt.selection.elements;

import org.efaps.admin.datamodel.Status;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyElement
    extends AbstractElement<KeyElement>
    implements IAuxillary
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(KeyElement.class);

    @Override
    public KeyElement getThis()
    {
        return this;
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        Object object = _row == null ? null : _row[0];
        if (object != null) {
            if (object instanceof Status) {
                object = ((Status) object).getKey();
            } else {
                LOG.warn("KeyElement was called with unexpected Object: {}", object);
            }
        }
        return object;
    }
}
