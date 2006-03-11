/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * The contents of this file are subject to the Netscape Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/NPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express oqr
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Rhino code, released
 * May 6, 1999.
 *
 * The Initial Developer of the Original Code is Netscape
 * Communications Corporation.  Portions created by Netscape are
 * Copyright (C) 1997-1999 Netscape Communications Corporation. All
 * Rights Reserved.
 *
 * Contributor(s):
 * Norris Boyd
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Public License (the "GPL"), in which case the
 * provisions of the GPL are applicable instead of those above.
 * If you wish to allow use of your version of this file only
 * under the terms of the GPL and not to allow others to use your
 * version of this file under the NPL, indicate your decision by
 * deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL.  If you do not delete
 * the provisions above, a recipient may use your version of this
 * file under either the NPL or the GPL.
 */

// API class

package org.efaps.js;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.AbstractLinkType;
import org.efaps.admin.datamodel.attributetype.BooleanType;
import org.efaps.admin.datamodel.attributetype.DateTimeType;
import org.efaps.admin.datamodel.attributetype.IntegerType;
import org.efaps.admin.datamodel.attributetype.PersonLinkType;
import org.efaps.admin.datamodel.attributetype.RealType;
import org.efaps.admin.datamodel.attributetype.StatusLinkType;
import org.efaps.admin.datamodel.attributetype.StringType;
import org.efaps.admin.datamodel.attributetype.TypeType;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.Type;
//import org.efaps.db.Checkin;
//import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;


/**
 * This is interface that all objects in JavaScript must implement.
 * The interface provides for the management of properties and for
 * performing conversions.
 * <p>
 * Host system implementors may find it easier to extend the ScriptableObject
 * class rather than implementing Scriptable when writing host objects.
 * <p>
 * There are many static methods defined in ScriptableObject that perform
 * the multiple calls to the Scriptable interface needed in order to
 * manipulate properties in prototype chains.
 * <p>
 *
 * @see org.mozilla.javascript.ScriptableObject
 * @author Norris Boyd
 * @author Nick Thompson
 * @author Brendan Eich
 * @todo must be rewritten completly (maybe not needed anymore?)
 */
public class EFapsInstance implements Scriptable  {


Instance instance = null;


/*    public void jsConstructor(String _oid)  {
System.out.println("ein-constructor");
try {
} catch (Exception e)  {
  e.printStackTrace();
}
    }
*/

    // Method jsConstructor defines the JavaScript constructor
    public void jsConstructor(String _type, String _name) {
//System.out.println("jsConstructor _type="+_type+" _name="+_name);
try {
//  if (type==null)  {
//    instance = new Instance(Shell.context, _type);
//  } else  {

Type type = Type.get(_type);

    Attribute nameAttr = type.getAttribute("Name");
    if (nameAttr==null)  {
      instance = new Instance(Shell.context, type, 0);
    } else  {
      SearchQuery query =  new SearchQuery();
      query.setQueryTypes(Shell.getContext(), _type);
      query.addWhereExprEqValue(Shell.getContext(), "Name", _name);
      query.addSelect(Shell.getContext(), "Name");
      query.execute(Shell.context);
      if (query.next())  {
        instance = new Instance(Shell.context, query.getRowOIDs(Shell.context));
      } else  {
        instance = new Instance(Shell.context, type, 0);
      }
    }
//  }

} catch (Exception e)  {
  e.printStackTrace();
}
    }

    public EFapsInstance() {
//System.out.println("null-constructor");
instance = null;
    }

    public EFapsInstance(Instance _instance) {
//System.out.println("_instance-constructor");
instance = _instance;
    }

    /**
     * Get the name of the set of objects implemented by this Java class.
     * This corresponds to the [[Class]] operation in ECMA and is used
     * by Object.prototype.toString() in ECMA.<p>
     * See ECMA 8.6.2 and 15.2.4.2.
     */
    public String getClassName() {
      return "EFapsInstance";
    }

