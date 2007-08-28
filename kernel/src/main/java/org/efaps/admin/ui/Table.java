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

package org.efaps.admin.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.Context;
import org.efaps.util.cache.CacheReloadException;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Table extends Collection implements Cloneable  {

  /**
   * The static variable defines the class name in eFaps.
   */
  static public EFapsClassName EFAPS_CLASSNAME = EFapsClassName.TABLE;

  /**
   * This is the constructor to set the id and the name.
   *
   * @param _id     id of the new table
   * @param _name   name of the new table
   */
  public Table(final Long _id, final String _uuid, final String _name) {
    super(_id, _uuid, _name);
  }

  /**
   * The instance  method returns the title of the table.
   *
   * @param _context  context for this request
   * @return title of the form
   */
  public String getViewableName(Context _context)  {
    String title = "";
    ResourceBundle msgs = ResourceBundle.getBundle("org.efaps.properties.AttributeRessource", _context.getLocale());
    try  {
      title = msgs.getString("Table.Title."+getName());
    } catch (MissingResourceException e)  {
    }
    return title;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method sets a new property value.
   *
   * @param _name   name of the property
   * @param _value  value of the property
   * @param _toId   id of the to object
   */
  protected void setProperty(final String _name, 
                             final String _value) throws CacheReloadException  {
    if (_name.equals("SelectionColRef"))  {
      setSelectableColRef(Attribute.get(_value));
    } else  {
      super.setProperty(_name, _value);
    }
  }

  /**
   * Creates and returns a copy of this table object.
   */
  public Table cloneTable()   {
    Table ret = null;
    try  {
      ret = (Table)super.clone();
    } catch (CloneNotSupportedException e)  {
e.printStackTrace();
    }
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the instance variable for the selectable column reference. If the
   * variable is set, the table gets for each line a selectable check bottom.
   *
   * @see #setSelectableColRef
   * @see #getSelectableColRef
   */
  private Attribute selectableColRef = null;

  /**
   * @see #getPageStyle
   * @see #isMultiPageStyle
   * @see #isSinglePageStyle
   * @see #setMultiPageStyle
   * @see #setPageStyle
   * @see #setSinglePageStyle
   */
  private enum PageStyle {SINGLE, MULTI};

  /**
   * This is the instance variable for the style of the page of the table.
   * Values defined in {@link #PageStyle} are possible. Default value is
   * {@link #PageStyle,MULTI}.
   *
   *
   */
  private PageStyle pageStyle = PageStyle.MULTI;

private String eventOnLoad = null;

public void setEventOnLoad(String _eventOnLoad)  {
  this.eventOnLoad = _eventOnLoad;
}

public String getEventOnLoad()  {
  return this.eventOnLoad;
}

private String javaScriptCode = null;

public void setJavaScriptCode(String _javaScriptCode)  {
  this.javaScriptCode = _javaScriptCode;
}

public String getJavaScriptCode()  {
  return this.javaScriptCode;
}

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #selectableColRef}.
   *
   * @param _selectableColRef new value for instance variable
   *                          {@link #selectableColRef}
   * @see #selectableColRef
   * @see #getSelectableColRef
   */
  public void setSelectableColRef(Attribute _selectableColRef)  {
    this.selectableColRef = _selectableColRef;

    this.selectableColRefIndex= addFieldExpr(
      getSelectableColRef().getTable().getSqlTable()+"."+getSelectableColRef().getName()
    );
  }

private int selectableColRefIndex = -1;
public int getSelectableColRefIndex()  {
  return this.selectableColRefIndex;
}

  /**
   * This is the getter method for instance variable {@link #selectableColRef}.
   *
   * @see #selectableColRef
   * @see #setSelectableColRef
   */
  public Attribute getSelectableColRef()  {
    return this.selectableColRef;
  }

  /**
   * This is the setter method for instance variable {@link #pageStyle}.
   *
   * @param _pageStyle new value for instance variable {@link #pageStyle}.
   * @see #pageStyle
   * @see #getPageStyle
   */
  private void setPageStyle(PageStyle _pageStyle)  {
    this.pageStyle = _pageStyle;
  }

  /**
   * This is the getter method for instance variable {@link #pageStyle}.
   *
   * @see #pageStyle
   * @see #setPageStyle
   */
  private PageStyle getPageStyle()  {
    return this.pageStyle;
  }

  /**
   * The table is representated in single page style. The value is stored in
   * instance variable {@link #pageStyle} with the value
   * {@link #PageStyle.SINGLE}.
   *
   * @see #pageStyle
   * @see #setPageStyle
   * @see #isSinglePageStyle
   */
  public void setSinglePageStyle()  {
    setPageStyle(PageStyle.SINGLE);
  }

  /**
   * If instance variable {@link #pageStyle} is set to
   * {@link #PageStyle.SINGLE}, a <i>true</i> is returned, otherwise a
   * <i>false</i> is returned.
   *
   * @return <i>true</i> / <i>false</i>
   * @see #pageStyle
   * @see #getPageStyle
   * @see #setSinglePageStyle
   */
  public boolean isSinglePageStyle()  {
    return (getPageStyle() == PageStyle.SINGLE);
  }

  /**
   * The table is representated in multi page style. The value is stored in
   * instance variable {@link #pageStyle} with the value
   * {@link #PageStyle.MULTI}.
   *
   * @see #pageStyle
   * @see #setPageStyle
   * @see #isMultiPageStyle
   */
  public void setMultiPageStyle()  {
    setPageStyle(PageStyle.MULTI);
  }

  /**
   * If instance variable {@link #pageStyle} is set to
   * {@link #PageStyle.MULTI}, a <i>true</i> is returned, otherwise a
   * <i>false</i> is returned.
   *
   * @return <i>true</i> / <i>false</i>
   * @see #pageStyle
   * @see #getPageStyle
   * @see #setMultiPageStyle
   */
  public boolean isMultiPageStyle()  {
    return (getPageStyle() == PageStyle.MULTI);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Table}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link Table}
   * @see #getCache
   */
  static public Table get(long _id)  {
    return getCache().get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Table}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Table}
   * @see #getCache
   */
  static public Table get(String _name)  {
    return getCache().get(_name);
  }

  /**
   * Returns for given parameter <i>UUID</i> the instance of class
   * {@link Table}.
   *
   * @param _uuid UUID to search in the cache
   * @return instance of class {@link Table}
   * @see #getCache
   */
  static public Table get(UUID _uuid)  {
    return getCache().get(_uuid);
  }
  
  /**
   * Static getter method for the type hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static UserInterfaceObjectCache<Table> getCache()  {
    return cache;
  }

  /**
   * Stores all instances of class {@link Table}.
   *
   * @see #getCache
   */
  static private UserInterfaceObjectCache < Table > cache = new UserInterfaceObjectCache < Table > (Table.class);

  /////////////////////////////////////////////////////////////////////////////

}
