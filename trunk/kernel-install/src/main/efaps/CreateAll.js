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
 * Author:          The eFaps Team
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

importClass(Packages.org.efaps.db.Context);

var CURRENT_TIMESTAMP = Context.getDbType().getCurrentTimeStamp();

/**
 * Prints the given text out.
 *
 * @param _text (String) text to print out
 */
function _eFapsPrint(_text)  {
  EFAPS_LOGGER.info("    " + _text);
}

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
function _eFapsCommonLog(_subject, _text)  {
  if (_text!=null && _subject!=null)  {
    _eFapsPrint("  - " + _subject + "  (" + _text + ")");
  } else if (_subject!=null)  {
    _eFapsPrint("  - " + _subject);
  }
}

function _eFapsCommonSQLTableUpdate(_stmt, _text, _table, _array)  {
  _eFapsCommonLog("Update Table '" + _table + "'", _text);

  for (var i=0; i<_array.length; i++)  {
    _stmt.execute("alter table " + _table + " add " + _array[i]);
  }
}

function _exec(_stmt, _subject, _text, _cmd)  {
  _eFapsCommonLog(_subject, _text);
  var bck = _stmt.execute(_cmd);
}

function _insert(_stmt, _subject, _text, _table, _columns, _values)  {
  var ret;

  _eFapsCommonLog(_subject, _text);

  var cmd =  "insert into " + _table + " (";
  if (!Context.getDbType().supportsGetGeneratedKeys())  {
    cmd += "ID,";
  }
  cmd += _columns + ") values  (";
  if (!Context.getDbType().supportsGetGeneratedKeys())  {
    var newId = Context.getDbType().getNewId(_stmt.getConnection(), _table, "ID");
    cmd += newId  + ",";
  }
  cmd += _values + ")";
  _stmt.execute(cmd);
  var rs = _stmt.executeQuery("select max(ID) from " + _table);

  if (rs.next())  {
    ret= rs.getString(1);
  }
  rs.close();
  return ret;
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

  Context.begin();
  _eFapsCreateUserTablesStep1();
  _eFapsCreateDataModelTablesStep1();
  _eFapsCreateCommonTablesStep2();
  _eFapsCreateDataModelTablesStep2();
  _eFapsCreateUserTablesStep2();
  _eFapsInitRunLevel();
  Context.commit();

}

function _eFapsCreateInsertSQLTable(_stmt, _text, _uuid, _name, _sqlTable, _sqlColId, _sqlColType, _tableMain)  {
  var sqlColType = (_sqlColType==null ? "null" : "'"+_sqlColType+"'");

  var rs = _stmt.executeQuery("select ID from T_CMABSTRACT where NAME='Admin_DataModel_SQLTable'");
  var typeIdSQLTable = "-20000";
  if (rs.next())  {
    typeIdSQLTable = rs.getString(1);
  }
  rs.close();

  // get id for SQL Table defined in _tableMain
  var tableMainId = "null";
  if (_tableMain != null)  {
    var rs = _stmt.executeQuery("select ID from T_CMABSTRACT where NAME='" + _tableMain + "'");
    rs.next();
    tableMainId = rs.getString(1);
    rs.close();
  }

  var ret = _insert(_stmt, _text, null,
                    "T_CMABSTRACT",
                    "TYPEID,UUID,NAME,REVISION,CREATOR,CREATED,MODIFIER,MODIFIED",
                    typeIdSQLTable
                        + ", '" + _uuid + "'"
                        + ",'" + _name + "'"
                        + ",''"
                        + ", 1"
                        + "," + CURRENT_TIMESTAMP
                        + ",1"
                        + "," + CURRENT_TIMESTAMP);
  _exec(_stmt, null, null,  "insert into T_DMTABLE values  (" + ret + ",'" + _sqlTable + "','" + _sqlColId + "'," + sqlColType + "," + tableMainId + ")");
  return ret;
}

function _eFapsCreateInsertType(_stmt, _text, _uuid, _name, _parentType)  {
  // get id for type 'Admin_DataModel_Type'
  var rs = _stmt.executeQuery("select ID from T_CMABSTRACT where NAME='Admin_DataModel_Type'");
  var typeIdType = "-21000";
  if (rs.next())  {
    typeIdType = rs.getString(1);
  }
  rs.close();

  // get id for given type name in _parentType
  var parentTypeId = "null";
  if (_parentType != null)  {
    rs = _stmt.executeQuery("select ID from T_CMABSTRACT where NAME='" + _parentType + "'");
    rs.next();
    parentTypeId = rs.getString(1);
    rs.close();
  }

  var ret = _insert(_stmt, _text, null,
                    "T_CMABSTRACT",
                    "TYPEID,NAME,UUID,REVISION,CREATOR,CREATED,MODIFIER,MODIFIED",
                    typeIdType + ", '" + _name + "','" + _uuid + "','',1," + CURRENT_TIMESTAMP + ",1," + CURRENT_TIMESTAMP);
  _exec(_stmt, null, null, "insert into T_DMTYPE values  (" + ret + ", " + parentTypeId + ", null)");
  return ret;
}

