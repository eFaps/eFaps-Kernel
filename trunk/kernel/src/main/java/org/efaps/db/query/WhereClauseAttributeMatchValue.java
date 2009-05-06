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
 * The class represents an match where clause between an attributes and a
 * value.
 *
 * @author tmo
 * @version $Id$
 */
public class WhereClauseAttributeMatchValue
    extends WhereClauseAttributeCompareValueAbstract  {

  public WhereClauseAttributeMatchValue(final AbstractQuery _query, final Attribute _attr, final String _value)  {
    super(_query, _attr, _value);
  }

  /**
   * @todo compare does not work if an attribute has more than one sql column!!
   * @todo bugfixing for Apache derby!!! (because ID is hardcoded as number value...) see TODO comment
   */
  public void appendWhereClause(final CompleteStatement _completeStatement,
      final int _orderIndex)  {

    if (_orderIndex<0 || getSelType().getOrderIndex()<_orderIndex)  {

String sqlColName = getAttr().getSqlColNames().get(0);

      _completeStatement.appendWhereAnd();
      _completeStatement
          .appendWhere(getAttr().getTable().getSqlTable())
          .appendWhere(getSelType().getTypeIndex())
          .appendWhere(".")
          .appendWhere(sqlColName);

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
}