    /**
     * Get a named property from the object.
     *
     * Looks property up in this object and returns the associated value
     * if found. Returns NOT_FOUND if not found.
     * Note that this method is not expected to traverse the prototype
     * chain. This is different from the ECMA [[Get]] operation.
     *
     * Depending on the property selector, the runtime will call
     * this method or the form of <code>get</code> that takes an
     * integer:
     * <table>
     * <tr><th>JavaScript code</th><th>Java code</th></tr>
     * <tr><td>a.b      </td><td>a.get("b", a)</td></tr>
     * <tr><td>a["foo"] </td><td>a.get("foo", a)</td></tr>
     * <tr><td>a[3]     </td><td>a.get(3, a)</td></tr>
     * <tr><td>a["3"]   </td><td>a.get(3, a)</td></tr>
     * <tr><td>a[3.0]   </td><td>a.get(3, a)</td></tr>
     * <tr><td>a["3.0"] </td><td>a.get("3.0", a)</td></tr>
     * <tr><td>a[1.1]   </td><td>a.get("1.1", a)</td></tr>
     * <tr><td>a[-4]    </td><td>a.get(-4, a)</td></tr>
     * </table>
     * <p>
     * The values that may be returned are limited to the following:
     * <UL>
     * <LI>java.lang.Boolean objects</LI>
     * <LI>java.lang.String objects</LI>
     * <LI>java.lang.Number objects</LI>
     * <LI>org.mozilla.javascript.Scriptable objects</LI>
     * <LI>null</LI>
     * <LI>The value returned by Context.getUndefinedValue()</LI>
     * <LI>NOT_FOUND</LI>
     * </UL>
     * @param name the name of the property
     * @param start the object in which the lookup began
     * @return the value of the property (may be null), or NOT_FOUND
     * @see org.mozilla.javascript.Context#getUndefinedValue
     */
    public Object get(String _name, Scriptable start) {
Object ret = NOT_FOUND;
try {
//System.out.println("--->get("+_name+","+start+")");
EFapsInstance self = (EFapsInstance)start;

if (_name.equals("oid"))  {
  ret = instance.getOid();
} else  {

ret = basics.get(_name);

if (ret==null)  {
  Attribute attr = self.instance.getType().getAttribute(_name);
  if (attr!=null)  {
    SearchQuery query = new SearchQuery();
    query.setObject(Shell.getContext(), self.instance);

if (AbstractLinkType.class.isAssignableFrom(attr.getAttributeType().getClassRepr()))  {
  query.addSelect(Shell.getContext(), _name+".OID");
}
query.addSelect(Shell.getContext(), _name);
query.setExpandChildTypes(true);
//    query.add(attr);
//if (attr instanceof AbstractLinkType)  {
//  query.addAllFromString(Shell.getContext(), "$<Type>");
//}

    query.execute(Shell.context);
if (query.next())  {
//    AttributeTypeInterface result=query.get(Shell.context, attr);
Object result = query.get(Shell.getContext(), _name);
    query.close();

//    } else if (result instanceof PersonLinkType)  {
//      ret = result.getViewableString(null);
//    } else if (result instanceof StatusLinkType)  {
//      ret = result.getViewableString(null);

    if (result instanceof Type)  {
      ret = ((Type)result).getName();
/*    } else if (result instanceof AbstractLinkType)  {
      result = query.get(Shell.getContext(), _name+".OID");
      String oid = result.getViewableString(Shell.getContext().getLocale());
      ret = new EFapsInstance();
      ((EFapsInstance)ret).instance = new Instance(Shell.context, oid);
*/
    } else  {
      result = ret;
    }
} else  {
  System.err.println("----------------------> nullllll");
}
  } else  {

//attr = self.instance.getType().getLinks().get(_name);

//    if (attr!=null)  {
//System.out.println("found link!!! mache expand");

List<EFapsInstance> list = new ArrayList<EFapsInstance>();

SearchQuery query = new SearchQuery();
query.setExpand(Shell.getContext(), self.instance, _name);
query.execute(Shell.getContext());

while (query.next())  {
  list.add(new EFapsInstance(new Instance(Shell.getContext(), query.getRowOIDs(Shell.getContext()))));
}
query.close();

ret = new NativeArray(list.toArray());

//    } else  {
//      System.err.println("undefined attribute name "+_name);
//    }
  }
}
}

} catch (Exception e)  {
  e.printStackTrace();
}

return ret;
    }

    /**
     * Get a property from the object selected by an integral index.
     *
     * Identical to <code>get(String, Scriptable)</code> except that
     * an integral index is used to select the property.
     *
     * @param index the numeric index for the property
     * @param start the object in which the lookup began
     * @return the value of the property (may be null), or NOT_FOUND
     * @see org.mozilla.javascript.Scriptable#get(String,Scriptable)
     */
    public Object get(int index, Scriptable start)  {
//System.out.println("--->get("+index+","+start+")");
return NOT_FOUND;
    }

