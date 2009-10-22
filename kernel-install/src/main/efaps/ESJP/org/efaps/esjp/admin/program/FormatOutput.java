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

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * Class is used to render a css into a readable html.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("27dcb0e6-bd78-4442-a9d6-05f491a2900f")
@EFapsRevision("$Rev$")
public class FormatOutput implements EventExecution {

  /**
   * not used!
   *
   * @param _parameter as passed from eFaps
   * @throws EFapsException on error
   * @return Return with value for field
   */
  public Return execute(final Parameter _parameter) throws EFapsException {
    return new Return();
  }

  /**
   * Method is called as UI_FIELD_VALUE event from inside form
   * "Admin_Program_CSSForm".
   *
   * @param _parameter as passed from eFaps
   * @throws EFapsException on error
   * @return Return with value for field
   */
  public Return css(final Parameter _parameter) throws EFapsException {
    final Return ret = new Return();

    final Instance instance = _parameter.getCallInstance();

    final Checkout checkout = new Checkout(instance);
    final InputStream ins = checkout.execute();
    final BufferedReader reader
                               = new BufferedReader(new InputStreamReader(ins));
    final StringBuilder strb = new StringBuilder();

    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        strb.append(line.replaceAll("\\s", "&nbsp;"));
        strb.append("<br/>");
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

    ret.put(ReturnValues.SNIPLETT, strb.toString());

    return ret;
  }

  /**
   * Method is called as UI_FIELD_VALUE event from inside form
   * "Admin_Program_JavaForm".
   *
   * @param _parameter as passed from eFaps
   * @throws EFapsException on error
   * @return Return with value for field
   */
  public Return java(final Parameter _parameter) throws EFapsException {
    final Return ret = new Return();

    final Instance instance = _parameter.getCallInstance();

    final Checkout checkout = new Checkout(instance);
    final InputStream ins = checkout.execute();
    final BufferedReader reader
                               = new BufferedReader(new InputStreamReader(ins));
    final StringBuilder strb = new StringBuilder();

    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        strb.append(line.replaceAll("\\s", "&nbsp;").replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;").replaceAll("\\s", "&nbsp;"));
        strb.append("<br/>");
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

    ret.put(ReturnValues.SNIPLETT, strb.toString());

    return ret;
  }

  /**
   * TODO the format does not work always!
   * Method is called as UI_FIELD_VALUE event from inside form
   * "Admin_Program_JavaScriptForm".
   *
   * @param _parameter as passed from eFaps
   * @throws EFapsException on error
   * @return Return with value for field
   */
  public Return javascript(final Parameter _parameter) throws EFapsException {
    final Return ret = new Return();

    final Instance instance = _parameter.getCallInstance();

    final Checkout checkout = new Checkout(instance);
    final InputStream ins = checkout.execute();
    final BufferedReader reader
                               = new BufferedReader(new InputStreamReader(ins));
    final StringBuilder strb = new StringBuilder();

    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        strb.append(line.replaceAll("\\s", "&nbsp;").replaceAll("<", "&lt;")
                        .replaceAll(">", "&gt;").replaceAll("\\s", "&nbsp;"));
        strb.append("<br/>");
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

    ret.put(ReturnValues.SNIPLETT, strb.toString());

    return ret;
  }
}
