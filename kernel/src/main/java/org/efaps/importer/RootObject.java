/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Insert;
import org.efaps.util.EFapsException;

/**
 * This Class represents a simplified an specialized version of an InsertObject.
 * In this case the Object represents the Root and therefore can't be a child.
 * The Root means in this case the &lt;import&gt;&lt;/import&gt; of the
 * XML-File.
 *
 * @author jmox
 * @version $Id$
 */
public class RootObject extends AbstractObject {

  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(RootObject.class);

  final List<AbstractObject> CHILDS = new ArrayList<AbstractObject>();

  static String DATEFORMAT = null;

  final static Map<String, OrderObject> ORDER =
      new HashMap<String, OrderObject>();

  public void setDateFormat(final String _DateFormat) {
    DATEFORMAT = _DateFormat;
  }

  public void addOrder(final OrderObject _order) {

    ORDER.put(_order.getType(), _order);
  }

  public static OrderObject getOrder(final String _type) {
    return ORDER.get(_type);
  }

  @Override
  public Map<String, Object> getAttributes() {
    // not needed here
    return null;
  }

  @Override
  public String getType() {
    // not needed here
    return null;
  }

  @Override
  public void setID(final String _ID) {
    // not needed here
  }

  public void addChild(final AbstractObject _Object) {
    this.CHILDS.add(_Object);
  }

  @Override
  public void dbAddChilds() {
    for (final AbstractObject object : this.CHILDS) {
      try {
        if (LOG.isInfoEnabled()) {
          LOG.info("Inserting the Base-Objects '"
              + object.getType()
              + "' to the Database");
        }
        final Insert insert = new Insert(object.getType());

        for (final Entry<String, Object> element : object.getAttributes()
            .entrySet()) {
          if (element.getValue() instanceof DateTime) {
            insert.add(element.getKey().toString(), (DateTime) element
                .getValue());

          } else {
            insert.add(element.getKey().toString(), element.getValue()
                .toString());
          }
        }
        for (final ForeignObject link : object.getLinks()) {
          insert.add(link.getLinkAttribute(), link.dbGetID());
        }
        insert.executeWithoutAccessCheck();
        final String ID = insert.getId();
        insert.close();

        object.setID(ID);

      } catch (final EFapsException e) {
        LOG.error("insertDB()", e);
      } catch (final Exception e) {
        LOG.error("insertDB()", e);
      }

    }

    for (final AbstractObject object : this.CHILDS) {
      object.dbAddChilds();
    }

  }

  @Override
  public String getParrentAttribute() {
    // not needed here
    return null;
  }

  @Override
  public Set<ForeignObject> getLinks() {
    // not needed here
    return null;
  }

  @Override
  public boolean isCheckinObject() {
    return false;
  }

  @Override
  public void dbCheckObjectIn() {
    // not needed here
  }

  @Override
  public Set<String> getUniqueAttributes() {
    // not needed here
    return null;
  }

  @Override
  public Object getAttribute(final String _attribute) {
    // not needed here
    return null;
  }

  @Override
  public boolean hasChilds() {
    // not needed here
    return false;
  }

  @Override
  public String dbUpdateOrInsert(final AbstractObject _parent, final String _ID) {
    // not needed here
    return null;
  }

  @Override
  public String getID() {
    // not needed here
    return null;
  }

}
