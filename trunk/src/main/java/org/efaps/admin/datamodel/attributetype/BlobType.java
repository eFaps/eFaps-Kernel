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
import org.efaps.db.query.CachedResult;

/**
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class BlobType
    extends AbstractFileType
{
    public Object readValue(final Attribute _attribute,
                            final CachedResult _rs,
                            final List<Integer> _index)
        throws Exception
    {
        return _rs.getString(_index.get(0).intValue());
    }
}
