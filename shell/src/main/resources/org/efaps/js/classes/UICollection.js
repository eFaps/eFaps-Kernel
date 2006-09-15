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

importClass(Packages.java.io.FileWriter);
importClass(Packages.java.io.PrintWriter);

importClass(Packages.org.efaps.db.Insert);
importClass(Packages.org.efaps.db.SearchQuery);

/**
 * This is the Constructor for class UICollection.
 */
function UICollection()  {
  throw "abstract class 'UICollection' can not be used directly!";
}

///////////////////////////////////////////////////////////////////////////////
// field methods

/**
 * Removes all fields of the collectional object.
 */
UICollection.prototype.cleanupFields = function()  {
  var query = new SearchQuery();
  query.setExpand(Shell.getContext(), this.getOid(), "Admin_UI_Field\\Collection");
  query.addSelect(Shell.getContext(), "OID");
  query.execute(Shell.getContext());
  while (query.next())  {
    var oid = query.get(Shell.getContext(), "OID");
    (new Field(new Instance(Shell.getContext(), oid))).remove();
  }
  query.close();
}

/**
 * Adds a new field to the collectional object (web form or web table) and 
 * returns a new instance of the Field javascript representing this new created
 * and added field to the collectional object.
 *
 * @param _name (String) name of the field to add to the collectional object
 * @return (Field) instance of Field javascript class 
 */
UICollection.prototype.addField = function(_name)  {
  var insert = new Insert(Shell.getContext(), "Admin_UI_Field");
  insert.add(Shell.getContext(), "Collection", this.getId());
  insert.add(Shell.getContext(), "Name", _name);
  insert.executeWithoutAccessCheck();
  return new Field(insert.getInstance());
}

/**
 * Writes the JS update scripts for all fields.
 *
 * @param _file (Writer)  open file to write through
 */
UICollection.prototype._writeFields = function(_file)  {
  var query = new SearchQuery();
  query.setExpand(Shell.getContext(), this.getOid(), "Admin_UI_Field\\Collection");
  query.addSelect(Shell.getContext(), "Name");
  query.addSelect(Shell.getContext(), "OID");
  query.execute(Shell.getContext());
  while (query.next())  {
    var fieldOid  = query.get(Shell.getContext(), "OID");
    var fieldName = query.get(Shell.getContext(), "Name");
    _file.println("  with (addField(\""+fieldName+"\"))  {");
    var field = new Field(new Instance(Shell.getContext(), fieldOid));
    field._writeProperties(_file, "  ");
    field._writeLinks(_file, "  ");
    _file.println("  }");
  }
  query.close();
}

///////////////////////////////////////////////////////////////////////////////
// common methods

/**
 * The method cleans the object. All fields and properties are removed.
 */
UICollection.prototype.cleanup = function()  {
  this.cleanupFields();
  this.cleanupProperties();
}

/**
 * Creates the update script for current administration user interface 
 * collection object (web forms and web tables).
 *
 * @param _path   (String)  file is created in this path
 * @param _author (String)  author used in the header
 */
UICollection.prototype.writeUpdateScript = function(_path, _author)  {
  var file = new PrintWriter(new FileWriter(_path+"/"+this.FILE_PREFIX+this.getName()+".js"));

  this._writeHeader(file, _author);

  file.println("with ("+this.VARNAME+")  {");
  this._writeProperties(file);
  this._writeFields(file);
  file.println("}");

  file.close();
}
