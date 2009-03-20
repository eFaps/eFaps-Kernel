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

package org.efaps.ui.wicket.models.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.wicket.IClusterable;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.Picker;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public class UIPicker implements IClusterable {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  private final Map<String, String> valueMap
                                                = new HashMap<String, String>();
  private List<?> valueList;

  private final String title;

  private final UIFormCell cell;

  private final UUID uuid;

  private final String label;

  private final String[] headings;
  /**
   * @param _pickerName
   */
  public UIPicker(final UIFormCell _cell, final String _name) {
    this.cell = _cell;
    final Picker picker = Picker.get(_name);
    this.uuid = picker.getUUID();
    this.title = DBProperties.getProperty(_name + ".Title");
    this.label = DBProperties.getProperty(_name + ".Label");
    this.headings = evaluateHeadings(picker);
  }

  private String[] evaluateHeadings(final Picker _picker) {
    final List<String> headingList = new ArrayList<String>();
    headingList.add("");
    for (final Field field : _picker.getFields()) {
      headingList.add(DBProperties.getProperty(field.getLabel() + ".Label"));
    }
    return headingList.toArray(new String[headingList.size()]);
  }

  public Picker getPicker() {
    return Picker.get(this.uuid);
  }

  /**
   * @param object
   * @return
   * @throws EFapsException
   */
  public void execute(final Object _others) throws EFapsException {
    final Context context = Context.getThreadContext();
    final String[] contextoid = { this.cell.getInstanceKey() };
    context.getParameters().put("oid", contextoid);
    final List<Return> returns = getPicker().executeEvents(EventType.UI_PICKER,
                     ParameterValues.INSTANCE, this.cell.getInstance(),
                     ParameterValues.OTHERS, _others,
                     ParameterValues.PARAMETERS, context.getParameters(),
                     ParameterValues.CLASS, this);

      final Return valret = returns.get(0);
      this.valueList = (List<?>) valret.get(ReturnValues.VALUES);
      for (final Object obj : this.valueList) {
        final String[] arr = (String[]) obj;
        this.valueMap.put(arr[0], arr[1]);
      }
  }


  /**
   * Getter method for instance variable {@link #valueMap}.
   *
   * @return value of instance variable {@link #valueMap}
   */
  public Map<String, String> getValueMap() {
    return this.valueMap;
  }

  public String getValue(final String _key) {
    return this.valueMap.get(_key);
  }

  /**
   * Getter method for instance variable {@link #valueList}.
   *
   * @return value of instance variable {@link #valueList}
   */
  public List<?> getValueList() {
    return this.valueList;
  }


  /**
   * Getter method for instance variable {@link #title}.
   *
   * @return value of instance variable {@link #title}
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Getter method for instance variable {@link #label}.
   *
   * @return value of instance variable {@link #label}
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * Getter method for instance variable {@link #headings}.
   *
   * @return value of instance variable {@link #headings}
   */
  public String[] getHeadings() {
    return this.headings;
  }
}
