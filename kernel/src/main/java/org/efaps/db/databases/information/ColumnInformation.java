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

package org.efaps.db.databases.information;

import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.db.databases.AbstractDatabase.ColumnType;

/**
 * Stores information about one column within a table.
 *
 * @author tmo
 * @version $Id$
 */
public class ColumnInformation
{
  /**
   * Name of column in upper case.
   *
   * @see #getName
   */
  private final String name;

  /**
   * Set of all possible column types.
   */
  private final Set<ColumnType> types;

  /**
   * Size of the column (for string).
   *
   * @see #size
   */
  private final int size;

  /**
   * Could the column have a null value?
   *
   * @see #isNullable
   */
  private final boolean isNullable;

  /**
   * Constructor to initialize all instance variables.
   *
   * @param _name   name of column
   * @param _types  set of column types
   * @param _size   size of column
   */
  protected ColumnInformation(final String _name,
                              final Set<ColumnType> _types,
                              final int _size,
                              final boolean _isNullable)
  {
    this.name = _name.toUpperCase();
    this.types = _types;
    this.size = _size;
    this.isNullable = _isNullable;
  }

  /**
   * This is the getter method for instance variable {@link #name}.
   *
   * @return value of instance variable {@link #name}
   * @see #name
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * This is the getter method for instance variable {@link #size}.
   *
   * @return value of instance variable {@link #size}
   * @see #size
   */
  public int getSize()
  {
    return this.size;
  }

  /**
   * This is the getter method for instance variable {@link #isNullable}.
   *
   * @return value of instance variable {@link #isNullable}
   * @see #isNullable
   */
  public boolean isNullable()
  {
    return this.isNullable;
  }

  /**
   * Returns string representation of this class instance. The information
   * includes {@link #name}, {@link #types}, {@link #size} and
   * {@link #isNullable}.
   */
  @Override
  public String toString()
  {
    return new ToStringBuilder(this)
            .append("name", this.name)
            .append("types", this.types)
            .append("size", this.size)
            .append("isNullable", this.isNullable)
            .toString();
  }
}
