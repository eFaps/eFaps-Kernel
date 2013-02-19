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


package org.efaps.db.databases;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Factory to create the DataBase istance.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class DataBaseFactory
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataBaseFactory.class);


    /**
     * Hyrachical list of clazzNames that will be check in the given
     * sequence if the database is connected.
     */
    private static final List<String> CLAZZNAMES = new ArrayList<String>();

    static {
        DataBaseFactory.CLAZZNAMES.add("org.efaps.db.databases.PostgreSQLDatabase");
        DataBaseFactory.CLAZZNAMES.add("org.efaps.db.databases.DerbyDatabase");
        DataBaseFactory.CLAZZNAMES.add("org.efaps.db.databases.MySQLDatabase");
        DataBaseFactory.CLAZZNAMES.add("org.efaps.db.databases.OracleDatabase");
        DataBaseFactory.CLAZZNAMES.add("org.efaps.db.databases.OracleDatabaseWithAutoSequence");
    }

    /**
     * @param _connection Connection to be uesd
     * @return AbstractDatabase
     */
    public static AbstractDatabase<?> getDatabase(final Connection _connection)
    {
        AbstractDatabase<?> ret = null;
        for (final String clazzName : DataBaseFactory.CLAZZNAMES) {
            try {
                ret = (AbstractDatabase<?>) Class.forName(clazzName).newInstance();
                if (ret.isConnected(_connection)) {
                    break;
                }
            } catch (final ClassNotFoundException e) {
                DataBaseFactory.LOG.error("Error on instanciating of {} {}", clazzName, e);
            } catch (final InstantiationException e) {
                DataBaseFactory.LOG.error("Error on instanciating of {} {}", clazzName, e);
            } catch (final IllegalAccessException e) {
                DataBaseFactory.LOG.error("Error on instanciating of {} {}", clazzName, e);
            } catch (final SQLException e) {
                DataBaseFactory.LOG.error("Catched Error from DataBase {}, trying next.", clazzName, e);
            }
        }
        return ret;
    }
}
