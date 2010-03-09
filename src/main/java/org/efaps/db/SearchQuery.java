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
import java.util.UUID;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.query.WhereClause;
import org.efaps.db.query.WhereClauseAttrEqAttr;
import org.efaps.db.query.WhereClauseAttributeEqualValue;
import org.efaps.db.query.WhereClauseAttributeGreaterValue;
import org.efaps.db.query.WhereClauseAttributeLessValue;
import org.efaps.db.query.WhereClauseAttributeMatchValue;
import org.efaps.db.query.WhereClauseAttributeNotEqualValue;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id$ TODO:
 *          description
 */
public class SearchQuery
    extends AbstractQuery
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

    /**
     * Set the type for the query.
     *
     * @param _uuid UUID of the type to be used
     * @throws EFapsException on error
     * @return this SearchQuery
     */
    public SearchQuery setQueryTypes(final UUID _uuid)
        throws EFapsException
    {
        return setQueryTypes(_uuid, true);
    }

    /**
     * Set the type for the query.
     *
     * @param _uuid UUID of the type to be used
     * @param _companyDepend if <code>true</code> the company will be included
     *            if necessary automatically
     * @throws EFapsException on error
     * @return this SearchQuery
     */
    public SearchQuery setQueryTypes(final UUID _uuid,
                                     final boolean _companyDepend)
        throws EFapsException
    {
        return setQueryTypes(Type.get(_uuid), _companyDepend);
    }

    /**
     * Set the type for the query.
     *
     * @param _types type to be used
     * @throws EFapsException on error
     * @return this SearchQuery
     */
    public SearchQuery setQueryTypes(final String _types)
        throws EFapsException
    {
        return setQueryTypes(Type.get(_types), true);
    }

    /**
     * Set the type for the query.
     *
     * @param _type type to be used
     * @param _companyDepend if <code>true</code> the company will be included
     *            if necessary automatically
     * @throws EFapsException on error
     * @return this SearchQuery
     */
    public SearchQuery setQueryTypes(final Type _type,
                                     final boolean _companyDepend)
        throws EFapsException
    {
        if (_type != null) {
            this.type = _type;
            setExpandChildTypes(false);
            addSelect(true, this.type, this.type, "OID");
            this.types.add(this.type);
            if (_companyDepend && this.type.isCompanyDepended()) {
                if (Context.getThreadContext().getCompany() == null) {
                    throw new EFapsException(SearchQuery.class, "noCompany");
                }
                setCompanyClause(new WhereClauseAttributeEqualValue(this,
                                this.type.getCompanyAttribute(),
                                Context.getThreadContext().getCompany().getId()));
            }
        }
        return this;
    }

    /**
     * Set the object for this query.
     *
     * @param _instance Instance
     * @throws EFapsException on error
     * @deprecated use PrintQuery!
     */
    @Deprecated
    public void setObject(final Instance _instance)
        throws EFapsException
    {
        final Type type = _instance.getType();
        addSelect(true, type, type, "OID");
        this.types.add(this.type);
        this.type = type;
        addWhereExprEqValue("ID", "" + _instance.getId());
    }

    /**
     * Set the object for this query.
     *
     * @param _oid oid of the Instance
     * @throws EFapsException on error
     * @see #setObject(Instance)
     * @deprecated use PrintQuery!
     */
    @Deprecated
    public void setObject(final String _oid)
        throws EFapsException
    {
        setObject(Instance.get(_oid));
    }

    /**
     * Set the object for this query.
     *
     * @param _type type for the Instance
     * @param _id id for the Instance
     * @throws EFapsException on error
     * @see #setObject(Instance)
     * @deprecated use PrintQuery!
     */
    @Deprecated
    public void setObject(final Type _type,
                          final long _id)
        throws EFapsException
    {
        setObject(Instance.get(_type, _id));
    }

    /**
   *
   */
    public void setExpand(final String _oid,
                          final String _expand)
        throws EFapsException
    {
        setExpand(Instance.get(_oid), _expand);
    }

    public void setExpand(final Instance _instance,
                          final String _expand)
        throws EFapsException
    {
        setExpand(_instance, _expand, true);
    }

    /**
     * TODO: Exception
     */
    public void setExpand(final Instance _instance,
                          final String _expand,
                          final boolean _companyDepend)
        throws EFapsException
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
                SearchQuery.LOG.error("Could not found attribute or link with name " + "'" + one + "' for type '"
                                + type.getName() + "'");
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

    /**
     * @param _attrName name of the attribute to compare for not equal
     * @param _value    value to compare for equal
     * @throws EFapsException if Attribute is not found
     * @return the added WhereClause
     */
    public WhereClause addWhereExprNotEqValue(final String _attrName,
                                              final Object _value)
        throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_attrName);
        if (attr == null) {
            SearchQuery.LOG.debug("unknown attribute name '" + _attrName + "' for type '" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprEqValue", "UnknownExpression",
                            _attrName, this.type.getName());
        }
        final WhereClause ret = new WhereClauseAttributeNotEqualValue(this, attr, _value);
        getMainWhereClauses().add(ret);
        return ret;
    }

    /**
     * @param _attrName name of the attribute to compare for not equal
     * @param _value    value to compare for equal
     * @throws EFapsException if Attribute is not found
     * @return the added WhereClause
     */
    public WhereClause addWhereExprNotEqValue(final String _attrName,
                                              final long _value)
        throws EFapsException
    {
        return addWhereExprNotEqValue(_attrName, Long.valueOf(_value));
    }

    /**
     * @param _attrName name of the attribute to compare for not equal
     * @param _value    value to compare for equal
     * @throws EFapsException if Attribute is not found
     * @return the added WhereClause
     */
    public WhereClause addWhereExprEqValue(final String _attrName,
                                           final Object _value)
        throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_attrName);
        if (attr == null) {
            SearchQuery.LOG.debug("unknown attribute name '" + _attrName + "' for type '" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprEqValue", "UnknownExpression",
                            _attrName, this.type.getName());
        }
        final WhereClause ret = new WhereClauseAttributeEqualValue(this, attr, _value);
        getMainWhereClauses().add(ret);
        return ret;
    }

    /**
     * @param _attrName name of the attribute to compare for not equal
     * @param _value    value to compare for equal
     * @throws EFapsException if Attribute is not found
     * @return the added WhereClause
     */
    public WhereClause addWhereExprEqValue(final String _attrName,
                                           final long _value)
        throws EFapsException
    {
        return addWhereExprEqValue(_attrName, Long.valueOf(_value));
    }
    /**
     * @param _attrName name of the attribute to compare for match
     * @param _value    value to compare for match
     * @throws EFapsException if Attribute is not found
     * @return the added WhereClause
     */
    public WhereClause addWhereExprMatchValue(final String _attrName,
                                              final Object _value)
        throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_attrName);
        if (attr == null) {
            SearchQuery.LOG.debug("unknown attribute name '" + _attrName + "' for type '" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprMatchValue", "UnknownExpression", _attrName, this.type
                            .getName());
        }
        final WhereClause ret = new WhereClauseAttributeMatchValue(this, attr, _value);
        getMainWhereClauses().add(ret);
        return ret;
    }

    /**
     * @param _attrName     name of the attribute to compare for greater
     * @param _value    value to compare for equal
     * @throws EFapsException if Attribute is not found
     * @return the added WhereClause
     */
    public WhereClause addWhereExprGreaterValue(final String _attrName,
                                                final Object _value)
        throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_attrName);
        if (attr == null) {
            SearchQuery.LOG.debug("unknown attribute name '" + _attrName + "' for type '" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprGreaterValue", "UnknownExpression", _attrName, this.type
                            .getName());
        }
        final WhereClause ret = new WhereClauseAttributeGreaterValue(this, attr, _value);
        getMainWhereClauses().add(ret);
        return ret;
    }

    /**
     * @param _attrName name of the attribute to compare for less
     * @param _value    value to compare for equal
     * @throws EFapsException if Attribute is not found
     * @return the added WhereClause
     */
    public WhereClause addWhereExprLessValue(final String _attrName,
                                             final Object _value)
        throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(_attrName);
        if (attr == null) {
            SearchQuery.LOG.debug("unknown attribute name '" + _attrName + "' for type '" + this.type.getName() + "'");
            throw new EFapsException(getClass(), "addWhereExprLessValue", "UnknownExpression", _attrName, this.type
                            .getName());
        }
        final WhereClause ret = new WhereClauseAttributeLessValue(this, attr, _value);
        getMainWhereClauses().add(ret);
        return ret;
    }

    /**
     * @param _attr     Attribute to be compared to
     * @param _value    value to be compared
     * @throws EFapsException if Attribute is not found
     * @return the added WhereClause
     */
    public WhereClause addWhereAttrEqValue(final Attribute _attr,
                                           final Object _value)
        throws EFapsException
    {
        final WhereClause ret = new WhereClauseAttributeEqualValue(this, _attr, _value);
        getMainWhereClauses().add(ret);
        return ret;
    }

    /**
     * @param _attr1 Attribute to be compared to
     * @param _attr2 Attribute to be compared
     * @throws EFapsException if Attribute is not found
     * @return the added WhereClause
     */
    public WhereClause addWhereAttrEqAttr(final Attribute _attr1,
                                          final Attribute _attr2)
        throws EFapsException
    {
        final WhereClause ret = new WhereClauseAttrEqAttr(this, _attr1, _attr2);
        getMainWhereClauses().add(ret);
        return ret;
    }
}
