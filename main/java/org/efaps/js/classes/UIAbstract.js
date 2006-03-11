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
