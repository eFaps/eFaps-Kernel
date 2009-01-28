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

package org.efaps.db.query;

/**
 * The class is used to build a complete statement with correct order etc.
 * The class is working like a string buffer.
 *
 * @author tmo
 * @version $Id$
 */
public class CompleteStatement  {

  public void appendUnion()  {
    append(" union ");
expr = false;
    setWhere(false);
    setFrom(false);
  }


boolean expr = false;

  public void appendWhereAnd()  {
    if (hasWhere() && expr)  {
      append(" and ");
expr = false;
    }
  }


  /**
   *
   */
  public CompleteStatement appendWhere(String _where)  {
    if (!hasWhere())  {
      append(" where ");
      setWhere(true);
    }
    append(_where);
expr = true;
    return this;
  }

  /**
   *
   */
  public CompleteStatement appendWhere(int _where)  {
    if (!hasWhere())  {
      append(" where ");
      setWhere(true);
    }
    append(_where);
expr = true;
    return this;
  }

  /**
   *
   */
  public CompleteStatement appendWhere(long _where)  {
    if (!hasWhere())  {
      append(" where ");
      setWhere(true);
    }
    append(_where);
expr = true;
    return this;
  }

  /**
   *
   */
  public CompleteStatement appendFrom(String _from)  {
    if (!hasFrom())  {
      append(" from ");
      setFrom(true);
    } else  {
      append(",");
    }
    append(_from);
    return this;
  }

  /**
   *
   */
  public CompleteStatement append(long _text)  {
    getStatement().append(_text);
    return this;
  }

  /**
   *
   */
  public CompleteStatement append(int _text)  {
    getStatement().append(_text);
    return this;
  }

  /**
   *
   */
  public CompleteStatement append(String _text)  {
    getStatement().append(_text);
    return this;
  }

  ///////////////////////////////////////////////////////////////////////////

  private StringBuilder statement = new StringBuilder();

  /**
   * The instance variable stores if a where clause must be appended.
   *
   * @see #hasWhere
   */
  private boolean where = false;

  /**
   * The instance variable stores if a from clause must be appended.
   *
   * @see #hasWhere
   */
  private boolean from = false;

  ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #selectStatement}.
   *
   * @return value of instance variable {@link #selectStatement}
   * @see #selectStatement
   */
  public StringBuilder getStatement()  {
    return this.statement;
  }

  /**
   * This is the getter method for instance variable {@link #where}.
   *
   * @return value of instance variable {@link #where}
   * @see #where
   */
  private boolean hasWhere()  {
    return this.where;
  }

  /**
   * This is the setter method for instance variable {@link #where}.
   *
   * @param _where new value for instance variable {@link #where}
   * @see #where
   * @see #getWhere
   */
  private void setWhere(boolean _where)  {
    this.where = _where;
  }

  /**
   * This is the getter method for instance variable {@link #from}.
   *
   * @return value of instance variable {@link #from}
   * @see #from
   */
  private boolean hasFrom()  {
    return this.from;
  }

  /**
   * This is the setter method for instance variable {@link #from}.
   *
   * @param _from new value for instance variable {@link #from}
   * @see #from
   * @see #getFrom
   */
  private void setFrom(boolean _from)  {
    this.from = _from;
  }
}
