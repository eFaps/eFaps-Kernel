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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.efaps.admin.index.Indexer.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Search.
 *
 * @author The eFaps Team
 */
public final class Search
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Search.class);

    /**
     * Instantiates a new search.
     */
    private Search()
    {

    }

    /**
     * Search.
     *
     * @param _query the query
     */
    public static void search(final String _query)
    {
        try {
            LOG.debug("Starting search with: {}", _query);
            final StandardQueryParser queryParser = new StandardQueryParser(Index.getAnalyzer());
            final Query query = queryParser.parse(_query, "ALL");

            final IndexReader reader = DirectoryReader.open(Index.getDirectory());

            final IndexSearcher searcher = new IndexSearcher(reader);
            final TopDocs docs = searcher.search(query, 100);
            final ScoreDoc[] hits = docs.scoreDocs;

            LOG.debug("Found {} hits.", hits.length);
            for (int i = 0; i < hits.length; ++i) {
                final int docId = hits[i].doc;
                final Document d = searcher.doc(docId);
                LOG.debug("{}. {}\t {}", i + 1, d.get(Key.OID.name()), d.get(Key.MSGPHRASE.name()));
            }
            reader.close();
        } catch (final IOException | QueryNodeException e) {
            LOG.error("Catched Exception", e);
        }
    }
}
