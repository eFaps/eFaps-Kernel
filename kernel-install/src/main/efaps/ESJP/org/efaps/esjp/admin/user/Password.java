/*
 * Copyright 2003-2008 The eFaps Team
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

import java.util.UUID;

import javax.security.auth.login.LoginException;

import org.efaps.admin.common.SystemAttribute;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.FieldValue.HtmlType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.Update;
import org.efaps.db.Update.Status;
import org.efaps.jaas.SetPasswordHandler;
import org.efaps.jaas.efaps.UserLoginModule.UpdateException;
import org.efaps.util.EFapsException;

@EFapsUUID("ff1b1140-3da0-491f-8bf4-c42f71ea4343")
public class Password
{

  private static String PWDOLD = "passwordold";

  private static String PWDNEW = "passwordnew";

  private static String PWDNEWREPEAT = "passwordnew2";

  public Return changePwdUI(final Parameter _parameter)
      throws EFapsException
  {

    final Context context = Context.getThreadContext();
    final String passwordold = context.getParameter(PWDOLD);
    final String passwordnew = context.getParameter(PWDNEW);
    final Return ret = new Return();

    final SetPasswordHandler handler = new SetPasswordHandler("eFaps");
    try {
      if (handler.setPassword(context.getPerson().getName(), passwordnew,
          passwordold)) {
        ret.put(ReturnValues.TRUE, "true");
      }
    } catch (final LoginException e) {
      if (e instanceof UpdateException) {
        ret.put(ReturnValues.VALUES,
                "Admin_User_PwdChgForm/Password.validatePwdValue.ShortPwd");
      } else {
        ret.put(ReturnValues.VALUES,
                "Admin_User_PwdChgForm/Password.changePwdUI.checkPassword");
      }
    }
    return ret;
  }

  public Return validateFormUI(final Parameter _parameter)
      throws EFapsException
  {
    final Return ret = new Return();
    final Context context = Context.getThreadContext();
    final String passwordnew = context.getParameter(PWDNEW);
    final String passwordnew2 = context.getParameter(PWDNEWREPEAT);

    if (passwordnew.equals(passwordnew2)) {
      ret.put(ReturnValues.TRUE, "true");
    } else {
      ret.put(ReturnValues.VALUES,
              "Admin_User_PwdChgForm/Password.validateFormUI.unequal");
    }
    return ret;
  }

  /**
   * this method is called fiurst
   *
   * @param _parameter
   * @return
   */
  public Return getFieldValueUI(final Parameter _parameter) {
    final StringBuilder ret = new StringBuilder();
    final FieldValue fieldvalue =
        (FieldValue) _parameter.get(ParameterValues.UIOBJECT);

    final HtmlType htmltype = fieldvalue.getHtmlType();

    final Return retVal = new Return();

    if (htmltype == HtmlType.CREATEHTML) {
      final String fieldName = fieldvalue.getFieldDef().getField().getName();
      ret.append("<br/>&nbsp;").append("<input name=\"").append(fieldName)
          .append("\" type=\"password\" size=\"20\">").append(
              "&nbsp;<br/><br/>");
    }
    if (ret != null) {
      retVal.put(ReturnValues.VALUES, ret);
    }
    return retVal;
  }

  /**
   * executed on a validateevent on the attribute
   *
   * @param _parameter
   * @return
   * @throws EFapsException
   */
  public Return validatePwdValue(final Parameter _parameter)
      throws EFapsException
  {
    final Return ret = new Return();
    final Context context = Context.getThreadContext();
    final String passwordnew = context.getParameter(PWDNEW);

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
  public Return setPwdValueUI(final Parameter _parameter)
      throws EFapsException
  {
    final Return ret = new Return();
    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);

    // Common_Main_PwdChg
    final Role setpwdRole = Role.get(UUID.fromString("2c101471-43e3-4c97-9045-f48f5b12b6ed"));
    if (Context.getThreadContext().getPerson().isAssigned(setpwdRole)) {

      final String password = Context.getThreadContext().getParameter("setpassword");

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
