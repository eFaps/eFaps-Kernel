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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.EnumUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.index.Indexer.Key;
import org.efaps.db.Instance;
import org.efaps.json.index.SearchResult;
import org.efaps.json.index.SearchResult.Element;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Search.
 *
 * @author The eFaps Team
 */
public final class Searcher
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Searcher.class);

    /** The types. */
    private final Map<Type, List<Instance>> typeMapping = new HashMap<>();

    /** The docs. */
    private final Map<Instance, Element> elements = new LinkedHashMap<>();

    /**
     * Instantiates a new search.
     */
    private Searcher()
    {
    }

    /**
     * Search.
     *
     * @param _search the search
     * @return the search result
     * @throws EFapsException on error
     */
    protected SearchResult executeSearch(final ISearch _search)
        throws EFapsException
    {
        final SearchResult ret = new SearchResult();
        try {
            LOG.debug("Starting search with: {}", _search.getQuery());
            final StandardQueryParser queryParser = new StandardQueryParser(Index.getAnalyzer());
            queryParser.setAllowLeadingWildcard(true);
            if (EFapsSystemConfiguration.get().containsAttributeValue(KernelSettings.INDEXDEFAULTOP)) {
                queryParser.setDefaultOperator(EnumUtils.getEnum(StandardQueryConfigHandler.Operator.class,
                                EFapsSystemConfiguration.get().getAttributeValue(KernelSettings.INDEXDEFAULTOP)));
            } else {
                queryParser.setDefaultOperator(StandardQueryConfigHandler.Operator.AND);
            }
            final Query query = queryParser.parse(_search.getQuery(), "ALL");

            final IndexReader reader = DirectoryReader.open(Index.getDirectory());
            Sort sort = _search.getSort();
            if (sort == null) {
                sort  = new Sort(new SortField(Key.CREATED.name(), SortField.Type.LONG, true));
            }

            final SortedSetDocValuesReaderState state =
                            new DefaultSortedSetDocValuesReaderState(reader, DBProperties.getProperty("index.Type"));

            final IndexSearcher searcher = new IndexSearcher(reader);
            final FacetsCollector coll = new FacetsCollector();
            final TopFieldDocs docss = FacetsCollector.search(searcher, query, _search.getNumHits(), sort, coll);
            System.out.println(docss);
            final Facets facets = new SortedSetDocValuesFacetCounts(state, coll);
            final FacetResult result = facets.getTopChildren(100, "TYPE");
            System.out.println(result);

            ret.setHitCount(searcher.count(query));
            if (ret.getHitCount() > 0) {
                final TopDocs docs = searcher.search(query, _search.getNumHits(), sort);
                final ScoreDoc[] hits = docs.scoreDocs;

                LOG.debug("Found {} hits.", hits.length);
                for (int i = 0; i < hits.length; ++i) {
                    final Document doc = searcher.doc(hits[i].doc);
                    final String oid = doc.get(Key.OID.name());
                    final String text = doc.get(Key.MSGPHRASE.name());
                    LOG.debug("{}. {}\t {}", i + 1, oid, text);
                    final Instance instance = Instance.get(oid);
                    final List<Instance> list;
                    if (this.typeMapping.containsKey(instance.getType())) {
                        list = this.typeMapping.get(instance.getType());
                    } else {
                        list = new ArrayList<Instance>();
                        this.typeMapping.put(instance.getType(), list);
                    }
                    list.add(instance);
                    final Element element = new Element().setOid(oid).setText(text);
                    for (final Entry<String, Collection<String>> entry : _search.getResultFields().entrySet()) {
                        for (final String name : entry.getValue()) {
                            final String value = doc.get(name);
                            if (value != null) {
                                element.addField(name, value);
                            }
                        }
                    }
                    this.elements.put(instance, element);
                }
            }
            reader.close();
            checkAccess();
            ret.getElements().addAll(this.elements.values());
        } catch (final IOException | QueryNodeException e) {
            LOG.error("Catched Exception", e);
        }
        return ret;
    }

    /**
     * Check access.
     *
     * @throws EFapsException on error
     */
    private void checkAccess()
        throws EFapsException
    {
        // check the access for the given instances
        final Map<Instance, Boolean> accessmap = new HashMap<Instance, Boolean>();
        for (final Entry<Type, List<Instance>> entry : this.typeMapping.entrySet()) {
            accessmap.putAll(entry.getKey().checkAccess(entry.getValue(), AccessTypeEnums.SHOW.getAccessType()));
        }
        this.elements.entrySet().removeIf(entry -> accessmap.size() > 0 && (!accessmap.containsKey(entry.getKey())
                        || !accessmap.get(entry.getKey())));
    }

    /**
     * Search.
     *
     * @param _query the query
     * @return the search result
     * @throws EFapsException on error
     */
    public static SearchResult search(final String _query)
        throws EFapsException
    {
        final ISearch searchDef = Index.getSearch();
        searchDef.setQuery(_query);
        return search(searchDef);
    }

    /**
     * Search.
     *
     * @param _search the search
     * @return the search result
     * @throws EFapsException on error
     */
    public static SearchResult search(final ISearch _search)
        throws EFapsException
    {
        return new Searcher().executeSearch(_search);
    }
}
