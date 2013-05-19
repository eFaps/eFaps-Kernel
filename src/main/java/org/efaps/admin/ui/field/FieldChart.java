/*
 * Copyright 2003 - 2013 The eFaps Team
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


package org.efaps.admin.ui.field;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldChart
    extends Field
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * @param _id id of the field group
     * @param _uuid UUID of the field group
     * @param _name name of the field group
     */
    public FieldChart(final long _id,
                      final String _uuid,
                      final String _name)
    {
        super(_id, _uuid, _name);
    }
}
