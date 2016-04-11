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

package org.efaps.beans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.AbstractPrintQuery;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class work together with the generated classes of the kernel
 * (org.efpas.beans.valueparser) to get the Values for a String expression
 * defined as a DBProperty like: "$< Type> '$< Name>': Details" from the
 * eFaps-DataBase. The Values for an expression ("$< Type>") retrieved from the
 * eFaps-DataBase will be combined with pure Text ("Details").
 *
 * @author The eFaps Team
 *
 */
public class ValueList
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ValueList.class);

    /**
     * Enum used to differ expression parts from text parts.
     */
    public enum TokenType
    {
        /** Used for token of type expression. */
        EXPRESSION,
        /** Used for token of type text. */
        TEXT
    }

    /**
     * Holds the Expressions used in this ValueList.
     */
    private final Set<String> expressions = new HashSet<String>();

    /**
     * Holds the tokens of this ValueList.
     */
    private final ArrayList<Token> tokens = new ArrayList<Token>();

    /**
     * Getter method for instance variable {@link #tokens}.
     *
     * @return value of instance variable {@link #tokens}
     */
    public ArrayList<Token> getTokens()
    {
        return this.tokens;
    }

    /**
     * Get the ValueList.
     *
     * @return String with the Values, which looks like the original
     */
    public String getValueList()
    {
        final StringBuffer buf = new StringBuffer();

        for (final Token token : this.tokens) {
            switch (token.type) {
                case EXPRESSION:
                    buf.append("$<").append(token.value).append(">");
                    break;
                case TEXT:
                    buf.append(token.value);
                    break;
                default:
                    break;
            }
        }
        return buf.toString();
    }

    /**
     * Add an Expression to this ValueList.
     *
     * @param _expression String with the expression
     */
    public void addExpression(final String _expression)
    {
        this.tokens.add(new Token(ValueList.TokenType.EXPRESSION, _expression));
        getExpressions().add(_expression);
    }

    /**
     * Add Text to the Tokens.
     *
     * @param _text Text to be added
     */
    public void addText(final String _text)
    {
        this.tokens.add(new Token(ValueList.TokenType.TEXT, _text));
    }

    /**
     * This method adds the expressions of this ValueList to the given query.
     *
     * @param _print PrintQuery the expressions should be added
     * @throws EFapsException on error
     * @see {@link #makeString(AbstractQuery)}
     */
    public void makeSelect(final AbstractPrintQuery _print)
        throws EFapsException
    {
        for (final String expression : getExpressions()) {
            // TODO remove, only selects allowed
            if (_print.getMainType().getAttributes().containsKey(expression)) {
                _print.addAttribute(expression);
                ValueList.LOG.warn(
                               "The arguments for ValueList must only contain selects. Invalid: '{}' for ValueList: {}",
                                expression, getValueList());
            } else if (expression.contains("[") || expression.equals(expression.toLowerCase())) {
                _print.addSelect(expression);
            } else {
                 ValueList.LOG.warn(
                               "The arguments for ValueList must only contain selects. Invalid: '{}' for ValueList: {}",
                                    expression, getValueList());
            }
        }
    }

    /**
     * This method retrieves the Values from the given PrintQuery and
     * combines them with the Text partes.
     *
     * @param _callInstance instance on which the query was called
     * @param _print        AbstractPrintQuery the ValueString should be retrieved
     * @param _mode         target mode
     * @return String with the actuall Value of this ValueList
     * @throws EFapsException on error
     * @see {@link #makeSelect(AbstractQuery)}
     *
     */
    public String makeString(final Instance _callInstance,
                             final AbstractPrintQuery _print,
                             final TargetMode _mode)
        throws EFapsException
    {
        final StringBuilder buf = new StringBuilder();

        for (final Token token : this.tokens) {
            switch (token.type) {
                case EXPRESSION:
                    Attribute attr = null;
                    Object value = null;
                    if (_print.getMainType().getAttributes().containsKey(token.value)) {
                        attr = _print.getAttribute4Attribute(token.value);
                        value = _print.getAttribute(token.value);
                    } else {
                        attr = _print.getAttribute4Select(token.value);
                        value = _print.getSelect(token.value);
                    }
                    if (attr != null) {
                        buf.append(attr.getAttributeType().getUIProvider().getStringValue(
                                        UIValue.get(null, attr, value)));
                    } else if (value != null) {
                        buf.append(value);
                    }
                    break;
                case TEXT:
                    buf.append(token.value);
                    break;
                default:
                    break;
            }
        }
        return buf.toString();
    }

    /**
     * This is the getter method for the instance variable {@link #expressions}.
     *
     * @return value of instance variable {@link #expressions}
     * @see #expressions
     */
    public Set<String> getExpressions()
    {
        return this.expressions;
    }

    /**
     * This class holds the definitions of the ValueList.
     */
    public final class Token
    {
        /**
         * This instance variable holds the Type of this token.
         */
        private final TokenType type;

        /**
         * This instance variable holds the value of this token.
         */
        private final String value;

        /**
         * Constructor setting the instance variables.
         *
         * @param _type TokenType
         * @param _value Value for the token
         */
        private Token(final TokenType _type,
                      final String _value)
        {
            this.type = _type;
            this.value = _value;
        }

        /**
         * Getter method for instance variable {@link #type}.
         *
         * @return value of instance variable {@link #type}
         */
        public TokenType getType()
        {
            return this.type;
        }

        /**
         * Getter method for instance variable {@link #value}.
         *
         * @return value of instance variable {@link #value}
         */
        public String getValue()
        {
            return this.value;
        }
    }
}
