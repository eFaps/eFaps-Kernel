/*
 * Copyright 2003 - 2013 The eFaps Team
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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractUserObject
    extends AbstractAdminObject
    implements Serializable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractUserObject.class);

    /**
     * Instance variable holding the Status (active, inactive).
     */
    private boolean status;

    /**
     * Constructor to set instance variables of the user object.
     *
     * @param _id       id to set
     * @param _uuid     uuid to set
     * @param _name     name to set
     * @param _status   status to set
     */
    protected AbstractUserObject(final long _id,
                                 final String _uuid,
                                 final String _name,
                                 final boolean _status)
    {
        super(_id, _uuid, _name);
        this.status = _status;
    }

    /**
     * Checks, if the given person is assigned to this user object. The method
     * must be overwritten by the special implementations.
     *
     * @param _person person to test
     * @return <i>true</i> if the person is assigned to this user object,
     *         otherwise <i>false</i>
     * @see #persons
     * @see #getPersons
     */
    public abstract boolean hasChildPerson(final Person _person);

    /**
     * Checks, if the context user is assigned to this user object. The instance
     * method uses {@link #hasChildPerson} to test this.
     *
     * @see #hasChildPerson
     * @return true if person is assigned
     */
    public boolean isAssigned()
    {
        boolean ret = false;
        try {
            ret = hasChildPerson(Context.getThreadContext().getPerson());
        } catch (final EFapsException e) {
            AbstractUserObject.LOG.error("could not read Person ", e);
        }
        return ret;
    }

    /**
     * Assign this user object to the given JAAS system under the given JAAS
     * key.
     *
     * @param _jaasSystem   JAAS system to which the person is assigned
     * @param _jaasKey      key under which the person is know in the JAAS
     *                      system
     * @throws EFapsException if the assignment could not be made
     */
    public void assignToJAASSystem(final JAASSystem _jaasSystem,
                                   final String _jaasKey)
        throws EFapsException
    {
        ConnectionResource rsrc = null;
        try {
            final Context context = Context.getThreadContext();
            rsrc = context.getConnectionResource();
            final Type keyType = CIAdminUser.JAASKey.getType();

            PreparedStatement stmt = null;
            final StringBuilder cmd = new StringBuilder();
            try {
                long keyId = 0;
                if (Context.getDbType().supportsGetGeneratedKeys()) {
                    cmd.append("insert into ").append(keyType.getMainTable().getSqlTable()).append(
                                    "(KEY,CREATOR,CREATED,MODIFIER,MODIFIED,").append("USERABSTRACT,USERJAASSYSTEM) ")
                                    .append("values (");
                } else {
                    keyId = Context.getDbType().getNewId(rsrc.getConnection(), keyType.getMainTable().getSqlTable(),
                                    "ID");
                    cmd.append("insert into ").append(keyType.getMainTable().getSqlTable()).append(
                                    "(ID,KEY,CREATOR,CREATED,MODIFIER,MODIFIED,").append(
                                    "USERABSTRACT,USERJAASSYSTEM) ").append("values (").append(keyId).append(",");
                }
                cmd.append("'").append(_jaasKey).append("',").append(context.getPersonId()).append(",").append(
                                Context.getDbType().getCurrentTimeStamp()).append(",").append(context.getPersonId())
                                .append(",").append(Context.getDbType().getCurrentTimeStamp()).append(",").append(
                                                getId()).append(",").append(_jaasSystem.getId()).append(")");
                stmt = rsrc.getConnection().prepareStatement(cmd.toString());
                final int rows = stmt.executeUpdate();
                if (rows == 0) {
                    AbstractUserObject.LOG.error("could not execute '" + cmd.toString()
                            + "' for JAAS system '" + _jaasSystem.getName()
                            + "' for user object '" + toString() + "' with JAAS key '" + _jaasKey + "'");
                    throw new EFapsException(getClass(), "assignToJAASSystem.NotInserted", _jaasSystem.getName(),
                                    _jaasKey, toString());
                }
            } catch (final SQLException e) {
                AbstractUserObject.LOG.error("could not execute '" + cmd.toString()
                        + "' to assign user object '" + toString()
                        + "' with JAAS key '" + _jaasKey + "' to JAAS system '" + _jaasSystem.getName() + "'", e);
                throw new EFapsException(getClass(), "assignToJAASSystem.SQLException", e, cmd.toString(), _jaasSystem
                                .getName(), _jaasKey, toString());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (final SQLException e) {
                    AbstractUserObject.LOG.error("Could not close a statement.", e);
                }
            }

            rsrc.commit();
        } finally {
            if ((rsrc != null) && rsrc.isOpened()) {
                rsrc.abort();
            }
        }
    }

    /**
     * Assign this user object to the given user object for given JAAS system.
     *
     * @param _assignType   type used to assign (in other words the
     *                      relationship type)
     * @param _jaasSystem   JAAS system for which this user object is assigned
     *                      to the given object
     * @param _object user object to which this user object is assigned
     * @throws EFapsException if assignment could not be done
     */
    @SuppressWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    protected void assignToUserObjectInDb(final Type _assignType,
                                          final JAASSystem _jaasSystem,
                                          final AbstractUserObject _object)
        throws EFapsException
    {
        ConnectionResource rsrc = null;
        try {
            final Context context = Context.getThreadContext();
            rsrc = context.getConnectionResource();

            Statement stmt = null;
            final StringBuilder cmd = new StringBuilder();
            try {

                cmd.append("insert into ").append(_assignType.getMainTable().getSqlTable()).append("(");
                long keyId = 0;
                if (!Context.getDbType().supportsGetGeneratedKeys()) {
                    keyId = Context.getDbType().getNewId(rsrc.getConnection(),
                                    _assignType.getMainTable().getSqlTable(), "ID");
                    cmd.append("ID,");
                }
                cmd.append("TYPEID,CREATOR,CREATED,MODIFIER,MODIFIED,").append(
                                "USERABSTRACTFROM,USERABSTRACTTO,USERJAASSYSTEM) ").append("values (");
                if (keyId != 0) {
                    cmd.append(keyId).append(",");
                }
                cmd.append(_assignType.getId()).append(",").append(context.getPersonId()).append(",").append(
                                Context.getDbType().getCurrentTimeStamp()).append(",").append(context.getPersonId())
                                .append(",").append(Context.getDbType().getCurrentTimeStamp()).append(",").append(
                                                getId()).append(",").append(_object.getId()).append(",").append(
                                                _jaasSystem.getId()).append(")");

                stmt = rsrc.getConnection().createStatement();
                final int rows = stmt.executeUpdate(cmd.toString());
                if (rows == 0) {
                    AbstractUserObject.LOG.error("could not execute '" + cmd.toString()
                            + "' to assign user object '" + toString()
                            + "' to object '" + _object + "' for JAAS system '" + _jaasSystem + "' ");
                    throw new EFapsException(getClass(), "assignInDb.NotInserted", _jaasSystem.getName(), _object
                                    .getName(), getName());
                }
            } catch (final SQLException e) {
                AbstractUserObject.LOG.error("could not execute '" + cmd.toString()
                        + "' to assign user object '" + toString()
                        + "' to object '" + _object + "' for JAAS system '" + _jaasSystem + "' ", e);
                throw new EFapsException(getClass(), "assignInDb.SQLException", e, cmd.toString(), getName());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (final SQLException e) {
                    AbstractUserObject.LOG.error("Could not close a statement.", e);
                }
            }
            rsrc.commit();
        } finally {
            if ((rsrc != null) && rsrc.isOpened()) {
                rsrc.abort();
            }
        }
    }

    /**
     * Unassign this user object from the given user object for given JAAS
     * system.
     *
     * @param _unassignType type used to unassign (in other words the
     *            relationship type)
     * @param _jaasSystem JAAS system for which this user object is unassigned
     *            from given object
     * @param _object user object from which this user object is unassigned
     * @throws EFapsException if unassignment could not be done
     */
    protected void unassignFromUserObjectInDb(final Type _unassignType,
                                              final JAASSystem _jaasSystem,
                                              final AbstractUserObject _object)
        throws EFapsException
    {

        ConnectionResource rsrc = null;
        try {
            rsrc = Context.getThreadContext().getConnectionResource();
            Statement stmt = null;
            final StringBuilder cmd = new StringBuilder();
            try {
                cmd.append("delete from ").append(_unassignType.getMainTable().getSqlTable()).append(" ").append(
                                "where USERJAASSYSTEM=").append(_jaasSystem.getId()).append(" ").append(
                                "and USERABSTRACTFROM=").append(getId()).append(" ").append("and USERABSTRACTTO=")
                                .append(_object.getId());

                stmt = rsrc.getConnection().createStatement();
                stmt.executeUpdate(cmd.toString());

            } catch (final SQLException e) {
                AbstractUserObject.LOG.error("could not execute '" + cmd.toString()
                        + "' to unassign user object '" + toString()
                        + "' from object '" + _object + "' for JAAS system '" + _jaasSystem + "' ", e);
                throw new EFapsException(getClass(), "unassignFromUserObjectInDb.SQLException", e, cmd.toString(),
                                getName());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (final SQLException e) {
                    AbstractUserObject.LOG.error("Could not close a statement.", e);
                }
            }
            rsrc.commit();
        } finally {
            if ((rsrc != null) && rsrc.isOpened()) {
                rsrc.abort();
            }
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link AbstractUserObject}. The returned AbstractUserObject can be a
     * {@link Role}, {@link Group}, {@link Company}, {@link Consortium}
     * or {@link Person}. It is searched in the given sequence. User is searched last
     * due to the reason that it is the only object that is not always stored
     * in a cache an might produce queries against the DataBase
     *
     * @param _id id to search in the cache
     * @return instance of class {@link AbstractUserObject}
     * @throws EFapsException on error
     */
    public static AbstractUserObject getUserObject(final long _id)
        throws EFapsException
    {
        AbstractUserObject ret = Role.get(_id);
        if (ret == null) {
            ret = Group.get(_id);
        }
        if (ret == null) {
            ret = Company.get(_id);
        }
        if (ret == null) {
            ret = Consortium.get(_id);
        }
        if (ret == null) {
            ret = Person.get(_id);
        }
        return ret;
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link AbstractUserObject}.The returned AbstractUserObject can be a
     * {@link Role}, {@link Group}, {@link Company}, {@link Consortium}
     * or {@link Person}. It is searched in the given sequence. User is searched
     * last due to the reason that it is the only object that is not always stored
     * in a cache an might produce queries against the DataBase
     *
     * @param _name name to search in the cache
     * @return instance of class {@link AbstractUserObject}
     * @throws EFapsException on error
     */
    public static AbstractUserObject getUserObject(final String _name)
        throws EFapsException
    {
        AbstractUserObject ret = Role.get(_name);
        if (ret == null) {
            ret = Group.get(_name);
        }
        if (ret == null) {
            ret = Company.get(_name);
        }
        if (ret == null) {
            ret = Consortium.get(_name);
        }
        if (ret == null) {
            ret = Person.get(_name);
        }
        return ret;
    }

    /**
     * This is the getter method for the instance variable {@link #status}.
     *
     * @return value of instance variable {@link #status}
     */
    public boolean getStatus()
    {
        return this.status;
    }

    /**
     * This is the setter method for the instance variable {@link #status}.
     *
     * @param _status the status to set
     */
    public void setStatus(final boolean _status)
    {
        this.status = _status;
    }

    /**
     * Method to set the status of a UserObject in the eFaps Database.
     *
     * @param _status status to set
     * @throws EFapsException on error
     */
    protected void setStatusInDB(final boolean _status)
        throws EFapsException
    {
        ConnectionResource rsrc = null;
        try {
            final Context context = Context.getThreadContext();
            rsrc = context.getConnectionResource();

            PreparedStatement stmt = null;
            final StringBuilder cmd = new StringBuilder();
            try {

                cmd.append(" update T_USERABSTRACT set STATUS=? where ID=").append(getId());
                stmt = rsrc.getConnection().prepareStatement(cmd.toString());
                stmt.setBoolean(1, _status);
                final int rows = stmt.executeUpdate();
                if (rows == 0) {
                    AbstractUserObject.LOG.error("could not execute '" + cmd.toString()
                            + "' to update status information for person '" + toString() + "'");
                    throw new EFapsException(getClass(), "setStatusInDB.NotUpdated", cmd.toString(), getName());
                }
            } catch (final SQLException e) {
                AbstractUserObject.LOG.error("could not execute '" + cmd.toString()
                        + "' to update status information for person '" + toString() + "'", e);
                throw new EFapsException(getClass(), "setStatusInDB.SQLException", e, cmd.toString(), getName());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (final SQLException e) {
                    throw new EFapsException(getClass(), "setStatusInDB.SQLException", e, cmd.toString(), getName());
                }
            }
            rsrc.commit();
        } finally {
            if ((rsrc != null) && rsrc.isOpened()) {
                rsrc.abort();
            }
        }
    }
}
