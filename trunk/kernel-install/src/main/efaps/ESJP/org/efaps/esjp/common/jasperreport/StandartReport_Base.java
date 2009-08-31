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
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("c3a1f5f8-b263-4ad4-b144-db68437074cc")
@EFapsRevision("$Rev$")
public class StandartReport_Base implements EventExecution
{

    /**
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     * @param _parameter parameter as passed fom the eFaps esjp API
     * @return Return
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        final String name = (String) properties.get("JasperReport");

        final HashMap<String, Object> parameter = new HashMap<String, Object>();
        parameter.put(JRParameter.REPORT_FILE_RESOLVER, new JasperFileResolver());
        parameter.put(JRParameter.REPORT_LOCALE, Context.getThreadContext().getLocale());
        parameter.put(JRParameter.REPORT_RESOURCE_BUNDLE, new EFapsResourceBundle());
        parameter.put("EFAPS_SUBREPORT", new SubReportContainer());

        final SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_Program_JasperReportCompiled");
        query.addWhereExprEqValue("Name", name);
        query.addSelect("OID");
        query.execute();
        Instance instance = null;
        if (query.next()) {
            instance = Instance.get((String) query.get("OID"));
        }
        final Checkout checkout = new Checkout(instance);
        final InputStream iin = checkout.execute();
        try {
            final JasperReport jasperReport = (JasperReport) JRLoader.loadObject(iin);

            final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameter, new EFapsDataSource(
                            jasperReport));

            JasperExportManager.exportReportToPdfFile(jasperPrint, "/Users/janmoxter/Documents/Test.pdf");
        } catch (final JRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new Return();
    }
}

