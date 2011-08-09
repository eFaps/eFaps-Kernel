/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.db.print;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.AbstractPrintQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * Select Part for <code>linkfrom[TYPERNAME#ATTRIBUTENAME]</code>.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LinkFromSelect
    extends AbstractPrintQuery
{
    /**
     * Name of the Attribute the link to is based on.
     */
    private final String attrName;

    /**
     * Type the {@link #attrName} belongs to.
     */
    private final Type type;

    /**
     * Did this query return a result.
     */
    private boolean hasResult;

    /**
     * @param _linkFrom linkfrom element of the query
     */
    public LinkFromSelect(final String _linkFrom)
    {
        final String[] linkfrom = _linkFrom.split("#");
        this.type = Type.get(linkfrom[0]);
        this.attrName = linkfrom[1];
        final OneSelect onsel = new OneSelect(this, _linkFrom);
        addOneSelect(onsel);
        onsel.setFromSelect(this);
        onsel.getSelectParts().add(new ISelectPart() {

            public Type getType()
            {
                return LinkFromSelect.this.type;
            }

            public int join(final OneSelect _oneselect,
                            final SQLSelect _select,
                            final int _relIndex)
            {
                // nothing to join here
                return 0;
            }

            @Override
            public void addObject(final ResultSet _rs)
                throws SQLException
            {
                // no objects must be added
            }

            @Override
            public Object getObject()
            {
                return null;
            }

            @Override
            public void add2Where(final OneSelect _oneselect,
                                  final SQLSelect _select)
            {
                // no clause must be added
            }
        });
    }

    /**
     * Getter method for instance variable {@link #hasResult}.
     *
     * @return value of instance variable {@link #hasResult}
     */
    public boolean hasResult()
    {
        return this.hasResult;
    }

    /**
     * Execute the from select for the given instance.
     * @param _onesel instance
     * @throws EFapsException on error
     * @return true if statement didi return values, else false
     */
    public boolean execute(final OneSelect _onesel)
        throws EFapsException
    {
        this.hasResult = executeOneCompleteStmt(createSQLStatement(_onesel), getAllSelects());
        return this.hasResult;
    }

    /**
     * Method to create on Statement out of the different parts.
     *
     * @param _parentOnesel instance
     * @return String containing the SQL statement
     * @throws EFapsException on error
     */
    private String createSQLStatement(final OneSelect _parentOnesel)
        throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(this.attrName);

        final SQLSelect select = new SQLSelect()
                .column(0, "ID")
                .column(0, attr.getSqlColNames().get(0))
                .from(this.type.getMainTable().getSqlTable(), 0);

        // on a from  select only one table is the base
        getAllSelects().get(0).append2SQLFrom(select);

        int colIndex = select.getColumns().size() + 1;

        for (final OneSelect oneSel : getAllSelects()) {
            colIndex += oneSel.append2SQLSelect(select, colIndex);
        }
        select.addPart(SQLPart.WHERE)
            .addColumnPart(0, attr.getSqlColNames().get(0))
            .addPart(SQLPart.IN).addPart(SQLPart.PARENTHESIS_OPEN);

        if (_parentOnesel.isMultiple()) {
            boolean first = true;
            final List<?> ids = (List<?>) _parentOnesel.getObject();
            for (final Object id : ids) {
                if (first) {
                    first = false;
                } else {
                    select.addPart(SQLPart.COMMA);
                }
                select.addValuePart(id);
            }
        } else {
            select.addValuePart(_parentOnesel.getObject());
        }
        select.addPart(SQLPart.PARENTHESIS_CLOSE);

        _parentOnesel.setValueSelect(null);

        // in a subquery the type must also be set
        if (this.type.getMainTable().getSqlColType() != null) {
            select.addPart(SQLPart.AND)
                .addColumnPart(0, this.type.getMainTable().getSqlColType())
                .addPart(SQLPart.IN).addPart(SQLPart.PARENTHESIS_OPEN);
            boolean first = true;
            if (this.type.isAbstract()) {
                for (final Type atype : getAllChildTypes(this.type)) {
                    if (first) {
                        first = false;
                    } else {
                        select.addPart(SQLPart.COMMA);
                    }
                    select.addValuePart(atype.getId());
                }
            } else {
                select.addValuePart(this.type.getId());
            }
            select.addPart(SQLPart.PARENTHESIS_CLOSE);
        }

        if (AbstractPrintQuery.LOG.isDebugEnabled()) {
            AbstractPrintQuery.LOG.debug(select.getSQL());
        }
        return select.getSQL();
    }

    /**
     * Recursive method to get all child types for a type.
     * @param _parent parent type
     * @return list of all child types
     */
    private List<Type> getAllChildTypes(final Type _parent)
    {
        final List<Type> ret = new ArrayList<Type>();
        for (final Type child : _parent.getChildTypes()) {
            ret.addAll(getAllChildTypes(child));
            ret.add(child);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean executeOneCompleteStmt(final String _complStmt,
                                             final List<OneSelect> _oneSelects)
        throws EFapsException
    {
        boolean ret = false;
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();

            if (AbstractPrintQuery.LOG.isDebugEnabled()) {
                AbstractPrintQuery.LOG.debug(_complStmt.toString());
            }

            final Statement stmt = con.getConnection().createStatement();

            final ResultSet rs = stmt.executeQuery(_complStmt.toString());

            while (rs.next()) {
                for (final OneSelect onesel : _oneSelects) {
                    onesel.addObject(rs);
                }
                ret = true;
            }

            rs.close();
            stmt.close();
            con.commit();
        } catch (final SQLException e) {
            throw new EFapsException(InstanceQuery.class, "executeOneCompleteStmt", e);
        } finally {
            if (con != null && con.isOpened()) {
                con.abort();
            }
        }
        return ret;
    }

    /**
     * Getter method for instance variable {@link #oneSelect}.
     *
     * @return value of instance variable {@link #oneSelect}
     */
    public OneSelect getMainOneSelect()
    {
        return getAllSelects().get(0);
    }

    /**
     * Returns the {@link #type} for which the attribute {@link #attrName}
     * belongs to.
     *
     * @return type
     * @see #type
     */
    public Type getType()
    {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instance getCurrentInstance()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Instance> getInstanceList()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getMainType()
    {
        return null;
    }
}
