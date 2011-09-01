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

package org.efaps.importer;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.xml.sax.Attributes;

/**
 * <p>Class to create InsertObjects with the Digester using a Constructor with
 * Parameters.</p>
 * <p>This Constructor is needed, because the Attribute "type" is needed from
 * the beginning to store the information about an insert object sorted by the
 * type. This information can't be changed later.</p>
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class InsertObjectBuilder
    implements ObjectCreationFactory
{
    /**
     * Returns a new instance of the insert object for given type defined in
     * <code>_attributes</code>.
     *
     * @param _attributes   attributes used to get the type for the insert
     *                      object
     * @return new insert object instance
     */
    public Object createObject(final Attributes _attributes)
    {
        return new InsertObject(_attributes.getValue("type"));
    }

    /**
     * The digester is not needed from the insert object factory and therefore
     * this method is only a stub.
     *
     * @return always <code>null</code>
     */
    public Digester getDigester()
    {
        return null;
    }

    /**
     * The digester is not needed from the insert object factory and therefore
     * this method is only a stub.
     *
     * @param _digester     not used
     */
    public void setDigester(final Digester _digester)
    {
    }
}
