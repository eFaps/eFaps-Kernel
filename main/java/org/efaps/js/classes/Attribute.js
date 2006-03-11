/**
 * The class represents an attribute of a type.
 */

function Attribute(_instance)  {
  this.instance = _instance;

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
}

///////////////////////////////////////////////////////////////////////////////
// common methods

/**
 * Cleanup all related information of this attribute.
 */
Attribute.prototype.cleanup = function()  {
  this.cleanupProperties();
}

/**
 * Delete this attribute.
 */
Attribute.prototype.remove = function()  {
  this.cleanup();
  var del = new Packages.org.efaps.db.Delete(Shell.getContext(), this.getOid());
  del.execute(Shell.getContext());
}

/**
 * Writes the JS update scripts for this attribute.
 *
 * @param _file   (Writer)  open file to write through
 * @param _space  (String)  space to write in front of the attributes
 */
Attribute.prototype._writeUpdateScript = function(_file, _space)  {
  var query = new Packages.org.efaps.db.SearchQuery();
  query.setObject(Shell.getContext(), this.getOid());
  query.addSelect(Shell.getContext(), "OID");
  query.addSelect(Shell.getContext(), "AttributeType.Name");
  query.addSelect(Shell.getContext(), "TypeLink.Name");
  query.addSelect(Shell.getContext(), "Table.Name");
  query.addSelect(Shell.getContext(), "SQLColumn");
  query.execute(Shell.getContext());
  if (query.next())  {
    var attrType  = query.get(Shell.getContext(), "AttributeType.Name").value;
    var typeLink  = query.get(Shell.getContext(), "TypeLink.Name").value;
    var tableName = query.get(Shell.getContext(), "Table.Name").value;
    var sqlColumn = query.get(Shell.getContext(), "SQLColumn").value;

    _file.println(_space + "  setAttributeType(\"" + attrType + "\");");
    if (typeLink.length()>0)  {
      _file.println(_space + "  setTypeLink(\"" + typeLink + "\");");

    }
    _file.println(_space + "  setSQLTable(\"" + tableName + "\");");
    _file.println(_space + "  setSQLColumn(\"" + sqlColumn + "\");");
  }
  query.close();

  this._writeProperties(_file, _space);
}

///////////////////////////////////////////////////////////////////////////////
// getter and setter methods

/**
 * Sets the attribute type for this attribute.
 *
 * @param _newAttrType new attribute type to set
 */
Attribute.prototype.setAttributeType = function(_newAttrType)  {
  var attrTypeInst = new Packages.org.efaps.db.Instance(
      Shell.getContext(),
      (new EFapsInstance("Admin_DataModel_AttributeType", _newAttrType)).oid
  );
  this._setAttrValue("AttributeType", attrTypeInst.getId());
}

/**
 * Sets the SQL Table for this attribute. The SQL Table can be the name string 
 * of the sql table or the SQL Table object representation in javascript.
 *
 * @param _newSQLTable new SQL table to set
 */
Attribute.prototype.setSQLTable = function(_newSQLTable)  {
  if (_newSQLTable!=null)  {
    var sqlTable;
    if (typeof(_newSQLTable)=="string")  {
      sqlTable = new SQLTable(_newSQLTable);
    } else  {
      sqlTable = _newSQLTable;
    }
    this._setAttrValue("Table", sqlTable.getId());
  } else  {
    this._setAttrValue("Table", null);
  }
}

/**
 * Sets the SQL Column for this attribute.
 *
 * @param _newSQLColumn new SQL Column to set
 */
Attribute.prototype.setSQLColumn = function(_newSQLColumn)  {
  this._setAttrValue("SQLColumn", _newSQLColumn);
}

/**
 * Sets the Type Link for this attribute. The Type Link can be the name string 
 * of the type or the type object representation in javascript.
 *
 * @param _newTypeLink new type for the link to set
 */
Attribute.prototype.setTypeLink = function(_newTypeLink)  {
  if (_newTypeLink!=null)  {
    var newType;
    if (typeof(_newTypeLink)=="string")  {
      newType = new Type(_newTypeLink);
    } else  {
      newType = _newTypeLink;
    }
    this._setAttrValue("TypeLink", newType.getId());
  } else  {
    this._setAttrValue("TypeLink", null);
  }
}
