/*
 * Copyright 2003 - 2018 The eFaps Team
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

import org.efaps.ci.CIAttribute;
import org.efaps.db.stmt.InsertStmt;
import org.efaps.eql2.IInsertStatement;
import org.efaps.eql2.bldr.AbstractInsertEQLBuilder;
import org.efaps.util.EFapsException;

/**
 * The Class Insert.
 */
public class Insert
    extends AbstractInsertEQLBuilder<Insert>
{

    /**
     * Sets a value for an CIAttribute.
     *
     * @param _attr the attr
     * @param _value the value
     * @return the insert
     * @throws EFapsException if parsing went wrong
     */
    public Insert set(final CIAttribute _attr, final Object _value)
        throws EFapsException
    {
        return super.set(_attr.name, Converter.convert(_value));
    }

    /**
     * Stmt.
     *
     * @return the prints the stmt
     */
    public InsertStmt stmt()
    {
        return InsertStmt.get((IInsertStatement) getStmt());
    }

    @Override
    protected Insert getThis()
    {
        return this;
    }
}