function _eFapsCreateInsertAttr(_stmt, _tableId, _typeId, _name, _sqlColumn, _attrType, _typeLink)  {
  // get if for type 'Admin_DataModel_Attribute'
  var rs = _stmt.executeQuery("select ID from T_CMABSTRACT where NAME='Admin_DataModel_Attribute'");
  var typeIdAttr = "-22000";
  if (rs.next())  {
    typeIdAttr = rs.getString(1);
  }
  rs.close();

  // get id for given type name in _typeLink
  var typeLinkId = "null";
  if (_typeLink != null)  {
    rs = _stmt.executeQuery("select ID from T_CMABSTRACT where NAME='" + _typeLink + "'");
    rs.next();
    typeLinkId = rs.getString(1);
    rs.close();
  }

  // get id for given type name in _parentType
  rs = _stmt.executeQuery("select ID from V_DMATTRIBUTETYPE where NAME='" + _attrType + "'");
  rs.next();
  var attrTypeId = rs.getString(1);
  rs.close();

  var ret = _insert(_stmt, null, null,
                    "T_CMABSTRACT",
                    "TYPEID,NAME,REVISION,CREATOR,CREATED,MODIFIER,MODIFIED",
                    typeIdAttr + ", '" + _name + "', '', 1," + CURRENT_TIMESTAMP + ",1," + CURRENT_TIMESTAMP);
  _exec(_stmt, null, null, "insert into T_DMATTRIBUTE (ID, DMTABLE, DMTYPE, DMATTRIBUTETYPE, DMTYPELINK, SQLCOLUMN ) values  (" + ret + ", " + _tableId + ", " + _typeId + ",  " + attrTypeId + ", " + typeLinkId + ", '" + _sqlColumn + "')");
  return ret;
}

/**
 * The private function creates all user tables.
 */
function _eFapsCreateUserTablesStep1()  {
  _eFapsPrint("Create User Tables");

  var conRsrc = Context.getThreadContext().getConnectionResource();
  var stmt = conRsrc.getConnection().createStatement();

  _insert(stmt, "Insert JAAS System eFaps", null,
          "T_USERJAASSYSTEM",
          "NAME, UUID, "
                + "CREATOR, CREATED, MODIFIER, MODIFIED, "
                + "CLASSNAMEPERSON,"
                + "CLASSNAMEROLE,"
                + "CLASSNAMEGROUP,"
                + "METHODPERSONKEY,"
                + "METHODPERSONNAME,"
                + "METHODROLEKEY,"
                + "METHODGROUPKEY",
          "'eFaps', '878a1347-a5f3-4a68-a9a4-d214e3570a62',"
                + "1, " + CURRENT_TIMESTAMP + ", 1, " + CURRENT_TIMESTAMP + ", "
                + "'org.efaps.jaas.efaps.PersonPrincipal',"
                + "'org.efaps.jaas.efaps.RolePrincipal',"
                + "'org.efaps.jaas.efaps.GroupPrincipal',"
                + "'getName',"
                + "'getName',"
                + "'getName',"
                + "'getName'");

  _insert(stmt, "Insert Administrator Person", null,
          "T_USERABSTRACT",
          "TYPEID, NAME, CREATOR, CREATED, MODIFIER, MODIFIED",
          "-10000, '" + EFAPS_USERNAME + "', 1, " + CURRENT_TIMESTAMP + ", 1, " + CURRENT_TIMESTAMP
  );
  _exec(stmt, null, null,
    "insert into T_USERPERSON(ID, FIRSTNAME, LASTNAME, PASSWORD) "+
        "values (1,'The','Administrator', '')"
  );

  _insert(stmt, "Insert Administrator Role",  null,
          "T_USERABSTRACT",
          "TYPEID, NAME, CREATOR, CREATED, MODIFIER, MODIFIED",
          "-11000, 'Administration', 1, " + CURRENT_TIMESTAMP + ", 1, " + CURRENT_TIMESTAMP
  );

  _insert(stmt, "Connect Administrator Person to Role Administration", null,
          "T_USERABSTRACT2ABSTRACT",
          "TYPEID,CREATOR,CREATED,MODIFIER,MODIFIED,USERABSTRACTFROM,USERABSTRACTTO,USERJAASSYSTEM",
          "-12000, 1, " + CURRENT_TIMESTAMP + ", 1, " + CURRENT_TIMESTAMP + ", 1, 2, 1"
  );

  conRsrc.commit();
}

/**
 * The private function creates all user tables.
 */
