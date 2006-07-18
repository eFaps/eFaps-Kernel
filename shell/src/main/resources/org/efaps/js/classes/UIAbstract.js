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

importClass(Packages.org.efaps.db.Delete);
importClass(Packages.org.efaps.db.Insert);
importClass(Packages.org.efaps.db.SearchQuery);

/**
 * This is the Constructor for class Abstract.
 */
function UIAbstract()  {
  throw "abstract class 'UIAbstract' can not be used directly!";
}

///////////////////////////////////////////////////////////////////////////////
// link methods

/**
 * Remove all links.
 */
UIAbstract.prototype.cleanupLinks = function UIAbstract_cleanupLinks()  {
  var query = new SearchQuery();
  query.setExpand(Shell.getContext(), this.getInstance(), "Admin_UI_Link\\From");
  query.addSelect(Shell.getContext(), "OID");
  query.execute(Shell.getContext());

  while (query.next())  {
    var oid = query.get(Shell.getContext(), "OID");
    var del = new Delete(Shell.getContext(), oid);
    del.execute(Shell.getContext());
  }
}

/**
 * Create one link
 *
 * @param _linkType   link type name to create
 * @param _toType     type of the object to connect
 * @param _toName     name of the object to connect
 */
UIAbstract.prototype._createLink = function(_linkType, _toType, _toName)  {
  var query = new SearchQuery();
  query.setQueryTypes(Shell.getContext(), _toType);
  query.addWhereExprEqValue(Shell.getContext(), "Name", _toName);
  query.addSelect(Shell.getContext(), "ID");
  query.execute(Shell.context);
  if (query.next())  {
    var toId = query.get(Shell.getContext(), "ID");
    var insert = new Insert(Shell.getContext(), _linkType);
    insert.add(Shell.getContext(), "From", this.getId());
    insert.add(Shell.getContext(), "To", toId);
    insert.execute(Shell.getContext());
  } else  {
    print("!!!!!!!!!!!!!!!!!!! '"+_toType+"' '"+_toName+"' not found!");
  }
}

/**
 * Writes the JS update scripts for all links.
 *
 * @param _file (Writer)    open file to write through
 * @param _space  (String)  space to write in front of the properties
 */
UIAbstract.prototype._writeLinks = function(_file, _space)  {
  var query = new SearchQuery();
  query.setExpand(Shell.getContext(), this.getInstance(), "Admin_UI_Link\\From");
  query.addSelect(Shell.getContext(), "Type");
  query.addSelect(Shell.getContext(), "To.Name");
  query.execute(Shell.getContext());

  var links = new Packages.java.util.TreeSet();
  while (query.next())  {
    var type   = query.get(Shell.getContext(), "Type").getName();
    var toName = query.get(Shell.getContext(), "To.Name");

    if (type.equals("Admin_UI_LinkIcon"))  {
      links.add("  addIcon(\""+toName+"\");");
    } else if (type.equals("Admin_UI_LinkTargetForm"))  {
      links.add("  addTargetForm(\""+toName+"\");");
    } else if (type.equals("Admin_UI_LinkTargetMenu"))  {
      links.add("  addTargetMenu(\""+toName+"\");");
    } else if (type.equals("Admin_UI_LinkTargetSearch"))  {
      links.add("  addTargetSearch(\""+toName+"\");");
    } else if (type.equals("Admin_UI_LinkTargetTable"))  {
      links.add("  addTargetTable(\""+toName+"\");");
    }
  }
  
  var linksArray = links.toArray();
  for (index in linksArray)  {
    if (_space)  {
      _file.print(_space);
    }
    _file.println(linksArray[index]);
  }
}

///////////////////////////////////////////////////////////////////////////////
// access methods

/**
 * Remove all access
 */
UIAbstract.prototype.cleanupAccess = function UIAbstract_cleanupAccess()  {
  var query = new SearchQuery();
  query.setExpand(Shell.getContext(), this.getInstance(), "Admin_UI_Access\\UILink");
  query.addSelect(Shell.getContext(), "OID");
  query.execute(Shell.getContext());

  while (query.next())  {
    var oid = query.get(Shell.getContext(), "OID");
    var del = new Delete(Shell.getContext(), oid);
    del.execute(Shell.getContext());
  }
}

/**
 * Add one Role who has access.
 *
 * @param _userName     name of the role who get access
 */
UIAbstract.prototype.addRole = function(_userName)  {
  var query = new SearchQuery();
  query.setQueryTypes(Shell.getContext(), "Admin_User_Role");
  query.addWhereExprEqValue(Shell.getContext(), "Name", _userName);
  query.addSelect(Shell.getContext(), "ID");
  query.execute(Shell.context);
  if (query.next())  {
    var userId = query.get(Shell.getContext(), "ID");
    var insert = new Insert(Shell.getContext(), "Admin_UI_Access");
    insert.add(Shell.getContext(), "UserLink", userId);
    insert.add(Shell.getContext(), "UILink",   this.getId());
    insert.execute(Shell.getContext());
  } else  {
    print("!!!!!!!!!!!!!!!!!!! Role '" + _userName + "' not found!");
  }
}

/**
 * Writes the JS update scripts for all access.
 *
 * @param _file (Writer)    open file to write through
 * @param _space  (String)  space to write in front of the properties
 */
UIAbstract.prototype._writeAccess = function(_file, _space)  {
/*  var query = new SearchQuery();
  query.setExpand(Shell.getContext(), this.getInstance(), "Admin_UI_Link\\From");
  query.addSelect(Shell.getContext(), "Type");
  query.addSelect(Shell.getContext(), "To.Name");
  query.execute(Shell.getContext());

  var links = new Packages.java.util.TreeSet();
  while (query.next())  {
    var type   = query.get(Shell.getContext(), "Type").getName();
    var toName = query.get(Shell.getContext(), "To.Name");

    if (type.equals("Admin_UI_LinkIcon"))  {
      links.add("  addIcon(\""+toName+"\");");
    } else if (type.equals("Admin_UI_LinkTargetForm"))  {
      links.add("  addTargetForm(\""+toName+"\");");
    } else if (type.equals("Admin_UI_LinkTargetMenu"))  {
      links.add("  addTargetMenu(\""+toName+"\");");
    } else if (type.equals("Admin_UI_LinkTargetSearch"))  {
      links.add("  addTargetSearch(\""+toName+"\");");
    } else if (type.equals("Admin_UI_LinkTargetTable"))  {
      links.add("  addTargetTable(\""+toName+"\");");
    }
  }
  
  var linksArray = links.toArray();
  for (index in linksArray)  {
    if (_space)  {
      _file.print(_space);
    }
    _file.println(linksArray[index]);
  }
*/
}
