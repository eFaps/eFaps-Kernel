/*
 * Copyright 2003 - 2013 The eFaps Team
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.print.OneSelect;
import org.efaps.util.EFapsException;

/**
 *
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class MultiPrintQuery extends AbstractPrintQuery
{

    /**
     * Instances this PrintQuery is based on.
     */
    private final List<Instance> instances;

    /**
     * Main type of this Query.
     */
    private final Type mainType;

    /**
     * Iterator used to iterate over the given instances.
     */
    private Iterator<Instance> iterator;

    /**
     * Current instance.
     */
    private Instance current;

    /**
     * @param _instances instance to be updated.
     * @throws EFapsException on error
     */
    public MultiPrintQuery(final List<Instance> _instances)
        throws EFapsException
    {
        this.instances = _instances;
        if (this.instances.size() > 0) {
            final Set<Type> types = new HashSet<Type>();
            for (final Instance instance : _instances) {
                if (!types.contains(instance.getType())) {
                    types.add(instance.getType());
                }
            }
            // if only one type is given the main type is this type
            // if more than one type is given they must have the same parent
            // type, if not it will not work. The next common parent is used.
            if (types.size() == 1) {
                this.mainType = _instances.get(0).getType();
            } else {
                final List<List<Type>> typeLists = new ArrayList<List<Type>>();
                for (final Type type : types) {
                    final List<Type> parents = new ArrayList<Type>();
                    Type currentType = type;
                    parents.add(currentType);
                    while (currentType.getParentType() != null) {
                        currentType = currentType.getParentType();
                        parents.add(currentType);
                    }
                    typeLists.add(parents);
                }

                Type tempType = null;
                final List<Type> compList = typeLists.get(0);
                typeLists.remove(0);
                for (final Type comp : compList) {
                    boolean found = true;
                    for (final List<Type> typeList : typeLists) {
                        if (!typeList.contains(comp)) {
                            found = false;
                            break;
                        }
                    }
                    if (found) {
                        tempType = comp;
                        break;
                    }
                }
                this.mainType = tempType;
            }
        } else {
            this.mainType = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getMainType()
    {
        return this.mainType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instance getCurrentInstance()
    {
        return this.current;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Instance> getInstanceList()
    {
        return this.instances;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute() throws EFapsException
    {
        if (isMarked4execute()) {
            final Map<Type, List<Instance>> types = new HashMap<Type, List<Instance>>();
            for (final Instance instance : this.instances) {
                List<Instance> list;
                if (!types.containsKey(instance.getType())) {
                    list = new ArrayList<Instance>();
                    types.put(instance.getType(), list);
                } else {
                    list = types.get(instance.getType());
                }
                list.add(instance);
            }
            //check the access for the given instances
            final Map<Instance, Boolean> accessmap = new HashMap<Instance, Boolean>();
            for (final Entry<Type, List<Instance>> entry : types.entrySet()) {
                accessmap.putAll(entry.getKey().checkAccess(entry.getValue(), AccessTypeEnums.SHOW.getAccessType()));
            }

            final Iterator<Instance> tempIter = this.instances.iterator();
            while (tempIter.hasNext()) {
                final Instance instance = tempIter.next();
                if (accessmap.size() > 0) {
                    if (!accessmap.containsKey(instance) || !accessmap.get(instance)) {
                        tempIter.remove();
                    }
                }
            }
        }
        return executeWithoutAccessCheck();
    }

    /**
     * Method to move the iterator to the next value.
     * @return true if the iterator was moved successfully to the next value
     */
    public boolean next()
    {
        boolean ret = false;
        if (this.iterator == null) {
            this.iterator = this.instances.iterator();
        }

        if (this.iterator.hasNext()) {
            this.current = this.iterator.next();
            ret = true;
        }
        if (ret) {
            for (final OneSelect oneSelect : getAllSelects()) {
                ret = oneSelect.next();
                if (!ret) {
                    break;
                }
            }
        }
        return ret;
    }
}
