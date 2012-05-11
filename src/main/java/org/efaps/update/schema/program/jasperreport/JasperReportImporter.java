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

package org.efaps.update.schema.program.jasperreport;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlDigesterFactory;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.efaps.ci.CIAdminProgram;
import org.efaps.update.schema.program.AbstractSourceImporter;
import org.efaps.update.util.InstallationException;
import org.xml.sax.SAXException;

/**
 * Class is used to import a JasperReport into the eFaps DataBase.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JasperReportImporter
    extends AbstractSourceImporter
{
    /**
     * Design of the current report.
     */
    private JasperDesign jasperDesign;

    /**
     * @param _url  url to the file to be imported
     * @throws InstallationException on error
     */
    public JasperReportImporter(final URL _url)
        throws InstallationException
    {
        super(CIAdminProgram.JasperReport, _url);
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
            this.jasperDesign = new JRXmlLoader(JRXmlDigesterFactory.createDigester()).loadXML(newCodeInputStream());
        } catch (final ParserConfigurationException e) {
            throw new InstallationException("source code for " + getUrl() + "could not be parsed", e);
        } catch (final SAXException e) {
            throw new InstallationException("source code for " + getUrl() + "could not parsed", e);
        } catch (final JRException e) {
            throw new InstallationException("source code for " + getUrl() + "could not encoded", e);
        } catch (final UnsupportedEncodingException e) {
            throw new InstallationException("source code for " + getUrl() + "could not encoded", e);
        }
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
    protected String evalRevision()
    {
        return "1";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UUID evalUUID()
    {
        UUID ret = null;
        final JRDesignParameter para = (JRDesignParameter) this.jasperDesign.getParametersMap().get("EFAPS_DEFINITION");
        if (para != null) {
            final String uuid = para.getPropertiesMap().getProperty("UUID");
            if (uuid != null) {
                ret = UUID.fromString(uuid);
            }
        }
        return ret;
    }
}
