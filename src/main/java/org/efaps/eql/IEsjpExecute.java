/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.eql;

import java.util.List;
import java.util.Map;

import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public interface IEsjpExecute
{

    /**
     * @param _mapping mapping for the selects, null in case of execute
     * @param _parameters list of parameters to be passed
     * @return a data list as result
     * @throws EFapsException on error
     */
    List<Map<String, Object>> execute(Map<String, String> _mapping,
                                      String... _parameters)
        throws EFapsException;
}
