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

function Search(_name)  {
  this.object = new EFapsInstance("Admin_UI_Search", _name);
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

  // add child methods
  this._writeChilds       = Menu.prototype._writeChilds;
  this.cleanupChilds      = Menu.prototype.cleanupChilds;
  this.printChilds        = Menu.prototype.printChilds;
  this.addChild           = Menu.prototype.addChild;

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
  this.cleanup            = Menu.prototype.cleanup;
  this.writeUpdateScript  = Menu.prototype.writeUpdateScript;
}

///////////////////////////////////////////////////////////////////////////////
// instance variables

/**
 * Prefix of script file names.
 */
Search.prototype.FILE_PREFIX = new String("SEARCH_");

/**
 * Name of the variable used in update scripts.
 */
Search.prototype.VARNAME     = new String("SEARCH");

///////////////////////////////////////////////////////////////////////////////
// common methods

Search.prototype.update = function(_fileName, _objName)  {
  if (this.getOid()==null || this.getOid()=="" || this.getOid()=="0")  {
    print("  - create");
    this.object.Name = _objName;
    this.object.create();
    this.instance = new Instance(Shell.getContext(), this.object.oid);
  } else  {
    print("  - cleanup");
    this.cleanup();
  }
  SEARCH = this;
  print("  - import");
  load(_fileName);
  SEARCH = null;
}

///////////////////////////////////////////////////////////////////////////////
// static functions

/**
 * Extract the search name from a file name.
 *
 * @param (String)_fileName   file name
 * @return extracted search name
 */
function getSearchNameFromFileName(_fileName)  {
  var tmp = new String(_fileName);
  return tmp.substring(Search.prototype.FILE_PREFIX.length, tmp.length-3);
}

/**
 * Import exact one search.
 *
 * @param (File)_fileName   file name of the search to import
 */
function importSearch(_fileName)  {
  var fileName = new File(_fileName);
  if (fileName.getName().startsWith(Search.prototype.FILE_PREFIX) && fileName.getName().endsWith(".js"))  {
    var objName = getSearchNameFromFileName(fileName.getName());
    print("Import Search '"+objName+"'");
    var imp = new Search(objName);
    imp.update(_fileName, objName);
  }
}

/**
 * All menus of the filelist are created. This is needed, because the menus can 
 * be referenced by other menus, commands, searches etc.
 *
 * @param _fileList array with list of files
 */
function createSearches(_fileList)  {
  print("");
  print("Create Searches");
  print("~~~~~~~~~~~~~~~");
  for (indx in _fileList)  {
    var fileName = new File(_fileList[indx]);
    if (fileName.getName().startsWith(Search.prototype.FILE_PREFIX) && fileName.getName().endsWith(".js"))  {
      var objName = getSearchNameFromFileName(fileName.getName());
      var obj = new EFapsInstance("Admin_UI_Search", objName);
      if (obj.oid==null || obj.oid=="" || obj.oid=="0")  {
        print("Create Menu '"+objName+"'");
        obj.Name = objName;
        obj.create();
      }
    }
  }
}

/**
 * A list of Search Definition files is imported. The algorithmen checks first
 * for existance of a search and creates the not existant searches.
 *
 * @param _fileList array with list of files
 */
function importSearches(_fileList)  {
  print("");
  print("Import Searches");
  print("~~~~~~~~~~~~~~~");
  for (indx in _fileList)  {
    importSearch(_fileList[indx]);
  }
  print("");
}

/*
  var codeRev = getCodeRevision(path+"/"+fileName);
  var dbRev = SEARCH.getRevision();

  if (codeRev==null || codeRev.length==0 || codeRev!=dbRev)  {
    if (codeRev!=null)  {
      SEARCH.setRevision(codeRev);
    }
*/

/**
 * Create javascript update script files for all matching searches in given 
 * path for given author.
 *
 * @param _path   (String)  files are created in this path
 * @param _match  (String)  match for menu names
 * @param _author (String)  author used in the header
 */
function createScriptSearches(_path, _match, _author)  {
  var query = new SearchQuery();
  query.setQueryTypes(Shell.getContext(), "Admin_UI_Search");
  query.addWhereExprEqValue(Shell.getContext(), "Name", _match);
  query.addSelect(Shell.getContext(), "Name");
  
  query.execute(Shell.getContext());
  
  print("");
  print("Create Search Scripts:");
  print("~~~~~~~~~~~~~~~~~~~~~~");
  while (query.next())  {
    var name = query.get(Shell.getContext(), "Name");
    print("  - '"+  name  +"'");
    var search = new Search(name);
    search.writeUpdateScript(_path, _author);
  }

  query.close();
}
