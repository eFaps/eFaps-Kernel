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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

  private final String attributeName;

  private final Set<String> setAttributes = new HashSet<String>();

  /**
   * @param _id
   * @param _uuid
   * @param _name
   * @param typeLinkId
   * @param attributeType
   * @throws CacheReloadException
   */
  protected AttributeSet(final long _id,
                        final Type _type,
                        final String _name,
                        final AttributeType _attributeType,
                        final String _sqlColNames,
                        final long _tableId,
                        final long _typeLinkId) throws CacheReloadException {
    super(_id, null, evaluateName(_type.getName(), _name));

    this.attributeName = (_name == null) ? null : _name.trim();

    getTypeCache().add(this);
    readFromDB4Properties();

    this.attributeType = _attributeType;

    final Attribute attr = new Attribute(_id, _name, _sqlColNames, SQLTable
        .get(_tableId), AttributeType.get("Link"), null);
    attr.setParent(this);
    addAttribute(attr);

    attr.setLink(_type);
    _type.addLink(attr);

    if (_typeLinkId > 0){
      final Type parent = Type.get(_typeLinkId);
      this.setParentType(parent);
      parent.addChildType(this);
      getAttributes().putAll(parent.getAttributes());
    }

  }

  public AttributeType getAttributeType() {
    return this.attributeType;
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
    return this.getAttribute(this.attributeName).getSqlColNames();
  }

  @Override
  protected void addAttribute(final Attribute _attribute) {
    super.addAttribute(_attribute);
    // in the superconstructur this method is called, so the set might not be
    // initialised
    if (this.setAttributes!=null) {
      this.setAttributes.add(_attribute.getName());
    }
  }

  public Set<String> getSetAttributes() {
    return this.setAttributes;
  }

  public static String evaluateName(final String _typeName, final String _name) {
    final StringBuilder ret = new StringBuilder();
    ret.append(_typeName).append(":").append(_name).toString();
    return ret.toString();
  }

  public static AttributeSet get(final String _typeName, final String _name){
   return (AttributeSet) Type.get(evaluateName(_typeName, _name));
  }

  /**
   * @param name
   * @param expression
   * @return
   */
  public static AttributeSet find(final String _typeName, final String _name) {
    AttributeSet ret = (AttributeSet) Type.get(evaluateName(_typeName, _name));
    if (ret==null){
      if (Type.get(_typeName).getParentType()!=null) {
        ret = AttributeSet.find(Type.get(_typeName).getParentType().getName(), _name);
      }
    }
    return ret;
  }


}