function _eFapsCreateUserTablesStep2()  {

    _eFapsPrint("Insert User Types and create User Views");

    var conRsrc = Context.getThreadContext().getConnectionResource();
    var stmt = conRsrc.getConnection().createStatement();

    text = "Insert Type for 'Admin_User_Person' (only to store ID for type)";
    var typeIdPerson        = _eFapsCreateInsertType(stmt, text, "fe9d94fd-2ed8-4c44-b1f0-00e150555888", "Admin_User_Person", null);

    text = "Insert Type for 'Admin_User_Role' (only to store ID for type)";
    var typeIdRole          = _eFapsCreateInsertType(stmt, text, "e4d6ecbe-f198-4f84-aa69-5a9fd3165112", "Admin_User_Role", null);

    text = "Insert Type for 'Admin_User_Group' (only to store ID for type)";
    var typeIdGroup         = _eFapsCreateInsertType(stmt, text, "f5e1e2ff-bfa9-40d9-8340-a259f48d5ad9", "Admin_User_Group", null);

    text = "Insert Type for 'Admin_User_Company' (only to store ID for type)";
    var typeIdCompany       = _eFapsCreateInsertType(stmt, text, "6a5388e9-7f7f-4bc0-b7a0-3245302faad5", "Admin_User_Company", null);

    text = "Insert Type for 'Admin_User_Person2Role' (only to store ID for type)";
    var typeIdPerson2Role   = _eFapsCreateInsertType(stmt, text, "37deb6ae-3e1c-4642-8823-715120386fc3", "Admin_User_Person2Role", null);

    text = "Insert Type for 'Admin_User_Person2Group' (only to store ID for type)";
    var typeIdPerson2Group  = _eFapsCreateInsertType(stmt, text, "fec64148-a39b-4f69-bedd-9c3bcfe8e1602", "Admin_User_Person2Group", null);

    text = "Insert Type for 'Admin_User_Person2Company' (only to store ID for type)";
    var typeIdPerson2Company  = _eFapsCreateInsertType(stmt, text, "a79898fb-966a-44ee-a338-d034e2aad83a", "Admin_User_Person2Company", null);

    _exec(stmt, "Table 'T_USERABSTRACT'", "update type id for persons",
      "update T_USERABSTRACT set TYPEID=" + typeIdPerson + " where TYPEID=-10000"
    );
    _exec(stmt, "Table 'T_USERABSTRACT'", "update type id for persons",
      "update T_USERABSTRACT set TYPEID=" + typeIdRole + " where TYPEID=-11000"
    );
    _eFapsCommonSQLTableUpdate(stmt, "Foreign Contraint for column TYPEID", "T_USERABSTRACT", [
        ["constraint USERABSTR_FK_TYPEID foreign key(TYPEID) references T_DMTYPE(ID)"]
    ]);

    _exec(stmt, "Table 'T_USERABSTRACT2ABSTRACT'", "update type id for connection between person and role",
      "update T_USERABSTRACT2ABSTRACT set TYPEID="+typeIdPerson2Role+" where TYPEID=-12000"
    );
    _eFapsCommonSQLTableUpdate(stmt, "Foreign Contraint for column TYPEID", "T_USERABSTRACT2ABSTRACT", [
        ["constraint USRABS2ABS_FK_TYPEID foreign key(TYPEID) references T_DMTYPE(ID)"]
    ]);

    _exec(stmt, "View 'V_USERPERSONJASSKEY'", "view representing all persons related to the JAAS keys",
      "create view V_USERPERSONJASSKEY as "
        + "select "
        +       "T_USERABSTRACT.ID,"
        +       "T_USERABSTRACT.NAME,"
        +       "T_USERJAASKEY.USERJAASSYSTEM as JAASSYSID,"
        +       "T_USERJAASKEY.JAASKEY as JAASKEY "
        +   "from T_USERABSTRACT,T_USERJAASKEY "
        +   "where T_USERABSTRACT.TYPEID=" + typeIdPerson + " "
        +       "and T_USERABSTRACT.ID=T_USERJAASKEY.USERABSTRACT"
    );


    _exec(stmt, "View 'V_USERROLE'", "view representing all roles",
      "create view V_USERROLE as "
        + "select "
        +       "T_USERABSTRACT.ID,"
        +       "T_USERABSTRACT.NAME, "
        +       "T_USERABSTRACT.UUID, "
        +       "T_USERABSTRACT.STATUS "
        +   "from T_USERABSTRACT "
        +   "where T_USERABSTRACT.TYPEID=" + typeIdRole
    );

    _exec(stmt, "View 'V_USERROLEJASSKEY'", "view representing all roles related to the JAAS keys",
      "create view V_USERROLEJASSKEY as "
        + "select "
        +       "T_USERABSTRACT.ID,"
        +       "T_USERABSTRACT.NAME,"
        +       "T_USERJAASKEY.USERJAASSYSTEM as JAASSYSID,"
        +       "T_USERJAASKEY.JAASKEY as JAASKEY "
        +   "from T_USERABSTRACT,T_USERJAASKEY "
        +   "where T_USERABSTRACT.TYPEID=" + typeIdRole + " "
        +       "and T_USERABSTRACT.ID=T_USERJAASKEY.USERABSTRACT"
    );

    _exec(stmt, "View 'V_USERGROUP'", "view representing all groups",
      "create view V_USERGROUP as "+
        "select "+
            "T_USERABSTRACT.ID,"+
            "T_USERABSTRACT.NAME, "+
            "T_USERABSTRACT.STATUS "+
          "from T_USERABSTRACT "+
          "where T_USERABSTRACT.TYPEID="+typeIdGroup
    );

    _exec(stmt, "View 'V_USERCOMPANY'", "view representing all companies",
            "create view V_USERCOMPANY as "+
              "select "+
                  "T_USERABSTRACT.ID,"+
                  "T_USERABSTRACT.NAME, "+
                  "T_USERABSTRACT.UUID, " +
                  "T_USERABSTRACT.STATUS "+
                "from T_USERABSTRACT "+
                "where T_USERABSTRACT.TYPEID="+ typeIdCompany
          );

    _exec(stmt, "View 'V_USERGROUPJASSKEY'", "view representing all groups related to the JAAS keys",
      "create view V_USERGROUPJASSKEY as "
        + "select "
        +       "T_USERABSTRACT.ID,"
        +       "T_USERABSTRACT.NAME,"
        +       "T_USERJAASKEY.USERJAASSYSTEM as JAASSYSID,"
        +       "T_USERJAASKEY.JAASKEY as JAASKEY "
        +   "from T_USERABSTRACT,T_USERJAASKEY "
        +   "where T_USERABSTRACT.TYPEID=" + typeIdGroup + " "
        +       "and T_USERABSTRACT.ID=T_USERJAASKEY.USERABSTRACT"
    );

    _exec(stmt, "View 'V_USERPERSON2ROLE'", "view representing connection between person and role depending on JAAS systems",
      "create view V_USERPERSON2ROLE as "
        + "select "
        +       "T_USERABSTRACT2ABSTRACT.ID,"
        +       "T_USERABSTRACT2ABSTRACT.USERABSTRACTFROM,"
        +       "T_USERABSTRACT2ABSTRACT.USERABSTRACTTO,"
        +       "T_USERABSTRACT2ABSTRACT.USERJAASSYSTEM as JAASSYSID "
        +   "from T_USERABSTRACT2ABSTRACT "
        +   "where T_USERABSTRACT2ABSTRACT.TYPEID=" + typeIdPerson2Role
    );

    _exec(stmt, "View 'V_USERPERSON2COMPANY'", "view representing connection between person and company depending on JAAS systems",
            "create view V_USERPERSON2COMPANY as "
              + "select "
              +       "T_USERABSTRACT2ABSTRACT.ID,"
              +       "T_USERABSTRACT2ABSTRACT.USERABSTRACTFROM,"
              +       "T_USERABSTRACT2ABSTRACT.USERABSTRACTTO,"
              +       "T_USERABSTRACT2ABSTRACT.USERJAASSYSTEM as JAASSYSID "
              +   "from T_USERABSTRACT2ABSTRACT "
              +   "where T_USERABSTRACT2ABSTRACT.TYPEID=" + typeIdPerson2Company
          );

    _exec(stmt, "View 'V_USERPERSON2GROUP'", "view representing connection between person and group depending on JAAS systems",
      "create view V_USERPERSON2GROUP as "
        + "select "
        +       "T_USERABSTRACT2ABSTRACT.ID,"
        +       "T_USERABSTRACT2ABSTRACT.USERABSTRACTFROM,"
        +       "T_USERABSTRACT2ABSTRACT.USERABSTRACTTO,"
        +       "T_USERABSTRACT2ABSTRACT.USERJAASSYSTEM as JAASSYSID "
        +   "from T_USERABSTRACT2ABSTRACT "
        +   "where T_USERABSTRACT2ABSTRACT.TYPEID=" + typeIdPerson2Group
    );

    conRsrc.commit();
}


