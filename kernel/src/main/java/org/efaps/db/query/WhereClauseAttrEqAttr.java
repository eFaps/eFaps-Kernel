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

package org.efaps.db.query;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.AbstractQuery;

/**
 * The class represents an equal where clause between two attributes.
 *
 * @author tmo
 * @version $Id$
 */
public class WhereClauseAttrEqAttr extends WhereClause  {

  public WhereClauseAttrEqAttr(AbstractQuery _query, Attribute _attr1, Attribute _attr2)  {
    setAttr1(_attr1);
    setAttr2(_attr2);
    setSelectType1(_query.getSelectType(getAttr1().getParent()));
    setSelectType2(_query.getSelectType(getAttr2().getParent()));
  }

  public WhereClauseAttrEqAttr(AbstractQuery.SelectType _selectType1, Attribute _attr1, AbstractQuery.SelectType _selectType2, Attribute _attr2)  {
    setAttr1(_attr1);
    setAttr2(_attr2);
    setSelectType1(_selectType1);
    setSelectType2(_selectType2);
  }

  /**
   * @todo compare does not work if an attribute has more than one sql column!!
   */
  public void appendWhereClause(CompleteStatement _completeStatement, int _orderIndex)  {
    if (_orderIndex<0 || (getSelectType1().getOrderIndex()<_orderIndex && getSelectType2().getOrderIndex()<_orderIndex))  {
String sqlColName1 = getAttr1().getSqlColNames().get(0);
String sqlColName2 = getAttr2().getSqlColNames().get(0);
      _completeStatement.appendWhereAnd();
      _completeStatement.appendWhere(getAttr1().getTable().getSqlTable());
      _completeStatement.appendWhere(getSelectType1().getTypeIndex());
      _completeStatement.appendWhere(".");
//        _completeStatement.appendWhere(getAttr1().getSqlColName());
_completeStatement.appendWhere(sqlColName1);
      _completeStatement.appendWhere("=");
      _completeStatement.appendWhere(getAttr2().getTable().getSqlTable());
      _completeStatement.appendWhere(getSelectType2().getTypeIndex());
      _completeStatement.appendWhere(".");
//        _completeStatement.appendWhere(getAttr2().getSqlColName());
_completeStatement.appendWhere(sqlColName2);
    } else if (_orderIndex>=0 && getSelectType1().getOrderIndex()<_orderIndex && getSelectType2().getOrderIndex()==_orderIndex)  {
String sqlColName1 = getAttr1().getSqlColNames().get(0);
      _completeStatement.appendWhereAnd();
      _completeStatement.appendWhere(getAttr1().getTable().getSqlTable());
      _completeStatement.appendWhere(getSelectType1().getTypeIndex());
      _completeStatement.appendWhere(".");
//        _completeStatement.appendWhere(getAttr1().getSqlColName());
_completeStatement.appendWhere(sqlColName1);
      _completeStatement.appendWhere(" is null");
    } else if (_orderIndex>=0 && getSelectType1().getOrderIndex()==_orderIndex && getSelectType2().getOrderIndex()<_orderIndex)  {
String sqlColName2 = getAttr2().getSqlColNames().get(0);
      _completeStatement.appendWhereAnd();
      _completeStatement.appendWhere(getAttr2().getTable().getSqlTable());
      _completeStatement.appendWhere(getSelectType2().getTypeIndex());
      _completeStatement.appendWhere(".");
//        _completeStatement.appendWhere(getAttr2().getSqlColName());
_completeStatement.appendWhere(sqlColName2);
      _completeStatement.appendWhere(" is null");
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  private Attribute attr1 = null;
  private Attribute attr2 = null;
  private AbstractQuery.SelectType selectType1 = null;
  private AbstractQuery.SelectType selectType2 = null;

  ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #attr1}.
   *
   * @return value of instance variable {@link #attr1}
   * @see #attr1
   * @see #setAttr1
   */
  private Attribute getAttr1()   {
    return this.attr1;
  }

  /**
   * This is the setter method for instance variable {@link #attr1}.
   *
   * @param _attr1 new value for instance variable {@link #attr1}
   * @see #attr1
   * @see #getAttr1
   */
  private void setAttr1(Attribute _attr1)  {
    this.attr1 = _attr1;
  }

  /**
   * This is the getter method for instance variable {@link #attr2}.
   *
   * @return value of instance variable {@link #attr2}
   * @see #attr2
   * @see #setAttr2
   */
  private Attribute getAttr2()   {
    return this.attr2;
  }

  /**
   * This is the setter method for instance variable {@link #attr2}.
   *
   * @param _attr2 new value for instance variable {@link #attr2}
   * @see #attr2
   * @see #getAttr2
   */
  private void setAttr2(Attribute _attr2)  {
    this.attr2 = _attr2;
  }

  /**
   * This is the getter method for instance variable {@link #selectType1}.
   *
   * @return value of instance variable {@link #selectType1}
   * @see #selectType1
   * @see #setSelectType1
   */
  private AbstractQuery.SelectType getSelectType1()   {
    return this.selectType1;
  }

  /**
   * This is the setter method for instance variable {@link #selectType1}.
   *
   * @param _selectType1 new value for instance variable {@link #selectType1}
   * @see #selectType1
   * @see #getSelectType1
   */
  private void setSelectType1(AbstractQuery.SelectType _selectType1)  {
    this.selectType1 = _selectType1;
  }

  /**
   * This is the getter method for instance variable {@link #selectType2}.
   *
   * @return value of instance variable {@link #selectType2}
   * @see #selectType2
   */
  private AbstractQuery.SelectType getSelectType2()   {
    return this.selectType2;
  }

  /**
   * This is the setter method for instance variable {@link #selectType2}.
   *
   * @param _selectType2 new value for instance variable {@link #selectType2}
   * @see #selectType2
   * @see #getSelectType2
   */
  private void setSelectType2(AbstractQuery.SelectType _selectType2)  {
    this.selectType2 = _selectType2;
  }
}
