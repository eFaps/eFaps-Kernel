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

import org.apache.wicket.IClusterable;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.field.Field;

/**
 * @author jmox
 * @version $Id$
 */
public class UITableHeader  implements IClusterable{

  private static final long serialVersionUID = 1L;

  private final String label;

  private final boolean sortable;

  private final String name;

  private final boolean filterable;

  private final boolean fixedWidth;

  private SortDirection sortDirection;

  private int width;

  private String markupId;

  public UITableHeader(final Field _field, final SortDirection _sortdirection) {
    this.label = _field.getLabel();
    this.sortable = _field.isSortAble();
    this.name = _field.getName();
    this.filterable = _field.isFilterable();
    this.sortDirection = _sortdirection;
    this.width = _field.getWidth();
    this.fixedWidth = _field.isFixedWidth();
  }

  public String getLabel() {
    if (this.label != null) {
      return DBProperties.getProperty(this.label);
    } else {
      return "";
    }
  }

  /**
   * This is the getter method for the instance variable {@link #sortable}.
   *
   * @return value of instance variable {@link #sortable}
   */

  public boolean isSortable() {
    return this.sortable;
  }

  /**
   * This is the getter method for the instance variable {@link #name}.
   *
   * @return value of instance variable {@link #name}
   */

  public String getName() {
    return this.name;
  }

  /**
   * This is the getter method for the instance variable {@link #filterable}.
   *
   * @return value of instance variable {@link #filterable}
   */

  public boolean isFilterable() {
    return this.filterable;
  }

  /**
   * This is the getter method for the instance variable {@link #sortDirection}.
   *
   * @return value of instance variable {@link #sortDirection}
   */

  public SortDirection getSortDirection() {
    return this.sortDirection;
  }

  /**
   * This is the setter method for the instance variable {@link #sortDirection}.
   *
   * @param sortDirection
   *                the sortDirection to set
   */
  public void setSortDirection(SortDirection sortDirection) {
    this.sortDirection = sortDirection;
  }

  /**
   * This is the getter method for the instance variable {@link #width}.
   *
   * @return value of instance variable {@link #width}
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * This is the setter method for the instance variable {@link #width}.
   *
   * @param width
   *                the width to set
   */
  public void setWidth(int width) {
    this.width = width;
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
   * This is the getter method for the instance variable {@link #markupId}.
   *
   * @return value of instance variable {@link #markupId}
   */
  public String getMarkupId() {
    return this.markupId;
  }

  /**
   * This is the setter method for the instance variable {@link #markupId}.
   *
   * @param _markupId
   *                the markupId to set
   */
  public void setMarkupId(final String _markupId) {
    this.markupId = _markupId;
  }

  public UITableHeader getObject() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setObject(UITableHeader arg0) {
    // TODO Auto-generated method stub

  }

  public void detach() {
    // TODO Auto-generated method stub

  }


}
