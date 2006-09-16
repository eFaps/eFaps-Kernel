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
   */
  private final Type type;

  /**
   * The instance variable stores the database id of the instance in the 
   * database.
   *
   * @see #getId
   */
  private final long id;

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor used if no object exists but the type is know (e.g. if a new
   * object will be created...). The database id is set to 0.
   *
   * @param _type type of the instance
   */
  public Instance(final Type _type)  {
    this.type = _type;
    this.id = 0;
  }

  /**
   * Constructor used if the type and the database id is known.
   *
   * @param _type type of the instance
   * @param _id   id in the database of the instance
   */
  public Instance(final Type _type, final long _id)  {
    this.type = _type;
    this.id = _id;
  }

  /**
   * Constructor used if the type and the database id is known. The database id 
   * is defined in a string and converted to a long.
   *
   * @param _type type of the instance
   * @param _id   id in the database of the instance as string
   */
  public Instance(final Type _type, final String _id)  {
    this.type = _type;
    if ((_id != null) && (_id.length() > 0))  {
      this.id = Long.parseLong(_id);
    } else  {
      this.id = 0;
    }
  }

  /**
   * Constructor used if the type and the database id is known. The type is 
   * only known as string and searched in the cache. The database id is defined 
   * in a string and converted to a long.
   *
   * @param _type type of the instance as string
   * @param _id   id in the database of the instance as string
   */
  public Instance(final String _type, final String _id)  {
    if ((_type != null) && (_type.length() > 0))  {
      this.type = Type.get(_type);
    } else  {
      this.type = null;
    }
    if ((_id != null) && (_id.length() > 0))  {
      this.id = Long.parseLong(_id);
    } else  {
      this.id = 0;
    }
  }

  /**
   * Constructor used if the string representation of the object id is known.
   *
   * @param _oid  objecd id in string representation
   */
  public Instance(final String _oid)  {
    if (_oid != null)  {
      int index = _oid.indexOf(".");
      if (index >= 0)  {
        this.type = Type.get(Long.parseLong(_oid.substring(0, index)));
        this.id = Long.parseLong(_oid.substring(index+1));
      } else  {
        this.type = null;
        this.id = 0;
      }
    } else  {
      this.type = null;
      this.id = 0;
    }
  }

  /**
   * @deprecated
   */
  public Instance(final Context _context, final Type _type, final long _id)  {
    this(_type, _id);
  }

  /**
   * @deprecated
   */
  public Instance(final Context _context, final Type _type, final String _id)  {
    this(_type, _id);
  }

  /**
   * @deprecated
   */
  public Instance(final Context _context, final String _typeName, final String _id)  {
    this(_typeName, _id);
  }

  /**
   * @deprecated
   */
  public Instance(final Context _context, final String _oid)  {
    this(_oid);
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The string representation which is defined by this instance is returned.
   *
   * @return string representation of the object id
   */
  public String getOid()  {
    String ret = null;
    if ((getType() != null) && (getId() != 0))  {
      ret = getType().getId() + "." + getId();
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
   * This is the getter method for the instance variable {@link #id}.
   *
   * @return value of instance variable {@link #id}
   * @see #id
   */
  public long getId()  {
    return this.id;
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