    /**
     * Indicates whether or not a named property is defined in an object.
     *
     * Does not traverse the prototype chain.<p>
     *
     * The property is specified by a String name
     * as defined for the <code>get</code> method.<p>
     *
     * @param name the name of the property
     * @param start the object in which the lookup began
     * @return true if and only if the named property is found in the object
     * @see org.mozilla.javascript.Scriptable#get
     * @see org.mozilla.javascript.ScriptableObject#getProperty
     */
    public boolean has(String name, Scriptable start)  {
//System.out.println("--->has("+name+","+start+")");
return true;
    }

    /**
     * Indicates whether or not an indexed  property is defined in an object.
     *
     * Does not traverse the prototype chain.<p>
     *
     * The property is specified by an integral index
     * as defined for the <code>get</code> method.<p>
     *
     * @param index the numeric index for the property
     * @param start the object in which the lookup began
     * @return true if and only if the indexed property is found in the object
     * @see org.mozilla.javascript.Scriptable#get
     * @see org.mozilla.javascript.ScriptableObject#getProperty
     */
    public boolean has(int index, Scriptable start)  {
//System.out.println("--->put("+index+","+start+")");
return false;
    }

    /**
     * Sets a named property in this object.
     * <p>
     * The property is specified by a string name
     * as defined for <code>get</code>.
     * <p>
     * The possible values that may be passed in are as defined for
     * <code>get</code>. A class that implements this method may choose
     * to ignore calls to set certain properties, in which case those
     * properties are effectively read-only.<p>
     * For properties defined in a prototype chain,
     * use <code>putProperty</code> in ScriptableObject. <p>
     * Note that if a property <i>a</i> is defined in the prototype <i>p</i>
     * of an object <i>o</i>, then evaluating <code>o.a = 23</code> will cause
     * <code>set</code> to be called on the prototype <i>p</i> with
     * <i>o</i> as the  <i>start</i> parameter.
     * To preserve JavaScript semantics, it is the Scriptable
     * object's responsibility to modify <i>o</i>. <p>
     * This design allows properties to be defined in prototypes and implemented
     * in terms of getters and setters of Java values without consuming slots
     * in each instance.<p>
     * <p>
     * The values that may be set are limited to the following:
     * <UL>
     * <LI>java.lang.Boolean objects</LI>
     * <LI>java.lang.String objects</LI>
     * <LI>java.lang.Number objects</LI>
     * <LI>org.mozilla.javascript.Scriptable objects</LI>
     * <LI>null</LI>
     * <LI>The value returned by Context.getUndefinedValue()</LI>
     * </UL><p>
     * Arbitrary Java objects may be wrapped in a Scriptable by first calling
     * <code>Context.toObject</code>. This allows the property of a JavaScript
     * object to contain an arbitrary Java object as a value.<p>
     * Note that <code>has</code> will be called by the runtime first before
     * <code>set</code> is called to determine in which object the
     * property is defined.
     * Note that this method is not expected to traverse the prototype chain,
     * which is different from the ECMA [[Put]] operation.
     * @param name the name of the property
     * @param start the object whose property is being set
     * @param value value to set the property to
     * @see org.mozilla.javascript.Scriptable#has
     * @see org.mozilla.javascript.Scriptable#get
     * @see org.mozilla.javascript.ScriptableObject#putProperty
     * @see org.mozilla.javascript.Context#toObject
     */
    public void put(String _name, Scriptable _object, Object _value)  {
//System.out.println("getPrototype()="+getPrototype());
//System.out.println("_object()="+_object);


if (_value instanceof Function)  {
  basics.put(_name, _value);
} else  {
//System.out.println("--->put("+_name+","+_value+","+_object+")");
  EFapsInstance self = (EFapsInstance)_object;
//System.out.println("--->put("+self.instance+")");
//System.out.println("--->put("+self.instance.getType()+")");
  Attribute attr = self.instance.getType().getAttribute(_name);
  if (attr!=null)  {
    self.attributes.put(attr, _value);
  } else  {
System.err.println("Attribute '"+_name+"' does not exists for type '"+self.instance.getType().getName()+"'.");
  }
}
    }

