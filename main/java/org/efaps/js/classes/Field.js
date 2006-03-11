importClass(Packages.org.efaps.db.Delete);

/**
 * The class represents a field of a form or a table.
 */
function Field(_instance)  {
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

  // add link methods
  this._createLink        = UIAbstract.prototype._createLink;
  this._writeLinks        = UIAbstract.prototype._writeLinks;
  this.cleanupLinks       = UIAbstract.prototype.cleanupLinks;
  this.addIcon            = Command.prototype.addIcon;
}

///////////////////////////////////////////////////////////////////////////////
// common methods

Field.prototype.cleanup = function()  {
  this.cleanupProperties();
  this.cleanupLinks();
}

Field.prototype.remove = function()  {
  this.cleanup();
  var del = new Delete(Shell.getContext(), this.getOid());
  del.execute(Shell.getContext());
}
