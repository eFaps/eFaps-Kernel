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


package org.efaps.eql;

import java.util.Collection;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface IStatement
{
    /**
     * StatementType.
     */
    public enum StmtType {
        /**
         * ESJP is executed.
         */
        ESJP,
        /**
         * Query is executed.
         */
        QUERY,
        /**
         * Print is executed.
         */
        PRINT;
    }

    /**
     * @param _type Type to be added to the Statement
     */
    public void addType(final String _type);

    /**
     * @param _select Select to be added to the Statement
     */
    public void addSelect(final String _select);

    /**
     * @param _select Select to be added to the Statement
     * @param _alias  alias for the related select
     */
    public void addSelect(final String _select,
                          final String _alias);

    /**
     * @param _attr Select to be added to the Statement
     * @param _value  alias for the related select
     */
    public void addWhereAttrEq(final String _attr,
                               final String _value);

    /**
     * @param _select Select to be added to the Statement
     * @param _value  alias for the related select
     */
    public void addWhereSelectEq(final String _select,
                                 final String _value);
    /**
     * @param _attr     Name of the Attribute
     * @param _values   list of values
     */
    public void addWhereAttrIn(final String _attr,
                               final Collection<String> _values);

    /**
     * @param _select     Name of the Attribute
     * @param _values   list of values
     */
    public void addWhereSelectIn(final String _select,
                                 final Collection<String> _values);

    /**
     * @param _select Select to be added to the Statement
     * @param _alias  alias for the related select
     */
    public void addWhereAttrGreater(final String _attr,
                                    final String _value);

    /**
     * @param _select Select to be added to the Statement
     * @param _alias  alias for the related select
     */
    public void addWhereSelectGreater(final String _select,
                                      final String _value);

    /**
     * @param _select Select to be added to the Statement
     * @param _alias  alias for the related select
     */
    public void addWhereAttrLess(final String _attr,
                                 final String _value);

    /**
     * @param _select Select to be added to the Statement
     * @param _alias  alias for the related select
     */
    public void addWhereSelectLess(final String _select,
                                   final String _value);

    /**
     * @param _oid set the object the Statement will be executed for
     */
    public void setObject(final String _oid);

    /**
     * @param _stmtType set the type of Statement will be executed for
     */
    public void setStmtType(final StmtType _stmtType);

    /**
     * @param _className set the esjp to be executed
     */
    public void setEsjp(final String _className);

    /**
     * @param _parameter parameter to be added to the Statement
     */
    public void addParameter(final String _parameter);

    /**
     * @param _parameter parameter to be added to the Statement
     */
    public void addOrderBy(final String _para, boolean _asc);

}
