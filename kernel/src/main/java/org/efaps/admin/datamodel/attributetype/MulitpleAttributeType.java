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
import java.util.List;

import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.db.query.CachedResult;

/**
 * Class used as a wrapper to return, update etc. of a list of Attributes.
 *
 * @author jmox
 * @version $Id: $
 */
public class MulitpleAttributeType extends AbstractType{

  private final AttributeTypeInterface attrInterf;

  /**
   * @param _attrInterf
   */
  public MulitpleAttributeType(final AttributeTypeInterface _attrInterf) {
    this.attrInterf = _attrInterf;
  }

  /**
   * @param _rs
   * @param _indexes
   * @return
   */
  @Override
  public Object readValue(final CachedResult _rs,
                          final List<Integer> _indexes) {
    final Object values = _rs.getObject(_indexes.get(0).intValue());
    final List<Object> ret = new ArrayList<Object>();
    for (final Object value : (List<?>) values){
      attrInterf.set(value);
      ret.add(attrInterf.get());
    }
    return ret;
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


}
