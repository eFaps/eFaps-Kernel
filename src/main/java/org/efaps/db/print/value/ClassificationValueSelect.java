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
package org.efaps.db.print.value;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.print.OneSelect;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class ClassificationValueSelect
    extends OIDValueSelect
{
    /**
     * Mapping of the instances to classIds. Used as temporary cache.
     */
    private final Map<Instance, List<Long>> instances2classId = new HashMap<>();

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
        final ArrayList<Object> ret = new ArrayList<>();
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
                    final Set<Classification> clazzes = getClassification(this.instances2classId.get(instance));
                    for (final Classification clazz : clazzes) {
                        final Object tmp = ((TypeValueSelect) getChildValueSelect())
                                        .analyzeChildValue(getChildValueSelect(), clazz);
                        ret.add(tmp);
                    }
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
        final List<Object> ret = new ArrayList<>();
        for (final Object object : _objectList) {
            ret.add(getValue(object));
        }
        return _objectList.size() > 0 ? ret.size() > 1 ? ret : ret.get(0) : null;
    }

    /**
     * Method to get only the leaf of the classification tree.
     *
     * @param _classIds List of classids
     * @return set
     * @throws CacheReloadException on error
     */
    private Set<Classification> getClassification(final List<Long> _classIds)
        throws CacheReloadException
    {
        final Set<Classification> noadd = new HashSet<>();
        final Set<Classification> add = new HashSet<>();
        if (_classIds != null) {
            for (final Long id : _classIds) {
                Classification clazz = (Classification) Type.get(id);
                if (!noadd.contains(clazz)) {
                    add.add(clazz);
                    while (clazz.getParentClassification() != null) {
                        clazz = clazz.getParentClassification();
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
     * @param _oid oid of the current instance
     * @throws EFapsException on error
     */
    private void getValues4Instances(final String _oid)
        throws EFapsException
    {
        // group the instance by type
        // check if the current instance is the list (happens if this
        // value select is e.g. part of a linkto
        final Map<Type, List<Instance>> type2instance = new HashMap<>();
        final Instance currentInst = Instance.get(_oid);
        final List<Instance> instances = new ArrayList<>();
        if (getOneSelect().getQuery().getInstanceList().contains(currentInst)) {
            instances.addAll(getOneSelect().getQuery().getInstanceList());
        } else {
            for (final Object object : getOneSelect().getObjectList()) {
                final String oid =  (String) super.getValue(object);
                instances.add(Instance.get(oid));
            }
        }

        for (final Instance instance : instances) {
            final List<Instance> tmpList;
            if (type2instance.containsKey(instance.getType())) {
                tmpList = type2instance.get(instance.getType());
            } else {
                tmpList = new ArrayList<>();
                type2instance.put(instance.getType(), tmpList);
            }
            tmpList.add(instance);
        }

        // make one union part for every type
        for (final Entry<Type, List<Instance>> entry : type2instance.entrySet()) {

            boolean union = false;
            final Set<Classification> classTypes = new HashSet<>();
            Type curr = entry.getKey();
            while (curr.getParentType() != null) {
                classTypes.addAll(curr.getClassifiedByTypes());
                curr = curr.getParentType();
            }
            classTypes.addAll(curr.getClassifiedByTypes());
            final SQLSelect unionSelect = new SQLSelect();
            for (final Classification clazz : classTypes) {

                final Attribute typeAttr = clazz.getClassifyRelationType()
                                .getAttribute(clazz.getRelTypeAttributeName());
                final Attribute linkAttr = clazz.getClassifyRelationType()
                                .getAttribute(clazz.getRelLinkAttributeName());
                final SQLSelect select;
                if (union) {
                    unionSelect.addPart(SQLPart.UNION).addPart(SQLPart.ALL);
                    select = new SQLSelect();
                } else {
                    select = unionSelect;
                    union = true;
                }
                select.column(0, "ID")
                    .column(0, linkAttr.getSqlColNames().get(0))
                    .column(0, typeAttr.getSqlColNames().get(0))
                    .from(clazz.getClassifyRelationType().getMainTable().getSqlTable(), 0)
                    .addPart(SQLPart.WHERE)
                    .addColumnPart(0, linkAttr.getSqlColNames().get(0))
                    .addPart(SQLPart.IN).addPart(SQLPart.PARENTHESIS_OPEN);

                boolean first = true;
                for (final Instance instance : entry.getValue()) {
                    if (first) {
                        first = false;
                    } else {
                        select.addPart(SQLPart.COMMA);
                    }
                    select.addValuePart(instance.getId());
                }
                select.addPart(SQLPart.PARENTHESIS_CLOSE);
                if (clazz.getClassifyRelationType().getMainTable().getSqlColType() != null) {
                    select.addPart(SQLPart.AND)
                        .addColumnPart(0, clazz.getClassifyRelationType().getMainTable().getSqlColType())
                        .addPart(SQLPart.EQUAL).addValuePart(clazz.getClassifyRelationType().getId());
                }
                if (union && !select.equals(unionSelect)) {
                    unionSelect.addNestedSelectPart(select.getSQL());
                }
            }
            if (classTypes.size() > 0) {
                executeOneCompleteStmt(unionSelect.getSQL(), entry.getValue());
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
    protected boolean executeOneCompleteStmt(final String _complStmt,
                                             final List<Instance> _instances)
        throws EFapsException
    {
        final boolean ret = false;
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();

            final Statement stmt = con.createStatement();

            final ResultSet rs = stmt.executeQuery(_complStmt.toString());
            final Map<Long, List<Long>> link2clazz = new HashMap<>();
            while (rs.next()) {
                final long linkId = rs.getLong(2);
                final long classificationID = rs.getLong(3);
                final List<Long> templ;
                if (link2clazz.containsKey(linkId)) {
                    templ = link2clazz.get(linkId);
                } else {
                    templ = new ArrayList<>();
                    link2clazz.put(linkId, templ);
                }
                templ.add(classificationID);
            }
            for (final Instance instance : _instances) {
                this.instances2classId.put(instance, link2clazz.get(instance.getId()));
            }
            rs.close();
            stmt.close();
        } catch (final SQLException e) {
            throw new EFapsException(ClassificationValueSelect.class, "executeOneCompleteStmt", e);
        }
        return ret;
    }
}