function _eFapsCreateAttrType(_stmt, _text, _uuid, _name, _classDM, _classUI, _alUpd, _crUp)  {
  // get id for type 'Admin_DataModel_Type'
  var rs = _stmt.executeQuery("select ID from T_CMABSTRACT where NAME='Admin_DataModel_AttributeType'");
  rs.next();
  var typeIdType = rs.getString(1);
  rs.close();

  var ret = _insert(_stmt, _text, null,
                    "T_CMABSTRACT",
                    "TYPEID,NAME,UUID,REVISION,CREATOR,CREATED,MODIFIER,MODIFIED",
                    typeIdType + ", '" + _name + "','" + _uuid + "','',1," + CURRENT_TIMESTAMP + ",1," + CURRENT_TIMESTAMP);
  _exec(_stmt, null, null, "insert into T_DMATTRIBUTETYPE(ID,CLASSNAME,CLASSNAMEUI,ALWAYSUPDATE,CREATEUPDATE)"
                           + " values  (" + ret + ", '" + _classDM + "', '" + _classUI + "', " + _alUpd + ", " + _crUp + ")");
  return ret;
}

var ATTRTYPESQLTABLEID;
var ATTRTYPETYPEID;

/**
 * The private functions creates all data model tables
 */
function _eFapsCreateDataModelTablesStep1()  {
  _eFapsPrint("Create Data Model Tables");
  var conRsrc = Context.getThreadContext().getConnectionResource();
  var stmt = conRsrc.getConnection().createStatement();

  /////////////////////////////////////////
  // insert 'attribute type'

  var text = "Insert Table for 'Admin_DataModel_AttributeType'";
  ATTRTYPESQLTABLEID = _eFapsCreateInsertSQLTable(stmt, text, "30152cda-e5a3-418d-ad1e-ad44be1307c2", "Admin_DataModel_AttributeTypeSQLTable", "T_DMATTRIBUTETYPE", "ID", null, null);

  var text = "Insert Type for Type for 'Admin_DataModel_AttributeType'";
  ATTRTYPETYPEID = _eFapsCreateInsertType(stmt, text, "c482e3d3-8387-4406-a1c2-b0e708af78f3", "Admin_DataModel_AttributeType", null);

  var text = "Insert Attribute Types";
  _eFapsCreateAttrType(stmt, text, 'acfb7dd8-71e9-43c0-9f22-8d98190f7290', 'Type',           'org.efaps.admin.datamodel.attributetype.TypeType',           'org.efaps.admin.datamodel.ui.TypeUI',           null, null);
  _eFapsCreateAttrType(stmt, null, '72221a59-df5d-4c56-9bec-c9167de80f2b', 'String',         'org.efaps.admin.datamodel.attributetype.StringType',         'org.efaps.admin.datamodel.ui.StringUI',         null, null);
  _eFapsCreateAttrType(stmt, null, '87a372f0-9e71-45ed-be32-f2a95480a7ee', 'Password',       'org.efaps.admin.datamodel.attributetype.PasswordType',       'org.efaps.admin.datamodel.ui.PasswordUI',       null, null);
  _eFapsCreateAttrType(stmt, null, 'bb1d4c0b-4fee-4607-94b9-7c742949c099', 'OID',            'org.efaps.admin.datamodel.attributetype.OIDType',            'org.efaps.admin.datamodel.ui.StringUI',         null, null);
  _eFapsCreateAttrType(stmt, null, 'b9d0e298-f96b-4b78-aa6c-ae8c71952f6c', 'Long',           'org.efaps.admin.datamodel.attributetype.LongType',           'org.efaps.admin.datamodel.ui.NumberUI',         null, null);
  _eFapsCreateAttrType(stmt, null, '41451b64-cb24-4e77-8d9e-5b6eb58df56f', 'Integer',        'org.efaps.admin.datamodel.attributetype.IntegerType',        'org.efaps.admin.datamodel.ui.NumberUI',         null, null);
  _eFapsCreateAttrType(stmt, null, 'd4a96228-1af9-448b-8f0b-7fe2790835af', 'Real',           'org.efaps.admin.datamodel.attributetype.RealType',           'org.efaps.admin.datamodel.ui.StringUI',         null, null);
  _eFapsCreateAttrType(stmt, null, '7fb3799d-4e31-45a3-8c5e-4fbf445ec3c1', 'Boolean',        'org.efaps.admin.datamodel.attributetype.BooleanType',        'org.efaps.admin.datamodel.ui.BooleanUI',        null, null);
  _eFapsCreateAttrType(stmt, null, '68ce3aa6-e3e8-40bb-b48f-2a67948c2e7e', 'Date',           'org.efaps.admin.datamodel.attributetype.DateTimeType',       'org.efaps.admin.datamodel.ui.DateUI',           null, null);
  _eFapsCreateAttrType(stmt, null, 'd8ddc848-115e-4abf-be66-0856ac64b21a', 'Time',           'org.efaps.admin.datamodel.attributetype.StringType',         'org.efaps.admin.datamodel.ui.StringUI',         null, null);
  _eFapsCreateAttrType(stmt, null, 'e764db0f-70f2-4cd4-b2fe-d23d3da72f78', 'DateTime',       'org.efaps.admin.datamodel.attributetype.DateTimeType',       'org.efaps.admin.datamodel.ui.DateTimeUI',       null, null);
  _eFapsCreateAttrType(stmt, null, '513d35f5-58e2-4243-acd2-5fec5359778a', 'Created',        'org.efaps.admin.datamodel.attributetype.CreatedType',        'org.efaps.admin.datamodel.ui.DateTimeUI',       null, 1   );
  _eFapsCreateAttrType(stmt, null, 'a8556408-a15d-4f4f-b740-6824f774dc1d', 'Modified',       'org.efaps.admin.datamodel.attributetype.ModifiedType',       'org.efaps.admin.datamodel.ui.DateTimeUI',       1,    null);
  _eFapsCreateAttrType(stmt, null, '440f472f-7be2-41d3-baec-4a2f0e4e5b31', 'Link',           'org.efaps.admin.datamodel.attributetype.LinkType',           'org.efaps.admin.datamodel.ui.StringUI',         null, null);
  _eFapsCreateAttrType(stmt, null, '9d6b2e3e-68ce-4509-a5f0-eae42323a696', 'LinkWithRanges', 'org.efaps.admin.datamodel.attributetype.LinkWithRanges',     'org.efaps.admin.datamodel.ui.LinkWithRangesUI', null, null);
  _eFapsCreateAttrType(stmt, null, '7b8f98de-1967-44e0-b174-027349868a61', 'PersonLink',     'org.efaps.admin.datamodel.attributetype.PersonLinkType',     'org.efaps.admin.datamodel.ui.UserUI',           null, null);
  _eFapsCreateAttrType(stmt, null, '76122fe9-8fde-4dd4-a229-e48af0fb4083', 'CreatorLink',    'org.efaps.admin.datamodel.attributetype.CreatorLinkType',    'org.efaps.admin.datamodel.ui.UserUI',           null, 1   );
  _eFapsCreateAttrType(stmt, null, '447a7c87-8395-48c4-b2ed-d4e96d46332c', 'ModifierLink',   'org.efaps.admin.datamodel.attributetype.ModifierLinkType',   'org.efaps.admin.datamodel.ui.UserUI',           1,    null);
  _eFapsCreateAttrType(stmt, null, 'a5367e5a-78b7-47b4-be7f-abf5423171f0', 'OwnerLink',      'org.efaps.admin.datamodel.attributetype.OwnerLinkType',      'org.efaps.admin.datamodel.ui.UserUI',           null, 1   );
  _eFapsCreateAttrType(stmt, null, 'c9c98b47-d5da-4665-939c-9686c82914ac', 'PolicyLink',     'org.efaps.admin.datamodel.attributetype.StringType',         'org.efaps.admin.datamodel.ui.StringUI',         null, null);
  _eFapsCreateAttrType(stmt, null, 'adb13c3d-9506-4da2-8d75-b54c76779c6c', 'MultiLineArray', 'org.efaps.admin.datamodel.attributetype.MultiLineArrayType', 'org.efaps.admin.datamodel.ui.StringUI',         null, null);
  _eFapsCreateAttrType(stmt, null, '358d1f0e-43ae-425d-a4a0-8d5bad6f40d7', 'Decimal',        'org.efaps.admin.datamodel.attributetype.DecimalType',        'org.efaps.admin.datamodel.ui.DecimalUI',        null, null);
  _eFapsCreateAttrType(stmt, null, 'f1795d01-1567-4cb3-9620-b3cd4e2af932', 'IntegerWithUoM', 'org.efaps.admin.datamodel.attributetype.IntegerWithUoMType', 'org.efaps.admin.datamodel.ui.NumberWithUoMUI',  null, null);
  _eFapsCreateAttrType(stmt, null, 'de1c00bc-b041-49d6-9d85-82de0a6bee0d', 'DecimalWithUoM', 'org.efaps.admin.datamodel.attributetype.DecimalWithUoMType', 'org.efaps.admin.datamodel.ui.DecimalWithUoMUI',  null, null);
  _eFapsCreateAttrType(stmt, null, '0161bcdb-45e9-4839-a709-3a1c56f8a76a', 'Status',         'org.efaps.admin.datamodel.attributetype.StatusType',         'org.efaps.admin.datamodel.ui.LinkWithRangesUI', null, null);
  _eFapsCreateAttrType(stmt, null, '66c5d239-47d7-4fef-a79b-9dac432ab7ba', 'CompanyLink',    'org.efaps.admin.datamodel.attributetype.CompanyLinkType',    'org.efaps.admin.datamodel.ui.UserUI',           null, 1   );
  _eFapsCreateAttrType(stmt, null, '76651147-1108-492e-815f-44bb68856962', 'FormatedString', 'org.efaps.admin.datamodel.attributetype.FormatedStringType', 'org.efaps.admin.datamodel.ui.FormatedStringUI', null, null);



  _eFapsCreateInsertAttr(stmt, ATTRTYPESQLTABLEID, ATTRTYPETYPEID, 'Classname',     'CLASSNAME',    'String', null);
  _eFapsCreateInsertAttr(stmt, ATTRTYPESQLTABLEID, ATTRTYPETYPEID, 'ClassnameUI',   'CLASSNAMEUI',  'String', null);
  _eFapsCreateInsertAttr(stmt, ATTRTYPESQLTABLEID, ATTRTYPETYPEID, 'AlwaysUpdate',  'ALWAYSUPDATE', 'Boolean', null);
  _eFapsCreateInsertAttr(stmt, ATTRTYPESQLTABLEID, ATTRTYPETYPEID, 'CreateUpdate',  'CREATEUPDATE', 'Boolean', null);

  conRsrc.commit();
}

