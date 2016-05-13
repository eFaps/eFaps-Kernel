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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class IndexContext
{

    /** The analyzer. */
    private Analyzer analyzer;

    /** The directory. */
    private Directory directory;

    /** The company id. */
    private long companyId;

    /** The language. */
    private String language;

    /**
     * Getter method for the instance variable {@link #analyzer}.
     *
     * @return value of instance variable {@link #analyzer}
     */
    public Analyzer getAnalyzer()
    {
        return this.analyzer;
    }

    /**
     * Setter method for instance variable {@link #analyzer}.
     *
     * @param _analyzer value for instance variable {@link #analyzer}
     * @return the index context
     */
    public IndexContext setAnalyzer(final Analyzer _analyzer)
    {
        this.analyzer = _analyzer;
        return this;
    }

    /**
     * Getter method for the instance variable {@link #directory}.
     *
     * @return value of instance variable {@link #directory}
     */
    public Directory getDirectory()
    {
        return this.directory;
    }

    /**
     * Setter method for instance variable {@link #directory}.
     *
     * @param _directory value for instance variable {@link #directory}
     * @return the index context
     */
    public IndexContext setDirectory(final Directory _directory)
    {
        this.directory = _directory;
        return this;
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
     * Setter method for instance variable {@link #companyId}.
     *
     * @param _companyId value for instance variable {@link #companyId}
     * @return the index context
     */
    public IndexContext setCompanyId(final long _companyId)
    {
        this.companyId = _companyId;
        return this;
    }

    /**
     * Getter method for the instance variable {@link #language}.
     *
     * @return value of instance variable {@link #language}
     */
    public String getLanguage()
    {
        return this.language;
    }

    /**
     * Setter method for instance variable {@link #language}.
     *
     * @param _language value for instance variable {@link #language}
     * @return the index context
     */
    public IndexContext setLanguage(final String _language)
    {
        this.language = _language;
        return this;
    }
}
