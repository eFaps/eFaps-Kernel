/*
 * Copyright 2005 The eFaps Team
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
 */

package org.efaps.admin.lifecycle;

import org.efaps.admin.AdminObject;

public abstract class LifeCycleObject extends AdminObject  {

  /**
   * Constructor to set the id and name of the life cycle object.
   *
   * @param _id         id to set
   * @param _name name  to set
   */
  protected LifeCycleObject(long _id, String _name)  {
    super(_id, _name);
  }
}