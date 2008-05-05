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

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The class is used to store information about unique keys within SQL tables.
 *
 * @author tmo
 * @version $Id$
 */
public class UniqueKeyInformation
{
  /**
   * Name of the unique key in upper case.
   */
  private final String ukName;

  /**
   * Comma separated string of column names in upper case for which this unique
   * key is defined.
   */
  private final StringBuilder columnNames = new StringBuilder();

  /**
   * Constructor to initialize all instance variables.
   *
   * @param _ukName   name of unique key
   * @param _colName  first name of column of a unique key
   */
  protected UniqueKeyInformation(final String _ukName,
                                 final String _colName)
  {
    this.ukName = _ukName.toUpperCase();
    this.columnNames.append(_colName.toUpperCase());
  }

  /**
   * Append a new name of column for which this unique key is defined.
   *
   * @param _colName  further column names which are defined in this unique key
   */
  protected void appendColumnName(final String _colName)
  {
    this.columnNames.append(',').append(_colName.toUpperCase());
  }

  /**
   * Getter method for instance variable {@link #columnNames}.
   *
   * @return value of instance variable columnNames
   * @see #columnNames
   */
  public String getColumnNames()  {
    return this.columnNames.toString();
  }

  /**
   * Returns string representation of this class instance. The information
   * includes {@link #ukName} and {@link #columnNames}.
   */
  @Override
  public String toString()
  {
    return new ToStringBuilder(this)
            .append("ukName", this.ukName)
            .append("columnNames", this.columnNames)
            .toString();
  }
}
