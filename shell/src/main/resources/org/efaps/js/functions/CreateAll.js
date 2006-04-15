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

importClass(Packages.org.efaps.db.Context);
importClass(Packages.org.efaps.admin.user.Person);
importClass(Packages.org.efaps.db.databases.AbstractDatabase);

var TYPE_INTEGER      = Context.getDbType().getColumnType(AbstractDatabase.ColumnType.INTEGER);
var TYPE_STRING_SHORT = Context.getDbType().getColumnType(AbstractDatabase.ColumnType.STRING_SHORT);
var TYPE_STRING_LONG  = Context.getDbType().getColumnType(AbstractDatabase.ColumnType.STRING_LONG);
var TYPE_DATETIME     = Context.getDbType().getColumnType(AbstractDatabase.ColumnType.DATETIME);
var TYPE_BLOB         = Context.getDbType().getColumnType(AbstractDatabase.ColumnType.BLOB);
var CURRENT_TIMESTAMP = Context.getDbType().getCurrentTimeStamp();

function _exec(_stmt, _subject, _txt, _cmd)  {
  if (_txt!=null && _subject!=null)  {
    print("  - " + _subject + "  (" + _txt + ")");
  }
//print(_cmd);
  var bck = _stmt.execute(_cmd);
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

    var p = Person.get("Administrator");
    var c = new Context(Shell.transactionManager.getTransaction(), p, null);
    Context.setThreadContext(c);

    p.setPassword(c, "Administrator");
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
try  {
  Shell.transactionManager.begin();
  var context = new Context(Shell.transactionManager.getTransaction(), null, null);
  Context.setThreadContext(context);
  Packages.org.efaps.db.Cache.reloadCache(context);
  Shell.transactionManager.rollback();
  context.close();

} catch (e)  {
  print(e);
}

  Shell.transactionManager.begin();
  var context = new Context(Shell.transactionManager.getTransaction(), Packages.org.efaps.admin.user.Person.get("Administrator"), null);
  Context.setThreadContext(context);
  Shell.setContext(context);
  _eFapsCreateAllImportDataModel();
  Shell.transactionManager.commit();
  context.close();

print("############ Reload Cache");
try  {
  Shell.transactionManager.begin();
  var context = new Context(Shell.transactionManager.getTransaction(), null, null);
  Context.setThreadContext(context);
  Packages.org.efaps.db.Cache.reloadCache(context);
  Shell.transactionManager.rollback();
  context.close();

} catch (e)  {
  print(e);
}

  Shell.transactionManager.begin();
  var context = new Context(Shell.transactionManager.getTransaction(), Packages.org.efaps.admin.user.Person.get("Administrator"), null);
  Context.setThreadContext(context);
  Shell.setContext(context);
  _eFapsCreateAllImportUserInterface();
  Shell.transactionManager.commit();
  context.close();

print("############ Reload Cache");
try  {
  Shell.transactionManager.begin();
  var context = new Context(Shell.transactionManager.getTransaction(), null, null);
  Context.setThreadContext(context);
  Packages.org.efaps.db.Cache.reloadCache(context);
  Shell.transactionManager.rollback();
  context.close();

} catch (e)  {
  print(e);
}

  _eFapsCreateAllUpdatePassword();
}



function _eFapsCreateInsertSQLTable(_stmt, _text, _id, _name, _sqlTable, _sqlColId, _sqlColType, _sqlNewIdSelect, _tableMainId)  {
  var sqlColType = (_sqlColType==null ? "null" : "'"+_sqlColType+"'");
  var sqlNewIdSelect = (_sqlNewIdSelect==null ? "null" : "'"+_sqlNewIdSelect+"'");
  var tableMainId = (_tableMainId==null ? "null" : _tableMainId);

  _exec(_stmt, _text, "",   "insert into ABSTRACT values  (20000, "+_id+", '"+_name+"', '', 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");
  _exec(_stmt, null, null, "insert into DMTABLE  values  ("+_id+",'"+_sqlTable+"','"+_sqlColId+"',"+sqlColType+","+sqlNewIdSelect+", "+tableMainId+")");
}

function _eFapsCreateInsertType(_stmt, _text, _id, _name, _parentTypeId)  {
  var parentTypeId = (_parentTypeId==null ? "null" : _parentTypeId);

  _exec(_stmt, _text, "",   "insert into ABSTRACT values  (21000,"+_id+", '"+_name+"', '', 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");
  _exec(_stmt, null, null, "insert into DMTYPE   values  ("+_id+", "+parentTypeId+", null)");
}

function _eFapsCreateInsertAttr(_stmt, _tableId, _typeId, _id, _name, _sqlColumn, _attrTypeId, _typeLink)  {
  var typeLink = (_typeLink==null ? "null" : _typeLink);

  _exec(_stmt, null, null, "insert into ABSTRACT            values  (23000, "+_id+", '"+_name+"', '', 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");
  _exec(_stmt, null, null, "insert into DMATTRIBUTE         values  ("+_id+", "+_tableId+", "+_typeId+",  "+_attrTypeId+", "+typeLink+", '"+_sqlColumn+"')");
}