    /**
     * Sets an indexed property in this object.
     * <p>
     * The property is specified by an integral index
     * as defined for <code>get</code>.<p>
     *
     * Identical to <code>put(String, Scriptable, Object)</code> except that
     * an integral index is used to select the property.
     *
     * @param index the numeric index for the property
     * @param start the object whose property is being set
     * @param value value to set the property to
     * @see org.mozilla.javascript.Scriptable#has
     * @see org.mozilla.javascript.Scriptable#get
     * @see org.mozilla.javascript.Scriptable#put(String,Scriptable,Object)
     * @see org.mozilla.javascript.ScriptableObject#putProperty
     */
    public void put(int index, Scriptable start, Object value)  {
//System.out.println("--->put("+index+","+value+","+start+")");
    }

    /**
     * Removes a property from this object.
     * This operation corresponds to the ECMA [[Delete]] except that
     * the no result is returned. The runtime will guarantee that this
     * method is called only if the property exists. After this method
     * is called, the runtime will call Scriptable.has to see if the
     * property has been removed in order to determine the boolean
     * result of the delete operator as defined by ECMA 11.4.1.
     * <p>
     * A property can be made permanent by ignoring calls to remove
     * it.<p>
     * The property is specified by a String name
     * as defined for <code>get</code>.
     * <p>
     * To delete properties defined in a prototype chain,
     * see deleteProperty in ScriptableObject.
     * @param name the identifier for the property
     * @see org.mozilla.javascript.Scriptable#get
     * @see org.mozilla.javascript.ScriptableObject#deleteProperty
     */
    public void delete(String name)  {
//System.out.println("--->delete("+name+")");
    }

    /**
     * Removes a property from this object.
     *
     * The property is specified by an integral index
     * as defined for <code>get</code>.
     * <p>
     * To delete properties defined in a prototype chain,
     * see deleteProperty in ScriptableObject.
     *
     * Identical to <code>delete(String)</code> except that
     * an integral index is used to select the property.
     *
     * @param index the numeric index for the property
     * @see org.mozilla.javascript.Scriptable#get
     * @see org.mozilla.javascript.ScriptableObject#deleteProperty
     */
    public void delete(int index)  {
//System.out.println("--->delete("+index+")");
    }

    /**
     * Get the prototype of the object.
     * @return the prototype
     */
    public Scriptable getPrototype()  {
//System.out.println("getPrototype()");
      return this.prototype;
    }

    /**
     * Set the prototype of the object.
     * @param prototype the prototype to set
     */
    public void setPrototype(Scriptable _prototype)  {
//System.out.println("setPrototype()"+_prototype.getClass());
      this.prototype = _prototype;
    }

    /**
     * Get the parent scope of the object.
     * @return the parent scope
     */
    public Scriptable getParentScope()  {
//System.out.println("getParentScope()");
      return this.parent;
    }

    /**
     * Set the parent scope of the object.
     * @param parent the parent scope to set
     */
    public void setParentScope(Scriptable _parent)  {
//System.out.println("setParentScope("+_parent+")");
      this.parent = _parent;
    }

    /**
     * Get an array of property ids.
     *
     * Not all property ids need be returned. Those properties
     * whose ids are not returned are considered non-enumerable.
     *
     * @return an array of Objects. Each entry in the array is either
     *         a java.lang.String or a java.lang.Number
     */
    public Object[] getIds()  {
return new Object[0];
    }

    /**
     * Get the default value of the object with a given hint.
     * The hints are String.class for type String, Number.class for type
     * Number, Scriptable.class for type Object, and Boolean.class for
     * type Boolean. <p>
     *
     * A <code>hint</code> of null means "no hint".
     *
     * See ECMA 8.6.2.6.
     *
     * @param hint the type hint
     * @return the default value
     */
    public Object getDefaultValue(Class hint)  {
//System.out.println("--->getDefaultValue("+hint+")");
return null;
    }

    /**
     * The instanceof operator.
     *
     * <p>
     * The JavaScript code "lhs instanceof rhs" causes rhs.hasInstance(lhs) to
     * be called.
     *
     * <p>
     * The return value is implementation dependent so that embedded host objects can
     * return an appropriate value.  See the JS 1.3 language documentation for more
     * detail.
     *
     * <p>This operator corresponds to the proposed EMCA [[HasInstance]] operator.
     *
     * @param instance The value that appeared on the LHS of the instanceof
     *              operator
     *
     * @return an implementation dependent value
     */
    public boolean hasInstance(Scriptable instance)  {
return false;
    }

