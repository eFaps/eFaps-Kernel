/*
 * Copyright 2005 The eFaps Team
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

package org.efaps.beans;

import java.util.ArrayList;

import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.ui.Field;
import org.efaps.db.Context;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

public class FormPasswordBean extends FormBean  {

  public FormPasswordBean()  {
    super();
System.out.println("FormPasswordBean.constructor");
  }

  public void finalize()  {
System.out.println("FormPasswordBean.destructor");
  }

  public void execute() throws Exception  {
System.out.println("FormPasswordBean");
    setValues(new ArrayList());
    getValues().add(null);
/*    for (int i=0; i<getForm().getFields().size(); i++)  {
      Field field = (Field)getForm().getFields().get(i);
      AttributeTypeInterface attrValue = field.getAttribute().newInstance();
      attrValue.setField(field);
      add(attrValue);
    }
*/
  }

  /**
   * The instance method process the create of a new object.
   *
   * @param _context  context for this request
   */
  protected void processCreate(Context _context) throws Exception  {
    throw new Exception("Create for password not allowed! Wrong target mode type!");
  }

  /**
   * The instance method process the update of current selected object.
   *
   * @param _context  context for this request
   */
  protected void processUpdate(Context _context) throws Exception  {
System.out.println("FormPasswordBean.processUpdate");

    String oldPasswd  = getRequest().getParameter("OldPassword");
    String newPasswd1 = getRequest().getParameter("NewPassword1");
    String newPasswd2 = getRequest().getParameter("NewPassword2");

    boolean bck = _context.getPerson().checkPassword(_context, oldPasswd);
    if (!bck)  {
      throw new EFapsException(getClass(), "WrongPassword");
    }
    if (!newPasswd1.equals(newPasswd2))  {
      throw new EFapsException(getClass(), "NewPasswordsNotEqual");
    }

     _context.getPerson().setPassword(_context, newPasswd1);
  }
}