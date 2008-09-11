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
 * Revision:        $Rev:  $
 * Last Changed:    $Date:  $
 * Last Changed By: $Author: $
 */

package org.efaps.admin.datamodel.attributetype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.MultipleAttributeTypeInterface;
import org.efaps.db.query.CachedResult;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id: $
 */
public class MultiLineArrayType extends AbstractType implements MultipleAttributeTypeInterface{

  /* (non-Javadoc)
   * @see org.efaps.admin.datamodel.attributetype.AbstractType#readValue(org.efaps.db.query.CachedResult, java.util.List)
   */
  @Override
  public Object readValue(final CachedResult _rs, final List<Integer> _indexes)
      throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.efaps.admin.datamodel.attributetype.AbstractType#set(java.lang.Object)
   */
  @Override
  public void set(final Object _value) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.efaps.admin.datamodel.AttributeTypeInterface#get()
   */
  public Object get() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.efaps.admin.datamodel.MultipleAttributeTypeInterface#readValues()
   */
  public Map<String,List<Object>> readValues(final CachedResult _rs,
        final Map<Integer, String> _index2expression) {

    final Map<String,List<Object>> ret = new HashMap<String, List<Object>>();

    for (final Entry<Integer, String> entry : _index2expression.entrySet()) {
      final List<?> objList = (List<?>) _rs.getObject(entry.getKey());
      final List<Object> tmp = new ArrayList<Object>();
      for (final Object obj :objList){
        if (obj instanceof String && obj != null){
          tmp.add(((String)obj).trim());
        } else {
          tmp.add(obj);
        }
      }

      ret.put(entry.getValue(), tmp);
    }

    return ret;
  }

}
