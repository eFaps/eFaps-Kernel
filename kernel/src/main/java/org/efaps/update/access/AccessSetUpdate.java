/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.update.access;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class AccessSetUpdate extends AbstractUpdate
{
  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /** Link to access types. */
  private final static Link LINK2ACCESSTYPE
                    = new Link("Admin_Access_AccessSet2Type",
                               "AccessSetLink",
                               "Admin_Access_AccessType", "AccessTypeLink");

  /** Link to data model types. */
  private final static Link LINK2DATAMODELTYPE
                    = new Link("Admin_Access_AccessSet2DataModelType",
                               "AccessSetLink",
                               "Admin_DataModel_Type", "DataModelTypeLink");

  /** Link to persons. */
  private final static Link LINK2PERSON
                    = new Link("Admin_Access_AccessSet2UserAbstract",
                               "AccessSetLink",
                               "Admin_User_Person", "UserAbstractLink");

  /** Link to roles. */
  private final static Link LINK2ROLE
                    = new Link("Admin_Access_AccessSet2UserAbstract",
                               "AccessSetLink",
                               "Admin_User_Role", "UserAbstractLink");

  /** Link to groups. */
  private final static Link LINK2GROUP
                    = new Link("Admin_Access_AccessSet2UserAbstract",
                               "AccessSetLink",
                               "Admin_User_Group", "UserAbstractLink");

  private final static Set <Link> ALLLINKS = new HashSet < Link > ();
  static {
    ALLLINKS.add(LINK2ACCESSTYPE);
    ALLLINKS.add(LINK2DATAMODELTYPE);
    ALLLINKS.add(LINK2PERSON);
    ALLLINKS.add(LINK2ROLE);
    ALLLINKS.add(LINK2GROUP);
  }

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   * @param _url        URL of the file
   */
  public AccessSetUpdate(final URL _url)
  {
    super(_url, "Admin_Access_AccessSet", ALLLINKS);
  }

  /**
   * Creates new instance of class {@link Definition}.
   *
   * @return new definition instance
   * @see Definition
   */
  @Override
  protected AbstractDefinition newDefinition()
  {
    return new Definition();
  }

  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  private class Definition extends AbstractDefinition {

    @Override
    protected void readXML(final List<String> _tags,
                           final Map<String,String> _attributes,
                           final String _text)
    {
      final String value = _tags.get(0);
      if ("access-type".equals(value))  {
        addLink(LINK2ACCESSTYPE, new LinkInstance(_text));
      } else if ("type".equals(value))  {
        addLink(LINK2DATAMODELTYPE, new LinkInstance(_text));
      } else if ("group".equals(value))  {
        addLink(LINK2GROUP, new LinkInstance(_text));
      } else if ("person".equals(value))  {
        addLink(LINK2PERSON, new LinkInstance(_text));
      } else if ("role".equals(value))  {
        addLink(LINK2ROLE, new LinkInstance(_text));
      } else  {
        super.readXML(_tags, _attributes, _text);
      }
    }
  }
}
