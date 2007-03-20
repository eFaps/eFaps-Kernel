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

package org.efaps.admin.datamodel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class AttributeType extends DataModelObject  {

  /**
   * Logging instance used in this class.
   */
  private final static Log LOG = LogFactory.getLog(AttributeType.class);

  /**
   * This is the sql select statement to select all attribute types from the
   * database.
   */
  private final static String SQL_SELECT  = "select "+
                                                "ID,"+
                                                "NAME,"+
                                                "UUID,"+
                                                "CLASSNAME,"+
                                                "CLASSNAMEUI,"+
                                                "ALWAYSUPDATE,"+
                                                "CREATEUPDATE "+
                                              "from DMATTRIBUTETYPE";

  /**
   * This is the constructor for class {@link Attribute}. Every instance of
   * class {@link Attribute} must have a name (parameter <i>_name</i>) and
   * an identifier (parameter <i>_id</i>).
   *
   * @param _id         id of the attribute
   * @param _uuid       universal unique identifier
   * @param _name       name of the instance
   */
  protected AttributeType(final long _id, 
                          final String _uuid,
                          final String _name)  {
    super(_id, _uuid, _name);
  }

  /**
   * Returns the name of the attribute type.
   *
   * @param _context
   * @see #getName
   */
  public String getViewableName(Context _context)  {
/*    String name = getName();
    ResourceBundle msgs = ResourceBundle.getBundle("org.efaps.properties.AttributeRessource", _context.getRequest().getLocale());
    try  {
      name = msgs.getString(getParent().getName()+"/"+name);
    } catch (MissingResourceException e)  {
    }
    return name;
*/
return getName();
  }

  /**
   *
   *
   * @return new instance of the class representation
   * @see #classRepr
   */
  AttributeTypeInterface newInstance() throws EFapsException  {
    AttributeTypeInterface ret = null;
    try  {
      ret = (AttributeTypeInterface)getClassRepr().newInstance();
    } catch (InstantiationException e)  {
      throw new EFapsException(getClass(), "newInstance.InstantiationException", e);
    } catch (IllegalAccessException e)  {
      throw new EFapsException(getClass(), "newInstance.IllegalAccessException", e);
    }
    return ret;
  }

  /**
   * The parameter <i>_classRepr</i> is the name of the class representation.
   * The method searches for the class and stores the class representation
   * in instance variable {@link #classRepr}.
   *
   * @param _classRepr class name of the class representation
   * @see #classRepr
   * @see #setClassRepr(Class)
   */
  private void setClassRepr(String _classRepr) throws ClassNotFoundException  {
    setClassRepr(Class.forName(_classRepr));
  }

  /**
   *
   */
  private void setUI(String _className) throws EFapsException  {
    try  {
      setUI((UIInterface)Class.forName(_className).newInstance());
    } catch (ClassNotFoundException e)  {
      throw new EFapsException(getClass(), "setUIClass.ClassNotFoundException", e, _className);
    } catch (InstantiationException e)  {
      throw new EFapsException(getClass(), "setUIClass.InstantiationException", e, _className);
    } catch (IllegalAccessException e)  {
      throw new EFapsException(getClass(), "setUIClass.IllegalAccessException", e, _className);
    } catch (ClassCastException e)  {
      throw new EFapsException(getClass(), "setUIClass.ClassCastException", e, _className);
    }
  }

  /**
   *
   */
  public String toString()  {
    return new ToStringBuilder(this).
        appendSuper(super.toString()).
        append("classRepr", getClassRepr()).
        append("alwaysUpdate", isAlwaysUpdate()).
        append("createUpdate", isCreateUpdate()).
        toString();
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable store the class representation for the attribute
   * type. With the class representation, a new instance of the value can
   * be created.
   *
   * @see #getClassRepr
   * @see #setClassRepr(Class)
   * @see #setClassRepr(String)
   */
  private Class classRepr = null;

  /**
   * The instance variable stores the instance for the ui interface.
   */
  private UIInterface ui = null;

  /**
   * The instance variable store the behavour, if an update is made. If the
   * value is set to <i>true</i>, the attribute must be always updated.
   *
   * @see #getAlwaysUpdate
   * @see #setAlwaysUpdate
   */
  private boolean alwaysUpdate = false;

  /**
   * The instance variable store the behavour, if an insert is made. If the
   * value is set to <i>true</i>, the attribute must be updated for an insert.
   *
   * @see #getCreateUpdate
   * @see #setCreateUpdate
   */
  private boolean createUpdate = false;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #classRepr}.
   *
   * @return value of instance variable {@link #classRepr}
   * @see #classRepr
   * @see #setClassRepr(Class)
   */
  public Class getClassRepr()  {
    return this.classRepr;
  }

  /**
   * This is the setter method for instance variable {@link #classRepr}.
   *
   * @param _classRepr new value for instance variable {@link #classRepr}
   * @see #classRepr
   * @see #getClassRepr
   */
  private void setClassRepr(Class _classRepr)  {
    this.classRepr = _classRepr;
  }

  /**
   * This is the getter method for instance variable {@link #ui}.
   *
   * @return value of instance variable {@link #ui}
   * @see #ui
   * @see #setUI(Class)
   */
  public UIInterface getUI()  {
    return this.ui;
  }

  /**
   * This is the setter method for instance variable {@link #ui}.
   *
   * @param _ui new value for instance variable {@link #ui}
   * @see #ui
   * @see #getUI
   */
  private void setUI(UIInterface _ui)  {
    this.ui = _ui;
  }

  /**
   * This is the getter method for instance variable {@link #alwaysUpdate}.
   *
   * @return value of instance variable {@link #alwaysUpdate}
   * @see #alwaysUpdate
   * @see #setAlwaysUpdate
   */
  public boolean isAlwaysUpdate()  {
    return this.alwaysUpdate;
  }

  /**
   * This is the setter method for instance variable {@link #alwaysUpdate}.
   *
   * @param _alwaysUpdate new value for instance variable {@link #alwaysUpdate}
   * @see #alwaysUpdate
   * @see #getAlwaysUpdate
   */
  void setAlwaysUpdate(boolean _alwaysUpdate)  {
    this.alwaysUpdate = _alwaysUpdate;
  }

  /**
   * This is the getter method for instance variable {@link #createUpdate}.
   *
   * @return value of instance variable {@link #createUpdate}
   * @see #createUpdate
   * @see #setCreateUpdate
   */
  public boolean isCreateUpdate()  {
    return this.createUpdate;
  }

  /**
   * This is the setter method for instance variable {@link #createUpdate}.
   *
   * @param _createUpdate new value for instance variable {@link #createUpdate}
   * @see #createUpdate
   * @see #getCreateUpdate
   */
  void setCreateUpdate(boolean _createUpdate)  {
    this.createUpdate = _createUpdate;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Initialise the cache of attribute types.
   *
   * @param _context  eFaps context for this request
   */
  static public void initialise() throws CacheReloadException  {
    ConnectionResource con = null;
    try  {
      con = Context.getThreadContext().getConnectionResource();

      Statement stmt = null;
      try  {
        stmt = con.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next())  {
          long id = rs.getLong(1);
          String name = rs.getString(2).trim();
          String uuid = rs.getString(3);
          uuid = (uuid == null) ? null : uuid.trim();

          if (LOG.isDebugEnabled())  {
            LOG.debug("read attribute type '" + name + "' "
                      + "(id = " + id + ", uuid = '" + uuid + "')");
          }

          AttributeType attrType = new AttributeType(id, uuid, name);
          attrType.setClassRepr(rs.getString(4).trim());
          attrType.setUI(rs.getString(5).trim());
          if (rs.getInt(6) != 0)  {
            attrType.setAlwaysUpdate(true);
          }
          if (rs.getInt(7) != 0)  {
            attrType.setCreateUpdate(true);
          }
          getCache().add(attrType);
        }
        rs.close();
      } finally  {
        if (stmt != null)  {
          stmt.close();
        }
      }
      con.commit();
    } catch (ClassNotFoundException e)  {
      throw new CacheReloadException("could not read attribute types", e);
    } catch (SQLException e)  {
      throw new CacheReloadException("could not read attribute types", e);
    } catch (EFapsException e)  {
      throw new CacheReloadException("could not read attribute types", e);
    } finally  {
      if ((con != null) && con.isOpened())  {
        try  {
          con.abort();
        } catch (EFapsException e)  {
          throw new CacheReloadException("could not read attribute types", e);
        }
      }
    }
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class
   * {@link AttributeType}.
   *
   * @param _context  eFaps context for this request
   * @param _id       id to search in the cache
   * @return instance of class {@link AttributeType}
   * @see #getCache
   * @see #read
   */
  static public AttributeType get(long _id)  {
    return getCache().get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link AttributeType}.
   *
   * @param _context  eFaps context for this request
   * @param _name     name to search in the cache
   * @return instance of class {@link AttributeType}
   * @see #getCache
   * @see #read
   */
  static public AttributeType get(String _name)  {
    return getCache().get(_name);
  }

  /**
   * Static getter method for the type hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static Cache<AttributeType> getCache()  {
    return cache;
  }

  /**
   * Stores all instances of class {@link AttributeType}.
   *
   * @see #getCache
   */
  static private Cache<AttributeType> cache = new Cache<AttributeType>(
    new CacheReloadInterface()  {
        public int priority()  {
          return CacheReloadInterface.Priority.AttributeType.number;
        };
        public void reloadCache() throws CacheReloadException  {
        };
    }
  );
}