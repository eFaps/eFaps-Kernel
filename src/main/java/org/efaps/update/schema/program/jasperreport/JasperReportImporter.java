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

package org.efaps.update.schema.program.jasperreport;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.schema.program.AbstractSourceImporter;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.query.QueryExecuterFactory;
import net.sf.jasperreports.engine.xml.JRXmlDigesterFactory;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 * Class is used to import a JasperReport into the eFaps DataBase.
 *
 * @author The eFaps Team
 */
public class JasperReportImporter
    extends AbstractSourceImporter
{
    /** The Constant PROPKEY. */
    public static final String APPPROPKEY = "org.efaps.admin.program.jasper.EFapsApplication";

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JasperReportImporter.class);

    /**
     * Design of the current report.
     */
    private JasperDesign jasperDesign;

    /**
     * Instantiates a new jasper report importer.
     *
     * @param _installFile the install file
     * @throws InstallationException on error
     */
    public JasperReportImporter(final InstallFile _installFile)
        throws InstallationException
    {
        super(CIAdminProgram.JasperReport, _installFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readCode()
        throws InstallationException
    {
        super.readCode();
        try {
            DefaultJasperReportsContext.getInstance().setProperty("net.sf.jasperreports.query.executer.factory.eFaps",
                            FakeQueryExecuterFactory.class.getName());

            this.jasperDesign = new JRXmlLoader(DefaultJasperReportsContext.getInstance(), JRXmlDigesterFactory
                            .createDigester(DefaultJasperReportsContext.getInstance())).loadXML(newCodeInputStream());
        } catch (final ParserConfigurationException e) {
            throw new InstallationException("source code for " + getUrl() + "could not be parsed", e);
        } catch (final SAXException e) {
            throw new InstallationException("source code for " + getUrl() + "could not parsed", e);
        } catch (final JRException e) {
            // the error is very useful for the user so print it to the log
            LOG.error("The file {} cannot be read due to an JRException {}", getUrl(), e);
            throw new InstallationException("source code for " + getUrl() + "throws JRException", e);
        } catch (final UnsupportedEncodingException e) {
            throw new InstallationException("source code for " + getUrl() + "could not encoded", e);
        }
    }

    /**
     * Method to search the Instance which is imported.
     *
     * @return Instance of the imported program
     * @throws InstallationException if search failed
     */
    @Override
    public Instance searchInstance()
        throws InstallationException
    {
        Instance ret = null;
        try {
            // check if type exists. Necessary for first time installations
            if (getCiType().getType() != null && getEFapsUUID() != null) {
                final QueryBuilder queryBldr = new QueryBuilder(getCiType());
                queryBldr.addWhereAttrEqValue(CIAdminProgram.Abstract.UUID, getEFapsUUID().toString());
                final InstanceQuery query = queryBldr.getQuery();
                query.executeWithoutAccessCheck();
                if (query.next()) {
                    ret = query.getCurrentValue();
                }
            }
        } catch (final EFapsException e)  {
            throw new InstallationException("Could not find '" + getCiType() + "' '" + getProgramName() + "'", e);
        }
        return ret == null ? super.searchInstance() : ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String evalProgramName()
    {
        return this.jasperDesign.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String evalApplication()
    {
        return this.jasperDesign.getProperty(APPPROPKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UUID evalUUID()
    {
        return this.jasperDesign.getUUID();
    }

    /**
     * Internal FakeExecuterFactory to be able to set them from esjp.
     */
    @SuppressWarnings("checkstyle:abstractclassname")
    public static class FakeQueryExecuterFactory
        implements QueryExecuterFactory
    {

        @Override
        public JRQueryExecuter createQueryExecuter(final JRDataset _dataset,
                                                   final Map<String, ? extends JRValueParameter> _parameters)
            throws JRException
        {
            return null;
        }

        @Override
        public Object[] getBuiltinParameters()
        {
            return null;
        }

        @Override
        public JRQueryExecuter createQueryExecuter(final JasperReportsContext _jasperReportsContext,
                                                   final JRDataset _dataset,
                                                   final Map<String, ? extends JRValueParameter> _parameters)
            throws JRException
        {
            return null;
        }

        @Override
        public boolean supportsQueryParameterType(final String _className)
        {
            return false;
        }
    }
}