/**
 * The private functions creates all data model tables
 */
function _eFapsCreateDataModelTablesStep2()  {
  /////////////////////////////////////////
  // insert 'sql table'
  var conRsrc = Context.getThreadContext().getConnectionResource();
  var stmt = conRsrc.getConnection().createStatement();

  text = "Insert Table for 'Admin_DataModel_SQLTable'";
  var sqlTableIdSQLTable = _eFapsCreateInsertSQLTable(stmt, text, "5ffb40ef-3518-46c8-a78f-da3ffbfea4c0", "Admin_DataModel_SQLTableSQLTable", "T_DMTABLE", "ID", null, "Admin_Common_AbstractSQLTable");

  text = "Insert Type for 'Admin_DataModel_SQLTable'";
  var typeIdSQLTable = _eFapsCreateInsertType(stmt, text, "ebf29cc2-cf42-4cd0-9b6e-92d9b644062b", "Admin_DataModel_SQLTable", "Admin_Abstract");
  _eFapsCreateInsertAttr(stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLTable',         'SQLTABLE',         'String', null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLColumnID',      'SQLCOLUMNID',      'String', null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdSQLTable, typeIdSQLTable, 'SQLColumnType',    'SQLCOLUMNTYPE',    'String', null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdSQLTable, typeIdSQLTable, 'DMTableMain',      'DMTABLEMAIN',      'Link', "Admin_DataModel_SQLTable");

  _exec(stmt, "Update type id for sql tables",
              null, "update T_CMABSTRACT set TYPEID=" + typeIdSQLTable + " where TYPEID=-20000");

  /////////////////////////////////////////
  // insert 'type'

  text = "Insert Table for 'Admin_DataModel_Type'";
  var sqlTableIdType = _eFapsCreateInsertSQLTable(stmt, text, "8f4df2db-8fda-4f00-9144-9a3e344d0abc", "Admin_DataModel_TypeSQLTable", "T_DMTYPE", "ID", null, "Admin_Common_AbstractSQLTable");

  text = "Insert Type for 'Admin_DataModel_Type'";
  var typeIdType = _eFapsCreateInsertType(stmt, text, "8770839d-60fd-4bb4-81fd-3903d4c916ec", "Admin_DataModel_Type", "Admin_Abstract");
  _eFapsCreateInsertAttr(stmt, sqlTableIdType, typeIdType, 'SQLCacheExpr',     'SQLCACHEEXPR',     'String', null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdType, typeIdType, 'ParentType',       'PARENTDMTYPE',     'Link', "Admin_DataModel_Type");

  _exec(stmt, "Update type id for types",
              null, "update T_CMABSTRACT set TYPEID=" + typeIdType + " where TYPEID=-21000");

  /////////////////////////////////////////
  // insert 'attribute'

  text = "Insert Table for 'Admin_DataModel_Attribute'";
  var sqlTableIdAttr = _eFapsCreateInsertSQLTable(stmt, text, "d3a64746-3666-4678-9603-f304bf16bb92", "Admin_DataModel_AttributeSQLTable", "T_DMATTRIBUTE", "ID", null, "Admin_Common_AbstractSQLTable");

  text = "Insert Type for 'Admin_DataModel_Attribute'";
  var typeIdAttr = _eFapsCreateInsertType(stmt, text, "518a9802-cf0e-4359-9b3c-880f71e1387f", "Admin_DataModel_Attribute", "Admin_Abstract");
  _eFapsCreateInsertAttr(stmt, sqlTableIdAttr, typeIdAttr, 'Table',             'DMTABLE',         'Link', "Admin_DataModel_SQLTable");
  _eFapsCreateInsertAttr(stmt, sqlTableIdAttr, typeIdAttr, 'ParentType',        'DMTYPE',          'Link', "Admin_DataModel_Type");
  _eFapsCreateInsertAttr(stmt, sqlTableIdAttr, typeIdAttr, 'AttributeType',     'DMATTRIBUTETYPE', 'Link', "Admin_DataModel_AttributeType");
  _eFapsCreateInsertAttr(stmt, sqlTableIdAttr, typeIdAttr, 'TypeLink',          'DMTYPELINK',      'Link', "Admin_DataModel_Type");
  _eFapsCreateInsertAttr(stmt, sqlTableIdAttr, typeIdAttr, 'SQLColumn',         'SQLCOLUMN',       'String', null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdAttr, typeIdAttr, 'DefaultValue',      'DEFAULTVAL',      'String', null);

  _exec(stmt, "Update type id for attributes",
              null, "update T_CMABSTRACT set TYPEID=" + typeIdAttr + " where TYPEID=-22000");


  text = "Insert Type for 'Admin_DataModel_AttributeSet'";
  var typeIdAttrSet = _eFapsCreateInsertType(stmt, text, "a23b6c9f-5220-438f-93d0-f4651c3ba455", "Admin_DataModel_AttributeSet", "Admin_DataModel_Attribute");

  text = "Insert Type for 'Admin_DataModel_AttributeSetAttribute'";
  var typeIdAttrSetAttr = _eFapsCreateInsertType(stmt, text, "f601ffc5-819c-41a0-8663-3e1b0fb35a9b", "Admin_DataModel_AttributeSetAttribute", "Admin_DataModel_Attribute");

  /////////////////////////////////////////
  // insert 'admin property'

  text = "Insert Table for 'Admin_Common_Property'";
  var sqlTableIdProp = _eFapsCreateInsertSQLTable(stmt, text, "5cf99cd6-06d6-4322-a344-55d206666c9c", "Admin_Common_PropertySQLTable", "T_CMPROPERTY", "ID", null, null);

  text = "Insert Type for 'Admin_Common_Property'";
  var typeIdProp = _eFapsCreateInsertType(stmt, text, "f3d54a86-c323-43d8-9c78-284d61d955b3", "Admin_Common_Property", null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdProp, typeIdProp, 'OID',              'ID',               'OID',      null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdProp, typeIdProp, 'ID',               'ID',               'Integer',  null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdProp, typeIdProp, 'Name',             'NAME',             'String',   null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdProp, typeIdProp, 'Value',            'VALUE',            'String',   null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdProp, typeIdProp, 'Abstract',         'ABSTRACT',         'Link',     "Admin_Abstract");

  conRsrc.commit();
}

