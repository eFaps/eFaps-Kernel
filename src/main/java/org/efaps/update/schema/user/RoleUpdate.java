/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.update.schema.user;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.db.Instance;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.LinkInstance;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 */
public class RoleUpdate
    extends AbstractUpdate
{
    /**
     * Set of all links used by commands.
     */
    private static final Set<Link> ALLLINKS = new HashSet<>();

    /** Link from UI object to role. */
    private static final Link LINK2ACCESSCMD = new Link("Admin_UI_Access", "UserLink",
                    "Admin_UI_Command", "UILink").setIncludeChildTypes(true);

    /** Link from AccessSet to roles. */
    private static final Link LINK2ACCESSSET = new Link("Admin_Access_AccessSet2UserAbstract", "UserAbstractLink",
                                                   "Admin_Access_AccessSet", "AccessSetLink");

    static {
        RoleUpdate.ALLLINKS.add(RoleUpdate.LINK2ACCESSCMD);
        RoleUpdate.ALLLINKS.add(RoleUpdate.LINK2ACCESSSET);
    }

    /**
     * Global type or local.
     */
    private boolean global = true;

    /**
     * Instantiates a new role update.
     *
     * @param _installFile the install file
     */
    public RoleUpdate(final InstallFile _installFile)
    {
        super(_installFile, "temp", RoleUpdate.ALLLINKS);
    }

    @Override
    public String getDataModelTypeName()
    {
        return this.global ? "Admin_User_RoleGlobal" : "Admin_User_RoleLocal";
    }

    /**
     * Creates new instance of class {@link RoleDefinition}.
     *
     * @return new definition instance
     * @see RoleDefinition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new RoleDefinition();
    }

    /**
     *
     */
    public class RoleDefinition
        extends AbstractDefinition
    {

        /**
         * Will the access be set or not.
         */
        private boolean setAccess = false;

        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
        {
            final String value = _tags.get(0);
            if ("status".equals(value)) {
                addValue("Status", _text);
            } else if ("global".equals(value)) {
                RoleUpdate.this.global = Boolean.valueOf(_text);
            } else if ("access".equals(value)) {
                this.setAccess = true;
                if (_tags.size() > 2) {
                    final String subValue1 = _tags.get(1);
                    if ("userinterface".equals(subValue1)) {
                        final String subValue2 = _tags.get(2);
                        if ("cmd".equals(subValue2)) {
                            addLink(RoleUpdate.LINK2ACCESSCMD, new LinkInstance(_text));
                        } else {
                            super.readXML(_tags, _attributes, _text);
                        }
                    } else if ("datamodel".equals(subValue1)) {
                        final String subValue2 = _tags.get(2);
                        if ("accessset".equals(subValue2)) {
                            addLink(RoleUpdate.LINK2ACCESSSET, new LinkInstance(_text));
                        }
                    } else {
                        super.readXML(_tags, _attributes, _text);
                    }
                }
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }

        @Override
        protected void setLinksInDB(final Instance _instance,
                                    final Link _linktype,
                                    final Set<LinkInstance> _links)
            throws EFapsException
        {
            if (this.setAccess) {
                super.setLinksInDB(_instance, _linktype, _links);
            }
        }
    }
}
