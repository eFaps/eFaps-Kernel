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

package org.efaps.beans;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.faces.convert.CharacterConverter;
import javax.faces.convert.Converter;
import javax.faces.convert.DateTimeConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.user.Person;
import org.efaps.beans.table.ColumnHeader;
import org.efaps.beans.table.ColumnValue;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * The bean is used to select the history data from the database and return
 * this to the JSP page.
 *
 * @author tmo
 * @version $Rev$
 */
public class HistoryBean extends AbstractBean  {

  private DataModel columnHeaders = null;

  /**
   * All found history entries are stored in this list.
   *
   * @see #getHistoryEntries
   */
//  private final List < List > entries = new ArrayList < List > ();
  private DataModel data;

  /**
   * The value is <i>true</i> if the  list must be sorted ascending.
   *
   * @see #doSort
   * @see #isAscending
   * @see #setAscending
   */
  private boolean ascending = true;

  /**
   * The string stores the name of the column which must be used to sort.
   *
   * @see #doSort
   * @see #getSort
   * @see #setSort
   */
  private String sort = null;

  /**
   * The string stores the name of the column with was used to sort.
   * The value is updated after a new sort is done in {@link #doSort}.
   *
   * @see #doSort
   */
  private String sortedColumn = null;

  /**
   * The value is <i>true</i> if the  list was sorted ascending.
   * The value is updated after a new sort is done in {@link #doSort}.
   *
   * @see #doSort
   */
  private boolean sortedAscending = false;

  /**
   * The value stores the bean used to translate strings.
   */
  private ResourceBundleBean i18nBean = null;

  public void setOid(String _oid) throws Exception  {
   if (_oid != null)  {
      super.setOid(_oid);
      doExecute();
    }
  }

  protected void doExecute() throws Exception  {
    Context context = Context.getThreadContext();

    Instance instance = getInstance();

   if (instance == null)  {
      throw new EFapsException(getClass(), "doExecute.NoInstance");
    }

    ConnectionResource con = null;
    try  {
      con = context.getConnectionResource();

      Statement stmt = null;

      try  {
        stmt = con.getConnection().createStatement();

        stmt.execute(
            "select EVENTTYPEID,MODIFIED,MODIFIER,ATTRID,ATTRVALUE "
                + "from ABSTRACT_HISTORY "
                + "where FORTYPEID=" + instance.getType().getId() + " "
                   + "and FORID=" + instance.getId()
        );

        List rows = new ArrayList();

        ResultSet rs = stmt.getResultSet();
        while (rs.next()) {
          List row = new ArrayList();
          String eventType = "History.Event." + Type.get(rs.getLong(1)).getName();

          Attribute attr = Attribute.get(rs.getLong(4));
          String attrName = "";
          if (attr != null)  {
            attrName = this.i18nBean.translate(instance.getType().getName() + "/" + attr.getName() + ".Label");
          }

          String value = rs.getString(5);

          row.add(new ColumnValue < String > (this.i18nBean.translate(eventType)));
          row.add(new ColumnValue < Date >   (rs.getTimestamp(2)));
          row.add(new ColumnValue < String > (Person.get(rs.getLong(3)).getName()));
          row.add(new ColumnValue < String > (attrName));
          row.add(new ColumnValue < String > (value == null ? "" : value));
          rows.add(row);
        }

        this.data = new ListDataModel(rows);

      } finally  {
       try  {
          if (stmt != null)  {
            stmt.close();
          }
        } catch (java.sql.SQLException e)  {
        }
      }

      con.commit();
    } catch (EFapsException e)  {
e.printStackTrace();
      if (con != null)  {
        try {
          con.abort();
        } catch (Throwable e2)  {
        }
      }
      throw e;
    } catch (Throwable e)  {
e.printStackTrace();
      if (con != null)  {
        try {
          con.abort();
        } catch (Throwable e2)  {
        }
      }
      throw new EFapsException(getClass(), "doExecute.Throwable", e);
    }
  }

