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

importClass(java.io.File);
importClass(java.io.FileInputStream);

importClass(Packages.org.efaps.db.Checkin);
importClass(Packages.org.efaps.db.Insert);
importClass(Packages.org.efaps.db.Instance);
importClass(Packages.org.efaps.db.SearchQuery);

/**
 * The class represents images used e.g. for type icons or for user interfaces.
 */
function Image(_name)  {
  var query = new SearchQuery();
  query.setQueryTypes(Shell.getContext(), "Admin_UI_Image");
  query.addWhereExprEqValue(Shell.getContext(), "Name", _name);
  query.addSelect(Shell.getContext(), "OID");
  query.executeWithoutAccessCheck();

  if (query.next())  {
    var oid = query.get(Shell.getContext(), "OID");
    this.instance = new Instance(Shell.getContext(), oid);
  } else  {
    this.instance = null;
  }
  query.close();

  this.object = new EFapsInstance("Admin_UI_Image", _name);

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
}

///////////////////////////////////////////////////////////////////////////////
// instance variables

/**
 * Prefix of script file names.
 */
Image.prototype.FILE_PREFIX = new Packages.java.lang.String("IMAGE_");

///////////////////////////////////////////////////////////////////////////////
// common methods

/**
 * Updates current image in eFaps with image of file. If the image in eFaps 
 * does not exists, the image is created.
 *
 * @param _fileName (String)  name of the update file
 * @param _objName  (String)  name of the object
 */
Image.prototype.update = function(_fileName, _objName)  {
  if (this.getOid()==null || this.getOid()=="" || this.getOid()=="0")  {
    print("  - create");
    var insert = new Insert(Shell.getContext(), "Admin_UI_Image");
    insert.add(Shell.getContext(), "Name", _objName);
    insert.executeWithoutAccessCheck();
    this.instance = insert.getInstance();
  } else  {
    print("  - update");
  }
  this.checkin(_fileName);
}

/**
 * The file with the name '_fileName' is checked in the object. The file could
 * be accessable via the class patch or normal system path (method first 
 * searches inside the classpath, than in the system path).
 *
 * @param _fileName name of the file to checkin
 */
Image.prototype.checkin = function(_fileName)  {
  var context = Packages.org.mozilla.javascript.Context.enter();
  var reader;
  try  {
    var stream = _eFapsClassLoader.getResourceAsStream(_fileName);
    if (stream==null)  {
      stream = new FileInputStream(new File(_fileName));
    }

    var checkin = new Checkin(Shell.getContext(), this.getInstance());
    checkin.execute(Shell.getContext(), _fileName, stream, stream.available());

    stream.close();
  } catch (e)  {
print(e);
    context.reportError(e.toString());
  } finally  {
    try  {reader.close();} catch(f) {}
  }
}


///////////////////////////////////////////////////////////////////////////////
// static functions

/**
 * One image file with the given name is imported.
 *
 * @param _fileName   file to import
 */
function importImage(_fileName)  {
  var file = new Packages.java.io.File(_fileName);
  var fileName = new Packages.java.lang.String(file.getName());
  if (fileName.startsWith(Image.prototype.FILE_PREFIX) && (fileName.endsWith(".gif") || fileName.endsWith(".png")))  {
    var objName = new String(fileName.substring(Image.prototype.FILE_PREFIX.length(), fileName.length()-4));
    print("Import Image '"+objName+"'");
    var imp = new Image(objName);
    imp.update(_fileName, objName);
  }
}

/**
 * A list of Image Definition files is imported.
 *
 * @param _fileList (Array) array with list of files
 */
function importImages(_fileList)  {
  print("");
  print("Import Images");
  print("~~~~~~~~~~~~~");
  for (indx in _fileList)  {
    importImage(_fileList[indx]);
  }
  print("");
}

/**
 * Creates all Images in the given path matching given match.
 *
 * @param _path   (String) path to write all images in
 * @param _match  (String) match for menu names
 */
function createScriptImages(_path, _match)  {
  var query = new SearchQuery();
  query.setQueryTypes(Shell.getContext(), "Admin_UI_Image");
  query.addWhereExprEqValue(Shell.getContext(), "Name", _match);
  query.addSelect(Shell.getContext(), "Name");
  
  query.executeWithoutAccessCheck();
  
  print("Create Images:");
  print("~~~~~~~~~~~~~~");
  while (query.next())  {
    var name = query.get(Shell.getContext(), "Name");
    print("  - '"+  name  +"'");
    var image = new Image(name);
    image.object.checkout("File", _path+"/"+name);
  }

  query.close();
}
