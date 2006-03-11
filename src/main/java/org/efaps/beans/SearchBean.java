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

package org.efaps.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Search;
import org.efaps.admin.ui.Table;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;


import org.efaps.db.Instance;

public class SearchBean extends AbstractCollectionBean implements TableBeanInterface  {

  public SearchBean()  {
System.out.println("SearchBean.constructor");
  }

  public void finalize()  {
System.out.println("SearchBean.destructor");
  }


  /**
   * The instance method sets the search for this bean object.
   *
   * @param _name name of the search object
   * @see #search
   */
  public void setSearchName(String _name) throws Exception  {
    addHiddenValue("search" , _name);
    Context context = createNewContext();
    try  {
      setSearch(Search.get(context, _name));
    } catch (Throwable e)  {
      context.close();
    }
  }

  /**
   * The instance method sets the selected search command and depending on
   * this selected search command the search form and search table.
   *
   * @param _name name of the command object
   */
  public void setCommandName(String _name) throws Exception  {
    addHiddenValue("searchCommand" , _name);
    setCommand(getSearch().getSearchCommand(_name));
    setForm(((Search.SearchCommand)getCommand()).getSearchForm());
    setTable(((Search.SearchCommand)getCommand()).getResultTable());
    setMode(getCommand().getTargetMode());
/*
    Context context = createNewContext();
    try  {
System.out.println("hallo!.1");
      if (getSearch()!=null && getSearch().hasAccess(context))  {
System.out.println("hallo!.2");
        setMenuHolder(new MenuAbstractBean.MenuHolder(context, getSearch()));
System.out.println("hallo!.3");
      }
    } catch (Exception e)  {
      throw e;
    } finally  {
      try  {
        context.close();
      } catch (Exception e)  {
      }
    }
*/
  }

//protected void getSearchCommand(_context) throws Exception  {
//  String cmd = getSearch().getProperty(_context, "");
//}

  /**
   * With this instance method the checkboxes for the web table is controlled.
   *
   * @return <i>true</i> if the check boxes must be shown, other <i>false</i>
   *         is returned.
   */
  public boolean isShowCheckBoxes()  {
    return isConnectMode();
  }

  /**
   * The instance method sets the object id for this bean. To set the
   * object id means to set the instance for this bean. The instande method
   * also adds the parameters to the hidden parameters.
   *
   * @param _oid    object id
   * @see #instance
   */
  public void setOid(String _oid) throws Exception  {
    super.setOid(_oid);
    if (_oid!=null)  {
      addHiddenValue("parentOid", _oid);
    }
  }

  /**
   * This is only a dummy method, because the execute methods are implemented
   * by other methods. Because of that, an exception is always thrown if this
   * method is executed.
   *
   * @see #execute4ResultTable
   * @see #execute4SearchForm
   * @see #execute4Connect
   */
  public void execute() throws Exception  {
    throw new Exception("The method 'execute()' of the SearchBean is not allowed to execute!");
  }

  /////////////////////////////////////////////////////////////////////////////


