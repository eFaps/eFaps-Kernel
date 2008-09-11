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

package org.efaps.admin.ui.field;

import org.efaps.util.cache.CacheReloadException;

/**
 * @author jmox
 * @version $Id$
 *
 */
public class FieldHeading extends Field {

  /**
   * instance variable to store the level of the Heading
   */
  private int level = 1;

  public FieldHeading(final long _id, final String _uuid, final String _name) {
    super(_id, _uuid, _name);
    super.setCreatable(false);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.efaps.admin.ui.field.Field#setProperty(java.lang.String,
   *      java.lang.String)
   */
  @Override
  protected void setProperty(final String _name, final String _value)
                                                         throws CacheReloadException {
    if ("Level".equals(_name)) {
      this.level = Integer.parseInt(_value);
    } else {
      super.setProperty(_name, _value);
    }
  }


  /**
   * This is the getter method for the instance variable {@link #level}.
   *
   * @return value of instance variable {@link #level}
   */
  public int getLevel() {
    return this.level;
  }

}
