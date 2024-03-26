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

import org.apache.lucene.analysis.Analyzer;
import org.efaps.util.EFapsException;

/**
 * The Interface IAnalyzerProvider.
 *
 * @author The eFaps Team
 */
public interface IAnalyzerProvider
{

    /**
     * Gets the analyzer. The method is responsible to provide the correct
     * Analyzer for the current Context. (Company dependend, Language etc.)
     *
     * @return the analyzer
     * @throws EFapsException on error
     */
    Analyzer getAnalyzer()
        throws EFapsException;;
}
