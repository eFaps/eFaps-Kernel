/*
 * Copyright 2003-2007 The eFaps Team
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

package org.efaps.esjp.admin.user;

import java.util.Map;
import java.util.UUID;

import org.efaps.admin.AbstractAdminObject.EFapsClassName;
import org.efaps.admin.common.SystemAttribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.FieldValue.HtmlType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.Update;
import org.efaps.db.Update.Status;
import org.efaps.util.EFapsException;

public class Password {

  public Return changePwdUI(final Parameter _parameter) throws EFapsException {

    final Context context = Context.getThreadContext();
    final String passwordold = context.getParameter("passwordold");
    final String passwordnew = context.getParameter("passwordnew");
    final Return ret = new Return();

    if (context.getPerson().checkPassword(passwordold)) {
      final Type type = Type.get(EFapsClassName.USER_PERSON.name);
      final Update update = new Update(type, "" + context.getPerson().getId());
      final Status status = update.add("Password", passwordnew);

      if ((status.isOk())) {
        update.execute();
        ret.put(ReturnValues.TRUE, "true");
      } else {
        ret.put(ReturnValues.VALUES, status.getReturnValue());
      }
    } else {
      ret.put(ReturnValues.VALUES,
          "Admin_User_PwdChgForm/Password.changePwdUI.checkPassword");
    }
    return ret;
  }

  public Return validateFormUI(final Parameter _parameter)
                                                          throws EFapsException {
    final Return ret = new Return();
    final Context context = Context.getThreadContext();
    final String passwordnew = context.getParameter("password");
    final String passwordnew2 = context.getParameter("passwordnew2");

    if (passwordnew.equals(passwordnew2)) {
      ret.put(ReturnValues.TRUE, "true");
    } else {
      ret.put(ReturnValues.VALUES,
          "Admin_User_PwdChgForm/Password.validateFormUI.unequal");
    }
    return ret;
  }

  public Return getFieldValueUI(final Parameter _parameter) {
    final StringBuilder ret = new StringBuilder();
    final FieldValue fieldvalue =
        (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
    final String field =
        (String) ((Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES))
            .get("field");

    final HtmlType htmltype = fieldvalue.getHtmlType();

    final Return retVal = new Return();

    if (htmltype == HtmlType.CREATEHTML) {
      if ("1".equals(field)) {
        ret.append("<br/>&nbsp;").append(
            "<input name=\"passwordold\" type=\"password\" size=\"20\">")
            .append("&nbsp;<br/><br/>");
      } else if ("2".equals(field)) {
        ret.append("<br/>&nbsp;").append(
            "<input name=\"password\" type=\"password\" size=\"20\">").append(
            "&nbsp;<br/><br/>");
      } else if ("3".equals(field)) {
        ret.append("<br/>&nbsp;").append(
            "<input name=\"passwordnew2\" type=\"password\" size=\"20\">")
            .append("&nbsp;<br/><br/>");
      }
    }
    if (ret != null) {
      retVal.put(ReturnValues.VALUES, ret);
    }
    return retVal;
  }

  public Return validatePwdValue(final Parameter _parameter)
                                                            throws EFapsException {
    final Return ret = new Return();
    final Context context = Context.getThreadContext();
    final String passwordnew = context.getParameter("password");

    if (passwordnew.length() > SystemAttribute.get(
        UUID.fromString("bb26c4a4-65a8-41e9-bc64-5fe0148cf805"))
        .getIntegerValue()) {
      ret.put(ReturnValues.TRUE, "true");
    } else {
      ret.put(ReturnValues.VALUES,
          "Admin_User_PwdChgForm/Password.validatePwdValue.ShortPwd");
    }
    return ret;
  }

  /**
   * this method is used from Admins wich have the Role Common_Main_PwdChg to
   * set a Password for a User
   *
   * @param _parameter
   * @return
   * @throws EFapsException
   */
  public Return setPwdValueUI(final Parameter _parameter) throws EFapsException {
    final Return ret = new Return();
    final Instance instance =
        (Instance) _parameter.get(ParameterValues.INSTANCE);

    // Common_Main_PwdChg
    final Role setpwdRole =
        Role.get(UUID.fromString("2c101471-43e3-4c97-9045-f48f5b12b6ed"));
    if (Context.getThreadContext().getPerson().isAssigned(setpwdRole)) {

      final String password =
          Context.getThreadContext().getParameter("password");

      final Update update = new Update(instance);
      final Status status = update.add("Password", password);
      if ((status.isOk())) {
        update.execute();
        ret.put(ReturnValues.TRUE, "true");
      } else {
        ret.put(ReturnValues.VALUES, status.getReturnValue());
      }
    } else {
      ret.put(ReturnValues.VALUES,
          "Admin_User_PersonSetPwdForm/Password.setPwdValueUI.NoRight");
    }
    return ret;
  }
}
