/*
 * Copyright 2003 - 2009 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.db;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Type;
import org.efaps.util.EFapsException;

/**
 * PrintQuery is a query uses to get the value for one object, specified by one
 * instance. The PrintQuery is able to execute various of the partes for the
 * select from EQL definition.
 *
 * TODO description!
 * TODO .type
 * TODO .value
 * TODO .attribute[ValueUOM].number
 * .attribute[ValueUOM].uom .attribute[ValueUOM].base
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PrintQuery extends AbstractPrintQuery
{

    /**
     * Instance this PrintQuery is based on.
     */
    private final Instance instance;

    /**
     * @param _type Type to be updated
     * @param _id id to be updated
     * @throws EFapsException on error
     */
    public PrintQuery(final Type _type, final String _id)
        throws EFapsException
    {
        this(Instance.get(_type, _id));
    }

    /**
     * @param _type Type to be updated
     * @param _id id to be updated
     * @throws EFapsException on error
     */
    public PrintQuery(final String _type, final String _id)
        throws EFapsException
    {
        this(Type.get(_type), _id);
    }

    /**
     * @param _oid OID of the instance to be updated.
     * @throws EFapsException on error
     */
    public PrintQuery(final String _oid)
        throws EFapsException
    {
        this(Instance.get(_oid));
    }

    /**
     * @param _instance instance to be updated.
     * @throws EFapsException on error
     */
    public PrintQuery(final Instance _instance)
        throws EFapsException
    {
        this.instance = _instance;
    }

    /**
     * Getter method for instance variable {@link #instance}.
     *
     * @return value of instance variable {@link #instance}
     */
    public Instance getInstance()
    {
        return this.instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getMainType()
    {
        return this.instance.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instance getCurrentInstance()
    {
        return this.instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Instance> getInstanceList()
    {
        final List<Instance> ret = new ArrayList<Instance>();
        ret.add(this.instance);
        return ret;
    }
}
