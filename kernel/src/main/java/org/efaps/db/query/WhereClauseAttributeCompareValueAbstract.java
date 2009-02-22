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
import org.efaps.admin.datamodel.attributetype.DateTimeType;
import org.efaps.db.AbstractQuery;

/**
 * The class represents an equal where clause between two attributes.
 *
 * @author tmo
 * @version $Id$
 */
abstract class WhereClauseAttributeCompareValueAbstract extends WhereClause  {

  private final Attribute attr;

  private final String value;

  private final AbstractQuery.SelectType selType;

  public WhereClauseAttributeCompareValueAbstract(final AbstractQuery _query,
      final Attribute _attr, final String _value)  {

    this.attr = _attr;
    this.value = _value;
    this.selType = _query.getSelectType(getAttr().getParent());
    getSelType().add4Where(getAttr());
  }

  /**
   * @todo compare does not work if an attribute has more than one sql column!!
   * @todo bugfixing for Apache derby!!! (because ID is hardcoded as number value...) see TODO comment
   */
  protected void appendWhereClause(final CompleteStatement _completeStatement,
      final int _orderIndex, final String _operator)  {

    if (_orderIndex<0 || getSelType().getOrderIndex()<_orderIndex)  {

      final String sqlColName = getAttr().getSqlColNames().get(0);

      _completeStatement.appendWhereAnd();
      _completeStatement
          .appendWhere(getAttr().getTable().getSqlTable())
          .appendWhere(getSelType().getTypeIndex())
          .appendWhere(".")
          .appendWhere(sqlColName)
          .appendWhere(_operator);

// TODO: bug-fixing wg. cloudescape
if (getAttr().getLink()!=null || getAttr().getName().equals("ID"))  {
     _completeStatement.appendWhere(getValue()).appendWhere("");
} else  {
  // in case of DateTimeType the value must be cast to a timestamp
  if (this.attr.getAttributeType().getClassRepr().equals(DateTimeType.class)) {
    _completeStatement.appendWhere(" timestamp ");
  }

     _completeStatement.appendWhere("'").appendWhere(getValue()).appendWhere("'");
}
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #attr}.
   *
   * @return value of instance variable {@link #attr}
   * @see #attr
   */
  protected Attribute getAttr()   {
    return this.attr;
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   *
   * @return value of instance variable {@link #value}
   * @see #value
   */
  protected String getValue()   {
    return this.value;
  }

  /**
   * This is the getter method for instance variable {@link #selType}.
   *
   * @return value of instance variable {@link #selType}
   * @see #selType
   */
  protected AbstractQuery.SelectType getSelType()   {
    return this.selType;
  }
}
