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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.efaps.db.Insert;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class represents a simplified an specialized version of an InsertObject.
 * In this case the Object represents the Root and therefore can't be a child.
 * The Root means in this case the &lt;import&gt;&lt;/import&gt; of the
 * XML-File.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class RootObject
    extends AbstractObject
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RootObject.class);

    private final static Map<String, OrderObject> ORDER = new HashMap<String, OrderObject>();

    //default is yyyy-MM-dd'T'HH:mm:ss.SSSZZ
    static String DATEFORMAT;

    private final List<AbstractObject> childs = new ArrayList<AbstractObject>();


    public static void setDateFormat(final String _DateFormat)
    {
        RootObject.DATEFORMAT = _DateFormat;
    }

    public static void addOrder(final OrderObject _order)
    {
        RootObject.ORDER.put(_order.getType(), _order);
    }

    public static OrderObject getOrder(final String _type)
    {
        return ORDER.get(_type);
    }


    public void addChild(final AbstractObject _object)
    {
        this.childs.add(_object);
    }

    @Override()
    public void dbAddChilds()
    {
        for (final AbstractObject object : this.childs) {
            try {
                if (RootObject.LOG.isInfoEnabled()) {
                    RootObject.LOG.info("Inserting the Base-Objects '"
                            + object.getType() + "' to the Database");
                }
                final Insert insert = new Insert(object.getType());

                for (final Entry<String, Object> element : object.getAttributes().entrySet()) {
                    if (element.getValue() instanceof DateTime) {
                        insert.add(element.getKey().toString(), (DateTime) element.getValue());
                    } else {
                        insert.add(element.getKey().toString(), element.getValue().toString());
                    }
                }
                for (final ForeignObject link : object.getLinks()) {
                    insert.add(link.getLinkAttribute(), link.dbGetValue());
                }
                insert.executeWithoutAccessCheck();
                final String newId = insert.getId();
                insert.close();

                object.setID(newId);

            } catch (final EFapsException e) {
                RootObject.LOG.error("insertDB()", e);
            } catch (final Exception e) {
                RootObject.LOG.error("insertDB()", e);
            }
        }

        for (final AbstractObject object : this.childs) {
            object.dbAddChilds();
        }
    }

    /**
     * The method is not required for root objects and therefore the method is
     * only a stub method.
     *
     * @return always <code>null</code>
     */
    @Override()
    public Map<String, Object> getAttributes()
    {
        return null;
    }

    /**
     * The method is not required for root objects and therefore the method is
     * only a stub method.
     *
     * @return always <code>null</code>
     */
    @Override()
    public String getType()
    {
        return null;
    }

    /**
     * The method is not required for root objects and therefore the method is
     * only a stub method.
     *
     * @param _id       not used
     */
    @Override()
    public void setID(final String _id)
    {
    }

    /**
     * The method is not required for root objects and therefore the method is
     * only a stub method.
     *
     * @return always <code>null</code>
     */
    @Override()
    public String getParrentAttribute()
    {
        return null;
    }

    /**
     * The method is not required for root objects and therefore the method is
     * only a stub method.
     *
     * @return always <code>null</code>
     */
    @Override()
    public Set<ForeignObject> getLinks()
    {
        return null;
    }

    /**
     * The method is not required for root objects and therefore the method is
     * only a stub method.
     *
     * @return always <i>false</i>
     */
    @Override()
    public boolean isCheckinObject()
    {
        return false;
    }

    /**
     * The method is not required for root objects and therefore the method is
     * only a stub method.
     */
    @Override()
    public void dbCheckObjectIn()
    {
    }

    /**
     * The method is not required for root objects and therefore the method is
     * only a stub method.
     *
     * @return always <code>null</code>
     */
    @Override()
    public Set<String> getUniqueAttributes()
    {
        // not needed here
        return null;
    }

    /**
     * The method is not required for root objects and therefore the method is
     * only a stub method.
     *
     * @param _attribute    not used
     * @return always <code>null</code>
     */
    @Override()
    public Object getAttribute(final String _attribute)
    {
        // not needed here
        return null;
    }

    /**
     * The method is not required for root objects and therefore the method is
     * only a stub method.
     *
     * @return always <i>false</i>
     */
    @Override()
    public boolean hasChilds()
    {
        return false;
    }

    /**
     * The method is not required for root objects and therefore the method is
     * only a stub method.
     *
     * @param _parent   not used
     * @param _id       not used
     * @return always <code>null</code>
     */
    @Override()
    public String dbUpdateOrInsert(final AbstractObject _parent,
                                   final String _id)
    {
        return null;
    }

    /**
     * The method is not required for root objects and therefore the method is
     * only a stub method.
     *
     * @return always <code>null</code>
     */
    @Override()
    public String getID()
    {
        return null;
    }
}
