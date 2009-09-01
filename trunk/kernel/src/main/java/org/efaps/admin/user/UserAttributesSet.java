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

package org.efaps.admin.user;

import static org.efaps.admin.EFapsClassNames.USER_ATTRIBUTEABSTRACT;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * This class represents a set of UserAttribute related to a User.<br>
 * For each User a set of UserAttribute can be made to store user-related
 * content. e.g. Color of the UserInterface etc. The Class will read all
 * Already in the eFpas-DataBase existing UserAttribute and store them in a
 * map. A new or altered UserAttribute which is created/altered during the
 * Session will only be stored in the map and not into the eFaps-DataBase. To
 * store the UserAttribute into the eFapsDataBase the method
 * {@link #storeInDb()} must be called explicitly.
 *
 * @author The eFasp Team
 * @version $Id$
 */
public class UserAttributesSet
{

    /**
     * This static variable is the Key used to store the UserAttribtues into the
     * SessionContext {@link #org.efaps.db.Context.getUserAttribute()}.
     */
    public static final String CONTEXTMAPKEY = "eFapsUserAttributes";

    /**
     * This enum is used to get a Realtion to the necessary Types in the
     * eFpas-DataBase.
     *
     * @author jmox
     * @version $Id$
     */
    public enum UserAttributesDefinition
    {

        ATTRIBUTE("Admin_User_Attribute", "Key", "Value");

        /**
         * stores the name of the Type.
         */
        public final String name;

        /**
         * stores the Name of the Attribute containing the Key.
         */
        public final String keyAttribute;

        /**
         * stores the Name of the Attribute containing the Value.
         */
        public final String valueAttribute;

        private UserAttributesDefinition(final String _name, final String _keyAttribute, final String _value)
        {
            this.name = _name;
            this.keyAttribute = _keyAttribute;
            this.valueAttribute = _value;
            MAPPER.put(_name, this);
        }

    }

    /**
     * this static Map contains the name-to=UserAttribute Relation used by the
     * enum.
     */
    private final static Map<String, UserAttributesDefinition> MAPPER = new HashMap<String, UserAttributesDefinition>();

    /**
     * instance map to store a Key-to-UserAttribute Relation.
     */
    private final Map<String, UserAttribute> attributes = new HashMap<String, UserAttribute>();

    /**
     * this instance variable stores the Id of the User this UserAttributeSet
     * belongs to.
     */
    private final Long userId;

    /**
     * constructor using the constructor {@link #UserAttributesSet(long)}
     * through searching the Id for the given Name
     *
     * @param _userName UserName of the User this UserAttributesSet will belong
     *            to
     * @throws EFapsException
     */
    public UserAttributesSet(final String _userName) throws EFapsException
    {
        this(Person.get(_userName).getId());
    }

    /**
     * constructor setting the Id of the User this UserAttributesSet belongs to
     *
     * @param _userId Id of the User this UserAttributesSet will belong to
     * @throws EFapsException
     */
    public UserAttributesSet(final long _userId) throws EFapsException
    {
        this.userId = _userId;
        UserAttributesDefinition.values();
        readUserAttributes();
    }

    /**
     * method to initialise this UserAttributesSet
     *
     * @throws EFapsException
     */
    public void initialise() throws EFapsException
    {
        UserAttributesDefinition.values();
        readUserAttributes();
    }

    /**
     * method to check if this UserAttributesSet contains a UserAttribute with
     * the given key
     *
     * @param _key key to check if this UserAttributesSet contains it
     * @return true if the key was found, else false
     */
    public boolean containsKey(final String _key)
    {
        return this.attributes.containsKey(_key);
    }

    /**
     * method to get the Value for a Key as a String
     *
     * @param _key Key for the Value, that should be returned as String
     * @return String of the Value if exist, else null
     */
    public String getString(final String _key)
    {
        String ret = null;
        if (this.attributes.containsKey(_key)) {
            ret = this.attributes.get(_key).getValue();
        }
        return ret;
    }

    /**
     * method to set a Key-Value-Pair into the UserAttributesSet. It uses
     * {@link #set(String, String, org.efaps.admin.user.UserAttributesSet.UserAttributesDefinition)}
     * to set the the Relation. It will search in the {@link #MAPPER} to
     * retrieve the UserAttributesDefinition. If found it will use the found
     * one, else it will use the a default (UserAttributesDefinition.ATTRIBUTE).
     *
     * @param _key key to be set
     * @param _value value to be set
     * @throws EFapsException
     */
    public void set(final String _key, final String _value) throws EFapsException
    {
        if (MAPPER.containsKey(_key)) {
            set(_key, _value, MAPPER.get(_key));
        } else {
            set(_key, _value, UserAttributesDefinition.ATTRIBUTE);
        }
    }

    /**
     * this method sets a Key-Value-Pair into the UserAttributesSet. The method
     * will search for the Key and if the Key allready exist it will update the
     * UserAttribute in this UserAttributesSet. If the Key does not exist a new
     * UserAttribute will be added to this UserAttribute.
     *
     * @param _key key to be set
     * @param _value value to be set
     * @param _definition Type of the Key-Value-Pair
     * @throws EFapsException
     */
    public void set(final String _key, final String _value, final UserAttributesDefinition _definition)
        throws EFapsException
    {
        if (_key == null || _value == null) {
            throw new EFapsException(this.getClass(), "set", _key, _value, _definition);
        } else {
            final UserAttribute userattribute = this.attributes.get(_key);
            if (userattribute == null) {
                this.attributes.put(_key, new UserAttribute(_definition.name, _value.trim(), true));
            } else if (!userattribute.getValue().equals(_value.trim())) {
                userattribute.setUpdate(true);
                userattribute.setValue(_value.trim());
            }

        }
    }

    /**
     * This method stores the UserAttribute of this UserAttributeSet into the
     * eFaps-DataBase. Only UserAttribute witch where added or updated in this
     * session will be updated/inserted in the eFpas-DataBASe.
     *
     * @throws EFapsException
     */
    public void storeInDb() throws EFapsException
    {

        for (final Entry<String, UserAttribute> entry : this.attributes.entrySet()) {
            if (entry.getValue().isUpdate()) {
                final SearchQuery query = new SearchQuery();
                query.setQueryTypes(entry.getValue().getType());
                query.addSelect("OID");
                query.addWhereExprEqValue("UserLink", this.userId.toString());
                if (MAPPER.get(entry.getValue().getType()).keyAttribute != null) {
                    query.addWhereExprEqValue(MAPPER.get(entry.getValue().getType()).keyAttribute, entry.getKey());
                }
                query.execute();
                Update update;
                if (query.next()) {
                    update = new Update(query.get("OID").toString());
                } else {
                    update = new Insert(entry.getValue().getType());
                    if (MAPPER.get(entry.getValue().getType()).keyAttribute != null) {
                        update.add(MAPPER.get(entry.getValue().getType()).keyAttribute, entry.getKey());
                    }
                    update.add("UserLink", this.userId.toString());
                }
                update.add("Value", entry.getValue().getValue());
                update.execute();
                update.close();
                query.close();
            }
        }
        this.attributes.clear();
    }

    /**
     * this method reads all UserAttribute from the eFaps-DataBase which belong
     * to the User this UserAttributesSet is realted to and stores them in a Map
     * for fast access.
     *
     * @see #UserAttribute
     * @throws EFapsException
     */
    private void readUserAttributes() throws EFapsException
    {
        final Set<Type> types = Type.get(USER_ATTRIBUTEABSTRACT).getChildTypes();
        for (final Type type : types) {
            if (MAPPER.containsKey(type.getName())) {
                final UserAttributesDefinition definition = MAPPER.get(type.getName());
                final SearchQuery query = new SearchQuery();

                query.setQueryTypes(definition.name);

                query.addSelect(definition.valueAttribute);
                if (definition.keyAttribute != null) {
                    query.addSelect(definition.keyAttribute);
                }
                query.addWhereExprEqValue("UserLink", this.userId);
                query.execute();
                while (query.next()) {
                    String key;
                    if (definition.keyAttribute == null) {
                        key = definition.name;
                    } else {
                        key = query.get(definition.keyAttribute).toString().trim();
                    }
                    this.attributes.put(key, new UserAttribute(definition.name, query.get(definition.valueAttribute)
                                    .toString().trim(), false));
                }
            }
        }
    }

    /**
     * Each instance of this class represents one UserAttribute for this
     * UserAttributeSet.
     *
     * @author jmox
     * @version $Id$
     */
    private class UserAttribute
    {

        /**
         * the value of this UserAttribute
         */
        private String value;

        /**
         * must this UserAttribute be updated in the eFaps-DataBase
         */
        private boolean update;

        /**
         * Type of this UserAttribute
         */
        private final String type;

        public UserAttribute(final String _type, final String _value, final boolean _update)
        {
            this.type = _type;
            this.value = _value;
            this.update = _update;
        }

        /**
         * This is the getter method for the instance variable {@link #value}.
         *
         * @return value of instance variable {@link #value}
         */
        public String getValue()
        {
            return this.value;
        }

        /**
         * This is the setter method for the instance variable {@link #value}.
         *
         * @param _value the value to set
         */
        public void setValue(final String _value)
        {
            this.value = _value;
        }

        /**
         * This is the getter method for the instance variable {@link #update}.
         *
         * @return value of instance variable {@link #update}
         */
        public boolean isUpdate()
        {
            return this.update;
        }

        /**
         * This is the setter method for the instance variable {@link #update}.
         *
         * @param _update the update to set
         */
        public void setUpdate(final boolean _update)
        {
            this.update = _update;
        }

        /**
         * This is the getter method for the instance variable {@link #type}.
         *
         * @return value of instance variable {@link #type}
         */
        public String getType()
        {
            return this.type;
        }
    }

}
