/*
 * Copyright 2003 - 2010 The eFaps Team
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.query.OneRoundQuery;
import org.efaps.util.EFapsException;

/**
 * TODO:  description
 * @author The eFaps Team
 * @version $Id$
 */
public class ListQuery
    extends AbstractQuery
{
    /**
     * Stores all instances for which this query is executed.
     */
    private final List<Instance> instances;

    /**
     * Stores all select statements for this query.
     */
    private final Set<String> selects = new HashSet<String>();

    private final Set<String> multiSelects = new HashSet<String>();

    private final Map<String, ListQuery> subSelects = new HashMap<String, ListQuery>();

    private OneRoundQuery query = null;

    private AttributeSet attributeSet;

    /**
     * @param _instances list of instances for which this query is executed
     */
    public ListQuery(final List<Instance> _instances)
    {
        this.instances = _instances;
    }

    public ListQuery()
    {
        this.instances = new ArrayList<Instance>();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // instance methods

    @Override
    public void execute() throws EFapsException
    {
        try {
            if (this.instances.size() > 0) {
                this.query = new OneRoundQuery(this.instances, this.selects, this);
                this.query.execute();
                for (final Map.Entry<String, ListQuery> sub : this.subSelects.entrySet()) {
                    while (this.query.next()) {
                        if (this.multiSelects.contains(sub.getKey())) {
                            sub.getValue().instances.addAll(this.instances);
                        } else {
                            final Attribute attr = this.query.getAttribute(sub.getKey());
                            if ((attr != null) && (attr.getLink() != null)) {
                                final Object value = this.query.getValue(sub.getKey());
                                if (value != null) {
                                    // we must differ between ids that are
                                    // returned and
                                    // AdminObject (e.g. Person in case of
                                    // CreatorLink)
                                    if (value instanceof Number) {
                                        final Long id = ((Number) value).longValue();
                                        if ((id != null) && (id != 0)) {
                                            sub.getValue().addInstance(attr.getLink(), id);
                                        }
                                    } else if (value instanceof AbstractAdminObject) {
                                        sub.getValue().addInstance(attr.getLink(),
                                                        ((AbstractAdminObject) value).getId());
                                    }
                                }
                            }
                        }
                    }
                    this.query.beforeFirst();
                    sub.getValue().execute();
                }
            }
        } catch (final Exception e) {
            throw (new EFapsException(this.getClass(), "execute", e));
        }
    }

    /**
     * @param set
     */
    public void setExpand(final AttributeSet _set)
    {
        this.attributeSet = _set;
    }

    public Set<String> getMultiSelects()
    {
        return this.multiSelects;
    }

    private boolean gotoKey(final Object _key)
    {
        return this.query == null ? false : this.query.gotoKey(_key);
    }

    /**
     * Adds one select statement to this query.
     *
     * @param _select select statement to add
     * @see #selects
     */
    @Override
    public void addSelect(final String _select)
    {
        final int idx = _select.indexOf(".");
        if (idx > 0) {
            // differ select expression from sub expression
            final String select = _select.substring(0, idx);
            final String subSel = _select.substring(idx + 1);
            this.selects.add(select);
            // make the subquery depending on the select statement
            ListQuery subQuery = this.subSelects.get(select);
            if (subQuery == null) {
                subQuery = new ListQuery();
                this.subSelects.put(select, subQuery);
            }
            subQuery.addSelect(subSel);
        } else {
            this.selects.add(_select);
        }
    }

    private void addInstance(final Type _type, final long _id)
    {
        this.instances.add(Instance.get(_type, _id));
    }

    /**
     * @return <i>true</i> if a new row is selected and exists, otherwise
     *         <i>false</i>
     */
    @Override
    public boolean next()
    {
        return (this.query != null) ? this.query.next() : false;
    }

    /**
     * The instance method returns for the given key the attribute value.
     *
     * @param _key key for which the attribute value must returned
     * @return atribute value for given key
     */
    @Override
    public Object get(final String _select) throws EFapsException
    {
        try {
            final int idx = _select.indexOf(".");
            Object ret = null;
            if (idx > 0) {
                // differ select expression from sub expression
                final String select = _select.substring(0, idx);
                final String subSel = _select.substring(idx + 1);
                // evalute sub select expression for given id
                final ListQuery subQuery = this.subSelects.get(select);
                Object key = this.query.getValue(select);
                if (key instanceof AbstractAdminObject) {
                    key = ((AbstractAdminObject) key).getId();
                }
                if (subQuery.gotoKey(key)) {
                    ret = subQuery.get(subSel);
                }
            } else if (this.multiSelects.contains(_select)) {
                final ListQuery subQuery = this.subSelects.get(_select);
                final Object key = this.query.getValue(_select);
                if (subQuery.gotoKey(key)) {
                    ret = subQuery.query.getMultiLineValue();
                }
            } else {
                ret = this.query.getValue(_select);
            }
            return ret;
        } catch (final Exception e) {
            throw (new EFapsException(this.getClass(), "get", e));
        }

    }

    @Override
    public Type getType()
    {
        return this.query.getType();
    }

    public Instance getInstance()
    {
        return this.query.getInstance();
    }

    public List<Instance> getInstances(final String _select)
    {
        final List<Instance> ret = new ArrayList<Instance>();
        if (this.multiSelects.contains(_select)) {
            final ListQuery subQuery = this.subSelects.get(_select);
            ret.addAll(subQuery.query.getInstances());
        } else {
            ret.addAll(this.instances);
        }
        return ret;
    }

    public Map<String, ListQuery> getSubSelects()
    {
        return this.subSelects;
    }

    /**
     * The instance method returns for the given key the atribute.
     *
     * @param _key key for which the attribute value must returned
     * @return attribute for given key
     * @throws EFapsException
     */
    @Override
    public Attribute getAttribute(final String _select) throws EFapsException
    {
        final int idx = _select.indexOf(".");
        Attribute ret = null;
        if (idx > 0) {
            // differ select expression from sub expression
            final String select = _select.substring(0, idx);
            final String subSel = _select.substring(idx + 1);
            if (select.contains("\\")) {
                ret = this.query.getAttribute(select);
            } else {
                // evalute sub select expression for given id
                final ListQuery subQuery = this.subSelects.get(select);
                if (subQuery.gotoKey(this.query.getValue(select))) {
                    ret = subQuery.getAttribute(subSel);
                }
            }
        } else {
            ret = this.query.getAttribute(_select);
        }
        return ret;
    }

    /**
     * Returns a string representation of this .
     *
     * @return string representation of this
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("selects", this.selects.toString())
                        .append("subSelects", this.subSelects.toString()).toString();
    }

    /**
     * @return the expand
     */
    public AttributeSet getAttributeSet()
    {
        return this.attributeSet;
    }
}
