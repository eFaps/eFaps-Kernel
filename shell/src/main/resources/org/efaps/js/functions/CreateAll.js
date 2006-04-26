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
function _eFapsCreateLog(_subject, _text)  {
  if (_text!=null && _subject!=null)  {
    print("  - " + _subject + "  (" + _text + ")");
  } else if (_subject!=null)  {
    print("  - " + _subject);
  }
}

function _exec(_stmt, _subject, _text, _cmd)  {
  _eFapsCreateLog(_subject, _text);
  var bck = _stmt.execute(_cmd);
}

function _insert(_stmt, _subject, _text, _cmd, _table)  {
  var ret;

  _eFapsCreateLog(_subject, _text);

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
  reloadCache();

  Shell.transactionManager.begin();
  var context = new Context(Shell.transactionManager.getTransaction(), Packages.org.efaps.admin.user.Person.get("Administrator"), null);
  Context.setThreadContext(context);
  Shell.setContext(context);
  _eFapsCreateAllImportDataModel();
  Shell.transactionManager.commit();
  context.close();

  print("############ Reload Cache");
  reloadCache();

  Shell.transactionManager.begin();
  var context = new Context(Shell.transactionManager.getTransaction(), Packages.org.efaps.admin.user.Person.get("Administrator"), null);
  Context.setThreadContext(context);
  Shell.setContext(context);
  _eFapsCreateAllImportUserInterface();
  Shell.transactionManager.commit();
  context.close();

  print("############ Reload Cache");
  reloadCache();

  _eFapsCreateAllUpdatePassword();
}


function _eFapsCreateCreateTable(_con, _stmt, _text, _table, _parentTable, _array)  {
  _eFapsCreateLog("Table '" + _table + "'", _text);
  
  Context.getDbType().createTable(_con, _table, _parentTable);
  for (var i=0; i<_array.length; i++)  {
    _stmt.execute("alter table " + _table + " add " + _array[i]);
  }
}

function _eFapsCreateInsertSQLTable(_stmt, _text, _name, _sqlTable, _sqlColId, _sqlColType, _tableMainId)  {
  var sqlColType = (_sqlColType==null ? "null" : "'"+_sqlColType+"'");
  var tableMainId = (_tableMainId==null ? "null" : _tableMainId);

  var ret = _insert(_stmt, _text, null, 
      "insert into ABSTRACT "+
          "(TYPEID,NAME,REVISION,CREATOR,CREATED,MODIFIER,MODIFIED) "
          +"values  (-20000, '"+_name+"', '', 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")", "ABSTRACT");
  _exec(_stmt, null, null,  "insert into DMTABLE values  ("+ret+",'"+_sqlTable+"','"+_sqlColId+"',"+sqlColType+","+tableMainId+")");
  return ret;
}

function _eFapsCreateInsertType(_stmt, _text, _name, _parentTypeId)  {
  var parentTypeId = (_parentTypeId==null ? "null" : _parentTypeId);

  var ret = _insert(_stmt, _text, null, 
      "insert into ABSTRACT "+
          "(TYPEID,NAME,REVISION,CREATOR,CREATED,MODIFIER,MODIFIED) "
          +"values  (-21000, '"+_name+"', '', 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")", "ABSTRACT");
  _exec(_stmt, null, null, "insert into DMTYPE values  ("+ret+", "+parentTypeId+", null)");
  return ret;
}

function _eFapsCreateInsertAttr(_stmt, _tableId, _typeId, _name, _sqlColumn, _attrTypeId, _typeLink)  {
  var typeLink = (_typeLink==null ? "null" : _typeLink);

  var ret = _insert(_stmt, null, null, 
      "insert into ABSTRACT "+
          "(TYPEID,NAME,REVISION,CREATOR,CREATED,MODIFIER,MODIFIED) "
          +"values  (-22000, '"+_name+"', '', 1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")", "ABSTRACT");
  _exec(_stmt, null, null, "insert into DMATTRIBUTE values  ("+ret+", "+_tableId+", "+_typeId+",  "+_attrTypeId+", "+typeLink+", '"+_sqlColumn+"')");
  return ret;
}

