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

package org.efaps.update.schema.common;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.common.MsgPhrase;
import org.efaps.admin.common.MsgPhrase.Argument;
import org.efaps.admin.common.MsgPhrase.Label;
import org.efaps.admin.user.Company;
import org.efaps.ci.CIAdmin;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the import / update of system configurations for eFaps read from a
 * XML configuration item file.
 *
 * @author The eFaps Team
 * @version $Id: MsgPhraseUpdate.java 14409 2014-11-11 03:50:45Z jan@moxter.net
 *          $
 */
public class MsgPhraseUpdate
    extends AbstractUpdate
{

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MsgPhraseUpdate.class);

    /**
     * Default constructor to initialize this system configuration update
     * instance for given <code>_url</code>.
     *
     * @param _url URL of the file
     */
    public MsgPhraseUpdate(final InstallFile _installFile)
    {
        super(_installFile, "Admin_Common_MsgPhrase");
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

    /**
     * Handles the definition of one version for an system configuration defined
     * within XML configuration item file.
     */
    public class Definition
        extends AbstractDefinition
    {

        private String parent = null;

        private final Set<MsgPhrase.Label> labels = new HashSet<>();

        private final Set<MsgPhrase.Argument> arguments = new HashSet<>();

        /**
         * @param _tags current path as list of single tags
         * @param _attributes attributes for current path
         * @param _text content for current path
         * @throws EFapsException on error
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
        {
            final String value = _tags.get(0);
            switch (value) {
                case "parent":
                    this.parent = _text;
                    break;
                case "label":
                    addLabel(_text, _attributes.get("language"), _attributes.get("company"));
                    break;
                case "argument":
                    addArgument(_text, _attributes.get("index"), _attributes.get("language"),
                                    _attributes.get("company"));
                    break;
                default:
                    super.readXML(_tags, _attributes, _text);
                    break;
            }
        }

        /**
         * @param _text
         * @param _string
         * @param _string2
         * @param _string3
         */
        private void addArgument(final String _value,
                                 final String _index,
                                 final String _language,
                                 final String _company)
            throws CacheReloadException
        {
            final MsgPhrase.Argument argument = new MsgPhrase.Argument();
            argument.setValue(_value);
            argument.setIndex(Integer.valueOf(_index));
            if (_company != null) {
                final Company company = Company.get(UUID.fromString(_company));
                if (company != null) {
                    argument.setCompanyId(company.getId());
                }
            }
            if (_language != null) {
                argument.setLanguageId(getLanguageId(_language));
            }
            this.arguments.add(argument);
        }

        private Long getLanguageId(final String _language)
        {
            Long ret = null;
            try {
                final QueryBuilder queryBldr = new QueryBuilder(CIAdmin.Language);
                queryBldr.addWhereAttrEqValue(CIAdmin.Language.Language, _language);
                final InstanceQuery query = queryBldr.getQuery();
                query.executeWithoutAccessCheck();
                if (query.next()) {
                    ret = query.getCurrentValue().getId();
                }
            } catch (final EFapsException e) {
                LOG.error("getLanguageId()", e);
            }
            return ret;
        }

        /**
         * @param _text
         * @param _string
         * @param _string2
         */
        private void addLabel(final String _value,
                              final String _language,
                              final String _company)
            throws CacheReloadException
        {
            final MsgPhrase.Label label = new MsgPhrase.Label();
            label.setValue(_value);
            if (_company != null) {
                final Company company = Company.get(UUID.fromString(_company));
                if (company != null) {
                    label.setCompanyId(company.getId());
                }
            }
            if (_language != null) {
                label.setLanguageId(getLanguageId(_language));
            }
            this.labels.add(label);
        }

        /**
         * If the current life cycle <code>step</code> is
         * {@link UpdateLifecycle#EFAPS_UPDATE EFAPS_UPDATE}, the
         * {@link #attributes} are updated.
         *
         * @param _step current life cycle update step
         * @param _allLinkTypes all link types to update
         * @throws InstallationException if update failed
         * @see #attributes
         */
        @Override
        public void updateInDB(final UpdateLifecycle _step,
                               final Set<Link> _allLinkTypes)
            throws InstallationException
        {
            try {
                if (_step == UpdateLifecycle.EFAPS_UPDATE) {
                    // set the id of the parent (if defined)
                    if (this.parent != null && this.parent.length() > 0) {
                        final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.MsgPhrase);
                        queryBldr.addWhereAttrEqValue(CIAdminCommon.MsgPhrase.Name, this.parent);
                        final InstanceQuery query = queryBldr.getQuery();
                        query.executeWithoutAccessCheck();
                        if (query.next()) {
                            final Instance instance = query.getCurrentValue();
                            addValue(CIAdminCommon.MsgPhrase.ParentLink.name, "" + instance.getId());
                        } else {
                            addValue(CIAdminCommon.MsgPhrase.ParentLink.name, null);
                        }
                    } else {
                        addValue(CIAdminCommon.MsgPhrase.ParentLink.name, null);
                    }
                }

                super.updateInDB(_step, _allLinkTypes);

                if (_step == UpdateLifecycle.EFAPS_UPDATE && getInstance() != null && getInstance().isValid()) {
                    final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.MsgPhraseConfigAbstract);
                    queryBldr.addWhereAttrEqValue(CIAdminCommon.MsgPhraseConfigAbstract.AbstractLink, getInstance());
                    final InstanceQuery query = queryBldr.getQuery();
                    query.executeWithoutAccessCheck();
                    while (query.next()) {
                        new Delete(query.getCurrentValue()).executeWithoutAccessCheck();
                    }

                    for (final Label label : this.labels) {
                        final Insert insert = new Insert(CIAdminCommon.MsgPhraseLabel);
                        insert.add(CIAdminCommon.MsgPhraseLabel.AbstractLink, getInstance());
                        insert.add(CIAdminCommon.MsgPhraseLabel.Value, label.getValue());
                        insert.add(CIAdminCommon.MsgPhraseLabel.CompanyLink, label.getCompanyId());
                        insert.add(CIAdminCommon.MsgPhraseLabel.LanguageLink, label.getLanguageId());
                        insert.executeWithoutAccessCheck();
                    }

                    for (final Argument argument : this.arguments) {
                        final Insert insert = new Insert(CIAdminCommon.MsgPhraseArgument);
                        insert.add(CIAdminCommon.MsgPhraseArgument.AbstractLink, getInstance());
                        insert.add(CIAdminCommon.MsgPhraseArgument.Value, argument.getValue());
                        insert.add(CIAdminCommon.MsgPhraseArgument.Index, argument.getIndex());
                        insert.add(CIAdminCommon.MsgPhraseArgument.CompanyLink, argument.getCompanyId());
                        insert.add(CIAdminCommon.MsgPhraseArgument.LanguageLink, argument.getLanguageId());
                        insert.executeWithoutAccessCheck();
                    }
                }
            } catch (final EFapsException e) {
                throw new InstallationException("update did not work", e);
            }
        }
    }
}
