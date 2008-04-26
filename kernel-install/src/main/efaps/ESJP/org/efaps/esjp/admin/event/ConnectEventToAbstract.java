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

package org.efaps.esjp.admin.event;

import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.FieldValue.HtmlType;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * ESJP used to create and update events.
 *
 * @author tmo
 * @version $Id$
 */
@EFapsUUID("3e6ba860-22ff-486b-b424-27a0ee9e72f2")
public class ConnectEventToAbstract
{
  /**
   * UUID of the program type.
   */
  static private final UUID UUID_PROGRAM = UUID.fromString("11043a35-f73c-481c-8c77-00306dbce824");

  /**
   * Shows a drop down list of all allowed event types depending on the parent
   * type.
   *
   * @param _parameter  parameters from the field type4NotView of the form
   *                    Admin_Event_Definition
   * @return HTML string with the drop down list
   * @throws EFapsException
   */
  public Return getEventTypesUI(final Parameter _parameter)
      throws EFapsException
  {
    final FieldValue fieldvalue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
    final HtmlType htmlType = fieldvalue.getHtmlType();

    final Instance callInstance = _parameter.getCallInstance();

    Instance parentInstance = null;
    if (htmlType == HtmlType.CREATEHTML)  {
      parentInstance = callInstance;
    } else if (htmlType == HtmlType.EDITHTML)  {
      final SearchQuery query = new SearchQuery();
      query.setObject(callInstance);
      query.addSelect("Abstract.OID");
      query.execute();
      query.next();
      parentInstance = new Instance((String) query.get("Abstract.OID"));
      query.close();
    }


final String allowedEvenTypes = parentInstance.getType().getProperty("AllowedEvents");

final String typeLabel = DBProperties.getProperty(new StringBuilder(allowedEvenTypes).append(".Label").toString());

    final String fieldName = fieldvalue.getFieldDef().getField().getName();
    final StringBuilder ret = new StringBuilder();
    ret.append("<select name=\"").append(fieldName).append("\" size=\"1\">");
    ret.append("<option value=\"").append(allowedEvenTypes).append("\">").append(typeLabel).append("</option>");
    ret.append("</select>");

    final Return retVal = new Return();
    if (ret != null) {
      retVal.put(ReturnValues.VALUES, ret);
    }
    return retVal;
  }

  /**
   * Search for all existing programs in eFaps and returns them as drop down
   * list so that the user could select one.
   *
   * @param _parameter  parameters from the field program4NotView of the form
   *                    Admin_Event_Definition
   * @return HTML with the drop down list for all existing programs within
   *         eFaps
   * @throws EFapsException
   * @see #UUID_PROGRAM
   */
  public Return getProgramsUI(final Parameter _parameter) throws EFapsException
  {
    final FieldValue fieldvalue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
    final HtmlType htmlType = fieldvalue.getHtmlType();

    // selected program (if mode edit)
    long selectedId = 0;
    if (htmlType == HtmlType.EDITHTML)  {
      final Instance callInstance = _parameter.getCallInstance();
      final SearchQuery query = new SearchQuery();
      query.setObject(callInstance);
      query.addSelect("JavaProg");
      query.execute();
      query.next();
      selectedId = (Long) query.get("JavaProg");
      query.close();
    }

    // search for all programs
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(Type.get(UUID_PROGRAM).getName());
    query.addSelect("Name");
    query.addSelect("ID");
    query.execute();

    // build drop down list
    final String fieldName = fieldvalue.getFieldDef().getField().getName();
    final StringBuilder ret = new StringBuilder();
    ret.append("<select name=\"").append(fieldName).append("\" size=\"1\">");
    while (query.next())  {
      final long id = (Long) query.get("ID");
      final String name = (String) query.get("Name");
      ret.append("<option value=\"").append(id);
      if (id == selectedId)  {
        ret.append(" selected=\"selected\"");
      }
      ret.append("\">").append(name).append("</option>");
    }
    ret.append("</select>");

    // and return the string
    final Return retVal = new Return();
    retVal.put(ReturnValues.VALUES, ret);
    return retVal;
  }

  /**
   * Creates a new event for given type and program depending on the user input
   * on the web form Admin_Event_Definition.
   *
   * @param _parameter  from the form Admin_Event_Definition in mode create
   */
  public void createNewEventUI(final Parameter _parameter)
      throws EFapsException
  {
    final Instance callInstance  = _parameter.getCallInstance();
    final Type eventType         = Type.get(_parameter.getParameterValue("type4NotView"));
    final String name            = _parameter.getParameterValue("name");
    final String index           = _parameter.getParameterValue("index");
    final String programId       = _parameter.getParameterValue("program4NotView");
    final String method          = _parameter.getParameterValue("method");

    final Insert insert = new Insert(eventType);
    insert.add("Name", name);
    insert.add("IndexPosition", index);
    insert.add("Abstract", "" + callInstance.getId());
    insert.add("JavaProg", programId);
    insert.add("Method", method);
    insert.execute();
  }
}
