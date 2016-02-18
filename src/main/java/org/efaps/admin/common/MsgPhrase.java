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

package org.efaps.admin.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.text.ExtendedMessageFormat;
import org.efaps.admin.user.Company;
import org.efaps.ci.CIAdmin;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Context;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.efaps.util.MsgFormat;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class MsgPhrase
    implements CacheObjectInterface, Serializable
{

    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MsgPhrase.class);

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE = MsgPhrase.class.getName() + ".UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = MsgPhrase.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = MsgPhrase.class.getName() + ".Name";

    /**
     * Name of the Cache by Name.
     */
    private static final String ARGUMENTCACHE = MsgPhrase.class.getName() + ".Argument";

    /**
     * Name of the Cache by Name.
     */
    private static final String LABELCACHE = MsgPhrase.class.getName() + ".Label";

    /**
     * The instance variable stores the id of this SystemAttribute.
     *
     * @see #getId()
     */
    private final long id;

    /**
     * The instance variable stores the UUID of this SystemAttribute.
     *
     * @see #getUUID()
     */
    private final UUID uuid;

    /**
     * The instance variable stores the Name of this SystemAttribute.
     *
     * @see #getName()
     */
    private final String name;

    /**
     * Labels.
     */
    private final Set<Label> labels = new HashSet<>();

    /**
     * Arguments.
     */
    private final Set<Argument> arguments = new HashSet<>();

    /**
     * Id fo the parent.
     */
    private final Long parentId;

    /**
     * Constructor setting instance variables.
     *
     * @param _id id of the MsgPhrase
     * @param _uuid uuid of the MsgPhrase
     * @param _name name of the MsgPhrase
     * @param _parentId if ot the parent
     */
    private MsgPhrase(final long _id,
                      final String _name,
                      final String _uuid,
                      final Long _parentId)
    {
        this.id = _id;
        this.uuid = UUID.fromString(_uuid);
        this.name = _name;
        this.parentId = _parentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getUUID()
    {
        return this.uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getId()
    {
        return this.id;
    }

    /**
     * load the phrase.
     * @throws EFapsException on error
     */
    private void load()
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.MsgPhraseConfigAbstract);
        queryBldr.addWhereAttrEqValue(CIAdminCommon.MsgPhraseConfigAbstract.AbstractLink, getId());
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CIAdminCommon.MsgPhraseConfigAbstract.CompanyLink,
                        CIAdminCommon.MsgPhraseConfigAbstract.LanguageLink,
                        CIAdminCommon.MsgPhraseConfigAbstract.Value,
                        CIAdminCommon.MsgPhraseConfigAbstract.Int1);
        multi.executeWithoutAccessCheck();
        while (multi.next()) {
            AbstractConfig conf = null;
            if (multi.getCurrentInstance().getType().isCIType(CIAdminCommon.MsgPhraseArgument)) {
                conf = new Argument();
                ((Argument) conf).setIndex(multi.<Integer>getAttribute(CIAdminCommon.MsgPhraseConfigAbstract.Int1));
                this.arguments.add((Argument) conf);
            } else if (multi.getCurrentInstance().getType().isCIType(CIAdminCommon.MsgPhraseLabel)) {
                conf = new Label();
                this.labels.add((Label) conf);
            }
            if (conf == null) {
                LOG.error("Wrong type: ", this);
            } else {
                conf.setCompanyId(multi.<Long>getAttribute(CIAdminCommon.MsgPhraseConfigAbstract.CompanyLink));
                conf.setLanguageId(multi.<Long>getAttribute(CIAdminCommon.MsgPhraseConfigAbstract.LanguageLink));
                conf.setValue(multi.<String>getAttribute(CIAdminCommon.MsgPhraseConfigAbstract.Value));
            }
        }
    }

    /**
     * @return the label
     * @throws EFapsException on error
     */
    public String getLabel()
        throws EFapsException
    {
        return getLabel(Context.getThreadContext().getLanguage(), Context.getThreadContext().getCompany());
    }

    /**
     * @param _language language
     * @param _company  Comany
     * @return the label
     * @throws EFapsException on error
     */
    public String getLabel(final String _language,
                           final Company _company)
        throws EFapsException
    {
        String ret = "empty";
        final Cache<String, String> cache = InfinispanCache.get().<String, String>getCache(
                        MsgPhrase.LABELCACHE);
        final String key = _language + "_" + _company.getId() + "_" + getUUID().toString();
        if (cache.containsKey(key)) {
            ret = cache.get(key);
        } else {
            long languageid = 0;
            final QueryBuilder queryBldr = new QueryBuilder(CIAdmin.Language);
            queryBldr.addWhereAttrEqValue(CIAdmin.Language.Language, _language);
            final InstanceQuery query = queryBldr.getQuery();
            query.executeWithoutAccessCheck();
            if (query.next()) {
                languageid = query.getCurrentValue().getId();
            }
            Label current = null;
            MsgPhrase phrase = this;
            while (phrase != null) {
                for (final Label label : phrase.labels) {
                    if (current == null) {
                        current = label;
                    } else {
                        if (label.getPriority(languageid, _company.getId()) > current.getPriority(languageid,
                                        _company.getId())) {
                            current = label;
                        }
                    }
                }
                phrase = phrase.getParent();
            }
            if (current != null) {
                ret = current.getValue();
                cache.put(key, ret);
            }
        }
        return ret;
    }

    /**
     * @return list of arguments
     * @throws EFapsException on error
     */
    public List<String> getArguments()
        throws EFapsException
    {
        return getArguments(Context.getThreadContext().getLanguage(), Context.getThreadContext().getCompany());
    }

     /**
     * @param _language Language
     * @param _company  Company
     * @return list of arguments
     * @throws EFapsException on error
     */
    public List<String> getArguments(final String _language,
                                     final Company _company)
        throws EFapsException
    {
        final List<String> ret = new ArrayList<>();
        final Cache<String, List<String>> cache = InfinispanCache.get().<String, List<String>>getCache(
                        MsgPhrase.ARGUMENTCACHE);
        final String key = _language + "_" + _company.getId() + "_" + getUUID().toString();
        if (cache.containsKey(key)) {
            ret.addAll(cache.get(key));
        } else {
            long languageid = 0;
            final QueryBuilder queryBldr = new QueryBuilder(CIAdmin.Language);
            queryBldr.addWhereAttrEqValue(CIAdmin.Language.Language, _language);
            final InstanceQuery query = queryBldr.getQuery();
            query.executeWithoutAccessCheck();
            if (query.next()) {
                languageid = query.getCurrentValue().getId();
            }
            final Map<Integer, Argument> orderMap = new TreeMap<>();

            MsgPhrase phrase = this;
            while (phrase != null) {
                for (final Argument argument : phrase.arguments) {
                    if (orderMap.containsKey(argument.getIndex())) {
                        final Argument current = orderMap.get(argument.getIndex());
                        if (argument.getPriority(languageid, _company.getId()) > current.getPriority(languageid,
                                        _company.getId())) {
                            orderMap.put(argument.getIndex(), argument);
                        }
                    } else {
                        orderMap.put(argument.getIndex(), argument);
                    }
                }
                phrase = phrase.getParent();
            }
            for (final Argument argument : orderMap.values()) {
                ret.add(argument.getValue());
            }
            cache.put(key, ret);
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #parentId}.
     *
     * @return value of instance variable {@link #parentId}
     */
    public Long getParentId()
    {
        return this.parentId;
    }

    /**
     * Getter method for the instance variable {@link #parentId}.
     *
     * @return value of instance variable {@link #parentId}
     */
    public boolean hasParent()
    {
        return getParentId() != null;
    }

    /**
     * Getter method for the instance variable {@link #parentId}.
     *
     * @return value of instance variable {@link #parentId}
     * @throws EFapsException on error
     */
    public MsgPhrase getParent()
        throws EFapsException
    {
        MsgPhrase ret;
        if (hasParent()) {
            ret = MsgPhrase.get(getParentId());
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * @param _object objects to be passed on to the Messageformatter
     * @return formatted String
     * @throws EFapsException on error
     */
    public String format(final Object... _object)
        throws EFapsException
    {
        return format(Context.getThreadContext().getLanguage(), Context.getThreadContext().getCompany(), _object);
    }

    /**
     * @param _language Language
     * @param _company  Company
     * @param _object objects to be passed on to the Messageformatter
     * @return formatted String
     * @throws EFapsException on error
     */
    public String format(final String _language,
                         final Company _company,
                         final Object... _object)
        throws EFapsException
    {
        final ExtendedMessageFormat frmt = MsgFormat.getFormat(getLabel(_language, _company));
        return frmt.format(_object);
    }

    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof MsgPhrase) {
            ret = ((MsgPhrase) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return Long.valueOf(getId()).intValue();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link MsgPhrase}.
     *
     * @param _id id of the system configuration
     * @return instance of class {@link MsgPhrase}
     * @throws EFapsException on error
     */
    public static MsgPhrase get(final long _id)
        throws EFapsException
    {
        final Cache<Long, MsgPhrase> cache = InfinispanCache.get().<Long, MsgPhrase>getCache(MsgPhrase.IDCACHE);
        if (!cache.containsKey(_id)) {
            MsgPhrase.loadMsgPhrase(_id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link MsgPhrase}.
     *
     * @param _name name of the system configuration
     * @return instance of class {@link MsgPhrase}
     * @throws EFapsException on error
     */
    public static MsgPhrase get(final String _name)
        throws EFapsException
    {
        final Cache<String, MsgPhrase> cache = InfinispanCache.get().<String, MsgPhrase>getCache(MsgPhrase.NAMECACHE);
        if (!cache.containsKey(_name)) {
            MsgPhrase.loadMsgPhrase(_name);
        }
        return cache.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link MsgPhrase}.
     *
     * @param _uuid uuid of the system configuration
     * @return instance of class {@link MsgPhrase}
     * @throws EFapsException on error
     */
    public static MsgPhrase get(final UUID _uuid)
        throws EFapsException
    {
        final Cache<UUID, MsgPhrase> cache = InfinispanCache.get().<UUID, MsgPhrase>getCache(MsgPhrase.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            MsgPhrase.loadMsgPhrase(_uuid);
        }
        return cache.get(_uuid);
    }

    /**
     * Method to initialize the {@link #CACHE cache} for the system
     * configurations.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(MsgPhrase.UUIDCACHE)) {
            InfinispanCache.get().<UUID, MsgPhrase>getCache(MsgPhrase.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, MsgPhrase>getCache(MsgPhrase.UUIDCACHE)
                            .addListener(new CacheLogListener(MsgPhrase.LOG));
        }
        if (InfinispanCache.get().exists(MsgPhrase.IDCACHE)) {
            InfinispanCache.get().<Long, MsgPhrase>getCache(MsgPhrase.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, MsgPhrase>getCache(MsgPhrase.IDCACHE)
                            .addListener(new CacheLogListener(MsgPhrase.LOG));
        }
        if (InfinispanCache.get().exists(MsgPhrase.NAMECACHE)) {
            InfinispanCache.get().<String, MsgPhrase>getCache(MsgPhrase.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, MsgPhrase>getCache(MsgPhrase.NAMECACHE)
                            .addListener(new CacheLogListener(MsgPhrase.LOG));
        }
        if (InfinispanCache.get().exists(MsgPhrase.LABELCACHE)) {
            InfinispanCache.get().getCache(MsgPhrase.LABELCACHE).clear();
        } else {
            InfinispanCache.get().<String, String>getCache(MsgPhrase.LABELCACHE)
                            .addListener(new CacheLogListener(MsgPhrase.LOG));
        }
        if (InfinispanCache.get().exists(MsgPhrase.ARGUMENTCACHE)) {
            InfinispanCache.get().getCache(MsgPhrase.ARGUMENTCACHE).clear();
        } else {
            InfinispanCache.get().<String, List<String>>getCache(MsgPhrase.ARGUMENTCACHE)
                            .addListener(new CacheLogListener(MsgPhrase.LOG));
        }
    }

    /**
     * @param _phrase MsgPhrase to be cached
     */
    private static void cacheMsgPhrase(final MsgPhrase _phrase)
    {
        final Cache<UUID, MsgPhrase> cache4UUID = InfinispanCache.get()
                        .<UUID, MsgPhrase>getIgnReCache(MsgPhrase.UUIDCACHE);
        cache4UUID.put(_phrase.getUUID(), _phrase);

        final Cache<String, MsgPhrase> nameCache = InfinispanCache.get()
                        .<String, MsgPhrase>getIgnReCache(MsgPhrase.NAMECACHE);
        nameCache.put(_phrase.getName(), _phrase);

        final Cache<Long, MsgPhrase> idCache = InfinispanCache.get()
                        .<Long, MsgPhrase>getIgnReCache(MsgPhrase.IDCACHE);
        idCache.putIfAbsent(_phrase.getId(), _phrase);
    }


    /**
     * @param _criteria criteria to use
     * @return true
     * @throws EFapsException on error
     */
    private static boolean loadMsgPhrase(final Object _criteria)
        throws EFapsException
    {
        final boolean ret = false;
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.MsgPhrase);
        if (_criteria instanceof Long) {
            queryBldr.addWhereAttrEqValue(CIAdminCommon.MsgPhrase.ID, _criteria);
        } else if (_criteria instanceof UUID) {
            queryBldr.addWhereAttrEqValue(CIAdminCommon.MsgPhrase.UUID, _criteria.toString());
        } else {
            queryBldr.addWhereAttrEqValue(CIAdminCommon.MsgPhrase.Name, _criteria);
        }
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CIAdminCommon.MsgPhrase.ID, CIAdminCommon.MsgPhrase.UUID, CIAdminCommon.MsgPhrase.Name,
                        CIAdminCommon.MsgPhrase.ParentLink);
        multi.executeWithoutAccessCheck();
        if (multi.next()) {
            final MsgPhrase phrase = new MsgPhrase(multi.<Long>getAttribute(CIAdminCommon.MsgPhrase.ID),
                            multi.<String>getAttribute(CIAdminCommon.MsgPhrase.Name),
                            multi.<String>getAttribute(CIAdminCommon.MsgPhrase.UUID),
                            multi.<Long>getAttribute(CIAdminCommon.MsgPhrase.ParentLink));
            phrase.load();
            cacheMsgPhrase(phrase);
        }
        if (multi.next()) {
            LOG.error("MsgPhrase is not unique for criteria: {}", _criteria);
        }
        return ret;
    }

    /**
     * Object class.
     */
    public abstract static class AbstractConfig
    {

        /**
         * Id of the language.
         */
        private long languageId = 0;

        /**
         * Id of the company.
         */
        private long companyId = 0;

        /**
         * value.
         */
        private String value;

        /**
         * Setter method for instance variable {@link #companyId}.
         *
         * @param _companyId value for instance variable {@link #companyId}
         */
        public void setCompanyId(final Long _companyId)
        {
            if (_companyId != null) {
                this.companyId = _companyId;
            }
        }

        /**
         * Getter method for the instance variable {@link #languageId}.
         *
         * @return value of instance variable {@link #languageId}
         */
        public long getLanguageId()
        {
            return this.languageId;
        }

        /**
         * Setter method for instance variable {@link #languageId}.
         *
         * @param _languageId value for instance variable {@link #languageId}
         */
        public void setLanguageId(final Long _languageId)
        {
            if (_languageId != null) {
                this.languageId = _languageId;
            }
        }

        /**
         * Getter method for the instance variable {@link #companyId}.
         *
         * @return value of instance variable {@link #companyId}
         */
        public long getCompanyId()
        {
            return this.companyId;
        }

        /**
         * Getter method for the instance variable {@link #value}.
         *
         * @return value of instance variable {@link #value}
         */
        public String getValue()
        {
            return this.value;
        }

        /**
         * Setter method for instance variable {@link #value}.
         *
         * @param _value value for instance variable {@link #value}
         */
        public void setValue(final String _value)
        {
            this.value = _value;
        }

        /**
         * @param _languageId id of the language
         * @param _companyId id for the company
         * @return priority
         */
        public int getPriority(final Long _languageId,
                               final Long _companyId)
        {
            int ret = 0;
            if (_languageId == getLanguageId()) {
                ret = ret + 60;
            }
            if (_companyId == getCompanyId()) {
                ret = ret + 40;
            }
            return ret;
        }
    }

    /**
     * Label object.
     */
    public static class Label
        extends AbstractConfig
    {

    }

    /**
     * Argument Object.
     */
    public static class Argument
        extends AbstractConfig
    {

        /**
         * Index.
         */
        private int index = 0;

        /**
         * Getter method for the instance variable {@link #index}.
         *
         * @return value of instance variable {@link #index}
         */
        public int getIndex()
        {
            return this.index;
        }

        /**
         * Setter method for instance variable {@link #index}.
         *
         * @param _index value for instance variable {@link #index}
         */
        public void setIndex(final int _index)
        {
            this.index = _index;
        }

    }
}
