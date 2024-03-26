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
package org.efaps.update.schema.program.jasperreport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.efaps.admin.program.jasper.JasperUtil;
import org.efaps.ci.CIAdminProgram;
import org.efaps.ci.CIType;
import org.efaps.db.Checkin;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.Update;
import org.efaps.update.schema.program.jasperreport.JasperReportCompiler.OneJasperReport;
import org.efaps.update.schema.program.jasperreport.JasperReportImporter.FakeQueryExecuterFactory;
import org.efaps.update.schema.program.staticsource.AbstractStaticSourceCompiler;
import org.efaps.util.EFapsException;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRReport;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JRCompiler;
import net.sf.jasperreports.engine.design.JasperDesign;

/**
 * Class serves as the compiler for JasperReports.
 *
 * @author The eFaps Team
 */
public class JasperReportCompiler
    extends AbstractStaticSourceCompiler<OneJasperReport>
{

    /**
     * Stores the list of classpath needed to compile (if needed).
     */
    private final List<String> classPathElements;

    /**
     * Constructor setting the classpath elements.
     *
     * @param _classPathElements elemnts for the classpath
     */
    public JasperReportCompiler(final List<String> _classPathElements)
    {
        this.classPathElements = _classPathElements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void compile()
        throws EFapsException
    {
        final List<OneJasperReport> sources = readSources();
        compile(sources.toArray(new OneJasperReport[sources.size()]));
    }

    /**
     * @param _sources source to be compiled
     * @throws EFapsException on error
     */
    public void compile(final OneJasperReport... _sources)
        throws EFapsException
    {
        final Map<String, String> compiled = readCompiledSources();

        for (final OneJasperReport onesource : _sources) {

            if (AbstractStaticSourceCompiler.LOG.isInfoEnabled()) {
                AbstractStaticSourceCompiler.LOG.info("compiling " + onesource.getName());
            }

            final Update update;
            if (compiled.containsKey(onesource.getName())) {
                update = new Update(compiled.get(onesource.getName()));
            } else {
                update = new Insert(getClassName4TypeCompiled());
            }
            update.add("Name", onesource.getName());
            update.add("ProgramLink", "" + onesource.getInstance().getId());
            update.executeWithoutAccessCheck();
            final Instance instance = update.getInstance();
            update.close();
            compileJasperReport(onesource.getInstance(), instance);
        }
    }

    /**
     * Method to compile one JasperReport.
     *
     * @param _instSource instance of the source
     * @param _instCompiled instance of the compiled source
     * @throws EFapsException on error
     */
    private void compileJasperReport(final Instance _instSource,
                                     final Instance _instCompiled)
        throws EFapsException
    {
        // make the classPath
        final String sep = System.getProperty("os.name").startsWith("Windows") ? ";" : ":";
        final StringBuilder classPath = new StringBuilder();
        for (final String classPathElement : this.classPathElements) {
            classPath.append(classPathElement).append(sep);
        }
        final DefaultJasperReportsContext reportContext = DefaultJasperReportsContext.getInstance();
        reportContext.setProperty(JRCompiler.COMPILER_CLASSPATH, classPath.toString());
        reportContext.setProperty("net.sf.jasperreports.compiler.groovy", JasperGroovyCompiler.class.getName());
        reportContext.setProperty("net.sf.jasperreports.query.executer.factory.eFaps",
                        FakeQueryExecuterFactory.class.getName());
        try {
            final JasperDesign jasperDesign = JasperUtil.getJasperDesign(_instSource);

            // the fault value for the language is no information but the used compiler needs a value,
            // therefore it must be set explicitly
            if (jasperDesign.getLanguage() == null) {
                jasperDesign.setLanguage(JRReport.LANGUAGE_JAVA);
            }

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            JasperCompileManager.compileReportToStream(jasperDesign, out);

            final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            final Checkin checkin = new Checkin(_instCompiled);
            checkin.executeWithoutAccessCheck(jasperDesign.getName() + ".jasper", in, in.available());
            out.close();
            in.close();
        } catch (final JRException e) {
            throw new EFapsException(JasperReportCompiler.class, "JRException", e);
        } catch (final IOException e) {
            throw new EFapsException(JasperReportCompiler.class, "IOException", e);
        }
    }

    /**
     * Not needed in this case. {@inheritDoc}
     */
    @Override
    protected String getCompiledString(final Instance _instance)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CIType getClassName4Type()
    {
        return CIAdminProgram.JasperReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CIType getClassName4Type2Type()
    {
        return CIAdminProgram.JasperReport2JasperReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CIType getClassName4TypeCompiled()
    {
        return CIAdminProgram.JasperReportCompiled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OneJasperReport getNewSource(final String _name,
                                       final Instance _instance)
    {
        return new OneJasperReport(_name, _instance);
    }

    /**
     */
    public static class OneJasperReport
        extends AbstractStaticSourceCompiler.AbstractSource
    {
        /**
         * @param _name name
         * @param _instance Instance
         */
        public OneJasperReport(final String _name,
                               final Instance _instance)
        {
            super(_name, _instance);
        }
    }
}
