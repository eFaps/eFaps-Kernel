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

package org.efaps.ui.wicket.models.objects;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.IClusterable;

import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.ui.wicket.models.cell.UITableCell;


public class UIRow implements IClusterable {
  private static final long serialVersionUID = 1L;

  /**
   * The instance variable stores all oids in a string.
   *
   * @see #getOids
   */
  private final String instanceKeys;

  /**
   * The instance variable stores the values for the table.
   *
   * @see #getValues
   */
  private final List<UITableCell> values = new ArrayList<UITableCell>();

  /**
   * The constructor creates a new instance of class Row.
   *
   * @param _instanceKeys
   *                string with all oids for this row
   */
  public UIRow(final String _instanceKeys) {
    super();
    this.instanceKeys = _instanceKeys;
  }

  // /////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The instance method adds a new attribute value (from instance
   * {@link AttributeTypeInterface}) to the values.
   *
   * @see #values
   */
  public void add(final UITableCell _cellmodel) {

    this.values.add(_cellmodel);
  }

  /**
   * This is the getter method for the instance variable {@link #instanceKeys}.
   *
   * @return value of instance variable {@link #instanceKeys}
   * @see #instanceKeys
   */
  public String getInstanceKeys() {
    return this.instanceKeys;
  }

  // /////////////////////////////////////////////////////////////////////////

  /**
   * The instance method returns the size of the array list {@link #values}.
   *
   * @see #values
   */
  public int getSize() {
    return getValues().size();
  }

  /**
   * This is the getter method for the values variable {@link #values}.
   *
   * @return value of values variable {@link #values}
   * @see #values
   */
  public List<UITableCell> getValues() {
    return this.values;
  }

}
