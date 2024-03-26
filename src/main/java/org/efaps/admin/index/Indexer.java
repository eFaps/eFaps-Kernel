/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.admin.index;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.CreatedType;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.index.IndexDefinition.IndexField;
import org.efaps.admin.program.esjp.EFapsClassLoader;
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
     */
    public enum Key
    {

        /** The oid. */
        OID,

        /** The all. */
        ALL,

        /** The msgphrase. */
        MSGPHRASE,

        /** The created numeric field used for sorting. */
        CREATED,

        /** The created string field used for searching. */
        CREATEDSTR;
    }

    /**
     * The Enum Dim.
     */
    public enum Dimension
    {

        /** The type. */
        DIMTYPE,

        /** The created. */
        DIMCREATED;
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
            try (
                            IndexWriter writer = new IndexWriter(_context.getDirectory(), config);
                            TaxonomyWriter taxonomyWriter = new DirectoryTaxonomyWriter(
                                            _context.getTaxonomyDirectory());) {

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
                    LOG.debug("Indexing: oid: {} type: {} ", oid, type);
                    final Document doc = new Document();
                    doc.add(new FacetField(Dimension.DIMTYPE.name(), type));
                    doc.add(new FacetField(Dimension.DIMCREATED.name(), String.valueOf(created.getYear()),
                                    String.format("%02d", created.getMonthOfYear())));
                    doc.add(new StringField(Key.OID.name(), oid, Store.YES));
                    doc.add(new TextField(DBProperties.getProperty("index.Type"), type, Store.YES));
                    doc.add(new NumericDocValuesField(Key.CREATED.name(), created.getMillis()));
                    doc.add(new StringField(Key.CREATEDSTR.name(),
                                    DateTools.dateToString(created.toDate(), DateTools.Resolution.DAY), Store.NO));

                    final StringBuilder allBldr = new StringBuilder()
                                    .append(type).append(" ");

                    for (final IndexField field : def.getFields()) {
                        final String name = DBProperties.getProperty(field.getKey());
                        Object value = multi.getSelect(field.getSelect());
                        if (value != null) {
                            if (StringUtils.isNoneEmpty(field.getTransform())) {
                                final Class<?> clazz = Class.forName(field.getTransform(),
                                                false, EFapsClassLoader.getInstance());
                                final ITransformer transformer = (ITransformer) clazz.getConstructor().newInstance();
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
                                    doc.add(new NumericDocValuesField(name, val));
                                    allBldr.append(value).append(" ");
                                    break;
                                case SEARCHLONG:
                                    long val2 = 0;
                                    if (value instanceof String) {
                                        val2 = NumberUtils.toLong((String) value);
                                    } else if (value instanceof Number) {
                                        val2 = ((Number) value).longValue();
                                    }
                                    doc.add(new LongPoint(name, val2));
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
                    writer.updateDocument(new Term(Key.OID.name(), oid),
                                    Index.getFacetsConfig().build(taxonomyWriter, doc));
                    LOG.debug("Add Document: {}", doc);
                }
                writer.close();
                taxonomyWriter.close();
            } catch (final IOException | ClassNotFoundException | InstantiationException | IllegalAccessException
                            | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                            | SecurityException e) {
                throw new EFapsException(Indexer.class, "Catched", e);
            } finally {
                Context.getThreadContext().setCompany(currentCompany);
                Context.getThreadContext().setLanguage(currentLanguage);
            }
        }
    }
}
