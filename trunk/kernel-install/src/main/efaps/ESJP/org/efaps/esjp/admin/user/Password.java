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

package org.efaps.esjp.admin.user;

import java.util.UUID;

import javax.security.auth.login.LoginException;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.Update;
import org.efaps.db.Update.Status;
import org.efaps.jaas.SetPasswordHandler;
import org.efaps.jaas.efaps.UserLoginModule.UpdateException;
import org.efaps.util.EFapsException;

/**
 * Esjp used to change th epassword for a user.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("ff1b1140-3da0-491f-8bf4-c42f71ea4343")
@EFapsRevision("$Rev$")
public class Password
{

    /**
     * Key used for the field for the old password.
     */
    private static String PWDOLD = "passwordold";

    /**
     * Key used for the field for the new password.
     */
    private static String PWDNEW = "passwordnew";

    /**
     * Key used for the field for the new password repetition.
     */
    private static String PWDNEWREPEAT = "passwordnew2";

    /**
     * Method is called to change the password of a user in the efpas database.
     *
     * @param _parameter Parameter as passed from eFaps to esjp
     * @return Return
     * @throws EFapsException on error
     */
    public Return changePwdUI(final Parameter _parameter) throws EFapsException
    {

        final Context context = Context.getThreadContext();
        final String passwordold = context.getParameter(PWDOLD);
        final String passwordnew = context.getParameter(PWDNEW);
        final Return ret = new Return();

        final SetPasswordHandler handler = new SetPasswordHandler("eFaps");
        try {
            if (handler.setPassword(context.getPerson().getName(), passwordnew, passwordold)) {
                ret.put(ReturnValues.TRUE, "true");
            }
        } catch (final LoginException e) {
            if (e instanceof UpdateException) {
                ret.put(ReturnValues.VALUES, "Admin_User_PwdChgForm/Password.validatePwdValue.ShortPwd");
            } else {
                ret.put(ReturnValues.VALUES, "Admin_User_PwdChgForm/Password.changePwdUI.checkPassword");
            }
        }
        return ret;
    }

    /**
     * Method is called from a command to validate the form. It checks if the
     * new password and the repetition are equal.
     *
     * @param _parameter Parameter as passed from eFaps to esjp
     * @return Return with true if equal
     * @throws EFapsException on error
     */
    public Return validateFormUI(final Parameter _parameter) throws EFapsException
    {
        final Return ret = new Return();
        final Context context = Context.getThreadContext();
        final String passwordnew = context.getParameter(PWDNEW);
        final String passwordnew2 = context.getParameter(PWDNEWREPEAT);

        if (passwordnew.equals(passwordnew2)) {
            ret.put(ReturnValues.TRUE, "true");
        } else {
            ret.put(ReturnValues.VALUES, "Admin_User_PwdChgForm/Password.validateFormUI.unequal");
        }
        return ret;
    }

    /**
     * This method is called first to render simple inputfields.
     *
     * @param _parameter Parameter as passed from eFaps to esjp
     * @return Return
     */
    public Return getFieldValueUI(final Parameter _parameter)
    {
        final StringBuilder ret = new StringBuilder();
        final FieldValue fieldvalue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);

        final TargetMode mode = fieldvalue.getTargetMode();

        final Return retVal = new Return();

        if (mode.equals(TargetMode.CREATE)) {
            final String fieldName = fieldvalue.getField().getName();
            ret.append("<br/>&nbsp;").append("<input name=\"").append(fieldName).append(
                            "\" type=\"password\" size=\"20\">").append("&nbsp;<br/><br/>");
        }
        if (ret != null) {
            retVal.put(ReturnValues.SNIPLETT, ret);
        }
        return retVal;
    }

    /**
     * Executed on a validate event on the attribute.
     *
     * @param _parameter Parameter as passed from eFaps to esjp
     * @return Return
     * @throws EFapsException on error
     */
    public Return validatePwdValue(final Parameter _parameter) throws EFapsException
    {
        final Return ret = new Return();
        final Object[] newPwd = (Object[]) _parameter.get(ParameterValues.NEW_VALUES);
        // Admin_User_PwdLengthMin
        if (newPwd[0].toString().length() > SystemConfiguration.get(
                        UUID.fromString("acf2b19b-f7c4-4e4a-a724-fb2d9ed30079")).getAttributeValueAsInteger(
                        "PwdLengthMin")) {
            ret.put(ReturnValues.TRUE, "true");
        } else {
            ret.put(ReturnValues.VALUES, "Admin_User_PwdChgForm/Password.validatePwdValue.ShortPwd");
        }
        return ret;
    }

    /**
     * This method is used from Admins which have the Role Common_Main_PwdChg to
     * set a Password for a User.
     *
     * @param _parameter Parameter as passed from eFaps to esjp
     * @return Return
     * @throws EFapsException on error
     */
    public Return setPwdValueUI(final Parameter _parameter) throws EFapsException
    {
        final Return ret = new Return();
        final Instance instance = _parameter.getInstance();

        // Common_Main_PwdChg
        final Role setpwdRole = Role.get(UUID.fromString("2c101471-43e3-4c97-9045-f48f5b12b6ed"));

        if (Context.getThreadContext().getPerson().isAssigned(setpwdRole)) {

            final String pwd = Context.getThreadContext().getParameter("setpassword");

            final Update update = new Update(instance);
            final Status status = update.add("Password", pwd);
            if ((status.isOk())) {
                update.execute();
                ret.put(ReturnValues.TRUE, "true");
            } else {
                ret.put(ReturnValues.VALUES, status.getReturnValue());
            }
        } else {
            ret.put(ReturnValues.VALUES, "Admin_User_PersonSetPwdForm/Password.setPwdValueUI.NoRight");
        }
        return ret;
    }
}
