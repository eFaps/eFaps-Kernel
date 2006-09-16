/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.db;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.efaps.admin.datamodel.Type;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Instance  {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The instance variable stores the type definition for which this class is
   * the instance.
   *
   * @see #getType
   * @see #setType
   */
  private Type type = null;

  /**
   * The instance variable stores the id of the instance in the database.
   *
   * @see #getId
   * @see #setId
   */
  private long id = 0;

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  public Instance(Type _type)  {
    setType(_type);
  }

  public Instance(Context _context, Type _type, long _id)  {
    setType(_type);
    setId(_id);
  }

  public Instance(Context _context, Type _type, String _id)  {
    setType(_type);
    if (_id!=null && _id.length()>0)  {
      setId(Long.parseLong(_id));
    }
  }

  public Instance(Context _context, String _typeName, String _id) throws Exception  {
    if (_typeName!=null && _typeName.length()>0)  {
      setType(Type.get(_typeName));
      if ((_id != null) && (_id.length() > 0))  {
        setId(Long.parseLong(_id));
      }
    }
  }

  public Instance(Context _context, String _oid) throws Exception  {
    if (_oid!=null)  {
      int index = _oid.indexOf(".");
      if (index >= 0)  {
        setType(Type.get(Long.parseLong(_oid.substring(0, index))));
        setId(Long.parseLong(_oid.substring(index+1)));
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  public String getOid()  {
    String ret = null;
    if (getType()!=null && getId()!=0)  {
      ret = getType().getId()+"."+getId();
    }
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * This is the getter method for the instance variable {@link #type}.
   *
   * @return value of instance variable {@link #type}
   * @see #type
   * @see #setType
   */
  public Type getType()  {
    return this.type;
  }

  /**
   * This is the setter method for the instance variable {@link #type}.
   *
   * @param _type  new value for instance variable {@link #type}
   * @see #type
   * @see #getType
   */
  private void setType(Type _type)  {
    this.type = _type;
  }

  /**
   * This is the getter method for the instance variable {@link #id}.
   *
   * @return value of instance variable {@link #id}
   * @see #id
   * @see #setId
   */
  public long getId()  {
    return this.id;
  }

  /**
   * This is the setter method for the instance variable {@link #id}.
   *
   * @param _id  new value for instance variable {@link #id}
   * @see #id
   * @see #getId
   */
  private void setId(long _id)  {
    this.id = _id;
  }

  /**
   * The method returns a string representation of the instance object. It does
   * not replace method {@link #getOid}!.
   *
   * @return string representation of this instance object
   */
  public String toString()  {
    return new ToStringBuilder(this).
      append("oid", getOid()).
      append("type", getType()).
      append("id", getId()).
      toString();
  }
}