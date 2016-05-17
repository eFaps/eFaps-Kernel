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
package org.efaps.admin.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.common.MsgPhrase;
import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminDataModel;
import org.efaps.ci.CIAdminIndex;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class IndexDefinition.
 *
 * @author The eFaps Team
 */
public final class IndexDefinition
    implements Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Name of the Cache by UUID.
     */
    private static String UUIDCACHE = IndexDefinition.class.getName() + ".UUID";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(IndexDefinition.class);

    /** The Constant NULL. */
    private static final IndexDefinition NULLINDEXDEF = new IndexDefinition(null, null);

    /** The uuid. */
    private final UUID uuid;

    /** The fields. */
    private final Set<IndexField> fields = new HashSet<>();

    /** The msg phrase id. */
    private final long msgPhraseId;

    /**
     * Instantiates a new index definition.
     *
     * @param _uuid the uuid
     * @param _msgPhraseId the msg phrase id
     */
    private IndexDefinition(final UUID _uuid,
                            final Long _msgPhraseId)
    {
        this.uuid = _uuid;
        this.msgPhraseId = _msgPhraseId == null ? 0 : _msgPhraseId;
    }

    /**
     * Gets the uuid.
     *
     * @return the uuid
     */
    public UUID getUUID()
    {
        return this.uuid;
    }

    /**
     * Gets the fields.
     *
     * @return the fields
     */
    public Set<IndexField> getFields()
    {
        return this.fields;
    }

    /**
     * Gets the msg phrase.
     *
     * @return the msg phrase
     * @throws EFapsException on error
     */
    public MsgPhrase getMsgPhrase()
        throws EFapsException
    {
        return MsgPhrase.get(this.msgPhraseId);
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error
     */
    public static void initialize()
        throws CacheReloadException
    {
        initialize(true);
    }

    /**
     * Initialize.
     *
     * @param _restart the restart
     * @throws CacheReloadException the cache reload exception
     */
    private static void initialize(final boolean _restart)
        throws CacheReloadException
    {
        if (InfinispanCache.get().exists(IndexDefinition.UUIDCACHE)) {
            if (_restart) {
                InfinispanCache.get().<UUID, IndexDefinition>getCache(IndexDefinition.UUIDCACHE).clear();
            }
        } else {
            InfinispanCache.get().<UUID, Type>getCache(IndexDefinition.UUIDCACHE).addListener(new CacheLogListener(
                            IndexDefinition.LOG));
        }
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Type}.
     *
     * @param _uuid uuid of the type to get
     * @return instance of class {@link Type}
     * @throws EFapsException on error
     */
    public static IndexDefinition get(final UUID _uuid)
        throws EFapsException
    {
        initialize(true);
        final Cache<UUID, IndexDefinition> cache = InfinispanCache.get().<UUID, IndexDefinition>getCache(
                        IndexDefinition.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            IndexDefinition.loadDefinition(_uuid);
        }
        final IndexDefinition ret = cache.get(_uuid);
        return ret == null || ret.equals(NULLINDEXDEF) ? null : ret;
    }

    /**
     * Load definition.
     *
     * @param _typeUUID the type uuid
     * @throws EFapsException on error
     */
    private static void loadDefinition(final UUID _typeUUID)
        throws EFapsException
    {
        final Type type = Type.get(_typeUUID);
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminIndex.IndexDefinition);
        queryBldr.addWhereAttrEqValue(CIAdminIndex.IndexDefinition.Active, true);
        queryBldr.addWhereAttrEqValue(CIAdminIndex.IndexDefinition.TypeLink, type.getId());
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CIAdminIndex.IndexDefinition.MsgPhraseLink);
        multi.executeWithoutAccessCheck();

        IndexDefinition def;
        if (multi.next()) {
            def = new IndexDefinition(_typeUUID,
                            multi.<Long>getAttribute(CIAdminIndex.IndexDefinition.MsgPhraseLink));

            final QueryBuilder fieldQueryBldr = new QueryBuilder(CIAdminIndex.IndexField);
            fieldQueryBldr.addWhereAttrEqValue(CIAdminIndex.IndexField.DefinitionLink, multi.getCurrentInstance());
            final MultiPrintQuery fieldMulti = fieldQueryBldr.getPrint();
            fieldMulti.addAttribute(CIAdminIndex.IndexField.Key, CIAdminIndex.IndexField.Select,
                            CIAdminIndex.IndexField.FieldType, CIAdminIndex.IndexField.Identifier);
            final SelectBuilder selTransName = SelectBuilder.get().linkto(CIAdminIndex.IndexField.TransformerLink)
                            .attribute(CIAdminProgram.Java.Name);
            fieldMulti.addSelect(selTransName);
            fieldMulti.executeWithoutAccessCheck();
            while (fieldMulti.next()) {
                final IndexField field = new IndexField(fieldMulti.<String>getAttribute(
                                    CIAdminIndex.IndexField.Identifier),
                                fieldMulti.<String>getAttribute(CIAdminIndex.IndexField.Key),
                                fieldMulti.<String>getAttribute(CIAdminIndex.IndexField.Select),
                                fieldMulti.<FieldType>getAttribute(CIAdminIndex.IndexField.FieldType),
                                fieldMulti.<String>getSelect(selTransName));
                def.fields.add(field);
            }
        } else {
            def = NULLINDEXDEF;
        }
        final Cache<UUID, IndexDefinition> cache = InfinispanCache.get().<UUID, IndexDefinition>getCache(
                        IndexDefinition.UUIDCACHE);
        cache.put(_typeUUID, def);

        // only if it is not a null index check if it must be joined with parent index definitions
        final List<IndexDefinition> defs = new ArrayList<>();
        Type current = type;
        while (current.getParentType() != null) {
            current = current.getParentType();
            final IndexDefinition parentDef = get(current.getUUID());
            if (parentDef != null) {
                defs.add(parentDef);
            }
        }
        boolean dirty = false;
        for (final IndexDefinition parentDef : defs) {
            for (final IndexField parentField : parentDef.fields) {
                boolean found = false;
                if (def.equals(NULLINDEXDEF)) {
                    def = new IndexDefinition(_typeUUID, parentDef.msgPhraseId);
                }
                for (final IndexField currentField : def.fields) {
                    if (currentField.getIdentifier().equals(parentField.getIdentifier())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    def.fields.add(new IndexField(parentField.getIdentifier(), parentField.getKey(),
                                    parentField.getSelect(), parentField.getFieldType(), parentField.getTransform()));
                    dirty = true;
                }
            }
        }
        if (dirty) {
            cache.put(_typeUUID, def);
        }
    }

    /**
     * Gets the.
     *
     * @return the list< index definition>
     * @throws EFapsException on error
     */
    public static List<IndexDefinition> get()
        throws EFapsException
    {
        final List<IndexDefinition> ret = new ArrayList<>();

        final QueryBuilder queryBldr = new QueryBuilder(CIAdminIndex.IndexDefinition);
        final MultiPrintQuery multi = queryBldr.getPrint();
        final SelectBuilder selUUID = SelectBuilder.get().linkto(CIAdminIndex.IndexDefinition.TypeLink)
                        .attribute(CIAdminDataModel.Type.UUID);
        multi.addSelect(selUUID);
        multi.execute();
        while (multi.next()) {
            final UUID uuidTmp = UUID.fromString(multi.getSelect(selUUID));
            final Set<Type> types = getChildTypes(Type.get(uuidTmp));
            for (final Type type : types) {
                final IndexDefinition indexDef = IndexDefinition.get(type.getUUID());
                if (!type.isAbstract()) {
                    ret.add(indexDef);
                }
            }
        }
        return ret;
    }

    /**
     * Gets the type list.
     *
     * @param _type the type
     * @return the type list
     * @throws CacheReloadException the cache reload exception
     */
    private static Set<Type> getChildTypes(final Type _type)
        throws CacheReloadException
    {
        final Set<Type> ret = new HashSet<Type>();
        ret.add(_type);
        for (final Type child : _type.getChildTypes()) {
            ret.addAll(getChildTypes(child));
        }
        return ret;
    }

    /**
     * The Class IndexField.
     *
     * @author The eFaps Team
     */
    public static final class IndexField
        implements Serializable
    {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The identifier. */
        private final String identifier;

        /** The key. */
        private final String key;

        /** The select. */
        private final String select;

        /** The field type. */
        private final FieldType fieldType;

        /** The transform esjp classname. */
        private final String transform;

        /**
         * Instantiates a new index field.
         *
         * @param _identifier the identifier
         * @param _key the key
         * @param _select the select
         * @param _fieldType the field type
         * @param _transform the transform
         */
        private IndexField(final String _identifier,
                           final String _key,
                           final String _select,
                           final FieldType _fieldType,
                           final String _transform)
        {
            this.identifier = _identifier;
            this.key = _key;
            this.select = _select;
            this.fieldType = _fieldType;
            this.transform = _transform;
        }

        /**
         * Gets the key.
         *
         * @return the key
         */
        public String getKey()
        {
            return this.key;
        }

        /**
         * Gets the select.
         *
         * @return the select
         */
        public String getSelect()
        {
            return this.select;
        }

        /**
         * Gets the field type.
         *
         * @return the field type
         */
        public FieldType getFieldType()
        {
            return this.fieldType;
        }

        /**
         * Gets the transform esjp classname.
         *
         * @return the transform esjp classname
         */
        public String getTransform()
        {
            return this.transform;
        }

        /**
         * Gets the identifier.
         *
         * @return the identifier
         */
        public String getIdentifier()
        {
            return this.identifier;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
