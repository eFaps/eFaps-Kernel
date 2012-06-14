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
 * Last Changed By: $Author: jmox\n$
 */

package org.efaps.importer;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.ObjectCreationFactory;
import org.xml.sax.Attributes;

/**
 * <p>Class to create order objects with the digester using a constructor with
 * parameters.</p>
 * <p>This Constructor is needed, because the attribute "type" and "direction"
 * are needed from the beginning.</p>
 *
 * @author The eFaps Team
 */
public class OrderObjectBuilder
    implements ObjectCreationFactory<OrderObject>
{
    /**
     * Returns a new instance of the order object for given type and direction
     * defined in <code>_attributes</code>.
     *
     * @param _attributes   attributes used to get the type for the order
     *                      object
     * @return new order object instance
     */
    public OrderObject createObject(final Attributes _attributes)
    {
        return new OrderObject(_attributes.getValue("type"), _attributes.getValue("direction"));
    }

    /**
     * The digester is not needed from the order object factory and therefore
     * this method is only a stub.
     *
     * @return always <code>null</code>
     */
    public Digester getDigester()
    {
        return null;
    }

    /**
     * The digester is not needed from the order object factory and therefore
     * this method is only a stub.
     *
     * @param _digester     not used
     */
    public void setDigester(final Digester _digester)
    {
    }
}
