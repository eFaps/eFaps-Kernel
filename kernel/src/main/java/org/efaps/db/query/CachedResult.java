/*
 * Copyright 2006 The eFaps Team
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * The class is used to cache all results from a eFaps search query. Multiple
 * database selects could be added to one instance. The class simulates the
 * class {@link javax.sql.rowset.JoinRowSet}, because the original classes do
 * not work with Oracle JDBC drivers.
 */
public class CachedResult  {

  private Map < Object, List > cache = new HashMap < Object, List> ();

  /**
   * The variable stores the same values than in {@link #cache}, but without
   * key. The order is not changed, the order is the same than the result of
   * select statement.
   */
  private List < List > rows = new ArrayList < List > ();

  private Iterator < List > iter = null;

  /**
   * The instance variable is a pointer to the current row in the cached
   * result table list.
   */
  private List currentRow = null;

  public CachedResult()  {
  }

  /**
   * Moves the cursor to the front of this cached result object, just before
   * the first row. This method has no effect if the cached result contains no
   * rows.
   */
  public void beforeFirst()  {
    this.iter = this.rows.iterator();
  }

  /**
   * Moves the cursor down one row from its current position. A cached result
   * cursor is initially positioned before the first row; the first call to the
   * method next makes the first row the current row; the second call makes
   * the second row the current row, and so on.
   *
   * @return true if the new current row is valid; false if there are no more
   *         rows
   */
  public boolean next()  {
    if (this.iter == null)  {
      beforeFirst();
    }
    boolean ret = this.iter.hasNext();
    if (ret)  {
      this.currentRow = this.iter.next();
    }
    return ret;
  }

  /**
   * All values in the result set are added to the cached result. The column in
   * the result set specified with parameter <code>_keyIndex</code> is used to
   * identify the row in the cached result. This column is compared to the key
   * in the cached result.<br/>
   * The method does not set the cursor of the result set to the first row! In
   * other words, the method estimates that the result set is on the first row!
   *
   * @param _rs       result set with values to add to this cached result instance
   * @param _keyIndex index in the result set used as key to found the values
   *                  in the cache
   */
  public void populate(final ResultSet _rs, final int _keyIndex) throws SQLException  {
    ResultSetMetaData metaData = _rs.getMetaData();
    int columnCount = metaData.getColumnCount();

    if (this.cache.size() == 0)  {
      while (_rs.next())  {
        List list = new ArrayList(columnCount);
        for (int i = 1; i <= columnCount; i++)  {
          list.add(_rs.getObject(i));
        }
        this.cache.put(_rs.getObject(_keyIndex), list);
        this.rows.add(list);
      }
    } else  {
      while (_rs.next())  {
        List list = this.cache.get(_rs.getObject(_keyIndex));
        if (list != null)  {
          for (int i = 1; i <= columnCount; i++)  {
            if (i != _keyIndex)  {
              list.add(_rs.getObject(i));
            }
          }
        }
      }
    }
  }

  /**
   * @param _index  column index
   */
  public Object getObject(final int _index)  {
    return this.currentRow.get(_index - 1);
  }

  /**
   * @param _index  column index
   */
  public String getString(final int _index)  {
    Object obj =  getObject(_index);

    return obj == null ? null : obj.toString();
  }

  /**
   * @param _index  column index
   */
  public Long getLong(final int _index)  {
    Long ret = null;
    Object obj = getObject(_index);
    if (obj instanceof Number)  {
      ret = ((Number) obj).longValue();
    } else if (obj != null)  {
      ret = Long.parseLong(obj.toString());
    }
    return ret;
  }

  /**
   * @param _index  column index
   */
  public Double getDouble(final int _index)  {
    Double ret = null;
    Object obj = getObject(_index);
    if (obj instanceof Number)  {
      ret = ((Number) obj).doubleValue();
    } else if (obj != null)  {
      ret = Double.parseDouble(obj.toString());
    }
    return ret;
  }

  /**
   * @param _index  column index
   */
  public Date getTimestamp(final int _index)  {
    Date ret = null;
    Object obj = getObject(_index);
// TODO: timestamp from Oracle database does not work!
    if (obj instanceof Timestamp)  {
      ret = new Date(((Timestamp) obj).getTime());
    } else if (obj instanceof Date)  {
      ret = (Date) obj;
    }
    return ret;
  }
}