/**
 * The private function inserts all attribute types.
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateAllInsertAttributeTypes(_stmt)  {
  text = "Insert Attribute Types";
  _exec(_stmt, text, "",   "insert into DMATTRIBUTETYPE values ( 99,'Type',           1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.TypeType',         'org.efaps.admin.datamodel.ui.TypeUI',    null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (100,'String',         1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StringType',       'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (110,'Password',       1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.PasswordType',     'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (111,'OID',            1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.OIDType',          'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (200,'Long',           1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.LongType',         'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (210,'Integer',        1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.IntegerType',      'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (220,'Real',           1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.RealType',         'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (290,'Boolean',        1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.BooleanType',      'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (310,'Date',           1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StringType',       'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (320,'Time',           1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StringType',       'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (330,'DateTime',       1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.DateTimeType',     'org.efaps.admin.datamodel.ui.DateTimeUI',null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (331,'Created',        1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.CreatedType',      'org.efaps.admin.datamodel.ui.DateTimeUI',null, 1   )");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (332,'Modified',       1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.ModifiedType',     'org.efaps.admin.datamodel.ui.DateTimeUI',1,    null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (400,'Link',           1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.LinkType',         'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (401,'LinkWithRanges', 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.LinkWithRanges',   'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (410,'PersonLink',     1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.PersonLinkType',   'org.efaps.admin.datamodel.ui.UserUI',    null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (411,'CreatorLink',    1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.CreatorLinkType',  'org.efaps.admin.datamodel.ui.UserUI',    null, 1   )");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (412,'ModifierLink',   1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.ModifierLinkType', 'org.efaps.admin.datamodel.ui.UserUI',    1,    null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (413,'OwnerLink',      1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.OwnerLinkType',    'org.efaps.admin.datamodel.ui.UserUI',    null, 1   )");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (420,'PolicyLink',     1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StringType',       'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (421,'StatusLink',     1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.StatusLinkType',   'org.efaps.admin.datamodel.ui.StringUI',  null, null)");
//  _exec(_stmt, null, null, "insert into DMATTRIBUTETYPE values (501,'Blob',           1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",'org.efaps.admin.datamodel.attributetype.BlobType',         'org.efaps.admin.datamodel.ui.FileUI',    null, null)");
}

/**
 * The private function creates all user tables.
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateAllUserTables(_stmt)  {
  print("Create User Tables");

  _exec(_stmt, "Table 'USERABSTRACT'", "Abstract User",
    "create table USERABSTRACT ("+
      "TYPEID                "+TYPE_INTEGER+"                   not null,"+
      "ID                    "+TYPE_INTEGER+"                   not null,"+
      "NAME                  "+TYPE_STRING_SHORT+"(128)         not null,"+
      "CREATOR               "+TYPE_INTEGER+"                   not null,"+
      "CREATED               "+TYPE_DATETIME+"                  not null,"+
      "MODIFIER              "+TYPE_INTEGER+"                   not null,"+
      "MODIFIED              "+TYPE_DATETIME+"                  not null,"+
  "STATUS  int  default 10001 not null,"+
      "constraint USERABSTR_PK_ID     primary key(ID),"+
      "constraint USERABSTR_UK_NAME   unique(NAME)"+
    ")"
  );

  _exec(_stmt, "Table 'USERPERSON'", "Person object",
    "create table USERPERSON ("+
      "ID                    "+TYPE_INTEGER+"                   not null,"+
      "FIRSTNAME             "+TYPE_STRING_SHORT+"(128)         not null,"+
      "LASTNAME              "+TYPE_STRING_SHORT+"(128)         not null,"+
      "EMAIL                 "+TYPE_STRING_SHORT+"(128)         not null,"+
      "ORG                   "+TYPE_STRING_SHORT+"(128),"+
      "URL                   "+TYPE_STRING_SHORT+"(254),"+
      "PHONE                 "+TYPE_STRING_SHORT+"(32),"+
      "MOBILE                "+TYPE_STRING_SHORT+"(32),"+
      "FAX                   "+TYPE_STRING_SHORT+"(32),"+
      "PASSWORD              "+TYPE_STRING_SHORT+"(128),"+
      "constraint USERPERSON_UK_ID    unique(ID),"+
      "constraint USERPERSON_FK_ID    foreign key(ID)           references USERABSTRACT(ID)"+
    ")"
  );

  _exec(_stmt, "Table 'USERABSTRACT2ABSTRACT'", "Connection between Abstract User and Abstract User",
    "create table USERABSTRACT2ABSTRACT ("+
      "ID                    "+TYPE_INTEGER+"                   not null,"+
      "TYPEID                "+TYPE_INTEGER+"                   not null,"+
      "CREATOR               "+TYPE_INTEGER+"                   not null,"+
      "CREATED               "+TYPE_DATETIME+"                  not null,"+
      "MODIFIER              "+TYPE_INTEGER+"                   not null,"+
      "MODIFIED              "+TYPE_DATETIME+"                  not null,"+
      "USERABSTRACTFROM      "+TYPE_INTEGER+"                   not null,"+
      "USERABSTRACTTO        "+TYPE_INTEGER+"                   not null,"+
      "constraint USRABS2ABS_PK_ID    primary key(ID),"+
      "constraint USRABS2ABS_UK_FRTO  unique(USERABSTRACTFROM,USERABSTRACTTO),"+
      "constraint USRABS2ABS_FK_CRTR  foreign key(CREATOR)          references USERABSTRACT(ID),"+
      "constraint USRABS2ABS_FK_MDFR  foreign key(MODIFIER)         references USERABSTRACT(ID),"+
      "constraint USRABS2ABS_FK_FROM  foreign key(USERABSTRACTFROM) references USERABSTRACT(ID),"+
      "constraint USRABS2ABS_FK_TO    foreign key(USERABSTRACTTO)   references USERABSTRACT(ID)"+
    ")"
  );
}

/**
 * The private function creates all Team Center tables.
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateAllTeamCenterTables(_stmt)  {

  _exec(_stmt, "Table 'TCFOLDER'", "Folder Object",
    "create table TCFOLDER ("+
      "ID                    "+TYPE_INTEGER+"                   not null,"+
      "PARENTFOLDER          "+TYPE_INTEGER+","+
      "constraint TCFOLDER_UK_ID      unique(ID),"+
      "constraint TCFOLDER_FK_ID      foreign key(ID)           references ABSTRACT(ID),"+
      "constraint TCFOLDER_FK_PRNTFOL foreign key(PARENTFOLDER) references TCFOLDER(ID)"+
    ")"
  );

  _exec(_stmt, "Table 'TCDOCUMENT'", "Document Object",
    "create table TCDOCUMENT ("+
      "ID                    "+TYPE_INTEGER+"                   not null,"+
      "FILENAME              "+TYPE_STRING_SHORT+"(128),"+
      "FILELENGTH            "+TYPE_INTEGER+","+
      "constraint TCDOCUMENT_UK_ID    unique(ID),"+
      "constraint TCDOCUMENT_FK_ID    foreign key(ID)           references ABSTRACT(ID)"+
    ")"
  );

  _exec(_stmt, "Table 'TCDOC2FOL'", "Connection beetween Documents and Folders",
    "create table TCDOC2FOL ("+
      "ID                    "+TYPE_INTEGER+"                   not null,"+
      "TCDOCUMENT            "+TYPE_INTEGER+"                   not null,"+
      "TCFOLDER              "+TYPE_INTEGER+"                   not null,"+
      "CREATOR               "+TYPE_INTEGER+"                   not null,"+
      "CREATED               "+TYPE_DATETIME+"                  not null,"+
      "MODIFIER              "+TYPE_INTEGER+"                   not null,"+
      "MODIFIED              "+TYPE_DATETIME+"                  not null,"+
      "constraint TCDOC2FOL_PK_ID     primary key(ID),"+
      "constraint TCDOC2FOL_UK_FOLDOC unique(TCDOCUMENT,TCFOLDER),"+
      "constraint TCDOC2FOL_FK_TCDOC  foreign key(TCDOCUMENT)   references ABSTRACT(ID),"+
      "constraint TCDOC2FOL_FK_TCFLDR foreign key(TCFOLDER)     references TCFOLDER(ID)"+
    ")"
  );
}


/**
 * The private function creates all user interface tables.
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateAllUITables(_stmt)  {
  print("Create User Interface Tables");

  _exec(_stmt, "Table 'UIFILE'", "table used to import files for user interface objects",
    "create table UIFILE ("+
      "ID                    "+TYPE_INTEGER+"                       not null,"+
      "FILENAME              "+TYPE_STRING_SHORT+"(128),"+
      "FILELENGTH            "+TYPE_INTEGER+","+
      "FILECONTENT           "+TYPE_BLOB+","+
      "constraint UIFILE_UK_ID        unique(ID),"+
      "constraint UIFILE_FK_ID        foreign key(ID)               references ABSTRACT(ID)"+
    ")"
  );

  _exec(_stmt, "Table 'UIABSTRACT2UIABSTRACT'", "connection between UI objects",
    "create table UIABSTRACT2UIABSTRACT ("+
      "ID                    "+TYPE_INTEGER+"                       not null,"+
      "TYPEID                "+TYPE_INTEGER+"                       not null,"+
      "CREATOR               "+TYPE_INTEGER+"                       not null,"+
      "CREATED               "+TYPE_DATETIME+"                      not null,"+
      "MODIFIER              "+TYPE_INTEGER+"                       not null,"+
      "MODIFIED              "+TYPE_DATETIME+"                      not null,"+
      "FROMID                "+TYPE_INTEGER+"                       not null,"+
      "TOID                  "+TYPE_INTEGER+"                       not null,"+
      "constraint UIABS2ABS_PK_ID     primary key(ID),"+
      "constraint UIABS2ABS_FK_CRTR   foreign key(CREATOR)          references USERPERSON(ID),"+
      "constraint UIABS2ABS_FK_MDFR   foreign key(MODIFIER)         references USERPERSON(ID),"+
      "constraint UIABS2ABS_FK_FRMID  foreign key(FROMID)           references ABSTRACT(ID),"+
      "constraint UIABS2ABS_FK_TOID   foreign key(TOID)             references ABSTRACT(ID)"+
    ")"
  );

  _exec(_stmt, "Table 'UIACCESS'", "user access for one UI object",
    "create table UIACCESS ("+
      "UIABSTRACT            "+TYPE_INTEGER+"                       not null,"+
      "USERABSTRACT          "+TYPE_INTEGER+"                       not null,"+
      "CREATOR               "+TYPE_INTEGER+"                       not null,"+
      "CREATED               "+TYPE_DATETIME+"                      not null,"+
      "MODIFIER              "+TYPE_INTEGER+"                       not null,"+
      "MODIFIED              "+TYPE_DATETIME+"                      not null,"+
      "ID                    "+TYPE_INTEGER+"                       not null,"+
      "constraint UIACS_PK_ID                                       primary key(ID),"+
      "constraint UIACS_FK_USRABSTR   foreign key(USERABSTRACT)     references USERABSTRACT(ID),"+
      "constraint UIACS_FK_CRTR       foreign key(CREATOR)          references USERPERSON(ID),"+
      "constraint UIACS_FK_MDFR       foreign key(MODIFIER)         references USERPERSON(ID),"+
      "constraint UIACS_FK_UIABSTR    foreign key(UIABSTRACT)       references ABSTRACT(ID)"+
    ")"
  );

  _exec(_stmt, "Table 'UIFIELD'", "fields for forms and tables",
    "create table UIFIELD ("+
      "ID                    "+TYPE_INTEGER+"                       not null,"+
      "COLLECTION            "+TYPE_INTEGER+"                       not null,"+
      "constraint UIFLD_UK_ID         unique(ID),"+
      "constraint UIFLD_FK_ID         foreign key(ID)               references ABSTRACT(ID),"+
      "constraint UIFLD_FK_CLCT       foreign key(COLLECTION)       references ABSTRACT(ID)"+
    ")"
  );
//      "constraint UIFLD_UK_NAME_CLCT  unique(NAME, COLLECTION),"+

}

function createAll()  {

  var context = new Context();

  try  {

    var stmt = context.getConnection().createStatement();

    ///////////////////////////////////////////////////////////////////////////////

    _eFapsCreateAllUserTables(stmt);

    ///////////////////////////////////////////////////////////////////////////////

    print("Create Abstract Tables");

    _exec(stmt, "Table 'ABSTRACT'", "Abstract",
      "create table ABSTRACT ("+
        "TYPEID                "+TYPE_INTEGER+"                   not null,"+
        "ID                    "+TYPE_INTEGER+"                   not null,"+
        "NAME                  "+TYPE_STRING_SHORT+"(128)         not null,"+
        "REVISION              "+TYPE_STRING_SHORT+"(10),"+
        "CREATOR               "+TYPE_INTEGER+"                   not null,"+
        "CREATED               "+TYPE_DATETIME+"                  not null,"+
        "MODIFIER              "+TYPE_INTEGER+"                   not null,"+
        "MODIFIED              "+TYPE_DATETIME+"                  not null,"+
        "constraint ABSTR_PK_ID         primary key(ID),"+
        "constraint ABSTR_FK_CRTR       foreign key(CREATOR)      references USERPERSON(ID),"+
        "constraint ABSTR_FK_MDFR       foreign key(MODIFIER)     references USERPERSON(ID)"+
      ")"
    );

    _exec(stmt, "Table 'PROPERTY'", "Properties",
      "create table PROPERTY ("+
        "ID                    "+TYPE_INTEGER+"                   not null,"+
        "ABSTRACT              "+TYPE_INTEGER+"                   not null,"+
        "NAME                  "+TYPE_STRING_SHORT+"(128)         not null,"+
        "VALUE                 "+TYPE_STRING_SHORT+"(128),"+
        "constraint PROPERTY_PK_ID      primary key(ID),"+
        "constraint PROPERTY_UK_IDNAME  unique(ABSTRACT,NAME),"+
        "constraint PROPERTY_FK_ABSTR   foreign key(ABSTRACT)     references ABSTRACT(ID) on delete cascade"+
      ")"
    );

    ///////////////////////////////////////////////////////////////////////////////

    print("Create Access Tables");

    _exec(stmt, "Table 'LCPOLICY'", "Policy object",
      "create table LCPOLICY ("+
        "ID                    "+TYPE_INTEGER+"                   not null,"+
        "NAME                  "+TYPE_STRING_SHORT+"(128)         not null,"+
        "CREATOR               "+TYPE_INTEGER+"                   not null,"+
        "CREATED               "+TYPE_DATETIME+"                  not null,"+
        "MODIFIER              "+TYPE_INTEGER+"                   not null,"+
        "MODIFIED              "+TYPE_DATETIME+"                  not null,"+
        "constraint LCPOLICY_PK_ID      primary key(ID),"+
        "constraint LCPOLICY_UK_NAME    unique(NAME),"+
        "constraint LCPOLICY_FK_CRTR    foreign key(CREATOR)      references USERPERSON(ID),"+
        "constraint LCPOLICY_FK_MDFR    foreign key(MODIFIER)     references USERPERSON(ID)"+
      ")"
    );

    _exec(stmt, "Table 'LCSTATUS'", "Status object",
      "create table LCSTATUS ("+
        "ID                    "+TYPE_INTEGER+"                   not null,"+
        "NAME                  "+TYPE_STRING_SHORT+"(128)         not null,"+
        "CREATOR               "+TYPE_INTEGER+"                   not null,"+
        "CREATED               "+TYPE_DATETIME+"                  not null,"+
        "MODIFIER              "+TYPE_INTEGER+"                   not null,"+
        "MODIFIED              "+TYPE_DATETIME+"                  not null,"+
        "LCPOLICY              "+TYPE_INTEGER+"                   not null,"+
        "constraint LCSTS_PK_ID         primary key(ID),"+
        "constraint LCSTS_UK_NM_PLCY    unique(NAME,LCPOLICY),"+
        "constraint LCSTS_FK_CREATOR    foreign key(CREATOR)      references USERPERSON(ID),"+
        "constraint LCSTS_FK_MODIFIER   foreign key(MODIFIER)     references USERPERSON(ID),"+
        "constraint LCSTS_FK_LCPOLICY   foreign key(LCPOLICY)     references LCPOLICY(ID)"+
      ")"
    );

    _exec(stmt, "Table 'LCACCESSTYPE'", "Access Types",
      "create table LCACCESSTYPE ("+
        "ID                    "+TYPE_INTEGER+"                   not null,"+
        "NAME                  "+TYPE_STRING_SHORT+"(128)         not null,"+
        "CREATOR               "+TYPE_INTEGER+"                   not null,"+
        "CREATED               "+TYPE_DATETIME+"                  not null,"+
        "MODIFIER              "+TYPE_INTEGER+"                   not null,"+
        "MODIFIED              "+TYPE_DATETIME+"                  not null,"+
        "constraint LCACCESSTP_PK_ID    primary key(ID),"+
        "constraint LCACCESSTP_UK_NAME  unique(NAME),"+
        "constraint LCACCESSTP_FK_CRTR  foreign key(CREATOR)      references USERPERSON(ID),"+
        "constraint LCACCESSTP_FK_MDFR  foreign key(MODIFIER)     references USERPERSON(ID)"+
      ")"
    );

    _exec(stmt, "Table 'LCSTATUSACCESS'", "Access Itself",
      "create table LCSTATUSACCESS ("+
        "ID                    "+TYPE_INTEGER+"                   not null,"+
        "NAME                  "+TYPE_STRING_SHORT+"(128)         not null,"+
        "CREATOR               "+TYPE_INTEGER+"                   not null,"+
        "CREATED               "+TYPE_DATETIME+"                  not null,"+
        "MODIFIER              "+TYPE_INTEGER+"                   not null,"+
        "MODIFIED              "+TYPE_DATETIME+"                  not null,"+
        "LCSTATUS              "+TYPE_INTEGER+"                   not null,"+
        "LCACCESSTYPE          "+TYPE_INTEGER+"                   not null,"+
        "USERABSTRACT          "+TYPE_INTEGER+"                   not null,"+
        "constraint LCSTSACS_PK_ID      primary key(ID),"+
        "constraint LCSTSACS_UNIQUE     unique(LCSTATUS,LCACCESSTYPE,USERABSTRACT),"+
        "constraint LCSTSACS_FK_CRTR    foreign key(CREATOR)      references USERPERSON(ID),"+
        "constraint LCSTSACS_FK_MDFR    foreign key(MODIFIER)     references USERPERSON(ID),"+
        "constraint LCSTSACS_FK_STS     foreign key(LCSTATUS)     references LCSTATUS(ID),"+
        "constraint LCSTSACS_FK_ACSTP   foreign key(LCACCESSTYPE) references LCACCESSTYPE(ID),"+
        "constraint LCSTSACS_FK_USR     foreign key(USERABSTRACT) references USERABSTRACT(ID)"+
      ")"
    );

    ///////////////////////////////////////////////////////////////////////////////

    print("Create Data Model Tables");

    _exec(stmt, "Table 'DMTABLE'", "sql table names for the data model",
      "create table DMTABLE ("+
        "ID                    "+TYPE_INTEGER+"                   not null,"+
        "SQLTABLE              "+TYPE_STRING_SHORT+"(35)          not null,"+
        "SQLCOLUMNID           "+TYPE_STRING_SHORT+"(35)          not null,"+
        "SQLCOLUMNTYPE         "+TYPE_STRING_SHORT+"(35),"+
        "SQLNEWIDSELECT        "+TYPE_STRING_LONG+"(4000),"+
        "DMTABLEMAIN           "+TYPE_INTEGER+","+
        "constraint DMTABLE_UK_ID       unique(ID),"+
        "constraint DMTABLE_FK_ID       foreign key(ID)                 references ABSTRACT(ID),"+
        "constraint DMTABLE_UK_SQLTBLE  unique(SQLTABLE),"+
        "constraint DMTABLE_FK_DMTBLMN  foreign key(DMTABLEMAIN)        references DMTABLE(ID)"+
      ")"
    );

    _exec(stmt, "Table 'DMTYPE'", "type definition",
      "create table DMTYPE ("+
        "ID                    "+TYPE_INTEGER+"                   not null,"+
        "PARENTDMTYPE          "+TYPE_INTEGER+","+
        "SQLCACHEEXPR          "+TYPE_STRING_SHORT+"(50),"+
        "constraint DMTYPE_UK_ID        unique(ID),"+
        "constraint DMTYPE_FK_ID        foreign key(ID)           references ABSTRACT(ID),"+
        "constraint DMTYPE_FK_PRNTDMTP  foreign key(PARENTDMTYPE) references DMTYPE(ID)"+
      ")"
    );

    _exec(stmt, "Table 'DMTYPE2POLICY'", "connection of policies for type definitions",
      "create table DMTYPE2POLICY ("+
        "ID                    "+TYPE_INTEGER+"                   not null,"+
        "CREATOR               "+TYPE_INTEGER+"                   not null,"+
        "CREATED               "+TYPE_DATETIME+"                  not null,"+
        "MODIFIER              "+TYPE_INTEGER+"                   not null,"+
        "MODIFIED              "+TYPE_DATETIME+"                  not null,"+
        "DMTYPE                "+TYPE_INTEGER+"                   not null,"+
        "LCPOLICY              "+TYPE_INTEGER+"                   not null,"+
        "constraint DMTPE2PLCY_PK_ID    primary key(ID),"+
        "constraint DMTPE2PLCY_FK_CRTR  foreign key(CREATOR)      references USERPERSON(ID),"+
        "constraint DMTPE2PLCY_FK_MDFR  foreign key(MODIFIER)     references USERPERSON(ID),"+
        "constraint DMTPE2PLCY_FK_DMTP  foreign key(DMTYPE)       references DMTYPE(ID),"+
        "constraint DMTPE2PLCY_FK_PLCY  foreign key(LCPOLICY)     references LCPOLICY(ID)"+
      ")"
    );

    _exec(stmt, "Table 'DMATTRIBUTETYPE'", "attribute types",
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

    _exec(stmt, "Table 'DMATTRIBUTE'", "attributes of types",
      "create table DMATTRIBUTE ("+
        "ID                    "+TYPE_INTEGER+"                       not null,"+
        "DMTABLE               "+TYPE_INTEGER+"                       not null,"+
        "DMTYPE                "+TYPE_INTEGER+"                       not null,"+
        "DMATTRIBUTETYPE       "+TYPE_INTEGER+"                       not null,"+
        "DMTYPELINK            "+TYPE_INTEGER+","+
        "SQLCOLUMN             "+TYPE_STRING_SHORT+"(50)              not null,"+
        "constraint DMATTR_UK_ID        unique(ID),"+
        "constraint DMATTR_FK_ID        foreign key(ID)               references ABSTRACT(ID),"+
        "constraint DMATTR_FK_DMTABLE   foreign key(DMTABLE)          references DMTABLE(ID),"+
        "constraint DMATTR_FK_DMTYPE    foreign key(DMTYPE)           references DMTYPE(ID),"+
        "constraint DMATTR_FK_DMATTRTP  foreign key(DMATTRIBUTETYPE)  references DMATTRIBUTETYPE(ID),"+
        "constraint DMATTR_FK_DMTPLINK  foreign key(DMTYPELINK)       references DMTYPE(ID)"+
      ")"
    );
//        "constraint DMATTR_UK_DMTP_NM   unique(DMTYPE,NAME),"+

    _exec(stmt, "View 'ADMINTYPE'", "view representing all types",
      "create view ADMINTYPE as "+
        "select "+
            "ABSTRACT.ID,"+
            "ABSTRACT.NAME,"+
            "DMTYPE.PARENTDMTYPE,"+
            "DMTYPE.SQLCACHEEXPR "+
          "from DMTYPE,ABSTRACT "+
          "where ABSTRACT.ID=DMTYPE.ID"
    );

    _exec(stmt, "View 'ADMINATTRIBUTE'", "view representing all attributes",
      "create view ADMINATTRIBUTE as "+
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

    _exec(stmt, "View 'ADMINSQLTABLE'", "view representing all sql tables",
      "create view ADMINSQLTABLE as "+
        "select "+
              "ABSTRACT.ID,"+
              "ABSTRACT.NAME,"+
              "DMTABLE.SQLTABLE,"+
              "DMTABLE.SQLCOLUMNID,"+
              "DMTABLE.SQLCOLUMNTYPE,"+
              "DMTABLE.SQLNEWIDSELECT,"+
              "DMTABLE.DMTABLEMAIN "+
          "from DMTABLE,ABSTRACT "+
          "where ABSTRACT.ID=DMTABLE.ID"
    );


    ///////////////////////////////////////////////////////////////////////////////

    _eFapsCreateAllUITables(stmt);
    _eFapsCreateAllTeamCenterTables(stmt);

    ///////////////////////////////////////////////////////////////////////////////

    print("Standard Inserts");

    _exec(stmt, "Insert Administrator Person", "",
      "insert into USERABSTRACT(TYPEID, ID, NAME, CREATOR, CREATED, MODIFIER, MODIFIED, STATUS) "+
          "values (10000, 1,'Administrator', 1, "+CURRENT_TIMESTAMP+", 1, "+CURRENT_TIMESTAMP+", 10001)"
    );
    _exec(stmt, null, null,
      "insert into USERPERSON(ID, FIRSTNAME, LASTNAME, EMAIL, URL, PASSWORD) "+
          "values (1,'The','Administrator','info@efaps.org','www.efaps.org', '')"
    );


    _exec(stmt, "Insert Administrator Role",  "",
      "insert into USERABSTRACT(TYPEID, ID, NAME, CREATOR, CREATED, MODIFIER, MODIFIED, STATUS) "+
          "values (11000, 1000,'Administration', 1, "+CURRENT_TIMESTAMP+", 1, "+CURRENT_TIMESTAMP+", 10001)"
    );

    _exec(stmt, "Connect Administrator Person to Role Administration", "",
      "insert into USERABSTRACT2ABSTRACT "+
          "values (1, 10100, 1, "+CURRENT_TIMESTAMP+", 1, "+CURRENT_TIMESTAMP+", 1, 1000)"
    );

    var text = "Insert all access types";
    _exec(stmt, text, "",   "insert into LCACCESSTYPE values (100,'show',    1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");
    _exec(stmt, null, null, "insert into LCACCESSTYPE values (101,'read',    1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");
    _exec(stmt, null, null, "insert into LCACCESSTYPE values (102,'modify',  1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");
    _exec(stmt, null, null, "insert into LCACCESSTYPE values (110,'create',  1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");
    _exec(stmt, null, null, "insert into LCACCESSTYPE values (111,'delete',  1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");
    _exec(stmt, null, null, "insert into LCACCESSTYPE values (120,'checkout',1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");
    _exec(stmt, null, null, "insert into LCACCESSTYPE values (121,'checkin', 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");
    _exec(stmt, null, null, "insert into LCACCESSTYPE values (130,'promote', 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");
    _exec(stmt, null, null, "insert into LCACCESSTYPE values (131,'demote',  1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");

    text = "Insert Person Policy";
    _exec(stmt, text, "",   "insert into LCPOLICY values (10000,'Admin_User_Person',1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")");
    _exec(stmt, null, null, "insert into LCSTATUS values (10000,'Inactive',1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",10000)");
    _exec(stmt, null, null, "insert into LCSTATUS values (10001,'Active',1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",10000)");

_eFapsCreateAllInsertAttributeTypes(stmt);

    _exec(stmt, "Foreign Contraint", "", 
      "alter table USERABSTRACT add constraint USRABSTR_FK_CRTR   foreign key(CREATOR)    references USERPERSON(ID)"
    );
    _exec(stmt, "Foreign Contraint", "",
      "alter table USERABSTRACT add constraint USRABSTR_MDFR      foreign key(MODIFIER)   references USERPERSON(ID)"
    );
    //alter table USERABSTRACT add constraint USRABSTR_FK_STS     foreign key(STATUS)     references LCSTATUS(ID);

    ///////////////////////////////////////////////////////////////////////////////

    text = "Insert Table for 'Admin_Abstract'";
    _eFapsCreateInsertSQLTable(stmt, text, 15999, "Admin_AbstractTable", "ABSTRACT", "ID", "TYPEID", "select max(ID)+1 from ABSTRACT", null);

    text = "Insert Type for 'Admin_Abstract'";
    _eFapsCreateInsertType(stmt, text, 15000, "Admin_Abstract", null);
    _eFapsCreateInsertAttr(stmt, 15999, 15000, 15001, 'Type',             'TYPEID',            99, null);
    _eFapsCreateInsertAttr(stmt, 15999, 15000, 15002, 'OID',              'TYPEID,ID',        111, null);
    _eFapsCreateInsertAttr(stmt, 15999, 15000, 15003, 'ID',               'ID',               210, null);
    _eFapsCreateInsertAttr(stmt, 15999, 15000, 15004, 'Creator',          'CREATOR',          411, null);
    _eFapsCreateInsertAttr(stmt, 15999, 15000, 15005, 'Created',          'CREATED',          331, null);
    _eFapsCreateInsertAttr(stmt, 15999, 15000, 15006, 'Modifier',         'MODIFIER',         412, null);
    _eFapsCreateInsertAttr(stmt, 15999, 15000, 15007, 'Modified',         'MODIFIED',         332, null);
    _eFapsCreateInsertAttr(stmt, 15999, 15000, 15008, 'Name',             'NAME',             100, null);
    _eFapsCreateInsertAttr(stmt, 15999, 15000, 15009, 'Revision',         'REVISION',         100, null);

    text = "Insert Table for 'Admin_DataModel_SQLTable'";
    _eFapsCreateInsertSQLTable(stmt, text, 20999, "Admin_DataModel_SQLTableTable", "DMTABLE", "ID", null, null, 15999);

    text = "Insert Type for 'Admin_DataModel_SQLTable'";
    _eFapsCreateInsertType(stmt, text, 20000, "Admin_DataModel_SQLTable", 15000);
    _eFapsCreateInsertAttr(stmt, 20999, 20000, 20020, 'SQLTable',         'SQLTABLE',         100, null);
    _eFapsCreateInsertAttr(stmt, 20999, 20000, 20021, 'SQLColumnID',      'SQLCOLUMNID',      100, null);
    _eFapsCreateInsertAttr(stmt, 20999, 20000, 20022, 'SQLColumnType',    'SQLCOLUMNTYPE',    100, null);
    _eFapsCreateInsertAttr(stmt, 20999, 20000, 20023, 'SQLNewIDSelect',   'SQLNEWIDSELECT',   100, null);
    _eFapsCreateInsertAttr(stmt, 20999, 20000, 20024, 'DMTableMain',      'DMTABLEMAIN',      400, 20000);
    _exec(stmt, null, null, "insert into PROPERTY values(20000,20000,'Tree','Admin_DataModel_SQLTableTree')");
    _exec(stmt, null, null, "insert into PROPERTY values(20001,20000,'Icon','${COMMONURL}/Image.jsp?name=eFapsAdminDataModelSQLTable')");

    text = "Insert Table for 'Admin_DataModel_Type'";
    _eFapsCreateInsertSQLTable(stmt, text, 21999, "Admin_DataModel_TypeTable", "DMTYPE", "ID", null, null, 15999);

    text = "Insert Type for 'Admin_DataModel_Type'";
    _eFapsCreateInsertType(stmt, text, 21000, "Admin_DataModel_Type", 15000);
    _eFapsCreateInsertAttr(stmt, 21999, 21000, 21020, 'SQLCacheExpr',     'SQLCACHEEXPR',     100, null);
    _eFapsCreateInsertAttr(stmt, 21999, 21000, 21021, 'ParentType',       'PARENTDMTYPE',     400, 21000);
    _exec(stmt, null, null, "insert into PROPERTY values(21000,21000,'Tree','Admin_DataModel_TypeTree')");
    _exec(stmt, null, null, "insert into PROPERTY values(21001,21000,'Icon','${COMMONURL}/Image.jsp?name=eFapsAdminDataModelType')");

    text = "Insert Table for 'Admin_DataModel_AttributeType'";
    _eFapsCreateInsertSQLTable(stmt, text, 22999, "Admin_DataModel_AttributeTypeTable", "DMATTRIBUTETYPE", "ID", null, "select DMATTRIBUTETYPE_SEQ.nextval from DUAL", null);

    text = "Insert Type for 'Admin_DataModel_AttributeType'";
    _eFapsCreateInsertType(stmt, text, 22000, "Admin_DataModel_AttributeType", null);
    _eFapsCreateInsertAttr(stmt, 22999, 22000, 22001, 'OID',              'ID',               111, null);
    _eFapsCreateInsertAttr(stmt, 22999, 22000, 22002, 'ID',               'ID',               210, null);
    _eFapsCreateInsertAttr(stmt, 22999, 22000, 22003, 'Name',             'NAME',             100, null);
    _eFapsCreateInsertAttr(stmt, 22999, 22000, 22004, 'Creator',          'CREATOR',          411, null);
    _eFapsCreateInsertAttr(stmt, 22999, 22000, 22005, 'Created',          'CREATED',          331, null);
    _eFapsCreateInsertAttr(stmt, 22999, 22000, 22006, 'Modifier',         'MODIFIER',         412, null);
    _eFapsCreateInsertAttr(stmt, 22999, 22000, 22007, 'Modified',         'MODIFIED',         332, null);
    _eFapsCreateInsertAttr(stmt, 22999, 22000, 22020, 'Classname',        'CLASSNAME',        100, null);
    _eFapsCreateInsertAttr(stmt, 22999, 22000, 22021, 'ClassnameUI',      'CLASSNAMEUI',      100, null);
    _eFapsCreateInsertAttr(stmt, 22999, 22000, 22022, 'AlwaysUpdate',     'ALWAYSUPDATE',     290, null);
    _eFapsCreateInsertAttr(stmt, 22999, 22000, 22023, 'CreateUpdate',     'CREATEUPDATE',     290, null);
    _exec(stmt, null, null, "insert into PROPERTY values(22000,22000,'Tree','Admin_DataModel_AttributeTypeTree')");
    _exec(stmt, null, null, "insert into PROPERTY values(22001,22000,'Icon','${COMMONURL}/Image.jsp?name=eFapsAdminDataModelAttributeType')");

    text = "Insert Table for 'Admin_DataModel_Attribute'";
    _eFapsCreateInsertSQLTable(stmt, text, 23999, "Admin_DataModel_AttributeTable", "DMATTRIBUTE", "ID", null, null, 15999);

    text = "Insert Type for 'Admin_DataModel_Attribute'";
    _eFapsCreateInsertType(stmt, text, 23000, "Admin_DataModel_Attribute", 15000);
    _eFapsCreateInsertAttr(stmt, 23999, 23000, 23020, 'Table',             'DMTABLE',         400, 20000);
    _eFapsCreateInsertAttr(stmt, 23999, 23000, 23021, 'ParentType',        'DMTYPE',          400, 21000);
    _eFapsCreateInsertAttr(stmt, 23999, 23000, 23022, 'AttributeType',     'DMATTRIBUTETYPE', 400, 22000);
    _eFapsCreateInsertAttr(stmt, 23999, 23000, 23023, 'TypeLink',          'DMTYPELINK',      400, 21000);
    _eFapsCreateInsertAttr(stmt, 23999, 23000, 23024, 'SQLColumn',         'SQLCOLUMN',       100, null);
    _exec(stmt, null, null, "insert into PROPERTY values(23000,23000,'Tree','Admin_DataModel_AttributeTree')");
    _exec(stmt, null, null, "insert into PROPERTY values(23001,23000,'Icon','${COMMONURL}/Image.jsp?name=eFapsAdminDataModelAttribute')");

    text = "Insert Table for 'Admin_Property'";
    _eFapsCreateInsertSQLTable(stmt, text, 26999, "Admin_PropertyTable", "PROPERTY", "ID", null, "select max(ID)+1 from PROPERTY", null);

    text = "Insert Type for 'Admin_Property'";
    _eFapsCreateInsertType(stmt, text, 26000, "Admin_Property", null);
    _eFapsCreateInsertAttr(stmt, 26999, 26000, 26001, 'OID',              'ID',               111, null);
    _eFapsCreateInsertAttr(stmt, 26999, 26000, 26002, 'ID',               'ID',               210, null);
    _eFapsCreateInsertAttr(stmt, 26999, 26000, 26003, 'Name',             'NAME',             100, null);
    _eFapsCreateInsertAttr(stmt, 26999, 26000, 26020, 'Value',            'VALUE',            100, null);
    _eFapsCreateInsertAttr(stmt, 26999, 26000, 26021, 'Abstract',         'ABSTRACT',         400, 15000);
    _exec(stmt, null, null, "insert into PROPERTY values(26000,26000,'Tree','Admin_PropertyTree')");
    _exec(stmt, null, null, "insert into PROPERTY values(26001,26000,'Icon','${COMMONURL}/Image.jsp?name=Admin_PropertyImage')");

    text = "Insert Table for 'Admin_DataModel_Table2Type'";
    _eFapsCreateInsertSQLTable(stmt, text, 27999, "Admin_DataModel_Table2TypeTable", "DMTABLE2TYPE", "ID", null, null, null);
    _exec(stmt, null, null, "insert into PROPERTY values (27999, 27999, 'ReadOnly', 'true')");

    text = "Insert Type for 'Admin_DataModel_Table2Type'";
    _eFapsCreateInsertType(stmt, text, 27000, "Admin_DataModel_Table2Type", null);
    _eFapsCreateInsertAttr(stmt, 27999, 27000, 27001, 'OID',              'ID',               111, null);
    _eFapsCreateInsertAttr(stmt, 27999, 27000, 27002, 'ID',               'ID',               210, null);
    _eFapsCreateInsertAttr(stmt, 27999, 27000, 27003, 'FromTable',        'FROMID',           400, 20000);
    _eFapsCreateInsertAttr(stmt, 27999, 27000, 27004, 'ToType',           'TOID',             400, 21000);

    ///////////////////////////////////////////////////////////////////////////////

    text = "Insert Type for 'Admin_User_Person' (only to store ID for type)";
    _eFapsCreateInsertType(stmt, text, 10000, "Admin_User_Person", null);
    _exec(stmt, null, null, "insert into DMTYPE2POLICY       values (1,1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",10000,10000)");

    text = "Insert Type for 'Admin_User_Person2Role' (only to store ID for type)";
    _eFapsCreateInsertType(stmt, text, 10100, "Admin_User_Person2Role", null);

    text = "Insert Type for 'Admin_User_Person2Group' (only to store ID for type)";
    _eFapsCreateInsertType(stmt, text, 10200, "Admin_User_Person2Group", null);

    text = "Insert Type for 'Admin_User_Role' (only to store ID for type)";
    _eFapsCreateInsertType(stmt, text, 11000, "Admin_User_Role", null);

    text = "Insert Type for 'Admin_User_Group' (only to store ID for type)";
    _eFapsCreateInsertType(stmt, text, 12000, "Admin_User_Group", null);

    ///////////////////////////////////////////////////////////////////////////////

    stmt.close();
  } catch (e)  {
    print(e);
    throw e;
  } finally  {
    context.close();
  }
}