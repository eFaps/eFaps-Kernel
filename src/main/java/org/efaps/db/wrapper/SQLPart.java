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

package org.efaps.db.wrapper;


/**
 * Enum is used to write the different standard sql part of a sql statement.
 * This serves to elamintaed problems in future implementation of different
 * databases.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public enum SQLPart {
    /** all. */
    ALL("all"),
    /** and. */
    AND("and"),
    /** asc. */
    ASC("asc"),
    /** ,. */
    COMMA(","),
    /** distinct. */
    DISTINCT("distinct"),
    /** desc. */
    DESC("desc"),
    /** Renders as default: "delete". */
    DELETE("delete"),
    /** from. */
    FROM("from"),
    /** =. */
    EQUAL("="),
    /** >. */
    GREATER(">"),
    /** FALSE. */
    FALSE("FALSE"),
    /** in. */
    IN("in"),
    /** Renders as default: "inner". */
    INNER("inner"),
    /** Renders as default: "insert". */
    INSERT("insert"),
    /** Renders as default: "into". */
    INTO("into"),
    /** is. */
    IS("is"),
    /** left. */
    JOIN("join"),
    /** left. */
    LEFT("left"),
    /** <. */
    LESS("<"),
    /** like. */
    LIKE("like"),
    /** limit. */
    LIMIT("limit"),
    /** null. */
    NULL("null"),
    /** not. */
    NOT("not"),
    /** on. */
    ON("on"),
    /** or. */
    OR("or"),
    /** order by. */
    ORDERBY("order by"),
    /** ). */
    PARENTHESIS_CLOSE(")"),
    /** (. */
    PARENTHESIS_OPEN("("),
    /** select. */
    SELECT("select"),
    /**  Renders as default: "set". */
    SET("set"),
    /** .*/
    SPACE(" "),
    /** TRUE. */
    TRUE("TRUE"),
    /** union. */
    UNION("union"),
    /** !=. */
    UNEQUAL("!="),
    /** Renders as default: "update". */
    UPDATE("update"),
    /** upper. */
    UPPER("upper"),
    /** Renders as default: "values". */
    VALUES("values"),
    /** where. */
    WHERE("where");

    /**
     * Default Value.
     */
    private final String defaultValue;

    /**
     * @param _default default value
     */
    private SQLPart(final String _default)
    {
        this.defaultValue = _default;
    }

    /**
     * Getter method for the instance variable {@link #defaultValue}.
     *
     * @return value of instance variable {@link #defaultValue}
     */
    public String getDefaultValue()
    {
        return this.defaultValue;
    }
}

