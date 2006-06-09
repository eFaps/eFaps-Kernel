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
 */
public class SearchQuery extends AbstractQuery  {

  /**
   *
   */
  public SearchQuery()  {
  }

//ArrayList<Type> types = new ArrayList<Type>();

Type type = null;

public void setQueryTypes(Context _context, String _types) throws Exception  {
  if (_types != null)  {
    this.type = Type.get(_types);
    setExpandChildTypes(false);
    addSelect(true, type, type, "OID");
    types.add(this.type);
  }
}

ArrayList<Type> types = new ArrayList<Type>();

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

  /**
   * The method adds all attributes in the string beginning with a
   * &quot;$&lt;&quot; and ending with a &quot;&gt;&quot;.
   *
   * @param _context  context for this request
   * @param _text     text string with all attributes
   * @return <i>true</i> if an attribute from the text string is added to the
   *         query
   * @see #add(Attribute)
   * @see #replaceAllInString
   */
  public boolean addAllFromString(Context _context, String _text) throws Exception  {
    boolean ret = false;
    int index = _text.indexOf("$<");
    while (index>=0)  {
      int end = _text.indexOf(">", index);
      if (end<0)  {
        break;
      }
      addSelect(_context, _text.substring(index+2,end));
      index = _text.indexOf("$<", end);
      ret = true;
    }
    return ret;
  }

  /**
   * The instance method replaces all the attributes in the text string
   * beginning with a &quot;$&lt;&quot; and ending with a &quot;&gt;&quot;
   * with the related values.
   *
   * @param _context  context for this request
   * @param _text     text string with attributes to replace with the values
   * @return replaced text string
   * @see #addAllFromString
   */
  public String replaceAllInString(Context _context, String _text) throws Exception  {
    int index = _text.indexOf("$<");
    while (index>=0)  {
      int end = _text.indexOf(">", index);
      if (end<0)  {
        break;
      }
      String expr = _text.substring(index+2,end);

      Object value = get(_context, expr);
      if (value!=null)  {
        _text = _text.substring(0, index) +
            value +
            _text.substring(end+1);
      } else  {
        _text = _text.substring(0, index) + _text.substring(end+1);
      }
      index = _text.indexOf("$<", end);
    }
    return _text;
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * The method adds a single field from a form or a table to the select
   * statement.
   *
   * @param _field  field to add to the query
   * @see #add(Object, Attribute)
   */
  public void add(Field _field) throws Exception  {
//System.out.println("                           add field "+_field);
    addSelect(false, _field, this.type, _field.getExpression());
    if (_field.getAlternateOID()!=null)  {
      addSelect(true, _field, this.type, _field.getAlternateOID());
    }
  }

  /**
   * The method adds an expression to the selectstatement.
   *
   * @param _expression expression to add
   */
  public void addSelect(Context _context, String _expression) throws Exception  {
    addSelect(false, _expression, this.type, _expression);
  }

  /**
   * All object ids for one row are returned. The objects id defined in the
   * expand are returned in the same order.
   *
   * @param _context  eFaps context for this request
   * @return pipe separated string of object ids
   */
  public String getRowOIDs(Context _context) throws Exception  {
    StringBuffer rowOIDs = new StringBuffer();
    boolean first = true;
    for (Type type : types)  {
      String value = getOID(_context, type);
      if (first)  {
        first = false;
      } else  {
        rowOIDs.append('|');
      }
      rowOIDs.append(value);
    }
    return rowOIDs.toString();
  }

  /**
   * The instance method returns the instance for the current selected row.
   *
   * @param _context  context for this request
   */
  public Instance getInstance(Context _context, Field _field) throws Exception  {
    Instance ret = null;

    if (_field!=null && _field.getAlternateOID()!=null)  {
      String value = getOID(_context, _field);
      ret = new Instance(_context, value);
    } else  {
      ret = getInstance(_context, this.type);
    }
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////
}