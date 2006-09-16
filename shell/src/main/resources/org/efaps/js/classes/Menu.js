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

importClass(Packages.org.efaps.db.Instance);
importClass(Packages.org.efaps.db.SearchQuery);

function Menu(_name)  {
  this.object = new EFapsInstance("Admin_UI_Menu", _name);
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
  this.addIcon            = Command.prototype.addIcon;
  this.addTargetForm      = Command.prototype.addTargetForm;
  this.addTargetMenu      = Command.prototype.addTargetMenu;
  this.addTargetSearch    = Command.prototype.addTargetSearch;
  this.addTargetTable     = Command.prototype.addTargetTable;

  // add common methods
  this._writeHeader       = Abstract.prototype._writeHeader;
}

///////////////////////////////////////////////////////////////////////////////
// instance variables

/**
 * Prefix of script file names.
 */
Menu.prototype.FILE_PREFIX = new String("MENU_");

/**
 * Name of the variable used in update scripts.
 */
Menu.prototype.VARNAME     = new String("MENU");

///////////////////////////////////////////////////////////////////////////////
// child methods

Menu.prototype.cleanupChilds = function()  {
  var query = new SearchQuery();
  query.setExpand(Shell.getContext(), this.getOid(), "Admin_UI_Menu2Command\\FromMenu");
  query.addSelect(Shell.getContext(), "OID");
  query.executeWithoutAccessCheck();
  while (query.next())  {
    var oid = query.get(Shell.getContext(), "OID");
    var del = new Delete(Shell.getContext(), oid);
    del.execute(Shell.getContext());
  }
  query.close();
}

Menu.prototype.printChilds = function() {
/*  var childs = this.object.Admin_UI_Menu2Command\FromMenu;
  if (childs!=null)  {
    var i = 0;
    while (i < childs.length) {
      print(childs[i].ToCommand.Name);
      i=i+1;
    }
  }
*/
}

/**
 * @TODO what happens if object not exists?
 */
Menu.prototype.addChild = function(_object)  {
  if (typeof _object == "object")  {
    if (_object instanceof Command || _object instanceof Menu)  {
if (_object.getId()!=null && _object.getId()!="0")  {
      var insert = new Insert(Shell.getContext(), "Admin_UI_Menu2Command");
      insert.add(Shell.getContext(), "FromMenu", this.getId());
      insert.add(Shell.getContext(), "ToCommand", _object.getId());
      insert.executeWithoutAccessCheck();
} else  {
      print("!!!!!!!!!!!!!!!!!!! object  '"+_object+"' not found!");
}
    } else  {
      print("!!!!!!!!!!!!!!!!!!! unknown object  '"+_object+"'!");
    }
  }
}

/**
 * Writes the JS update scripts for the childs.
 *
 * @param _file (Writer)  open file to write through
 */
Menu.prototype._writeChilds = function(_file)  {
  var query = new SearchQuery();
  query.setExpand(Shell.getContext(), this.getOid(), "Admin_UI_Menu2Command\\FromMenu.ToCommand");
  query.addSelect(Shell.getContext(), "Type");
  query.addSelect(Shell.getContext(), "Name");
  query.executeWithoutAccessCheck();
  while (query.next())  {
    var type = query.get(Shell.getContext(), "Type").getName();
    var name = query.get(Shell.getContext(), "Name");
    if (type=="Admin_UI_Command")  {
      _file.println("  addChild(new Command(\""+name+"\"));");
    } else  {
      _file.println("  addChild(new Menu(\""+name+"\"));");
    }
  }
  query.close();
}

///////////////////////////////////////////////////////////////////////////////
// common methods

Menu.prototype.cleanup = function()  {
  this.cleanupProperties();
  this.cleanupLinks();
  this.cleanupChilds();
}

Menu.prototype._create = function(_name)  {
  var insert = new Insert(Shell.context, "Admin_UI_Menu");
  insert.add(Shell.context, "Name", _name);
  insert.executeWithoutAccessCheck();
  this.instance = insert.getInstance();
}

