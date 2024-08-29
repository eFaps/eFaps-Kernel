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
package org.efaps.eql.builder;

import org.efaps.db.stmt.CountStmt;
import org.efaps.db.stmt.selection.EvalHelper;
import org.efaps.eql2.ICountQueryStatement;
import org.efaps.eql2.bldr.AbstractCountEQLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Count extends AbstractCountEQLBuilder<Count>
{
    private static final Logger LOG = LoggerFactory.getLogger(Count.class);

    @Override
    protected Count getThis()
    {
        return this;
    }

    @Override
    public Where where()
    {
        return (Where) super.where();
    }

    public CountStmt stmt()
    {
        LOG.debug("Stmt: {}", getStmt().eqlStmt());
        return CountStmt.get((ICountQueryStatement) getStmt(), new EvalHelper());
    }
}