    /**
     * The prototype of this object.
     */
    protected Scriptable prototype = null;

    /**
     * The parent scope of this object.
     */
    protected Scriptable parent;


public void jsFunction_attributes()  {
  System.out.println("Type '"+instance.getType().getName()+"' has following attributes:");
  for (Iterator iter = instance.getType().getAttributes().entrySet().iterator(); iter.hasNext(); )  {
    Map.Entry entry = (Map.Entry)iter.next();
    Attribute attr = (Attribute)(entry.getValue());
    System.out.println(""+attr.getName());
  }
}

public void jsFunction_links()  {
  System.out.println("Type '"+instance.getType().getName()+"' has following links:");
  for (String name : instance.getType().getLinks().keySet())  {
//    Attribute attr = (Attribute)(entry.getValue());
    System.out.println(""+name);
  }
}

// TODO: muss complete umgeschrieben werrden (mit Klasse org.efaps.db.Delete )
/*public void jsFunction_remove()  {
//System.out.println("remove");
  if (instance!=null && instance.getOid()!=null && instance.getOid().length()>0)  {

    java.sql.Statement stmt = null;
    try  {
      stmt = Shell.context.getConnection().createStatement();

      org.efaps.admin.datamodel.Table mainTable = instance.getType().getMainTable();
      Iterator iter = instance.getType().getTables().iterator();
      while (iter.hasNext())  {
        org.efaps.admin.datamodel.Table curTable = (org.efaps.admin.datamodel.Table)iter.next();
        if (curTable!=mainTable)  {
          StringBuffer buf = new StringBuffer();
          buf.append("delete from ").append(curTable.getSqlTable()).append(" ");
          buf.append("where ").append(curTable.getSqlColId()).append("=").append(instance.getId()).append("");
//System.out.println("buf="+buf.toString());
          stmt.addBatch(buf.toString());
        }
      }
      StringBuffer buf = new StringBuffer();
      buf.append("delete from ").append(mainTable.getSqlTable()).append(" ");
      buf.append("where ").append(mainTable.getSqlColId()).append("=").append(instance.getId()).append("");
      stmt.addBatch(buf.toString());
//System.out.println("buf="+buf.toString());

      stmt.executeBatch();
    } catch (Exception e)  {
e.printStackTrace();
    } finally  {
      try  {
        stmt.close();
      } catch (java.sql.SQLException e)  {
      }
    }
  }
}
*/

public void jsFunction_create() throws Exception  {
  Insert insert = new Insert(Shell.context, instance.getType());
  for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext(); )  {
    Map.Entry entry = (Map.Entry)iter.next();
    Attribute attr = (Attribute)entry.getKey();
    String value = entry.getValue().toString();
    insert.add(Shell.context, attr, value);
  }
  insert.execute(Shell.context);
  instance = insert.getInstance();
  attributes.clear();
}

public void jsFunction_update() throws Exception  {
  Update update = new Update(Shell.context, instance);
  for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext(); )  {
    Map.Entry entry = (Map.Entry)iter.next();
    Attribute attr = (Attribute)entry.getKey();
    String value = entry.getValue().toString();
    update.add(Shell.context, attr, value);
  }
  update.execute(Shell.context);
  attributes.clear();
}

// TODO: Exception handling?
  /**
   * Checkin into an attribute from a given file.
   *
   * @param _attrName   name of the attribute getting the file
   * @param _fileName   name of file to check in
   */
/*  public void jsFunction_checkin(String _attrName, String _fileName) throws Exception  {

    Checkin checkin = new Checkin(Shell.getContext(), this.instance, _attrName);

    File file = new File(_fileName);
    FileInputStream in = new FileInputStream(file);
    checkin.execute(Shell.getContext(), file.getName(), in, in.available());
    in.close();
  }
*/

// TODO: Exception handling?
  /**
   * Checkout from attribute into given file.
   *
   * @param _attrName   name of the attribute with the file
   * @param _fileName   name of file where to check out
   */
/*  public void jsFunction_checkout(String _attrName, String _fileName) throws Exception  {
    Checkout checkout = new Checkout(Shell.getContext(), this.instance, _attrName);

    File file =new File(_fileName);
    checkout.process(Shell.getContext(), new FileOutputStream(file));
  }
*/

private Map<Attribute,Object> attributes = new HashMap<Attribute,Object>();


private static Map<String,Object> basics = new HashMap<String,Object>();

}
