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

package org.efaps.db.query;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.AbstractQuery;

/**
 * The class represents an equal where clause between two attributes.
 *
 * @author tmo
 * @version $Id$
 */
public class WhereClauseAttributeEqualValue
    extends WhereClauseAttributeCompareValueAbstract  {

  public WhereClauseAttributeEqualValue(final AbstractQuery _query, final Attribute _attr, final String _value)  {
    super(_query, _attr, _value);
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
}
