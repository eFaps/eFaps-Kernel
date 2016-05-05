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

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * The Class Index.
 *
 * @author The eFaps Team
 */
public final class Index
{
    /**
     * Instantiates a new index.
     */
    private Index()
    {
    }

    /**
     * Gets the analyzer.
     *
     * @return the analyzer
     */
    public static Analyzer getAnalyzer()
    {
        return new StandardAnalyzer(SpanishAnalyzer.getDefaultStopSet());
    }

    /**
     * Gets the directory.
     *
     * @return the directory
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Directory getDirectory()
        throws IOException
    {
        return FSDirectory.open(new File("/eFaps/index").toPath());
    }
}
