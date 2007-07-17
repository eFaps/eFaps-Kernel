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

package org.efaps.admin.datamodel.ui;

import org.efaps.admin.ui.Field;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class FieldDefinition {

  ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /** Stores the label of the field definition. */
  final String label;

  /** Stores the field of the field definition. */
  final Field field;

  ///////////////////////////////////////////////////////////////////////////
  // constructors / desctructors

  public FieldDefinition(final String _label,
                         final Field _field)
  {
    this.label = _label;
    this.field = _field;
  }

  ///////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * Getter method for instance variable {@link #label}.
   *
   * @see #label
   */
  public String getLabel()  {
    return this.label;
  }

  /**
   * Getter method for instance variable {@link #field}.
   *
   * @see #field
   */
  public Field getField()  {
    return this.field;
  }
}

