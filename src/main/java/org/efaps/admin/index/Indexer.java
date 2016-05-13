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

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.CreatedType;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.index.IndexDefinition.IndexField;
import org.efaps.admin.user.Company;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The Class Indexer.
 *
 * @author The eFaps Team
 */
public final class Indexer
{

    /**
     * The Enum Key.
     *
     * @author The eFaps Team
     */
    public enum Key
    {

        /** The oid. */
        OID,

        /** The all. */
        ALL,

        /** The msgphrase. */
        MSGPHRASE,

        /** The created field. */
        CREATED;
    }

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    /**
     * Instantiates a new indexer.
     */
    private Indexer()
    {
    }

    /**
     * Index or reindex using the Indexdefinitions.
     *
     * @throws EFapsException the e faps exception
     */
    public static void index()
        throws EFapsException
    {
        final List<IndexDefinition> defs = IndexDefinition.get();
        for (final IndexDefinition def : defs) {
            final QueryBuilder queryBldr = new QueryBuilder(def.getUUID());
            final InstanceQuery query = queryBldr.getQuery();
            index(query.execute());
        }
    }

    /**
     * Index or reindex a given list of instances. The given instances m,ust be
     * all of the same type!
     *
     * @param _instances the _instances
     * @throws EFapsException the e faps exception
     */
    public static void index(final List<Instance> _instances)
        throws EFapsException
    {
        Indexer.index(new IndexContext().setAnalyzer(Index.getAnalyzer()).setDirectory(Index.getDirectory())
                        .setLanguage(Context.getThreadContext().getLanguage())
                        .setCompanyId(Context.getThreadContext().getCompany().getId()), _instances);
    }

    /**
     * Index or reindex a given list of instances. The given instances m,ust be
     * all of the same type!
     *
     * @param _context the _context
     * @param _instances the instances
     * @throws EFapsException the e faps exception
     */
    public static void index(final IndexContext _context,
                             final List<Instance> _instances)
        throws EFapsException
    {
        if (CollectionUtils.isNotEmpty(_instances)) {
            final Company currentCompany = Context.getThreadContext().getCompany();
            final String currentLanguage = Context.getThreadContext().getLanguage();

            Context.getThreadContext().setCompany(Company.get(_context.getCompanyId()));
            Context.getThreadContext().setLanguage(_context.getLanguage());
            final IndexWriterConfig config = new IndexWriterConfig(_context.getAnalyzer());
            try (IndexWriter writer = new IndexWriter(_context.getDirectory(), config)) {
                final IndexDefinition def = IndexDefinition.get(_instances.get(0).getType().getUUID());
                final MultiPrintQuery multi = new MultiPrintQuery(_instances);
                for (final IndexField field : def.getFields()) {
                    multi.addSelect(field.getSelect());
                }
                Attribute createdAttr = null;
                if (!_instances.get(0).getType().getAttributes(CreatedType.class).isEmpty()) {
                    createdAttr = _instances.get(0).getType().getAttributes(CreatedType.class).iterator().next();
                    multi.addAttribute(createdAttr);
                }
                multi.addMsgPhrase(def.getMsgPhrase());
                multi.executeWithoutAccessCheck();
                while (multi.next()) {
                    final String oid = multi.getCurrentInstance().getOid();
                    final String type = multi.getCurrentInstance().getType().getLabel();
                    final DateTime created;
                    if (createdAttr == null) {
                        created = new DateTime();
                    } else {
                        created = multi.getAttribute(createdAttr);
                    }

                    final Document doc = new Document();
                    doc.add(new StringField(Key.OID.name(), oid, Store.YES));
                    doc.add(new TextField(DBProperties.getProperty("index.Type"), type, Store.YES));
                    doc.add(new NumericDocValuesField(Key.CREATED.name(), created.getMillis()));

                    final StringBuilder allBldr = new StringBuilder()
                                    .append(type).append(" ");

                    for (final IndexField field : def.getFields()) {
                        final String name = DBProperties.getProperty(field.getKey());
                        Object value = multi.getSelect(field.getSelect());
                        if (value != null) {
                            if (StringUtils.isNoneEmpty(field.getTransform())) {
                                final Class<?> clazz = Class.forName(field.getTransform());
                                final ITransformer transformer = (ITransformer) clazz.newInstance();
                                value = transformer.transform(value);
                            }
                            switch (field.getFieldType()) {
                                case LONG:
                                    long val = 0;
                                    if (value instanceof String) {
                                        val = NumberUtils.toLong((String) value);
                                    } else if (value instanceof Number) {
                                        val = ((Number) value).longValue();
                                    }
                                    doc.add(new LongField(name, val, Store.YES));
                                    allBldr.append(value).append(" ");
                                    break;
                                case SEARCHLONG:
                                    long val2 = 0;
                                    if (value instanceof String) {
                                        val2 = NumberUtils.toLong((String) value);
                                    } else if (value instanceof Number) {
                                        val2 = ((Number) value).longValue();
                                    }
                                    doc.add(new LongField(name, val2, Store.NO));
                                    allBldr.append(value).append(" ");
                                    break;
                                case STRING:
                                    doc.add(new StringField(name, String.valueOf(value), Store.YES));
                                    allBldr.append(value).append(" ");
                                    break;
                                case SEARCHSTRING:
                                    doc.add(new StringField(name, String.valueOf(value), Store.NO));
                                    allBldr.append(value).append(" ");
                                    break;
                                case TEXT:
                                    doc.add(new TextField(name, String.valueOf(value), Store.YES));
                                    allBldr.append(value).append(" ");
                                    break;
                                case SEARCHTEXT:
                                    doc.add(new TextField(name, String.valueOf(value), Store.NO));
                                    allBldr.append(value).append(" ");
                                    break;
                                case STORED:
                                    doc.add(new StoredField(name, String.valueOf(value)));
                                    allBldr.append(value).append(" ");
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    doc.add(new StoredField(Key.MSGPHRASE.name(), multi.getMsgPhrase(def.getMsgPhrase())));
                    doc.add(new TextField(Key.ALL.name(), allBldr.toString(), Store.NO));
                    writer.updateDocument(new Term("oid", oid), doc);
                    LOG.debug("Add Document: {}", doc);
                }
                writer.close();
            } catch (final IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new EFapsException(Indexer.class, "IOException", e);
            } finally {
                Context.getThreadContext().setCompany(currentCompany);
                Context.getThreadContext().setLanguage(currentLanguage);
            }
        }
    }
}
