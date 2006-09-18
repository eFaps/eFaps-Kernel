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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.shell.method.update.user;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXException;

import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.shell.method.update.AbstractUpdate;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class JAASSystemUpdate extends AbstractUpdate  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(JAASSystemUpdate.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * All definitions of versions are added to this list.
   */
  private List < Definition > definitions = new ArrayList < Definition > ();
 
  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  public JAASSystemUpdate() {
    super("Admin_User_JAASSystem");
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Adds one definition of a update for a specific version to all definitions
   * in {@link #definitions}.
   *
   * @param _definition definition to add
   * @see #definitions
   */
  public void addDefinition(final Definition _definition)  {
    this.definitions.add(_definition);
  }

  public void updateInDB() throws EFapsException,Exception {
    Context context = Context.getThreadContext();

    Instance instance = getInstance();
    for (Definition def : this.definitions)  {
      Update update = new Update(context, instance);
      update.add(context, "Revision", def.globalVersion  
                                      + "#" + def.localVersion);
      for (Map.Entry < String, String > entry : def.values.entrySet())  {
        update.add(context, entry.getKey(), entry.getValue());
      }
      update.executeWithoutAccessCheck();
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * Returns a string representation with values of all instance variables.
   *
   * @return string representation of this access type update
   */
  public String toString()  {
    return new ToStringBuilder(this).
      appendSuper(super.toString()).
      append("definitions",     this.definitions).
      toString();
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  public static JAASSystemUpdate readXMLFile(final String _fileName) throws IOException  {
//    } catch (IOException e)  {
//      LOG.error("could not open file '" + _fileName + "'", e);
    return readXMLFile(new File(_fileName));
  }

  public static JAASSystemUpdate readXMLFile(final File _file) throws IOException  {
    JAASSystemUpdate ret = null;

    try  {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("user-jaassystem", JAASSystemUpdate.class);

      digester.addCallMethod("user-jaassystem/uuid", "setUUID", 1);
      digester.addCallParam("user-jaassystem/uuid", 0);

      digester.addObjectCreate("user-jaassystem/definition", Definition.class);
      digester.addSetNext("user-jaassystem/definition", "addDefinition");

      digester.addCallMethod("user-jaassystem/definition/version", "setVersion", 4);
      digester.addCallParam("user-jaassystem/definition/version/application", 0);
      digester.addCallParam("user-jaassystem/definition/version/global", 1);
      digester.addCallParam("user-jaassystem/definition/version/local", 2);
      digester.addCallParam("user-jaassystem/definition/version/mode", 3);
      
      digester.addCallMethod("user-jaassystem/definition/name", "setName", 1);
      digester.addCallParam("user-jaassystem/definition/name", 0);

      digester.addCallMethod("user-jaassystem/definition/person/classname", "setPersonClassName", 1);
      digester.addCallParam("user-jaassystem/definition/person/classname", 0);

      digester.addCallMethod("user-jaassystem/definition/person/name-method", "setPersonNameMethod", 1);
      digester.addCallParam("user-jaassystem/definition/person/name-method", 0);

      digester.addCallMethod("user-jaassystem/definition/person/key-method", "setPersonKeyMethod", 1);
      digester.addCallParam("user-jaassystem/definition/person/key-method", 0);

      digester.addCallMethod("user-jaassystem/definition/person/firstname-method", "setPersonFirstNameMethod", 1);
      digester.addCallParam("user-jaassystem/definition/person/firstname-method", 0);

      digester.addCallMethod("user-jaassystem/definition/person/lastname-method", "setPersonLastNameMethod", 1);
      digester.addCallParam("user-jaassystem/definition/person/lastname-method", 0);

      digester.addCallMethod("user-jaassystem/definition/person/email-method", "setPersonEmailMethod", 1);
      digester.addCallParam("user-jaassystem/definition/person/email-method", 0);

      digester.addCallMethod("user-jaassystem/definition/person/organisation-method", "setPersonOrganisationMethod", 1);
      digester.addCallParam("user-jaassystem/definition/person/organisation-method", 0);

      digester.addCallMethod("user-jaassystem/definition/person/url-method", "setPersonUrlMethod", 1);
      digester.addCallParam("user-jaassystem/definition/person/url-method", 0);

      digester.addCallMethod("user-jaassystem/definition/person/phone-method", "setPersonPhoneMethod", 1);
      digester.addCallParam("user-jaassystem/definition/person/phone-method", 0);

      digester.addCallMethod("user-jaassystem/definition/person/mobile-method", "setPersonMobileMethod", 1);
      digester.addCallParam("user-jaassystem/definition/person/mobile-method", 0);

      digester.addCallMethod("user-jaassystem/definition/person/fax-method", "setPersonFaxMethod", 1);
      digester.addCallParam("user-jaassystem/definition/person/fax-method", 0);

      digester.addCallMethod("user-jaassystem/definition/role/classname", "setRoleClassName", 1);
      digester.addCallParam("user-jaassystem/definition/role/classname", 0);

      digester.addCallMethod("user-jaassystem/definition/role/key-method", "setRoleKeyMethod", 1);
      digester.addCallParam("user-jaassystem/definition/role/key-method", 0);

      digester.addCallMethod("user-jaassystem/definition/group/classname", "setGroupClassName", 1);
      digester.addCallParam("user-jaassystem/definition/gorup/classname", 0);

      digester.addCallMethod("user-jaassystem/definition/group/key-method", "setGroupKeyMethod", 1);
      digester.addCallParam("user-jaassystem/definition/group/key-method", 0);

      digester.addCallMethod("user-jaassystem/definition/assigned-role", "assignRole", 2);
      digester.addCallParam("user-jaassystem/definition/assigned-role/name", 0);
      digester.addCallParam("user-jaassystem/definition/assigned-role/key", 1);

      ret = (JAASSystemUpdate) digester.parse(_file);
    } catch (SAXException e)  {
e.printStackTrace();
      //      LOG.error("could not read file '" + _fileName + "'", e);
    }
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class Definition  {
    
    ///////////////////////////////////////////////////////////////////////////
    // instance variables

    /**
     * Name of the application for which this definition is defined.
     *
     * @see #setVersion
     */
    private String application = null;
     
    /**
     * Number of the global version of the application.
     *
     * @see #setVersion
     */
    private long globalVersion = 0;

    /**
     * Text of the local version of this definition.
     *
     * @see #setVersion
     */
    private String localVersion = null;

    /**
     * 
     *
     * @see #setVersion
     */
    private String mode = null;

    /**
     * Name of the access set for which this definition is defined.
     */
    private Map < String, String > values = new HashMap < String, String > ();

    ///////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     * The version information of this defintion is set.
     *
     * @param _application    name of the application for which the version is 
     *                        defined
     * @param _globalVersion  global version
     */
    public void setVersion(final String _application, 
                           final String _globalVersion,
                           final String _localVersion,
                           final String _mode)  {
      this.application = _application;
      this.globalVersion = Long.valueOf(_globalVersion);
      this.localVersion = _localVersion;
      this.mode = _mode;
    }
    
    /**
     * 
     * @see #values
     */
    public void setName(final String _name)  {
      this.values.put("Name", _name);
    }
    
    /**
     * 
     * @see #values
     */
    public void setPersonClassName(final String _value)  {
      this.values.put("ClassNamePerson", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonKeyMethod(final String _value)  {
      this.values.put("MethodNamePersonKey", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonNameMethod(final String _value)  {
      this.values.put("MethodNamePersonName", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonFirstNameMethod(final String _value)  {
      this.values.put("MethodNamePersonFirstName", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonLastNameMethod(final String _value)  {
      this.values.put("MethodNamePersonLastName", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonEmailMethod(final String _value)  {
      this.values.put("MethodNamePersonEmail", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonOrganisationMethod(final String _value)  {
      this.values.put("MethodNamePersonOrganisation", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonUrlMethod(final String _value)  {
      this.values.put("MethodNamePersonUrl", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonPhoneMethod(final String _value)  {
      this.values.put("MethodNamePersonPhone", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonMobileMethod(final String _value)  {
      this.values.put("MethodNamePersonMobile", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonFaxMethod(final String _value)  {
      this.values.put("MethodNamePersonFax", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setRoleClassName(final String _value)  {
      this.values.put("ClassNameRole", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setRoleKeyMethod(final String _value)  {
      this.values.put("MethodNameRoleKey", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setGroupClassName(final String _value)  {
      this.values.put("ClassNameGroup", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setGroupKeyMethod(final String _value)  {
      this.values.put("MethodNameGroupKey", _value);
    }

    /**
     * Returns a string representation with values of all instance variables
     * of a definition.
     *
     * @return string representation of this definition of an access type 
     *         update
     */
    public String toString()  {
      return new ToStringBuilder(this).
        append("application",     this.application).
        append("global version",  this.globalVersion).
        append("local version",   this.localVersion).
        append("mode",            this.mode).
        append("values",          this.values).
        toString();
    }
  }
}