function _eFapsCreateInsertProp(_stmt, _abstractId, _key, _value)  {
  _exec(_stmt, null, null, 
      "insert into PROPERTY (ABSTRACT,NAME,VALUE) "
          +"values("+_abstractId+",'"+_key+"','"+_value+"')");
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
 * The private function inserts the SQL Tables for the event definitions.
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateAllEventTable(_con, _stmt)  {
  print("Create Event SQL Table");

  _eFapsCreateCreateTable(_con, _stmt, "Definition of Events (e.g. Triggers)", "EVENTDEF", "ABSTRACT",[
      ["ABSTRACT              "+TYPE_INTEGER+"                   not null"],
      ["INDEXPOS              "+TYPE_INTEGER+"                   not null"],
      ["constraint EVENTDEF_UK_ID_INDEXPOS unique(ID,INDEXPOS)"]
  ]);

  _eFapsCreateCreateTable(_con, _stmt, "History of Events for Objects", "HISTORY", null,[
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
}

/**
 * The private function creates all user tables.
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateAllUserTables(_con, _stmt)  {
  print("Create User Tables");

  _eFapsCreateCreateTable(_con, _stmt, "Abstract User", "USERABSTRACT", null,[
      ["TYPEID                "+TYPE_INTEGER+"                   not null"],
      ["NAME                  "+TYPE_STRING_SHORT+"(128)         not null"],
      ["CREATOR               "+TYPE_INTEGER+"                   not null"],
      ["CREATED               "+TYPE_DATETIME+"                  not null"],
      ["MODIFIER              "+TYPE_INTEGER+"                   not null"],
      ["MODIFIED              "+TYPE_DATETIME+"                  not null"],
["STATUS  int  default 10001 not null"],
      ["constraint USERABSTR_UK_NAME   unique(NAME)"]
  ]);

  _eFapsCreateCreateTable(_con, _stmt, "Person object", "USERPERSON", "USERABSTRACT",[
      ["FIRSTNAME             "+TYPE_STRING_SHORT+"(128)         not null"],
      ["LASTNAME              "+TYPE_STRING_SHORT+"(128)         not null"],
      ["EMAIL                 "+TYPE_STRING_SHORT+"(128)         not null"],
      ["ORG                   "+TYPE_STRING_SHORT+"(128)"],
      ["URL                   "+TYPE_STRING_SHORT+"(254)"],
      ["PHONE                 "+TYPE_STRING_SHORT+"(32)"],
      ["MOBILE                "+TYPE_STRING_SHORT+"(32)"],
      ["FAX                   "+TYPE_STRING_SHORT+"(32)"],
      ["PASSWORD              "+TYPE_STRING_SHORT+"(128)"]
  ]);

  _eFapsCreateCreateTable(_con, _stmt, "Connection between Abstract User and Abstract User", "USERABSTRACT2ABSTRACT", null,[
      ["TYPEID                "+TYPE_INTEGER+"                   not null"],
      ["CREATOR               "+TYPE_INTEGER+"                   not null"],
      ["CREATED               "+TYPE_DATETIME+"                  not null"],
      ["MODIFIER              "+TYPE_INTEGER+"                   not null"],
      ["MODIFIED              "+TYPE_DATETIME+"                  not null"],
      ["USERABSTRACTFROM      "+TYPE_INTEGER+"                   not null"],
      ["USERABSTRACTTO        "+TYPE_INTEGER+"                   not null"],
      ["constraint USRABS2ABS_UK_FRTO  unique(USERABSTRACTFROM,USERABSTRACTTO)"],
      ["constraint USRABS2ABS_FK_CRTR  foreign key(CREATOR)          references USERABSTRACT(ID)"],
      ["constraint USRABS2ABS_FK_MDFR  foreign key(MODIFIER)         references USERABSTRACT(ID)"],
      ["constraint USRABS2ABS_FK_FROM  foreign key(USERABSTRACTFROM) references USERABSTRACT(ID)"],
      ["constraint USRABS2ABS_FK_TO    foreign key(USERABSTRACTTO)   references USERABSTRACT(ID)"]
  ]);
}

/**
 * The private function creates all Team Center tables.
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateAllTeamCenterTables(_con, _stmt)  {

  print("Create TeamCenter Tables");

  _eFapsCreateCreateTable(_con, _stmt, "Folder object", "TCFOLDER", "ABSTRACT",[
      ["PARENTFOLDER          "+TYPE_INTEGER],
      ["constraint TCFOLDER_FK_PRNTFOL foreign key(PARENTFOLDER) references TCFOLDER(ID)"]
  ]);

  _eFapsCreateCreateTable(_con, _stmt, "Document object", "TCDOCUMENT", "ABSTRACT",[
      ["FILENAME              "+TYPE_STRING_SHORT+"(128)"],
      ["FILELENGTH            "+TYPE_INTEGER]
  ]);

  _eFapsCreateCreateTable(_con, _stmt, "Connection beetween Documents and Folders", "TCDOC2FOL", null,[
      ["TCDOCUMENT            "+TYPE_INTEGER+"                   not null"],
      ["TCFOLDER              "+TYPE_INTEGER+"                   not null"],
      ["CREATOR               "+TYPE_INTEGER+"                   not null"],
      ["CREATED               "+TYPE_DATETIME+"                  not null"],
      ["MODIFIER              "+TYPE_INTEGER+"                   not null"],
      ["MODIFIED              "+TYPE_DATETIME+"                  not null"],
      ["constraint TCDOC2FOL_UK_FOLDOC unique(TCDOCUMENT,TCFOLDER)"],
      ["constraint TCDOC2FOL_FK_TCDOC  foreign key(TCDOCUMENT)   references ABSTRACT(ID)"],
      ["constraint TCDOC2FOL_FK_TCFLDR foreign key(TCFOLDER)     references TCFOLDER(ID)"],
      ["constraint TCDOC2FOL_FK_CRTR   foreign key(CREATOR)      references USERPERSON(ID)"],
      ["constraint TCDOC2FOL_FK_MDFR   foreign key(MODIFIER)     references USERPERSON(ID)"]
  ]);
}


/**
 * The private function creates all user interface tables.
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateAllUITables(_con, _stmt)  {
  print("Create User Interface Tables");

  _eFapsCreateCreateTable(_con, _stmt, "table used to import files for user interface objects", "UIFILE", "ABSTRACT",[
      ["FILENAME              "+TYPE_STRING_SHORT+"(128)"],
      ["FILELENGTH            "+TYPE_INTEGER],
      ["FILECONTENT           "+TYPE_BLOB]
  ]);

  _eFapsCreateCreateTable(_con, _stmt, "connection between UI objects", "UIABSTRACT2UIABSTRACT", null,[
      ["TYPEID                "+TYPE_INTEGER+"                       not null"],
      ["CREATOR               "+TYPE_INTEGER+"                       not null"],
      ["CREATED               "+TYPE_DATETIME+"                      not null"],
      ["MODIFIER              "+TYPE_INTEGER+"                       not null"],
      ["MODIFIED              "+TYPE_DATETIME+"                      not null"],
      ["FROMID                "+TYPE_INTEGER+"                       not null"],
      ["TOID                  "+TYPE_INTEGER+"                       not null"],
      ["constraint UIABS2ABS_FK_CRTR   foreign key(CREATOR)          references USERPERSON(ID)"],
      ["constraint UIABS2ABS_FK_MDFR   foreign key(MODIFIER)         references USERPERSON(ID)"],
      ["constraint UIABS2ABS_FK_FRMID  foreign key(FROMID)           references ABSTRACT(ID)"],
      ["constraint UIABS2ABS_FK_TOID   foreign key(TOID)             references ABSTRACT(ID)"]
  ]);

  _eFapsCreateCreateTable(_con, _stmt, "user access for one UI object", "UIACCESS", null,[
      ["UIABSTRACT            "+TYPE_INTEGER+"                       not null"],
      ["USERABSTRACT          "+TYPE_INTEGER+"                       not null"],
      ["CREATOR               "+TYPE_INTEGER+"                       not null"],
      ["CREATED               "+TYPE_DATETIME+"                      not null"],
      ["MODIFIER              "+TYPE_INTEGER+"                       not null"],
      ["MODIFIED              "+TYPE_DATETIME+"                      not null"],
      ["constraint UIACS_FK_USRABSTR   foreign key(USERABSTRACT)     references USERABSTRACT(ID)"],
      ["constraint UIACS_FK_CRTR       foreign key(CREATOR)          references USERPERSON(ID)"],
      ["constraint UIACS_FK_MDFR       foreign key(MODIFIER)         references USERPERSON(ID)"],
      ["constraint UIACS_FK_UIABSTR    foreign key(UIABSTRACT)       references ABSTRACT(ID)"]
  ]);

  _eFapsCreateCreateTable(_con, _stmt, "fields for forms and tables", "UIFIELD", "ABSTRACT",[
      ["COLLECTION            "+TYPE_INTEGER+"                       not null"],
      ["constraint UIFLD_FK_CLCT       foreign key(COLLECTION)       references ABSTRACT(ID)"]
  ]);
//      "constraint UIFLD_UK_NAME_CLCT  unique(NAME, COLLECTION),"+

}

function createAll()  {

  var context = new Context();

  try  {
    var con = context.getConnection();
    var stmt = con.createStatement();

    ///////////////////////////////////////////////////////////////////////////////

    _eFapsCreateAllUserTables(context.getConnection(), stmt);

    ///////////////////////////////////////////////////////////////////////////////

    print("Create Abstract Tables");

    Context.getDbType().createTable(con, "ABSTRACT", null);
    _exec(stmt, "Table 'ABSTRACT'", "Abstract",
      "alter table ABSTRACT "+
        "add TYPEID                "+TYPE_INTEGER+"                   not null "+
        "add NAME                  "+TYPE_STRING_SHORT+"(128)         not null "+
        "add REVISION              "+TYPE_STRING_SHORT+"(10) "+
        "add CREATOR               "+TYPE_INTEGER+"                   not null "+
        "add CREATED               "+TYPE_DATETIME+"                  not null "+
        "add MODIFIER              "+TYPE_INTEGER+"                   not null "+
        "add MODIFIED              "+TYPE_DATETIME+"                  not null "+
        "add constraint ABSTR_FK_CRTR       foreign key(CREATOR)      references USERPERSON(ID) "+
        "add constraint ABSTR_FK_MDFR       foreign key(MODIFIER)     references USERPERSON(ID)"
    );

    Context.getDbType().createTable(con, "PROPERTY", null);
    _exec(stmt, "Table 'PROPERTY'", "Properties",
      "alter table PROPERTY "+
        "add ABSTRACT              "+TYPE_INTEGER+"                   not null "+
        "add NAME                  "+TYPE_STRING_SHORT+"(128)         not null "+
        "add VALUE                 "+TYPE_STRING_SHORT+"(128) "+
        "add constraint PROPERTY_UK_ABNAME  unique(ABSTRACT,NAME) "+
        "add constraint PROPERTY_FK_ABSTR   foreign key(ABSTRACT)     references ABSTRACT(ID) on delete cascade"
    );

    ///////////////////////////////////////////////////////////////////////////////

    print("Create Access Tables");

    _eFapsCreateCreateTable(con, stmt, "Policy object", "LCPOLICY", null,[
        ["NAME                  "+TYPE_STRING_SHORT+"(128)         not null"],
        ["CREATOR               "+TYPE_INTEGER+"                   not null"],
        ["CREATED               "+TYPE_DATETIME+"                  not null"],
        ["MODIFIER              "+TYPE_INTEGER+"                   not null"],
        ["MODIFIED              "+TYPE_DATETIME+"                  not null"],
        ["constraint LCPOLICY_UK_NAME    unique(NAME)"],
        ["constraint LCPOLICY_FK_CRTR    foreign key(CREATOR)      references USERPERSON(ID)"],
        ["constraint LCPOLICY_FK_MDFR    foreign key(MODIFIER)     references USERPERSON(ID)"]
    ]);

    _eFapsCreateCreateTable(con, stmt, "Status object", "LCSTATUS", null,[
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

    _eFapsCreateCreateTable(con, stmt, "Access Itself", "LCSTATUSACCESS", null,[
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
        ["constraint LCSTSACS_FK_ACSTP   foreign key(LCACCESSTYPE) references LCACCESSTYPE(ID)"],
        ["constraint LCSTSACS_FK_USR     foreign key(USERABSTRACT) references USERABSTRACT(ID)"]
    ]);

    ///////////////////////////////////////////////////////////////////////////////

    print("Create Data Model Tables");

    _eFapsCreateCreateTable(con, stmt, "sql table names for the data model", "DMTABLE", "ABSTRACT",[
        ["SQLTABLE              "+TYPE_STRING_SHORT+"(35)          not null"],
        ["SQLCOLUMNID           "+TYPE_STRING_SHORT+"(35)          not null"],
        ["SQLCOLUMNTYPE         "+TYPE_STRING_SHORT+"(35)"],
        ["DMTABLEMAIN           "+TYPE_INTEGER],
        ["constraint DMTABLE_UK_SQLTBLE  unique(SQLTABLE)"],
        ["constraint DMTABLE_FK_DMTBLMN  foreign key(DMTABLEMAIN)  references DMTABLE(ID)"]
    ]);

    _eFapsCreateCreateTable(con, stmt, "type definition", "DMTYPE", "ABSTRACT",[
        ["PARENTDMTYPE          "+TYPE_INTEGER],
        ["SQLCACHEEXPR          "+TYPE_STRING_SHORT+"(50)"],
        ["constraint DMTYPE_FK_PRNTDMTP  foreign key(PARENTDMTYPE) references DMTYPE(ID)"]
    ]);

    _eFapsCreateCreateTable(con, stmt, "connection of policies for type definitions", "DMTYPE2POLICY", null,[
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

    _eFapsCreateCreateTable(con, stmt, "attributes of types", "DMATTRIBUTE", "ABSTRACT",[
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
              "DMTABLE.DMTABLEMAIN "+
          "from DMTABLE,ABSTRACT "+
          "where ABSTRACT.ID=DMTABLE.ID"
    );


    ///////////////////////////////////////////////////////////////////////////////

    _eFapsCreateAllEventTable(con, stmt);
    _eFapsCreateAllUITables(con, stmt);
    _eFapsCreateAllTeamCenterTables(con, stmt);

    ///////////////////////////////////////////////////////////////////////////////

    print("Standard Inserts");

    _exec(stmt, "Insert Administrator Person", null,
      "insert into USERABSTRACT(TYPEID, NAME, CREATOR, CREATED, MODIFIER, MODIFIED, STATUS) "+
          "values (-10000, 'Administrator', 1, "+CURRENT_TIMESTAMP+", 1, "+CURRENT_TIMESTAMP+", 10001)"
    );
    _exec(stmt, null, null,
      "insert into USERPERSON(ID, FIRSTNAME, LASTNAME, EMAIL, URL, PASSWORD) "+
          "values (1,'The','Administrator','info@efaps.org','www.efaps.org', '')"
    );


    _exec(stmt, "Insert Administrator Role",  null,
      "insert into USERABSTRACT(TYPEID, NAME, CREATOR, CREATED, MODIFIER, MODIFIED, STATUS) "+
          "values (-11000, 'Administration', 1, "+CURRENT_TIMESTAMP+", 1, "+CURRENT_TIMESTAMP+", 10001)"
    );

    _exec(stmt, "Connect Administrator Person to Role Administration", null,
      "insert into USERABSTRACT2ABSTRACT(TYPEID,CREATOR,CREATED,MODIFIER,MODIFIED,USERABSTRACTFROM,USERABSTRACTTO) "+
          "values (-12000, 1, "+CURRENT_TIMESTAMP+", 1, "+CURRENT_TIMESTAMP+", 1, 2)"
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
    var newId = _insert(stmt, text, "",   "insert into LCPOLICY(NAME,CREATOR,CREATED,MODIFIER,MODIFIED) values ('Admin_User_Person',1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+")","LCPOLICY");
    _exec(stmt, null, null, "insert into LCSTATUS(NAME,CREATOR,CREATED,MODIFIER,MODIFIED,LCPOLICY) values ('Inactive',1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+","+newId+")");
    _exec(stmt, null, null, "insert into LCSTATUS(NAME,CREATOR,CREATED,MODIFIER,MODIFIED,LCPOLICY) values ('Active',1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+","+newId+")");




_eFapsCreateAllInsertAttributeTypes(stmt);

    _exec(stmt, "Foreign Contraint", "", 
      "alter table USERABSTRACT add constraint USRABSTR_FK_CRTR   foreign key(CREATOR)    references USERPERSON(ID)"
    );
    _exec(stmt, "Foreign Contraint", "",
      "alter table USERABSTRACT add constraint USRABSTR_MDFR      foreign key(MODIFIER)   references USERPERSON(ID)"
    );
    //alter table USERABSTRACT add constraint USRABSTR_FK_STS     foreign key(STATUS)     references LCSTATUS(ID);

    ///////////////////////////////////////////////////////////////////////////////
    // insert 'abstract'

    text = "Insert Table for 'Admin_Abstract'";
    var sqlTableIdAbstract = _eFapsCreateInsertSQLTable(stmt, text, "Admin_AbstractTable", "ABSTRACT", "ID", "TYPEID", null);

    text = "Insert Type for 'Admin_Abstract'";
    var typeIdAbstract = _eFapsCreateInsertType(stmt, text, "Admin_Abstract", null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Type',             'TYPEID',            99, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'OID',              'TYPEID,ID',        111, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'ID',               'ID',               210, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Creator',          'CREATOR',          411, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Created',          'CREATED',          331, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Modifier',         'MODIFIER',         412, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Modified',         'MODIFIED',         332, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Name',             'NAME',             100, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Revision',         'REVISION',         100, null);

    /////////////////////////////////////////
    // insert 'sql table' 

    text = "Insert Table for 'Admin_DataModel_SQLTable'";
    var sqlTableIdSQLTable = _eFapsCreateInsertSQLTable(stmt, text, "Admin_DataModel_SQLTableTable", "DMTABLE", "ID", null, sqlTableIdAbstract);

    text = "Insert Type for 'Admin_DataModel_SQLTable'";
    var typeIdSQLTable = _eFapsCreateInsertType(stmt, text, "Admin_DataModel_SQLTable", typeIdAbstract);
    _eFapsCreateInsertAttr(stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLTable',         'SQLTABLE',         100, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLColumnID',      'SQLCOLUMNID',      100, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLColumnType',    'SQLCOLUMNTYPE',    100, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLNewIDSelect',   'SQLNEWIDSELECT',   100, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdSQLTable, typeIdSQLTable, 'DMTableMain',      'DMTABLEMAIN',      400, typeIdSQLTable);
    _eFapsCreateInsertProp(stmt, typeIdSQLTable, "Tree", "Admin_DataModel_SQLTableTree");
    _eFapsCreateInsertProp(stmt, typeIdSQLTable, "Icon", "${ROOTURL}/servlet/image/eFapsAdminDataModelSQLTable");

    /////////////////////////////////////////
    // insert 'type' 
    
    text = "Insert Table for 'Admin_DataModel_Type'";
    var sqlTableIdType = _eFapsCreateInsertSQLTable(stmt, text, "Admin_DataModel_TypeTable", "DMTYPE", "ID", null, sqlTableIdAbstract);

    text = "Insert Type for 'Admin_DataModel_Type'";
    var typeIdType = _eFapsCreateInsertType(stmt, text, "Admin_DataModel_Type", typeIdAbstract);
    _eFapsCreateInsertAttr(stmt, sqlTableIdType, typeIdType, 'SQLCacheExpr',     'SQLCACHEEXPR',     100, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdType, typeIdType, 'ParentType',       'PARENTDMTYPE',     400, typeIdType);
    _eFapsCreateInsertProp(stmt, typeIdType, "Tree", "Admin_DataModel_TypeTree");
    _eFapsCreateInsertProp(stmt, typeIdType, "Icon", "${ROOTURL}/servlet/image/eFapsAdminDataModelType");

    /////////////////////////////////////////
    // insert 'attribute type' 

    text = "Insert Table for 'Admin_DataModel_AttributeType'";
    var sqlTableIdAttrType = _eFapsCreateInsertSQLTable(stmt, text, "Admin_DataModel_AttributeTypeTable", "DMATTRIBUTETYPE", "ID", null, null);

    text = "Insert Type for 'Admin_DataModel_AttributeType'";
    var typeIdAttrType = _eFapsCreateInsertType(stmt, text, "Admin_DataModel_AttributeType", null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttrType, typeIdAttrType, 'OID',              'ID',               111, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttrType, typeIdAttrType, 'ID',               'ID',               210, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttrType, typeIdAttrType, 'Name',             'NAME',             100, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttrType, typeIdAttrType, 'Creator',          'CREATOR',          411, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttrType, typeIdAttrType, 'Created',          'CREATED',          331, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttrType, typeIdAttrType, 'Modifier',         'MODIFIER',         412, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttrType, typeIdAttrType, 'Modified',         'MODIFIED',         332, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttrType, typeIdAttrType, 'Classname',        'CLASSNAME',        100, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttrType, typeIdAttrType, 'ClassnameUI',      'CLASSNAMEUI',      100, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttrType, typeIdAttrType, 'AlwaysUpdate',     'ALWAYSUPDATE',     290, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttrType, typeIdAttrType, 'CreateUpdate',     'CREATEUPDATE',     290, null);
    _eFapsCreateInsertProp(stmt, typeIdAttrType, "Tree", "Admin_DataModel_AttributeTypeTree");
    _eFapsCreateInsertProp(stmt, typeIdAttrType, "Icon", "${ROOTURL}/servlet/image/eFapsAdminDataModelAttributeType");

    /////////////////////////////////////////
    // insert 'attribute' 

    text = "Insert Table for 'Admin_DataModel_Attribute'";
    var sqlTableIdAttr = _eFapsCreateInsertSQLTable(stmt, text, "Admin_DataModel_AttributeTable", "DMATTRIBUTE", "ID", null, sqlTableIdAbstract);

    text = "Insert Type for 'Admin_DataModel_Attribute'";
    var typeIdAttr = _eFapsCreateInsertType(stmt, text, "Admin_DataModel_Attribute", typeIdAbstract);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttr, typeIdAttr, 'Table',             'DMTABLE',         400, typeIdSQLTable);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttr, typeIdAttr, 'ParentType',        'DMTYPE',          400, typeIdType);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttr, typeIdAttr, 'AttributeType',     'DMATTRIBUTETYPE', 400, typeIdAttrType);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttr, typeIdAttr, 'TypeLink',          'DMTYPELINK',      400, typeIdType);
    _eFapsCreateInsertAttr(stmt, sqlTableIdAttr, typeIdAttr, 'SQLColumn',         'SQLCOLUMN',       100, null);
    _eFapsCreateInsertProp(stmt, typeIdAttr, "Tree", "Admin_DataModel_AttributeTree");
    _eFapsCreateInsertProp(stmt, typeIdAttr, "Icon", "${ROOTURL}/servlet/image/eFapsAdminDataModelAttribute");

    /////////////////////////////////////////
    // insert 'admin property' 

    text = "Insert Table for 'Admin_Property'";
    var sqlTableIdProp = _eFapsCreateInsertSQLTable(stmt, text, "Admin_PropertyTable", "PROPERTY", "ID", null, null);

    text = "Insert Type for 'Admin_Property'";
    var typeIdProp = _eFapsCreateInsertType(stmt, text, "Admin_Property", null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdProp, typeIdProp, 'OID',              'ID',               111, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdProp, typeIdProp, 'ID',               'ID',               210, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdProp, typeIdProp, 'Name',             'NAME',             100, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdProp, typeIdProp, 'Value',            'VALUE',            100, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdProp, typeIdProp, 'Abstract',         'ABSTRACT',         400, typeIdAbstract);
    _eFapsCreateInsertProp(stmt, typeIdProp, "Tree", "Admin_PropertyTree");
    _eFapsCreateInsertProp(stmt, typeIdProp, "Icon", "${ROOTURL}/servlet/image/Admin_PropertyImage");

    /////////////////////////////////////////
    // insert 'sql table 2 type' 

    text = "Insert Table for 'Admin_DataModel_Table2Type'";
    var sqlTableIdTable2Type = _eFapsCreateInsertSQLTable(stmt, text, "Admin_DataModel_Table2TypeTable", "DMTABLE2TYPE", "ID", null, null);
    _eFapsCreateInsertProp(stmt, sqlTableIdTable2Type, "ReadOnly", "true");

    text = "Insert Type for 'Admin_DataModel_Table2Type'";
    var typeIdTable2Type = _eFapsCreateInsertType(stmt, text, "Admin_DataModel_Table2Type", null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdTable2Type, typeIdTable2Type, 'OID',              'ID',               111, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdTable2Type, typeIdTable2Type, 'ID',               'ID',               210, null);
    _eFapsCreateInsertAttr(stmt, sqlTableIdTable2Type, typeIdTable2Type, 'FromTable',        'FROMID',           400, typeIdSQLTable);
    _eFapsCreateInsertAttr(stmt, sqlTableIdTable2Type, typeIdTable2Type, 'ToType',           'TOID',             400, typeIdType);

    /////////////////////////////////////////
    // insert 'Event Definition' 

    print("Create Event Definitions (needed for reload cache)");

    text = "Insert Table for 'Admin_Event_Definition'";
    var sqlTableEventDef = _eFapsCreateInsertSQLTable(stmt, text, "Admin_Event_DefinitionTable", "EVENTDEF", "ID", null, sqlTableIdAbstract);

    text = "Insert Type for 'Admin_Event_Definition'";
    var typeIdEventDef = _eFapsCreateInsertType(stmt, text, "Admin_Event_Definition", typeIdAbstract);
    _eFapsCreateInsertAttr(stmt, sqlTableEventDef, typeIdEventDef, "IndexPosition",    "INDEXPOS",         210, null);
    _eFapsCreateInsertAttr(stmt, sqlTableEventDef, typeIdEventDef, "Abstract",         "ABSTRACT",         400, typeIdAbstract);

    ///////////////////////////////////////////////////////////////////////////////

    print("Insert User Types and create User Views");

    text = "Insert Type for 'Admin_User_Person' (only to store ID for type)";
    var typeIdPerson        = _eFapsCreateInsertType(stmt, text, "Admin_User_Person", null);
/*    
    _exec(stmt, null, null, "insert into DMTYPE2POLICY       values (1,1,"+CURRENT_TIMESTAMP+",1,"+CURRENT_TIMESTAMP+",10000,10000)");
*/

    text = "Insert Type for 'Admin_User_Role' (only to store ID for type)";
    var typeIdRole          = _eFapsCreateInsertType(stmt, text, "Admin_User_Role", null);

    text = "Insert Type for 'Admin_User_Group' (only to store ID for type)";
    var typeIdGroup         = _eFapsCreateInsertType(stmt, text, "Admin_User_Group", null);
   
    text = "Insert Type for 'Admin_User_Person2Role' (only to store ID for type)";
    var typeIdPerson2Role   = _eFapsCreateInsertType(stmt, text, "Admin_User_Person2Role", null);

    text = "Insert Type for 'Admin_User_Person2Group' (only to store ID for type)";
    var typeIdPerson2Group  = _eFapsCreateInsertType(stmt, text, "Admin_User_Person2Group", null);


    _exec(stmt, "View 'V_USERPERSON'", "view representing all persons",
      "create view V_USERPERSON as "+
        "select "+
            "USERABSTRACT.ID,"+
            "USERABSTRACT.NAME,"+
            "USERPERSON.FIRSTNAME,"+
            "USERPERSON.LASTNAME,"+
            "USERPERSON.EMAIL,"+
            "USERPERSON.ORG,"+
            "USERPERSON.URL,"+
            "USERPERSON.PHONE,"+
            "USERPERSON.MOBILE,"+
            "USERPERSON.FAX,"+
            "USERPERSON.PASSWORD "+
          "from USERABSTRACT,USERPERSON "+
          "where USERABSTRACT.ID=USERPERSON.ID"
    );

    _exec(stmt, "View 'V_USERROLE'", "view representing all roles",
      "create view V_USERROLE as "+
        "select "+
            "USERABSTRACT.ID,"+
            "USERABSTRACT.NAME "+
          "from USERABSTRACT "+
          "where USERABSTRACT.TYPEID="+typeIdRole
    );

    _exec(stmt, "View 'V_USERGROUP'", "view representing all groups",
      "create view V_USERGROUP as "+
        "select "+
            "USERABSTRACT.ID,"+
            "USERABSTRACT.NAME "+
          "from USERABSTRACT "+
          "where USERABSTRACT.TYPEID="+typeIdGroup
    );

    _exec(stmt, "View 'V_USERPERSON2ROLE'", "view representing connection between person and role",
      "create view V_USERPERSON2ROLE as "+
        "select "+
            "USERABSTRACT2ABSTRACT.ID,"+
            "USERABSTRACT2ABSTRACT.USERABSTRACTFROM,"+
            "USERABSTRACT2ABSTRACT.USERABSTRACTTO "+
          "from USERABSTRACT2ABSTRACT "+
          "where USERABSTRACT2ABSTRACT.TYPEID="+typeIdPerson2Role
    );

    _exec(stmt, "View 'V_USERPERSON2GROUP'", "view representing connection between person and group",
      "create view V_USERPERSON2GROUP as "+
        "select "+
            "USERABSTRACT2ABSTRACT.ID,"+
            "USERABSTRACT2ABSTRACT.USERABSTRACTFROM,"+
            "USERABSTRACT2ABSTRACT.USERABSTRACTTO "+
          "from USERABSTRACT2ABSTRACT "+
          "where USERABSTRACT2ABSTRACT.TYPEID="+typeIdPerson2Group
    );

    _exec(stmt, "Table 'USERABSTRACT'", "update type id for persons",
      "update USERABSTRACT set TYPEID="+typeIdPerson+" where TYPEID=-10000"
    );
    _exec(stmt, "Table 'USERABSTRACT'", "update type id for persons",
      "update USERABSTRACT set TYPEID="+typeIdRole+" where TYPEID=-11000"
    );
    _exec(stmt, "Table 'USERABSTRACT'", "define foreign key for type id",
      "alter table USERABSTRACT "+
        "add constraint USERABSTR_FK_TYPEID foreign key(TYPEID) references DMTYPE(ID)"
    );

    _exec(stmt, "Table 'USERABSTRACT2ABSTRACT'", "update type id for connection between person and role",
      "update USERABSTRACT2ABSTRACT set TYPEID="+typeIdPerson2Role+" where TYPEID=-12000"
    );
    _exec(stmt, "Table 'USERABSTRACT2ABSTRACT'", "define foreign key for type id",
      "alter table USERABSTRACT2ABSTRACT "+
        "add constraint USRABS2ABS_FK_TYPEID foreign key(TYPEID) references DMTYPE(ID)"
    );

    ///////////////////////////////////////////////////////////////////////////////

    print("Activate foreign key for type id in table abstract");

    _exec(stmt, "Table 'ABSTRACT'", "update type id for sql tables",
      "update ABSTRACT set TYPEID=" + typeIdSQLTable + " where TYPEID=-20000"
    );
    _exec(stmt, "Table 'ABSTRACT'", "update type id for types",
      "update ABSTRACT set TYPEID=" + typeIdType + " where TYPEID=-21000"
    );
    _exec(stmt, "Table 'ABSTRACT'", "update type id for sql tables",
      "update ABSTRACT set TYPEID=" + typeIdAttr + " where TYPEID=-22000"
    );
    _exec(stmt, "Table 'ABSTRACT'", "define foreign key for type id",
      "alter table ABSTRACT "+
        "add constraint ABSTR_FK_TYPEID foreign key(TYPEID) references DMTYPE(ID)"
    );


    stmt.close();
  } catch (e)  {
    print(e);
    throw e;
  } finally  {
    context.close();
  }
}