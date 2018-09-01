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

package org.efaps.db.stmt.selection.elements;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.Instance;
import org.efaps.db.stmt.selection.ProxiedObject;
import org.efaps.eql.IEsjpSelect;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ExecElement.
 */
public class ExecElement
    extends InstanceElement
    implements IProxy
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ExecElement.class);

    /** The instances. */
    final List<Instance> instances = new ArrayList<>();

    /** The class name. */
    private String className;

    /** The parameters. */
    private String[] parameters;

    /** The esjp. */
    private IEsjpSelect esjp;

    /**
     * Instantiates a new exec element.
     *
     * @param _type the type
     */
    public ExecElement(final Type _type)
    {
        super(_type);
    }

    @Override
    public ExecElement getThis()
    {
        return this;
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        final Instance inst = (Instance) super.getObject(_row);
        this.instances.add(inst);
        return new ProxiedObject().setObject(inst).setProxy(this);
    }

    /**
     * Sets the esjp.
     *
     * @param _className the class name
     * @return the exec element
     */
    public ExecElement setEsjp(final String _className) {
        this.className = _className;
        return this;
    }

    /**
     * Sets the parameters.
     *
     * @param _parameters the parameters
     * @return the exec element
     */
    public ExecElement setParameters(final String[] _parameters) {
        this.parameters = _parameters;
        return this;
    }

    @Override
    public Object getValue(final Object _object)
        throws EFapsException
    {
        if (this.esjp == null) {
            try {
                final Class<?> clazz = Class.forName(this.className, false, EFapsClassLoader.getInstance());
                this.esjp = (IEsjpSelect) clazz.newInstance();
                if (ArrayUtils.isEmpty(this.parameters)) {
                    this.esjp.initialize(this.instances);
                } else {
                    this.esjp.initialize(this.instances, this.parameters);
                }
            } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOG.error("Catched error", e);
            }
        }
        return this.esjp.getValue((Instance) _object);
    }
}
