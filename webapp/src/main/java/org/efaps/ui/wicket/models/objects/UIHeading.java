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
import org.efaps.admin.ui.field.FieldHeading;

public class UIHeading implements IFormElement, IClusterable{

  private static final long serialVersionUID = 1L;

  /**
   * instance variable to store the level of the Heading
   */
  private int level = 1;

  private final String label;

  public UIHeading(final FieldHeading _heading) {
    this.label = DBProperties.getProperty(_heading.getLabel());
    this.level = _heading.getLevel();
  }

  /**
   * This is the getter method for the instance variable {@link #level}.
   *
   * @return value of instance variable {@link #level}
   */
  public int getLevel() {
    return this.level;
  }

  /**
   * This is the getter method for the instance variable {@link #label}.
   *
   * @return value of instance variable {@link #label}
   */
  public String getLabel() {
    return this.label;
  }



}
