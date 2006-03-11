importClass(Packages.java.util.TreeMap);

importClass(Packages.org.efaps.db.Delete);
importClass(Packages.org.efaps.db.Insert);
importClass(Packages.org.efaps.db.SearchQuery);

/**
 * This is the Constructor for class Abstract.
 */
function Abstract()  {
  throw "abstract class 'Abstract' can not be used directly!";
}

///////////////////////////////////////////////////////////////////////////////
// property methods

Abstract.prototype.printProperties = function() {
/*  var properties = this.object.Admin_UI_Property\Abstract;
  if (properties!=null)  {
    var i = 0;
    while (i < properties.length) {
      print(properties[i].Name + " = " + properties[i].Value);
      i=i+1;
    }
  }
*/
}

/**
 * Add a new property to the administational object.
 *
 * @param _name   (String)  name of the property
 * @param _value  (String)  value of the property
 */
Abstract.prototype.addProperty = function(_name, _value)  {
  var insert = new Insert(Shell.getContext(), "Admin_Property");
  insert.add(Shell.getContext(), "Name", _name);
  insert.add(Shell.getContext(), "Value", _value);
  insert.add(Shell.getContext(), "Abstract", this.getId());
  insert.execute(Shell.getContext());
}

Abstract.prototype.deleteProperty = function(_name)  {
/*  var properties = this.object.Admin_UI_Property\Abstract;
  if (properties!=null)  {
    var i = 0;
    while (i < properties.length) {
      if (properties[i].Name == _name)   {
        properties[i].remove();
        break;
      }
      i=i+1;
    }
  }
*/
}

/**
 * Deletes all properties of the administrational object.
 */
Abstract.prototype.cleanupProperties = function()  {
  var query = new SearchQuery();
  query.setExpand(Shell.getContext(), this.getOid(), "Admin_Property\\Abstract");
  query.addSelect(Shell.getContext(), "OID");
  query.execute(Shell.getContext());
  while (query.next())  {
    var propOid = query.get(Shell.getContext(), "OID");
    var del = new Delete(Shell.getContext(), propOid);
    del.execute(Shell.getContext());
  }
  query.close();
}

/**
 * Writes the JS update scripts for the properties. The properties are sorted 
 * by name.
 *
 * @param _file   (Writer)  open file to write through
 * @param _space  (String)  space to write in front of the properties
 */
Abstract.prototype._writeProperties = function(_file, _space)  {
  var query = new SearchQuery();
  query.setExpand(Shell.getContext(), this.getInstance(), "Admin_Property\\Abstract");
  query.addSelect(Shell.getContext(), "Name");
  query.addSelect(Shell.getContext(), "Value");
  query.execute(Shell.getContext());

  var map = new TreeMap();
  while (query.next())  {
    var name  = query.get(Shell.getContext(), "Name");
    var value = query.get(Shell.getContext(), "Value");

    value = value.replaceAll("\\\\", "\\\\\\\\");
    var text = "  addProperty(\""+name+"\", ";
    for (var j = name.length(); j<28; j++)  {
      text += " ";
    }
    text += "\""+value+"\");";
    if (_space)  {
      text = _space + text;
    }
    map.put(name, text);
  }
  query.close();

  var  iter = map.values().iterator();
  while (iter.hasNext())  {
    _file.println(iter.next());
  }
}

///////////////////////////////////////////////////////////////////////////////
// common methods

/**
 * Writes the header for the update script.
 *
 * @param _file   (Writer)  open file to write through
 * @param _author (String)  author used in the header
 */
Abstract.prototype._writeHeader = function(_file, _author)  {
  var type = this.getType();

 _file.println("/*******************************************************************************");
  _file.println("* "+type.getName()+":");
  _file.print("* ~");
  for (var i=0; i<type.getName().length(); i++)  {
    _file.print("~");
  }
  _file.println("");
  _file.println("* "+this.getName());
  _file.println("*");
  _file.println("* Description:");
  _file.println("* ~~~~~~~~~~~~");
  _file.println("*");
  _file.println("* History:");
  _file.println("* ~~~~~~~~");
  _file.println("* Revision: $Rev$");
  _file.println("* Date:     $Date$");
  _file.println("* By:       $Author$");
  _file.println("*");
  _file.println("* Author:");
  _file.println("* ~~~~~~~");
  if (_author)  {
    _file.println("* "+_author);
  } else  {
    _file.println("*");
  }
  _file.println("*******************************************************************************/");
  _file.println("");

}

///////////////////////////////////////////////////////////////////////////////
// helper methods for getter and setter 

/**
 *
 * @param _attrName     attribute name
 * @param _attrValue    new attribute value to set
 */
Abstract.prototype._setAttrValue = function(_attrName, _attrValue)  {
  var update = new Packages.org.efaps.db.Update(Shell.getContext(), this.getOid());
  update.add(Shell.getContext(), _attrName, _attrValue);  
  update.execute(Shell.getContext());
}

/**
 * @param _attrName     attribute name
 */
Abstract.prototype._getAttrValue = function(_attrName)  {
  var ret = null;
  var query = new Packages.org.efaps.db.SearchQuery();
  query.setObject(Shell.getContext(), this.getOid());
  query.addSelect(Shell.getContext(), _attrName);
  query.execute(Shell.getContext());
  if (query.next())  {
    ret = query.get(Shell.getContext(), _attrName);
  }
  return ret;
}

///////////////////////////////////////////////////////////////////////////////
// getter and setter methods

/**
 * @return instance object of this object
 */
Abstract.prototype.getInstance = function()  {
  return this.instance;
}

/**
 * @return id (not oid!) of this object
 */
Abstract.prototype.getId = function()  {
  return (this.getInstance()==null ? null : this.getInstance().getId());
}

/**
 * @return object id of this object
 */
Abstract.prototype.getOid = function()  {
  return (this.getInstance()==null ? null : this.getInstance().getOid());
}

/**
 * @return type name of this object
 */
Abstract.prototype.getType = function()  {
  return this._getAttrValue("Type");
}

/**
 * @return name of this object
 */
Abstract.prototype.getName = function()  {
  return this._getAttrValue("Name");
}

/**
 * @return revision of this object
 */
Abstract.prototype.getRevision = function()  {
  return this._getAttrValue("Revision");
}

/**
 * Sets the new revision of this object.
 *
 * @param _newRevision  new revision to set
 */
Abstract.prototype.setRevision = function(_newRevision)  {
  this._setAttrValue("Revision", _newRevision);
}
