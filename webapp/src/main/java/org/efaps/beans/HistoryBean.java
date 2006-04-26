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

import java.sql.ResultSet;
import java.sql.Statement;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

  /**
   * Logging instance used in this class.
   */
  private final static Log LOG = LogFactory.getLog(HistoryBean.class);

  /**
   * All column headers are stored as list in this instance variable.
   */
  private DataModel columnHeaders = null;

  /**
   * All found history entries are stored in this list.
   *
   * @see #getHistoryEntries
   */
  private DataModel data;

  /**
   * The string stores the name of the column which must be used to sort.
   *
   * @see #doSort
   * @see #getSortColumn
   * @see #setSortColumn
   */
  private String sortColumn = null;

  /**
   * The value is <i>true</i> if the  list must be sorted ascending.
   *
   * @see #doSort
   * @see #isSortAscending
   * @see #setSortAscending
   */
  private boolean sortAscending = true;

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

  /**
   * The instance is set to the given object id in the parameter. Then the
   * history data is read with {@link #doExecute} and sorted {@link #doSort}
   * by the modified column descending.
   *
   * @param _oid  new object id to set
   * @see #doExecute
   * @see #doSort
   * @see #sortColumn
   * @see #sortAscending
   */
  public void setOid(String _oid) throws Exception  {
   if (_oid != null)  {
      super.setOid(_oid);
      doExecute();
      this.sortColumn = "1";
      this.sortAscending = false;
      doSort();
    }
  }

  /**
   * All history data depending on the object id is read and stored in
   * instance variable {@link #data}.
   *
   * @throws EFapsException if no instance is given, from called methods, or
   *                        if another exception is thrown
   */
  protected void doExecute() throws EFapsException  {
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
                + "from HISTORY "
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
      LOG.error("while reading history data", e);
      if (con != null)  {
        try {
          con.abort();
        } catch (Throwable e2)  {
        }
      }
      throw e;
    } catch (Throwable e)  {
      LOG.error("while reading history data", e);
      if (con != null)  {
        try {
          con.abort();
        } catch (Throwable e2)  {
        }
      }
      throw new EFapsException(getClass(), "doExecute.Throwable", e);
    }
  }

  /**
   * The {@link #data} is sorted depending on the column defined in
   * {@link #sortColumn} in the way defined in {@link #sortAscending}. If the
   * data is already sorted with the same values (stored in
   * {@link #sortedColumn} and {@link #sortedAscending}), the sort is not done
   * again.
   *
   * @see #sortColumn
   * @see #sortAscending
   * @see #sortedColumn
   * @see #sortedAscending
   */
  protected void doSort()  {
    if ((this.sortColumn != null)
        && ((this.sortAscending != this.sortedAscending) || !this.sortColumn.equals(this.sortedColumn)))  {

      final int index = Integer.parseInt(this.sortColumn);
      final boolean sortAscending = this.sortAscending;

      Comparator < List < ColumnValue > > comparator = new Comparator < List < ColumnValue > >()  {
        public int compare(List < ColumnValue > _o1, List < ColumnValue > _o2)  {
          ColumnValue val1 = _o1.get(index);
          ColumnValue val2 = _o2.get(index);
          return sortAscending ?
              ((Comparable) val1.getValue()).compareTo(val2.getValue())
              : ((Comparable) val2.getValue()).compareTo(val1.getValue());
        }
      };

      Collections.sort((List < List < ColumnValue > >)this.data.getWrappedData(), comparator);
      this.sortedColumn = this.sortColumn;
      this.sortedAscending = this.sortAscending;
    }
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
   * @see #data
   */
  public DataModel getData()  {
    doSort();
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
      headerList.add(new ColumnHeader("0", this.i18nBean.translate("History.Header.EventType"),          stringConverter));
      headerList.add(new ColumnHeader("1", this.i18nBean.translate("History.Header.Modified"),           dateTimeConverter));
      headerList.add(new ColumnHeader("2", this.i18nBean.translate("History.Header.Modifier"),           stringConverter));
      headerList.add(new ColumnHeader("3", this.i18nBean.translate("History.Header.AttributeName"),      stringConverter));
      headerList.add(new ColumnHeader("4", this.i18nBean.translate("History.Header.AttributeNewValue"),  stringConverter));
      this.columnHeaders = new ListDataModel(headerList);
    }
    return this.columnHeaders;
  }

  /**
   * This is the getter method for instance variable {@link #sortColumn}.
   *
   * @return value of instance variable {@link #sortColumn}
   * @see #sortColumn
   * @see #setSortColumn
   */
  public String getSortColumn()  {
    return this.sortColumn;
  }

  /**
   * This is the setter method for instance variable {@link #sortColumn}.
   *
   * @param _sort   new value to set
   * @see #sortColumn
   * @see #getSortColumn
   */
  public void setSortColumn(final String _sortColumn)  {
    this.sortColumn = _sortColumn;
  }

  /**
   * This is the getter method for instance variable {@link #sortAscending}.
   *
   * @return value of instance variable {@link #sortAscending}
   * @see #sortAscending
   * @see #setSortAscending
   */
  public boolean isSortAscending()  {
    return this.sortAscending;
  }

  /**
   * This is the setter method for instance variable {@link #sortAscending}.
   *
   * @param _ascending  new value to set
   * @see #sortAscending
   * @see #isSortAscending
   */
  public void setAscending(final boolean _sortAscending)  {
    this.sortAscending = _sortAscending;
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