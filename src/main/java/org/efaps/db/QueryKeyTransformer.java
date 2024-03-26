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
package org.efaps.db;

import org.infinispan.query.Transformer;

public class QueryKeyTransformer
    implements Transformer
{

    @Override
    public Object fromString(String str)
    {
        final var strArray = str.split("\\|", 0);
        return QueryKey.get(strArray[0], strArray[1]);
    }

    @Override
    public String toString(Object obj)
    {
        final var queryKey = (QueryKey) obj;
        return queryKey.getKey() + "|" + queryKey.getSql();
    }
}
