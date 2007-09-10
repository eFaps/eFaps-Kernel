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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.webapp.models;

import org.apache.wicket.model.Model;

/**
 * @author jmo
 * @version $Id$
 *
 */
public class CellModel extends Model {

  private static final long serialVersionUID = 1L;

  private final String oid;

  private final String reference;

  private final String cellValue;

  private final String icon;

  private final int target;

  public CellModel(final String _oid, final String _reference,
                   final String _cellvalue, final String _icon,
                   final int _target) {
    this.oid = _oid;
    this.reference = _reference;
    this.cellValue = _cellvalue;
    this.icon = _icon;
    this.target = _target;
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

  public int getTarget() {
    return this.target;
  }

}
