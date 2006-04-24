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

import javax.faces.convert.Converter;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * An instance of this class represents one header / definition in a web table
 * or web form.
 *
 * @author tmo
 * @version $Rev$
 */
public class ColumnHeader implements Serializable  {

  private final String name;

  private final String label;

  private final Converter converter;

  public ColumnHeader(final String _name, final String _label, final Converter _converter)  {
    this.name       = _name;
    this.label      = _label;
    this.converter  = _converter;
  }

  public String getName()  {
    return this.name;
  }

  public String getLabel()  {
    return this.label;
  }

  public Converter getConverter()  {
    return this.converter;
  }

  /**
   * The method overrides the original method 'toString' and returns the name,
   * label and converter of this instance.
   *
   * @return name of the user interface object
   */
  public String toString()  {
    return new ToStringBuilder(this).
      append("name", getName()).
      append("label", getLabel()).
      append("converter", getConverter()).
      toString();
  }
}

