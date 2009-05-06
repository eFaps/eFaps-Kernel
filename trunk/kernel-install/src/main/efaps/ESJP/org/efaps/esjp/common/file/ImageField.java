/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.esjp.common.file;

import java.util.Map;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("09afa252-3548-4987-870a-616186351690")
@EFapsRevision("$Rev$")
public class ImageField {


public Return getViewFieldValueUI(final Parameter _parameter) {
    final Instance instance = _parameter.getCallInstance();
    final Map<?, ?> properties =
                        (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
    String height = "auto";
    String width = "auto";
    if (properties.get("height") != null) {
      height = (String) properties.get("height");
    }
    if (properties.get("width") != null) {
      width = (String) properties.get("width");
    }
    final StringBuilder ret = new StringBuilder();
    ret.append("<img style=\"height:").append(height).append("; width:")
       .append(width).append(";\" ")
       .append("src=\"/eFaps/servlet/checkout?oid=")
       .append(instance.getOid()).append("\" ")
       .append("/>");

    final Return retVal = new Return();
    retVal.put(ReturnValues.SNIPLETT, ret);
    return retVal;
  }
}