  protected void doSort(final String _column, final boolean _ascending)  {
/*    if ((_column != null) && ((_ascending != this.sortedAscending) || !_column.equals(this.sortedColumn)))  {
      Comparator < Entry > comparator = new Comparator < Entry >()  {
        public int compare(Entry _o1, Entry _o2)  {
          if (_column == null)  {
            return 0;
          }
          if (_column.equals("modified"))  {
            return _ascending ? _o1.getModified().compareTo(_o2.getModified()) : _o2.getModified().compareTo(_o1.getModified());
          } else if (_column.equals("modifier"))  {
            return _ascending ? _o1.getModifier().compareTo(_o2.getModifier()) : _o2.getModifier().compareTo(_o1.getModifier());
          } else  {
            return 0;
          }
        }
      };
      Collections.sort(this.entries, comparator);
      this.sortedColumn = _column;
      this.sortedAscending = _ascending;
    }
*/
  }

  public Object getColumnValue()  {
    Object columnValue = null;
    if (this.data.isRowAvailable() && this.columnHeaders.isRowAvailable())  {
      columnValue = ((List) data.getRowData()).get(this.columnHeaders.getRowIndex());
    }
    return columnValue;
  }

  /**
   * All history entries are sorted returned. The sort is done by column
   * {@link #sort} and ascending depending on {@link #ascending}.
   *
   * @return sorted history entries
   * @see #doSort
   * @see #sort
   * @see #ascending
   * @see #data
   */
  public DataModel getData()  {
// TODO: sort of the data
    return this.data;
  }

  void setData(final DataModel _data)  {
System.out.println("-------------------------------------------------------- etData");
System.out.println("preserved datamodel updated");
  // just here to see if the datamodel is updated if preservedatamodel=true
  }

  /**
   * Returns the list of transalted column headers. If the list is not already
   * defined, the list is new created.
   *
   * @return translated column headers
   */
  public DataModel getColumnHeaders()  {
    if (this.columnHeaders == null)  {
      Converter stringConverter = new CharacterConverter();
      DateTimeConverter dateTimeConverter = new DateTimeConverter();
      dateTimeConverter.setType("both");

      List headerList = new ArrayList();
      headerList.add(new ColumnHeader("eventtype",    this.i18nBean.translate("History.Header.EventType"),          stringConverter));
      headerList.add(new ColumnHeader("modified",     this.i18nBean.translate("History.Header.Modified"),           dateTimeConverter));
      headerList.add(new ColumnHeader("modifier",     this.i18nBean.translate("History.Header.Modifier"),           stringConverter));
      headerList.add(new ColumnHeader("attrid",       this.i18nBean.translate("History.Header.AttributeName"),      stringConverter));
      headerList.add(new ColumnHeader("attrnewvalue", this.i18nBean.translate("History.Header.AttributeNewValue"),  stringConverter));
      this.columnHeaders = new ListDataModel(headerList);
    }
    return this.columnHeaders;
  }

  /**
   * This is the getter method for instance variable {@link #sort}.
   *
   * @return value of instance variable {@link #sort}
   * @see #sort
   * @see #setSort
   */
  public String getSort()  {
    return this.sort;
  }

  /**
   * This is the setter method for instance variable {@link #sort}.
   *
   * @param _sort   new value to set
   * @see #sort
   * @see #getSort
   */
  public void setSort(String _sort)  {
    this.sort = _sort;
  }

  /**
   * This is the getter method for instance variable {@link #ascending}.
   *
   * @return value of instance variable {@link #ascending}
   * @see #ascending
   * @see #setAscending
   */
  public boolean isAscending()  {
    return this.ascending;
  }

  /**
   * This is the setter method for instance variable {@link #ascending}.
   *
   * @param _ascending  new value to set
   * @see #ascending
   * @see #isAscending
   */
  public void setAscending(final boolean _ascending)  {
    this.ascending = _ascending;
  }

  /**
   * This is the setter method for instance variable {@link #i18nBean}.
   *
   * @param _i18nBean  new value to set
   * @see #i18nBean
   */
  public void setI18nBean(final ResourceBundleBean _i18nBean)  {
    this.i18nBean = _i18nBean;
  }

}