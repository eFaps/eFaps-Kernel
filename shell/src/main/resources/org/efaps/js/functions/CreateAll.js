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

importClass(Packages.org.apache.commons.jexl.JexlHelper);
importClass(Packages.org.apache.commons.jexl.JexlContext);

importClass(Packages.org.efaps.db.Context);
importClass(Packages.org.efaps.admin.user.Person);
importClass(Packages.org.efaps.db.databases.AbstractDatabase);
importClass(Packages.org.efaps.update.access.AccessSetUpdate);
importClass(Packages.org.efaps.update.access.AccessTypeUpdate);
importClass(Packages.org.efaps.update.datamodel.SQLTableUpdate);
importClass(Packages.org.efaps.update.datamodel.TypeUpdate);
importClass(Packages.org.efaps.update.integration.WebDAVUpdate);
importClass(Packages.org.efaps.update.ui.CommandUpdate);
importClass(Packages.org.efaps.update.ui.FormUpdate);
importClass(Packages.org.efaps.update.ui.ImageUpdate);
importClass(Packages.org.efaps.update.ui.MenuUpdate);
importClass(Packages.org.efaps.update.ui.SearchUpdate);
importClass(Packages.org.efaps.update.ui.TableUpdate);
importClass(Packages.org.efaps.update.user.JAASSystemUpdate);
importClass(Packages.org.efaps.update.user.RoleUpdate);

var TYPE_INTEGER      = Context.getDbType().getColumnType(AbstractDatabase.ColumnType.INTEGER);
var TYPE_STRING_SHORT = Context.getDbType().getColumnType(AbstractDatabase.ColumnType.STRING_SHORT);
var TYPE_STRING_LONG  = Context.getDbType().getColumnType(AbstractDatabase.ColumnType.STRING_LONG);
var TYPE_DATETIME     = Context.getDbType().getColumnType(AbstractDatabase.ColumnType.DATETIME);
var TYPE_BLOB         = Context.getDbType().getColumnType(AbstractDatabase.ColumnType.BLOB);
var CURRENT_TIMESTAMP = Context.getDbType().getCurrentTimeStamp();

function _exec(_stmt, _subject, _text, _cmd)  {
  eFapsCommonLog(_subject, _text);
  var bck = _stmt.execute(_cmd);
}

function _insert(_stmt, _subject, _text, _cmd, _table)  {
  var ret;

  eFapsCommonLog(_subject, _text);

  _stmt.execute(_cmd);

  var rs = _stmt.executeQuery("select max(ID) from " + _table);
  
  if (rs.next())  {
    ret= rs.getString(1);
  }
  return ret;
}


/**
 * Updates the password for person 'Administrator' to 'Administrator'..
 *
 * @see #eFapsCreateAll
 */
function _eFapsCreateAllUpdatePassword()  {
  print("");
  print("Update Administrator Password");
  print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

  try  {
    Shell.transactionManager.begin();

    var c = Context.newThreadContext(Shell.transactionManager.getTransaction(), "Administrator");
    Shell.setContext(c);
    c.getPerson().setPassword(c, "Administrator");
    print("  - Done");
    Shell.transactionManager.commit();
    c.close();
  } catch (e)  {
    print("  - Error:"+e);
    try  {
      Shell.transactionManager.rollback();
      c.close();
    } catch (e)  {
    }
  }
}

/**
 * Import all predefined data model and configuration items
 * found in the jar file under the path <code>org/efaps/js/definitions</code>.
 *
 * @see #eFapsCreateAll
 */
function _eFapsCreateAllImportDataModel()  {
  var fileList = eFapsGetAllFiles("org/efaps/js/definitions", true);

  importSQLTables(fileList);
  importTypes(fileList);
}
  
/**
 * Import all XML files found in the sub directories.
 */
