/*
 * Copyright 2003 - 2013 The eFaps Team
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

import javax.xml.parsers.ParserConfigurationException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlDigesterFactory;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.commons.digester.Digester;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Util class used for jaspereport and dynamic jasper due to massive problems
 * with the used classloaders and diggesters.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class JasperUtil
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
            JasperUtil.LOG.debug("Loading JasperDesign for :{}", _instance);
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
}
