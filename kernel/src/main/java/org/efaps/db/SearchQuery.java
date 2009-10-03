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

import java.util.StringTokenizer;

import org.joda.time.ReadableDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.query.WhereClauseAttrEqAttr;
import org.efaps.db.query.WhereClauseAttributeEqualValue;
import org.efaps.db.query.WhereClauseAttributeGreaterValue;
import org.efaps.db.query.WhereClauseAttributeLessValue;
import org.efaps.db.query.WhereClauseAttributeMatchValue;
import org.efaps.db.query.WhereClauseAttributeNotEqualValue;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 * @todo description
 */
public class SearchQuery extends AbstractQuery
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SearchQuery.class);

    /**
     *
     */
    public SearchQuery()
    {
    }


    public void setQueryTypes(final String _types) throws EFapsException
    {
        setQueryTypes(_types, true);
    }

    public void setQueryTypes(final String _types, final boolean _companyDepend) throws EFapsException
    {
        if (_types != null) {
            this.type = Type.get(_types);
            setExpandChildTypes(false);
            addSelect(true, this.type, this.type, "OID");
            this.types.add(this.type);
            if (_companyDepend && this.type.isCompanyDepended()) {
                if (Context.getThreadContext().getCompany() == null) {
                    throw new EFapsException(SearchQuery.class, "noCompany");
                }
                addWhereAttrEqValue(this.type.getCompanyAttribute(), Context.getThreadContext().getCompany().getId());
            }
        }
    }



    /**
   *
   */
    public void setObject(final Instance _instance) throws EFapsException
    {
        final Type type = _instance.getType();
        addSelect(true, type, type, "OID");
        this.types.add(this.type);
        this.type = type;
        addWhereExprEqValue("ID", "" + _instance.getId());
    }

    /**
     * @see #setObject(Instance)
     */
    public void setObject(final String _oid) throws EFapsException
    {
        setObject(Instance.get(_oid));
    }

    /**
     * @see #setObject(Instance)
     */
    public void setObject(final Type _type, final long _id) throws EFapsException
    {
        setObject(Instance.get(_type, _id));
    }

    /**
   *
   */
    public void setExpand(final String _oid, final String _expand) throws EFapsException
    {
        setExpand(Instance.get(_oid), _expand);
    }

    public void setExpand(final Instance _instance, final String _expand) throws EFapsException
    {
        setExpand(_instance, _expand, true);
    }

    /**
     * @todo Exception
     */
    public void setExpand(final Instance _instance, final String _expand, final boolean _companyDepend) throws EFapsException
    {
        final StringTokenizer tokens = new StringTokenizer(_expand, ".");
        boolean first = true;
        Type type = _instance.getType();

        while (tokens.hasMoreTokens()) {
            final String one = tokens.nextToken();
            Attribute attr = type.getLinks().get(one);
            if (attr == null) {
                attr = type.getAttribute(one);
            }
            if (attr == null) {
                LOG.error("Could not found attribute or link with name " + "'" + one + "' for type '" + type.getName()
                                + "'");
                throw new EFapsException(getClass(), "setExpand.AttributeOrLinkNotFound", one, type.getName());
            }
            if (type.isKindOf(attr.getLink())) {
                type = attr.getParent();
            } else {
                type = attr.getLink();
            }
            addTypes4Order(type);
            if (first) {
                addWhereAttrEqValue(attr, _instance.getId());
                first = false;
            } else {
                addWhereAttrEqAttr(attr, type.getAttribute("ID"));
            }

            addSelect(true, type, type, "OID");
            this.types.add(type);

        }
        this.type = type;

        if (_companyDepend && this.type.isCompanyDepended()) {
            addWhereAttrEqValue(this.type.getCompanyAttribute(), Context.getThreadContext().getCompany().getId());
        }
    }

    public void addWhereExprNotEqValue(final String _expr, final String _value) throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_expr);
        if (attr == null) {
            LOG.debug("unknown expression '" + _expr + "' for type " + "'" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprEqValue", "UnknownExpression", _expr, this.type.getName());
        }
        getMainWhereClauses().add(new WhereClauseAttributeNotEqualValue(this, attr, _value));
    }

    /**
     * @param _expr
     * @param _value
     */
    public void addWhereExprNotEqValue(final String _expr, final long _value) throws EFapsException
    {
        addWhereExprNotEqValue(_expr, "" + _value);
    }

   /**
     * @param _expr expression to compare for equal
     * @param _value value to compare for equal
     */
    public void addWhereExprEqValue(final String _expr, final String _value) throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_expr);
        if (attr == null) {
            LOG.debug("unknown expression '" + _expr + "' for type " + "'" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprEqValue", "UnknownExpression", _expr, this.type.getName());
        }
        getMainWhereClauses().add(new WhereClauseAttributeEqualValue(this, attr, _value));
    }

    /**
     * @param _expr
     * @param _value
     */
    public void addWhereExprEqValue(final String _expr, final long _value) throws EFapsException
    {
        addWhereExprEqValue(_expr, "" + _value);
    }

    public void addWhereExprEqValue(final String _expr, final ReadableDateTime _dateTime) throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_expr);
        if (attr == null) {
            LOG.debug("unknown expression '" + _expr + "' for type " + "'" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprEqValue", "UnknownExpression", _expr, this.type.getName());
        }
        getMainWhereClauses().add(
                        new WhereClauseAttributeEqualValue(this, attr, _dateTime.toDateTime().toString(
                                        ISODateTimeFormat.dateTime())));
    }

    /**
     * @param _context eFaps context for this request
     * @param _expr expression to compare for equal
     * @param _value value to compare for equal
     */
    public void addWhereExprMatchValue(final String _expr, final String _value) throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_expr);
        if (attr == null) {
            LOG.debug("unknown expression '" + _expr + "' for type " + "'" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprMatchValue", "UnknownExpression", _expr, this.type
                            .getName());
        }
        getMainWhereClauses().add(new WhereClauseAttributeMatchValue(this, attr, _value));
    }

    /**
     * @param _expr expression to compare for greater
     * @param _value value to compare for equal
     */
    public void addWhereExprGreaterValue(final String _expr, final String _value) throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_expr);
        if (attr == null) {
            LOG.debug("unknown expression '" + _expr + "' for type " + "'" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprGreaterValue", "UnknownExpression", _expr, this.type
                            .getName());
        }
        getMainWhereClauses().add(new WhereClauseAttributeGreaterValue(this, attr, _value));
    }

    public void addWhereExprGreaterValue(final String _expr, final ReadableDateTime _dateTime) throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_expr);
        // TODO check if Attribute is DateTimeType
        if (attr == null) {
            LOG.debug("unknown expression '" + _expr + "' for type " + "'" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprGreaterValue", "UnknownExpression", _expr, this.type
                            .getName());
        }
        getMainWhereClauses().add(
                        new WhereClauseAttributeGreaterValue(this, attr, _dateTime.toDateTime().toString(
                                        ISODateTimeFormat.dateTime())));
    }

    /**
     * @param _expr expression to compare for less
     * @param _value value to compare for equal
     */
    public void addWhereExprLessValue(final String _expr, final String _value) throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_expr);
        if (attr == null) {
            LOG.debug("unknown expression '" + _expr + "' for type " + "'" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprLessValue", "UnknownExpression", _expr, this.type
                            .getName());
        }
        getMainWhereClauses().add(new WhereClauseAttributeLessValue(this, attr, _value));
    }

    /**
     * @param _expr expression to compare for less
     * @param _value value to compare for equal
     */
    public void addWhereExprLessValue(final String _expr, final ReadableDateTime _dateTime) throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_expr);
        if (attr == null) {
            LOG.debug("unknown expression '" + _expr + "' for type " + "'" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprLessValue", "UnknownExpression", _expr, this.type
                            .getName());
        }
        getMainWhereClauses().add(
                        new WhereClauseAttributeLessValue(this, attr, _dateTime.toDateTime().toString(
                                        ISODateTimeFormat.dateTime())));
    }

    /**
     * @param _attr
     * @param _value
     */
    public void addWhereAttrEqValue(final Attribute _attr, final String _value) throws EFapsException
    {
        getMainWhereClauses().add(new WhereClauseAttributeEqualValue(this, _attr, _value));
    }

    /**
     * @param _attr
     * @param _value
     */
    public void addWhereAttrEqValue(final Attribute _attr, final long _value) throws EFapsException
    {
        getMainWhereClauses().add(new WhereClauseAttributeEqualValue(this, _attr, "" + _value));
    }

    /**
     * @param _attr1
     * @param _attr2
     */
    public void addWhereAttrEqAttr(final Attribute _attr1, final Attribute _attr2) throws EFapsException
    {
        getMainWhereClauses().add(new WhereClauseAttrEqAttr(this, _attr1, _attr2));
    }
}