function _eFapsCreateAllImportXMLFiles(_version)  {
  var fileList = eFapsGetAllFiles("org/efaps/js/definitions", true);
  
  var jexlContext = JexlHelper.createContext();
  jexlContext.getVars().put("version", 
                            Packages.java.lang.Integer.parseInt(_version));

  // image
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = ImageUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
  // rolle
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = RoleUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
  // access type
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = AccessTypeUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
  // access set
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = AccessSetUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
  // sql table
/*  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = SQLTableUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
*/  // type
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = TypeUpdate.readXMLFile(file);
      if (update != null)  {
       print('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
  // JAAS system
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = JAASSystemUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
  // command
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = CommandUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
  // menu
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = MenuUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
  // search
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = SearchUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
  // form
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = FormUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
  // table
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = TableUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
  // webDAV
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = WebDAVUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
}

/**
 * Rebuilds a complete new eFaps instance in the database.
 * Following steps are made:
 * <ul>
 *   <li>delete all current existing tables of this database user</li>
 *   <li>create all needed eFaps tables including some rudimentary data model 
 *       definition needed to use eFaps standard import functionality </li>
 *   <li>reset context to load all user and roles</li>
 *   <li>import all other standard data model and user interface configuration 
 *       items</li>
 *   <li>set first password for person <i>Administrator</i> needed for login 
 *       via web interface</li>
 * </ul>
 *
 * @see #_eFapsCreateAllImport
 * @see #_eFapsCreateAllUpdatePassword
 */
function eFapsCreateAll()  {
  deleteAll();
  createAll();

  print("############ Reload Cache");
  reloadCache();

  Shell.transactionManager.begin();
  var context = Context.newThreadContext(Shell.transactionManager.getTransaction(), "Administrator");
  Shell.setContext(context);
  _eFapsUpdateSQLTables(context, "4");
  Shell.transactionManager.commit();
  context.close();

  print("############ Reload Cache");
  reloadCache();

  Shell.transactionManager.begin();
  var context = Context.newThreadContext(Shell.transactionManager.getTransaction(), "Administrator");
  Shell.setContext(context);
  _eFapsCreateAllImportDataModel();
  Shell.transactionManager.commit();
  context.close();

  print("############ Reload Cache");
  reloadCache();

  Shell.transactionManager.begin();
  var context = Context.newThreadContext(Shell.transactionManager.getTransaction(), "Administrator");
  _eFapsCreateAllImportXMLFiles('2');
  _eFapsCreateAllImportXMLFiles('3');
  Shell.transactionManager.commit();
  context.close();

  print("############ Reload Cache");
  reloadCache();

  _eFapsCreateAllUpdatePassword();
}

function _eFapsCreateInsertSQLTable(_stmt, _text, _uuid, _name, _sqlTable, _sqlColId, _sqlColType, _tableMain)  {
  var sqlColType = (_sqlColType==null ? "null" : "'"+_sqlColType+"'");

  var rs = _stmt.executeQuery("select ID from ABSTRACT where NAME='Admin_DataModel_SQLTable'");
  var typeIdSQLTable = "-20000";
  if (rs.next())  {
    typeIdSQLTable = rs.getString(1);
  }
  rs.close();

  // get id for SQL Table defined in _tableMain
  var tableMainId = "null";
  if (_tableMain != null)  {
    var rs = _stmt.executeQuery("select ID from ABSTRACT where NAME='" + _tableMain + "'");
    rs.next();
    tableMainId = rs.getString(1);
    rs.close();
  }

  var ret = _insert(_stmt, _text, null, 
      "insert into ABSTRACT "+
          "(TYPEID,UUID,NAME,REVISION,CREATOR,CREATED,MODIFIER,MODIFIED) "
          +"values  (" + typeIdSQLTable 
                        + ", '" + _uuid + "'"
                        + ",'" + _name + "'"
                        + ",''"
                        + ", 1"
                        + "," + CURRENT_TIMESTAMP 
                        + ",1"
                        + "," + CURRENT_TIMESTAMP + ")", "ABSTRACT");
  _exec(_stmt, null, null,  "insert into DMTABLE values  (" + ret + ",'" + _sqlTable + "','" + _sqlColId + "'," + sqlColType + "," + tableMainId + ")");
  return ret;
}

function _eFapsCreateInsertType(_stmt, _text, _uuid, _name, _parentType)  {
  // get id for type 'Admin_DataModel_Type'
  var rs = _stmt.executeQuery("select ID from ABSTRACT where NAME='Admin_DataModel_Type'");
  var typeIdType = "-21000";
  if (rs.next())  {
    typeIdType = rs.getString(1);
  }
  rs.close();

  // get id for given type name in _parentType
  var parentTypeId = "null";
  if (_parentType != null)  {
    rs = _stmt.executeQuery("select ID from ABSTRACT where NAME='" + _parentType + "'");
    rs.next();
    parentTypeId = rs.getString(1);
    rs.close();
  }

  var ret = _insert(_stmt, _text, null, 
      "insert into ABSTRACT "+
          "(TYPEID,NAME,UUID,REVISION,CREATOR,CREATED,MODIFIER,MODIFIED) "
          +"values  (" + typeIdType + ", '" + _name + "','" + _uuid + "','',1," + CURRENT_TIMESTAMP + ",1," + CURRENT_TIMESTAMP + ")", "ABSTRACT");
  _exec(_stmt, null, null, "insert into DMTYPE values  (" + ret + ", " + parentTypeId + ", null)");
  return ret;
}

function _eFapsCreateInsertAttr(_stmt, _tableId, _typeId, _name, _sqlColumn, _attrTypeId, _typeLink)  {
  var rs = _stmt.executeQuery("select ID from ABSTRACT where NAME='Admin_DataModel_Attribute'");
  var typeIdAttr = "-22000";
  if (rs.next())  {
    typeIdAttr = rs.getString(1);
  }
  rs.close();

  // get id for given type name in _typeLink
  var typeLinkId = "null";
  if (_typeLink != null)  {
    rs = _stmt.executeQuery("select ID from ABSTRACT where NAME='" + _typeLink + "'");
    rs.next();
    typeLinkId = rs.getString(1);
    rs.close();
  }

  var ret = _insert(_stmt, null, null, 
      "insert into ABSTRACT "+
          "(TYPEID,NAME,REVISION,CREATOR,CREATED,MODIFIER,MODIFIED) "
          +"values  (" + typeIdAttr + ", '"+_name+"', '', 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")", "ABSTRACT");
  _exec(_stmt, null, null, "insert into DMATTRIBUTE values  (" + ret + ", " + _tableId + ", " + _typeId + ",  " + _attrTypeId + ", " + typeLinkId + ", '" + _sqlColumn + "')");
  return ret;
}

function _eFapsCreateInsertProp(_stmt, _abstractId, _key, _value)  {
  _exec(_stmt, null, null, 
      "insert into PROPERTY (ABSTRACT,NAME,VALUE) "
          +"values("+_abstractId+",'"+_key+"','"+_value+"')");
}

/**
 * The private function inserts the SQL Tables for the event definitions.
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateEventTablesStep3(_context)  {
  print("Create Event SQL Table");
var con = _context.getConnectionResource();
var _con = con.getConnection();
var _stmt = _con.createStatement();
  // must be created for reload-cache-functionality and possibility to define types
  text = "Insert Table for 'Admin_Event_Definition'";
  var sqlTableEventDef = _eFapsCreateInsertSQLTable(_stmt, text, "1238f647-9cf5-4d9f-883e-c6d24db538f5", "Admin_Event_DefinitionSQLTable", "EVENTDEF", "ID", null, "Admin_AbstractSQLTable");

  text = "Insert Type for 'Admin_Event_Definition'";
  var typeIdEventDef = _eFapsCreateInsertType(_stmt, text, "9c1d52f4-94d6-4f95-ab81-bed23884cf03", "Admin_Event_Definition", "Admin_Abstract");
  _eFapsCreateInsertAttr(_stmt, sqlTableEventDef, typeIdEventDef, "IndexPosition",    "INDEXPOS",         210, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableEventDef, typeIdEventDef, "Abstract",         "ABSTRACT",         400, "Admin_Abstract");
con.commit();
}

/**
 * The private function creates all user tables.
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateUserTablesStep1(_context)  {
  print("Create User Tables");

  var con = _context.getConnectionResource();
var _con = con.getConnection();
var _stmt = _con.createStatement();

  _exec(_stmt, "Insert JAAS System eFaps", null,
    "insert into USERJAASSYSTEM(NAME, UUID, "
            + "CREATOR, CREATED, MODIFIER, MODIFIED, "
            + "CLASSNAMEPERSON,"
            + "CLASSNAMEROLE,"
            + "CLASSNAMEGROUP,"
            + "METHODPERSONKEY,"
            + "METHODPERSONNAME,"
            + "METHODROLEKEY,"
            + "METHODGROUPKEY"
            + ") "+
        "values ('eFaps', '878a1347-a5f3-4a68-a9a4-d214e3570a62',"
            + "1, " + CURRENT_TIMESTAMP + ", 1, " + CURRENT_TIMESTAMP + ", "
            + "'org.efaps.jaas.efaps.PersonPrincipal',"
            + "'org.efaps.jaas.efaps.RolePrincipal',"
            + "'org.efaps.jaas.efaps.GroupPrincipal',"
            + "'getName',"
            + "'getName',"
            + "'getName',"
            + "'getName'"
            + ")"
  );

  _exec(_stmt, "Insert Administrator Person", null,
    "insert into USERABSTRACT(TYPEID, NAME, CREATOR, CREATED, MODIFIER, MODIFIED, STATUS) "+
        "values (-10000, 'Administrator', 1, " + CURRENT_TIMESTAMP + ", 1, " + CURRENT_TIMESTAMP + ", 10001)"
  );
  _exec(_stmt, null, null,
    "insert into USERPERSON(ID, FIRSTNAME, LASTNAME, EMAIL, URL, PASSWORD) "+
        "values (1,'The','Administrator','info@efaps.org','www.efaps.org', '')"
  );

  _exec(_stmt, "Assign Person Administrator to JAAS System eFaps", null,
    "insert into USERJAASKEY(KEY, CREATOR, CREATED, MODIFIER, MODIFIED,USERABSTRACT,USERJAASSYSTEM) "+
        "values ('Administrator', 1, " + CURRENT_TIMESTAMP + ", 1, " + CURRENT_TIMESTAMP + ", 1, 1)"
  );

  _exec(_stmt, "Insert Administrator Role",  null,
    "insert into USERABSTRACT(TYPEID, NAME, CREATOR, CREATED, MODIFIER, MODIFIED, STATUS) "+
        "values (-11000, 'Administration', 1, " + CURRENT_TIMESTAMP + ", 1, " + CURRENT_TIMESTAMP + ", 10001)"
  );

  _exec(_stmt, "Assign Role Administration to JAAS System eFaps", null,
    "insert into USERJAASKEY(KEY, CREATOR, CREATED, MODIFIER, MODIFIED,USERABSTRACT,USERJAASSYSTEM) "+
        "values ('Administration', 1, " + CURRENT_TIMESTAMP + ", 1, " + CURRENT_TIMESTAMP + ", 2, 1)"
  );

  _exec(_stmt, "Connect Administrator Person to Role Administration", null,
    "insert into USERABSTRACT2ABSTRACT(TYPEID,CREATOR,CREATED,MODIFIER,MODIFIED,USERABSTRACTFROM,USERABSTRACTTO,USERJAASSYSTEM) "+
        "values (-12000, 1, " + CURRENT_TIMESTAMP + ", 1, " + CURRENT_TIMESTAMP + ", 1, 2, 1)"
  );

con.commit();
}

/**
 * The private function creates all user tables.
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateUserTablesStep2(_context)  {

    print("Insert User Types and create User Views");
var con = _context.getConnectionResource();
var _con = con.getConnection();
var _stmt = _con.createStatement();

    text = "Insert Type for 'Admin_User_Person' (only to store ID for type)";
    var typeIdPerson        = _eFapsCreateInsertType(_stmt, text, "fe9d94fd-2ed8-4c44-b1f0-00e150555888", "Admin_User_Person", null);

    text = "Insert Type for 'Admin_User_Role' (only to store ID for type)";
    var typeIdRole          = _eFapsCreateInsertType(_stmt, text, "e4d6ecbe-f198-4f84-aa69-5a9fd3165112", "Admin_User_Role", null);

    text = "Insert Type for 'Admin_User_Group' (only to store ID for type)";
    var typeIdGroup         = _eFapsCreateInsertType(_stmt, text, "f5e1e2ff-bfa9-40d9-8340-a259f48d5ad9", "Admin_User_Group", null);
   
    text = "Insert Type for 'Admin_User_Person2Role' (only to store ID for type)";
    var typeIdPerson2Role   = _eFapsCreateInsertType(_stmt, text, "37deb6ae-3e1c-4642-8823-715120386fc3", "Admin_User_Person2Role", null);

    text = "Insert Type for 'Admin_User_Person2Group' (only to store ID for type)";
    var typeIdPerson2Group  = _eFapsCreateInsertType(_stmt, text, "fec64148-a39b-4f69-bedd-9c3bcfe8e1602", "Admin_User_Person2Group", null);

    _exec(_stmt, "Table 'USERABSTRACT'", "update type id for persons",
      "update USERABSTRACT set TYPEID=" + typeIdPerson + " where TYPEID=-10000"
    );
    _exec(_stmt, "Table 'USERABSTRACT'", "update type id for persons",
      "update USERABSTRACT set TYPEID=" + typeIdRole + " where TYPEID=-11000"
    );
    eFapsCommonSQLTableUpdate(_con, _stmt, "Foreign Contraint for column TYPEID", "USERABSTRACT", [
        ["constraint USERABSTR_FK_TYPEID foreign key(TYPEID) references DMTYPE(ID)"]
    ]);

    _exec(_stmt, "Table 'USERABSTRACT2ABSTRACT'", "update type id for connection between person and role",
      "update USERABSTRACT2ABSTRACT set TYPEID="+typeIdPerson2Role+" where TYPEID=-12000"
    );
    eFapsCommonSQLTableUpdate(_con, _stmt, "Foreign Contraint for column TYPEID", "USERABSTRACT2ABSTRACT", [
        ["constraint USRABS2ABS_FK_TYPEID foreign key(TYPEID) references DMTYPE(ID)"]
    ]);

    _exec(_stmt, "View 'V_USERPERSONJASSKEY'", "view representing all persons related to the JAAS keys",
      "create or replace view V_USERPERSONJASSKEY as "
        + "select "
        +       "USERABSTRACT.ID,"
        +       "USERABSTRACT.NAME,"
        +       "USERJAASKEY.USERJAASSYSTEM as JAASSYSID,"
        +       "USERJAASKEY.KEY as JAASKEY "
        +   "from USERABSTRACT,USERJAASKEY "
        +   "where USERABSTRACT.TYPEID=" + typeIdPerson + " "
        +       "and USERABSTRACT.ID=USERJAASKEY.USERABSTRACT"
    );


    _exec(_stmt, "View 'V_USERROLE'", "view representing all roles",
      "create or replace view V_USERROLE as "
        + "select "
        +       "USERABSTRACT.ID,"
        +       "USERABSTRACT.NAME "
        +   "from USERABSTRACT "
        +   "where USERABSTRACT.TYPEID=" + typeIdRole
    );

    _exec(_stmt, "View 'V_USERROLEJASSKEY'", "view representing all roles related to the JAAS keys",
      "create or replace view V_USERROLEJASSKEY as "
        + "select "
        +       "USERABSTRACT.ID,"
        +       "USERABSTRACT.NAME,"
        +       "USERJAASKEY.USERJAASSYSTEM as JAASSYSID,"
        +       "USERJAASKEY.KEY as JAASKEY "
        +   "from USERABSTRACT,USERJAASKEY "
        +   "where USERABSTRACT.TYPEID=" + typeIdRole + " "
        +       "and USERABSTRACT.ID=USERJAASKEY.USERABSTRACT"
    );

    _exec(_stmt, "View 'V_USERGROUP'", "view representing all groups",
      "create or replace view V_USERGROUP as "+
        "select "+
            "USERABSTRACT.ID,"+
            "USERABSTRACT.NAME "+
          "from USERABSTRACT "+
          "where USERABSTRACT.TYPEID="+typeIdGroup
    );

    _exec(_stmt, "View 'V_USERGROUPJASSKEY'", "view representing all groups related to the JAAS keys",
      "create or replace view V_USERGROUPJASSKEY as "
        + "select "
        +       "USERABSTRACT.ID,"
        +       "USERABSTRACT.NAME,"
        +       "USERJAASKEY.USERJAASSYSTEM as JAASSYSID,"
        +       "USERJAASKEY.KEY as JAASKEY "
        +   "from USERABSTRACT,USERJAASKEY "
        +   "where USERABSTRACT.TYPEID=" + typeIdGroup + " "
        +       "and USERABSTRACT.ID=USERJAASKEY.USERABSTRACT"
    );

    _exec(_stmt, "View 'V_USERPERSON2ROLE'", "view representing connection between person and role depending on JAAS systems",
      "create or replace view V_USERPERSON2ROLE as "
        + "select "
        +       "USERABSTRACT2ABSTRACT.ID,"
        +       "USERABSTRACT2ABSTRACT.USERABSTRACTFROM,"
        +       "USERABSTRACT2ABSTRACT.USERABSTRACTTO,"
        +       "USERABSTRACT2ABSTRACT.USERJAASSYSTEM as JAASSYSID "
        +   "from USERABSTRACT2ABSTRACT "
        +   "where USERABSTRACT2ABSTRACT.TYPEID=" + typeIdPerson2Role
    );

    _exec(_stmt, "View 'V_USERPERSON2GROUP'", "view representing connection between person and group depending on JAAS systems",
      "create or replace view V_USERPERSON2GROUP as "
        + "select "
        +       "USERABSTRACT2ABSTRACT.ID,"
        +       "USERABSTRACT2ABSTRACT.USERABSTRACTFROM,"
        +       "USERABSTRACT2ABSTRACT.USERABSTRACTTO,"
        +       "USERABSTRACT2ABSTRACT.USERJAASSYSTEM as JAASSYSID "
        +   "from USERABSTRACT2ABSTRACT "
        +   "where USERABSTRACT2ABSTRACT.TYPEID=" + typeIdPerson2Group
    );
con.commit();
}
function _eFapsUpdateSQLTables(_context, _version)  {
  print("Update Tables");

  var fileList = eFapsGetAllFiles("org/efaps/js/definitions", true);

  var jexlContext = JexlHelper.createContext();
  jexlContext.getVars().put("version", 
                            Packages.java.lang.Integer.parseInt(_version));

  // sql table
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = SQLTableUpdate.readXMLFile(file);
      if (update != null)  {
        print ('  - ' + fileName);
        update.updateInDB(jexlContext);
      }
    }
  }
}

/**
 * The private functions creates all data model tables
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateDataModelTablesStep1(_context)  {
  print("Create Data Model Tables");
var con = _context.getConnectionResource();
var _con = con.getConnection();
var _stmt = _con.createStatement();

 
  text = "Insert Attribute Types";
  _exec(_stmt, text, "",   "insert into DMATTRIBUTETYPE values ( 99,'Type',           'acfb7dd8-71e9-43c0-9f22-8d98190f7290', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.TypeType',         'org.efaps.admin.datamodel.ui.TypeUI',    null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (100,'String',         '72221a59-df5d-4c56-9bec-c9167de80f2b', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StringType',       'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (110,'Password',       '87a372f0-9e71-45ed-be32-f2a95480a7ee', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.PasswordType',     'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (111,'OID',            'bb1d4c0b-4fee-4607-94b9-7c742949c099', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.OIDType',          'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (200,'Long',           'b9d0e298-f96b-4b78-aa6c-ae8c71952f6c', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.LongType',         'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (210,'Integer',        '41451b64-cb24-4e77-8d9e-5b6eb58df56f', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.IntegerType',      'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (220,'Real',           'd4a96228-1af9-448b-8f0b-7fe2790835af', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.RealType',         'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (290,'Boolean',        '7fb3799d-4e31-45a3-8c5e-4fbf445ec3c1', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.BooleanType',      'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (310,'Date',           '68ce3aa6-e3e8-40bb-b48f-2a67948c2e7e', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StringType',       'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (320,'Time',           'd8ddc848-115e-4abf-be66-0856ac64b21a', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StringType',       'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (330,'DateTime',       'e764db0f-70f2-4cd4-b2fe-d23d3da72f78', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.DateTimeType',     'org.efaps.admin.datamodel.ui.DateTimeUI',null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (331,'Created',        '513d35f5-58e2-4243-acd2-5fec5359778a', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.CreatedType',      'org.efaps.admin.datamodel.ui.DateTimeUI',null, 1   )");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (332,'Modified',       'a8556408-a15d-4f4f-b740-6824f774dc1d', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.ModifiedType',     'org.efaps.admin.datamodel.ui.DateTimeUI',1,    null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (400,'Link',           '440f472f-7be2-41d3-baec-4a2f0e4e5b31', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.LinkType',         'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (401,'LinkWithRanges', '9d6b2e3e-68ce-4509-a5f0-eae42323a696', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.LinkWithRanges',   'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (410,'PersonLink',     '7b8f98de-1967-44e0-b174-027349868a61', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.PersonLinkType',   'org.efaps.admin.datamodel.ui.UserUI',    null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (411,'CreatorLink',    '76122fe9-8fde-4dd4-a229-e48af0fb4083', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.CreatorLinkType',  'org.efaps.admin.datamodel.ui.UserUI',    null, 1   )");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (412,'ModifierLink',   '447a7c87-8395-48c4-b2ed-d4e96d46332c', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.ModifierLinkType', 'org.efaps.admin.datamodel.ui.UserUI',    1,    null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (413,'OwnerLink',      'a5367e5a-78b7-47b4-be7f-abf5423171f0', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.OwnerLinkType',    'org.efaps.admin.datamodel.ui.UserUI',    null, 1   )");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (420,'PolicyLink',     'c9c98b47-d5da-4665-939c-9686c82914ac', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StringType',       'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (421,'StatusLink',     '33b086bf-c993-4ae1-8b83-6d0eea5f41e9', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StatusLinkType',   'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
con.commit();
}

/**
 * The private functions creates all data model tables
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateDataModelTablesStep2(_context)  {
  /////////////////////////////////////////
  // insert 'sql table' 
var con = _context.getConnectionResource();
var _con = con.getConnection();
var _stmt = _con.createStatement();

  text = "Insert Table for 'Admin_DataModel_SQLTable'";
  var sqlTableIdSQLTable = _eFapsCreateInsertSQLTable(_stmt, text, "5ffb40ef-3518-46c8-a78f-da3ffbfea4c0", "Admin_DataModel_SQLTableSQLTable", "DMTABLE", "ID", null, "Admin_AbstractSQLTable");

  text = "Insert Type for 'Admin_DataModel_SQLTable'";
  var typeIdSQLTable = _eFapsCreateInsertType(_stmt, text, "ebf29cc2-cf42-4cd0-9b6e-92d9b644062b", "Admin_DataModel_SQLTable", "Admin_Abstract");
  _eFapsCreateInsertAttr(_stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLTable',         'SQLTABLE',         100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLColumnID',      'SQLCOLUMNID',      100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLColumnType',    'SQLCOLUMNTYPE',    100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdSQLTable, typeIdSQLTable, 'DMTableMain',      'DMTABLEMAIN',      400, "Admin_DataModel_SQLTable");

  /////////////////////////////////////////
  // insert 'type' 

  text = "Insert Table for 'Admin_DataModel_Type'";
  var sqlTableIdType = _eFapsCreateInsertSQLTable(_stmt, text, "8f4df2db-8fda-4f00-9144-9a3e344d0abc", "Admin_DataModel_TypeSQLTable", "DMTYPE", "ID", null, "Admin_AbstractSQLTable");

  text = "Insert Type for 'Admin_DataModel_Type'";
  var typeIdType = _eFapsCreateInsertType(_stmt, text, "8770839d-60fd-4bb4-81fd-3903d4c916ec", "Admin_DataModel_Type", "Admin_Abstract");
  _eFapsCreateInsertAttr(_stmt, sqlTableIdType, typeIdType, 'SQLCacheExpr',     'SQLCACHEEXPR',     100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdType, typeIdType, 'ParentType',       'PARENTDMTYPE',     400, "Admin_DataModel_Type");

  /////////////////////////////////////////
  // insert 'attribute type' 

  text = "Insert Table for 'Admin_DataModel_AttributeType'";
  var sqlTableIdAttrType = _eFapsCreateInsertSQLTable(_stmt, text, "30152cda-e5a3-418d-ad1e-ad44be1307c2", "Admin_DataModel_AttributeTypeSQLTable", "DMATTRIBUTETYPE", "ID", null, null);

  text = "Insert Type for 'Admin_DataModel_AttributeType'";
  var typeIdAttrType = _eFapsCreateInsertType(_stmt, text, "c482e3d3-8387-4406-a1c2-b0e708af78f3", "Admin_DataModel_AttributeType", null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAttrType, typeIdAttrType, 'OID',              'ID',               111, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAttrType, typeIdAttrType, 'ID',               'ID',               210, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAttrType, typeIdAttrType, 'Name',             'NAME',             100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAttrType, typeIdAttrType, 'Classname',        'CLASSNAME',        100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAttrType, typeIdAttrType, 'ClassnameUI',      'CLASSNAMEUI',      100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAttrType, typeIdAttrType, 'AlwaysUpdate',     'ALWAYSUPDATE',     290, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAttrType, typeIdAttrType, 'CreateUpdate',     'CREATEUPDATE',     290, null);

  /////////////////////////////////////////
  // insert 'attribute' 

  text = "Insert Table for 'Admin_DataModel_Attribute'";
  var sqlTableIdAttr = _eFapsCreateInsertSQLTable(_stmt, text, "d3a64746-3666-4678-9603-f304bf16bb92", "Admin_DataModel_AttributeSQLTable", "DMATTRIBUTE", "ID", null, "Admin_AbstractSQLTable");

  text = "Insert Type for 'Admin_DataModel_Attribute'";
  var typeIdAttr = _eFapsCreateInsertType(_stmt, text, "518a9802-cf0e-4359-9b3c-880f71e1387f", "Admin_DataModel_Attribute", "Admin_Abstract");
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAttr, typeIdAttr, 'Table',             'DMTABLE',         400, "Admin_DataModel_SQLTable");
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAttr, typeIdAttr, 'ParentType',        'DMTYPE',          400, "Admin_DataModel_Type");
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAttr, typeIdAttr, 'AttributeType',     'DMATTRIBUTETYPE', 400, "Admin_DataModel_AttributeType");
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAttr, typeIdAttr, 'TypeLink',          'DMTYPELINK',      400, "Admin_DataModel_Type");
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAttr, typeIdAttr, 'SQLColumn',         'SQLCOLUMN',       100, null);

  /////////////////////////////////////////
  // insert 'admin property' 

  text = "Insert Table for 'Admin_Property'";
  var sqlTableIdProp = _eFapsCreateInsertSQLTable(_stmt, text, "5cf99cd6-06d6-4322-a344-55d206666c9c", "Admin_PropertyTable", "PROPERTY", "ID", null, null);

  text = "Insert Type for 'Admin_Property'";
  var typeIdProp = _eFapsCreateInsertType(_stmt, text, "f3d54a86-c323-43d8-9c78-284d61d955b3", "Admin_Property", null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdProp, typeIdProp, 'OID',              'ID',               111, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdProp, typeIdProp, 'ID',               'ID',               210, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdProp, typeIdProp, 'Name',             'NAME',             100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdProp, typeIdProp, 'Value',            'VALUE',            100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdProp, typeIdProp, 'Abstract',         'ABSTRACT',         400, "Admin_Abstract");
  _eFapsCreateInsertProp(_stmt, typeIdProp, "Tree", "Admin_PropertyTree");
  _eFapsCreateInsertProp(_stmt, typeIdProp, "Icon", "${ROOTURL}/servlet/image/Admin_PropertyImage");
con.commit();
}

/**
 * The private functions creates all common tables
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateCommonTablesStep2(_context)  {
  print("Create Common Tables Step 2");
var con = _context.getConnectionResource();
var _con = con.getConnection();
var _stmt = _con.createStatement();

  text = "Insert Table for 'Admin_Abstract'";
  var sqlTableIdAbstract = _eFapsCreateInsertSQLTable(_stmt, text, "e76ff99d-0d3d-4154-b2ef-d65633d357c3", "Admin_AbstractSQLTable", "ABSTRACT", "ID", "TYPEID", null);

  text = "Insert Type for 'Admin_Abstract'";
  var typeIdAbstract = _eFapsCreateInsertType(_stmt, text, "2a869f46-0ec7-4afb-98e7-8b1125e1c43c", "Admin_Abstract", null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAbstract, typeIdAbstract, 'Type',             'TYPEID',            99, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAbstract, typeIdAbstract, 'OID',              'TYPEID,ID',        111, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAbstract, typeIdAbstract, 'ID',               'ID',               210, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAbstract, typeIdAbstract, 'Creator',          'CREATOR',          411, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAbstract, typeIdAbstract, 'Created',          'CREATED',          331, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAbstract, typeIdAbstract, 'Modifier',         'MODIFIER',         412, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAbstract, typeIdAbstract, 'Modified',         'MODIFIED',         332, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAbstract, typeIdAbstract, 'Name',             'NAME',             100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAbstract, typeIdAbstract, 'UUID',             'UUID',             100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdAbstract, typeIdAbstract, 'Revision',         'REVISION',         100, null);
con.commit();
}

function createAll()  {

  Shell.transactionManager.begin();

  var context = Context.newThreadContext(Shell.transactionManager.getTransaction());
  Shell.setContext(context);

  try  {
//    var con = context.getConnection();
//    var stmt = con.createStatement();
//var con = context.getConnectionResource();
//var stmt = con.getConnection().createStatement();
    
//    if (eFapsCommonVersionGet(con.getConnection(),stmt) < 1)  {

      _eFapsUpdateSQLTables(context, "1");
      _eFapsUpdateSQLTables(context, "2");

      _eFapsCreateUserTablesStep1     (context);
      _eFapsCreateDataModelTablesStep1(context);
      _eFapsCreateCommonTablesStep2   (context);
      _eFapsCreateDataModelTablesStep2(context);
      _eFapsCreateUserTablesStep2     (context);
      _eFapsUpdateSQLTables(context, "3");
      _eFapsCreateEventTablesStep3    (context);
//      eFapsCommonVersionInsert(con.getConnection(), stmt, "eFaps", 1);
//    }

//    if (eFapsCommonVersionGet(con,stmt) < 2)  {
//    }
    Shell.transactionManager.commit();

//    stmt.close();
//con.commit();
  } catch (e)  {
    print(e);
    Shell.transactionManager.abort();
    throw e;
  } finally  {
    context.close();
  }
}