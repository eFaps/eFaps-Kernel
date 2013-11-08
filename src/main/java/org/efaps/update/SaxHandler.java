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

package org.efaps.update;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.efaps.update.schema.access.AccessSetUpdate;
import org.efaps.update.schema.access.AccessTypeUpdate;
import org.efaps.update.schema.common.NumberGeneratorUpdate;
import org.efaps.update.schema.common.SystemConfigurationUpdate;
import org.efaps.update.schema.datamodel.DimensionUpdate;
import org.efaps.update.schema.datamodel.SQLTableUpdate;
import org.efaps.update.schema.datamodel.StatusGroupUpdate;
import org.efaps.update.schema.datamodel.TypeUpdate;
import org.efaps.update.schema.db.StoreUpdate;
import org.efaps.update.schema.dbproperty.DBPropertiesUpdate;
import org.efaps.update.schema.help.HelpMenuUpdate;
import org.efaps.update.schema.integration.WebDAVUpdate;
import org.efaps.update.schema.program.BPMImageUpdate;
import org.efaps.update.schema.program.JasperImageUpdate;
import org.efaps.update.schema.program.JasperReportUpdate;
import org.efaps.update.schema.program.WikiImageUpdate;
import org.efaps.update.schema.ui.CommandUpdate;
import org.efaps.update.schema.ui.FormUpdate;
import org.efaps.update.schema.ui.ImageUpdate;
import org.efaps.update.schema.ui.MenuUpdate;
import org.efaps.update.schema.ui.SearchUpdate;
import org.efaps.update.schema.ui.TableUpdate;
import org.efaps.update.schema.user.CompanyUpdate;
import org.efaps.update.schema.user.GroupUpdate;
import org.efaps.update.schema.user.JAASSystemUpdate;
import org.efaps.update.schema.user.RoleUpdate;
import org.efaps.util.EFapsException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class SaxHandler
    extends DefaultHandler
{
    /**
     * Tags used in this Handler.
     */
    private final Stack<String> tag = new Stack<String>();

    /**
     * Map of attributes for this Handler.
     */
    private final Map<String, String> attributes = new HashMap<String, String>();

    /**
     * Has this handler been called.
     */
    private boolean called = false;

    /**
     * Update.
     */
    private IUpdate update = null;

    /**
     * StringtbUIlder used to hold the content.
     */
    private StringBuilder content = null;

    /**
     * Url of the file that is parsed.
     */
    private URL url = null;

    /**
     * @param _url Url of the file to be parsed
     * @return AbstractUpdate for the file
     * @throws SAXException on parse exception
     * @throws IOException on file access error
     */
    public IUpdate parse(final URL _url)
        throws SAXException, IOException
    {
        this.url = _url;

        // einen XML Reader erzeugen
        final XMLReader reader = XMLReaderFactory.createXMLReader();
        // den eigenen Sax Content Handler registrieren
        reader.setContentHandler(this);
        // unsere Beispiel XML Datei parsen

        final URLConnection connection = this.url.openConnection();
        connection.setUseCaches(false);
        final InputStream stream = connection.getInputStream();
        reader.parse(new InputSource(stream));
        stream.close();

        return this.update;
    }

    /**
     * Getter method for instance variable {@link #update}.
     *
     * @return value of instance variable {@link #update}
     */
    public IUpdate getUpdate()
    {
        return this.update;
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     * @param _ch       char
     * @param _start    start index
     * @param _length   length
     * @throws SAXException on error
     */
    @Override
    public void characters(final char[] _ch,
                           final int _start,
                           final int _length)
        throws SAXException
    {

        if (_length > 0) {
            final String contentTmp = new String(_ch, _start, _length);
            if (!this.called && !this.tag.empty()) {
                if (this.content == null) {
                    this.content = new StringBuilder();
                }
                this.content.append(contentTmp);
            }
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     * @param _uri          uri
     * @param _localName    local name
     * @param _qName        qualified name
     * @throws SAXException on error
     */
    @Override
    public void endElement(final String _uri,
                           final String _localName,
                           final String _qName)
        throws SAXException
    {
        if (!this.called) {
            try {
                this.update.readXML(this.tag, this.attributes, (this.content != null)
                                ? this.content.toString().trim()
                                : null);
            } catch (final EFapsException e) {
                throw new SAXException(e);
            }
            this.called = true;
            this.content = null;
        }

        if (!this.tag.isEmpty()) {
            this.tag.pop();
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     * org.xml.sax.Attributes)
     * @param _uri          uri
     * @param _localName    local name
     * @param _qName        qualified Name
     * @param _attributes   Attributes
     * @throws SAXException on error
     */
    @Override
    public void startElement(final String _uri,
                             final String _localName,
                             final String _qName,
                             final Attributes _attributes)
        throws SAXException
    {
        if (this.update != null) {
            if (!this.called && !this.tag.isEmpty()) {
                try {
                    this.update.readXML(this.tag, this.attributes, (this.content != null)
                                    ? this.content.toString().trim()
                                    : null);
                } catch (final EFapsException e) {
                    throw new SAXException(e);
                }
            }
            this.called = false;
            this.content = null;
            this.tag.push(_qName);
            this.attributes.clear();
            for (int i = 0; i < _attributes.getLength(); i++) {
                this.attributes.put(_attributes.getQName(i), _attributes.getValue(i));
            }
        } else if ("access-set".equals(_qName)) {
            this.update = new AccessSetUpdate(this.url);
        } else if ("access-type".equals(_qName)) {
            this.update = new AccessTypeUpdate(this.url);
        } else if ("bpm-image".equals(_qName)) {
            this.update = new BPMImageUpdate(this.url);
        } else if ("common-systemconfiguration".equals(_qName)) {
            this.update = new SystemConfigurationUpdate(this.url);
        } else if ("datamodel-sqltable".equals(_qName)) {
            this.update = new SQLTableUpdate(this.url);
        } else if ("datamodel-type".equals(_qName)) {
            this.update = new TypeUpdate(this.url);
        } else if ("datamodel-dimension".equals(_qName)) {
            this.update = new DimensionUpdate(this.url);
        } else if ("datamodel-statusgroup".equals(_qName)) {
            this.update = new StatusGroupUpdate(this.url);
        } else if ("db-store".equals(_qName)) {
            this.update = new StoreUpdate(this.url);
        } else if ("integration-webdav".equals(_qName)) {
            this.update = new WebDAVUpdate(this.url);
        } else if ("jasperReport".equals(_qName)) {
            this.update = new JasperReportUpdate(this.url);
        } else if ("jasper-image".equals(_qName)) {
            this.update = new JasperImageUpdate(this.url);
        } else if ("numbergenerator".equals(_qName)) {
            this.update = new NumberGeneratorUpdate(this.url);
        } else if ("ui-command".equals(_qName)) {
            this.update = new CommandUpdate(this.url);
        } else if ("ui-form".equals(_qName)) {
            this.update = new FormUpdate(this.url);
        } else if ("ui-image".equals(_qName)) {
            this.update = new ImageUpdate(this.url);
        } else if ("ui-menu".equals(_qName)) {
            this.update = new MenuUpdate(this.url);
        } else if ("ui-search".equals(_qName)) {
            this.update = new SearchUpdate(this.url);
        } else if ("ui-table".equals(_qName)) {
            this.update = new TableUpdate(this.url);
        } else if ("user-company".equals(_qName)) {
            this.update = new CompanyUpdate(this.url);
        } else if ("user-jaassystem".equals(_qName)) {
            this.update = new JAASSystemUpdate(this.url);
        } else if ("user-role".equals(_qName)) {
            this.update = new RoleUpdate(this.url);
        } else if ("user-group".equals(_qName)) {
            this.update = new GroupUpdate(this.url);
        } else if ("import".equals(_qName)) {
            this.update = new ImportUpdate();
        } else if ("dbproperties".equals(_qName)) {
            this.update = new DBPropertiesUpdate(this.url);
        } else if ("help-menu".equals(_qName)) {
            this.update = new HelpMenuUpdate(this.url);
        } else if ("wiki-image".equals(_qName)) {
            this.update = new WikiImageUpdate(this.url);
        } else {
            this.update = new DefaultEmptyUpdate(this.url);
        }
    }
}
