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

package org.efaps.db;

/**
 * Abstract class where all eFaps database actions are derived.
 */
abstract class AbstractAction  {

  /**
   * Property name of the file name attribute used in store actions (checkin,
   * checkout).
   */
  protected static final String PROPERTY_STORE_ATTR_FILE_NAME   = "StoreAttributeFileName";

  /**
   * Property name of the file length attribute used in store actions
   * (checkin).
   */
  protected static final String PROPERTY_STORE_ATTR_FILE_LENGTH = "StoreAttributeFileLength";
}
