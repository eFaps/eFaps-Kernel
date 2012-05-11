/*
 * Copyright 2003 - 2012 The eFaps Team
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
 * <p>This Class gives the possibility to specify an order for objects, in
 * which they will be inserted into the Database.</p>
 * <p><b>Example:</b><br>
 * <pre>
 * &lt;order type="YDSS_DocumentVersion" direction="ascending"&gt;
 *      &lt;attribute index="1" name="Version" criteria="numerical"/&gt;
 * &lt;/order&gt;
 * </pre></p>
 *
 * @author The eFaps Team
 * @version $Id$
 *
 */
public class OrderObject
    implements Comparator<AbstractObject>
{
    /**
     * Contains the type of this order object.
     */
    private final String type;

    /**
     * Contains the direction of the order, <i>true</i> if ascending,
     * <i>false</i> if descending.
     */
    private final boolean ascending;

    /**
     * Contains the order attributes of this order object.
     */
    private final Map<Integer, OrderAttribute> orderAttributes = new TreeMap<Integer, OrderAttribute>();

    /**
     * Default constructor.
     */
    public OrderObject()
    {
        this.type = null;
        this.ascending = true;
    }

    /**
     * Constructor for an order object.
     *
     * @param _type         type of the order object
     * @param _direction    direction of the order, if it equals "descending"
     *                      the value of this.ascending will be set to
     *                      <i>false</i>, in all other cases to <i>true</i>
     */
    public OrderObject(final String _type,
                       final String _direction)
    {
        this.type = _type;
        this.ascending = !"descending".equalsIgnoreCase(_direction);
    }

    /**
     * Return the type of this order object.
     *
     * @return type of this order object
     * @see #type
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * Adds an order attribute to this order object.
     *
     * @param _index    index of the order attribute
     * @param _name     name of the order attribute
     * @param _criteria criteria for comparing
     */
    public void addAttribute(final Integer _index,
                             final String _name,
                             final String _criteria)
    {
        this.orderAttributes.put(_index, new OrderAttribute(_name, _criteria));
    }

    /**
     * @param _arg0     first argument
     * @param _arg1     second argument
     * @return result of compare
     */
    public int compare(final AbstractObject _arg0,
                       final AbstractObject _arg1)
    {
// TODO 1 ersetzen
        int returnValue;

        if (this.orderAttributes.get(1).getCriteria().equalsIgnoreCase("numerical")) {
            final Long comp1 = Long.parseLong((String) _arg0.getAttribute(this.orderAttributes.get(1).getName()));
            final Long comp2 = Long.parseLong((String) _arg1.getAttribute(this.orderAttributes.get(1).getName()));

            if (comp1 > comp2)  {
                returnValue = 1;
            } else if (comp1 < comp2)  {
                returnValue = -1;
            } else  {
                returnValue = 0;
            }
        } else {
            final String comp1 = (String) _arg0.getAttribute(this.orderAttributes.get(1).getName());
            final String comp2 = (String) _arg1.getAttribute(this.orderAttributes.get(1).getName());

            returnValue = comp1.compareToIgnoreCase(comp2);
        }

        if (!this.ascending) {
            returnValue *= -1;
        }
        return returnValue;
    }

    /**
     * Used to store an order attribute.
     */
    private static class OrderAttribute
    {
        /**
         * Contains the name of the attribute.
         */
        private final String name;

        /**
         * Defines the criteria  how this attribute will be ordered, (e.g.
         * "numerical").
         */
        private final String criteria;

        /**
         *
         * @param _name         name of the order attribute
         * @param _criteria     order criteria
         */
        public OrderAttribute(final String _name,
                              final String _criteria)
        {
            this.name = _name;
            this.criteria = _criteria;
        }

        /**
         * Returns the name of this order attribute.
         *
         * @return name of this order attribute
         * @see #name
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * Returns the criteria for this order attribute.
         *
         * @return criteria
         * @see #criteria
         */
        public String getCriteria()
        {
            return this.criteria;
        }
    }
}
