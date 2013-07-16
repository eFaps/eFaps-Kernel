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

package org.efaps.admin.datamodel.attributetype;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributevalue.PasswordStore;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.SQLUpdate;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Passwords a stored using the "Java Simplified Encryption" classes that
 * provide an easy way to store passwords using salted Digest with iterations.
 * That means that it is not possible (at least not very likely) to retrieve
 * the Password in clear text. So only the hash values can be used for
 * password comparison.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PasswordType
    extends StringType
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PasswordType.class);

    /**
     * Current Value of this Password.
     */
    private String currentValue;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                           final Attribute _attribute,
                           final Object... _values)
        throws SQLException
    {
        if (_insertUpdate instanceof SQLUpdate) {
            final long id = ((SQLUpdate) _insertUpdate).getId();
            final SQLSelect sel = new SQLSelect();
            sel.column(0, _attribute.getSqlColNames().get(0))
                .from(_attribute.getTable().getSqlTable(), 0)
                .addPart(SQLPart.WHERE)
                .addColumnPart(0, "ID")
                .addPart(SQLPart.EQUAL)
                .addValuePart(id);

            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();

                final Statement stmt = con.getConnection().createStatement();
                final ResultSet rs = stmt.executeQuery(sel.getSQL());
                if (rs.next()) {
                    this.currentValue = rs.getString(1);
                }
                rs.close();
                stmt.close();
                con.commit();
            } catch (final EFapsException e) {
                throw new SQLException(e);
            } finally {
                if (con != null && con.isOpened()) {
                    try {
                        con.abort();
                    } catch (final EFapsException e) {
                        throw new SQLException(e);
                    }
                }
            }
        }
        checkSQLColumnSize(_attribute, 1);
        _insertUpdate.column(_attribute.getSqlColNames().get(0), eval(_values));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String eval(final Object[] _values)
    {
        final PasswordStore pwd = new PasswordStore();
        final String passwd = super.eval(_values);
        try {
            pwd.setNew(passwd, this.currentValue);
        } catch (final EFapsException e) {
            PasswordType.LOG.error("Setting PasswordStore: " + pwd.toString());
        }
        if (PasswordType.LOG.isDebugEnabled()) {
            PasswordType.LOG.debug("Setting PasswordStore: " + pwd.toString());
        }
        return pwd.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
        throws EFapsException
    {
        final PasswordStore pwd = new PasswordStore();
        pwd.read((String) super.readValue(_attribute, _objectList));
        if (PasswordType.LOG.isDebugEnabled()) {
            PasswordType.LOG.debug("Reading PasswordStore: " + pwd.toString());
        }
        return pwd;
    }
}