/**
 * The private functions creates all common tables
 */
function _eFapsCreateCommonTablesStep2()  {
  _eFapsPrint("Create Common Tables Step 2");
  var conRsrc = Context.getThreadContext().getConnectionResource();
  var stmt = conRsrc.getConnection().createStatement();

  text = "Insert Table for 'Admin_Abstract'";
  var sqlTableIdAbstract = _eFapsCreateInsertSQLTable(stmt, text, "e76ff99d-0d3d-4154-b2ef-d65633d357c3", "Admin_Common_AbstractSQLTable", "T_CMABSTRACT", "ID", "TYPEID", null);

  text = "Insert Type for 'Admin_Abstract'";
  var typeIdAbstract = _eFapsCreateInsertType(stmt, text, "2a869f46-0ec7-4afb-98e7-8b1125e1c43c", "Admin_Abstract",        null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Type',             'TYPEID',           'Type',         null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'OID',              'TYPEID,ID',        'OID',          null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'ID',               'ID',               'Integer',      null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Creator',          'CREATOR',          'CreatorLink',  null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Created',          'CREATED',          'Created',      null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Modifier',         'MODIFIER',         'ModifierLink', null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Modified',         'MODIFIED',         'Modified',     null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Name',             'NAME',             'String',       null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'UUID',             'UUID',             'String',       null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Revision',         'REVISION',         'String',       null);
  _eFapsCreateInsertAttr(stmt, sqlTableIdAbstract, typeIdAbstract, 'Purpose',          'PURPOSE',          'Integer',      null);

  /////////////////////////////////////////
  // update attribute types

  _exec(stmt, "Udpate SQL Table for 'Admin_DataModel_AtributeTypeSQLTable'",
              null, "update T_DMTABLE set DMTABLEMAIN=" + sqlTableIdAbstract + " where ID=" + ATTRTYPESQLTABLEID);
  _exec(stmt, "Udpate Type for 'Admin_DataModel_AtributeType'",
              null, "update T_DMTYPE set PARENTDMTYPE=" + typeIdAbstract + " where ID=" + ATTRTYPETYPEID);

  conRsrc.commit();
}

function _eFapsInitRunLevel()  {
  var conRsrc = Context.getThreadContext().getConnectionResource();
  var stmt = conRsrc.getConnection().createStatement();

  var id = _insert(stmt, "Insert shell Runlevel", null,
          "T_RUNLEVEL",
          "RUNLEVEL,UUID",
          "'shell','edfb9537-9d91-4fa0-acb1-cf3f2678a245'");

  _insert(stmt, null, null,
          "T_RUNLEVELDEF",
          "RUNLEVELID,PRIORITY,CLASS,METHOD",
          "" + id + ",1, 'org.efaps.admin.common.SystemConfiguration', 'initialize'");

  _insert(stmt, null, null,
          "T_RUNLEVELDEF",
          "RUNLEVELID,PRIORITY,CLASS,METHOD",
          "" + id + ",2, 'org.efaps.admin.dbproperty.DBProperties', 'initialize'");
  _insert(stmt, null, null,
          "T_RUNLEVELDEF",
          "RUNLEVELID,PRIORITY,CLASS,METHOD",
          "" + id + ",3, 'org.efaps.admin.user.JAASSystem', 'initialize'");
  _insert(stmt, null, null,
          "T_RUNLEVELDEF",
          "RUNLEVELID,PRIORITY,CLASS,METHOD",
          "" + id + ",4, 'org.efaps.admin.user.Role', 'initialize'");
  _insert(stmt, null, null,
          "T_RUNLEVELDEF",
          "RUNLEVELID,PRIORITY,CLASS,METHOD",
          "" + id + ",5, 'org.efaps.admin.user.Group', 'initialize'");
  _insert(stmt, null, null,
          "T_RUNLEVELDEF",
          "RUNLEVELID,PRIORITY,CLASS,METHOD",
          "" + id + ",6, 'org.efaps.admin.user.Company', 'initialize'");
  _insert(stmt, null, null,
          "T_RUNLEVELDEF",
          "RUNLEVELID,PRIORITY,CLASS,METHOD",
          "" + id + ",7, 'org.efaps.admin.datamodel.AbstractDataModelObject', 'initialize'");
  _insert(stmt, null, null,
          "T_RUNLEVELDEF",
          "RUNLEVELID,PRIORITY,CLASS,METHOD",
          "" + id + ",8, 'org.efaps.db.store.Store', 'initialize'");

  conRsrc.commit();
}
