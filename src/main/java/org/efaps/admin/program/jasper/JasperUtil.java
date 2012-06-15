/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.admin.program.jasper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRCompiler;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignGroup;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRProperties;
import net.sf.jasperreports.engine.xml.JRXmlDigesterFactory;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.commons.digester.Digester;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ar.com.fdvs.dj.core.DJDefaultScriptlet;
import ar.com.fdvs.dj.core.DJJRDesignHelper;
import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.LayoutManager;
import ar.com.fdvs.dj.core.registration.ColumnRegistrationManager;
import ar.com.fdvs.dj.core.registration.DJGroupRegistrationManager;
import ar.com.fdvs.dj.core.registration.DJGroupVariableDefRegistrationManager;
import ar.com.fdvs.dj.domain.ColumnProperty;
import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DynamicJasperDesign;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.DynamicReportOptions;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.entities.DJGroup;
import ar.com.fdvs.dj.domain.entities.DJGroupVariableDef;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import ar.com.fdvs.dj.domain.entities.columns.PercentageColumn;
import ar.com.fdvs.dj.util.LayoutUtils;

/**
 * Util class used for jaspereport and dynamic jasper due to massive problems
 * with the used classloaders and diggesters.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class JasperUtil
    extends DynamicJasperHelper
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JasperUtil.class);

    /**
     * Singelton util class.
     */
    private JasperUtil()
    {
    }

    /**
     * Get a JasperDesign for an instance.
     *
     * @param _instance Instance the JasperDesign is wanted for
     * @return JasperDesign
     * @throws EFapsException on error
     */
    public static JasperDesign getJasperDesign(final Instance _instance)
        throws EFapsException
    {
        final Checkout checkout = new Checkout(_instance);
        final InputStream source = checkout.execute();
        JasperDesign jasperDesign = null;
        try {
            final Digester digester = new Digester();
            JRXmlDigesterFactory.configureDigester(digester);
            final JRXmlLoader loader = new JRXmlLoader(digester);
            jasperDesign = loader.loadXML(source);
        } catch (final ParserConfigurationException e) {
            throw new EFapsException(JasperUtil.class, "getJasperDesign", e);
        } catch (final SAXException e) {
            throw new EFapsException(JasperUtil.class, "getJasperDesign", e);
        } catch (final JRException e) {
            throw new EFapsException(JasperUtil.class, "getJasperDesign", e);
        }
        return jasperDesign;
    }

    /**
     * Create a jasperprint with a template. The method is basically the same as
     * the method with the same name in <code>DynamicJasperHelper</code>. Due to
     * the problem that the templates can only be passed as file url this
     * workaround was necessary.
     *
     * @param _dynReport DynamicReport
     * @param _layoutManager LayoutManager
     * @param _dataSource JRDataSource
     * @param _parameters Map
     * @param _template JasperDesign
     * @return JasperPrint
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public static JasperPrint generateJasperPrint(final DynamicReport _dynReport,
                                                  final LayoutManager _layoutManager,
                                                  final JRDataSource _dataSource,
                                                  final Map<String, Object> _parameters,
                                                  final JasperDesign _template)
        throws EFapsException
    {

        JasperPrint jp = null;
        try {
            Map<String, Object> parameters;
            if (_parameters == null) {
                parameters = new HashMap<String, Object>();
            } else {
                parameters = _parameters;
            }
            DynamicJasperHelper.visitSubreports(_dynReport, parameters);
            DynamicJasperHelper.compileOrLoadSubreports(_dynReport, parameters, "");

            final DynamicJasperDesign jd = JasperUtil.generateJasperDesign(_dynReport, _template);
            JasperUtil.registerEntities(jd, _dynReport, _layoutManager);

            DynamicJasperHelper.registerParams(jd, parameters);

            _layoutManager.applyLayout(jd, _dynReport);
            JRProperties.setProperty(JRCompiler.COMPILER_PREFIX, "ar.com.fdvs.dj.util.DJJRJdtCompiler");
            final JasperReport jr = JasperCompileManager.compileReport(jd);
            parameters.putAll(jd.getParametersWithValues());

            jp = JasperFillManager.fillReport(jr, parameters, _dataSource);
        } catch (final JRException e) {
            throw new EFapsException(JasperUtil.class, "generateJasperPrint", e);
        }
        return jp;
    }

    /**
     * Helper method for
     * {@linkplain JasperUtil#generateJasperPrint(DynamicReport, LayoutManager, JRDataSource, Map, JasperDesign)}.
     * @param _dynReport    DynamicReport
     * @param _template     JasperDesign
     * @return DynamicJasperDesign
     */
    protected static DynamicJasperDesign generateJasperDesign(final DynamicReport _dynReport,
                                                              final JasperDesign _template)
    {
        DynamicJasperDesign jd = null;
        if (_template != null) {
            jd = DJJRDesignHelper.downCast(_template, _dynReport);
            JasperUtil.populateReportOptionsFromDesign(jd, _dynReport);
        } else {
            jd = DJJRDesignHelper.getNewDesign(_dynReport);
        }
        jd.setScriptletClass(DJDefaultScriptlet.class.getName());
        DynamicJasperHelper.registerParameters(jd, _dynReport);
        return jd;
    }

    /**
     * Helper method for
     * {@linkplain JasperUtil#generateJasperPrint(DynamicReport, LayoutManager, JRDataSource, Map, JasperDesign)}.
     * @param _dynDesign    DynamicJasperDesign
     * @param _dynReport     DynamicReport
     *
     */
    protected static void populateReportOptionsFromDesign(final DynamicJasperDesign _dynDesign,
                                                          final DynamicReport _dynReport)
    {
        final DynamicReportOptions options = _dynReport.getOptions();

        options.setBottomMargin(new Integer(_dynDesign.getBottomMargin()));
        options.setTopMargin(new Integer(_dynDesign.getTopMargin()));
        options.setLeftMargin(new Integer(_dynDesign.getLeftMargin()));
        options.setRightMargin(new Integer(_dynDesign.getRightMargin()));

        options.setColumnSpace(new Integer(_dynDesign.getColumnSpacing()));
        options.setColumnsPerPage(new Integer(_dynDesign.getColumnCount()));

        options.setPage(new Page(_dynDesign.getPageHeight(), _dynDesign.getPageWidth()));

        if (_dynReport.getQuery() != null) {
            final JRDesignQuery query = new JRDesignQuery();
            query.setText(_dynReport.getQuery().getText());
            query.setLanguage(_dynReport.getQuery().getLanguage());
            _dynDesign.setQuery(query);
        }
        if (_dynReport.getReportName() != null) {
            _dynDesign.setName(_dynReport.getReportName());
        }
    }

    /**
     * Helper method for
     * {@linkplain JasperUtil#generateJasperPrint(DynamicReport, LayoutManager, JRDataSource, Map, JasperDesign)}.
     * @param _dynDesign     DynamicJasperDesign
     * @param _dynReport     DynamicReport
     * @param _layoutManager LayoutManager
     */
    @SuppressWarnings("unchecked")
    private static void registerEntities(final DynamicJasperDesign _dynDesign,
                                         final DynamicReport _dynReport,
                                         final LayoutManager _layoutManager)
    {
        final ColumnRegistrationManager columnRegistrationManager = new ColumnRegistrationManager(_dynDesign,
                        _dynReport, _layoutManager);
        columnRegistrationManager.registerEntities(_dynReport.getColumns());

        final DJGroupRegistrationManager djGroupRegistrationManager = new DJGroupRegistrationManager(_dynDesign,
                        _dynReport, _layoutManager);
        djGroupRegistrationManager.registerEntities(_dynReport.getColumnsGroups());

        for (final Iterator<?> iterator = _dynReport.getColumns().iterator(); iterator.hasNext();) {
            final AbstractColumn column = (AbstractColumn) iterator.next();
            if (column instanceof PercentageColumn) {
                final PercentageColumn percentageColumn = (PercentageColumn) column;
                for (final Iterator<?> iterator2 = _dynReport.getColumnsGroups().iterator(); iterator2.hasNext();) {
                    final DJGroup djGroup = (DJGroup) iterator2.next();
                    final JRDesignGroup jrGroup = LayoutUtils.getJRDesignGroup(_dynDesign, _layoutManager, djGroup);
                    final DJGroupVariableDefRegistrationManager variablesRM = new DJGroupVariableDefRegistrationManager(
                                    _dynDesign, _dynReport, _layoutManager, jrGroup);
                    final DJGroupVariableDef variable = new DJGroupVariableDef(
                                    percentageColumn.getGroupVariableName(djGroup),
                                    percentageColumn.getPercentageColumn(), DJCalculation.SUM);
                    final Collection<DJGroupVariableDef> entities = new ArrayList<DJGroupVariableDef>();
                    entities.add(variable);
                    variablesRM.registerEntities(entities);
                }
            }
        }

        for (final Iterator<?> iter = _dynReport.getFields().iterator(); iter.hasNext();) {
            final ColumnProperty element = (ColumnProperty) iter.next();
            final JRDesignField field = new JRDesignField();
            field.setValueClassName(element.getValueClassName());
            field.setName(element.getProperty());
            try {
                _dynDesign.addField(field);
            } catch (final JRException e) {
                JasperUtil.LOG.warn(e.getMessage());
            }
        }

        final Locale locale = _dynReport.getReportLocale() == null ? Locale.getDefault() : _dynReport.getReportLocale();
        if (JasperUtil.LOG.isDebugEnabled()) {
            JasperUtil.LOG.debug("Requested Locale = " + _dynReport.getReportLocale() + ", Locale to use: " + locale);
        }
        _dynDesign.getParametersWithValues().put(JRParameter.REPORT_LOCALE, locale);
    }
}
