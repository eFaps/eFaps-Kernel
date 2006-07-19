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

importClass(Packages.java.io.File);
importClass(Packages.java.io.FileWriter);

importClass(Packages.org.efaps.db.Delete);
importClass(Packages.org.efaps.db.Insert);
importClass(Packages.org.efaps.db.Instance);
importClass(Packages.org.efaps.db.SearchQuery);

/**
 * Implements the Type represenation in JavaScript.
 */

var TYPE = null;

function Type(_name)  {
  this.object = new EFapsInstance("Admin_DataModel_Type", _name);
  this.instance = new Instance(Shell.getContext(), this.object.oid);

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
Type.prototype.FILE_PREFIX = new String("TYPE_");

/**
 * Name of the variable used in update scripts.
 */
Type.prototype.VARNAME     = new String("TYPE");

///////////////////////////////////////////////////////////////////////////////
// trigger methods

/**
 * A new trigger is created and connected to this type.
 * 
 * @param _type     trigger type to add
 * @param _indexPo  index position
 * @param _subject  name of the trigger to create and connect
 * @return 
 */
Type.prototype.addTrigger = function(_type, _indexPos, _subject)  {
  var insert = new Insert(Shell.getContext(), _type);
  insert.add(Shell.getContext(), "Name", _subject);
  insert.add(Shell.getContext(), "IndexPosition", _indexPos);
  insert.add(Shell.getContext(), "Abstract", this.getId());
  insert.execute(Shell.getContext());

  return new Trigger(insert.getInstance());
}

/**
 * All triggers connected to this type are removed.
 */
Type.prototype.cleanupTriggers = function()  {
  var query = new SearchQuery();
  query.setExpand(Shell.getContext(), this.getOid(), "Admin_Event_Definition\\Abstract");
  query.addSelect(Shell.getContext(), "OID");
  query.execute(Shell.getContext());
  while (query.next())  {
    var attrOid  = query.get(Shell.getContext(), "OID");
    (new Trigger(new Instance(Shell.getContext(), attrOid))).remove();
  }
  query.close();
}

///////////////////////////////////////////////////////////////////////////////
// attribute methods

/**
 * First, it is checked if an attribute with this name already exists on the 
 * type. If yes, this attribute is returned. Otherwise a new attribute is 
 * created and connected to this type. The new attribute gets a few default 
 * values:
 * <ul>
 *   <li>the attribute type is always <b>String</b>.
 *   <li>the SQL Table is <b>Admin_DataModel_AbstractTable</b>
 *   <li>the SQL column is the name of the attribute plus the prefix <b>-</b>
 * </ul>
 *
 * @param _name name of the attribute to create and connect
 * @return new created attribute javascript representation
 */
Type.prototype.addAttribute = function(_name)  {
  var attr;

  var query = new SearchQuery();
  query.setQueryTypes(Shell.getContext(), "Admin_DataModel_Attribute");
  query.addWhereExprEqValue(Shell.getContext(), "Name", _name);
  query.addWhereExprEqValue(Shell.getContext(), "ParentType", this.getId());
  query.addSelect(Shell.getContext(), "OID");
  query.execute(Shell.getContext());
  if (query.next())  {
    var attrOid = query.get(Shell.getContext(), "OID");
    attr = new Attribute(new Instance(Shell.getContext(), attrOid));
  } else  {
    var insert = new Insert(Shell.getContext(), "Admin_DataModel_Attribute");
    insert.add(Shell.getContext(), "ParentType", this.getId());
    insert.add(Shell.getContext(), "Name", _name);
    insert.add(Shell.getContext(), "AttributeType", (new DMAttributeType("Type")).getId());
    insert.add(Shell.getContext(), "Table", (new SQLTable("Admin_AbstractSQLTable")).getId());
    insert.add(Shell.getContext(), "SQLColumn", "-"+_name);
    insert.execute(Shell.getContext());

    attr = new Attribute(insert.getInstance());
  }
  return attr;
}

Type.prototype.printAttributes = function() {
/*
  var attributes = this.object.Admin_DataModel_Attribute\ParentType;
  if (attributes!=null)  {
    var i = 0;
    while (i < attributes.length) {
      print(attributes[i].Name);
      i=i+1;
    }
  }
*/
}

/**
 * All defined attributes of this types are cleaned up in the database (not 
 * deleted, only all properties are removed!). A remove of the attribute is not 
 * always allowed, because it could be that the attribute is already referenced
 * (e.g. in a history entry).
 */
Type.prototype.cleanupAttributes = function()  {
  var query = new SearchQuery();
  query.setExpand(Shell.getContext(), this.getOid(), "Admin_DataModel_Attribute\\ParentType");
  query.addSelect(Shell.getContext(), "OID");
  query.execute(Shell.getContext());
  while (query.next())  {
    var attrOid  = query.get(Shell.getContext(), "OID");
    (new Attribute(new Instance(Shell.getContext(), attrOid))).cleanup();
  }
  query.close();
}

/**
 * Writes the JS update scripts for the attributes.
 *
 * @param _file   (Writer)  open file to write through
 * @param _space  (String)  space to write in front of the attributes
 */
Type.prototype._writeAttributes = function(_file, _space)  {
/*
  var attrs = this.object.Admin_DataModel_Attribute\ParentType;
  if (attrs!=null)  {
    var i = 0;
    while (i < attrs.length) {
      var name  = attrs[i].Name;
      _file.println(_space + "  with (addAttribute(\""+name+"\"))  {");
      (new Attribute(attrs[i]))._writeUpdateScript(_file, _space + "  ");
//  this._writeProperties(file);
      _file.println(_space + "  }");
      i++;
    }
  }
*/
}

///////////////////////////////////////////////////////////////////////////////
// common methods

Type.prototype.cleanup = function()  {
  this.cleanupAttributes();
  this.cleanupProperties();
  this.cleanupTriggers();
  this.setParentType(null);
}

Type.prototype.update = function(_fileName, _objName)  {
  if (this.getOid()==null || this.getOid()=="" || this.getOid()=="0")  {
    print("  - create");
    this.object.Name = _objName;
    this.object.create();
  } else  {
    print("  - cleanup");
    this.cleanup();
  }
  TYPE = this;
  print("  - import");
  load(_fileName);
  TYPE = null;
}

/**
 * Creates the update script for current type.
 *
 * @param _path   (String)  file is created in this path
 * @param _author (String)  author used in the header
 */
Type.prototype.writeUpdateScript = function(_path, _author)  {
  var file = new PrintWriter(new FileWriter(_path+"/"+this.FILE_PREFIX+this.getName()+".js"));

  this._writeHeader(file, _author);

  file.println("with ("+this.VARNAME+")  {");

  var query = new SearchQuery();
  query.setObject(Shell.getContext(), this.getOid());
  query.addSelect(Shell.getContext(), "OID");
  query.addSelect(Shell.getContext(), "ParentType");
  query.addSelect(Shell.getContext(), "ParentType.Name");
  query.execute(Shell.getContext());
  if (query.next())  {
    var parType   = query.get(Shell.getContext(), "ParentType.Name").value;
    
    if (parType.length()>0)  {
      file.println("  setParentType(new Type(\""+parType+"\"));");
    }
  }
  query.close();

  this._writeAttributes(file, "");
  this._writeProperties(file);
  file.println("}");

  file.close();
}

///////////////////////////////////////////////////////////////////////////////
// getter and setter methods

/**
 * The parent type of this type is set.
 *
 * @param _parentType   parent type to set (name string or instance of class 
 *                      Type)
 */
Type.prototype.setParentType = function(_parentType)  {
  if (_parentType!=null)  {
    var parentType;
    if (typeof(_parentType)=="string")  {
      parentType = new Type(_parentType);
    } else  {
      parentType = _parentType;
    }
    this._setAttrValue("ParentType", parentType.getId());
  } else  {
    this._setAttrValue("ParentType", null);
  }
}

///////////////////////////////////////////////////////////////////////////////
// static functions

/**
 *
 */
function importType(_fileName)  {
  var fileName = new File(_fileName);
  if (fileName.getName().startsWith(Type.prototype.FILE_PREFIX) && fileName.getName().endsWith(".js"))  {
    var objName = new String(fileName.getName().substring(5, fileName.getName().length()-3));
    print("Import Type '"+objName+"'");
    var impType = new Type(objName);
    impType.update(_fileName, objName);
  }
}

/**
 * A list of Type Definition files is imported. The algorithmen checks first
 * for existance of a type and creates the not existant types.
 *
 * @param _fileList array with list of files
 */
function importTypes(_fileList)  {
  print("");
  print("Import Types");
  print("~~~~~~~~~~~~");
  for (indx in _fileList)  {
    var fileName = new File(_fileList[indx]);
    if (fileName.getName().startsWith(Type.prototype.FILE_PREFIX) && fileName.getName().endsWith(".js"))  {
      var objName = new String(fileName.getName().substring(5, fileName.getName().length()-3));
      var obj = new EFapsInstance("Admin_DataModel_Type", objName);
      if (obj.oid==null || obj.oid=="" || obj.oid=="0")  {
        print("Create Type '"+objName+"'");
        obj.Name = objName;
        obj.create();
      }
    }
  }
  for (indx in _fileList)  {
    importType(_fileList[indx]);
  }
  print("");
}

/**
 * @param _path   (String)  files are created in this path
 * @param _match  (String)  match for menu names
 * @param _author (String)  author used in the header
 */
function createScriptTypes(_path, _match, _author)  {
  var query = new SearchQuery();
  query.setQueryTypes(Shell.getContext(), "Admin_DataModel_Type");
  query.addWhereExprEqValue(Shell.getContext(), "Name", _match);
  query.addSelect(Shell.getContext(), "Name");
  
  query.execute(Shell.getContext());
  
  print("");
  print("Create Type Scripts:");
  print("~~~~~~~~~~~~~~~~~~~~");
  while (query.next())  {
    var value = query.get(Shell.getContext(), "Name")
    var name = value.value;
    print("  - '"+  name  +"'");
    var type = new Type(name);
    type.writeUpdateScript(_path, _author);
  }

  query.close();
}