Menu.prototype.update = function(_fileName, _objName)  {
  if (this.getOid()==null || this.getOid()=="" || this.getOid()=="0")  {
    print("  - create");
    this._create(_objName);
  } else  {
    print("  - cleanup");
    this.cleanup();
  }
  MENU = this;
  print("  - import");
  load(_fileName);
  MENU = null;
}

/**
 * Creates the update script for current menu.
 *
 * @param _path   (String)  file is created in this path
 * @param _author (String)  author used in the header
 */
Menu.prototype.writeUpdateScript = function(_path, _author)  {
  var file = new PrintWriter(new FileWriter(_path+"/"+this.FILE_PREFIX+this.getName()+".js"));

  this._writeHeader(file, _author);

  file.println("with ("+this.VARNAME+")  {");
  this._writeProperties(file);
  this._writeLinks(file);
  this._writeChilds(file);
  file.println("}");

  file.close();
}

///////////////////////////////////////////////////////////////////////////////
// static functions

/**
 * Extract the menu name from a file name.
 *
 * @param (String)_fileName   file name
 * @return extracted menu name
 */
function getMenuNameFromFileName(_fileName)  {
  var tmp = new String(_fileName);
  return tmp.substring(Menu.prototype.FILE_PREFIX.length, tmp.length-3);
}

/**
 * Import exact one menu.
 *
 * @param (File)_fileName   file name of the menu to import
 */
function importMenu(_fileName)  {
  var fileName = new File(_fileName);
  if (fileName.getName().startsWith(Menu.prototype.FILE_PREFIX) && fileName.getName().endsWith(".js"))  {
    var objName = getMenuNameFromFileName(fileName.getName());
    print("Import Menu '"+objName+"'");
    var impMenu = new Menu(objName);
    impMenu.update(_fileName, objName);
  }
}

/**
 * All menus in the list are created physically in the database (only the 
 * menu, no other information like properties, links, ...). This is needed, 
 * because the menus can  be referenced by other menus, commands, searches 
 * etc.
 *
 * @param _fileList array with list of files
 */
function createMenus(_fileList)  {
  print("");
  print("Create Menus");
  print("~~~~~~~~~~~~");
  for (indx in _fileList)  {
    var fileName = new File(_fileList[indx]);
    if (fileName.getName().startsWith(Menu.prototype.FILE_PREFIX) && fileName.getName().endsWith(".js"))  {
      var objName = getMenuNameFromFileName(fileName.getName());
      var menu = new Menu(objName);
      if (menu.getOid()==null || menu.getOid()=="" || menu.getOid()=="0")  {
        print("Create Menu '"+objName+"'");
        menu._create(objName);
      }
    }
  }
}

/**
 * A list of Menu Definition files is imported. The algorithmen checks first
 * for existance of a menu and creates the not existant menus.
 *
 * @param _fileList array with list of files
 */
function importMenus(_fileList)  {
  print("");
  print("Import Menus");
  print("~~~~~~~~~~~~");
  for (indx in _fileList)  {
    importMenu(_fileList[indx]);
  }
  print("");
}

/*
  var codeRev = getCodeRevision(path+"/"+fileName);
  var dbRev = MENU.object.Revision;

  if (codeRev==null || codeRev.length==0 || codeRev!=dbRev)  {
    if (codeRev!=null)  {
      MENU.object.Revision = codeRev;
      MENU.object.update();
    }
    print("  - update to revision '"+codeRev+"'");
*/

/**
 * @param _path   (String)  files are created in this path
 * @param _match  (String)  match for menu names
 * @param _author (String)  author used in the header
 */
function createScriptMenus(_path, _match, _author)  {
  var query = new Packages.org.efaps.db.SearchQuery();
  query.setQueryTypes(Shell.getContext(), "Admin_UI_Menu");
  query.addWhereExprEqValue(Shell.getContext(), "Name", _match);
  query.addSelect(Shell.getContext(), "Name");
  
  query.executeWithoutAccessCheck();
  
  print("");
  print("Create Menu Scripts:");
  print("~~~~~~~~~~~~~~~~~~~~");
  while (query.next())  {
    var name = query.get(Shell.getContext(), "Name");
    print("  - '"+  name  +"'");
    var menu = new Menu(name);
    menu.writeUpdateScript(_path, _author);
  }

  query.close();
}
