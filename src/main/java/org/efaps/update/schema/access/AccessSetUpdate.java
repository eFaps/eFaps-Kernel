/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.update.schema.access;


import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class AccessSetUpdate
    extends AbstractUpdate
{
    /** Link to access types. */
    private static final Link LINK2ACCESSTYPE = new Link("Admin_Access_AccessSet2Type", "AccessSetLink",
                                                         "Admin_Access_AccessType", "AccessTypeLink");

    /** Link to data model types. */
    private static final Link LINK2DATAMODELTYPE = new Link("Admin_Access_AccessSet2DataModelType", "AccessSetLink",
                                                            "Admin_DataModel_Type", "DataModelTypeLink");

    /** Link to Status. */
    private static final Link LINK2STATUS = new Link("Admin_Access_AccessSet2Status", "AccessSetLink",
                                                     "Admin_DataModel_StatusAbstract",
                                                     "SatusLink", "Type", "Key").setIncludeChildTypes(true);

    /** Link to persons. */
    private static final Link LINK2PERSON = new Link("Admin_Access_AccessSet2UserAbstract", "AccessSetLink",
                                                     "Admin_User_Person", "UserAbstractLink");

    /** Link to roles. */
    private static final Link LINK2ROLE = new Link("Admin_Access_AccessSet2UserAbstract", "AccessSetLink",
                                                   "Admin_User_RoleAbstract", "UserAbstractLink");

    /** Link to groups. */
    private static final Link LINK2GROUP = new Link("Admin_Access_AccessSet2UserAbstract", "AccessSetLink",
                                                    "Admin_User_Group", "UserAbstractLink");

    /**
     * Map of all links.
     */
    private static final Set<Link> ALLLINKS = new HashSet<Link>();
    static {
        AccessSetUpdate.ALLLINKS.add(AccessSetUpdate.LINK2ACCESSTYPE);
        AccessSetUpdate.ALLLINKS.add(AccessSetUpdate.LINK2DATAMODELTYPE);
        AccessSetUpdate.ALLLINKS.add(AccessSetUpdate.LINK2STATUS);
        AccessSetUpdate.ALLLINKS.add(AccessSetUpdate.LINK2PERSON);
        AccessSetUpdate.ALLLINKS.add(AccessSetUpdate.LINK2ROLE);
        AccessSetUpdate.ALLLINKS.add(AccessSetUpdate.LINK2GROUP);
    }

    /**
     *
     * @param _url URL of the file
     */
    public AccessSetUpdate(final URL _url)
    {
        super(_url, "Admin_Access_AccessSet", AccessSetUpdate.ALLLINKS);
    }

    /**
     * Creates new instance of class {@link AccessSetUpdate.Definition}.
     *
     * @return new definition instance
     * @see AccessSetUpdate.Definition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new Definition();
    }

    /**
     * Definition.
     */
    private class Definition extends AbstractDefinition
    {
        /**
         * Name of the current status group.
         */
        private String currentGroupName;

        /**
         * {@inheritDoc}
         * @throws EFapsException
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
        {
            final String value = _tags.get(0);
            if ("access-type".equals(value)) {
                addLink(AccessSetUpdate.LINK2ACCESSTYPE, new LinkInstance(_text));
            } else if ("type".equals(value)) {
                addLink(AccessSetUpdate.LINK2DATAMODELTYPE, new LinkInstance(_text));
            } else if ("group".equals(value)) {
                addLink(AccessSetUpdate.LINK2GROUP, new LinkInstance(_text));
            } else if ("person".equals(value)) {
                addLink(AccessSetUpdate.LINK2PERSON, new LinkInstance(_text));
            } else if ("role".equals(value)) {
                addLink(AccessSetUpdate.LINK2ROLE, new LinkInstance(_text));
            } else if ("status".equals(value)) {
                if (_tags.size() == 1) {
                    this.currentGroupName = _attributes.get("group");
                } else if ((_tags.size() == 2) && "key".equals(_tags.get(1))) {
                    final LinkInstance linkinstance = new LinkInstance();
                    linkinstance.getKeyAttr2Value().put("Key", _text);
                    linkinstance.getKeyAttr2Value().put("Type", this.currentGroupName);
                    addLink(AccessSetUpdate.LINK2STATUS, linkinstance);
                }
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void setLinksInDB(final Instance _instance,
                                    final Link _linktype,
                                    final Set<LinkInstance> _links)
            throws EFapsException
        {
            if (_links != null) {
                for (final LinkInstance linkInst : _links) {
                    if (linkInst.getKeyAttr2Value().containsKey("Type")) {
                        final String typeName = linkInst.getKeyAttr2Value().get("Type");
                        linkInst.getKeyAttr2Value().put("Type", ((Long) Type.get(typeName).getId()).toString());
                    }
                }
            }
            super.setLinksInDB(_instance, _linktype, _links);
        }
    }
}
