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

package org.efaps.admin.ui.field;

import org.efaps.util.cache.CacheReloadException;


/**
 * @author jmox
 * @version $Id$
 */
public class FieldCommand extends Field {

  private boolean renderButton;

  public FieldCommand(final long _id, final String _uuid, final String _name) {
    super(_id, _uuid, _name);
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Field}.
   *
   * @param _id
   *                id to search in the cache
   * @return instance of class {@link Field}
   */
  public static FieldCommand get(final long _id) {
    return (FieldCommand) Field.get(_id);
  }

  @Override
  protected void setProperty(final String _name, final String _value)
      throws CacheReloadException {
    if ("CmdRenderButton".equals(_name)) {
      this.renderButton = !("false".equalsIgnoreCase(_value));
    } else {
      super.setProperty(_name, _value);
    }
  }

  /**
   * Getter method for instance variable {@link #renderButton}.
   *
   * @return value of instance variable {@link #renderButton}
   */
  public boolean isRenderButton() {
    return this.renderButton;
  }



}
