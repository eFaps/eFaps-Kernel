/*
 * Copyright 2003 - 2007 The eFaps Team
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

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * This Class gives the Possibility to specify a Order for Objects, in wich they
 * will be inserted into the Database<br>
 * <br>
 * Example:<br>
 * &lt;order type="YDSS_DocumentVersion" direction="ascending"&gt;<br>
 * &lt;attribute index="1" name="Version" criteria="numerical"/&gt; <br>
 * &lt;/order&gt;
 * 
 * @author jmo
 * 
 */
public class OrderObject implements Comparator<AbstractObject> {

  /**
   * contains the Type of the OrderObject
   */
  private String                             type;

  /**
   * contains the direction of the order, true if ascending, false if descending
   */
  private boolean                            ascending;

  /**
   * contains the OrderAttribbutes of ths OrderObject
   */
  private final Map<Integer, OrderAttribute> orderAttributes = new TreeMap<Integer, OrderAttribute>();

  /**
   * default-Constructor
   */
  public OrderObject() {
    this.type = null;
    this.ascending = true;
  }

  /**
   * Constructof for an OrderObject
   * 
   * @param _type
   *          Type of the OrderObject
   * @param _direction
   *          Direction of the Order, if it equals "descending" the value of
   *          this.ascending will be set to false, in all other cases to true
   */
  public OrderObject(final String _type, final String _direction) {
    this.type = _type;
    this.ascending = !"descending".equalsIgnoreCase(_direction);
  }

  /**
   * get the Type of this OrderObject
   * 
   * @return Type of the OrderObject
   */
  public String getType() {
    return this.type;
  }

  /**
   * Method to add a OrderAttribute to this OrderObject
   * 
   * @param _index
   *          Index of the OrderAttribute
   * @param _name
   *          Name of the OrderAttribute
   * @param _criteria
   *          Criteria for comparing
   */
  public void addAttribute(final Integer _index, final String _name,
                           final String _criteria) {
    this.orderAttributes.put(_index, new OrderAttribute(_name, _criteria));
  }

  public int compare(final AbstractObject _arg0, final AbstractObject _arg1) {
    // TODO 1 ersetzen
    int returnValue;

    if (this.orderAttributes.get(1).getCriteria().equalsIgnoreCase("numerical")) {
      Long comp1 = Long.parseLong((String) _arg0
          .getAttribute(this.orderAttributes.get(1).getName()));
      Long comp2 = Long.parseLong((String) _arg1
          .getAttribute(this.orderAttributes.get(1).getName()));

      if (comp1 > comp2)

        returnValue = 1;

      else if (comp1 < comp2)

        returnValue = -1;

      else

        returnValue = 0;

    } else {

      String comp1 = (String) _arg0.getAttribute(this.orderAttributes.get(1)
          .getName());
      String comp2 = (String) _arg1.getAttribute(this.orderAttributes.get(1)
          .getName());

      returnValue = comp1.compareToIgnoreCase(comp2);
    }

    if (!this.ascending) {
      switch (returnValue) {
      case 1:
        returnValue = -1;
        break;
      case -1:
        returnValue = 1;
        break;
      }
    }

    return returnValue;
  }

  /**
   * private Class to store a OrderAttribute
   * 
   * @author jmo
   * 
   */
  private class OrderAttribute {
    /**
     * Contains the Name of the Attribute
     */
    private String name;

    /**
     * Criteria this Attribute will be ordered, like "numerical"
     */
    private String criteria;

    public OrderAttribute(final String _name, final String _criteria) {
      this.name = _name;
      this.criteria = _criteria;
    }

    /**
     * get the Name of this OrderAttribute
     * 
     * @return String with the Name of the Attribute
     */
    public String getName() {
      return this.name;
    }

    /**
     * get the Criteria for this OrderAttribute
     * 
     * @return
     */
    public String getCriteria() {
      return this.criteria;
    }
  }

}
