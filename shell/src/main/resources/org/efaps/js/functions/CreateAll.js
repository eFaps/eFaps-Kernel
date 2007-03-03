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
 * Import all predefined user interface configuration items
 * found in the jar file under the path <code>org/efaps/js/definitions</code>.
 *
 * @see #eFapsCreateAll
 */
function _eFapsCreateAllImportUserInterface()  {
  var fileList = eFapsGetAllFiles("org/efaps/js/definitions", true);

  importImages(fileList);
  importForms(fileList);
  importTables(fileList);

  createMenus(fileList);
  createSearches(fileList);

  importCommands(fileList);
  importMenus(fileList);
  importSearches(fileList);
}

/**
 * Import all XML files found in the sub directories.
 */
function _eFapsCreateAllImportXMLFiles(_version)  {
  var fileList = eFapsGetAllFiles("org/efaps/js/definitions", true);
  
  var jexlContext = JexlHelper.createContext();
  jexlContext.getVars().put("version", 
                            Packages.java.lang.Integer.parseInt(_version));

  // rolle
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = RoleUpdate.readXMLFile(file);
      if (update != null)  {
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
  _eFapsCreateUserTablesStep4(context);
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
  Shell.setContext(context);
  _eFapsCreateAllImportUserInterface();
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

function _eFapsCreateInsertType(_stmt, _text, _name, _parentType)  {
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
          "(TYPEID,NAME,REVISION,CREATOR,CREATED,MODIFIER,MODIFIED) "
          +"values  (" + typeIdType + ", '" + _name + "', '', 1," + CURRENT_TIMESTAMP + ",1," + CURRENT_TIMESTAMP + ")", "ABSTRACT");
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
/*
  eFapsCommonSQLTableCreate(_con, _stmt, "Definition of Events (e.g. Triggers)", "EVENTDEF", "ABSTRACT",[
      ["ABSTRACT              "+TYPE_INTEGER+"                   not null"],
      ["INDEXPOS              "+TYPE_INTEGER+"                   not null"],
      ["constraint EVENTDEF_UK_ID_INDEXPOS unique(ID,INDEXPOS)"]
  ]);
  eFapsCommonSQLTableCreate(_con, _stmt, "History of Events for Objects", "HISTORY", null,[
      ["EVENTTYPEID     "+TYPE_INTEGER+" not null"],
      ["FORTYPEID       "+TYPE_INTEGER+" not null"],
      ["FORID           "+TYPE_INTEGER+" not null"],
      ["MODIFIER        "+TYPE_INTEGER+" not null"],
      ["MODIFIED        "+TYPE_DATETIME+" not null"],
      ["ATTRID          "+TYPE_INTEGER],
      ["ATTRVALUE       "+TYPE_STRING_LONG+"(4000)"],
      ["constraint HIST_FK_EVNTYPEID foreign key(EVENTTYPEID)  references DMTYPE(ID)"],
      ["constraint HIST_FK_FORTYPEID foreign key(FORTYPEID)    references DMTYPE(ID)"],
      ["constraint HIST_FK_MODIFIER  foreign key(MODIFIER)     references USERABSTRACT(ID)"]
  ]);
*/
  // must be created for reload-cache-functionality and possibility to define types
  text = "Insert Table for 'Admin_Event_Definition'";
  var sqlTableEventDef = _eFapsCreateInsertSQLTable(_stmt, text, "1238f647-9cf5-4d9f-883e-c6d24db538f5", "Admin_Event_DefinitionSQLTable", "EVENTDEF", "ID", null, "Admin_AbstractSQLTable");

  text = "Insert Type for 'Admin_Event_Definition'";
  var typeIdEventDef = _eFapsCreateInsertType(_stmt, text, "Admin_Event_Definition", "Admin_Abstract");
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

/*    text = "Insert Person Policy";
    var newId = _insert(_stmt, text, "",   "insert into LCPOLICY(NAME,CREATOR,CREATED,MODIFIER,MODIFIED) values ('Admin_User_Person',1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")","LCPOLICY");
    _exec(_stmt, null, null, "insert into LCSTATUS(NAME,CREATOR,CREATED,MODIFIER,MODIFIED,LCPOLICY) values ('Inactive',1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+","+newId+")");
    _exec(_stmt, null, null, "insert into LCSTATUS(NAME,CREATOR,CREATED,MODIFIER,MODIFIED,LCPOLICY) values ('Active',1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+","+newId+")");
*/
//alter table USERABSTRACT add constraint USRABSTR_FK_STS     foreign key(STATUS)     references LCSTATUS(ID);

    text = "Insert Type for 'Admin_User_Person' (only to store ID for type)";
    var typeIdPerson        = _eFapsCreateInsertType(_stmt, text, "Admin_User_Person", null);
/*    
    _exec(_stmt, null, null, "insert into DMTYPE2POLICY       values (1,1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",10000,10000)");
*/

    text = "Insert Type for 'Admin_User_Role' (only to store ID for type)";
    var typeIdRole          = _eFapsCreateInsertType(_stmt, text, "Admin_User_Role", null);

    text = "Insert Type for 'Admin_User_Group' (only to store ID for type)";
    var typeIdGroup         = _eFapsCreateInsertType(_stmt, text, "Admin_User_Group", null);
   
    text = "Insert Type for 'Admin_User_Person2Role' (only to store ID for type)";
    var typeIdPerson2Role   = _eFapsCreateInsertType(_stmt, text, "Admin_User_Person2Role", null);

    text = "Insert Type for 'Admin_User_Person2Group' (only to store ID for type)";
    var typeIdPerson2Group  = _eFapsCreateInsertType(_stmt, text, "Admin_User_Person2Group", null);

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

    _exec(_stmt, "View 'V_USERJAASSYSTEM'", "view representing all JAAS system",
      "create or replace view V_USERJAASSYSTEM as "
        + "select "
        +     "ID,"
        +     "NAME,"
        +     "CLASSNAMEPERSON,"
        +     "CLASSNAMEROLE,"
        +     "CLASSNAMEGROUP,"
        +     "METHODPERSONKEY,"
        +     "METHODPERSONNAME,"
        +     "METHODPERSONFIRSTNAME,"
        +     "METHODPERSONLASTNAME,"
        +     "METHODPERSONEMAIL,"
        +     "METHODPERSONORG,"
        +     "METHODPERSONURL,"
        +     "METHODPERSONPHONE,"
        +     "METHODPERSONMOBILE,"
        +     "METHODPERSONFAX,"
        +     "METHODROLEKEY,"
        +     "METHODGROUPKEY "
        +   "from USERJAASSYSTEM"
    );

    _exec(_stmt, "View 'V_USERPERSON'", "view representing all persons",
      "create or replace view V_USERPERSON as "
        + "select "
        +       "USERABSTRACT.ID,"
        +       "USERABSTRACT.NAME,"
        +       "USERPERSON.FIRSTNAME,"
        +       "USERPERSON.LASTNAME,"
        +       "USERPERSON.EMAIL,"
        +       "USERPERSON.ORG,"
        +       "USERPERSON.URL,"
        +       "USERPERSON.PHONE,"
        +       "USERPERSON.MOBILE,"
        +       "USERPERSON.FAX,"
        +       "USERPERSON.PASSWORD "
        +   "from USERABSTRACT,USERPERSON "
        +   "where USERABSTRACT.ID=USERPERSON.ID"
    );

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

function _eFapsCreateUserTablesStep4(_context)  {
  print("Create User Tables");

  var fileList = eFapsGetAllFiles("org/efaps/js/definitions/Admin/User/DataModels", true);

  var jexlContext = JexlHelper.createContext();
  jexlContext.getVars().put("version", 
                            Packages.java.lang.Integer.parseInt("4"));

  // sql table
  for (i in fileList)  {
    var file = new Packages.java.io.File(fileList[i]);
    var fileName = new Packages.java.lang.String(file.getName());
    if (fileName.endsWith(".xml"))  {
      var update = SQLTableUpdate.readXMLFile(file);
      if (update != null)  {
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

/*  eFapsCommonSQLTableCreate(_con, _stmt, "sql table names for the data model", "DMTABLE", "ABSTRACT",[
      ["SQLTABLE              "+TYPE_STRING_SHORT+"(35)          not null"],
      ["SQLCOLUMNID           "+TYPE_STRING_SHORT+"(35)          not null"],
      ["SQLCOLUMNTYPE         "+TYPE_STRING_SHORT+"(35)"],
      ["DMTABLEMAIN           "+TYPE_INTEGER],
      ["constraint DMTABLE_UK_SQLTBLE  unique(SQLTABLE)"],
      ["constraint DMTABLE_FK_DMTBLMN  foreign key(DMTABLEMAIN)  references DMTABLE(ID)"]
  ]);

  eFapsCommonSQLTableCreate(_con, _stmt, "type definition", "DMTYPE", "ABSTRACT",[
      ["PARENTDMTYPE          "+TYPE_INTEGER],
      ["SQLCACHEEXPR          "+TYPE_STRING_SHORT+"(50)"],
      ["constraint DMTYPE_FK_PRNTDMTP  foreign key(PARENTDMTYPE) references DMTYPE(ID)"]
  ]);
*/
/*
  eFapsCommonSQLTableCreate(_con, _stmt, "connection of policies for type definitions", "DMTYPE2POLICY", null,[
      ["CREATOR               "+TYPE_INTEGER+"                   not null"],
      ["CREATED               "+TYPE_DATETIME+"                  not null"],
      ["MODIFIER              "+TYPE_INTEGER+"                   not null"],
      ["MODIFIED              "+TYPE_DATETIME+"                  not null"],
      ["DMTYPE                "+TYPE_INTEGER+"                   not null"],
      ["LCPOLICY              "+TYPE_INTEGER+"                   not null"],
      ["constraint DMTPE2PLCY_FK_CRTR  foreign key(CREATOR)      references USERPERSON(ID)"],
      ["constraint DMTPE2PLCY_FK_MDFR  foreign key(MODIFIER)     references USERPERSON(ID)"],
      ["constraint DMTPE2PLCY_FK_DMTP  foreign key(DMTYPE)       references DMTYPE(ID)"],
      ["constraint DMTPE2PLCY_FK_PLCY  foreign key(LCPOLICY)     references LCPOLICY(ID)"]
  ]);
*/
/*
  _exec(_stmt, "Table 'DMATTRIBUTETYPE'", "attribute types",
    "create table DMATTRIBUTETYPE ("+
      "ID                    "+TYPE_INTEGER+"                   not null,"+
      "NAME                  "+TYPE_STRING_SHORT+"(128)         not null,"+
      "CREATOR               "+TYPE_INTEGER+"                   not null,"+
      "CREATED               "+TYPE_DATETIME+"                  not null,"+
      "MODIFIER              "+TYPE_INTEGER+"                   not null,"+
      "MODIFIED              "+TYPE_DATETIME+"                  not null,"+
      "CLASSNAME             "+TYPE_STRING_SHORT+"(128)         not null,"+
      "CLASSNAMEUI           "+TYPE_STRING_SHORT+"(128)         not null,"+
      "ALWAYSUPDATE          "+TYPE_INTEGER+","+
      "CREATEUPDATE          "+TYPE_INTEGER+","+
      "constraint DMATTRTP_PK_ID      primary key(ID),"+
      "constraint DMATTRTP_FK_CRTR    foreign key(CREATOR)      references USERPERSON(ID),"+
      "constraint DMATTRTP_FK_MDFR    foreign key(MODIFIER)     references USERPERSON(ID)"+
    ")"
  );

  eFapsCommonSQLTableCreate(_con, _stmt, "attributes of types", "DMATTRIBUTE", "ABSTRACT",[
      ["DMTABLE               "+TYPE_INTEGER+"                       not null"],
      ["DMTYPE                "+TYPE_INTEGER+"                       not null"],
      ["DMATTRIBUTETYPE       "+TYPE_INTEGER+"                       not null"],
      ["DMTYPELINK            "+TYPE_INTEGER],
      ["SQLCOLUMN             "+TYPE_STRING_SHORT+"(50)              not null"],
      ["constraint DMATTR_FK_DMTABLE   foreign key(DMTABLE)          references DMTABLE(ID)"],
      ["constraint DMATTR_FK_DMTYPE    foreign key(DMTYPE)           references DMTYPE(ID)"],
      ["constraint DMATTR_FK_DMATTRTP  foreign key(DMATTRIBUTETYPE)  references DMATTRIBUTETYPE(ID)"],
      ["constraint DMATTR_FK_DMTPLINK  foreign key(DMTYPELINK)       references DMTYPE(ID)"]
  ]);
*/
  //        "constraint DMATTR_UK_DMTP_NM   unique(DMTYPE,NAME),"+

  _exec(_stmt, "View 'V_ADMINTYPE'", "view representing all types",
    "create view V_ADMINTYPE as "+
      "select "+
          "ABSTRACT.ID,"+
          "ABSTRACT.NAME,"+
          "DMTYPE.PARENTDMTYPE,"+
          "DMTYPE.SQLCACHEEXPR "+
        "from DMTYPE,ABSTRACT "+
        "where ABSTRACT.ID=DMTYPE.ID"
  );

  _exec(_stmt, "View 'V_ADMINATTRIBUTE'", "view representing all attributes",
    "create view V_ADMINATTRIBUTE as "+
      "select "+
          "ABSTRACT.ID,"+
          "ABSTRACT.NAME,"+
          "DMATTRIBUTE.DMTABLE,"+
          "DMATTRIBUTE.DMTYPE,"+
          "DMATTRIBUTE.DMATTRIBUTETYPE,"+
          "DMATTRIBUTE.DMTYPELINK,"+
          "DMATTRIBUTE.SQLCOLUMN "+
        "from DMATTRIBUTE,ABSTRACT "+
        "where ABSTRACT.ID=DMATTRIBUTE.ID"
  );

  _exec(_stmt, "View 'V_ADMINSQLTABLE'", "view representing all sql tables",
    "create view V_ADMINSQLTABLE as "+
      "select "+
            "ABSTRACT.ID,"+
            "ABSTRACT.NAME,"+
            "DMTABLE.SQLTABLE,"+
            "DMTABLE.SQLCOLUMNID,"+
            "DMTABLE.SQLCOLUMNTYPE,"+
            "DMTABLE.DMTABLEMAIN "+
        "from DMTABLE,ABSTRACT "+
        "where ABSTRACT.ID=DMTABLE.ID"
  );

  text = "Insert Attribute Types";
  _exec(_stmt, text, "",   "insert into DMATTRIBUTETYPE values ( 99,'Type',           null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.TypeType',         'org.efaps.admin.datamodel.ui.TypeUI',    null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (100,'String',         null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StringType',       'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (110,'Password',       null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.PasswordType',     'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (111,'OID',            null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.OIDType',          'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (200,'Long',           null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.LongType',         'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (210,'Integer',        null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.IntegerType',      'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (220,'Real',           null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.RealType',         'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (290,'Boolean',        null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.BooleanType',      'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (310,'Date',           null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StringType',       'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (320,'Time',           null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StringType',       'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (330,'DateTime',       null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.DateTimeType',     'org.efaps.admin.datamodel.ui.DateTimeUI',null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (331,'Created',        null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.CreatedType',      'org.efaps.admin.datamodel.ui.DateTimeUI',null, 1   )");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (332,'Modified',       null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.ModifiedType',     'org.efaps.admin.datamodel.ui.DateTimeUI',1,    null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (400,'Link',           null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.LinkType',         'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (401,'LinkWithRanges', null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.LinkWithRanges',   'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (410,'PersonLink',     null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.PersonLinkType',   'org.efaps.admin.datamodel.ui.UserUI',    null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (411,'CreatorLink',    null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.CreatorLinkType',  'org.efaps.admin.datamodel.ui.UserUI',    null, 1   )");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (412,'ModifierLink',   null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.ModifierLinkType', 'org.efaps.admin.datamodel.ui.UserUI',    1,    null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (413,'OwnerLink',      null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.OwnerLinkType',    'org.efaps.admin.datamodel.ui.UserUI',    null, 1   )");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (420,'PolicyLink',     null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StringType',       'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (421,'StatusLink',     null, 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StatusLinkType',   'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
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
  var typeIdSQLTable = _eFapsCreateInsertType(_stmt, text, "Admin_DataModel_SQLTable", "Admin_Abstract");
  _eFapsCreateInsertAttr(_stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLTable',         'SQLTABLE',         100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLColumnID',      'SQLCOLUMNID',      100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLColumnType',    'SQLCOLUMNTYPE',    100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdSQLTable, typeIdSQLTable, 'DMTableMain',      'DMTABLEMAIN',      400, "Admin_DataModel_SQLTable");

  /////////////////////////////////////////
  // insert 'type' 

  text = "Insert Table for 'Admin_DataModel_Type'";
  var sqlTableIdType = _eFapsCreateInsertSQLTable(_stmt, text, "8f4df2db-8fda-4f00-9144-9a3e344d0abc", "Admin_DataModel_TypeSQLTable", "DMTYPE", "ID", null, "Admin_AbstractSQLTable");

  text = "Insert Type for 'Admin_DataModel_Type'";
  var typeIdType = _eFapsCreateInsertType(_stmt, text, "Admin_DataModel_Type", "Admin_Abstract");
  _eFapsCreateInsertAttr(_stmt, sqlTableIdType, typeIdType, 'SQLCacheExpr',     'SQLCACHEEXPR',     100, null);
  _eFapsCreateInsertAttr(_stmt, sqlTableIdType, typeIdType, 'ParentType',       'PARENTDMTYPE',     400, "Admin_DataModel_Type");

  /////////////////////////////////////////
  // insert 'attribute type' 

  text = "Insert Table for 'Admin_DataModel_AttributeType'";
  var sqlTableIdAttrType = _eFapsCreateInsertSQLTable(_stmt, text, "30152cda-e5a3-418d-ad1e-ad44be1307c2", "Admin_DataModel_AttributeTypeSQLTable", "DMATTRIBUTETYPE", "ID", null, null);

  text = "Insert Type for 'Admin_DataModel_AttributeType'";
  var typeIdAttrType = _eFapsCreateInsertType(_stmt, text, "Admin_DataModel_AttributeType", null);
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
  var typeIdAttr = _eFapsCreateInsertType(_stmt, text, "Admin_DataModel_Attribute", "Admin_Abstract");
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
  var typeIdProp = _eFapsCreateInsertType(_stmt, text, "Admin_Property", null);
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
  var typeIdAbstract = _eFapsCreateInsertType(_stmt, text, "Admin_Abstract", null);
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

/**
 * The private functions creates all common tables
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateCommonTablesStep3(_context)  {
  print("Create Common Tables Step 3 (Activate foreign key for type id in table abstract)");
var con = _context.getConnectionResource();
var _con = con.getConnection();
var _stmt = _con.createStatement();

  var rs = _stmt.executeQuery("select ID from ABSTRACT where NAME='Admin_DataModel_SQLTable'");
  rs.next();
  var typeIdSQLTable = rs.getString(1);
  rs.close();
  _exec(_stmt, "Table 'ABSTRACT'", "update type id for sql tables",
    "update ABSTRACT set TYPEID=" + typeIdSQLTable + " where TYPEID=-20000"
  );

  rs = _stmt.executeQuery("select ID from ABSTRACT where NAME='Admin_DataModel_Type'");
  rs.next();
  var typeIdType = rs.getString(1);
  rs.close();
  _exec(_stmt, "Table 'ABSTRACT'", "update type id for types",
    "update ABSTRACT set TYPEID=" + typeIdType + " where TYPEID=-21000"
  );

  rs = _stmt.executeQuery("select ID from ABSTRACT where NAME='Admin_DataModel_Attribute'");
  rs.next();
  var typeIdAttr = rs.getString(1);
  rs.close();
  _exec(_stmt, "Table 'ABSTRACT'", "update type id for attributes",
    "update ABSTRACT set TYPEID=" + typeIdAttr + " where TYPEID=-22000"
  );
/*
  eFapsCommonSQLTableUpdate(_con, _stmt, "Foreign Contraint for column TYPEID", "ABSTRACT", [
      ["constraint ABSTR_FK_TYPEID foreign key(TYPEID) references DMTYPE(ID)"]
  ]);
*/
/*
  eFapsCommonSQLTableCreate(_con, _stmt, "Common Version Table", "COMMONVERSION", null,[
      ["NAME                  "+TYPE_STRING_SHORT+"(128)         not null"],
      ["REVISION              "+TYPE_INTEGER+"                   not null"],
      ["CREATOR               "+TYPE_INTEGER+"                   not null"],
      ["CREATED               "+TYPE_DATETIME+"                  not null"],
      ["MODIFIER              "+TYPE_INTEGER+"                   not null"],
      ["MODIFIED              "+TYPE_DATETIME+"                  not null"],
      ["constraint COMVER_UK_NAMEREV   unique(NAME,REVISION)"],
      ["constraint COMVER_FK_CRTR      foreign key(CREATOR)      references USERPERSON(ID)"],
      ["constraint COMVER_FK_MDFR      foreign key(MODIFIER)     references USERPERSON(ID)"]
  ]);
*/
  _exec(_stmt, "View 'V_COMMONVERSION'", "view representing all versions",
    "create view V_COMMONVERSION as "
      + "select "
      +         "NAME,"
      +         "max(REVISION) as VERSION "
      +     "from COMMONVERSION "
      +     "group by NAME"
  );
con.commit();
}

/**
 * The private functions creates all lifecycle tables
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateLifeCycleTablesStep1(_context)  {
  print("Create LifeCycle Tables");
var con = _context.getConnectionResource();
var _con = con.getConnection();
var _stmt = _con.createStatement();

  eFapsCommonSQLTableCreate(_con, _stmt, "Policy object", "LCPOLICY", null,[
      ["NAME                  "+TYPE_STRING_SHORT+"(128)         not null"],
      ["CREATOR               "+TYPE_INTEGER+"                   not null"],
      ["CREATED               "+TYPE_DATETIME+"                  not null"],
      ["MODIFIER              "+TYPE_INTEGER+"                   not null"],
      ["MODIFIED              "+TYPE_DATETIME+"                  not null"],
      ["constraint LCPOLICY_UK_NAME    unique(NAME)"],
      ["constraint LCPOLICY_FK_CRTR    foreign key(CREATOR)      references USERPERSON(ID)"],
      ["constraint LCPOLICY_FK_MDFR    foreign key(MODIFIER)     references USERPERSON(ID)"]
  ]);

  eFapsCommonSQLTableCreate(_con, _stmt, "Status object", "LCSTATUS", null,[
      ["NAME                  "+TYPE_STRING_SHORT+"(128)         not null"],
      ["CREATOR               "+TYPE_INTEGER+"                   not null"],
      ["CREATED               "+TYPE_DATETIME+"                  not null"],
      ["MODIFIER              "+TYPE_INTEGER+"                   not null"],
      ["MODIFIED              "+TYPE_DATETIME+"                  not null"],
      ["LCPOLICY              "+TYPE_INTEGER+"                   not null"],
      ["constraint LCSTS_UK_NM_PLCY    unique(NAME,LCPOLICY)"],
      ["constraint LCSTS_FK_CREATOR    foreign key(CREATOR)      references USERPERSON(ID)"],
      ["constraint LCSTS_FK_MODIFIER   foreign key(MODIFIER)     references USERPERSON(ID)"],
      ["constraint LCSTS_FK_LCPOLICY   foreign key(LCPOLICY)     references LCPOLICY(ID)"]
  ]);

  eFapsCommonSQLTableCreate(_con, _stmt, "Access Itself", "LCSTATUSACCESS", null,[
      ["NAME                  "+TYPE_STRING_SHORT+"(128)         not null"],
      ["CREATOR               "+TYPE_INTEGER+"                   not null"],
      ["CREATED               "+TYPE_DATETIME+"                  not null"],
      ["MODIFIER              "+TYPE_INTEGER+"                   not null"],
      ["MODIFIED              "+TYPE_DATETIME+"                  not null"],
      ["LCSTATUS              "+TYPE_INTEGER+"                   not null"],
      ["LCACCESSTYPE          "+TYPE_INTEGER+"                   not null"],
      ["USERABSTRACT          "+TYPE_INTEGER+"                   not null"],
      ["constraint LCSTSACS_UNIQUE     unique(LCSTATUS,LCACCESSTYPE,USERABSTRACT)"],
      ["constraint LCSTSACS_FK_CRTR    foreign key(CREATOR)      references USERPERSON(ID)"],
      ["constraint LCSTSACS_FK_MDFR    foreign key(MODIFIER)     references USERPERSON(ID)"],
      ["constraint LCSTSACS_FK_STS     foreign key(LCSTATUS)     references LCSTATUS(ID)"],
      ["constraint LCSTSACS_FK_ACSTP   foreign key(LCACCESSTYPE) references ACCESSTYPE(ID)"],
      ["constraint LCSTSACS_FK_USR     foreign key(USERABSTRACT) references USERABSTRACT(ID)"]
  ]);
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
//      _eFapsCreateLifeCycleTablesStep1(context);
      _eFapsCreateCommonTablesStep2   (context);
      _eFapsCreateDataModelTablesStep2(context);
      _eFapsCreateUserTablesStep2     (context);
      _eFapsCreateCommonTablesStep3   (context);
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