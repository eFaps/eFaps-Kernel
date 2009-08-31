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

package org.efaps.esjp.common.jasperreport;

import java.io.InputStream;
import java.util.HashMap;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("ef631aef-45d8-4192-90c9-56898175228d")
@EFapsRevision("$Rev$")
abstract class SubReportContainer_Base extends HashMap<String, JRDataSource>
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     * @param _key key to the JRDataSource
     * @return JRDataSource
     */
    @Override
    public JRDataSource get(final Object _key)
    {
        JRDataSource ret = super.get(_key);
        if (ret == null) {
            try {
                final SearchQuery query = new SearchQuery();
                query.setQueryTypes("Admin_Program_JasperReportCompiled");
                query.addWhereExprEqValue("Name", (String) _key);
                query.addSelect("OID");
                query.execute();
                Instance instance = null;
                if (query.next()) {
                    instance = Instance.get((String) query.get("OID"));
                }
                final Checkout checkout = new Checkout(instance);
                final InputStream iin = checkout.execute();
                final JasperReport jasperReport = (JasperReport) JRLoader.loadObject(iin);
                ret = new EFapsDataSource(jasperReport);
                super.put((String) _key, ret);
            } catch (final JRException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final EFapsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return ret;
    }
}
