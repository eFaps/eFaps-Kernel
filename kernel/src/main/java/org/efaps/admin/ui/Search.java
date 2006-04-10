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

package org.efaps.admin.ui;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 *
 */
public class Search extends MenuAbstract  {

  /**
   * The static variable defines the class name in eFaps.
   */
  static public EFapsClassName EFAPS_CLASSNAME = EFapsClassName.SEARCH;

  /**
   * This is the constructor to create a new instance of the class Search. The
   * parameter <i>_name</i> is a must value to identify clearly the search
   * instance.
   *
   * @param _context  context for this request
   * @param _id       search id
   * @param _name     search name
   */
  public Search(Long _id, String _name) throws Exception  {
    super(_id, _name);
//    readFromDB(_context);
  }

  /**
   * A search menu with the given id is added to this search.
   *
   * @param _context  eFaps context for this request
   * @param _id       id of the search menu to add
   */
  protected void add(Context _context, long _id) throws Exception  {
    add(new SearchMenu(_context, _id));
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method returns for the name the search command.
   *
   * @param _searchCommand
   * @see #searchCommands
   */
  public SearchCommand getSearchCommand(String _searchCommand)  {
    return getSearchCommands().get(_searchCommand);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores all search commands for this search for easy
   * accessing in a own hash map.
   *
   * @see #getSearchCommands
   * @see #addAllSearchCommands
   */
  private Map<String,SearchCommand> searchCommands = new HashMap<String,SearchCommand>();

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #searchCommands}.
   *
   * @return the value of the instance variable {@link #searchCommands}.
   * @see #searchCommands
   */
  private Map<String,SearchCommand> getSearchCommands()  {
    return this.searchCommands;
  }

  /////////////////////////////////////////////////////////////////////////////

/*public void addOneSearch(String _label, Form _form, Table _table)  {
  Command command = new Command();
//  command.setLabel(_label);
  command.setReference("javascript:openSearchForm('"+this.forms.size()+"')");
  getMenu().add(command);
this.forms.add(_form);
this.tables.add(_table);
}
*/

/*ArrayList forms = new ArrayList();
ArrayList tables = new ArrayList();

public Form getForm(int _index)  {
  return (Form)this.forms.get(_index);
}

public Table getTable(int _index)  {
  return (Table)this.tables.get(_index);
}
*/
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Command}.
   *
   * @param _context  context this request
   * @param _name     name to search in the cache
   * @return instance of class {@link Command}
   * @see #getCache
   */
  static public Search get(Context _context, String _name) throws EFapsException  {
    Search search = (Search)getCache().get(_name);
    if (search==null)  {
      search = getCache().read(_context, _name);
    }
    return search;
  }

  /**
   * Static getter method for the type hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  protected static UserInterfaceObjectCache<Search> getCache()  {
    return searchCache;
  }

  /**
   * Stores all instances of class {@link Search}.
   *
   * @see #getCache
   */
  static private UserInterfaceObjectCache<Search> searchCache = new UserInterfaceObjectCache<Search>(Search.class);

  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////

  public class SearchMenu extends MenuAbstract  {

    private SearchMenu(Context _context, long _id) throws Exception  {
      super(_id, null);
      readFromDB(_context);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * @see #readFromDB4SearchCommands
     */
    protected void readFromDB(Context _context) throws Exception  {
      readFromDB4Name(_context);
      super.readFromDB(_context);
    }

    private void readFromDB4Name(Context _context) throws Exception  {
      SearchQuery query = new SearchQuery();
      query.setQueryTypes(_context, EFapsClassName.MENU.name);
      query.addWhereExprEqValue(_context, "ID", getId());
      query.addSelect(_context, "Name");
      query.execute(_context);

      if (query.next())  {
        setName((String)query.get(_context, "Name"));
      } else  {
throw new Exception("search does not exists");
      }
setLabel(getName()+".Label");
    }

    /**
     * A search menu with the given id is added to this search.
     *
     * @param _context  eFaps context for this request
     * @param _id       id of the search menu to add
     */
    protected void add(Context _context, long _id) throws Exception  {
      add(new SearchCommand(_context, _id));
    }

    /**
     * Add a command to the menu structure.
     *
     * @param _command command to add
     */
    public void add(SearchCommand _command)  {
      getSearchCommands().put(_command.getName(), _command);
      super.add(_command);
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////

  public class SearchCommand extends CommandAbstract  {

    private SearchCommand(Context _context, long _id) throws Exception  {
      super(_id, null);
      readFromDB(_context);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * @see #readFromDB4SearchCommands
     */
    protected void readFromDB(Context _context) throws Exception  {
      readFromDB4Name(_context);
      super.readFromDB(_context);
    }

    /**
     * The instance method reads the name of the search command.
     *
     * @param _context  context for this request
     */
    private void readFromDB4Name(Context _context) throws Exception  {
      SearchQuery query = new SearchQuery();
      query.setQueryTypes(_context, EFapsClassName.COMMAND.name);
      query.addWhereExprEqValue(_context, "ID", getId());
      query.addSelect(_context, "Name");
      query.execute(_context);

      if (query.next())  {
        setName((String)query.get(_context, "Name"));
      } else  {
throw new Exception("search does not exists");
      }
setLabel(getName()+".Label");
    }

    /**
     * The instance method sets all properties of search command object.
     *
     * @param _context  context for this request
     * @param _name     name of the property (key)
     * @param _value    value of the property
     * @param _toId     id of the user interface object the property references
     */
    protected void setProperty(Context _context, String _name, String _value) throws Exception  {
      if (_name.equals("ConnectChildAttribute"))  {
        setConnectChildAttribute(_value);
      } else if (_name.equals("ConnectParentAttribute"))  {
        setConnectParentAttribute(_value);
      } else if (_name.equals("ConnectType"))  {
        setConnectType(Type.get(_value));
      } else if (_name.equals("ResultTable"))  {
        Table resultTable = Table.get(_context, _value);
if (resultTable==null)  {
  throw new Exception("Can not find table with name '"+_value+"'");
}
        setResultTable(resultTable);
      } else if (_name.equals("SearchForm"))  {
        Form searchForm = Form.get(_context, _value);
if (searchForm==null)  {
  throw new Exception("Can not find form with name '"+_value+"'");
}
        setSearchForm(searchForm);
      } else if (_name.equals("SearchType"))  {
Type searchType = Type.get(_value);
if (searchType==null)  {
  throw new Exception("Can not find type with name '"+_value+"'");
}
setSearchType(searchType);
      } else  {
        super.setProperty(_context, _name, _value);
      }
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * The instance variable stores the search type for this search command.
     *
     * @see #setSearchForm
     * @see #getSearchForm
     */
    private Type searchType = null;

    /**
     * The instance variable stores the result table for this search command.
     *
     * @see #setResultTable
     * @see #getResultTable
     */
    private Table resultTable = null;

    /**
     * The instance variable stores the search form for this search command.
     *
     * @see #setSearchForm
     * @see #getSearchForm
     */
    private Form searchForm = null;

    /**
     * The instance variable stores the name of the child attribute of the
     * connect type object.
     *
     * @see #setConnectChildAttribute
     * @see #getConnectChildAttribute
     */
    private String connectChildAttribute = null;

    /**
     * The instance variable stores the name of the parent attribute of the
     * connect type object.
     *
     * @see #setConnectParentAttribute
     * @see #getConnectParentAttribute
     */
    private String connectParentAttribute = null;

    /**
     * The instance variable stores the connect type.
     *
     * @see #setConnectType
     * @see #getConnectType
     */
    private Type connectType = null;

    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is the setter method for instance variable {@link #searchType}.
     *
     * @param _searchType  new value for instance variable {@link #searchType}
     * @see #searchType
     * @see #getsearchType
     */
    private void setSearchType(Type _searchType)  {
      this.searchType = _searchType;
    }

    /**
     * This is the getter method for instance variable {@link #searchType}.
     *
     * @return the value of the instance variable {@link #searchType}.
     * @see #searchType
     * @see #setsearchType
     */
    public Type getSearchType()  {
      return this.searchType;
    }

    /**
     * This is the setter method for instance variable
     * {@link #connectChildAttribute}.
     *
     * @param _connectChildAttribute  new value for instance variable
     *                                {@link #connectChildAttribute}
     * @see #connectChildAttribute
     * @see #getConnectChildAttribute
     */
    private void setConnectChildAttribute(String _connectChildAttribute)  {
      this.connectChildAttribute = _connectChildAttribute;
    }

    /**
     * This is the getter method for instance variable
     * {@link #connectChildAttribute}.
     *
     * @return the value of the instance variable
     *         {@link #connectChildAttribute}.
     * @see #connectChildAttribute
     * @see #setConnectChildAttribute
     */
    public String getConnectChildAttribute()  {
      return this.connectChildAttribute;
    }

    /**
     * This is the setter method for instance variable
     * {@link #connectParentAttribute}.
     *
     * @param _connectParentAttribute   new value for instance variable
     *                                  {@link #connectParentAttribute}
     * @see #connectParentAttribute
     * @see #getConnectParentAttribute
     */
    private void setConnectParentAttribute(String _connectParentAttribute)  {
      this.connectParentAttribute = _connectParentAttribute;
    }

    /**
     * This is the getter method for instance variable
     * {@link #connectParentAttribute}.
     *
     * @return the value of the instance variable
     *         {@link #connectParentAttribute}.
     * @see #connectParentAttribute
     * @see #setConnectParentAttribute
     */
    public String getConnectParentAttribute()  {
      return this.connectParentAttribute;
    }

    /**
     * This is the setter method for instance variable {@link #connectType}.
     *
     * @param _connectType  new value for instance variable
     *                      {@link #connectType}
     * @see #connectType
     * @see #getConnectType
     */
    private void setConnectType(Type _connectType)  {
      this.connectType = _connectType;
    }

    /**
     * This is the getter method for instance variable {@link #connectType}.
     *
     * @return the value of the instance variable {@link #connectType}.
     * @see #connectType
     * @see #setConnectType
     */
    public Type getConnectType()  {
      return this.connectType;
    }

    /**
     * This is the setter method for instance variable {@link #resultTable}.
     *
     * @param _resultTable new value for instance variable {@link #resultTable}
     * @see #resultTable
     * @see #getResultTable
     */
    private void setResultTable(Table _resultTable)  {
      this.resultTable = _resultTable;
    }

    /**
     * This is the getter method for instance variable {@link #resultTable}.
     *
     * @return the value of the instance variable {@link #resultTable}.
     * @see #resultTable
     * @see #setResultTable
     */
    public Table getResultTable()  {
      return this.resultTable;
    }

    /**
     * This is the setter method for instance variable {@link #searchForm}.
     *
     * @param _searchForm new value for instance variable {@link #searchForm}
     * @see #searchForm
     * @see #getSearchForm
     */
    private void setSearchForm(Form _searchForm)  {
      this.searchForm = _searchForm;
    }

    /**
     * This is the getter method for instance variable {@link #searchForm}.
     *
     * @return the value of the instance variable {@link #searchForm}.
     * @see #searchForm
     * @see #setSearchForm
     */
    public Form getSearchForm()  {
      return this.searchForm;
    }

    ///////////////////////////////////////////////////////////////////////////
  }
}