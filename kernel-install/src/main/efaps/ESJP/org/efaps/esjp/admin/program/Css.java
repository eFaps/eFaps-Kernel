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

package org.efaps.esjp.admin.program;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.FieldValue.HtmlType;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("27dcb0e6-bd78-4442-a9d6-05f491a2900f")
@EFapsRevision("$Rev$")
public class Css implements EventExecution {

  public Return execute(final Parameter _parameter) throws EFapsException {
    final Return ret = new Return();
    final FieldValue fieldvalue = (FieldValue) _parameter
        .get(ParameterValues.UIOBJECT);
    final Instance instance = (Instance) _parameter
        .get(ParameterValues.CALL_INSTANCE);

    final Checkout checkout = new Checkout(instance);
    final InputStream ins = checkout.execute();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
    final StringBuilder strb = new StringBuilder();

    if (HtmlType.EDITHTML.equals(fieldvalue.getHtmlType())) {
      strb.append("<textarea>");
    }

    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        strb.append(line);
        if (HtmlType.VIEWHTML.equals(fieldvalue.getHtmlType())) {
          strb.append("<br/>");
        } else if (HtmlType.EDITHTML.equals(fieldvalue.getHtmlType())) {
          strb.append("/n");
        }

      }
    } catch (final IOException e) {
      e.printStackTrace();
    } finally {
      try {
        ins.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
    if (HtmlType.EDITHTML.equals(fieldvalue.getHtmlType())) {
      strb.append("</textarea>");
    }
    ret.put(ReturnValues.VALUES, strb.toString());

    return ret;
  }
}