  /**
   * The instance method executes the search and prepares the result for the
   * result table
   */
  public void execute4ResultTable() throws Exception  {
    Context context = createNewContext();
    try  {
      SearchQuery query = new SearchQuery();
//    boolean selectId = mode.equalsIgnoreCase(MODE_CONNECT);

Type type = ((Search.SearchCommand)getCommand()).getSearchType();

query.setQueryTypes(context, type.getName());


      for (int i=0; i<getForm().getFields().size(); i++)  {
        Field field = (Field)getForm().getFields().get(i);
        String value = getParameter(field.getName());
        if (value!=null && value.length()>0 && !value.equals("*"))  {
if (field.getProgramValue()==null)  {

// das ist das problem!! deshalb null-pointer-exceptions!!
//          query.addWhere(field, _request.getParameter(field.getName()));

query.addWhereExprEqValue(context, field.getExpression(), value);

} else  {
  field.getProgramValue().addSearchWhere(context, query, value);
}
        }
      }
      query.add(context, getTable());
      query.execute(context);

      setValues(new ArrayList());
      getTableBean().setValues(getValues());
      getTableBean().executeRowResult(context, query);

      setInitialised(true);
    } catch (Exception e)  {
      throw e;
    } finally  {
      try  {
        context.close();
      } catch (Exception e)  {
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method is called before the search form is shown. It
   * initialise the bean with the needed values that the search looks like
   * an web form.
   *
   * @see #add4Form
   */
  public void execute4SearchForm() throws Exception  {
    Context context = createNewContext();
    try  {
      setValues(new ArrayList());
      getValues().add(null);
      for (int i=0; i<getForm().getFields().size(); i++)  {
        Field field = (Field)getForm().getFields().get(i);
AttributeTypeInterface attrValue = null;
if (field.getExpression()!=null)  {
  Type type = ((Search.SearchCommand)getCommand()).getSearchType();
  attrValue = type.getAttribute(field.getExpression()).newInstance();
//  attrValue.setField(field);
} else if (field.getProgramValue()!=null)  {
  attrValue = field.getProgramValue().evalSearchAttributeValue(context, getInstance());
//  attrValue.setField(field);
} else if (field.getGroupCount()>0)  {
  if (getMaxGroupCount()<field.getGroupCount())  {
    setMaxGroupCount(field.getGroupCount());
  }
}
        add4Form(attrValue, field);
      }
      setInitialised(true);
    } catch (Exception e)  {
      throw e;
    } finally  {
      try  {
        context.close();
      } catch (Exception e)  {
      }
    }
  }

  /**
   * The instance method adds a new attribute value (from instance
   * {@link AttributeTypeInterface}) to the values for the search form.
   *
   * @param _attrValue  attribute to add
   * @see #values
   * @see #execute4SearchForm
   */
  private void add4Form(AttributeTypeInterface _attrValue, Field _field)  {
//    getValues().add(new Value(_attrValue, null, _field));
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   *
   */
  public void execute4Connect(String[] _oids) throws Exception  {
    if (_oids!=null)  {
      Search.SearchCommand command  = (Search.SearchCommand)getCommand();
      Type type                     = command.getConnectType();
      Attribute childAttr           = type.getAttribute(command.getConnectChildAttribute());
      Attribute parentAttr          = type.getAttribute(command.getConnectParentAttribute());

      if (childAttr==null)  {
throw new Exception("Could not found child attribute '"+command.getConnectChildAttribute()+"' for type '"+type.getName()+"'");
      }
      if (parentAttr==null)  {
throw new Exception("Could not found parent attribute '"+command.getConnectParentAttribute()+"' for type '"+type.getName()+"'");
      }

      Instance parent = getInstance();
      Context context = createNewContext();
      try  {
        for (int i=0; i<_oids.length; i++)  {
          Instance child = new Instance(context, _oids[i]);
          Insert insert = new Insert(context, type);
          insert.add(context, parentAttr, ""+parent.getId());
          insert.add(context, childAttr,  ""+child.getId());
          insert.execute(context);
        }
      } catch (Exception e)  {
        throw e;
      } finally  {
        try  {
          context.close();
        } catch (Exception e)  {
        }
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * @see #mode
   */
  public boolean isSearchMode()  {
    return true;
  }

  /**
   * @return <i>true</i> if the target frame is popup, otherwise <i>false</i>
   * @see #targetFrame
   */
  public boolean isPopup()  {
    return true;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the search user interface object for this
   * search bean.
   *
   * @see #getSearch
   * @see #setSearch
   */
  private Search search = null;

  /**
   * The instance variable stores the search form for the user input.
   *
   * @see #getForm
   * @see #setForm
   */
  private Form form = null;

  /**
   * The instance variable stores ???????????????????????????????
   *
   * @see #getTableBean
   * @see #setTableBean
   */
  private TableBean tableBean = new TableBean();

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for the instance variable {@link #search}.
   *
   * @return value of instance variable {@link #search}
   * @see #search
   * @see #setSearch
   */
  public Search getSearch()  {
    return this.search;
  }

  /**
   * This is the setter method for the instance variable {@link #search}.
   *
   * @param _search  new value for instance variable {@link #search}
   * @see #search
   * @see #getSearch
   */
  public void setSearch(Search _search)  {
    this.search = _search;
  }

  /**
   * This is the getter method for the instance variable {@link #form}.
   *
   * @return value of instance variable {@link #form}
   * @see #form
   * @see #setForm
   */
  public Form getForm()  {
    return this.form;
  }

  /**
   * This is the setter method for the instance variable {@link #form}.
   *
   * @param _form  new value for instance variable {@link #form}
   * @see #form
   * @see #getForm
   */
  public void setForm(Form _form)  {
    this.form = _form;
  }

  /**
   * This is the getter method for the instance variable {@link #tableBean}.
   *
   * @return value of instance variable {@link #tableBean}
   * @see #tableBean
   * @see #setTableBean
   */
  public TableBean getTableBean()  {
    return this.tableBean;
  }

  /////////////////////////////////////////////////////////////////////////////
  // table bean wrappe

  /**
   * This is the getter method for the instance variable {@link #table}.
   *
   * @return value of instance variable {@link #table}
   * @see #table
   * @see #setTable
   */
  public Table getTable()  {
    return getTableBean().getTable();
  }

  /**
   * This is the setter method for the instance variable {@link #table}.
   *
   * @param _table  new value for instance variable {@link #table}
   * @see #table
   * @see #getTable
   */
  public void setTable(Table _table)  {
    getTableBean().setTable(_table);
  }

  /**
   * This is the getter method for the instance variable {@link #sortKey}.
   *
   * @return value of instance variable {@link #sortKey}
   * @see #sortKey
   * @see #setSortKey
   */
  public String getSortKey()  {
    return getTableBean().getSortKey();
  }

  /**
   * This is the setter method for the instance variable {@link #sortKey}.
   *
   * @param _sortKey  new value for instance variable {@link #sortKey}
   * @see #sortKey
   * @see #getSortKey
   */
  public void setSortKey(String _sortKey)  {
    getTableBean().setSortKey(_sortKey);
  }

  /**
   * This is the getter method for the instance variable {@link #sortDirection}.
   *
   * @return value of instance variable {@link #sortDirection}
   * @see #sortDirection
   * @see #setSortDirection
   */
  public String getSortDirection()  {
    return getTableBean().getSortDirection();
  }

  /**
   * This is the setter method for the instance variable {@link #sortDirection}.
   *
   * @param _sortDirection  new value for instance variable {@link #sortDirection}
   * @see #sortDirection
   * @see #getSortDirection
   */
  public void setSortDirection(String _sortDirection)  {
    getTableBean().setSortDirection(_sortDirection);
  }

  /**
   * This is the getter method for the instance variable {@link #selectedFilter}.
   *
   * @return value of instance variable {@link #selectedFilter}
   * @see #selectedFilter
   * @see #setSelectedFilter
   */
  public int getSelectedFilter()  {
    return getTableBean().getSelectedFilter();
  }

  /**
   * This is the setter method for the instance variable {@link #selectedFilter}.
   *
   * @param _selectedFilter  new value for instance variable {@link #selectedFilter}
   * @see #selectedFilter
   * @see #getSelectedFilter
   */
  public void setSelectedFilter(int _selectedFilter)  {
    getTableBean().setSelectedFilter(_selectedFilter);
  }

  /**
   * The instance method sorts the table values depending on the sort key in
   * {@link #sortKey} and the sort direction in {@link #sortDirection}.
   */
  public boolean sort()  {
    return getTableBean().sort();
  }

  /**
   * This is the setter method for the response variable {@link #response}.
   *
   * @param _response  new value for response variable {@link #response}
   * @see #response
   * @see #getResponse
   */
  public void setResponse(HttpServletResponse _response)  {
    super.setResponse(_response);
    getTableBean().setResponse(_response);
  }

}