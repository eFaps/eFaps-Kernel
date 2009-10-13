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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.util.JRLoader;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
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
public abstract class StandartReport_Base implements EventExecution
{

    /**
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     * @param _parameter parameter as passed fom the eFaps esjp API
     * @return Return
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {
        final Return ret = new Return();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        final String name = (String) properties.get("JasperReport");
        final String dataSourceClass = (String) properties.get("DataSourceClass");

        final HashMap<String, Object> parameter = new HashMap<String, Object>();
        parameter.put(JRParameter.REPORT_FILE_RESOLVER, new JasperFileResolver());
        parameter.put(JRParameter.REPORT_LOCALE, Context.getThreadContext().getLocale());
        parameter.put(JRParameter.REPORT_RESOURCE_BUNDLE, new EFapsResourceBundle());
        parameter.put("EFAPS_SUBREPORT", new SubReportContainer(_parameter));

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
            JRDataSource dataSource;
            if (dataSourceClass != null) {
                final Class<?> clazz = Class.forName(dataSourceClass);
                final Method method = clazz.getMethod("init", new Class[] { JasperReport.class, Parameter.class });
                dataSource = (JRDataSource) clazz.newInstance();
                method.invoke(dataSource, jasperReport, _parameter);
            } else {
                dataSource = new EFapsDataSource();
                ((EFapsDataSource) dataSource).init(jasperReport, _parameter);
            }
            final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameter, dataSource);

            final String mime = (String) properties.get("Mime");

            ret.put(ReturnValues.VALUES, getFile(jasperPrint, mime));
            ret.put(ReturnValues.TRUE, true);

        } catch (final ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final JRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Method to get the File
     * @param jasperPrint jasperprint the file will be created for
     * @param mime          mimetype of the file, default pdf
     * @return  File
     * @throws IOException on error
     * @throws JRException on error
     */
    protected File getFile(final JasperPrint jasperPrint, final String mime) throws IOException, JRException {
        File file = null;
        if ("pdf".equalsIgnoreCase(mime) || mime == null) {
            file = File.createTempFile("PDF", ".pdf");
            final FileOutputStream os = new FileOutputStream(file);
            JasperExportManager.exportReportToPdfStream(jasperPrint, os);
            os.close();
        } else if ("odt".equalsIgnoreCase(mime)) {
            file = File.createTempFile("ODT", ".odt");
            final FileOutputStream os = new FileOutputStream(file);
            final JROdtExporter exporter = new JROdtExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.exportReport();
            os.close();
        } else if ("ods".equalsIgnoreCase(mime)) {
            file = File.createTempFile("ODS", ".ods");
            final FileOutputStream os = new FileOutputStream(file);
            final JROdsExporter exporter = new JROdsExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.exportReport();
            os.close();
        } else if ("rtf".equalsIgnoreCase(mime)) {
            file = File.createTempFile("RTF", ".rtf");
            final FileOutputStream os = new FileOutputStream(file);
            final JRRtfExporter exporter = new JRRtfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.exportReport();
            os.close();
        } else if ("docx".equalsIgnoreCase(mime)) {
            file = File.createTempFile("DOCX", ".docx");
            final FileOutputStream os = new FileOutputStream(file);
            final JRDocxExporter exporter = new JRDocxExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.exportReport();
            os.close();
        } else if ("txt".equalsIgnoreCase(mime)) {
            file = File.createTempFile("TXT", ".txt");
            final FileOutputStream os = new FileOutputStream(file);
            final JRTextExporter exporter = new JRTextExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT , new Integer(10));
            exporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH , new Integer(6));
            exporter.exportReport();
            os.close();
        }
        return file;
    }
}

