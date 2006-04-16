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

package org.efaps.db;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.JoinRowSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.ui.Collection;
import org.efaps.admin.ui.Field;
import org.efaps.db.query.CompleteStatement;
import org.efaps.db.query.WhereClause;
import org.efaps.db.query.WhereClauseAttrEqAttr;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 *
 */
public abstract class AbstractQuery  {

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(AbstractQuery.class);

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance destructor calls the instance method {@link #close} to close
   * the SQL statement if not happend till now. The exception thrown from
   * method {@link #close} is catched and no error is thrown from this
   * destructor.
   *
   * @see #close
   */
  public void finalize()  {
    try  {
      close();
    } catch (Exception e)  {
    }
  }

  /**
   * The method closes the SQL statement. The method must be always called to
   * close the query!
   *
   * @see #statement
   */
  public void close() throws Exception  {
/*    if (getStatement()!=null)  {
      try  {
        getStatement().close();
      } catch (Exception e)  {
        throw e;
      } finally  {
        setStatement(null);
      }
    }
*/
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method adds all fields of the collection user interface
   * object to the search query.
   *
   * @param _context      context for this request
   * @param _collection   collection user interface object with fields to add
   * @see #add(Field)
   */
  public void add(Context _context, Collection _collection) throws Exception  {
    for (int i=0; i<_collection.getFields().size(); i++)  {
      Field field = (Field)_collection.getFields().get(i);
      add(field);
//      if (field.getReference()!=null)  {
//        addAllFromString(_context, field.getReference());
//      }
    }
  }

  /**
   * The method adds an single attribute from a type to the select statement.
   *
   * @param _attr attribute to add to the query
   * @see #add(Object, Attribute)
   */
  public void add(Attribute _attr)  {
    addSelect(false, _attr, _attr);
  }

  /**
   * The method adds a single field from a form or a table to the select
   * statement.
   *
   * @param _field  field to add to the query
   * @see #add(Object, Attribute)
   */
abstract public void add(Field _field) throws Exception;
//  public void add(Field _field) throws Exception  {
//    if (_field.getAttribute()!=null)  {
//      add(_field, _field.getAttribute());
//    } else if (_field.getProgramValue()!=null)  {
//      _field.getProgramValue().addSelectAttributes(null, this);
//    }
//  }

  /**
   * @param _isOID  must be set to <i>true</i> is select expression selects
   *                the OID of the business object.
   * @param _key    key to store the select expression in the select
   *                map expression
   * @param _attr   attribute itself which must be selected
   */
  protected void addSelect(boolean _isOID, Object _key, Attribute _attr)  {
    getSelectType(_attr.getParent()).addSelect(_isOID, _key, _attr);
  }

  /**
   * @param _isOID      must be set to <i>true</i> is select expression
   *                    selects the OID of the business object.
   * @param _key        key to store the select expression in the select
   *                    map expression
   * @param _expression
   */
  protected void addSelect(boolean _isOID, Object _key, Type _type, String _expression) throws Exception  {
    getSelectType(_type).addSelect(_isOID, _key, _expression);
  }

  /**
   * The instance method adds types in the order of the expand.
   *
   * @param _type type to add in the correct order
   * @see #addTypes4Order(Type,boolean)
   */
  public void addTypes4Order(Type _type)  {
    addTypes4Order(_type, false);
  }

  /**
   * The instance method adds types in the order of the expand.
   *
   * @param _type         type to add in the correct order
   * @param _nullAllowed  type can be null
   * @see #selectTypesOrder
   * @see #getSelectType
   */
  public void addTypes4Order(Type _type, boolean _nullAllowed)  {
//System.out.println("addTypes4Order="+_type.getName()+":"+getSelectTypesOrder().size());
    SelectType selectType = getSelectType(_type);
    selectType.setOrderIndex(getSelectTypesOrder().size());
    selectType.setNullAllowed(_nullAllowed);
    getSelectTypesOrder().add(selectType);
  }

  /**
   * The instance method returns for the given type the select type class
   * instance of {@link #SelectType}.
   *
   * @param _type type for which the instance of {@link #SelectType} is
   *              searched
   * @see #addTypes4Order
   * @see #selectTypesOrder
   */
  public SelectType getSelectType(Type _type)  {
//System.out.println("----> AbstractQuery.getSelectType()._type = "+_type.getName());
SelectType selectType = getMainSelectTypes().get(_type);
    if (selectType==null)  {
      selectType = new SelectType(getMainJoinElement(), _type, getSelectTypesOrder().size());
getMainSelectTypes().put(_type, selectType);
      for (JoinElement elm : getJoinElements())  {
        elm.addSelectType(selectType);
      }
    }
    return selectType;
  }

  /**
   * The method returns the size of the select expressions.
   *
   * @return size of the select expressions
   * @see #getExpressions
   */
  public int selectSize()  {
    int ret = 0;
    for (JoinElement elm : getJoinElements())  {
      ret += elm.selectSize();
    }
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method returns for the given key the attribute value.
   *
   * @param _context  eFaps context for this request
   * @param _key      key for which the attribute value must returned
   * @return atribute value for given key
   */
  public Object get(Context _context, Object _key) throws Exception  {
    Object ret = null;
    SelExpr2Attr selExpr = getAllSelExprMap().get(_key);
    if (selExpr!=null)  {
      ret = selExpr.getAttrValue(_context);
    }
    return ret;
  }

  /**
   * The instance method returns for the given key the atribute.
   *
   * @param _context  eFaps context for this request
   * @param _key      key for which the attribute value must returned
   * @return attribute for given key
   */
  public Attribute getAttribute(Context _context, Object _key) throws Exception  {
    Attribute ret = null;
    SelExpr2Attr selExpr = getAllSelExprMap().get(_key);
    if (selExpr!=null)  {
      ret = selExpr.getAttribute();
    }
    return ret;
  }

  /**
   *
   * @param _context  eFaps context for this request
   * @param _key      key for which the object id value must returned
   * @return object id for given key
   */
  public String getOID(Context _context, Object _key) throws Exception  {
    String ret = null;
    SelExpr2Attr selExpr = getAllOIDSelExprMap().get(_key);
    if (selExpr!=null)  {
      ret = (String)selExpr.getAttrValue(_context);
    }
    return ret;
  }

  /**
   * The instance method returns the instance for the current selected row.
   *
   * @param _context  context for this request
   */
  public Instance getInstance(Context _context, Type _type) throws Exception  {
    SelectType selectType = getMainSelectTypes().get(_type);
if (selectType==null)  {
  throw new Exception("Type "+_type.getName()+" is not selected! New Instance can not created!");
}
//    String id = getResultSet().getString(selectType.getIndexId().intValue());
String id = this.cachedResult.getString(selectType.getIndexId().intValue());

Type type = _type;

if (selectType.getIndexType()!=null)  {
//  long typeId = getResultSet().getLong(selectType.getIndexType().intValue());
  long typeId = this.cachedResult.getLong(selectType.getIndexType().intValue());
  type = Type.get(typeId);
}

    return new Instance(_context, type, id);
  }

  /////////////////////////////////////////////////////////////////////////////

org.efaps.db.query.CachedResult cachedResult = null;

  /**
   * The instance method executes the query.
   */
  public void execute(Context _context) throws Exception  {
try  {
    if (getMainJoinElement().selectSize()>0)  {
//System.out.println("---------------AbstractQuery.getMainJoinElement().selectSize()>0");


int incSelIndex = 0;
//JoinRowSet jrs = _context.getDbType().createJoinRowSetInstance();
/*oracle.jdbc.rowset.OracleJoinRowSet jrs = new oracle.jdbc.rowset.OracleJoinRowSet();*/

this.cachedResult = new org.efaps.db.query.CachedResult();

      for (JoinElement joinElement : getJoinElements())  {


joinElement.setIncSelIndex(incSelIndex);
// warum diese überprüfung? weil der join jeweils die spalte mit der id zum vergleichen rauschmeisst!!
if (incSelIndex == 0)  {
  incSelIndex += joinElement.getSelectExpressions().size();
} else  {
  incSelIndex += joinElement.getSelectExpressions().size()-1;
}


        CompleteStatement completeStatement = new CompleteStatement();

        joinElement.appendStatement(completeStatement, -1, isExpandChildTypes());

        for (SelectType selectType : getSelectTypesOrder())  {
          if (selectType.isNullAllowed())  {
            completeStatement.appendUnion();
            joinElement.appendStatement(completeStatement, selectType.getOrderIndex(), isExpandChildTypes());
          }
        }

        execute(_context, completeStatement);

        cachedResult.populate(this.resultSet, joinElement.getMatchColumn());
      }


//      setResultSet(jrs);
setResultSet(null);


//System.out.println("----getAllSelExprMap()="+getAllSelExprMap());
for (SelExpr2Attr selExpr : getAllSelExprMap().values())  {
  selExpr.initSelectIndex();
}
//System.out.println("----getAllOIDSelExprMap()="+getAllOIDSelExprMap());
for (SelExpr2Attr selExpr : getAllOIDSelExprMap().values())  {
  selExpr.initSelectIndex();
}


    }
//System.out.println("----end execute()");
} catch (Exception e)  {
e.printStackTrace();
}
this.cachedResult.beforeFirst();
  }

  /**
   * The instance method executes the query.
   *
   * @param _context  context for this request
   */
  private void execute(Context _context, CompleteStatement _completeStatement) throws EFapsException  {
    ConnectionResource con = null;
    try  {
      con = _context.getConnectionResource();

      if (LOG.isTraceEnabled())  {
        LOG.trace(_completeStatement.getStatement().toString());
      }

      Statement stmt = con.getConnection().createStatement();
      ResultSet rs = stmt.executeQuery(_completeStatement.getStatement().toString());

CachedRowSet crs = _context.getDbType().createCachedRowSetInstance();

      crs.populate(rs);
      setResultSet(crs);

      rs.close();
      stmt.close();
      con.commit();
    } catch (EFapsException e)  {
      if (con != null)  {
        con.abort();
      }
      throw e;
    } catch (Throwable e)  {
      if (con != null)  {
        con.abort();
      }
// TODO: exception eintragen!
e.printStackTrace();
throw new EFapsException(getClass(), "execute.Throwable");
    }
  }


  /**
   *
   * @return <i>true</i> if a new row is selected and exists, otherwise
   *         <i>false</i>
   */
  public boolean next() throws Exception  {
    return this.cachedResult.next();
/*
    boolean ret;
    if (getResultSet()!=null)  {
      ret = getResultSet().next();
    } else  {
      ret = false;
    }
    return ret;
*/
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the statement instance for this query.
   *
   * @see #getStatement
   * @see #setStatement
   */
  private Statement statement = null;

  /**
   * The instance variable stores the result set for this query.
   *
   * @see #getResultSet
   * @see #setResultSet
   */
  private CachedRowSet resultSet = null;

  /**
   * The instance variable stores the order of the select types.
   *
   * @see #getSelectTypesOrder
   */
  private List<SelectType> selectTypesOrder = new ArrayList<SelectType>();

  /**
   * The instance variable stores all single join elements.
   *
   * @see #getJoinElements
   */
  private List<JoinElement> joinElements = new ArrayList<JoinElement>();

  /**
   * The instance variable maps expressions to join elements.
   */
  private Map<String,JoinElement> mapJoinElements = new HashMap<String,JoinElement>();

  /**
   * The instance variable stores the main instance of the join element. The
   * main join element is that join elment which stores the direct selectable
   * attribute values.
   *
   * @see #getMainJoinElement
   */
  private JoinElement mainJoinElement = new JoinElement();

  /**
   * The instance variable stores all main selected types. The key in this map
   * is the main table.
   *
   * @see #getSelectTypes
   */
  private Map<Type,SelectType> mainSelectTypes = new HashMap<Type,SelectType>();

  /**
   * The instance variable stores all main where clauses. This where clauses
   * must be used by all join elements! This is a different behaviour than
   * the where clauses for a join element.
   *
   * @see #getMainWhereClauses
   */
  private List<WhereClause> mainWhereClauses = new ArrayList<WhereClause>();

  /**
   * Should the child types als be expanded?
   *
   * @see #isExpandChildTypes
   * @see #setExpandChildTypes
   */
  private boolean expandChildTypes = true;

  /**
   * The instance variable stores all select expressions and their relations
   * to attributes for this query.
   *
   * @see #getAllSelExprMap
   */
  private Map<Object,SelExpr2Attr> allSelExprMap = new HashMap<Object,SelExpr2Attr>();

  /**
   * The instance variable stores all OID select expressions and their
   * relations to attributes for this query.
   *
   * @see #getAllOIDSelExprMap
   */
  private Map<Object,SelExpr2Attr> allOIDSelExprMap = new HashMap<Object,SelExpr2Attr>();

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #statement}.
   *
   * @return value of instance variable {@link #statement}
   * @see #statement
   * @see #setStatement
   */
/*  private Statement getStatement()   {
    return this.statement;
  }
*/
  /**
   * This is the setter method for instance variable {@link #statement}.
   *
   * @param _statement new value for instance variable {@link #statement}
   * @see #statement
   * @see #getStatement
   */
/*  private void setStatement(Statement _statement)  {
    this.statement = _statement;
  }
*/
  /**
   * This is the getter method for instance variable {@link #resultSet}.
   *
   * @return value of instance variable {@link #resultSet}
   * @see #resultSet
   * @see #setResultSet
   */
//  private CachedRowSet getResultSet()   {
//    return this.resultSet;
//  }

  /**
   * This is the setter method for instance variable {@link #resultSet}.
   *
   * @param _resultSet new value for instance variable {@link #resultSet}
   * @see #resultSet
   * @see #getResultSet
   */
  private void setResultSet(CachedRowSet _resultSet)  {
    this.resultSet = _resultSet;
  }

  /**
   * This is the getter method for instance variable {@link #selectTypesOrder}.
   *
   * @return value of instance variable {@link #selectTypesOrder}
   * @see #selectTypesOrder
   */
  public List<SelectType> getSelectTypesOrder()   {
    return this.selectTypesOrder;
  }

  /**
   * This is the getter method for instance variable {@link #joinElements}.
   *
   * @return value of instance variable {@link #joinElements}
   * @see #joinElements
   */
  protected List<JoinElement> getJoinElements()   {
    return this.joinElements;
  }

  /**
   * This is the getter method for instance variable {@link #mapJoinElements}.
   *
   * @return value of instance variable {@link #mapJoinElements}
   * @see #mapJoinElements
   */
  protected Map<String,JoinElement> getMapJoinElements()   {
    return this.mapJoinElements;
  }

  /**
   * This is the getter method for instance variable {@link #mainJoinElement}.
   *
   * @return value of instance variable {@link #mainJoinElement}
   * @see #mainJoinElement
   */
  protected JoinElement getMainJoinElement()   {
    return this.mainJoinElement;
  }

  /**
   * This is the getter method for instance variable {@link #mainSelectTypes}.
   *
   * @return value of instance variable {@link #mainSelectTypes}
   * @see #mainSelectTypes
   */
  private Map<Type,SelectType> getMainSelectTypes()   {
    return this.mainSelectTypes;
  }

  /**
   * This is the getter method for instance variable {@link #mainWhereClauses}.
   *
   * @return value of instance variable {@link #mainWhereClauses}
   * @see #mainWhereClauses
   */
  protected List<WhereClause> getMainWhereClauses()   {
    return this.mainWhereClauses;
  }

  /**
   * This is the getter method for instance variable {@link #expandChildTypes}.
   *
   * @return value of instance variable {@link #expandChildTypes}
   * @see #expandChildTypes
   * @see #setExpandChildTypes
   */
  protected boolean isExpandChildTypes()   {
    return this.expandChildTypes;
  }

  /**
   * This is the setter method for instance variable {@link #expandChildTypes}.
   *
   * @param _expandChildTypes new value for instance variable {@link #expandChildTypes}
   * @see #expandChildTypes
   * @see #isExpandChildTypes
   */
  public void setExpandChildTypes(boolean _expandChildTypes)  {
    this.expandChildTypes = _expandChildTypes;
  }

  /**
   * This is the getter method for instance variable {@link #allSelExprMap}.
   *
   * @return value of instance variable {@link #allSelExprMap}
   * @see #allSelExprMap
   */
  protected Map<Object,SelExpr2Attr> getAllSelExprMap()  {
    return this.allSelExprMap;
  }

  /**
   * This is the getter method for instance variable {@link #allOIDSelExprMap}.
   *
   * @return value of instance variable {@link #allOIDSelExprMap}
   * @see #allOIDSelExprMap
   */
  protected Map<Object,SelExpr2Attr> getAllOIDSelExprMap()  {
    return this.allOIDSelExprMap;
  }

  //###########################################################################
  //###########################################################################
  //###########################################################################
  //###########################################################################
  //###########################################################################

  /**
   * The class is used to make one join select.
   */
  public class JoinElement  {

    private JoinElement()  {
if (getMainSelectTypes()!=null)  {
      getSelectTypes().addAll(getMainJoinElement().getSelectTypes());
}
      getJoinElements().add(this);
    }


    protected void appendStatement(CompleteStatement _completeStatement, int _orderIndex, boolean _childTypes)  {
      _completeStatement.append("select distinct ");
      appendSelectExpressions(_completeStatement, _orderIndex);
      appendFrom(_completeStatement, _orderIndex);
      appendWhereClause(_completeStatement, _orderIndex, _childTypes);

      for (SelectType selectType : getSelectTypesOrder())  {
        if (selectType.isNullAllowed() && (_orderIndex<0 || selectType.getOrderIndex()<_orderIndex))  {
          _completeStatement.appendUnion();
          _completeStatement.append("select distinct ");
          appendSelectExpressions(_completeStatement, selectType.getOrderIndex());
          appendFrom(_completeStatement, selectType.getOrderIndex());
          appendWhereClause(_completeStatement, selectType.getOrderIndex(), _childTypes);
        }
      }
    }


    /**
     * Appends all select expressions from this one join element to the
     * comlete statement.
     *
     * @param _completeStatement  complete SQL statement to select values
     * @param _orderIndex         ????????????????????
     */
    private void appendSelectExpressions(CompleteStatement _completeStatement, int _orderIndex)  {
      Iterator<SelectExpression> iter = getSelectExpressions().iterator();
      while (iter.hasNext())  {
        SelectExpression selectExpr = iter.next();
        if (_orderIndex<0 || selectExpr.getSelectType().getOrderIndex()<_orderIndex)  {
          _completeStatement.append(selectExpr.getExpression());
        } else  {
          _completeStatement.append(selectExpr.getNullString());
        }
        if (iter.hasNext())  {
          _completeStatement.append(",");
        }
      }
    }

private void appendFrom(CompleteStatement _completeStatement, int _orderIndex)  {
  for (SelectType selectType: getSelectTypes())  {
    if (_orderIndex<0 || selectType.getOrderIndex()<_orderIndex)  {
      selectType.appendFrom(_completeStatement);
    }
  }
}

private void appendWhereClause(CompleteStatement _completeStatement, int _orderIndex, boolean _childTypes)  {
    Iterator<SelectType> typeIter = getSelectTypes().iterator();
    while (typeIter.hasNext())  {
      SelectType selectType = typeIter.next();
      if (_orderIndex<0 || selectType.getOrderIndex()<_orderIndex)  {
        _completeStatement.appendWhereAnd();
        selectType.appendTypeWhereClause(_completeStatement, _childTypes);
      }
    }

    for (WhereClause whereClause : whereClauses)  {
      whereClause.appendWhereClause(_completeStatement, _orderIndex);
    }

    for (WhereClause whereClause : getMainWhereClauses())  {
      whereClause.appendWhereClause(_completeStatement, _orderIndex);
    }
}

    /**
     * The method returns the size of the select expressions.
     *
     * @return size of the select expressions
     * @see #getExpressions
     */
    public int selectSize()  {
      return getExpressions().size();
    }

    /**
     * Returns for the given expression the select expression. The select
     * expression is tested for uniqueness (and if defined already reused).
     *
     * @param _selectType instance of SelectType
     * @param _expression select expression
     * @param _nullString if no select expression can be made, this is the
     *                    select expression for null value
     * @return new select expression
     */
    private SelectExpression getSelectExpression(SelectType _selectType, String _expression, String _nullString)  {
      SelectExpression selectExpr = getExpressions().get(_expression);
      if (selectExpr==null)  {
        selectExpr = new SelectExpression(getExpressions().size()+1, _expression, this, _selectType, _nullString);
        getExpressions().put(_expression, selectExpr);
        getSelectExpressions().add(selectExpr);
      }
      return selectExpr;
    }

    /**
     * @param _index      index used to create the sql prefix
     * @param _selectType instance of SelectType
     * @param _isOID      the select statement is an OID select and must
     *                    be stored in the OID map
     * @param _key        key used to store in the (OID) map
     * @param _attribute  attribute to select
     * @see #getSelectExpression
     */
    private void addSelectAttribute(int _index, SelectType _selectType, boolean _isOID, Object _key, Attribute _attr)  {
//System.out.println("addSelectAttribute="+_index+":"+_key+":"+_attr);

      ArrayList<SelectExpression> selectExprs = new ArrayList<SelectExpression>();

      if (_attr.getTable()!=null)  {
        String sqlPrefix = _attr.getTable().getSqlTable() + _index;
        _selectType.getTypeTableNames().add(_attr.getTable());
        for (String _sqlColName : _attr.getSqlColNames())  {
          SelectExpression selectExpr = getSelectExpression(_selectType, sqlPrefix + "." + _sqlColName, "''");
//System.out.println("selectExprs.add="+selectExpr);
          selectExprs.add(selectExpr);
        }
      }
      SelExpr2Attr selExpr2Attr = new SelExpr2Attr(_attr, selectExprs);
//System.out.println("selectExprs="+selExpr2Attr);
      if (_isOID)  {
        getAllOIDSelExprMap().put(_key, selExpr2Attr);
      } else  {
        getAllSelExprMap().put(_key, selExpr2Attr);
      }
    }


    /**
     * The instance method creates a new instance of {@link #SelectType} add
     * adds them to {@link #selectTypesOrder}.
     *
     * @param _type         type to add in the correct order
     * @param _nullAllowed  type can be null
     * @return new created instance of {@link #SelectType}
     * @see #selectTypesOrder
     */
    protected SelectType getNewSelectType(Type _type, boolean _nullAllowed)  {
  //System.out.println("addTypes4Order="+_type.getName()+":"+getSelectTypesOrder().size());
      SelectType selectType = new SelectType(this, _type, (getSelectTypesOrder().size()+1000));
      selectType.setOrderIndex(getSelectTypesOrder().size()+1000);
      selectType.setNullAllowed(_nullAllowed);
      getSelectTypesOrder().add(selectType);
addSelectType(selectType);
      return selectType;
    }


private void addWhere(SelectType _selectType1, Attribute _attr1, SelectType _selectType2, Attribute _attr2) throws Exception  {
whereClauses.add(new WhereClauseAttrEqAttr(_selectType1, _attr1, _selectType2, _attr2));
}


  private void addSelectType(SelectType _selectType)  {
    getSelectTypes().add(_selectType);
  }

    ///////////////////////////////////////////////////////////////////////////
private List<WhereClause> whereClauses = new ArrayList<WhereClause>();


    /**
     * The instance variable stores all select types of the select
     * statement.
     *
     * @see #getTableNames
     */
    private Set<SelectType> selectTypes = new HashSet<SelectType>();

    /**
     * The hash table instance variable stores depending on the key (field)
     * the index in the select statement.
     *
     * @see #getKeys
     */
//    private Map<Object,SelectExpression> keys = new HashMap<Object,SelectExpression>();

    /**
     * The hash table instance variable stores depending on the key (field)
     * the index in the OID select statement.
     *
     * @see #getKeysOID
     */
//    private Map<Object,SelectExpression> keysOID = new HashMap<Object,SelectExpression>();

    /**
     * This is the instance variable to hold all expressions. The SQL statement
     * is stores as key, the value is the index of the expression in the select
     * statement. This is used that an expression is only once in a select
     * statement (uniqueness)!.
     *
     * @see #getExpressions
     */
    private Map<String,SelectExpression> expressions = new HashMap<String,SelectExpression>();

    /**
     * The instance variable stores all select expressions.
     *
     * @see #getSelectExpressions
     */
    private List<SelectExpression> selectExpressions = new ArrayList<SelectExpression>();

    /**
     * The instance variable stores the order of the select types. The
     * information is needed if an expand is made (and in an expand a null
     * value is possible!).
     *
     * @see #getSelectTypesOrder
     */
    private List<SelectType> selectTypesOrder = new ArrayList<SelectType>();

    /**
     * The instance variable stores the column index of this join element. The
     * index is the select expression used to join with other select
     * statements. The default value is <i>1</i> for the first column.
     *
     * @see #getMatchColumn
     * @see #setMatchColumn
     */
    private int matchColumn = 1;

    /**
     * The instance variable stores the number of previous select epxressions
     * used to calculate the index of select expressions of this join element
     * in the complete join.
     *
     * @see #getIncSelIndex
     * @see #setIncSelIndex
     */
    private int incSelIndex = 0;

    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is the getter method for instance variable {@link #selectTypes}.
     *
     * @return value of instance variable {@link #selectTypes}
     * @see #selectTypes
     */
    private Set<SelectType> getSelectTypes()   {
      return this.selectTypes;
    }

    /**
     * This is the getter method for instance variable {@link #keys}.
     *
     * @return value of instance variable {@link #keys}
     * @see #keys
     */
//    private Map<Object,SelectExpression> getKeys()   {
//      return this.keys;
//    }

    /**
     * This is the getter method for instance variable {@link #keysOID}.
     *
     * @return value of instance variable {@link #keysOID}
     * @see #keysOID
     */
//    private Map<Object,SelectExpression> getKeysOID()   {
//      return this.keysOID;
//    }

    /**
     * This is the getter method for instance variable {@link #expressions}.
     *
     * @return value of instance variable {@link #expressions}
     * @see #expressions
     */
    protected Map<String,SelectExpression> getExpressions()  {
      return this.expressions;
    }

    /**
     * This is the getter method for instance variable {@link #selectExpressions}.
     *
     * @return value of instance variable {@link #selectExpressions}
     * @see #selectExpressions
     */
    protected List<SelectExpression> getSelectExpressions()  {
      return this.selectExpressions;
    }

    /**
     * This is the getter method for instance variable {@link #selectTypesOrder}.
     *
     * @return value of instance variable {@link #selectTypesOrder}
     * @see #selectTypesOrder
     */
    public List<SelectType> getSelectTypesOrder()   {
      return this.selectTypesOrder;
    }

    /**
     * This is the setter method for instance variable {@link #matchColumn}.
     *
     * @param _orderIndex new value for instance variable {@link #matchColumn}
     * @see #matchColumn
     * @see #getMatchColumn
     */
    private void setMatchColumn(int _matchColumn)  {
      this.matchColumn = _matchColumn;
    }

    /**
     * This is the getter method for instance variable {@link #matchColumn}.
     *
     * @return value of instance variable {@link #matchColumn}
     * @see #matchColumn
     * @see #setMatchColumn
     */
    public int getMatchColumn()   {
      return this.matchColumn;
    }

    /**
     * This is the setter method for instance variable {@link #incSelIndex}.
     *
     * @param _orderIndex new value for instance variable {@link #incSelIndex}
     * @see #incSelIndex
     * @see #getIncSelIndex
     */
    private void setIncSelIndex(int _incSelIndex)  {
      this.incSelIndex = _incSelIndex;
    }

    /**
     * This is the getter method for instance variable {@link #incSelIndex}.
     *
     * @return value of instance variable {@link #incSelIndex}
     * @see #incSelIndex
     * @see #setIncSelIndex
     */
    private int getIncSelIndex()   {
      return this.incSelIndex;
    }
  }

  //###########################################################################
  //###########################################################################
  //###########################################################################
  //###########################################################################
  //###########################################################################

  /**
   * The class stores the relation between an select expression and an
   * attribute.
   */
  private class SelExpr2Attr  {

    /**
     * Constructor
     *
     * @param _attr     attribute
     * @param _selExprs select expressions
     */
    private SelExpr2Attr(Attribute _attr, ArrayList<SelectExpression> _selExprs)  {
      setAttribute(_attr);
      setSelExprs(_selExprs);
    }

    /**
     * After execution of the query, the indexes where that attribute values
     * stands, could be initialised and used by {@link #getAttrValue}.
     */
    protected void initSelectIndex()  {
//System.out.println("~~~~~~~~~~~~++initSelectIndex+"+getSelExprs());
      for (SelectExpression selExpr : getSelExprs())  {
        int index = selExpr.getJoinElement().getIncSelIndex() + selExpr.getIndex();
//System.out.println("~~~~~~~~~~~~++index="+index);
        getIndexes().add(new Integer(index));
      }
//System.out.println("~~~~~~~~~~~~++getIndexes()="+getIndexes());
    }

    /**
     * @param _context  eFaps context for this request
     * @return attribute value with the value returned from the select
     *         expression
     */
    protected Object getAttrValue(Context _context) throws Exception  {
      if (getAttribute()==null)  {
throw new EFapsException(getClass(), "SelectExpression.get.NoAttribute");
      }
//System.out.println("~~~~~~~~~~~~++getIndexes()="+getIndexes());
      AttributeTypeInterface ret = getAttribute().newInstance();
return ret.readValue(_context, cachedResult, getIndexes());
//      return ret.readValue(_context, getResultSet(), getIndexes());
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Stores the attribute
     *
     * @see #getAttribute
     * @see #setAttribute
     */
    private Attribute attribute = null;

    /**
     * Stores all select expression.
     *
     * @see #getSelExpr
     * @see #setSelExpr
     */
    private ArrayList<SelectExpression> selExprs = null;

    /**
     * Stores all the indexes of the SQL select expression where the values of
     * the attribute are found (in the same order than defined in the
     * attribute).
     *
     * @see #getIndexes
     */
    private ArrayList<Integer> indexes = new ArrayList<Integer>();

    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is the getter method for instance variable {@link #attribute}.
     *
     * @return value of instance variable {@link #attribute}
     * @see #attribute
     * @see #setAttribute
     */
    public Attribute getAttribute()   {
      return this.attribute;
    }

    /**
     * This is the setter method for instance variable {@link #attribute}.
     *
     * @param _attribute new value for instance variable {@link #attribute}
     * @see #attribute
     * @see #getAttribute
     */
    private void setAttribute(Attribute _attribute)  {
      this.attribute = _attribute;
    }

    /**
     * This is the getter method for instance variable {@link #selExprs}.
     *
     * @return value of instance variable {@link #selExprs}
     * @see #selExprs
     * @see #setSelExprs
     */
    public ArrayList<SelectExpression> getSelExprs()   {
      return this.selExprs;
    }

    /**
     * This is the setter method for instance variable {@link #selExprs}.
     *
     * @param _selExpr new value for instance variable {@link #selExprs}
     * @see #selExprs
     * @see #getSelExprs
     */
    private void setSelExprs(ArrayList<SelectExpression> _selExprs)  {
      this.selExprs = _selExprs;
    }

    /**
     * This is the getter method for instance variable {@link #indexes}.
     *
     * @return value of instance variable {@link #indexes}
     * @see #indexes
     */
    public ArrayList<Integer> getIndexes()   {
      return this.indexes;
    }
  }

  //###########################################################################
  //###########################################################################
  //###########################################################################
  //###########################################################################
  //###########################################################################

  private class SelectExpression  {


    /**
     *
     */
    protected SelectExpression(int _index, String _expression,
        JoinElement _joinElement, SelectType _selectType, String _nullString)  {

      setIndex(_index);
      setExpression(_expression);
      setJoinElement(_joinElement);
      setSelectType(_selectType);
      setNullString(_nullString);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    private int index = 0;

    /**
     *
     */
    private String expression = null;

    /**
     *
     */
    private JoinElement joinElement = null;

    /**
     *
     */
    private SelectType selectType = null;

    /**
     *
     */
    private String nullString = null;

    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is the getter method for instance variable {@link #index}.
     *
     * @return value of instance variable {@link #index}
     * @see #index
     * @see #setIndex
     */
    public int getIndex()   {
      return this.index;
    }

    /**
     * This is the setter method for instance variable {@link #index}.
     *
     * @param _index new value for instance variable {@link #index}
     * @see #index
     * @see #getIndex
     */
    private void setIndex(int _index)  {
      this.index = _index;
    }

    /**
     * This is the getter method for instance variable {@link #expression}.
     *
     * @return value of instance variable {@link #expression}
     * @see #expression
     * @see #setExpression
     */
    public String getExpression()   {
      return this.expression;
    }

    /**
     * This is the setter method for instance variable {@link #expression}.
     *
     * @param _expression new value for instance variable {@link #expression}
     * @see #expression
     * @see #getExpression
     */
    private void setExpression(String _expression)  {
      this.expression = _expression;
    }

    /**
     * This is the getter method for instance variable {@link #joinElement}.
     *
     * @return value of instance variable {@link #joinElement}
     * @see #joinElement
     * @see #setJoinElement
     */
    public JoinElement getJoinElement()   {
      return this.joinElement;
    }

    /**
     * This is the setter method for instance variable {@link #joinElement}.
     *
     * @param _joinElement new value for instance variable {@link #joinElement}
     * @see #joinElement
     * @see #getJoinElement
     */
    private void setJoinElement(JoinElement _joinElement)  {
      this.joinElement = _joinElement;
    }

    /**
     * This is the getter method for instance variable {@link #selectType}.
     *
     * @return value of instance variable {@link #selectType}
     * @see #selectType
     * @see #setSelectType
     */
    public SelectType getSelectType()   {
      return this.selectType;
    }

    /**
     * This is the setter method for instance variable {@link #selectType}.
     *
     * @param _selectType new value for instance variable {@link #selectType}
     * @see #selectType
     * @see #getSelectType
     */
    private void setSelectType(SelectType _selectType)  {
      this.selectType = _selectType;
    }

    /**
     * This is the getter method for instance variable {@link #nullString}.
     *
     * @return value of instance variable {@link #nullString}
     * @see #nullString
     * @see #setNullString
     */
    public String getNullString()   {
      return this.nullString;
    }

    /**
     * This is the setter method for instance variable {@link #nullString}.
     *
     * @param _nullString new value for instance variable {@link #nullString}
     * @see #nullString
     * @see #getNullString
     */
    private void setNullString(String _nullString)  {
      this.nullString = _nullString;
    }
  }

  //###########################################################################
  //###########################################################################
  //###########################################################################
  //###########################################################################
  //###########################################################################

  public class SelectType  {

JoinElement joinElement = null;;

private JoinElement getJoinElement()  {
  return this.joinElement;
}

    private SelectType(JoinElement _joinElement, Type _type, int _typeIndex)  {
this.joinElement = _joinElement;
      setType(_type);
      setTypeIndex(_typeIndex);
      getTypeTableNames().add(getType().getMainTable());
      String expression = getType().getMainTable().getSqlTable() + getTypeIndex() + "." + getType().getMainTable().getSqlColId();
      SelectExpression selectExpr = getJoinElement().getSelectExpression(this, expression, "0");
      setIndexId(selectExpr.getIndex());
getJoinElement().setMatchColumn(selectExpr.getIndex());

if (getType().getMainTable().getSqlColType()!=null)  {
  expression = getType().getMainTable().getSqlTable() + getTypeIndex() + "." + getType().getMainTable().getSqlColType();
  selectExpr = getJoinElement().getSelectExpression(this, expression, "0");
  setIndexType(selectExpr.getIndex());
}
    }

    /**
     * @param _isOID  must be set to <i>true</i> is select expression selects
     *                the OID of the business object.
     * @param _key    key to store the select expression in the select
     *                map expression
     * @param _attr   attribute itself which must be selected
     */
    protected void addSelect(boolean _isOID, Object _key, Attribute _attr)  {
      getJoinElement().addSelectAttribute(getTypeIndex(), this, _isOID, _key, _attr);
//for (String _sqlColName : _attr.getSqlColNames())  {
//      String expression = _attr.getTable().getSqlTable() + getTypeIndex() + "." + _sqlColName;
//      getTypeTableNames().add(_attr.getTable());
//      SelectExpression selectExpr = getJoinElement().getSelectExpression(_isOID, _key, this, expression, "''", _attr);
//}
    }

    /**
     *
     * @param _isOID      must be set to <i>true</i> is select expression
     *                    selects the OID of the business object.
     * @param _key        key to store the select expression in the select
     *                    map expression
     * @param _expression expression itself which must be selected
     */
    protected void addSelect(boolean _isOID, Object _key, String _expression) throws Exception  {
//System.out.println("AbstractQuery.addSelect("+_isOID+","+_key+","+_expression+")");
      if (_expression!=null && _expression.length()>0)  {
        if (_expression.indexOf('.')>=0)  {
          JoinElement elm = new JoinElement();
          StringTokenizer tokens = new StringTokenizer(_expression, ".");
          String link = tokens.nextToken();
          Attribute attr = getType().getAttribute(link);
          if (attr==null)  {
throw new Exception("Link for '"+link+"' does not exists on type '"+getType().getName()+"'");
          }

// add new link type
Type linkType = attr.getLink();

if (linkType==null)  {
  throw new Exception("For Link '"+link+"' of type '" + getType().getName() + "' the type is not defined.");
}

SelectType selectType = elm.getNewSelectType(linkType, true);

Attribute attrFromLink = linkType.getAttribute(tokens.nextToken());

selectType.addSelect(_isOID, _key, attrFromLink);

elm.addWhere(this, attr, selectType, linkType.getAttribute("ID"));

//System.out.println("selectType.index="+selectType.getTypeIndex());

// for ID selection
String expression = getType().getMainTable().getSqlTable() + getTypeIndex()  + "." + getType().getMainTable().getSqlColId();
SelectExpression selectExpr = elm.getSelectExpression(this, expression, "''");
elm.setMatchColumn(selectExpr.getIndex());

getMapJoinElements().put(_expression, elm);

        } else  {
          Attribute attr = getType().getAttribute(_expression);
          if (attr==null)  {
throw new Exception("attribute '"+_expression+"' for type '"+getType().getName()+"' not found");
          }
          addSelect(_isOID, _key, attr);
        }
      }
    }


/**
 * Adds the tables from this attribute in a where clause.
 */
public void add4Where(Attribute _attr)  {
  for (String _sqlColName : _attr.getSqlColNames())  {
    String expression = _attr.getTable().getSqlTable() + getTypeIndex() + "." + _sqlColName;
    getTypeTableNames().add(_attr.getTable());
    SelectExpression selectExpr = getJoinElement().getSelectExpression(this, expression, "''");
  }
}




    protected void appendFrom(CompleteStatement _completeStatement)  {
      for (SQLTable table : getTypeTableNames())  {
        _completeStatement.appendFrom(table.getSqlTable()).append(" ").append(table.getSqlTable()).append(getTypeIndex());
      }
    }


    /**
     * @param _childTypes   also child types are allowed
     */
    protected void appendTypeWhereClause(CompleteStatement _completeStatement, boolean _childTypes)  {
      if (getType().getMainTable().getSqlColType()!=null)  {

if (_childTypes)  {
  _completeStatement.appendWhereAnd();
  _completeStatement.appendWhere(getType().getMainTable().getSqlTable()).appendWhere(getTypeIndex()).appendWhere(".");
  _completeStatement.appendWhere(getType().getMainTable().getSqlColType());
  _completeStatement.appendWhere(" in (");
  _completeStatement.appendWhere(getType().getId());
  for (Type child : getType().getChildTypes())  {
    _completeStatement.appendWhere(",").appendWhere(child.getId());
  }
  _completeStatement.appendWhere(")");
} else  {
        _completeStatement.appendWhereAnd();
        _completeStatement.appendWhere(getType().getMainTable().getSqlTable()).appendWhere(getTypeIndex()).appendWhere(".");
        _completeStatement.appendWhere(getType().getMainTable().getSqlColType());
        _completeStatement.appendWhere("=");
        _completeStatement.appendWhere(getType().getId());
}
      }

      Iterator<SQLTable> iter = getTypeTableNames().iterator();
      SQLTable table = iter.next();
      while (iter.hasNext())  {
        _completeStatement.appendWhereAnd();
        _completeStatement.appendWhere(table.getSqlTable()).appendWhere(getTypeIndex()).appendWhere(".").appendWhere(table.getSqlColId());
        _completeStatement.appendWhere("=");
        SQLTable nextTable = iter.next();
        _completeStatement.appendWhere(nextTable.getSqlTable()).appendWhere(getTypeIndex()).appendWhere(".").appendWhere(nextTable.getSqlColId());
      }
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * The instance variable stores the type this class instance is
     * representing.
     *
     * @see #getType
     * @see #setType
     */
    private Type type = null;

    /**
     * The instance method stores the index of the type id of this type.
     *
     * @see #getIndexId
     * @see #setIndexId
     */
    private Integer indexType = null;

    /**
     * The instance method stores the index of the id of this type.
     *
     * @see #getIndexId
     * @see #setIndexId
     */
    private Integer indexId = null;

    /**
     * The string instance variable stores the table names of the select
     * statement of this selected type.
     *
     * @see #getTableNames
     */
    private Set<SQLTable> typeTableNames = new HashSet<SQLTable>();

    /**
     * The instance variable stores the index of the type in the select
     * expressions of the table.
     *
     * @see #getTypeIndex
     * @see #setTypeIndex
     */
    private int typeIndex = 0;

    /**
     * The instance variable stores the index of the order.
     *
     * @see #getOrderIndex
     * @see #setOrderIndex
     */
    private int orderIndex = 0;

    /**
     * The instance variable stores if the type can be null.
     *
     * @see #isNullAllowed
     * @see #setNullAllowed
     */
    private boolean nullAllowed = false;

    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is the getter method for instance variable {@link #type}.
     *
     * @return value of instance variable {@link #type}
     * @see #type
     * @see #setType
     */
    public Type getType()   {
      return this.type;
    }

    /**
     * This is the setter method for instance variable {@link #type}.
     *
     * @param _type new value for instance variable {@link #type}
     * @see #type
     * @see #getType
     */
    private void setType(Type _type)  {
      this.type = _type;
    }

    /**
     * This is the getter method for instance variable {@link #indexType}.
     *
     * @return value of instance variable {@link #indexType}
     * @see #indexType
     * @see #setIndexType
     */
    protected Integer getIndexType()   {
      return this.indexType;
    }

    /**
     * This is the setter method for instance variable {@link #indexType}.
     *
     * @param _indexType new value for instance variable {@link #indexType}
     * @see #indexType
     * @see #getIndexType
     */
    private void setIndexType(Integer _indexType)  {
      this.indexType = _indexType;
    }

    /**
     * This is the getter method for instance variable {@link #indexId}.
     *
     * @return value of instance variable {@link #indexId}
     * @see #indexId
     * @see #setIndexId
     */
    protected Integer getIndexId()   {
      return this.indexId;
    }

    /**
     * This is the setter method for instance variable {@link #indexId}.
     *
     * @param _indexId new value for instance variable {@link #indexId}
     * @see #indexId
     * @see #getIndexId
     */
    private void setIndexId(Integer _indexId)  {
      this.indexId = _indexId;
    }

    /**
     * This is the getter method for instance variable {@link #typeTableNames}.
     *
     * @return value of instance variable {@link #typeTableNames}
     * @see #typeTableNames
     */
    protected Set<SQLTable> getTypeTableNames()   {
      return this.typeTableNames;
    }

    /**
     * This is the getter method for instance variable {@link #typeIndex}.
     *
     * @return value of instance variable {@link #typeIndex}
     * @see #typeIndex
     * @see #setTypeIndex
     */
    public int getTypeIndex()   {
      return this.typeIndex;
    }

    /**
     * This is the setter method for instance variable {@link #typeIndex}.
     *
     * @param _typeIndex new value for instance variable {@link #typeIndex}
     * @see #typeIndex
     * @see #getTypeIndex
     */
    private void setTypeIndex(int _typeIndex)  {
      this.typeIndex = _typeIndex;
    }

    /**
     * This is the getter method for instance variable {@link #nullAllowed}.
     *
     * @return value of instance variable {@link #nullAllowed}
     * @see #nullAllowed
     * @see #setNullAllowed
     */
    public boolean isNullAllowed()   {
      return this.nullAllowed;
    }

    /**
     * This is the setter method for instance variable {@link #nullAllowed}.
     *
     * @param _nullAllowed new value for instance variable {@link #nullAllowed}
     * @see #nullAllowed
     * @see #isNullAllowed
     */
    private void setNullAllowed(boolean _nullAllowed)  {
      this.nullAllowed = _nullAllowed;
    }

    /**
     * This is the getter method for instance variable {@link #orderIndex}.
     *
     * @return value of instance variable {@link #orderIndex}
     * @see #orderIndex
     * @see #setOrderIndex
     */
    public int getOrderIndex()   {
      return this.orderIndex;
    }

    /**
     * This is the setter method for instance variable {@link #orderIndex}.
     *
     * @param _orderIndex new value for instance variable {@link #orderIndex}
     * @see #orderIndex
     * @see #getOrderIndex
     */
    private void setOrderIndex(int _orderIndex)  {
      this.orderIndex = _orderIndex;
    }
  }
}