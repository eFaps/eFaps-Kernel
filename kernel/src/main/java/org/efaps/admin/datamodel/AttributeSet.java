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

package org.efaps.admin.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id$
 */
public class AttributeSet extends Type{

  private final AttributeType attributeType;

  private final List<String> sqlColNames = new ArrayList<String>();

  /**
   * @param _id
   * @param _uuid
   * @param _name
   * @param attributeType
   * @throws CacheReloadException
   */
  protected AttributeSet(final long _id, final String _typeName, final String _name, final AttributeType _attributeType, final String _sqlColNames) throws CacheReloadException {
    super(_id, UUID.randomUUID().toString(), _typeName  + ":" + _name);
    getTypeCache().add(this);
    readFromDB4Properties();
    this.attributeType = _attributeType;
    final StringTokenizer tokens = new StringTokenizer(_sqlColNames.trim(), ",");
    while (tokens.hasMoreTokens()) {
      final String colName = tokens.nextToken().trim();
      getSqlColNames().add(colName);

    }
  }

  public AttributeType getAttributeType() {
    return this.attributeType;
  }

  public static String evaluateName(final String _typeName, final String _name) {
    return _typeName  + ":" + _name;
  }

  public MultipleAttributeTypeInterface getAttributeTypeInstance() throws EFapsException {
    final MultipleAttributeTypeInterface ret = (MultipleAttributeTypeInterface) this.attributeType.newInstance();
    return ret;
  }
  /**
   * This is the getter method for instance variable {@link #sqlColNames}.
   *
   * @return value of instance variable {@link #sqlColNames}
   * @see #sqlColNames
   */
  public List<String> getSqlColNames() {
    return this.sqlColNames;
  }
}
