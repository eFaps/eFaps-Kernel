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
 * Revision:        $Rev:  $
 * Last Changed:    $Date:  $
 * Last Changed By: $Author: $
 */

package org.efaps.ui.wicket.models.cell;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.ui.field.Field;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id: $
 */
public class UIFormCellSet extends UIFormCell{

  private static final long serialVersionUID = 1L;

  /**
   * @param _field
   * @param _oid
   * @param value
   * @param values
   * @param _required
   * @param _label
   */
  public UIFormCellSet(final Field _field, final String _oid,
      final String value, final String _icon, final boolean _required,
      final String _label) {
    super(_field, _oid, value, _icon, _required, _label);
  }

  private final Map<Integer,Map<Integer,String>> xy2attr = new HashMap<Integer,Map<Integer,String>>();

  /**
   * @param child
   * @param _value
   */
  public void add(final int _x, final int _y, final String _value) {
    Map<Integer, String> xmap = this.xy2attr.get(_x);
    if (xmap == null){
      xmap = new HashMap<Integer, String>();
      this.xy2attr.put(_x, xmap);
    }
    xmap.put(_y, _value);
  }

  /**
   * @return
   */
  public int getYsize() {
    final Map<Integer, String> xmap = this.xy2attr.get(0);
    int ret = 0;
    if (xmap!=null) {
      ret = xmap.size();
    }
    return ret;
  }

  public int getXsize() {
    return this.xy2attr.size();
  }

  /**
   * @param x
   * @param _y
   * @return
   */
  public String getXYValue(final int _x, final int _y) {
    String ret = null;
    final Map<Integer, String> xmap = this.xy2attr.get(_x);
    if (xmap!=null){
      ret = xmap.get(_y);
    }
    return ret;
  }


}
