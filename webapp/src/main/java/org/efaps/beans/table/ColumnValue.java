/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.beans.table;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * An instance of this class represents one column in a web table or web form.
 *
 * @author tmo
 * @version $Rev$
 */
public class ColumnValue < H > implements Serializable  {

  private final H value;

  public ColumnValue(final H _value)  {
    this.value = _value;
  }

  public H getValue()  {
    return this.value;
  }

  /**
   * The method overrides the original method 'toString' and returns the value
   * of this instance.
   *
   * @return name of the user interface object
   */
  public String toString()  {
    return new ToStringBuilder(this).
      append("value", getValue()).
      toString();
  }
}
