/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */


package org.efaps.update;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Context;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public final class AppDependency
{

    /**
     * The mapping of all names to their Dependency.
     */
    private static final Map<String, AppDependency> NAME2APPDEPENDENCY = new  HashMap<String, AppDependency>();

    /**
     * Name of the AppDependency.
     */
    private final String name;

    /**
     * met or not.
     */
    private Boolean met;

    /**
     * @param _name the name of the AppDependency
     */
    private AppDependency(final String _name)
    {
        this.name = _name;
    }

    /**
     * @return true if the dependency is met
     * @throws InstallationException on error
     */
    public boolean isMet()
        throws InstallationException
    {
        if (this.met == null) {
            try {
                if (Context.getDbType().existsView(Context.getThreadContext().getConnection(), "V_ADMINTYPE")
                                && CIAdminCommon.ApplicationVersion.getType() != null) {
                    final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.Application);
                    queryBldr.addWhereAttrEqValue(CIAdminCommon.Application.Name, this.name);
                    final InstanceQuery query = queryBldr.getQuery();
                    this.met = query.executeWithoutAccessCheck().size() > 0;
                } else {
                    this.met = true;
                }
            } catch (final EFapsException e) {
                throw new InstallationException("Latest version could not be found", e);
            } catch (final SQLException e) {
                throw new InstallationException("Latest version could not be found", e);
            } finally {
                if (this.met == null) {
                    this.met = true;
                }
            }
        }
        return this.met;
    }
    /**
     * Getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    public String getName()
    {
        return this.name;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(final Object _object)
    {
        boolean ret;
        if (_object instanceof AppDependency) {
            ret = this.name.equals(((AppDependency) _object).getName());
        } else {
            ret = false;
        }
        return ret;
    }

    /**
     * @param _name name of a AppDependency
     * @return the AppDependency for the given name
     */
    public static AppDependency getAppDependency(final String _name)
    {
        final AppDependency ret;
        if (AppDependency.NAME2APPDEPENDENCY.containsKey(_name)) {
            ret = AppDependency.NAME2APPDEPENDENCY.get(_name);
        } else {
            ret = new AppDependency(_name);
            AppDependency.NAME2APPDEPENDENCY.put(_name, ret);
        }
        return ret;
    }

    /**
     *
     */
    public static void initialise()
    {
        AppDependency.NAME2APPDEPENDENCY.clear();
    }
}
