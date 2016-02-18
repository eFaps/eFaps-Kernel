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

package org.efaps.db.print.value;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.Instance;
import org.efaps.db.print.OneSelect;
import org.efaps.eql.IEsjpSelect;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class EsjpValueSelect
    extends InstanceValueSelect
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EsjpValueSelect.class);

    /**
     * Name of the esjp class.
     */
    private String className;

    /**
     * Esjp to be invoked.
     */
    private IEsjpSelect esjp;

    /**
     * List of parameters.
     */
    private final List<String> parameters = new ArrayList<>();

    /**
     * Instantiates a new esjp value select.
     *
     * @param _oneSelect the one select
     * @param _esjp the esjp
     */
    public EsjpValueSelect(final OneSelect _oneSelect,
                           final String _esjp)
    {
        super(_oneSelect);
        LOG.debug("instanciated EsjpValueSelect with: '{}'", _esjp);
        final String[] paraAr = _esjp.split("(?<!\\\\),");
        if (paraAr == null || paraAr.length == 0) {
            LOG.error("Invalid esjp Value select: '{]'", _esjp);
        } else {
            this.className = paraAr[0];
            for (int i = 1; i < paraAr.length; i++) {
                final String string = paraAr[i];
                if (string.startsWith("\"")) {
                    this.parameters.add(string.substring(1, string.length() - 2));
                } else {
                    this.parameters.add(string);
                }
            }
        }
    }

    /**
     * Method to get the value for the current object.
     *
     * @param _object current object
     * @throws EFapsException on error
     * @return object
     */
    @Override
    public Object getValue(final Object _object)
        throws EFapsException
    {
        final Instance inst = (Instance) super.getValue(_object);
        if (this.esjp == null) {
            try {
                final Class<?> clazz = Class.forName(this.className, false, EFapsClassLoader.getInstance());
                this.esjp = (IEsjpSelect) clazz.newInstance();
                final List<Instance> instances = new ArrayList<>();
                for (final Object obj : getOneSelect().getObjectList()) {
                    instances.add((Instance) super.getValue(obj));
                }
                if (this.parameters.isEmpty()) {
                    this.esjp.initialize(instances);
                } else {
                    this.esjp.initialize(instances, this.parameters.toArray(new String[this.parameters.size()]));
                }
            } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOG.error("Catched error", e);
            }
        }
        return this.esjp.getValue(inst);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "esjp";
    }

}
