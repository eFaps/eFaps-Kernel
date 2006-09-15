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

importClass(java.io.File);

importClass(Packages.org.efaps.db.Insert);
importClass(Packages.org.efaps.db.Instance);
importClass(Packages.org.efaps.db.SearchQuery);

var SQLTABLE = null;

/**
 * Implements the SQL Table represenation in JavaScript.
 */
function SQLTable(_name)  {
  var query = new SearchQuery();
  query.setQueryTypes(Shell.getContext(), "Admin_DataModel_SQLTable");
  query.addWhereExprEqValue(Shell.getContext(), "Name", _name);
  query.addSelect(Shell.getContext(), "OID");
  query.execute(Shell.getContext());

  if (query.next())  {
    var oid = query.get(Shell.getContext(), "OID");
    this.instance = new Instance(Shell.getContext(), oid);
  } else  {
    this.instance = null;
  }
  query.close();

  this.object = new EFapsInstance("Admin_DataModel_SQLTable", _name);

  // add helper methods for getter and setter 
  this._setAttrValue      = Abstract.prototype._setAttrValue;
  this._getAttrValue      = Abstract.prototype._getAttrValue;

  // add getter and setter methods
  this.getInstance        = Abstract.prototype.getInstance;
  this.getId              = Abstract.prototype.getId;
  this.getOid             = Abstract.prototype.getOid;
  this.getType            = Abstract.prototype.getType;
  this.getName            = Abstract.prototype.getName;
  this.getRevision        = Abstract.prototype.getRevision;
  this.setRevision        = Abstract.prototype.setRevision;

  // add property methods
  this._writeProperties   = Abstract.prototype._writeProperties;
  this.addProperty        = Abstract.prototype.addProperty;
  this.cleanupProperties  = Abstract.prototype.cleanupProperties;
  this.deleteProperty     = Abstract.prototype.addProperty;
  this.printProperties    = Abstract.prototype.printProperties;

  // add common methods
  this._writeHeader       = Abstract.prototype._writeHeader;
}

///////////////////////////////////////////////////////////////////////////////
// instance variables

/**
 * Prefix of script file names.
 */
SQLTable.prototype.FILE_PREFIX = new String("SQLTABLE_");

/**
 * Name of the variable used in update scripts.
 */
SQLTable.prototype.VARNAME     = new String("SQLTABLE");

///////////////////////////////////////////////////////////////////////////////
// common methods

SQLTable.prototype.cleanup = function()  {
  this.cleanupProperties();
  this.setSQLTable(("-" + this.getName()).substring(0, 34));
  this.setSQLColumnID("-");
  this.setSQLColumnType(null);
  this.setParentSQLTable(null);
}

SQLTable.prototype._create = function(_name)  {
  var insert = new Insert(Shell.context, "Admin_DataModel_SQLTable");
  insert.add(Shell.context, "Name", _name);
  insert.add(Shell.context, "SQLColumnID", "-");
  insert.add(Shell.context, "SQLTable", ("-" + _name).substring(0, 34));
  insert.executeWithoutAccessCheck();
  this.instance = insert.getInstance();
}

SQLTable.prototype.update = function(_fileName, _objName)  {
  if (this.getOid()==null || this.getOid()=="" || this.getOid()=="0")  {
    print("  - create");
    this._create(_objName);
  } else  {
    print("  - cleanup");
    this.cleanup();
  }
  print("  - import");
  SQLTABLE = this;
  load(_fileName);
  SQLTABLE = null;
}

SQLTable.prototype.print = function()  {
  var query = new Packages.org.efaps.db.SearchQuery();
  query.setObject(Shell.getContext(), this.getOid());
  query.addSelect(Shell.getContext(), "Name");
  query.addSelect(Shell.getContext(), "SQLTable");
  query.addSelect(Shell.getContext(), "SQLColumnID");
  query.addSelect(Shell.getContext(), "SQLColumnType");
//  query.addSelect(Shell.getContext(), "DMTableMain.Name");
  query.execute(Shell.getContext());
  if (query.next())  {
    print("Name:                      "+query.get(Shell.getContext(), "Name"));
    print("SQL Table Name:            "+query.get(Shell.getContext(), "SQLTable"));
    print("Name of ID Column:         "+query.get(Shell.getContext(), "SQLColumnID"));
    print("Name of Type Column:       "+query.get(Shell.getContext(), "SQLColumnType"));
//    print("Parent SQL Table:          "+query.get("DMTableMain.Name"));
  }
  query.close();
  this.printProperties();
}

