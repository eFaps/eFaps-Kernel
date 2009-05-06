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

package org.efaps.admin.datamodel;

import java.util.List;
import java.util.Map;

import org.efaps.db.query.CachedResult;


/**
 * @author jmox
 * @version $Id$
 */
public interface MultipleAttributeTypeInterface extends AttributeTypeInterface{

  // ///////////////////////////////////////////////////////////////////////////
  // methods for the interface to the database
  public Map<String,List<Object>> readValues(final CachedResult _rs, final Map<Integer,String> _index2expression);

}
