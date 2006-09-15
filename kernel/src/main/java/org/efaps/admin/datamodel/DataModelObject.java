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

package org.efaps.admin.datamodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.efaps.admin.AdminObject;
import org.efaps.db.Context;

/**
 * @author tmo
 * @version $Id$
 */
public abstract class DataModelObject extends AdminObject  {

  /**
   * Constructor to set the id and name of the data model object.
   *
   * @param _id         id to set
   * @param _name name  to set
   */
  protected DataModelObject(long _id, String _name)  {
    super(_id, null, _name);
  }

  /////////////////////////////////////////////////////////////////////////////

//protected void readFromDB4Properties(Context _context) throws Exception  {
//  readFromDB4Properties(_context.getConnection());
//}

  /**
   * The instance method reads the properteies for this data model object.
   * Each found property is set with instance method {@link #setProperty}.
   *
   * @param _context for this request
   * @see #setProperty
   */
  protected void readFromDB4Properties(Context _context) throws Exception  {
    Statement stmt = null;
    try  {
      stmt = _context.getConnection().createStatement();
      ResultSet rs = stmt.executeQuery(
          "select "+
            "PROPERTY.NAME,"+
            "PROPERTY.VALUE "+
          "from PROPERTY "+
          "where PROPERTY.ABSTRACT=" + getId() + ""
      );
      while (rs.next())  {
        String name =   rs.getString(1).trim();
        String value =  rs.getString(2).trim();
//        setProperty(_context, name, value);
setProperty(null, name, value);
      }
      rs.close();
    } catch (Exception e)  {
e.printStackTrace();
    } finally  {
      if (stmt != null)  {
        stmt.close();
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The static method initialise the data model cache in the correct order.
   */
/*  public static void initialise() throws Exception  {
  }
*/
}