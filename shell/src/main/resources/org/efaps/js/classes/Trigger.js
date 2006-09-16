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

/**
 * The class represents an Trigger of a type.
 */

function Trigger(_instance)  {
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
 * Cleanup all related information of this Trigger.
 */
Trigger.prototype.cleanup = function()  {
  this.cleanupProperties();
}

/**
 * Delete this Trigger.
 */
Trigger.prototype.remove = function()  {
  this.cleanup();
  var del = new Packages.org.efaps.db.Delete(Shell.getContext(), this.getOid());
  del.execute(Shell.getContext());
}

/**
 * Writes the JS update scripts for this Trigger.
 *
 * @param _file   (Writer)  open file to write through
 * @param _space  (String)  space to write in front of the Triggers
 */
Trigger.prototype._writeUpdateScript = function(_file, _space)  {
/*  var query = new Packages.org.efaps.db.SearchQuery();
  query.setObject(Shell.getContext(), this.getOid());
  query.addSelect(Shell.getContext(), "OID");
  query.addSelect(Shell.getContext(), "TriggerType.Name");
  query.addSelect(Shell.getContext(), "TypeLink.Name");
  query.addSelect(Shell.getContext(), "Table.Name");
  query.addSelect(Shell.getContext(), "SQLColumn");
  query.executeWithoutAccessCheck();
  if (query.next())  {
    var attrType  = query.get(Shell.getContext(), "TriggerType.Name").value;
    var typeLink  = query.get(Shell.getContext(), "TypeLink.Name").value;
    var tableName = query.get(Shell.getContext(), "Table.Name").value;
    var sqlColumn = query.get(Shell.getContext(), "SQLColumn").value;

    _file.println(_space + "  setTriggerType(\"" + attrType + "\");");
    if (typeLink.length()>0)  {
      _file.println(_space + "  setTypeLink(\"" + typeLink + "\");");

    }
    _file.println(_space + "  setSQLTable(\"" + tableName + "\");");
    _file.println(_space + "  setSQLColumn(\"" + sqlColumn + "\");");
  }
  query.close();
*/
  this._writeProperties(_file, _space);
}

///////////////////////////////////////////////////////////////////////////////
// getter and setter methods

