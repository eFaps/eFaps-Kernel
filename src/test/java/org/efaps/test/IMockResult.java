/*
 * Copyright 2003 - 2017 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.efaps.test;

import java.util.List;

import acolyte.jdbc.QueryResult;
import acolyte.jdbc.StatementHandler.Parameter;

/**
 * The Interface IResult.
 */
public interface IMockResult
{

    /**
     * Gets the sql.
     *
     * @return the sql
     */
    String[] getSqls();

    /**
     * Applies.
     *
     * @param _sql the sql
     * @param _parameters the parameters
     * @return true, if successful
     */
    boolean applies(String _sql, List<Parameter> _parameters);

    /**
     * Gets the result.
     *
     * @return the result
     */
    QueryResult getResult();
}
