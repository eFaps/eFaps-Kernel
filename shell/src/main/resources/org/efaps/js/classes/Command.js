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

importClass(java.io.FileWriter);
importClass(java.io.PrintWriter);

importClass(Packages.org.efaps.db.Instance);

function Command(_name)  {
  this.object = new EFapsInstance("Admin_UI_Command", _name);
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

  // add link methods
  this._createLink        = UIAbstract.prototype._createLink;
  this._writeLinks        = UIAbstract.prototype._writeLinks;
  this.cleanupLinks       = UIAbstract.prototype.cleanupLinks;

  // add access methods
  this._writeAccess       = UIAbstract.prototype._writeAccess;
  this.addRole            = UIAbstract.prototype.addRole;
  this.cleanupAccess      = UIAbstract.prototype.cleanupAccess;

  // add common methods
  this._writeHeader       = Abstract.prototype._writeHeader;
}

///////////////////////////////////////////////////////////////////////////////
// link methods

/**
 * Add a target menu to this command instance.
 *
 * @param _object (String) image name to add as icon
 */
Command.prototype.addIcon = function(_object)  {
  this._createLink("Admin_UI_LinkIcon", "Admin_UI_Image", _object);
}

/**
 * Add a target form to this command instance.
 *
 * @param _object (String) form name to add as target form
 */
Command.prototype.addTargetForm = function(_object)  {
  this._createLink("Admin_UI_LinkTargetForm", "Admin_UI_Form", _object);
}

/**
 * Add a target menu to this command instance.
 *
 * @param _object (String) menu name to add as target menu
 */
Command.prototype.addTargetMenu = function(_object)  {
  this._createLink("Admin_UI_LinkTargetMenu", "Admin_UI_Menu", _object);
}

/**
 * Add a target search to this command instance.
 *
 * @param _object (String) search name to add as target search
 */
Command.prototype.addTargetSearch = function(_object)  {
  this._createLink("Admin_UI_LinkTargetSearch", "Admin_UI_Search", _object);
}

/**
 * Add a target table to this command instance.
 *
 * @param _object (String) table name to add as target table
 */
Command.prototype.addTargetTable = function(_object)  {
  this._createLink("Admin_UI_LinkTargetTable", "Admin_UI_Table", _object);
}

///////////////////////////////////////////////////////////////////////////////
// common methods

Command.prototype.cleanup = function()  {
  this.cleanupProperties();
  this.cleanupLinks();
  this.cleanupAccess();
}

Command.prototype._create = function(_name)  {
  var insert = new Insert(Shell.context, "Admin_UI_Command");
  insert.add(Shell.context, "Name", _name);
  insert.executeWithoutAccessCheck();
  this.instance = insert.getInstance();
}

Command.prototype.update = function(_fileName, _objName)  {
  if (this.getOid()==null || this.getOid()=="" || this.getOid()=="0")  {
    print("  - create");
    this._create(_objName);
  } else  {
    print("  - cleanup");
    this.cleanup();
  }
  COMMAND = this;
  print("  - import");
  load(_fileName);
  COMMAND = null;
}

/**
 * Creates the update script for current command.
 *
 * @param _path   (String)  file is created in this path
 * @param _author (String)  author used in the header
 */
Command.prototype.writeUpdateScript = function(_path, _author)  {
  var file = new PrintWriter(new FileWriter(_path+"/"+PREFIX_COMMAND+this.getName()+".js"));

  this._writeHeader(file, _author);

  file.println("with (COMMAND)  {");
  this._writeProperties(file);
  this._writeLinks(file);
  file.println("}");

  file.close();
}

///////////////////////////////////////////////////////////////////////////////
// static functions

var PREFIX_COMMAND = new String("COMMAND_");

/**
 * Extract the command name from a file name.
 *
 * @param (String)_fileName   file name
 * @return extracted command name
 */
function getCommandNameFromFileName(_fileName)  {
  var tmp = new String(_fileName);
  return tmp.substring(PREFIX_COMMAND.length, tmp.length-3);
}

/**
 * One command file with the given name is imported.
 *
 * @param _fileName   file to import
 */
function importCommand(_fileName)  {
  var fileName = new File(_fileName);
  if (fileName.getName().startsWith(PREFIX_COMMAND) && fileName.getName().endsWith(".js"))  {
    var objName = getCommandNameFromFileName(fileName.getName());
    print("Import Command '"+objName+"'");
    var imp = new Command(objName);
    imp.update(_fileName, objName);
  }
}

/**
 * A list of Command Definition files is imported.
 *
 * @param _fileList array with list of files
 */
function importCommands(_fileList)  {
  print("");
  print("Import Commands");
  print("~~~~~~~~~~~~~~~");
  for (indx in _fileList)  {
    importCommand(_fileList[indx]);
  }
  print("");
}

/*
var COMMAND = new Command(objName);
if (COMMAND.object.oid==null)  {
  print("  - create");
  var instance = new EFapsInstance("Admin_UI_Command");
  instance.Name = objName;
  instance.Revision = "0";
  instance.create();
  COMMAND = new Command(objName);
}

var codeRev = getCodeRevision(path+"/"+fileName);
var dbRev = COMMAND.object.Revision;
if (codeRev==null || codeRev.length==0 || codeRev!=dbRev)  {



  if (codeRev!=null)  {
    COMMAND.object.Revision = codeRev;
    COMMAND.object.update();
  }
  print("  - update to revision '"+codeRev+"'");

}
}
*/

/**
 * @param _path   (String)  files are created in this path
 * @param _match  (String)  match for menu names
 * @param _author (String)  author used in the header
 */
function createScriptCommands(_path, _match, _author)  {
  var query = new Packages.org.efaps.db.SearchQuery();
  query.setQueryTypes(Shell.getContext(), "Admin_UI_Command");
  query.addWhereExprEqValue(Shell.getContext(), "Name", _match);
  query.addSelect(Shell.getContext(), "Name");

  query.executeWithoutAccessCheck();
  
  print("Create Command Scripts:");
  print("~~~~~~~~~~~~~~~~~~~~~~~");
  while (query.next())  {
    var name = query.get(Shell.getContext(), "Name");
    print("  - '"+  name  +"'");
    var menu = new Command(name);
    menu.writeUpdateScript(_path, _author);
  }

  query.close();
}
