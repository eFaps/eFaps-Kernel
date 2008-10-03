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

package org.efaps.ui.wicket.models.cell;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.field.Field;

/**
 * This class represents the model wich is used for rendering the components of
 * two cells inside a form. The first is holding the label for the second one
 * which contains the value. <br>
 * It uses a {@link org.efaps.admin.ui.field.Field} as the base for the data.
 *
 * @author jmox
 * @version $Id$
 */
public class UIFormCell extends UITableCell {

  private static final long serialVersionUID = 1L;

  /**
   * instance variable storing the Value for the first cell as a Label
   */
  private final String cellLabel;

  /**
   * instance variable storing if in case of edit or create this field is
   * required
   */
  private final boolean required;

  /**
   * instance variable storing the name of the field
   */
  private final String name;

  private final boolean hideLabel;

  private final int rowSpan;

  private final String typeName;



  public UIFormCell(final Field _field, final String _oid,
                    final String _cellValue, final String _icon,
                    final boolean _required, final String _label,
                    final String _typeName) {
    super(_field, _oid, _cellValue, _icon);
    this.required = _required;
    this.cellLabel = DBProperties.getProperty(_label);
    this.name = _field.getName();
    this.hideLabel = _field.isHideLabel();
    this.rowSpan = _field.getRowSpan();
    this.typeName = _typeName;
  }

  public String getTypeName() {
    return this.typeName;
  }

  /**
   * This is the getter method for the instance variable {@link #cellLabel}.
   *
   * @return value of instance variable {@link #cellLabel}
   */
  public String getCellLabel() {
    return this.cellLabel;
  }

  /**
   * This is the getter method for the instance variable {@link #required}.
   *
   * @return value of instance variable {@link #required}
   */
  public boolean isRequired() {
    return this.required;
  }

  /**
   * This is the getter method for the instance variable {@link #name}.
   *
   * @return value of instance variable {@link #name}
   */

  @Override
  public String getName() {
    return this.name;
  }

  public boolean isHideLabel() {
    return this.hideLabel;
  }

  public int getRowSpan() {
    return this.rowSpan;
  }

}
