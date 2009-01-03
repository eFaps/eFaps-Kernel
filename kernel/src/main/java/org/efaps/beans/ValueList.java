/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.beans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.ui.FieldDefinition;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.db.AbstractQuery;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * This class work together with the generated classes of the kernel
 * (org.efpas.beans.valueparser) to get the Values for a String expression
 * defined as a DBProperty like: "$< Type> '$< Name>': Details" from the
 * eFaps-DataBase. The Values for an expression ("$< Type>") retrieved from the
 * eFaps-DataBase will be combined with pure Text ("Details").
 *
 * @author tmo
 * @version $Id$
 */
public class ValueList {

  /**
   * enum used to differ expression parts from text parts
   */
  public enum TokenType {
    EXPRESSION,
    TEXT
  }

  /**
   * holds the Expressions used in this ValueList
   */
  private final Set<String> expressions = new HashSet<String>();

  /**
   * holds the tokens of this ValueList
   */
  private final ArrayList<Token> tokens = new ArrayList<Token>();

  /**
   * get the ValueList
   *
   * @return String with the Values, wich looks like the original
   */
  public String getValueList() {
    final StringBuffer buf = new StringBuffer();

    for (final Token token : this.tokens) {
      switch (token.type) {
        case EXPRESSION:
          buf.append("$<").append(token.value).append(">");
          break;
        case TEXT:
          buf.append(token.value);
          break;
      }
    }
    return buf.toString();
  }

  /**
   * add an Expression to this ValueList
   *
   * @param _expression
   *                String with the expression
   */
  public void addExpression(final String _expression) {
    this.tokens.add(new Token(TokenType.EXPRESSION, _expression));
    getExpressions().add(_expression);
  }

  /**
   * add Text to the Tokens
   *
   * @param _text
   *                Text to be added
   */
  public void addText(final String _text) {
    this.tokens.add(new Token(TokenType.TEXT, _text));
  }

  /**
   * This method adds the expressions of this ValueList to the given query
   *
   * @param _query
   *                AbstractQuery the expressions should be added
   * @throws EFapsException
   * @see {@link #makeString(AbstractQuery)}
   */
  public void makeSelect(final AbstractQuery _query) throws EFapsException {
    for (final String expression : getExpressions()) {
      _query.addSelect(expression);
    }
  }

  /**
   * This method retrieves the Values from the given AbstractQuery and combines
   * them with the Text partes.
   *
   * @param _callInstance instance on which the query was called
   * @param _query        AbstractQuery the ValueString should be retrieved
   * @return String with the actuall Value of this ValueList
   * @throws Exception
   * @see {@link #makeSelect(AbstractQuery)}
   */
  public String makeString(final Instance _callInstance,
                           final AbstractQuery _query)
      throws Exception
  {
    final StringBuilder buf = new StringBuilder();

    for (final Token token : this.tokens) {
      switch (token.type) {
        case EXPRESSION:
          final Attribute attr = _query.getAttribute(token.value);
          final Object value = _query.get(token.value);
          buf.append((new FieldValue(new FieldDefinition(null, null),
                                     attr,
                                     value,
                                     null)).getViewHtml(_callInstance, null));
          break;
        case TEXT:
          buf.append(token.value);
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
  public Set<String> getExpressions() {
    return this.expressions;
  }

  /**
   * this private class holds the Definitios of the ValueList
   */
  private class Token {

    /**
     * this instance variable holds the Type of this token
     */
    private final TokenType type;

    /**
     * this instance variable holds the value of this token
     */
    private final String value;

    /**
     * Constructor setting the instance variables
     *
     * @param _type
     * @param _value
     */
    Token(final TokenType _type, final String _value) {
      this.type = _type;
      this.value = _value;
    }

  }

}
