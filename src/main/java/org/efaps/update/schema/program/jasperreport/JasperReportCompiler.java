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

package org.efaps.update.schema.program.jasperreport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRProperties;
import net.sf.jasperreports.engine.xml.JRXmlDigester;
import net.sf.jasperreports.engine.xml.JRXmlDigesterFactory;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkin;
import org.efaps.db.Checkout;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.Update;
import org.efaps.update.schema.program.staticsource.AbstractStaticSourceCompiler;
import org.efaps.util.EFapsException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class serves as the compiler for JasperReports.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JasperReportCompiler
    extends AbstractStaticSourceCompiler
{
    /**
     * Stores the list of classpath needed to compile (if needed).
     */
    private final List<String> classPathElements;

    /**
     * Constructor setting the classpath elements.
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
    public void compile() throws EFapsException
    {

        final Map<String, String> compiled = readCompiledSources();

        final List<AbstractSource> allsource = readSources();

        for (final AbstractSource onesource : allsource) {

            if (AbstractStaticSourceCompiler.LOG.isInfoEnabled()) {
                AbstractStaticSourceCompiler.LOG.info("compiling " + onesource.getName());
            }

            final Update update;
            if (compiled.containsKey(onesource.getName())) {
                update = new Update(compiled.get(onesource.getName()));
            } else {
                update = new Insert(Type.get(getClassName4TypeCompiled()));
            }
            update.add("Name", onesource.getName());
            update.add("ProgramLink", "" + onesource.getId());
            update.executeWithoutAccessCheck();
            final Instance instance = update.getInstance();
            update.close();
            compileJasperReport(Instance.get(onesource.getOid()), instance);
        }
    }

    /**
     * Method to compile one JasperReport.
     * @param _instSource   instance of the source
     * @param _instCompiled instance of the compiled source
     * @throws EFapsException on error
     */
    private void compileJasperReport(final Instance _instSource, final Instance _instCompiled) throws EFapsException
    {
        final Checkout checkout = new Checkout(_instSource);
        checkout.preprocess();
        final InputStream source = checkout.execute();
        //make the classPath
        final String sep = System.getProperty("os.name").startsWith("Windows") ? ";" : ":";
        final StringBuilder classPath = new StringBuilder();
        for (final String classPathElement : this.classPathElements) {
            classPath.append(classPathElement).append(sep);
        }
        JRProperties.setProperty(JRProperties.COMPILER_CLASSPATH, classPath.toString());
        JRProperties.setProperty("net.sf.jasperreports.compiler.groovy",
                                 "org.efaps.update.schema.program.jasperreport.JasperGroovyCompiler");

        try {
            final JRXmlDigester digester = JRXmlDigesterFactory.createDigester();
            digester.setValidating(false);
            digester.setNamespaceAware(true);
            digester.setEntityResolver(new EntityResolver() {

                @Override
                public InputSource resolveEntity(final String arg0,
                                                 final String arg1)
                    throws SAXException, IOException
                {
                    final InputSource ret = new InputSource();
                    ret.setByteStream(new FileInputStream("/Users/janmoxter/Documents/jasperreport.xsd"));
                    ret.setEncoding("UTF-8");
                    return ret;
                }
            });
            final JRXmlLoader loader = new JRXmlLoader(digester);

            final JasperDesign jasperDesign = loader.loadXML(source);

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            JasperCompileManager.compileReportToStream(jasperDesign, out);

            final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            final Checkin checkin = new Checkin(_instCompiled);
            checkin.executeWithoutAccessCheck(jasperDesign.getName() + ".jasper", in, in.available());
            out.close();
            in.close();
        } catch (final ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final JRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Not needed in this case.
     * @see org.efaps.admin.program.staticsource.AbstractStaticSourceCompiler#getCompiledString(java.lang.String)
     * @param _oid oid
     * @return null
     */
    @Override
    protected String getCompiledString(final String _oid)
    {
        return null;
    }

   /**
     * {@inheritDoc}
     */
    @Override
    protected EFapsClassNames getClassName4Type()
    {
        return EFapsClassNames.ADMIN_PROGRAM_JASPERREPORT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EFapsClassNames getClassName4Type2Type()
    {
        return EFapsClassNames.ADMIN_PROGRAM_JASPERREPORT2JASPERREPORT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EFapsClassNames getClassName4TypeCompiled()
    {
        return EFapsClassNames.ADMIN_PROGRAM_JASPERREPORTCOMPILED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractSource getNewSource(final String _name, final String _oid, final long _id)
    {
        return new OneJasperReport(_name, _oid, _id);
    }

    /**
     */
    protected class OneJasperReport extends AbstractSource
    {
        /**
         * @param _name name
         * @param _oid  oid
         * @param _id   id
         */
        public OneJasperReport(final String _name, final String _oid, final long _id)
        {
            super(_name, _oid, _id);
        }
    }
}
