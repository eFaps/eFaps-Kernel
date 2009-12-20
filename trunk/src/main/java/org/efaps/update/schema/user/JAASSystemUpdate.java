/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.update.schema.user;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.db.Insert;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 * TODO:  description
 */
public class JAASSystemUpdate
    extends AbstractUpdate
{
    /** Link from JAAS systems to persons. */
    private static final Link LINK2PERSONS = new Link("Admin_User_JAASKey",
                                                      "JAASSystemLink",
                                                      "Admin_User_Person", "UserLink");

    /** Link from JAAS systems to roles. */
    private static final Link LINK2ROLES = new Link("Admin_User_JAASKey",
                                                    "JAASSystemLink",
                                                    "Admin_User_Role", "UserLink");

    /** Link from JAAS systems to groups. */
    private static final Link LINK2GROUPS = new Link("Admin_User_JAASKey",
                                                     "JAASSystemLink",
                                                     "Admin_User_Group", "UserLink");

    private static final Set <Link> ALLLINKS = new HashSet < Link > ();
    static {
        JAASSystemUpdate.ALLLINKS.add(JAASSystemUpdate.LINK2PERSONS);
        JAASSystemUpdate.ALLLINKS.add(JAASSystemUpdate.LINK2ROLES);
        JAASSystemUpdate.ALLLINKS.add(JAASSystemUpdate.LINK2GROUPS);
    }

    /**
     *
     * @param _url        URL of the file
     */
    public JAASSystemUpdate(final URL _url)
    {
        super(_url, "Admin_User_JAASSystem", JAASSystemUpdate.ALLLINKS);
    }

    /**
     * Creates new instance of class {@link JAASSystemUpdate.Definition}.
     *
     * @return new definition instance
     * @see JAASSystemUpdate.Definition
     */
    @Override()
    protected AbstractDefinition newDefinition()
    {
        return new Definition();
    }

    private class Definition
        extends AbstractDefinition
    {

        @Override()
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("assigned-group".equals(value))  {
                addLink(JAASSystemUpdate.LINK2GROUPS, new LinkInstance(_text, "Key", _attributes.get("key")));
            } else if ("assigned-person".equals(value))  {
                addLink(JAASSystemUpdate.LINK2PERSONS, new LinkInstance(_text, "Key", _attributes.get("key")));
            } else if ("assigned-role".equals(value))  {
                addLink(JAASSystemUpdate.LINK2ROLES, new LinkInstance(_text, "Key", _attributes.get("key")));
            } else if ("group".equals(value))  {
                if (_tags.size() > 1)  {
                    final String subValue = _tags.get(1);
                    if ("classname".equals(subValue))  {
                        addValue("ClassNameGroup", _text);
                    } else if ("key-method".equals(subValue))  {
                        addValue("MethodNameGroupKey", _text);
                    }
                }
            } else if ("person".equals(value))  {
                if (_tags.size() > 1)  {
                    final String subValue = _tags.get(1);
                    if ("classname".equals(subValue))  {
                        addValue("ClassNamePerson", _text);
                    } else if ("email-method".equals(subValue))  {
                        addValue("MethodNamePersonEmail", _text);
                    } else if ("firstname-method".equals(subValue))  {
                        addValue("MethodNamePersonFirstName", _text);
                    } else if ("key-method".equals(subValue))  {
                        addValue("MethodNamePersonKey", _text);
                    } else if ("lastname-method".equals(subValue))  {
                        addValue("MethodNamePersonLastName", _text);
                    } else if ("name-method".equals(subValue))  {
                        addValue("MethodNamePersonName", _text);
                    }
                }
            } else if ("role".equals(value))  {
                if (_tags.size() > 1)  {
                    final String subValue = _tags.get(1);
                    if ("classname".equals(subValue))  {
                        addValue("ClassNameRole", _text);
                    } else if ("key-method".equals(subValue))  {
                        addValue("MethodNameRoleKey", _text);
                    }
                }
            } else  {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * Because the attributes 'ClassNamePerson', 'MethodNamePersonKey' and
         * 'MethodNamePersonName' of the JAAS system are required attributes,
         * all attribute values of this attributes are set.
         *
         * @param _insert  insert instance
         * @throws InstallationException if insert failed
         */
        @Override()
        protected void createInDB(final Insert _insert)
            throws InstallationException
        {
            try {
                _insert.add("ClassNamePerson",      getValue("ClassNamePerson"));
            } catch (final EFapsException e) {
                throw new InstallationException("Attribute 'ClassNamePerson' for person could not be defined", e);
            }
            try {
                _insert.add("MethodNamePersonKey",  getValue("MethodNamePersonKey"));
            } catch (final EFapsException e) {
                throw new InstallationException("Attribute 'MethodNamePersonKey' for person could not be defined", e);
            }
            try {
                _insert.add("MethodNamePersonName", getValue("MethodNamePersonName"));
            } catch (final EFapsException e) {
                throw new InstallationException("Attribute 'MethodNamePersonName' for person could not be defined", e);
            }
            super.createInDB(_insert);
        }
    }
}
