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
 * Author:          tmo
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

/**
 * Write out some log stuff. The format is:
 * <ul>
 *   <li>
 *     if a subject and text is given:<br/>
 *     <code>[SPACE][SPACE]-[SUBJECT][SPACE]([TEXT])</code>
 *   </li>
 *   <li>
 *     if only subject is given:<br/>
 *     <code>[SPACE][SPACE]-[SUBJECT]</code>
 *   </li>
 *   <li>otherwise nothing is printed</li>
 * <ul>
 *
 * @param _subject  subject of the log text
 * @param _text     text of the log text
 */
function eFapsCommonLog(_subject, _text)  {
  if (_text!=null && _subject!=null)  {
    print("  - " + _subject + "  (" + _text + ")");
  } else if (_subject!=null)  {
    print("  - " + _subject);
  }
}

function eFapsCommonSQLTableCreate(_con, _stmt, _text, _table, _parentTable, _array)  {
  eFapsCommonLog("Create Table '" + _table + "'", _text);
  
  Context.getDbType().createTable(_con, _table, _parentTable);
  for (var i=0; i<_array.length; i++)  {
    _stmt.execute("alter table " + _table + " add " + _array[i]);
  }
}

function eFapsCommonSQLTableUpdate(_con, _stmt, _text, _table, _array)  {
  eFapsCommonLog("Update Table '" + _table + "'", _text);
  
  for (var i=0; i<_array.length; i++)  {
    _stmt.execute("alter table " + _table + " add " + _array[i]);
  }
}
