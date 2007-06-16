/*
 * Copyright 2003-2007 The eFaps Team
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

import java.io.StringReader;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * The bean is used for the title of JSP pages.
 *
 * @author tmo
 * @version $Id$
 */
public class TitleBean  {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Name of the command used to evalute the title.
   *
   * @see #getTitle
   * @see #setCommandName
   */
  private String commandName = null;

  /**
   * Object id of the instance which is used to evalute the expressions.
   *
   * @see #getTitle
   * @see #setOid
   */
  private String oid = null;


  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Translate depending from the command name in {@link #commandName} and the
   * extension <code>.Title</code> the text and returns them (used as title).
   * <br/>
   * If an object id in {@link oid} is given, the expressions in the title are
   * evaluated and replaced.
   *
   * @return translated text
   * @see #commandName
   */
   public String getTitle() throws Exception  {
     String title = DBProperties.getProperty(this.commandName + ".Title");

     if ((title != null) && (this.oid != null))  {
      SearchQuery query = new SearchQuery();
      query.setObject(this.oid);
      ValueParser parser = new ValueParser(new StringReader(title));
      ValueList list = parser.ExpressionString();
      list.makeSelect(query);
      if (query.selectSize() > 0) {
        query.execute();
        if (query.next()) {
          title = list.makeString(query);
        }
        query.close();
      }
    }

    return title;
   }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * The instance method sets the object id which is used to evalute the
   * expression within the title.
   *
   * @param _oid  new object id to set
   * @see #oid
   */
  public void setOid(final String _oid)  {
      this.oid = _oid;
  }

  /**
   * The instance method sets the command to the parameter name.
   *
   * @param _commandName  name of the command object
   * @see #commandName
   */
  public void setCommandName(final String _commandName)  {
    this.commandName = _commandName;
  }
}

