/*
 * Copyright 2005 The eFaps Team
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
 */

package org.efaps.db.query;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.AbstractQuery;

/**
 * The class represents an equal where clause between two attributes.
 */
public class WhereClauseAttrEqValue extends WhereClause  {

  public WhereClauseAttrEqValue(AbstractQuery _query, Attribute _attr, String _value)  {
    setAttr(_attr);
    setValue(_value);
    setSelType(_query.getSelectType(getAttr().getParent()));
getSelType().add4Where(getAttr());
  }

  /**
   * @todo compare does not work if an attribute has more than one sql column!!
   * @todo bugfixing for Apache derby!!! (because ID is hardcoded as number value...) see TODO comment
   */
 public void appendWhereClause(CompleteStatement _completeStatement, int _orderIndex)  {
   if (_orderIndex<0 || getSelType().getOrderIndex()<_orderIndex)  {

String sqlColName = getAttr().getSqlColNames().get(0);

     _completeStatement.appendWhereAnd();
     _completeStatement.appendWhere(getAttr().getTable().getSqlTable());
     _completeStatement.appendWhere(getSelType().getTypeIndex());
     _completeStatement.appendWhere(".");
//        _completeStatement.appendWhere(getAttr().getSqlColName());
_completeStatement.appendWhere(sqlColName);
     if (getValue().indexOf('*')>=0)  {
       _completeStatement.appendWhere(" like '").appendWhere(getValue().replace('*','%')).appendWhere("'");
     } else  {
// TODO: bug-fixing wg. cloudescape
if (getAttr().getLink()!=null || getAttr().getName().equals("ID"))  {
         _completeStatement.appendWhere("=").appendWhere(getValue()).appendWhere("");
} else  {
         _completeStatement.appendWhere("='").appendWhere(getValue()).appendWhere("'");
}



      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  private Attribute attr = null;

  private String value = null;

  private AbstractQuery.SelectType selType = null;

  ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #attr}.
   *
   * @return value of instance variable {@link #attr}
   * @see #attr
   * @see #setAttr
   */
  private Attribute getAttr()   {
    return this.attr;
  }

  /**
   * This is the setter method for instance variable {@link #attr}.
   *
   * @param _attr new value for instance variable {@link #attr}
   * @see #attr
   * @see #getAttr
   */
  private void setAttr(Attribute _attr)  {
    this.attr = _attr;
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   *
   * @return value of instance variable {@link #value}
   * @see #value
   * @see #setValue
   */
  private String getValue()   {
    return this.value;
  }

  /**
   * This is the setter method for instance variable {@link #value}.
   *
   * @param _value new value for instance variable {@link #value}
   * @see #value
   * @see #getValue
   */
  private void setValue(String _value)  {
    this.value = _value;
  }

  /**
   * This is the getter method for instance variable {@link #selType}.
   *
   * @return value of instance variable {@link #selType}
   * @see #selType
   * @see #setSelType
   */
  private AbstractQuery.SelectType getSelType()   {
    return this.selType;
  }

  /**
   * This is the setter method for instance variable {@link #selType}.
   *
   * @param _selType1 new value for instance variable {@link #selType}
   * @see #selType
   * @see #getSelType
   */
  private void setSelType(AbstractQuery.SelectType _selType)  {
    this.selType = _selType;
  }
}
