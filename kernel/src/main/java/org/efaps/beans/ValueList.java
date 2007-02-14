/*
 * Copyright 2005 The eFaps Team
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
import org.efaps.db.SearchQuery;
import org.efaps.db.Context;

public class ValueList  {



  public String getValueList()  {
    StringBuffer buf = new StringBuffer();

    for (Token token : this.tokens)  {
      switch (token.type)  {
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


  public void addExpression(String _expression)  {
this.tokens.add(new Token(TokenType.EXPRESSION, _expression));
getExpressions().add(_expression);
  }

  public void addText(String _text)  {
this.tokens.add(new Token(TokenType.TEXT, _text));
  }


  public void makeSelect(Context _context, SearchQuery _query) throws Exception  {
    for (String expression : getExpressions())  {
      _query.addSelect(_context, expression);
    }
  }


  public String makeString(Context _context, SearchQuery _query) throws Exception  {
    StringBuffer buf = new StringBuffer();

    for (Token token : this.tokens)  {
      switch (token.type)  {
        case EXPRESSION:
//          buf.append(_query.get(_context, token.value));
Attribute attr = _query.getAttribute(_context, token.value);
Object value = _query.get(_context, token.value);
buf.append(attr.getAttributeType().getUI().getViewHtml(_context, value, null));
          break;
        case TEXT:
          buf.append(token.value);
          break;
      }
    }

    return buf.toString();
  }


  ///////////////////////////////////////////////////////////////////////////

  private ArrayList<Token> tokens = new ArrayList<Token>();

  private Set<String> expressions = new HashSet<String>();

  ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for the instance variable {@link #expressions}.
   *
   * @return value of instance variable {@link #expressions}
   * @see #expressions
   */
  public Set<String> getExpressions()  {
    return this.expressions;
  }


  ///////////////////////////////////////////////////////////////////////////

  public enum TokenType {EXPRESSION, TEXT};

  private class Token  {

    Token(TokenType _type, String _value)  {
      this.type = _type;
      this.value = _value;
    }

    private final TokenType type;
    private final String value;
  }


}
