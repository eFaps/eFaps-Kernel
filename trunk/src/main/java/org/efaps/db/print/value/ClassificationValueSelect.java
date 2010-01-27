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

package org.efaps.db.print.value;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.print.OneSelect;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationValueSelect
    extends OIDValueSelect
{
    /**
     * Mapping of the instances to classIds. Used as temporary cache.
     */
    private final Map<Instance, List<Long>> instances2classId = new HashMap<Instance, List<Long>>();

    /**
     * Have the values been retrieved allready.
     */
    private boolean retrieved = false;

    /**
     * @param _oneSelect OneSelect
     */
    public ClassificationValueSelect(final OneSelect _oneSelect)
    {
        super(_oneSelect);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "classification";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final Object _object)
        throws EFapsException
    {
        final ArrayList<Classification> ret = new ArrayList<Classification>();
        final String oid = (String) super.getValue(_object);
        // TODO the execution of getting the value should be done on the execute
        // of the query!
        if (!this.retrieved) {
            getValues4Instances(oid);
            this.retrieved = true;
        }

        if (getChildValueSelect() == null) {
            throw new EFapsException(ClassificationValueSelect.class, "notyet");
        } else {
            if (oid != null) {
                if ("type".equals(getChildValueSelect().getValueType())) {
                    final Instance instance = Instance.get(oid);
                    ret.addAll(getClassification(this.instances2classId.get(instance)));
                }
            }
        }
        return ret.size() > 0 ? ret : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final List<Object> _objectList)
        throws EFapsException
    {
        final List<Object> ret = new ArrayList<Object>();
        for (final Object object : _objectList) {
            ret.add(getValue(object));
        }
        return _objectList.size() > 0 ? (ret.size() > 1 ? ret : ret.get(0)) : null;
    }

    /**
     * Method to get only the leaf of the classification tree.
     *
     * @param _classIds List of classids
     * @return set
     */
    private Set<Classification> getClassification(final List<Long> _classIds)
    {
        final Set<Classification> noadd = new HashSet<Classification>();
        final Set<Classification> add = new HashSet<Classification>();
        if (_classIds != null) {
            for (final Long id : _classIds) {
                Classification clazz = (Classification) Type.get(id);
                if (!noadd.contains(clazz)) {
                    add.add(clazz);
                    while (clazz.getParentClassification() != null) {
                        clazz = (Classification) clazz.getParentClassification();
                        if (add.contains(clazz)) {
                            add.remove(clazz);
                        }
                        noadd.add(clazz);
                    }
                }
            }
        }
        return add;
    }

    /**
     * Method to get the values for the instances.
     *
     * @throws EFapsException on error
     */
    private void getValues4Instances(final String _oid) throws EFapsException
    {
        // group the instance by type
        // check if the current instance is the list (happens if this
        // value select is e.g. part of a linkto
        final Map<Type, List<Instance>> type2instance = new HashMap<Type, List<Instance>>();
        final Instance currentInst = Instance.get(_oid);
        final List<Instance> instances = new ArrayList<Instance>();
        if (getOneSelect().getQuery().getInstanceList().contains(currentInst)) {
           instances.addAll(getOneSelect().getQuery().getInstanceList());
        } else {
            for (final Object object : getOneSelect().getObjectList()) {
               final String oid =  (String) super.getValue(object);
                   instances.add(Instance.get(oid));
              }
        }

        for (final Instance instance : instances) {
            List<Instance> tmpList;
            if (type2instance.containsKey(instance.getType())) {
                tmpList = type2instance.get(instance.getType());
            } else {
                tmpList = new ArrayList<Instance>();
                type2instance.put(instance.getType(), tmpList);
            }
            tmpList.add(instance);
        }

        // make one union part for every type
        for (final Entry<Type, List<Instance>> entry : type2instance.entrySet()) {
            final StringBuilder selBldr = new StringBuilder();
            boolean union = false;
            final Set<Classification> classTypes = new HashSet<Classification>();
            Type curr = entry.getKey();
            while (curr.getParentType() != null) {
                classTypes.addAll(curr.getClassifiedByTypes());
                curr = curr.getParentType();
            }
            classTypes.addAll(curr.getClassifiedByTypes());
            for (final Classification clazz : classTypes) {
                final Attribute typeAttr = clazz.getClassifyRelationType()
                                .getAttribute(clazz.getRelTypeAttributeName());
                final Attribute linkAttr = clazz.getClassifyRelationType()
                                .getAttribute(clazz.getRelLinkAttributeName());
                if (union) {
                    selBldr.append(" union all ");
                } else {
                    union = true;
                }
                selBldr.append(" select T0.ID,T0.").append(linkAttr.getSqlColNames().get(0)).append(",T0.")
                    .append(typeAttr.getSqlColNames().get(0))
                    .append(" from ").append(clazz.getClassifyRelationType().getMainTable().getSqlTable()).append(" T0")
                    .append(" where T0.").append(linkAttr.getSqlColNames().get(0)).append(" in (");
                boolean first = true;
                for (final Instance instance : entry.getValue()) {
                    if (first) {
                        first = false;
                    } else {
                        selBldr.append(",");
                    }
                    selBldr.append(instance.getId());
                }
                selBldr.append(")");
                if (clazz.getClassifyRelationType().getMainTable().getSqlColType() != null) {
                    selBldr.append(" and TO.").append(clazz.getClassifyRelationType().getMainTable().getSqlColType())
                        .append("=").append(clazz.getClassifyRelationType().getId());
                }
            }
            if (classTypes.size() > 0) {
                executeOneCompleteStmt(selBldr, entry.getValue());
            }
        }
    }

    /**
     * Method to execute one statement against the eFaps-DataBase.
     *
     * @param _complStmt ststement
     * @param _instances list of instances
     * @return true it values where found
     * @throws EFapsException on error
     */
    protected boolean executeOneCompleteStmt(final StringBuilder _complStmt,
                                             final List<Instance> _instances)
        throws EFapsException
    {
        final boolean ret = false;
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();

            final Statement stmt = con.getConnection().createStatement();

            final ResultSet rs = stmt.executeQuery(_complStmt.toString());
            final Map<Long, List<Long>> link2clazz = new HashMap<Long, List<Long>>();
            while (rs.next()) {
                final long linkId = rs.getLong(2);
                final long classificationID = rs.getLong(3);
                List<Long> templ;
                if (link2clazz.containsKey(linkId)) {
                    templ = link2clazz.get(linkId);
                } else {
                    templ = new ArrayList<Long>();
                    link2clazz.put(linkId, templ);
                }
                templ.add(classificationID);
            }
            for (final Instance instance : _instances) {
                this.instances2classId.put(instance, link2clazz.get(instance.getId()));
            }
            rs.close();
            stmt.close();
            con.commit();
        } catch (final EFapsException e) {
            if (con != null) {
                con.abort();
            }
            throw e;
        } catch (final Throwable e) {
            if (con != null) {
                con.abort();
            }
            // TODO: exception eintragen!
            throw new EFapsException(getClass(), "executeOneCompleteStmt.Throwable", e);
        }
        return ret;
    }

}
