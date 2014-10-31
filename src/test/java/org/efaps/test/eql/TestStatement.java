/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.test.eql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.eql.IStatement;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TestStatement
    implements IStatement
{

    private final List<String> types = new ArrayList<>();

    private final List<String> selects = new ArrayList<>();

    private final List<String> parameters = new ArrayList<>();

    private final Map<String, String> selects2alias = new HashMap<>();

    private final List<String> selectEsjps = new ArrayList<>();

    private final Map<String, String> selectEsjps2alias = new HashMap<>();

    private final Map<String, String> attr2whereEq = new HashMap<>();

    private final Map<String, Collection<String>> attr2whereIn = new HashMap<>();

    private final Map<String, String> attr2whereGreater = new HashMap<>();

    private final Map<String, String> attr2whereLess = new HashMap<>();

    private String object;

    private String esjp;

    private StmtType stmtType;

    /**
     * {@inheritDoc}
     */
    @Override
    public void addType(final String _type)
    {
        this.types.add(_type);
    }

    /**
     * Getter method for the instance variable {@link #types}.
     *
     * @return value of instance variable {@link #types}
     */
    public List<String> getTypes()
    {
        return this.types;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSelect(final String _select)
    {
        this.selects.add(_select);
    }

    /**
     * Getter method for the instance variable {@link #selects}.
     *
     * @return value of instance variable {@link #selects}
     */
    public List<String> getSelects()
    {
        return this.selects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSelect(final String _select,
                          final String _alias)
    {
        this.selects2alias.put(_select, _alias);
    }

    /**
     * Getter method for the instance variable {@link #selects2alias}.
     *
     * @return value of instance variable {@link #selects2alias}
     */
    public Map<String, String> getSelects2alias()
    {
        return this.selects2alias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWhereAttrEq(final String _attr,
                               final String _value)
    {
        this.attr2whereEq.put(_attr, _value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWhereAttrIn(final String _attr,
                               final Collection<String> _values)
    {
        this.attr2whereIn.put(_attr, _values);
    }

    /**
     * Getter method for the instance variable {@link #attr2where}.
     *
     * @return value of instance variable {@link #attr2where}
     */
    public Map<String, String> getAttr2whereEq()
    {
        return this.attr2whereEq;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObject(final String _oid)
    {
        this.object = _oid;
    }

    /**
     * Getter method for the instance variable {@link #object}.
     *
     * @return value of instance variable {@link #object}
     */
    public String getObject()
    {
        return this.object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStmtType(final StmtType _stmtType)
    {
        this.stmtType = _stmtType;

    }

    /**
     * Getter method for the instance variable {@link #stmtType}.
     *
     * @return value of instance variable {@link #stmtType}
     */
    public StmtType getStmtType()
    {
        return this.stmtType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEsjp(final String _className)
    {
        this.esjp = _className;
    }

    /**
     * Getter method for the instance variable {@link #esjp}.
     *
     * @return value of instance variable {@link #esjp}
     */
    public String getEsjp()
    {
        return this.esjp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWhereAttrGreater(final String _attr,
                                    final String _value)
    {
        this.attr2whereGreater.put(_attr, _value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWhereAttrLess(final String _attr,
                                 final String _value)
    {
        this.attr2whereLess.put(_attr, _value);
    }

    /**
     * Getter method for the instance variable {@link #attr2whereGreater}.
     *
     * @return value of instance variable {@link #attr2whereGreater}
     */
    public Map<String, String> getAttr2whereGreater()
    {
        return this.attr2whereGreater;
    }

    /**
     * Getter method for the instance variable {@link #attr2whereLess}.
     *
     * @return value of instance variable {@link #attr2whereLess}
     */
    public Map<String, String> getAttr2whereLess()
    {
        return this.attr2whereLess;
    }

    /**
     * Getter method for the instance variable {@link #attr2whereIn}.
     *
     * @return value of instance variable {@link #attr2whereIn}
     */
    public Map<String, Collection<String>> getAttr2whereIn()
    {
        return this.attr2whereIn;
    }


    /**
     * Getter method for the instance variable {@link #selectEsjps}.
     *
     * @return value of instance variable {@link #selectEsjps}
     */
    public List<String> getSelectEsjps()
    {
        return this.selectEsjps;
    }


    /**
     * Getter method for the instance variable {@link #selectEsjps2alias}.
     *
     * @return value of instance variable {@link #selectEsjps2alias}
     */
    public Map<String, String> getSelectEsjps2alias()
    {
        return this.selectEsjps2alias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEsjpSelect(final String _className)
    {
        this.selectEsjps.add(_className);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEsjpSelect(final String _className,
                              final String _alias)
    {
        this.selectEsjps2alias.put(_className, _alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addParameter(final String _parameter)
    {
        this.parameters.add(_parameter);
    }


    /**
     * Getter method for the instance variable {@link #parameters}.
     *
     * @return value of instance variable {@link #parameters}
     */
    public List<String> getParameters()
    {
        return this.parameters;
    }
}
