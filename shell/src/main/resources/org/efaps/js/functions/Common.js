/*
 * Copyright 2003-2007 The eFaps Team
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

importClass(Packages.org.efaps.db.Context);

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

function eFapsCommonSQLTableUpdate(_con, _stmt, _text, _table, _array)  {
  eFapsCommonLog("Update Table '" + _table + "'", _text);
  
  for (var i=0; i<_array.length; i++)  {
    _stmt.execute("alter table " + _table + " add " + _array[i]);
  }
}

/**
 * Insert a version information for an application stored in the database used 
 * to update DML information in the database.
 *
 * @param _name     application name for which the version is inserted
 * @param _version  new version number to insert
 */
function eFapsCommonVersionInsert(_con, _stmt, _name, _version)  {
  eFapsCommonLog("Update application '" + _name + "'", "new version " + _version);
  _stmt.execute(
      "insert into T_COMMONVERSION "
        +   "(NAME,REVISION,CREATOR,CREATED,MODIFIER,MODIFIED) "
        +   "values ('" + _name + "'," + _version + "," 
                + "1," + Context.getDbType().getCurrentTimeStamp() + ","
                + "1," + Context.getDbType().getCurrentTimeStamp()
                + ")"
  );
}

/**
 * Returns for the given application name the current installed version.
 *
 * @param _name   application name for which the version should returned
 */
function eFapsCommonVersionGet(_con, _stmt, _name)  {
  var ret = 0;

  if (Context.getDbType().existsView(_con, "V_COMMONVERSION"))  {
    var rs = _stmt.executeQuery(
        "select VERSION "
          +     "from V_COMMONVERSION "
          +     "where NAME='" + _name + "'"
    );
    if (rs.next())  {
      ret= rs.getLong(1);
    }
    rs.close();
  }
  return ret;
}
