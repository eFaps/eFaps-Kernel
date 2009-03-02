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

package org.efaps.importer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * This class presents an Object which is connected to an
 * <code>InsertObject</code> through an ID.
 *
 *
 * @author jmox
 * @version $Id$
 */
public class ForeignObject {

  /**
   * Logger for this class.
   */
  private static final Logger LOG
                                = LoggerFactory.getLogger(ForeignObject.class);

  /**
   * Contains the name of the attribute which links to an InsertObject.
   */
  private String linkattribute = null;

  /**
   * Contains the type of this ForeignObject.
   */
  private String type = null;

  /**
   * Contains the attributes and the values used for the Query.
   */
  private final Map<String, String> attributes = new HashMap<String, String>();

  /**
   * The attribute to be selected. Default "ID".
   */
  private String select;

  /**
   * Adds an Attribute, which will be used to construct the Query.
   *
   * @param _name   Name of the attribute
   * @param _value  Value of the attribute
   */
  public void addAttribute(final String _name, final String _value) {
    this.attributes.put(_name, _value.trim());
  }

  /**
   * Sets the LinkAttribute and the Type of the ForeignObject.
   *
   * @param _name   Name of the LinkAttribute
   * @param _type   Type of the ForeignObject
   * @param _select name of the field to be selected, default "ID"
   */
  public void setLinkAttribute(final String _name, final String _type,
                               final String _select) {
    this.linkattribute = _name;
    this.type = _type;
    this.select = _select != null ? _select : "ID";
  }

  /**
   * Returns the LinkAttribute of this ForeignObject.
   *
   * @return String containing the Name of the LinkAttribute
   */
  public String getLinkAttribute() {

    return this.linkattribute;
  }

  /**
   * Method to get the ID of the ForeignObject. <br>
   * <br>
   * To get the ID a Query is build. If the Query returns Null, then it will be
   * controled if a default is defined for this ForeignObject.If is so the
   * default is returned, otherwise null.
   *
   * @return String with the ID of the ForeignObject. Null if not found and no
   *         default is defined.
   */
  public String dbGetValue() {
    final SearchQuery query = new SearchQuery();
    String value = null;
    try {

      query.setQueryTypes(this.type);
      query.addSelect(this.select);
      query.setExpandChildTypes(true);

      for (final Entry<String, String> element : this.attributes.entrySet()) {
        query.addWhereExprEqValue(element.getKey().toString(), element
            .getValue().toString());
      }
      query.executeWithoutAccessCheck();
      if (query.next()) {
        value = query.get(this.select).toString();
      } else {
        value = DefaultObject.getDefault(this.type, this.linkattribute);

        if (value != null) {
          LOG.debug("Query did not return a Value; set Value to Defaultvalue: "
              + value);
        } else {
          LOG.error("the Search for a ForeignObject did return no Result!: - "
              + toString());
        }
      }

      query.close();
      return value;
    } catch (final EFapsException e) {
      LOG.error("getID()", e);
    }
    return null;
  }

  /**
   * (non-Javadoc).
   *
   * @see java.lang.Object#toString()
   * @return String representation of this class
   */
  @Override
  public String toString() {

    final StringBuilder tmp = new StringBuilder();
    tmp.append("Type: ");
    tmp.append(this.type);
    tmp.append(" - Attribute: ");
    tmp.append(this.linkattribute);
    tmp.append(" - Attributes: ");
    tmp.append(this.attributes.toString());
    return tmp.toString();
  }
}
