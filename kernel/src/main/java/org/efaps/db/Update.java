/*
 * Copyright 2003 - 2009 The eFaps Team
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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public class Update {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  private final static Status STATUSOK = new Status();

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Update.class);

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The instance variable stores the instance for which this update is made.
   *
   * @see #getInstance
   * @see #setInstance
   */
  private Instance instance = null;

  /**
   * The string instance variable stores the table names of the select
   * statement.
   *
   * @see #getExpr4Tables
   */
  private final Map<SQLTable, Map<String, AttributeTypeInterface>> expr4Tables =
      new Hashtable<SQLTable, Map<String, AttributeTypeInterface>>();

  private final Map<String, AttributeTypeInterface> mapAttr2Value =
      new HashMap<String, AttributeTypeInterface>();

  protected final Map<Attribute, Object> values =
      new HashMap<Attribute, Object>();

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  /**
   */
  public Update(final Type _type, final String _id) throws EFapsException {
    this(new Instance(_type, _id));

  }

  public Update(final String _type, final String _id) throws EFapsException {
    this(Type.get(_type),_id);
  }
  /**
   */
  public Update(final String _oid) throws EFapsException {
    this(new Instance(_oid));
  }

  /**
   *
   */
  public Update(final Instance _instance) throws EFapsException {
    setInstance(_instance);
    addAlwaysUpdateAttributes();
    if (!STATUSOK.getStati().isEmpty()) {
      STATUSOK.getStati().clear();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Add all attributes of the type which must be always updated.
   *
   * @param _type
   *                data model type
   */
  protected void addAlwaysUpdateAttributes() throws EFapsException {
    final Iterator<?> iter =
        getInstance().getType().getAttributes().entrySet().iterator();
    while (iter.hasNext()) {
      final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
      final Attribute attr = (Attribute) entry.getValue();
      final AttributeType attrType = attr.getAttributeType();
      if (attrType.isAlwaysUpdate()) {
        add(attr, null, false);
      }
    }
  }

  /**
   * The method closes the SQL statement.
   *
   * @see #statement
   */
  public void close() throws EFapsException {
    /*
     * if (getStatement()!=null) { try { getStatement().close(); } catch
     * (Exception e) { throw e; } finally { setStatement(null); } }
     */
  }

  /**
   * The method gets all events for the given EventType and executes them in the
   * given order. If no events are defined, nothing is done. The method return
   * TRUE if a event was found, otherwise FALSE.
   *
   * @param _context
   *                eFaps context for this request
   * @param eventtype
   *                trigger events to execute
   * @return true if a trigger was found and executed, otherwise false
   * @throws EFapsException
   */
  protected boolean executeEvents(final EventType eventtype)
                                                            throws EFapsException {
    final List<EventDefinition> triggers =
        getInstance().getType().getEvents(eventtype);
    if (triggers != null) {
      final Parameter parameter = new Parameter();
      parameter.put(ParameterValues.NEW_VALUES, this.values);
      parameter.put(ParameterValues.INSTANCE, getInstance());
      for (final EventDefinition evenDef : triggers) {
        evenDef.execute(parameter);
      }
      return true;
    }
    return false;
  }

  /**
   * @param _attr
   *                name of attribute to update
   * @param _value
   *                attribute value
   */
  public Status add(final String _attr, final String _value)
                                                            throws EFapsException {
    final Attribute attr = getInstance().getType().getAttribute(_attr);
    if (attr == null) {
      throw new EFapsException(getClass(), "add.UnknownAttributeName");
    }
    return add(attr, _value, true);
  }

  /**
   * @param _attr
   *                attribute to update
   * @param _value
   *                new attribute value
   */
  public Status add(final Attribute _attr, final String _value)
                                                               throws EFapsException {
    return add(_attr, _value, true);
  }

  /**
   * @param _attr
   *                attribute to update
   * @param _value
   *                new attribute value
   * @param _triggerRelevant
   */
  public Status add(final Attribute _attr, final String _value,
                    final boolean _triggerRelevant) throws EFapsException {
    return add(_attr, (Object) _value, _triggerRelevant);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // timestamp methods

  public Status add(final String _attr, final DateTime _value)
                                                               throws EFapsException {
    final Attribute attr = getInstance().getType().getAttribute(_attr);
    if (attr == null) {
      throw new EFapsException(getClass(), "add.UnknownAttributeName");
    }
    return add(attr, _value, true);
  }

  /**
   * @param _attr
   *                attribute to update
   * @param _value
   *                new attribute value
   */
  public Status add(final Attribute _attr, final DateTime _value)
                                                                  throws EFapsException {
    return add(_attr, _value, true);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // private common method

  private Status add(final Attribute _attr, final Object _value,
                     final boolean _triggerRelevant) throws EFapsException {

    if (_attr.hasEvents(EventType.VALIDATE)) {
      final List<Return> returns =
          _attr.executeEvents(EventType.VALIDATE, ParameterValues.NEW_VALUES,
              _value);
      for (final Return ret : returns) {
        if ((ret.get(ReturnValues.TRUE) == null)) {
          return new Status(ret.get(ReturnValues.VALUES), _attr, _value);
        }
      }

    }

    Map<String, AttributeTypeInterface> expressions =
        getExpr4Tables().get(_attr.getTable());

    if (expressions == null) {
      expressions = new HashMap<String, AttributeTypeInterface>();
      getExpr4Tables().put(_attr.getTable(), expressions);
    }

    final AttributeTypeInterface attrType = _attr.newInstance();
    attrType.setAttribute(_attr);
    attrType.set(_value);
    // TODO: was, wenn ein attribute mehr als ein SQL Column hat?
    // expressions.put(_attr.getSqlColName(), attrType);
    expressions.put(_attr.getSqlColNames().get(0), attrType);

    // merke object(Wert), merke attribute

    this.mapAttr2Value.put(_attr.getName(), attrType);

    if (_triggerRelevant) {
      this.values.put(_attr, _value);
    }
    return STATUSOK;
  }

  protected boolean test4Unique(final Context _context) throws EFapsException {
    return test4Unique(_context, getType());
  }

  private boolean test4Unique(final Context _context, final Type _type)
                                                                       throws EFapsException {
    boolean ret = false;

    if (_type.getUniqueKeys() != null) {
      for (final org.efaps.admin.datamodel.UniqueKey uk : _type.getUniqueKeys()) {

        final SearchQuery query = new SearchQuery();
        query.setQueryTypes(_type.getName());
        query.setExpandChildTypes(true);

        boolean testNeeded = false;
        for (final Attribute attr : uk.getAttributes()) {
          final AttributeTypeInterface value =
              this.mapAttr2Value.get(attr.getName());
          if (value != null) {
            query.addWhereAttrEqValue(attr, value.toString());
            testNeeded = true;
          }
        }
        if (testNeeded) {
          query.addSelect("ID");
          query.executeWithoutAccessCheck();

          while (query.next()) {
            final long id = (Long) query.get("ID");
            if (id != getInstance().getId()) {
              ret = true;
              break;
            }
          }
        }
      }
    }
    if (_type.getParentType() != null && ret == false) {
      ret = test4Unique(_context, _type.getParentType());
    }
    return ret;
  }

  /**
   * @throws EFapsException
   *                 thrown from {@link #executeWithoutAccessCheck}
   * @see #executeWithoutAccessCheck
   */
  public void execute() throws EFapsException {
    final boolean hasAccess =
        getType().hasAccess(getInstance(),
            AccessTypeEnums.MODIFY.getAccessType());

    if (!hasAccess) {
      throw new EFapsException(getClass(), "execute.NoAccess");
    }
    executeWithoutAccessCheck();
  }

  /**
   * Executes the update without checking the access rights (but with triggers):
   * <ol>
   * <li>executes the pre update trigger (if exists)</li>
   * <li>executes the override trigger (if exists)</li>
   * <li>executes if no override trigger exists or the override trigger is not
   * executed the update ({@see #executeWithoutTrigger})</li>
   * <li>executes the post update trigger (if exists)</li>
   * </ol>
   *
   * @throws EFapsException
   *                 thrown from {@link #executeWithoutTrigger} or when the
   *                 Status is invalid
   * @see #executeWithoutTrigger
   */
  public void executeWithoutAccessCheck() throws EFapsException {
    if (STATUSOK.getStati().isEmpty()) {

      executeEvents(EventType.UPDATE_PRE);

      if (!executeEvents(EventType.UPDATE_OVERRIDE)) {
        executeWithoutTrigger();
      }

      executeEvents(EventType.UPDATE_POST);
    } else {
      throw new EFapsException(getClass(), "executeWithout.StatusInvalid",
          STATUSOK.getStati());
    }
  }

  /**
   * The update is done without calling triggers and check of access rights.
   *
   * @throws EFapsException
   *                 if update not possible (unique key, object does not exists,
   *                 etc...)
   */
  public void executeWithoutTrigger() throws EFapsException {
    if (STATUSOK.getStati().isEmpty()) {

      final Context context = Context.getThreadContext();
      ConnectionResource con = null;
      try {
        con = context.getConnectionResource();

        if (test4Unique(context)) {
          throw new EFapsException(getClass(),
              "executeWithoutTrigger.UniqueKeyError");
        }

        for (final Map.Entry<SQLTable, Map<String, AttributeTypeInterface>> entry : getExpr4Tables()
            .entrySet()) {
          final SQLTable table = entry.getKey();
          final Map<?, ?> expressions = entry.getValue();

          PreparedStatement stmt = null;
          try {
            stmt = createOneStatement(con, table, expressions);
            final int rows = stmt.executeUpdate();
            if (rows == 0) {
              throw new EFapsException(getClass(),
                  "executeWithoutTrigger.ObjectDoesNotExists", this.instance);
            }
          }
          finally {
            stmt.close();
          }
        }
        con.commit();
      } catch (final SQLException e) {
        LOG.error("Update of '" + this.instance + "' not possible", e);
        throw new EFapsException(getClass(),
            "executeWithoutTrigger.SQLException", e, this.instance);
      }
      finally {
        if ((con != null) && con.isOpened()) {
          con.abort();
        }
      }
    } else {
      throw new EFapsException(getClass(), "executeWithout.StatusInvalid",
          STATUSOK.getStati());
    }

  }

  private PreparedStatement createOneStatement(final ConnectionResource _con,
                                               final SQLTable _table,
                                               final Map<?, ?> _expressions)
                                                                            throws SQLException,
                                                                            EFapsException {
    final List<AttributeTypeInterface> list =
        new ArrayList<AttributeTypeInterface>();
    final StringBuilder cmd = new StringBuilder();
    cmd.append("update ").append(_table.getSqlTable()).append(" set ");
    final Iterator<?> iter = _expressions.entrySet().iterator();
    boolean command = false;
    while (iter.hasNext()) {
      final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();

      if (command) {
        cmd.append(",");
      } else {
        command = true;
      }
      cmd.append(entry.getKey()).append("=");

      final AttributeTypeInterface attr =
          (AttributeTypeInterface) entry.getValue();
      if (!attr.prepareUpdate(cmd)) {
        list.add(attr);
      }
    }
    cmd.append(" where ").append(_table.getSqlColId()).append("=").append(
        getId()).append("");

    if (LOG.isTraceEnabled()) {
      LOG.trace(cmd.toString());
    }

    final PreparedStatement stmt =
        _con.getConnection().prepareStatement(cmd.toString());
    for (int i = 0, j = 1; i < list.size(); i++, j++) {
      final AttributeTypeInterface attr = list.get(i);
      if (LOG.isTraceEnabled()) {
        LOG.trace(attr.toString());
      }

      final List<Integer> x = new ArrayList<Integer>();
      x.add(j);
      attr.update(null, stmt, x);
    }
    return stmt;
  }

  /**
   * The instance method returns the Type instance of {@link #instance}.
   *
   * @return type of {@link #instance}
   * @see #instance
   */
  protected Type getType() {
    return getInstance().getType();
  }

  /**
   * The instance method returns the id of {@link #instance}.
   *
   * @return id of {@link #instance}
   * @see #instance
   */
  public String getId() {
    return "" + getInstance().getId();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // getter / setter methods

  /**
   * This is the getter method for instance variable {@link #instance}.
   *
   * @return value of instance variable {@link #instance}
   * @see #instance
   * @see #setInstance
   */
  public Instance getInstance() {
    return this.instance;
  }

  /**
   * This is the setter method for instance variable {@link #instance}.
   *
   * @param _out
   *                new value for instance variable {@link #instance}
   * @see #instance
   * @see #getInstance
   */
  protected void setInstance(final Instance _instance) {
    this.instance = _instance;
  }

  /**
   * This is the getter method for instance variable {@link #tableNames}.
   *
   * @return value of instance variable {@link #tableNames}
   * @see #tableNames
   * @see #setTableNames
   */
  protected Map<SQLTable, Map<String, AttributeTypeInterface>> getExpr4Tables() {
    return this.expr4Tables;
  }

  public static class Status {

    /**
     * this instance varaiable is only used in static Status STATUSOK.<br>
     * It stores all Instances of Status wich are not ok.
     *
     * @see #getStati()
     */
    private final List<Status> stati = new ArrayList<Status>();

    /**
     * this instance variable stores the ReturnValue of the esjp
     *
     * @see #getReturnValue()
     */
    private final Object returnValue;

    /**
     * this instance variable stores the Value wich was thought for the
     * Attribute
     *
     * @see #getAttribute()
     */
    private final Object value;

    /**
     * this instance variable stores the Attribute wich led to the creation of
     * this Status
     */
    private final Attribute attribute;

    /**
     * Constructor setting the instance variables and stores this Status in
     * STATUSOK.
     *
     * @param _returnvalue
     * @param _attribute
     * @param _value
     */
    public Status(final Object _returnvalue, final Attribute _attribute,
                  final Object _value) {
      this.returnValue = _returnvalue;
      this.value = _value;
      this.attribute = _attribute;
      STATUSOK.getStati().add(this);
    }

    public Status() {
      this.returnValue = null;
      this.value = null;
      this.attribute = null;
    }

    /**
     * This method can be called to see if the Status is Ok
     *
     * @return true if ok, else false
     */
    public boolean isOk() {
      if (this.equals(STATUSOK)) {
        return true;
      } else {
        return false;
      }
    }

    /**
     * This is the getter method for the instance variable {@link #returnValue}.
     *
     * @return value of instance variable {@link #returnValue}
     */

    public Object getReturnValue() {
      return this.returnValue;
    }

    /**
     * This is the getter method for the instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     */

    public Object getValue() {
      return this.value;
    }

    /**
     * This is the getter method for the instance variable {@link #attribute}.
     *
     * @return value of instance variable {@link #attribute}
     */

    public Attribute getAttribute() {
      return this.attribute;
    }

    /**
     * This is the getter method for the instance variable {@link #stati}.
     *
     * @return value of instance variable {@link #stati}
     */

    public List<Status> getStati() {
      return this.stati;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return new ToStringBuilder(this).append("AttributeName",
          getAttribute().getName()).append(" Value", getValue()).append(
          " ReturnValue:", getReturnValue()).toString();
    }

  }

}
