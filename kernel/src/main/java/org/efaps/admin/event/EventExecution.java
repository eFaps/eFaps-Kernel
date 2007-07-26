/*
 * Copyright 2003 - 2007 The eFaps Team
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

package org.efaps.admin.event;

import org.efaps.util.EFapsException;

/**
 * This interface is for the Programs loaded dynamically from the efapsdatabase
 * with the efapsClassLoader. To be invoked the Classes loaded with the
 * efapsclassloader must use this interfacs!
 * 
 * @author jmo
 * @version $Id$
 * 
 */
public interface EventExecution {

  /**
   * This method is calles from efaps to invoke the class
   * 
   * @param _context
   *          Context of the
   * @param _instance
   * @param _map
   *          Map with values from the trigger
   * @throws EFapsException 
   */
  public Return execute(final Parameter _parameter) throws EFapsException;
}