///////////////////////////////////////////////////////////////////////////////
// getter and setter methods

/**
 * @return SQL Table name
 */
SQLTable.prototype.getSQLTable = function()  {
  return this._getAttrValue("SQLTable");
}

/**
 * Sets the new SQL Table name of this type.
 *
 * @param _newSQLTable  new SQL Table name to set
 */
SQLTable.prototype.setSQLTable = function(_newSQLTable)  {
  this._setAttrValue("SQLTable", _newSQLTable);
}

/**
 * @return SQL Column ID name
 */
SQLTable.prototype.getSQLColumnID = function()  {
  return this._getAttrValue("SQLColumnID");
}

/**
 * Sets the new SQL Column ID of this type.
 *
 * @param _newSQLColumnID  new SQL Column ID to set
 */
SQLTable.prototype.setSQLColumnID = function(_newSQLColumnID)  {
  this._setAttrValue("SQLColumnID", _newSQLColumnID);
}

/**
 * @return SQL Column Type name
 */
SQLTable.prototype.getSQLColumnType = function()  {
  return this._getAttrValue("SQLColumnType");
}

/**
 * Sets the new SQL Column Type of this type.
 *
 * @param _newSQLColumnType  new SQL Column Type to set
 */
SQLTable.prototype.setSQLColumnType = function(_newSQLColumnType)  {
  this._setAttrValue("SQLColumnType", _newSQLColumnType);
}

/**
 * Sets the new SQL New ID Select Statement of this type.
 */
SQLTable.prototype.getParentSQLTable = function()  {
//  var prnt = this.object.DMTableMain;
//  var ret = null;
//  if (prnt && prnt!=null && prnt.ID!=0)  {
//    ret = new SQLTable(this.object.DMTableMain.Name);
//  }
//  return ret;
return "@@@@@ ERROR: NOT IMPLEMENTED";
}

/**
 * Sets the new SQL New ID Select Statement of this type.
 *
 * @param _newParentSQLTable  new parent SQL Table to set
 */
SQLTable.prototype.setParentSQLTable = function(_newParentSQLTable)  {
  if (_newParentSQLTable!=null)  {
    var sqlTable;
    if (typeof(_newParentSQLTable)=="string")  {
      sqlTable = new SQLTable(_newParentSQLTable);
    } else  {
      sqlTable = _newParentSQLTable;
    }
    var instance = new Packages.org.efaps.db.Instance(Shell.getContext(), sqlTable.getOid());
    this._setAttrValue("DMTableMain", instance.getId());
  } else  {
    this._setAttrValue("DMTableMain", null);
  }
}

///////////////////////////////////////////////////////////////////////////////
// static functions

/**
 * Extract the SQL table name from a file name.
 *
 * @param (String)_fileName   file name
 * @return extracted SQL table name
 */
function getSQLTableNameFromFileName(_fileName)  {
  var tmp = new String(_fileName);
  return tmp.substring(SQLTable.prototype.FILE_PREFIX.length, tmp.length-3);
}

/**
 * One file with the given name is imported.
 *
 * @param _fileName   file to import
 */
function importSQLTable(_fileName)  {
  var fileName = new File(_fileName);
  if (fileName.getName().startsWith(SQLTable.prototype.FILE_PREFIX) && fileName.getName().endsWith(".js"))  {
    var objName = getSQLTableNameFromFileName(fileName.getName());
    print("Import SQL Table '"+objName+"'");
    var impSQLTable = new SQLTable(objName);
    impSQLTable.update(_fileName, objName);
  }
}

/**
 * A list of SQL Table Definition files is imported.
 *
 * @param _fileList array with list of files
 */
function importSQLTables(_fileList)  {
  print("");
  print("Import SQL Tables");
  print("~~~~~~~~~~~~~~~~~");
  for (indx in _fileList)  {
    var fileName = new File(_fileList[indx]);
    if (fileName.getName().startsWith(SQLTable.prototype.FILE_PREFIX) && fileName.getName().endsWith(".js"))  {
      var objName = getSQLTableNameFromFileName(fileName.getName());
      var sqlTable = new SQLTable(objName);
      if (sqlTable.getOid()==null || sqlTable.getOid()=="" || sqlTable.getOid()=="0")  {
        print("Create SQL Table '"+objName+"'");
        sqlTable._create(objName);
      }
    }
  }
  for (indx in _fileList)  {
    importSQLTable(_fileList[indx]);
  }
  print("");
}
