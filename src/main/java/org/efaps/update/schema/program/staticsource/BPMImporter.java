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
package org.efaps.update.schema.program.staticsource;

import java.io.IOException;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.efaps.ci.CIAdminProgram;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.schema.program.AbstractSourceImporter;
import org.efaps.update.util.InstallationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class BPMImporter
    extends AbstractSourceImporter
{
    /**
     * Possible tagnames for the process tag.
     */
    private static String[] PROCESSTAGNAMES = new String[] {"process", "bpmn2:process"};

    /**
     * Default constructor.
     *
     * @param _installFile the install file
     * @throws InstallationException if BPM importer could not be initialized
     *                               because file from <code>_url</code> could
     *                               not be read
     */
    public BPMImporter(final InstallFile _installFile)
        throws InstallationException
    {
        super(CIAdminProgram.BPM, _installFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String evalProgramName()
        throws InstallationException
    {
        String ret = "";
        try {
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(getUrl().openStream(), AbstractSourceImporter.ENCODING);
            doc.getDocumentElement().normalize();

            for (final String tagName  : BPMImporter.PROCESSTAGNAMES) {
                final NodeList processNodeList = doc.getElementsByTagName(tagName);
                if (processNodeList != null && processNodeList.getLength() > 0) {
                    final Node processNode = processNodeList.item(0);
                    if (processNode.getNodeType() == Node.ELEMENT_NODE) {
                        final Element eElement = (Element) processNode;
                        ret = eElement.getAttribute("id");
                    }
                    break;
                }
            }
        } catch (final ParserConfigurationException e) {
            throw new InstallationException("could not Parse the given URL", e);
        } catch (final SAXException e) {
            throw new InstallationException("could not Parse the given URL", e);
        } catch (final IOException e) {
            throw new InstallationException("could not Read the given URL", e);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UUID evalUUID()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String evalApplication()
        throws InstallationException
    {
        String ret = "";
        try {
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(getUrl().openStream(), AbstractSourceImporter.ENCODING);
            doc.getDocumentElement().normalize();

            for (final String tagName  : BPMImporter.PROCESSTAGNAMES) {
                final NodeList processNodeList = doc.getElementsByTagName(tagName);
                if (processNodeList != null && processNodeList.getLength() > 0) {
                    final Node processNode = processNodeList.item(0);
                    if (processNode.getNodeType() == Node.ELEMENT_NODE) {
                        final Element eElement = (Element) processNode;
                        ret = eElement.getAttribute("tns:application");
                    }
                    break;
                }
            }
        } catch (final ParserConfigurationException e) {
            throw new InstallationException("could not Parse the given URL", e);
        } catch (final SAXException e) {
            throw new InstallationException("could not Parse the given URL", e);
        } catch (final IOException e) {
            throw new InstallationException("could not Read the given URL", e);
        }
        return ret;
    }
}
