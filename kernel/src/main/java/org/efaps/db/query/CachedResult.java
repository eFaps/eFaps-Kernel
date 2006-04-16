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

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

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

  public void beforeFirst()  {
    this.iter = this.rows.iterator();
  }

  public boolean next()  {
    boolean ret = this.iter.hasNext();

    if (ret)  {
      this.currentRow = this.iter.next();
    }
    return ret;
  }

  /**
   *
   */
  public void populate(final ResultSet _rs, final int _keyIndex) throws SQLException  {
    ResultSetMetaData metaData = _rs.getMetaData();
    int columnCount = metaData.getColumnCount();

    _rs.beforeFirst();
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

  public Object getObject(int _index)  {
    return this.currentRow.get(_index - 1);
  }


  public String getString(int _index)  {
    Object obj =  getObject(_index);

    return obj == null ? null : obj.toString();
  }

  public Long getLong(int _index)  {
    Long ret = null;
    Object obj = getObject(_index);
    if (obj instanceof Long)  {
      ret = (Long) obj;
// TODO: BigDecimal
    } else  {
      ret = Long.parseLong(obj.toString());
    }
    return ret;
  }

  public Double getDouble(int _index)  {
    return (Double) getObject(_index);
  }
}