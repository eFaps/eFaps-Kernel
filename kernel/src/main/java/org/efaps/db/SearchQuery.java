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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.ui.Field;
import org.efaps.db.query.WhereClauseAttrEqAttr;
import org.efaps.db.query.WhereClauseAttributeEqualValue;
import org.efaps.db.query.WhereClauseAttributeGreaterValue;
import org.efaps.db.query.WhereClauseAttributeLessValue;
import org.efaps.db.query.WhereClauseAttributeMatchValue;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class SearchQuery extends AbstractQuery  {

  /**
   *
   */
  public SearchQuery()  {
  }

//ArrayList<Type> types = new ArrayList<Type>();


public void setQueryTypes(Context _context, String _types) throws Exception  {
  if (_types != null)  {
    this.type = Type.get(_types);
    setExpandChildTypes(false);
    addSelect(true, type, type, "OID");
    types.add(this.type);
  }
}


  /**
   *
   */
  public void setObject(Context _context, Instance _instance) throws Exception  {
    Type type = _instance.getType();
addSelect(true, type, type, "OID");
types.add(this.type);
this.type = type;
    addWhereExprEqValue(_context, "ID", ""+_instance.getId());
  }

  /**
   *
   */
  public void setObject(Context _context, String _oid) throws Exception  {
    Instance instance = new Instance(_context, _oid);
    Type type = instance.getType();
addSelect(true, type, type, "OID");
types.add(this.type);
this.type = type;
    addWhereExprEqValue(_context, "ID", ""+instance.getId());
  }

  /**
   *
   */
  public void setExpand(Context _context, String _oid, String _expand) throws Exception  {
    setExpand(_context, new Instance(_context, _oid), _expand);
  }

  /**
   *
   */
  public void setExpand(Context _context, Instance _instance, String _expand) throws Exception  {
    StringTokenizer tokens = new StringTokenizer(_expand, ".");
    boolean first = true;
    Type type = _instance.getType();

    while (tokens.hasMoreTokens())  {
      String one = tokens.nextToken();
      Attribute attr = type.getLinks().get(one);
      if (attr==null)  {
        attr = type.getAttribute(one);
      }
      if (attr==null)  {
  throw new Exception("Could not found attribute or link with name '"+one+"' for type '"+type.getName()+"'");
      }
      if (type.isKindOf(attr.getLink()))  {
        type = attr.getParent();
      } else  {
        type = attr.getLink();
      }
      addTypes4Order(type);
      if (first)  {
        addWhereAttrEqValue(_context, attr, ""+_instance.getId());
        first=false;
      } else  {
        addWhereAttrEqAttr(_context, attr, type.getAttribute("ID"));
      }

addSelect(true, type, type, "OID");
types.add(type);

    }
//System.out.println("                           select type "+type);
this.type = type;

//for (Type childType : this.type.getChildTypes())  {
//  types.add(childType);
//}

  }

  //////////////////////////////////////////////////////////////////////////////
  // where clauses

  /**
   *
   * @param _context  eFaps context for this request
   * @param _expr     expression to compare for equal
   * @param _value    value to compare for equal
   */
  public void addWhereExprEqValue(final Context _context, final String _expr, final String _value) throws Exception  {
    Attribute attr = this.type.getAttribute(_expr);
    if (attr==null)  {
throw new Exception("unknown expression '"+_expr+"' for type '"+this.type.getName()+"'");
    }
    getMainWhereClauses().add(new WhereClauseAttributeEqualValue(this, attr, _value));
  }

  /**
   *
   * @param _context  eFaps context for this request
   * @param _expr     expression to compare for equal
   * @param _value    value to compare for equal
   */
  public void addWhereExprMatchValue(final Context _context, final String _expr, final String _value) throws Exception  {
    Attribute attr = this.type.getAttribute(_expr);
    if (attr==null)  {
throw new Exception("unknown expression '"+_expr+"' for type '"+this.type.getName()+"'");
    }
    getMainWhereClauses().add(new WhereClauseAttributeMatchValue(this, attr, _value));
  }

  /**
   *
   * @param _context  eFaps context for this request
   * @param _expr     expression to compare for greater
   * @param _value    value to compare for equal
   */
  public void addWhereExprGreaterValue(final Context _context, final String _expr, final String _value) throws Exception  {
    Attribute attr = this.type.getAttribute(_expr);
    if (attr==null)  {
throw new Exception("unknown expression '"+_expr+"' for type '"+this.type.getName()+"'");
    }
    getMainWhereClauses().add(new WhereClauseAttributeGreaterValue(this, attr, _value));
  }

  /**
   *
   * @param _context  eFaps context for this request
   * @param _expr     expression to compare for less
   * @param _value    value to compare for equal
   */
  public void addWhereExprLessValue(final Context _context, final String _expr, final String _value) throws Exception  {
    Attribute attr = this.type.getAttribute(_expr);
    if (attr==null)  {
throw new Exception("unknown expression '"+_expr+"' for type '"+this.type.getName()+"'");
    }
    getMainWhereClauses().add(new WhereClauseAttributeLessValue(this, attr, _value));
  }

  /**
   *
   * @param _expr
   * @param _value
   */
  public void addWhereExprEqValue(Context _context, String _expr, long _value) throws Exception  {
    addWhereExprEqValue(_context, _expr, ""+_value);
  }

  /**
   *
   * @param _attr
   * @param _value
   */
  public void addWhereAttrEqValue(Context _context, Attribute _attr, String _value)  {
    getMainWhereClauses().add(new WhereClauseAttributeEqualValue(this, _attr, _value));
  }

  /**
   * @param _attr1
   * @param _attr2
   */
  public void addWhereAttrEqAttr(Context _context, Attribute _attr1, Attribute _attr2)  {
    getMainWhereClauses().add(new WhereClauseAttrEqAttr(this, _attr1, _attr2));
  }

  //////////////////////////////////////////////////////////////////////////////

   /////////////////////////////////////////////////////////////////////////////
}