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

package org.efaps.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.query.OneRoundQuery;
import org.efaps.util.EFapsException;

/**
 * @todo description
 * @author tmo
 * @version $Id$
 */
public class ListQuery extends AbstractQuery {

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Stores all instances for which this query is executed.
   */
  private final List<Instance> instances;

  /**
   * Stores all select statements for this query.
   */
  private final Set<String> selects = new HashSet<String>();

  private final Map<String, ListQuery> subSelects =
      new HashMap<String, ListQuery>();

  private OneRoundQuery query = null;

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / desctructors

  /**
   * @param _instances
   *                list of instances for which this query is executed
   */
  public ListQuery(final List<Instance> _instances) {
    this.instances = _instances;
  }

  private ListQuery() {
    this.instances = new ArrayList<Instance>();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  @Override
  public void execute() throws EFapsException {
    try {
      if (this.instances.size() > 0) {
        this.query = new OneRoundQuery(this.instances, this.selects);
        this.query.execute();
        for (Map.Entry<String, ListQuery> sub : this.subSelects.entrySet()) {
          while (this.query.next()) {
            Attribute attr;
            attr = this.query.getAttribute(sub.getKey());
            if ((attr != null) && (attr.getLink() != null)) {
              if (this.query.getValue(sub.getKey()) != null) {
                Long id =
                    ((Number) this.query.getValue(sub.getKey())).longValue();
                if ((id != null) && (id != 0)) {
                  sub.getValue().addInstance(attr.getLink(), id);
                }
              }
            }
          }
          this.query.beforeFirst();
          sub.getValue().execute();
        }
      }
    } catch (Exception e) {
      throw (new EFapsException(this.getClass(), "execute", e));
    }
  }

  private boolean gotoKey(final Object _key) {
    return this.query.gotoKey(_key);
  }

  /**
   * Adds one select statement to this query.
   *
   * @param _select
   *                select statement to add
   * @see #selects
   */
  @Override
  public void addSelect(final String _select) {
    // System.out.println("_select=" + _select);
    final int idx = _select.indexOf(".");
    if (idx > 0) {
      // differ select expression from sub expression
      String select = _select.substring(0, idx);
      String subSel = _select.substring(idx + 1);
      this.selects.add(select);
      // make the subquery depending on the select statement
      ListQuery subQuery = this.subSelects.get(select);
      if (subQuery == null) {
        subQuery = new ListQuery();
        this.subSelects.put(select, subQuery);
      }
      subQuery.addSelect(subSel);
    } else {
      this.selects.add(_select);
    }
  }

  private void addInstance(final Type _type, final long _id) {
    this.instances.add(new Instance(_type, _id));
  }

  /**
   * @return <i>true</i> if a new row is selected and exists, otherwise
   *         <i>false</i>
   */
  @Override
  public boolean next() {
    return (this.query != null) ? this.query.next() : false;
  }

  /**
   * The instance method returns for the given key the attribute value.
   *
   * @param _key
   *                key for which the attribute value must returned
   * @return atribute value for given key
   */
  @Override
  public Object get(final String _select) throws EFapsException {
    try {
      final int idx = _select.indexOf(".");
      Object ret = null;
      if (idx > 0) {
        // differ select expression from sub expression
        String select = _select.substring(0, idx);
        String subSel = _select.substring(idx + 1);
        // evalute sub select expression for given id
        ListQuery subQuery = this.subSelects.get(select);
        if (subQuery.gotoKey(this.query.getValue(select))) {
          ret = subQuery.get(subSel);
        }
      } else {
        ret = this.query.getValue(_select);
      }
      return ret;
    } catch (Exception e) {
      throw (new EFapsException(this.getClass(), "get", e));
    }

  }

  public Type getType() throws Exception {
    return this.query.getType();
  }

  public Instance getInstance() throws Exception {
    return this.query.getInstance();
  }

  /**
   * The instance method returns for the given key the atribute.
   *
   * @param _key
   *                key for which the attribute value must returned
   * @return attribute for given key
   */
  @Override
  public Attribute getAttribute(final String _select) throws Exception {
    final int idx = _select.indexOf(".");
    Attribute ret = null;
    if (idx > 0) {
      // differ select expression from sub expression
      String select = _select.substring(0, idx);
      String subSel = _select.substring(idx + 1);
      // evalute sub select expression for given id
      ListQuery subQuery = this.subSelects.get(select);
      if (subQuery.gotoKey(this.query.getValue(select))) {
        ret = subQuery.getAttribute(subSel);
      }
    } else {
      ret = this.query.getAttribute(_select);
    }
    return ret;
  }

  /**
   * Returns a string representation of this .
   *
   * @return string representation of this
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).appendSuper(super.toString()).append(
        "selects", this.selects.toString()).append("subSelects",
        this.subSelects.toString()).toString();
  }
}
