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

package org.efaps.esjp.ui.xml;

import java.io.File;
import java.util.HashMap;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ui.wicket.util.MimeTypes;
import org.efaps.ui.xml.XMLExport;
import org.efaps.util.EFapsException;

/**
 * TODO description!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("fd1ecee1-a882-4fbe-8b3c-5e5c3ed4d6b7")
@EFapsRevision("$Rev$")
public class CreateXML implements EventExecution
{

  public Return execute(final Parameter _parameter) throws EFapsException
  {
    final Return ret = new Return();
    final XMLExport export = new XMLExport(_parameter.get(ParameterValues.OTHERS));
    final String mimetype =
        (String) ((HashMap<?, ?>) _parameter.get(ParameterValues.PROPERTIES))
            .get("MimeType");

    export.generateDocument(MimeTypes.getMimeTypeByEnding(mimetype));
    final File file = export.getFile();
    ret.put(ReturnValues.VALUES, file);

    return ret;
  }
}
