/*
 * Copyright 2003 - 2007 The eFaps Team
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
 * Class to create OrderObjects with the Digester using a Constructor with
 * Parameters.
 * 
 * This Constructor is needed, because the Attribute "type" and "direction" are
 * needed from the beginning.
 * 
 * @author jmo
 * 
 */
public class OrderObjectFactory implements ObjectCreationFactory {

  public Object createObject(final Attributes _attributes) {
    OrderObject ret = new OrderObject(_attributes.getValue("type"), _attributes
        .getValue("direction"));
    return ret;
  }

  public Digester getDigester() {
    return null;
  }

  public void setDigester(Digester digester) {
  }

}
