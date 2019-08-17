/*
 * Copyright 2003 - 2019 The eFaps Team
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

package org.efaps.eql.builder;

import org.efaps.eql2.bldr.AbstractQueryEQLBuilder;

/**
 * The Class Query.
 */
public class Query
    extends AbstractQueryEQLBuilder<Query>
{

    @Override
    public Print select()
    {
        return (Print) super.select();
    }

    @Override
    public Where where()
    {
        return (Where) super.where();
    }

}
