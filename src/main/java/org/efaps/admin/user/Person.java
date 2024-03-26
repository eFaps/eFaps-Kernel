/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.admin.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributevalue.PasswordStore;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.Update;
import org.efaps.db.Update.Status;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.util.ChronologyType;
import org.efaps.util.DateTimeUtil;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Class represents the instance of a person/user in eFaps.
 *
 * @author The eFasp Team
 *
 */
public final class Person
    extends AbstractUserObject
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Enum for all known and updated attributes from a person. Only this could
     * be defined which are in the SQL table T_USERPERSON.
     */
    public enum AttrName {
        /** Attribute Name for the First Name of the person. */
        FIRSTNAME("FIRSTNAME"),
        /** Attribute Name for the Last Name of the person. */
        LASTNAME("LASTNAME"),
        /** Attribute Name for the Chronology of the person. */
        CHRONOLOGY("CHRONOLOGY"),
        /** Attribute Name for the Timezone of the person. */
        TIMZONE("TIMZONE"),
        /** Attribute Name for the Locale of the person. */
        LOCALE("LOCALE"),
        /** Attribute Name for the language of the person. */
        LANGUAGE("LANG", true);

        /**
         * The name of the depending SQL column for an attribute in the table.
         */
        private final String sqlColumn;

        /**
         * The name of the depending SQL column for an attribute in the table.
         */
        private final boolean integer;

        /**
         * Constructor setting the instance variables.
         *
         * @param _sqlColumn name of the column in the table
         */
        AttrName(final String _sqlColumn)
        {
            this(_sqlColumn, false);
        }

        /**
         * Constructor setting the instance variables.
         *
         * @param _sqlColumn name of the column in the table
         * @param _integer is the column a integer column
         */
        AttrName(final String _sqlColumn,
                 final boolean _integer)
        {
            sqlColumn = _sqlColumn;
            integer = _integer;
        }
    }

    /**
     * This is the SQL select statement to select a Person from the database by ID.
     */
    private static final String SQL_ID = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERPERSON", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a Person from the database by Name.
     */
    private static final String SQL_NAME = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERPERSON", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a Person from the database by UUID.
     */
    private static final String SQL_UUID = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERPERSON", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a Role from the database using the JAAS key..
     */
    private static final String SQL_JAASKEY = new SQLSelect().column("ID")
                    .from("V_USERPERSONJASSKEY", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "JAASKEY").addPart(SQLPart.EQUAL).addValuePart("?")
                    .addPart(SQLPart.AND).addColumnPart(0, "JAASSYSID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * SQL select statement to select the relation to active companies.
     */
    private static final String SQL_COMPANY = new SQLSelect()
                    .column("USERABSTRACTTO")
                    .from("V_USERPERSON2COMPANY", 0)
                    .innerJoin("T_USERABSTRACT", 1, "ID", 0, "USERABSTRACTTO")
                    .addPart(SQLPart.WHERE).addColumnPart(1, "STATUS").addPart(SQLPart.EQUAL).addBooleanValue(true)
                    .addPart(SQLPart.AND).addColumnPart(0, "USERABSTRACTFROM")
                        .addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();
    /**
     * SQL select statement to select the relation to active companies including the JAASYSTEM as filter.
     */
    private static final String SQL_COMPANYJAASKEY = new SQLSelect()
                    .column("USERABSTRACTTO")
                    .from("V_USERPERSON2COMPANY", 0)
                    .innerJoin("T_USERABSTRACT", 1, "ID", 0, "USERABSTRACTTO")
                    .addPart(SQLPart.WHERE).addColumnPart(1, "STATUS").addPart(SQLPart.EQUAL).addBooleanValue(true)
                    .addPart(SQLPart.AND).addColumnPart(0, "USERABSTRACTFROM")
                        .addPart(SQLPart.EQUAL).addValuePart("?")
                    .addPart(SQLPart.AND).addColumnPart(0, "JAASSYSID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * SQL select statement to select the relation to active roles.
     */
    private static final String SQL_ROLE = new SQLSelect()
                    .column("USERABSTRACTTO")
                    .from("V_USERPERSON2ROLE", 0)
                    .innerJoin("T_USERABSTRACT", 1, "ID", 0, "USERABSTRACTTO")
                    .addPart(SQLPart.WHERE).addColumnPart(1, "STATUS").addPart(SQLPart.EQUAL).addBooleanValue(true)
                    .addPart(SQLPart.AND).addColumnPart(0, "USERABSTRACTFROM")
                        .addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();
    /**
     * SQL select statement to select the relation to active roles including the JAASYSTEM as filter.
     */
    private static final String SQL_ROLEJAASKEY = new SQLSelect()
                    .column("USERABSTRACTTO")
                    .from("V_USERPERSON2ROLE", 0)
                    .innerJoin("T_USERABSTRACT", 1, "ID", 0, "USERABSTRACTTO")
                    .addPart(SQLPart.WHERE).addColumnPart(1, "STATUS").addPart(SQLPart.EQUAL).addBooleanValue(true)
                    .addPart(SQLPart.AND).addColumnPart(0, "USERABSTRACTFROM")
                        .addPart(SQLPart.EQUAL).addValuePart("?")
                    .addPart(SQLPart.AND).addColumnPart(0, "JAASSYSID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * SQL select statement to select the relation to active groups.
     */
    private static final String SQL_GROUP = new SQLSelect()
                    .column("USERABSTRACTTO")
                    .from("V_USERPERSON2GROUP", 0)
                    .innerJoin("T_USERABSTRACT", 1, "ID", 0, "USERABSTRACTTO")
                    .addPart(SQLPart.WHERE).addColumnPart(1, "STATUS").addPart(SQLPart.EQUAL).addBooleanValue(true)
                    .addPart(SQLPart.AND).addColumnPart(0, "USERABSTRACTFROM")
                        .addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();
    /**
     * SQL select statement to select the relation to active groups including the JAASYSTEM as filter.
     */
    private static final String SQL_GROUPJAASKEY = new SQLSelect()
                    .column("USERABSTRACTTO")
                    .from("V_USERPERSON2GROUP", 0)
                    .innerJoin("T_USERABSTRACT", 1, "ID", 0, "USERABSTRACTTO")
                    .addPart(SQLPart.WHERE).addColumnPart(1, "STATUS").addPart(SQLPart.EQUAL).addBooleanValue(true)
                    .addPart(SQLPart.AND).addColumnPart(0, "USERABSTRACTFROM")
                        .addPart(SQLPart.EQUAL).addValuePart("?")
                    .addPart(SQLPart.AND).addColumnPart(0, "JAASSYSID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();


    /**
     * Name of the Cache by ID.
     */
    private static String IDCACHE = Person.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static String NAMECACHE = Person.class.getName() + ".Name";

    /**
     * Name of the Cache by UUID.
     */
    private static String UUIDCACHE = Person.class.getName() + ".UUID";

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Person.class);

    /**
     * HashSet instance variable to hold all id of roles for this person.
     *
     * @see #getRoles
     * @see #add(Role)
     */
    private final Set<Long> roles = new HashSet<>();

    /**
     * HashSet instance variable to hold all id of groups for this person.
     *
     * @see #getGroups
     * @see #add(Group)
     */
    private final Set<Long> groups = new HashSet<>();

    /**
     * HashSet instance variable to hold all id of groups for this person.
     *
     * @see #getCompanies
     * @see #add(Company)
     */
    private final Set<Long> companies = new HashSet<>();

    /**
     * HashSet instance variable to hold all id of associations for this person.
     *
     * @see #getAssociations
     * @see #add(Associations)
     */
    private final Set<Long> associations = new HashSet<>();

    /**
     * The map is used to store all attribute values depending on attribute
     * names defined in {@link #AttrName}.
     *
     * @see #setAttrValue
     * @see #updateAttrValue
     * @see #AttrName
     */
    private final Map<Person.AttrName, String> attrValues = new HashMap<>();

    /**
     * The map is used to store information about updates on attribute values.
     * This information is needed if the database must be updated.
     *
     * @see #updateAttrValue
     * @see #commitAttrValuesInDB
     * @see #AttrName
     */
    private final Map<Person.AttrName, String> attrUpdated = new HashMap<>();

    /**
     * The constructor creates a new instance of class {@link Person} and sets
     * the {@link #key} and {@link #id}.
     *
     * @param _id id of the person to set
     * @param _uuid UUID of the person to set
     * @param _name name of the person to set
     * @param _status status of the person to set
     */
    private Person(final long _id,
                   final String _uuid,
                   final String _name,
                   final boolean _status)
    {
        super(_id, _uuid, _name, _status);
    }

    /**
     * Checks, if the given person is assigned to this user object. Here it is
     * only tested if the person is the same as the user of the parameter.
     *
     * @param _person person to test
     * @return <i>true</i> if the person is the same person as this person,
     *         otherwise <i>false</i>
     */
    @Override
    public boolean hasChildPerson(final Person _person)
    {
        return _person.getId() == getId();
    }

    /**
     * Add a role to this person.
     *
     * @param _role role to add to this person
     * @see #roles
     */
    private void add(final Role _role)
    {
        roles.add(_role.getId());
    }

    /**
     * Tests, if the given role is assigned to this person.
     *
     * @param _role role to test
     * @return <code>true</code> if role is assigned to this person, otherwise
     *         <code>false</code>
     */
    public boolean isAssigned(final Role _role)
    {
        return roles.contains(_role.getId());
    }

    /**
     * Add a role to this person.
     *
     * @param _group group to add to this person
     * @see #groups
     */
    private void add(final Group _group)
    {
        groups.add(_group.getId());
    }

    /**
     * Tests, if the given group is assigned to this person.
     *
     * @param _group group to test
     * @return <code>true</code> if group is assigned to this person, otherwise
     *         <code>false</code>
     */
    public boolean isAssigned(final Group _group)
    {
        return groups.contains(_group.getId());
    }

    /**
     * Add a Company to this person.
     *
     * @param _company Company to add to this person
     */
    private void add(final Company _company)
    {
        companies.add(_company.getId());
    }

    /**
     * Tests, if the given Association is assigned to this person.
     *
     * @param _association Association to test
     * @return <code>true</code> if Association is assigned to this person,
     *          otherwise <code>false</code>
     */
    public boolean isAssigned(final Association _association)
    {
        return associations.contains(_association.getId());
    }

    /**
     * Add a Association to this person.
     *
     * @param _associations Association to add to this person
     * @see #groups
     */
    private void add(final Association _associations)
    {
        associations.add(_associations.getId());
    }

    /**
     * Tests, if the given group is assigned to this person.
     *
     * @param _company Company to test
     * @return <code>true</code> if group is assigned to this person, otherwise
     *         <code>false</code>
     */
    public boolean isAssigned(final Company _company)
    {
        return companies.contains(_company.getId());
    }

    /**
     * All assigned roles in {@link #roles} and groups in {@link #groups} are
     * removed in the cache from this person instance. This is needed if the
     * person assignments are rebuild (e.g. from a login servlet).
     */
    public void cleanUp()
    {
        roles.clear();
        groups.clear();
        companies.clear();
    }

    /**
     * The method sets the attribute values in the cache for given attribute
     * name to given new attribute value.
     *
     * @param _attrName name of attribute to set
     * @param _value new value to set
     * @see #attrValues
     */
    private void setAttrValue(final AttrName _attrName,
                              final String _value)
    {
        synchronized (attrValues) {
            attrValues.put(_attrName, _value);
        }
    }

    /**
     * Returns for given attribute name the value in the cache.
     *
     * @param _attrName name of attribute for which the value must returned
     * @return attribute value of given attribute name
     */
    public String getAttrValue(final AttrName _attrName)
    {
        return attrValues.get(_attrName);
    }

    /**
     * @return attribute value of first name
     */
    public String getFirstName()
    {
        return attrValues.get(Person.AttrName.FIRSTNAME);
    }

    /**
     * @return attribute value of last name
     */
    public String getLastName()
    {
        return attrValues.get(Person.AttrName.LASTNAME);
    }

    /**
     * Method to get the Locale of this Person. Default is the "English" Locale.
     *
     * @return Locale of this Person
     */
    public Locale getLocale()
    {
        final Locale ret;
        if (attrValues.get(Person.AttrName.LOCALE) != null) {
            final String localeStr = attrValues.get(Person.AttrName.LOCALE);
            final String[] countries = localeStr.split("_");
            if (countries.length == 2) {
                ret = new Locale(countries[0], countries[1]);
            } else if (countries.length == 3) {
                ret = new Locale(countries[0], countries[1], countries[2]);
            } else {
                ret = new Locale(localeStr);
            }
        } else {
            ret = Locale.ENGLISH;
        }
        return ret;
    }

    /**
     * Method to get the Language of the UserInterface for this Person. Default
     * is english.
     *
     * @return iso code of a language
     */
    public String getLanguage()
    {
        return attrValues.get(Person.AttrName.LANGUAGE) != null
                        ? attrValues.get(Person.AttrName.LANGUAGE)
                        : Locale.ENGLISH.getISO3Language();
    }

    /**
     * Method to get the Timezone of this Person. Default is the "UTC" Timezone.
     *
     * @return Timezone of this Person
     */
    public DateTimeZone getTimeZone()
    {
        return attrValues.get(Person.AttrName.TIMZONE) != null
                        ? DateTimeZone.forID(attrValues.get(Person.AttrName.TIMZONE))
                        : DateTimeZone.UTC;
    }

    /**
     * Method to get the Chronology of this Person. Default is the "ISO8601"
     * Chronology.
     *
     * @return Chronology of this Person
     */
    public Chronology getChronology()
    {
        return getChronologyType().getInstance(getTimeZone());
    }

    /**
     * Method to get the ChronologyType of this Person. Default is the "ISO8601"
     * ChronologyType.
     *
     * @return ChronologyType of this Person
     */
    public ChronologyType getChronologyType()
    {
        final String chronoKey = attrValues.get(Person.AttrName.CHRONOLOGY);
        final ChronologyType chronoType;
        if (chronoKey != null) {
            chronoType = ChronologyType.getByKey(chronoKey);
        } else {
            chronoType = ChronologyType.ISO8601;
        }
        return chronoType;
    }

    /**
     * Updates a value for an attribute in the cache and marks then as modified.
     * Only after calling method {@link #commitAttrValuesInDB} the updated
     * attribute value is stored in the database!
     *
     * @param _attrName name of attribute to update
     * @param _value new value to set directly
     */
    public void updateAttrValue(final AttrName _attrName,
                                final String _value)
    {
        this.updateAttrValue(_attrName, _value, _value);
    }

    /**
     * Updates a value for an attribute in the cache and marks then as modified.
     * Only after calling method {@link #commitAttrValuesInDB} the updated
     * attribute value is stored in the database!
     *
     * @param _attrName name of attribute to update
     * @param _value new value to set directly
     * @param _updateValue new value to be set in the database
     * @see #attrUpdated
     * @see #attrValues
     */
    public void updateAttrValue(final AttrName _attrName,
                                final String _value,
                                final String _updateValue)
    {
        synchronized (attrUpdated) {
            synchronized (attrValues) {
                attrValues.put(_attrName, _value);
            }
            attrUpdated.put(_attrName, _updateValue);
        }
    }

    /**
     * Commits update attribute defined in {@link #attrUpdated} with method
     * {@link #updateAttrValue} to the database. After database update,
     * {@link #attrUpdated} is cleared.
     *
     * @throws EFapsException on error
     * @see #attrUpdated
     * @see #attrValues
     * @see #updateAttrValue
     *
     */
    public void commitAttrValuesInDB()
        throws EFapsException
    {
        synchronized (attrUpdated) {
            if (attrUpdated.size() > 0) {
                Connection con = null;
                try {
                    con = Context.getConnection();

                    final StringBuilder cmd = new StringBuilder();
                    PreparedStatement stmt = null;
                    try {
                        cmd.append("update T_USERPERSON set ");
                        boolean first = true;
                        for (final AttrName attrName : attrUpdated.keySet()) {
                            if (first) {
                                first = false;
                            } else {
                                cmd.append(",");
                            }
                            cmd.append(attrName.sqlColumn).append("=?");
                        }
                        cmd.append(" where ID=").append(getId());
                        stmt = con.prepareStatement(cmd.toString());

                        int col = 1;
                        for (final AttrName attrName : attrUpdated.keySet()) {
                            final String tmp = attrUpdated.get(attrName);
                            if (attrName.integer) {
                                stmt.setInt(col, tmp == null ? 0 : Integer.parseInt(tmp.trim()));
                            } else {
                                stmt.setString(col, tmp == null ? null : tmp.trim());
                            }
                            col++;
                        }

                        final int rows = stmt.executeUpdate();
                        if (rows == 0) {
                            Person.LOG.error("could not update '" + cmd.toString() + "' person with user name '"
                                            + getName() + "' (id = " + getId() + ")");
                            throw new EFapsException(Person.class, "commitAttrValuesInDB.NotUpdated", cmd.toString(),
                                            getName(), getId());
                        }
                        // TODO: update modified date
                    } catch (final SQLException e) {
                        Person.LOG.error("could not update '" + cmd.toString() + "' person with user name '" + getName()
                                        + "' (id = " + getId() + ")", e);
                        throw new EFapsException(Person.class, "commitAttrValuesInDB.SQLException", e, cmd.toString(),
                                        getName(), getId());
                    } finally {
                        try {
                            if (stmt != null) {
                                stmt.close();
                            }
                        } catch (final SQLException e) {
                            throw new EFapsException(Person.class, "commitAttrValuesInDB.SQLException", e, cmd
                                            .toString(), getName(), getId());
                        }
                    }
                    con.commit();
                    con.close();
                } catch (final SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                attrUpdated.clear();
            }
        }
    }

    /**
     * The instance method checks if the given password is the same password as
     * the password in the database.
     *
     * @param _passwd password to check for this person
     * @return <i>true</i> if password is correct, otherwise <i>false</i>
     * @throws EFapsException if query for the password check failed
     */
    public boolean checkPassword(final String _passwd)
        throws EFapsException
    {
        boolean ret = false;
        final PrintQuery query = new PrintQuery(CIAdminUser.Person.getType(), getId());
        query.addAttribute(CIAdminUser.Person.Password,
                           CIAdminUser.Person.LastLogin,
                           CIAdminUser.Person.LoginTry,
                           CIAdminUser.Person.LoginTriesCounter,
                           CIAdminUser.Person.Status);
        if (query.executeWithoutAccessCheck()) {
            final PasswordStore pwd = query.<PasswordStore>getAttribute(CIAdminUser.Person.Password);
            if (pwd.checkCurrent(_passwd)) {
                ret = query.<Boolean>getAttribute(CIAdminUser.Person.Status);
            } else {
                setFalseLogin(query.<DateTime>getAttribute(CIAdminUser.Person.LoginTry),
                                query.<Integer>getAttribute(CIAdminUser.Person.LoginTriesCounter));
            }
        }
        return ret;
    }

    /**
     * Method that sets the time and the number of failed logins.
     *
     * @param _logintry time of the false Login
     * @param _count number of tries
     * @throws EFapsException on error
     */
    private void setFalseLogin(final DateTime _logintry,
                               final int _count)
        throws EFapsException
    {
        if (_count > 0) {
            final DateTime now = new DateTime(DateTimeUtil.getCurrentTimeFromDB().getTime());
            final SystemConfiguration kernelConfig = EFapsSystemConfiguration.get();

            final int minutes = kernelConfig.getAttributeValueAsInteger(KernelSettings.LOGIN_TIME_RETRY);

            final int maxtries = kernelConfig.getAttributeValueAsInteger(KernelSettings.LOGIN_MAX_TRIES);

            final int count = _count + 1;
            if (minutes > 0 && _logintry.minusMinutes(minutes).isBefore(now)) {
                updateFalseLoginDB(1);
            } else {
                updateFalseLoginDB(count);
            }
            if (maxtries > 0 && count > maxtries && getStatus()) {
                setStatusInDB(false);
            }
        } else {
            updateFalseLoginDB(1);
        }
    }

    /**
     * Method to set the number of false Login tries in the eFaps-DataBase.
     *
     * @param _tries number or tries
     * @throws EFapsException on error
     */
    private void updateFalseLoginDB(final int _tries)
        throws EFapsException
    {
        Connection con = null;
        try {
            con = Context.getConnection();

            Statement stmt = null;
            final StringBuilder cmd = new StringBuilder();
            try {

                cmd.append("update T_USERPERSON ").append("set LOGINTRY=").append(
                                Context.getDbType().getCurrentTimeStamp()).append(", LOGINTRIES=").append(_tries)
                                .append(" where ID=").append(getId());
                stmt = con.createStatement();
                final int rows = stmt.executeUpdate(cmd.toString());
                if (rows == 0) {
                    Person.LOG.error("could not execute '" + cmd.toString()
                                    + "' to update last login information for person '" + toString() + "'");
                    throw new EFapsException(getClass(), "updateLastLogin.NotUpdated", cmd.toString(), getName());
                }
            } catch (final SQLException e) {
                Person.LOG.error("could not execute '" + cmd.toString()
                                + "' to update last login information for person '" + toString() + "'", e);
                throw new EFapsException(getClass(), "updateLastLogin.SQLException", e, cmd.toString(), getName());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (final SQLException e) {
                    throw new EFapsException(getClass(), "updateLastLogin.SQLException", e, cmd.toString(), getName());
                }
            }
            con.commit();
            con.close();
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * The instance method sets the new password for the current context user.
     * Before the new password is set, some checks are made.
     *
     * @param _newPasswd new Password to set
     * @throws EFapsException on error
     * @return true if password set, else false
     */
    public Status setPassword(final String _newPasswd)
        throws EFapsException
    {
        final Type type = CIAdminUser.Person.getType();
        if (_newPasswd.length() == 0) {
            throw new EFapsException(getClass(), "PassWordLength", 1, _newPasswd.length());
        }
        final Update update = new Update(type, "" + getId());
        final Status status = update.add(CIAdminUser.Person.Password, _newPasswd);
        if (status.isOk()) {
            update.execute();
            update.close();
        } else {
            Person.LOG.error("Password could not be set by the Update, due to restrictions " + "e.g. length???");
            throw new EFapsException(getClass(), "TODO");
        }
        return status;
    }

    /**
     * The instance method reads all information from the database.
     *
     * @throws EFapsException on error
     * @see #readFromDBAttributes()
     */
    protected void readFromDB()
        throws EFapsException
    {
        readFromDBAttributes();
        roles.clear();
        for (final Role role : getRolesFromDB()) {
            add(role);
        }
        groups.clear();
        for (final Group group : getGroupsFromDB(null)) {
            add(group);
        }
        companies.clear();
        for (final Company company : getCompaniesFromDB(null)) {
            add(company);
        }
        associations.clear();
        for (final Association association : getAssociationsFromDB(null)) {
            add(association);
        }
    }

    /**
     * All attributes from this person are read from the database.
     *
     * @throws EFapsException if the attributes for this person could not be
     *             read
     */
    private void readFromDBAttributes()
        throws EFapsException
    {
        Connection con = null;
        try {
            con = Context.getConnection();
            Statement stmt = null;
            try {
                stmt = con.createStatement();

                final StringBuilder cmd = new StringBuilder("select ");
                for (final AttrName attrName : Person.AttrName.values()) {
                    cmd.append(attrName.sqlColumn).append(",");
                }
                cmd.append("0 as DUMMY ").append("from V_USERPERSON ").append("where V_USERPERSON.ID=").append(getId());

                final ResultSet resultset = stmt.executeQuery(cmd.toString());
                if (resultset.next()) {
                    for (final AttrName attrName : Person.AttrName.values()) {
                        final String tmp = resultset.getString(attrName.sqlColumn);
                        setAttrValue(attrName, tmp == null ? null : tmp.trim());
                    }
                }
                resultset.close();
            } catch (final SQLException e) {
                Person.LOG.error("read attributes for person with SQL statement is not " + "possible", e);
                throw new EFapsException(Person.class, "readFromDBAttributes.SQLException", e, getName(), getId());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    con.commit();
                } catch (final SQLException e) {
                    Person.LOG.error("close of SQL statement is not possible", e);
                }
            }
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
            }
        }
    }

    /**
     * The method reads directly from the database all stored Association for this
     * person. The found Association are returned as instance of {@link Set}.
     *
     * @param _jaasSystem JAAS system for which the roles must get from eFaps
     *            (if value is <code>null</code>, all companies independent from
     *            the related JAAS system are returned)
     * @return set of all found Association for given JAAS system
     * @throws EFapsException on error
     */
    public Set<Association> getAssociationsFromDB(final JAASSystem _jaasSystem)
        throws EFapsException
    {
        final Set<Association> ret = new HashSet<>();
        Connection con = null;
        try {
            final List<Long> associationIds = new ArrayList<>();
            con = Context.getConnection();

            Statement stmt = null;

            try {
                final StringBuilder cmd = new StringBuilder();
                cmd.append("select ").append("ID ")
                    .append("from T_USERASSOC ")
                    .append("where GROUPID in (")
                        .append("select ").append("USERABSTRACTTO ")
                        .append("from V_USERPERSON2GROUP ")
                        .append("where USERABSTRACTFROM =").append(getId())
                    .append(") and ROLEID in (")
                        .append("select ").append("USERABSTRACTTO ")
                        .append("from V_USERPERSON2ROLE ")
                        .append("where USERABSTRACTFROM =").append(getId())
                    .append(")");

                stmt = con.createStatement();
                final ResultSet resultset = stmt.executeQuery(cmd.toString());
                while (resultset.next()) {
                    associationIds.add(resultset.getLong(1));
                }
                resultset.close();

            } catch (final SQLException e) {
                throw new EFapsException(getClass(), "getAssociationsFromDB.SQLException", e, getName());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                        con.commit();
                    }
                } catch (final SQLException e) {
                    throw new EFapsException(getClass(), "getAssociationsFromDB.SQLException", e, getName());
                }
            }

            for (final Long associationId : associationIds) {
                final Association association = Association.get(associationId);
                ret.add(association);
            }
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
            }
        }
        return ret;
    }

    /**
     * The method reads directly from the database all stored companies for this
     * person. The found roles are returned as instance of {@link Set}.
     *
     * @param _jaasSystem JAAS system for which the roles must get from eFaps
     *            (if value is <code>null</code>, all companies independent from
     *            the related JAAS system are returned)
     * @return set of all found companies for given JAAS system
     * @throws EFapsException on error
     */
    public Set<Company> getCompaniesFromDB(final JAASSystem _jaasSystem)
        throws EFapsException
    {
        final Set<Company> ret = new HashSet<>();
        Connection con = null;
        try {
            final List<Long> companyIds = new ArrayList<>();
            con = Context.getConnection();

            PreparedStatement stmt = null;
            try {
                if (_jaasSystem == null) {
                    stmt = con.prepareStatement(Person.SQL_COMPANY);
                } else {
                    stmt = con.prepareStatement(Person.SQL_COMPANYJAASKEY);
                    stmt.setObject(2, _jaasSystem.getId());
                }
                stmt.setObject(1, getId());
                final ResultSet resultset = stmt.executeQuery();
                while (resultset.next()) {
                    companyIds.add(resultset.getLong(1));
                }
                resultset.close();
                con.commit();
                con.close();
            } catch (final SQLException e) {
                throw new EFapsException(getClass(), "getCompaniesFromDB.SQLException", e, getName());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (final SQLException e) {
                    throw new EFapsException(getClass(), "getCompaniesFromDB.SQLException", e, getName());
                }
            }
            for (final Long companyId : companyIds) {
                final Company company = Company.get(companyId);
                ret.add(company);
            }
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * The depending roles for the user are set for the given JAAS system. All
     * roles are added to the loaded roles in the cache of this person.
     *
     * @param _jaasSystem JAAS system for which the roles are set
     * @param _companies set of company to set for the JAAS system
     * @throws EFapsException from calling methods
     */
    public void setCompanies(final JAASSystem _jaasSystem,
                             final Set<Company> _companies)
        throws EFapsException
    {

        if (_jaasSystem == null) {
            throw new EFapsException(getClass(), "setRoles.nojaasSystem", getName());
        }
        if (_companies == null) {
            throw new EFapsException(getClass(), "setRoles.noRoles", getName());
        }
        for (final Company company : _companies) {
            add(company);
        }

        // current roles
        final Set<Company> companiesInDb = getCompaniesFromDB(_jaasSystem);

        // compare new Companies with current Companies (add missing Companies)
        for (final Company company : _companies) {
            if (!companiesInDb.contains(company)) {
                assignCompanyInDb(_jaasSystem, company);
            }
        }

        // compare current Companies with new Companies (remove Companies which are to much)
        for (final Company company : companiesInDb) {
            if (!_companies.contains(company)) {
                unassignCompanyInDb(_jaasSystem, company);
            }
        }
    }

    /**
     * For this person, a role is assigned for the given JAAS system.
     *
     * @param _jaasSystem JAAS system for which the role is assigned
     * @param _company the company
     * @throws EFapsException on error
     * @see AbstractUserObject#assignToUserObjectInDb(Type, JAASSystem,
     *      AbstractUserObject)
     */
    public void assignCompanyInDb(final JAASSystem _jaasSystem,
                                  final Company _company)
        throws EFapsException
    {
        assignToUserObjectInDb(CIAdminUser.Person2Company.getType(), _jaasSystem, _company);
    }

    /**
     * The given role is unassigned for the given JAAS system from this person.
     *
     * @param _jaasSystem JAAS system for which the role is assigned
     * @param _company the company
     * @throws EFapsException on error
     * @see AbstractUserObject#unassignFromUserObjectInDb(Type, JAASSystem,
     *      AbstractUserObject)
     */
    public void unassignCompanyInDb(final JAASSystem _jaasSystem,
                                    final Company _company)
        throws EFapsException
    {
        unassignFromUserObjectInDb(CIAdminUser.Person2Company.getType(), _jaasSystem, _company);
    }

    /**
     * The method reads directly from the database all stores roles for the this
     * person. The found roles are returned as instance of {@link java.util.Set}
     * .
     *
     * @return set of all found roles for all JAAS systems
     * @see #getRolesFromDB(JAASSystem);
     * @throws EFapsException on error
     */
    public Set<Role> getRolesFromDB()
        throws EFapsException
    {
        return getRolesFromDB((JAASSystem) null);
    }

    /**
     * The method reads directly from the database all stores roles for the this
     * person. The found roles are returned as instance of {@link java.util.Set}
     * .
     *
     * @param _jaasSystem JAAS system for which the roles are searched in eFaps
     *            (if value is <code>null</code>, all roles independent from the
     *            related JAAS system are returned)
     * @return set of all found roles for given JAAS system
     * @throws EFapsException on error
     */
    public Set<Role> getRolesFromDB(final JAASSystem _jaasSystem)
        throws EFapsException
    {

        final Set<Role> ret = new HashSet<>();
        Connection con = null;
        try {
            final List<Long> roleIds = new ArrayList<>();
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                if (_jaasSystem == null) {
                    stmt = con.prepareStatement(Person.SQL_ROLE);
                } else {
                    stmt = con.prepareStatement(Person.SQL_ROLEJAASKEY);
                    stmt.setObject(2, _jaasSystem.getId());
                }
                stmt.setObject(1, getId());
                final ResultSet resultset = stmt.executeQuery();
                while (resultset.next()) {
                    roleIds.add(resultset.getLong(1));
                }
                resultset.close();

            } catch (final SQLException e) {
                throw new EFapsException(getClass(), "getRolesFromDB.SQLException", e, getName());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    con.commit();
                } catch (final SQLException e) {
                    throw new EFapsException(getClass(), "getRolesFromDB.SQLException", e, getName());
                }
            }

            final Set<UUID> roleUUIDs = AppAccessHandler.getLoginRoles();
            for (final Long roleId : roleIds) {
                final Role role = Role.get(roleId);
                if (!AppAccessHandler.excludeMode()
                                || AppAccessHandler.excludeMode() && roleUUIDs.contains(role.getUUID())) {
                    ret.add(role);
                }
            }
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
            }
        }
        return ret;
    }

    /**
     * The depending roles for the user are set for the given JAAS system. All
     * roles are added to the loaded roles in the cache of this person.
     *
     * @param _jaasSystem JAAS system for which the roles are set
     * @param _roles set of roles to set for the JAAS system
     * @see #assignRoleInDb
     * @see #unassignRoleInDb
     * @throws EFapsException from calling methods
     */
    public void setRoles(final JAASSystem _jaasSystem,
                         final Set<Role> _roles)
        throws EFapsException
    {

        if (_jaasSystem == null) {
            throw new EFapsException(getClass(), "setRoles.nojaasSystem", getName());
        }
        if (_roles == null) {
            throw new EFapsException(getClass(), "setRoles.noRoles", getName());
        }

        for (final Role role : _roles) {
            add(role);
        }

        // current roles
        final Set<Role> rolesInDb = getRolesFromDB(_jaasSystem);

        // compare new roles with current roles (add missing roles)
        for (final Role role : _roles) {
            if (!rolesInDb.contains(role)) {
                assignRoleInDb(_jaasSystem, role);
            }
        }

        // compare current roles with new roles (remove roles which are to much)
        for (final Role role : rolesInDb) {
            if (!_roles.contains(role)) {
                unassignRoleInDb(_jaasSystem, role);
            }
        }
    }

    /**
     * For this person, a role is assigned for the given JAAS system.
     *
     * @param _jaasSystem JAAS system for which the role is assigned
     * @param _role role to assign
     * @see AbstractUserObject#assignToUserObjectInDb(Type, JAASSystem,
     *      AbstractUserObject)
     * @throws EFapsException on error
     */
    public void assignRoleInDb(final JAASSystem _jaasSystem,
                               final Role _role)
        throws EFapsException
    {
        assignToUserObjectInDb(CIAdminUser.Person2Role.getType(), _jaasSystem, _role);
    }

    /**
     * The given role is unassigned for the given JAAS system from this person.
     *
     * @param _jaasSystem JAAS system for which the role is assigned
     * @param _role role to unassign
     * @see AbstractUserObject#unassignFromUserObjectInDb(Type, JAASSystem,
     *      AbstractUserObject)
     * @throws EFapsException on error
     */
    public void unassignRoleInDb(final JAASSystem _jaasSystem,
                                 final Role _role)
        throws EFapsException
    {
        unassignFromUserObjectInDb(CIAdminUser.Person2Role.getType(), _jaasSystem, _role);
    }

    /**
     * The method reads directly from eFaps all stored groups for the this
     * person. The found groups are returned as instance of {@link Set}.
     *
     * @param _jaasSystem JAAS system for which the groups must fetched from
     *            eFaps (if value is <code>null</code>, all groups independent
     *            from the related JAAS system are returned)
     * @throws EFapsException on error
     * @return set of all found groups for given JAAS system
     */
    public Set<Group> getGroupsFromDB(final JAASSystem _jaasSystem)
        throws EFapsException
    {
        final Set<Group> ret = new HashSet<>();
        Connection con = null;
        try {
            final List<Long> groupIds = new ArrayList<>();
            con = Context.getConnection();

            PreparedStatement stmt = null;
            try {
                if (_jaasSystem == null) {
                    stmt = con.prepareStatement(Person.SQL_GROUP);
                } else {
                    stmt = con.prepareStatement(Person.SQL_GROUPJAASKEY);
                    stmt.setObject(2, _jaasSystem.getId());
                }
                stmt.setObject(1, getId());
                final ResultSet resultset = stmt.executeQuery();
                while (resultset.next()) {
                    groupIds.add(resultset.getLong(1));
                }
                resultset.close();
            } catch (final SQLException e) {
                throw new EFapsException(getClass(), "getGroupsFromDB.SQLException", e, getName());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    con.commit();
                } catch (final SQLException e) {
                    throw new EFapsException(getClass(), "getGroupsFromDB.SQLException", e, getName());
                }
            }
            for (final Long groupId : groupIds) {
                ret.add(Group.get(groupId));
            }
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
            }
        }
        return ret;
    }

    /**
     * The depending groups for the user are set for the given JAAS system. All
     * groups are added to the loaded groups in the cache of this person.
     *
     * @param _jaasSystem JAAS system for which the roles are set
     * @param _groups set of groups to set for the JAAS system
     * @see #assignGroupInDb(JAASSystem, Group)
     * @see #unassignGroupInDb(JAASSystem, Group)
     * @throws EFapsException from calling methods
     */
    public void setGroups(final JAASSystem _jaasSystem,
                          final Set<Group> _groups)
        throws EFapsException
    {
        if (_jaasSystem == null) {
            throw new EFapsException(getClass(), "setGroups.nojaasSystem", getName());
        }
        if (_groups == null) {
            throw new EFapsException(getClass(), "setGroups.noGroups", getName());
        }

        for (final Group group : _groups) {
            add(group);
        }

        // current groups
        final Set<Group> groupsInDb = getGroupsFromDB(_jaasSystem);

        // compare new roles with current groups (add missing groups)
        for (final Group group : _groups) {
            if (!groupsInDb.contains(group)) {
                assignGroupInDb(_jaasSystem, group);
            }
        }

        // compare current roles with new groups (remove groups which are to
        // much)
        for (final Group group : groupsInDb) {
            if (!_groups.contains(group)) {
                unassignGroupInDb(_jaasSystem, group);
            }
        }
    }

    /**
     * For this person, a group is assigned for the given JAAS system.
     *
     * @param _jaasSystem JAAS system for which the group is assigned
     * @param _group group to assign
     * @throws EFapsException on error
     * @see AbstractUserObject#assignToUserObjectInDb
     */
    public void assignGroupInDb(final JAASSystem _jaasSystem,
                                final Group _group)
        throws EFapsException
    {
        assignToUserObjectInDb(CIAdminUser.Person2Group.getType(), _jaasSystem, _group);
    }

    /**
     * The given group is unassigned for the given JAAS system from this person.
     *
     * @param _jaasSystem JAAS system for which the role is assigned
     * @param _group group to unassign
     * @throws EFapsException on error
     * @see AbstractUserObject#unassignFromUserObjectInDb
     */
    public void unassignGroupInDb(final JAASSystem _jaasSystem,
                                  final Group _group)
        throws EFapsException
    {
        unassignFromUserObjectInDb(CIAdminUser.Person2Group.getType(), _jaasSystem, _group);
    }

    /**
     * Update the last login date of this person to current time stamp.
     *
     * @throws EFapsException if the last login information could not be updated
     */
    public void updateLastLogin()
        throws EFapsException
    {
        Connection con = null;
        try {
            con = Context.getConnection();

            Statement stmt = null;
            final StringBuilder cmd = new StringBuilder();
            try {

                cmd.append("update T_USERPERSON ").append("set LASTLOGIN=").append(
                                Context.getDbType().getCurrentTimeStamp()).append(", LOGINTRIES=0 ")
                                .append("where ID=").append(getId());
                stmt = con.createStatement();
                final int rows = stmt.executeUpdate(cmd.toString());
                if (rows == 0) {
                    Person.LOG.error("could not execute '" + cmd.toString()
                                    + "' to update last login information for person '" + toString() + "'");
                    throw new EFapsException(getClass(), "updateLastLogin.NotUpdated", cmd.toString(), getName());
                }
            } catch (final SQLException e) {
                Person.LOG.error("could not execute '" + cmd.toString()
                                + "' to update last login information for person '" + toString() + "'", e);
                throw new EFapsException(getClass(), "updateLastLogin.SQLException", e, cmd.toString(), getName());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                        con.commit();
                    }
                } catch (final SQLException e) {
                    throw new EFapsException(getClass(), "updateLastLogin.SQLException", e, cmd.toString(), getName());
                }
            }
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new EFapsException(getClass(), "updateLastLogin.SQLException", e, getName());
            }
        }
    }

    /**
     * This is the getter method for instance variable {@link #roles}.
     *
     * @return the value of the instance variable {@link #roles}.
     * @see #roles
     */
    public Set<Long> getRoles()
    {
        return Collections.unmodifiableSet(roles);
    }

    /**
     * This is the getter method for instance variable {@link #groups}.
     *
     * @return the value of the instance variable {@link #groups}.
     * @see #groups
     */
    public Set<Long> getGroups()
    {
        return Collections.unmodifiableSet(groups);
    }

    /**
     * Getter method for instance variable {@link #companies}.
     *
     * @return value of instance variable {@link #companies}
     */
    public Set<Long> getCompanies()
    {
        return Collections.unmodifiableSet(companies);
    }

    /**
     * Getter method for instance variable {@link #associations}.
     *
     * @return value of instance variable {@link #associations}
     */
    public Set<Long> getAssociations()
    {
        return Collections.unmodifiableSet(associations);
    }

    @Override
    public Instance getInstance()
    {
        return Instance.get(CIAdminUser.Person.getType(), getId());
    }

    /**
     * Returns a string representation of this person.
     *
     * @return string representation of this person
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                        .appendSuper(super.toString())
                        .append("attrValues", attrValues)
                        .append("roles", roles)
                        .append("groups", groups)
                        .append("companies", companies)
                        .append("associations", associations)
                        .toString();
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof Person) {
            ret = ((Person) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return Long.valueOf(getId()).intValue();
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(Person.IDCACHE)) {
            InfinispanCache.get().<Long, Person>getCache(Person.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, Person>getCache(Person.IDCACHE).addListener(new CacheLogListener(Person.LOG));
        }
        if (InfinispanCache.get().exists(Person.NAMECACHE)) {
            InfinispanCache.get().<String, Person>getCache(Person.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, Person>getCache(Person.NAMECACHE)
                            .addListener(new CacheLogListener(Person.LOG));
        }
        if (InfinispanCache.get().exists(Person.UUIDCACHE)) {
            InfinispanCache.get().<UUID, Person>getCache(Person.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, Person>getCache(Person.UUIDCACHE)
                            .addListener(new CacheLogListener(Person.LOG));
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Person}.
     *
     * @param _id id to search in the cache
     * @throws EFapsException on error
     * @return instance of class {@link Person}
     * @see #CACHE
     * @see #getFromDB
     */
    public static Person get(final long _id)
        throws EFapsException
    {
        final Cache<Long, Person> cache = InfinispanCache.get().<Long, Person>getCache(Person.IDCACHE);
        if (!cache.containsKey(_id)) {
            Person.getPersonFromDB(Person.SQL_ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Person}.
     *
     * @param _uuid UUID to search in the cache
     * @throws EFapsException on error
     * @return instance of class {@link Person}
     * @see #getFromDB
     */
    public static Person get(final UUID _uuid)
        throws EFapsException
    {
        final Cache<UUID, Person> cache = InfinispanCache.get().<UUID, Person>getCache(Person.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            Person.getPersonFromDB(Person.SQL_UUID, _uuid.toString());
        }
        return cache.get(_uuid);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Person}.
     *
     * @param _name name to search in the cache
     * @throws EFapsException on error
     * @return instance of class {@link Person}
     * @see #CACHE
     * @see #getFromDB
     */
    public static Person get(final String _name)
        throws EFapsException
    {
        final Cache<String, Person> cache = InfinispanCache.get().<String, Person>getCache(Person.NAMECACHE);
        if (!cache.containsKey(_name)) {
            Person.getPersonFromDB(Person.SQL_NAME, _name);
        }
        return cache.get(_name);
    }

    /**
     * Reset a person. Meaning ti will be removed from all Caches.
     *
     * @param _key for the Person to be cleaned from Cache. UUID or Name.
     * @throws EFapsException the e faps exception
     */
    public static void reset(final String _key)
        throws EFapsException
    {
        final Person person;
        if (UUIDUtil.isUUID(_key)) {
            person = Person.get(UUID.fromString(_key));
        } else {
            person = Person.get(_key);
        }
        if (person != null) {
            InfinispanCache.get().<Long, Person>getCache(Person.IDCACHE).remove(person.getId());
            InfinispanCache.get().<String, Person>getCache(Person.NAMECACHE).remove(person.getName());
            if (person.getUUID() != null) {
                InfinispanCache.get().<UUID, Person>getCache(Person.UUIDCACHE).remove(person.getUUID());
            }
        }
    }

    /**
     * @param _person Person to be cached
     * @throws EFapsException on error
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED")
    private static void cachePerson(final Person _person)
        throws EFapsException
    {
        final Cache<String, Person> nameCache = InfinispanCache.get().<String, Person>getIgnReCache(Person.NAMECACHE);
        nameCache.putIfAbsent(_person.getName(), _person);

        final Cache<Long, Person> idCache = InfinispanCache.get().<Long, Person>getIgnReCache(Person.IDCACHE);
        idCache.putIfAbsent(_person.getId(), _person);

        if (_person.getUUID() != null) {
            final Cache<UUID, Person> uuidCache = InfinispanCache.get().<UUID, Person>getIgnReCache(Person.UUIDCACHE);
            uuidCache.putIfAbsent(_person.getUUID(), _person);
        }
    }

    /**
     * @param _sql      SQL Statment to be execuetd
     * @param _criteria filter criteria
     * @return true if successful
     * @throws EFapsException on error
     */
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private static Person getPersonFromDB(final String _sql,
                                          final Object _criteria)
        throws EFapsException
    {
        Person ret = null;
        Connection con = null;
        try {
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(_sql);
                stmt.setObject(1, _criteria);
                final ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    final long id = rs.getLong(1);
                    final String uuid = rs.getString(2);
                    final String name = rs.getString(3);
                    final boolean status = rs.getBoolean(4);
                    ret = new Person(id, uuid, name.trim(), status);
                    Person.cachePerson(ret);
                    Person.LOG.debug("read from DB Person:{} ", ret);
                }
                rs.close();
            } catch (final SQLException e) {
                Person.LOG.error("search for person with SQL statement '" + _sql + "' is not possible", e);
                throw new EFapsException(Person.class, "getFromDB.SQLException", e, _sql);
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    if (con != null) {
                        con.commit();
                    }
                } catch (final SQLException e) {
                    Person.LOG.error("Catched error on closing statement", e);
                }
            }
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
            }
        }
        if (ret != null) {
            ret.readFromDB();
        }
        return ret;
    }

    /**
     * Returns for given parameter <i>_jaasKey</i> the instance of class
     * {@link Person}. The parameter <i>_jaasKey</i> is the name of the person
     * used in the given JAAS system for the person.
     *
     * @param _jaasSystem JAAS system for which the JAAS key is named
     * @param _jaasKey key in the foreign JAAS system for which the person is
     *            searched
     * @throws EFapsException on error
     * @return instance of class {@link Person}, or <code>null</code> if person
     *         is not found
     * @see #get(long)
     */
    public static Person getWithJAASKey(final JAASSystem _jaasSystem,
                                        final String _jaasKey)
        throws EFapsException
    {
        long personId = 0;
        Connection con = null;
        try {
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(Person.SQL_JAASKEY);
                stmt.setObject(1, _jaasKey);
                stmt.setObject(2, _jaasSystem.getId());
                final ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    personId = rs.getLong(1);
                }
                rs.close();
            } catch (final SQLException e) {
                Person.LOG.error("search for person for JAAS system '" + _jaasSystem.getName() + "' with key '"
                                + _jaasKey + "' is not possible", e);
                throw new EFapsException(Person.class, "getWithJAASKey.SQLException", e, _jaasSystem.getName(),
                                _jaasKey);
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                        con.commit();
                    }
                } catch (final SQLException e) {
                    throw new EFapsException(Person.class, "getWithJAASKey.SQLException", e, _jaasSystem.getName(),
                                    _jaasKey);
                }
            }
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new EFapsException(Person.class, "updateLastLogin.SQLException", e);
            }
        }
        return Person.get(personId);
    }

    /**
     * Creates the person.
     *
     * @param _jaasSystem JAAS system which want to create a new person in eFaps
     * @param _jaasKey key of the person in the JAAS system
     * @param _userName name in the eFaps system (used as proposal, it's tested
     *            for uniqueness and changed if needed!)
     * @param _uuid the uuid
     * @return new created person
     * @throws EFapsException if person could not be created in eFaps
     * @see #assignToJAASSystem
     */
    public static Person createPerson(final JAASSystem _jaasSystem,
                                      final String _jaasKey,
                                      final String _userName,
                                      final String _uuid,
                                      final boolean _active)
        throws EFapsException
    {
        long persId = 0;
        final Type persType = CIAdminUser.Person.getType();
        Connection con = null;
        try {
            final Context context = Context.getThreadContext();
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                StringBuilder cmd = new StringBuilder();
                // TODO: check for uniqueness!
                if (Context.getDbType().supportsGetGeneratedKeys()) {
                    cmd.append("insert into ").append(persType.getMainTable().getSqlTable()).append(
                                    "(TYPEID,NAME,UUID,CREATOR,CREATED,MODIFIER,MODIFIED) ").append("values (");
                } else {
                    persId = Context.getDbType().getNewId(new ConnectionResource(con),
                                    persType.getMainTable().getSqlTable(), "ID");
                    cmd.append("insert into ").append(persType.getMainTable().getSqlTable())
                        .append("(ID,TYPEID,NAME,UUID,STATUS,CREATOR,CREATED,MODIFIER,MODIFIED) ")
                        .append("values (").append(persId).append(",");
                }
                cmd.append(persType.getId()).append(",")
                    .append("'").append(_userName).append("',")
                    .append("'").append(_uuid == null ? "" : _uuid).append("',")
                    .append("").append(_active).append(",")
                    .append(context.getPersonId())
                    .append(",").append(Context.getDbType().getCurrentTimeStamp()).append(",")
                    .append(context.getPersonId()).append(",")
                    .append(Context.getDbType().getCurrentTimeStamp()).append(")");

                if (persId == 0) {
                    stmt = con.prepareStatement(cmd.toString(), new String[] { "ID" });
                } else {
                    stmt = con.prepareStatement(cmd.toString());
                }

                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    Person.LOG.error("could not execute '" + cmd.toString() + "' for JAAS system '"
                                    + _jaasSystem.getName() + "' person with key '" + _jaasKey
                                    + "' and user name '" + _userName + "'");
                    throw new EFapsException(Person.class, "createPerson.NotInserted", cmd.toString(), _jaasSystem
                                    .getName(), _jaasKey, _userName);
                }
                if (persId == 0) {
                    final ResultSet resultset = stmt.getGeneratedKeys();
                    if (resultset.next()) {
                        persId = resultset.getLong(1);
                    }
                }

                stmt.close();

                cmd = new StringBuilder();
                cmd.append("insert into T_USERPERSON").append("(ID,FIRSTNAME,LASTNAME) ")
                    .append("values (").append(persId).append(",'-','-')");
                stmt = con.prepareStatement(cmd.toString());
                rows = stmt.executeUpdate();
                if (rows == 0) {
                    Person.LOG.error("could not execute '" + cmd.toString() + "' for JAAS system '"
                                    + _jaasSystem.getName()
                                    + "' person with key '" + _jaasKey + "' and user name '" + _userName + "'");
                    throw new EFapsException(Person.class, "createPerson.NotInserted", cmd.toString(), _jaasSystem
                                    .getName(), _jaasKey, _userName);
                }
                cmd = new StringBuilder();
                cmd.append("insert into T_CMGENINST").append("(INSTTYPEID,INSTID,EXID,EXSYSID) ")
                    .append("values (").append(persType.getId()).append(",").append(persId).append(",0,0)");
                stmt = con.prepareStatement(cmd.toString());
                stmt.executeUpdate();
            } catch (final SQLException e) {
                Person.LOG.error("could not create for JAAS system '" + _jaasSystem.getName() + "' person with key '"
                                + _jaasKey + "' and user name '" + _userName + "'", e);
                throw new EFapsException(Person.class, "createPerson.SQLException", e, _jaasSystem.getName(), _jaasKey,
                                _userName);
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                        con.commit();
                    }
                } catch (final SQLException e) {
                    throw new EFapsException(Person.class, "createPerson.SQLException", e, _jaasSystem.getName(),
                                    _jaasKey);
                }
            }
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new EFapsException(Person.class, "updateLastLogin.SQLException", e);
            }
        }

        final Person ret = Person.get(persId);
        ret.assignToJAASSystem(_jaasSystem, _jaasKey);
        return ret;
    }
}
