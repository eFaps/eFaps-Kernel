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

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id$
 */
public class UIFormCellSet extends UIFormCell {

  private static final long serialVersionUID = 1L;

  private final boolean editMode;

  private int newCount = 0;

  private final Map<Integer,Map<Integer,String>> xy2value
                                = new HashMap<Integer,Map<Integer,String>>();

  private final Map<Integer,String> x2definition
                                = new HashMap<Integer,String>();

  private final Map<Integer,Instance> y2Instance
                                = new HashMap<Integer, Instance>();

  /**
   * @param _fieldValue
   * @param _oid
   * @param _value
   * @param _icon
   * @param _required
   * @param _label
   * @param _edit
   * @throws EFapsException
   */
  public UIFormCellSet(final AbstractUIObject _parent,
                       final FieldValue _fieldValue, final Instance _instance,
                       final String _value, final String _icon,
                       final String _label, final boolean _edit)
      throws EFapsException {
    super(_parent, _fieldValue, _instance, _value, _icon, _label, "");
    this.editMode = _edit;
  }

  public int getNewCount() {
    return this.newCount++;
  }

  public boolean isEditMode() {
    return this.editMode;
  }


  /**
   * @param child
   * @param _value
   */
  public void add(final int _x, final int _y, final String _value) {
    Map<Integer, String> xmap = this.xy2value.get(_x);
    if (xmap == null){
      xmap = new HashMap<Integer, String>();
      this.xy2value.put(_x, xmap);
    }
    xmap.put(_y, _value);
  }

  /**
   * @return
   */
  public int getYsize() {
    final Map<Integer, String> xmap = this.xy2value.get(0);
    int ret = 0;
    if (xmap != null) {
      ret = xmap.size();
    }
    return ret;
  }

  public int getXsize() {
    return this.xy2value.size();
  }

  /**
   * @param x
   * @param _y
   * @return
   */
  public String getXYValue(final int _x, final int _y) {
    String ret = null;
    final Map<Integer, String> xmap = this.xy2value.get(_x);
    if (xmap != null){
      ret = xmap.get(_y);
    }
    return ret;
  }


  public void addDefiniton(final int _x, final String _definition) {
    this.x2definition.put(_x, _definition);
  }

  public int getDefinitionsize() {
    return this.x2definition.size();
  }

  public String getDefinitionValue(final int _x){
    return this.x2definition.get(_x);
  }

  /**
   * @param y
   * @param next
   */
  public void addInstance(final int _y, final Instance _instance) {
    this.y2Instance.put(_y, _instance);
  }

  public Instance getInstance(final int _y){
    return this.y2Instance.get(_y);
  }
}
