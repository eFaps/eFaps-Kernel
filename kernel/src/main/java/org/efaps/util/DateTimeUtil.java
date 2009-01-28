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

package org.efaps.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;

/**
 * @author jmox
 * @version $Id$
 */
public class DateTimeUtil {

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(DateTimeUtil.class);

  /**
   * static method to get the current timestamp from the eFpas-Database
   *
   * @return Timestamp containing the current Time of the eFaps-DataBase
   * @throws EFapsException
   */
  public static Timestamp getCurrentTimeFromDB() throws EFapsException {
    Timestamp now = null;
    final ConnectionResource rsrc =
        Context.getThreadContext().getConnectionResource();
    Statement stmt;
    try {
      stmt = rsrc.getConnection().createStatement();
      final ResultSet resultset =
          stmt.executeQuery("SELECT "
              + Context.getDbType().getCurrentTimeStamp());
      resultset.next();
      now = resultset.getTimestamp(1);
      resultset.close();
      stmt.close();
      rsrc.commit();
    } catch (SQLException e) {
      LOG.error("could not execute SQL-Statement", e);
    }
    return now;
  }

}
