/*
 * Copyright 2003-2007 The eFaps Team
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

package org.efaps.update.user;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.efaps.update.AbstractUpdate;

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
  private final static Logger LOG = LoggerFactory.getLogger(JAASSystemUpdate.class);

  /** Link from JAAS systems to persons */
  private final static Link LINK2PERSONS
                    = new Link("Admin_User_JAASKey", 
                               "JAASSystemLink", 
                               "Admin_User_Person", "UserLink");

  /** Link from JAAS systems to roles */
  private final static Link LINK2ROLES
                    = new Link("Admin_User_JAASKey", 
                               "JAASSystemLink", 
                               "Admin_User_Role", "UserLink");

  /** Link from JAAS systems to groups */
  private final static Link LINK2GROUPS
                    = new Link("Admin_User_JAASKey", 
                               "JAASSystemLink", 
                               "Admin_User_Group", "UserLink");

  private final static Set <Link> ALLLINKS = new HashSet < Link > ();  {
    ALLLINKS.add(LINK2PERSONS);
    ALLLINKS.add(LINK2ROLES);
    ALLLINKS.add(LINK2GROUPS);
  }

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  public JAASSystemUpdate() {
    super("Admin_User_JAASSystem", ALLLINKS);
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  public static JAASSystemUpdate readXMLFile(final URL _url)  {
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
      digester.addCallParam("user-jaassystem/definition/group/classname", 0);

      digester.addCallMethod("user-jaassystem/definition/group/key-method", "setGroupKeyMethod", 1);
      digester.addCallParam("user-jaassystem/definition/group/key-method", 0);

      digester.addCallMethod("user-jaassystem/definition/assigned-person", "assignPerson", 2);
      digester.addCallParam("user-jaassystem/definition/assigned-person/name", 0);
      digester.addCallParam("user-jaassystem/definition/assigned-person/key", 1);

      digester.addCallMethod("user-jaassystem/definition/assigned-role", "assignRole", 2);
      digester.addCallParam("user-jaassystem/definition/assigned-role/name", 0);
      digester.addCallParam("user-jaassystem/definition/assigned-role/key", 1);

      digester.addCallMethod("user-jaassystem/definition/assigned-group", "assignGroup", 2);
      digester.addCallParam("user-jaassystem/definition/assigned-group/name", 0);
      digester.addCallParam("user-jaassystem/definition/assigned-group/key", 1);

      ret = (JAASSystemUpdate) digester.parse(_url);

      if (ret != null)  {
        ret.setURL(_url);
      }
    } catch (IOException e) {
      LOG.error(_url.toString() + " is not readable", e);
    } catch (SAXException e) {
      LOG.error(_url.toString() + " seems to be invalide XML", e);
    }
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class Definition extends DefinitionAbstract  {
    
    /**
     *
     */
     public void assignPerson(final String _name, final String _key)  {
       addLink(LINK2PERSONS, _name, "Key", _key);
     }

    /**
     *
     */
     public void assignRole(final String _name, final String _key)  {
       addLink(LINK2ROLES, _name, "Key", _key);
     }

         /**
     *
     */
     public void assignGroup(final String _name, final String _key)  {
       addLink(LINK2GROUPS, _name, "Key", _key);
     }

     /**
     * 
     * @see #values
     */
    public void setPersonClassName(final String _value)  {
      addValue("ClassNamePerson", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonKeyMethod(final String _value)  {
      addValue("MethodNamePersonKey", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonNameMethod(final String _value)  {
      addValue("MethodNamePersonName", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonFirstNameMethod(final String _value)  {
      addValue("MethodNamePersonFirstName", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonLastNameMethod(final String _value)  {
      addValue("MethodNamePersonLastName", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonEmailMethod(final String _value)  {
      addValue("MethodNamePersonEmail", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonOrganisationMethod(final String _value)  {
      addValue("MethodNamePersonOrganisation", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonUrlMethod(final String _value)  {
      addValue("MethodNamePersonUrl", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonPhoneMethod(final String _value)  {
      addValue("MethodNamePersonPhone", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonMobileMethod(final String _value)  {
      addValue("MethodNamePersonMobile", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setPersonFaxMethod(final String _value)  {
      addValue("MethodNamePersonFax", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setRoleClassName(final String _value)  {
      addValue("ClassNameRole", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setRoleKeyMethod(final String _value)  {
      addValue("MethodNameRoleKey", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setGroupClassName(final String _value)  {
      addValue("ClassNameGroup", _value);
    }

    /**
     * 
     * @see #values
     */
    public void setGroupKeyMethod(final String _value)  {
      addValue("MethodNameGroupKey", _value);
    }

  }
}
