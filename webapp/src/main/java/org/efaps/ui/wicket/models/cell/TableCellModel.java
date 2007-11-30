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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.models.cell;

import org.apache.wicket.model.Model;

import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.admin.ui.field.Field;

/**
 * This class represents the model wich is used for rendering the components of
 * one cell inside a Table.It uses a {@link org.efaps.admin.ui.field.Field} as
 * the base for the data.
 *
 * @author jmox
 * @version $Id:CellModel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class TableCellModel extends Model {

  private static final long serialVersionUID = 1L;

  /**
   * instance variable storing the oid for the model
   */
  private final String oid;

  /**
   * instance variable storing the reference of the field
   */
  private String reference;

  /**
   * instance variable storing the actual value of the field
   */
  private final String cellValue;

  /**
   * instance variable storing the icon of the field
   */
  private final String icon;

  /**
   * instance variable storing the target of the field
   */
  private final Target target;

  /**
   * instance variable storing if the cell is fixed width
   */
  private final boolean fixedWidth;

  public TableCellModel(final Field _field, final String _oid,
                        final String _cellvalue, final String _icon) {
    super();
    this.reference = _field.getReference();
    this.target = _field.getTarget();
    this.oid = _oid;
    this.cellValue = _cellvalue;
    this.icon = _icon;
    this.fixedWidth = _field.isFixedWidth();
  }

  /**
   * This is the getter method for the instance variable {@link #oid}.
   *
   * @return value of instance variable {@link #oid}
   */

  public String getOid() {
    return this.oid;
  }

  /**
   * This is the getter method for the instance variable {@link #reference}.
   *
   * @return value of instance variable {@link #reference}
   */

  public String getReference() {
    return this.reference;
  }

  /**
   * This is the setter method for the instance variable {@link #reference}.
   *
   * @param _reference
   *                the reference to set
   */
  public void setReference(final String _reference) {
    this.reference = _reference;
  }

  /**
   * This is the getter method for the instance variable {@link #cellvalue}.
   *
   * @return value of instance variable {@link #cellvalue}
   */

  public String getCellValue() {
    return this.cellValue;
  }

  /**
   * This is the getter method for the instance variable {@link #icon}.
   *
   * @return value of instance variable {@link #icon}
   */

  public String getIcon() {
    return this.icon;
  }

  /**
   * This is the getter method for the instance variable {@link #target}.
   *
   * @return value of instance variable {@link #target}
   */

  public Target getTarget() {
    return this.target;
  }

  /**
   * This is the getter method for the instance variable {@link #fixedWidth}.
   *
   * @return value of instance variable {@link #fixedWidth}
   */
  public boolean isFixedWidth() {
    return this.fixedWidth;
  }

  /**
   * This method returns if the field is a link which makes a checkout
   *
   * @return
   */
  public boolean isCheckOut() {
    return this.reference.contains("/servlet/checkout");
  }
}
