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
 * 
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class CachedResult {

  // ////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Used to cache for given key (Object) the list of values (List).
   */
  private final Map<Object, List<Object>> cache =
      new HashMap<Object, List<Object>>();

  /**
   * The variable stores the same values than in {@link #cache}, but without
   * key. The order is not changed, the order is the same than the result of
   * select statement.
   */
  private final List<List<Object>> rows = new ArrayList<List<Object>>();

  private Iterator<List<Object>> iter = null;

  /**
   * The instance variable is a pointer to the current row in the cached result
   * table list.
   * 
   * @see #beforeFirst
   * @see #next
   * @see #gotoKey
   */
  private List<Object> currentRow = null;

  // ////////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  public CachedResult() {
  }

  // ////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Moves the cursor to the front of this cached result object, just before the
   * first row. This method has no effect if the cached result contains no rows.
   * Attention! After that, method {@link #next} must be called to get first
   * row!
   * 
   * @see #next
   */
  public void beforeFirst() {
    this.iter = this.rows.iterator();
  }

  /**
   * Moves the cursor down one row from its current position. A cached result
   * cursor is initially positioned before the first row; the first call to the
   * method next makes the first row the current row; the second call makes the
   * second row the current row, and so on.
   * 
   * @return <i>true</i> if the new current row is valid; <i>false</i> if
   *         there are no more rows
   */
  public boolean next() {
    if (this.iter == null) {
      beforeFirst();
    }
    boolean ret = this.iter.hasNext();
    if (ret) {
      this.currentRow = this.iter.next();
    }
    return ret;
  }

  /**
   * Moves the cursor to the row defined with given key.
   * 
   * @param _key
   *          key defing next row
   * @return <i>true</i> if a row for the key exists, otherwise <i>false>
   */
  public boolean gotoKey(final Object _key) {
    this.currentRow = this.cache.get(_key);
    return (this.currentRow != null);
  }

  /**
   * All values in the result set are added to the cached result. The column in
   * the result set specified with parameter <code>_keyIndex</code> is used to
   * identify the row in the cached result. This column is compared to the key
   * in the cached result.<br/> The method does not set the cursor of the
   * result set to the first row! In other words, the method estimates that the
   * result set is on the first row!
   * 
   * @param _rs
   *          result set with values to add to this cached result instance
   * @param _keyIndex
   *          index in the result set used as key to found the values in the
   *          cache
   */
  public void populate(final ResultSet _rs, final int _keyIndex)
      throws SQLException {
    ResultSetMetaData metaData = _rs.getMetaData();
    int columnCount = metaData.getColumnCount();

    if (this.cache.size() == 0) {
      while (_rs.next()) {
        List<Object> list = new ArrayList<Object>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
          switch (metaData.getColumnType(i)) {
            case java.sql.Types.TIMESTAMP:
              list.add(_rs.getTimestamp(i));
            break;
            default:
              list.add(_rs.getObject(i));
          }
        }
        this.cache.put(_rs.getObject(_keyIndex), list);
        this.rows.add(list);
      }
    } else {
      while (_rs.next()) {
        List<Object> list = this.cache.get(_rs.getObject(_keyIndex));
        if (list != null) {
          for (int i = 1; i <= columnCount; i++) {
            if (i != _keyIndex) {
              switch (metaData.getColumnType(i)) {
                case java.sql.Types.TIMESTAMP:
                  list.add(_rs.getTimestamp(i));
                break;
                default:
                  list.add(_rs.getObject(i));
              }
            }
          }
        }
      }
    }
  }

  /**
   * @param _index
   *          column index
   * @return object on given column index
   * @see #currentRow
   */
  public Object getObject(final int _index) {
    return this.currentRow.get(_index - 1);
  }

  /**
   * @param _index
   *          column index
   * @return string representation for the object on given column index
   * @see #currentRow
   */
  public String getString(final int _index) {
    Object obj = getObject(_index);

    return obj == null ? null : obj.toString();
  }

  /**
   * Returns the long representation for the object on given column index. If
   * the object on the given column index is not an instance of class
   * {@link java.lang.Number} (if it is a number, the long value is used), the
   * value is converted in a string and then parsed as long.
   * 
   * @param _index
   *          column index
   * @return long representation for the object on given column index;
   *         <code>null</code> if no value is defined
   * @see #currentRow
   */
  public Long getLong(final int _index) {
    Long ret = null;
    Object obj = getObject(_index);
    if (obj instanceof Number) {
      ret = ((Number) obj).longValue();
    } else if (obj != null) {
      ret = Long.parseLong(obj.toString());
    }
    return ret;
  }

  /**
   * Returns the double representation for the object on given column index. If
   * the object on the given column index is not an instance of class
   * {@link java.lang.Number} (if it is a number, the double value is used), the
   * value is converted in a string and then parsed as double.
   * 
   * @param _index
   *          column index
   * @return double representation for the object on given column index;
   *         <code>null</code> if no value is defined
   * @see #currentRow
   */
  public Double getDouble(final int _index) {
    Double ret = null;
    Object obj = getObject(_index);
    if (obj instanceof Number) {
      ret = ((Number) obj).doubleValue();
    } else if (obj != null) {
      ret = Double.parseDouble(obj.toString());
    }
    return ret;
  }

  /**
   * Returns the boolean representation for the object on given column index. If
   * the object on the given column index is not an instance of class Boolean.
   * The value is interpreteded as number and converted to an eequal boolen.
   * 
   * @param _index
   *          column index
   * @return boolean representation for the object on given column index;
   *         <code>null</code> if no value is defined
   * @see #currentRow
   */
  public Boolean getBoolean(final int _index) {
    Boolean ret = null;
    Object obj = getObject(_index);
    if (obj instanceof Boolean) {
      ret = (Boolean) obj;
    } else if (obj instanceof Number) {
      Integer intvalue = ((Number) obj).intValue();
      if ((intvalue != null) && (intvalue != 0)) {
        ret = true;
      } else {
        ret = false;
      }
    }
    return ret;
  }

  /**
   * @param _index
   *          column index
   * @return time stamp representation for the object on given column index for
   *         time stamp and date instances, otherwise <code>null</code>
   * @see #currentRow
   */
  public Timestamp getTimestamp(final int _index) {
    Timestamp ret = null;
    Object obj = getObject(_index);
    if (obj instanceof Timestamp) {
      ret = (Timestamp) obj;
    } else if (obj instanceof Date) {
      ret = new Timestamp(((Date) obj).getTime());
    }
    return ret;
  }
}
