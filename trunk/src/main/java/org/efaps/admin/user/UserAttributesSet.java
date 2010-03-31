/*
 * Copyright 2003 - 2010 The eFaps Team
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

import java.io.Serializable;
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
public class UserAttributesSet implements Serializable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * This static variable is the Key used to store the UserAttribtues into the
     * SessionContext {@link #org.efaps.db.Context.getUserAttribute()}.
     */
    public static final String CONTEXTMAPKEY = "eFapsUserAttributes";

    /**
     * This enumeration is used to get a relation to the necessary types in the
     * eFaps database for the attribute set.
     */
    public enum UserAttributesDefinition
    {
        /**
         *
         */
        ATTRIBUTE("Admin_User_Attribute", "Key", "Value");

        /**
         * stores the name of the Type.
         */
        private final String name;

        /**
         * Name of the attribute containing the Key.
         */
        private final String keyAttribute;

        /**
         * Name of the attribute containing the Value.
         */
        private final String valueAttribute;

        /**
         * Initializes the relationship definition for an user attribute set.
         *
         * @param _name             name of type (relationship)
         * @param _keyAttribute     name of the key attribute
         * @param _value            name of the value attribute
         */
        private UserAttributesDefinition(final String _name,
                                         final String _keyAttribute,
                                         final String _value)
        {
            this.name = _name;
            this.keyAttribute = _keyAttribute;
            this.valueAttribute = _value;
            MAPPER.put(_name, this);
        }

    }

    /**
     * this static Map contains the name-to=UserAttribute Relation used by the
     * enumeration.
     */
    private static final Map<String, UserAttributesSet.UserAttributesDefinition> MAPPER
        = new HashMap<String, UserAttributesSet.UserAttributesDefinition>();

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
     * Constructor using the constructor {@link #UserAttributesSet(long)}
     * through searching the person id for the given name.
     *
     * @param _userName     name of the user this attribute set will belong to
     * @throws EFapsException if user attribute set could not be fetched from
     *                        eFaps database
     */
    public UserAttributesSet(final String _userName)
        throws EFapsException
    {
        this(Person.get(_userName).getId());
    }

    /**
     * Constructor setting the {@link #userId} of the user this user attribute
     * set belongs to and fetching the user attributes for this {@link #userId}
     * from the eFaps database..
     *
     * @param _userId   id of the user this user attribute set will belong to
     * @throws EFapsException if attribute set could not be read from eFaps
     * @see #readUserAttributes()
     */
    public UserAttributesSet(final long _userId)
        throws EFapsException
    {
        this.userId = _userId;
        readUserAttributes();
    }

    /**
     * Initialize this user attribute set.
     *
     * @throws EFapsException if this attribute set could not be read from the
     *                        eFaps database
     * @see #readUserAttributes()
     */
    public void initialise()
        throws EFapsException
    {
        readUserAttributes();
    }

    /**
     * Check if this user attribute set contains an attribute for given
     * <code>_key</code>.
     *
     * @param _key  key to check if this user attribute set contains it
     * @return <i>true</i> if the key was found; otherwise <i>false</i>
     */
    public boolean containsKey(final String _key)
    {
        return this.attributes.containsKey(_key);
    }

    /**
     * Returns the value for a key as a String.
     *
     * @param _key  key for the searched value
     * @return string of the value if exist; otherwise <code>null</code>
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
     * Sets a key-value pair for the attribute set of this attribute set. It
     * uses {@link #set(String, String, UserAttributesDefinition)}
     * to set the the relationship information. It will search in the
     * {@link #MAPPER} to retrieve the definition of the attribute. If found it
     * will use the found one, else it will use the a default
     * {@link UserAttributesDefinition#ATTRIBUTE}.
     *
     * @param _key      key to be set
     * @param _value    value to be set
     * @throws EFapsException if <code>_key</code> or <code>_value</code> is
     *                        <code>null</code>
     */
    public void set(final String _key, final String _value)
        throws EFapsException
    {
        if (UserAttributesSet.MAPPER.containsKey(_key)) {
            set(_key, _value, UserAttributesSet.MAPPER.get(_key));
        } else {
            set(_key, _value, UserAttributesSet.UserAttributesDefinition.ATTRIBUTE);
        }
    }

    /**
     * Sets a key-value pair into the attribute set of an user. The method
     * will search for the key and if the key already exists it will update the
     * user attribute in this set. If the key does not exist a new user
     * attribute will be added to this set.
     *
     * @param _key          key to be set
     * @param _value        value to be set
     * @param _definition   type of the key-value pair
     * @throws EFapsException if <code>_key</code> or <code>_value</code> is
     *                        <code>null</code>
     */
    public void set(final String _key,
                    final String _value,
                    final UserAttributesDefinition _definition)
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
     * This method stores all user attributes of this user attributes set into
     * the eFaps database. Only user attributes which where added or updated in
     * this session will be updated/inserted in the eFpas database.
     *
     * @throws EFapsException if update of the user attributes failed
     */
    public void storeInDb()
        throws EFapsException
    {

        for (final Entry<String, UserAttribute> entry : this.attributes.entrySet()) {
            if (entry.getValue().isUpdate()) {
                final SearchQuery query = new SearchQuery();
                query.setQueryTypes(entry.getValue().getType());
                query.addSelect("OID");
                query.addWhereExprEqValue("UserLink", this.userId.toString());
                if (UserAttributesSet.MAPPER.get(entry.getValue().getType()).keyAttribute != null) {
                    query.addWhereExprEqValue(UserAttributesSet.MAPPER.get(entry.getValue().getType()).keyAttribute,
                                              entry.getKey());
                }
                query.execute();
                Update update;
                if (query.next()) {
                    update = new Update(query.get("OID").toString());
                } else {
                    update = new Insert(entry.getValue().getType());
                    if (UserAttributesSet.MAPPER.get(entry.getValue().getType()).keyAttribute != null) {
                        update.add(UserAttributesSet.MAPPER.get(entry.getValue().getType()).keyAttribute,
                                   entry.getKey());
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
     * Reads all {@link UserAttribute user attributes} from the eFaps database
     * which belong to the user this user attribute set is read to and caches
     * them in a map for fast access.
     *
     * @see #UserAttribute
     * @throws EFapsException if attributes could not be read from eFaps
     */
    private void readUserAttributes()
        throws EFapsException
    {
        final Set<Type> types = Type.get(USER_ATTRIBUTEABSTRACT).getChildTypes();
        for (final Type type : types) {
            if (UserAttributesSet.MAPPER.containsKey(type.getName())) {
                final UserAttributesDefinition definition = UserAttributesSet.MAPPER.get(type.getName());
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
     * Each instance of this class represents one UserAttribute for this user
     * attribute set.
     */
    private class UserAttribute implements Serializable
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Value of this user attribute.
         */
        private String value;

        /**
         * Must this user attribute updated in the eFaps database?
         */
        private boolean update;

        /**
         * Type of this user attribute.
         */
        private final String type;

        /**
         *
         * @param _type     type of this user attribute set
         * @param _value    value of this user attribute set
         * @param _update   <i>true</i> if the attribute must be updated in
         *                  eFaps
         */
        public UserAttribute(final String _type,
                             final String _value,
                             final boolean _update)
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
