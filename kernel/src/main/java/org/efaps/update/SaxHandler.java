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

package org.efaps.update;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import org.efaps.update.access.AccessSetUpdate;
import org.efaps.update.access.AccessTypeUpdate;
import org.efaps.update.common.SystemConfigurationUpdate;
import org.efaps.update.datamodel.SQLTableUpdate;
import org.efaps.update.datamodel.TypeUpdate;
import org.efaps.update.integration.WebDAVUpdate;
import org.efaps.update.ui.CommandUpdate;
import org.efaps.update.ui.FormUpdate;
import org.efaps.update.ui.ImageUpdate;
import org.efaps.update.ui.MenuUpdate;
import org.efaps.update.ui.SearchUpdate;
import org.efaps.update.ui.TableUpdate;
import org.efaps.update.user.JAASSystemUpdate;
import org.efaps.update.user.RoleUpdate;


/**
 * @author tmo
 * @version $Id$
 */
public class SaxHandler extends DefaultHandler
{

  private final Stack<String> tag = new Stack<String>();

  private final Map<String,String> attributes = new HashMap<String,String>();

  private boolean called = false;

  AbstractUpdate elem = null;

  StringBuilder content = null;

  private URL url = null;

  public SaxHandler()
  {

  }

  public AbstractUpdate parse(final URL _url)
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

    return this.elem;
  }

  @Override
  public void characters(final char[] _ch,
                         final int _start,
                         final int _length)
      throws SAXException
  {

    if (_length > 0) {
      final String content = new String (_ch,_start,_length);
      if (!this.called && !this.tag.empty())  {
        if (this.content == null)  {
          this.content = new StringBuilder();
        }
        this.content.append(content);
      }
    }
  }

  @Override
  public void endElement (final String _uri,
                          final String _localName,
                          final String _qName)
      throws SAXException
  {
    if (!this.called)
    {
      this.elem.readXML(this.tag,
                       this.attributes,
                       (this.content != null) ? this.content.toString().trim() : null);
      this.called = true;
      this.content = null;
    }

    if (!this.tag.isEmpty())  {
      this.tag.pop();
    }
  }

  @Override
  public void startElement(final String _uri,
                           final String _localName,
                           final String _qName,
                           final Attributes _attributes)
      throws SAXException
  {
    if (this.elem != null)  {
      if (!this.called && !this.tag.isEmpty())  {
        this.elem.readXML(this.tag, this.attributes, (this.content != null) ? this.content.toString().trim() : null);

      }
      this.called = false;
      this.content = null;

      this.tag.push(_qName);

      this.attributes.clear();
      for (int i = 0; i < _attributes.getLength() ; i++)  {
        this.attributes.put(_attributes.getQName(i),
                            _attributes.getValue(i));
      }
    } else if ("access-set".equals(_qName))  {
      this.elem = new AccessSetUpdate(this.url);
    } else if ("access-type".equals(_qName))  {
      this.elem = new AccessTypeUpdate(this.url);
    } else if ("common-systemconfiguration".equals(_qName))  {
      this.elem = new SystemConfigurationUpdate(this.url);
    } else if ("datamodel-sqltable".equals(_qName))  {
      this.elem = new SQLTableUpdate(this.url);
    } else if ("datamodel-type".equals(_qName))  {
      this.elem = new TypeUpdate(this.url);
    } else if ("integration-webdav".equals(_qName))  {
      this.elem = new WebDAVUpdate(this.url);
    } else if ("ui-command".equals(_qName))  {
      this.elem = new CommandUpdate(this.url);
    } else if ("ui-form".equals(_qName))  {
      this.elem = new FormUpdate(this.url);
    } else if ("ui-image".equals(_qName))  {
      this.elem = new ImageUpdate(this.url);
    } else if ("ui-menu".equals(_qName))  {
      this.elem = new MenuUpdate(this.url);
    } else if ("ui-search".equals(_qName))  {
      this.elem = new SearchUpdate(this.url);
    } else if ("ui-table".equals(_qName))  {
      this.elem = new TableUpdate(this.url);
    } else if ("user-jaassystem".equals(_qName))  {
      this.elem = new JAASSystemUpdate(this.url);
    } else if ("user-role".equals(_qName))  {
      this.elem = new RoleUpdate(this.url);
} else if ("import".equals(_qName))  {
  this.elem = new TypeUpdate(this.url);
} else if ("dbproperties".equals(_qName))  {
this.elem = new TypeUpdate(this.url);
    } else  {
      throw new SAXException("Unknown XML Tag " + _qName);
    }
  }
}