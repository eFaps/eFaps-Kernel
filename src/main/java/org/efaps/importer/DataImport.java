/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.importer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Class which contains the method to launch the import of Data into a efaps
 * connected Database.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DataImport
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataImport.class);

    /**
     * Contains the root object which is the base for all other objects.
     */
    private RootObject root = null;

    /**
     * default constructor.
     */
    public DataImport()
    {
        super();
    }

    /**
     * @param _xml    name and path of the XML file
     * @throws MalformedURLException if <code>_xml</code> could not be
     *                               converted into an URL
     * @see #readFile(File)
     */
    public void readFile(final String _xml)
        throws MalformedURLException
    {
        readFile(new File(_xml));
    }

    /**
     * @param _xml    name and path of the XML file
     * @throws MalformedURLException if <code>_xml</code> could not be
     *                               converted into an URL
     * @see #readFile(URL)
     */
    public void readFile(final File _xml)
        throws MalformedURLException
    {
        readFile(_xml.toURI().toURL());
    }

    /**
     * Method that uses the {@link org.apache.commons.digester.Digester} to
     * read the objects from the given XML-File an build the java-Objects in a
     * parent-child hierarchy.
     *
     * @param _url    URL to the XML-File
     * @return data import instance
     */
    public static DataImport readFile(final URL _url)
    {
        DataImport ret = new DataImport();
        try {
            final Digester digester = new Digester();
            digester.setValidating(false);

            digester.addObjectCreate("import", RootObject.class);

            final String def = "import/definition";

            // Read the Definitions
            digester.addCallMethod(def + "/date", "setDateFormat", 1);
            digester.addCallParam(def + "/date", 0, "format");

            // Read OrderObject
            digester.addFactoryCreate(def + "/order", new OrderObjectBuilder(), false);
            digester.addCallMethod(def + "/order/attribute",
                    "addAttribute", 3, new Class[] { Integer.class, String.class, String.class });
            digester.addCallParam(def + "/order/attribute", 0, "index");
            digester.addCallParam(def + "/order/attribute", 1, "name");
            digester.addCallParam(def + "/order/attribute", 2, "criteria");
            digester.addSetNext(def + "/order", "addOrder", "org.efaps.importer.OrderObject");

            // read default object
            digester.addObjectCreate(def + "/default", DefaultObject.class);
            digester.addCallMethod(def + "/default", "addDefault", 3);
            digester.addCallParam(def + "/default", 0, "type");
            digester.addCallParam(def + "/default", 1, "name");
            digester.addCallParam(def + "/default", 2);

            digester.addObjectCreate(def + "/default/linkattribute", ForeignObject.class);
            digester.addCallMethod(def + "/default/linkattribute", "setLinkAttribute", 3);
            digester.addCallParam(def + "/default/linkattribute", 0, "name");
            digester.addCallParam(def + "/default/linkattribute", 1, "type");
            digester.addCallParam(def + "/default/linkattribute", 2, "select");

            digester.addCallMethod(def + "/default/linkattribute/queryattribute", "addAttribute", 2);
            digester.addCallParam(def + "/default/linkattribute/queryattribute", 0, "name");
            digester.addCallParam(def + "/default/linkattribute/queryattribute", 1);

            digester.addSetNext(def + "/default/linkattribute", "addLink", "org.efaps.importer.ForeignObject");

            // create the objects
            digester.addFactoryCreate("*/object", new InsertObjectBuilder(), false);

            digester.addCallMethod("*/object/attribute", "addAttribute", 3);
            digester.addCallParam("*/object/attribute", 0, "name");
            digester.addCallParam("*/object/attribute", 1);
            digester.addCallParam("*/object/attribute", 2, "unique");

            digester.addCallMethod("*/object/file", "setCheckinObject", 2);
            digester.addCallParam("*/object/file", 0, "name");
            digester.addCallParam("*/object/file", 1, "url");

            digester.addCallMethod("*/object/parentattribute", "setParentAttribute", 2);
            digester.addCallParam("*/object/parentattribute", 0, "name");
            digester.addCallParam("*/object/parentattribute", 1, "unique");

            digester.addCallMethod("*/object/linkattribute", "addUniqueAttribute", 2);
            digester.addCallParam("*/object/linkattribute", 0, "unique");
            digester.addCallParam("*/object/linkattribute", 1, "name");

            digester.addSetNext("*/object", "addChild", "org.efaps.importer.InsertObject");

            digester.addObjectCreate("*/object/linkattribute", ForeignObject.class);
            digester.addCallMethod("*/object/linkattribute", "setLinkAttribute", 3);
            digester.addCallParam("*/object/linkattribute", 0, "name");
            digester.addCallParam("*/object/linkattribute", 1, "type");
            digester.addCallParam("*/object/linkattribute", 2, "select");

            digester.addCallMethod("*/object/linkattribute/queryattribute", "addAttribute", 2);
            digester.addCallParam("*/object/linkattribute/queryattribute", 0, "name");
            digester.addCallParam("*/object/linkattribute/queryattribute", 1);

            digester.addSetNext("*/object/linkattribute", "addLink", "org.efaps.importer.ForeignObject");

            ret.root = (RootObject) digester.parse(_url);

            if (!ret.hasData()) {
                ret = null;
            }

        } catch (final IOException e) {
            DataImport.LOG.error(_url.toString() + " is not readable", e);
        } catch (final SAXException e) {
            DataImport.LOG.error(_url.toString() + " seems to be invalide XML", e);
        }
        return ret;
    }

    /**
     * Starts the insertion of the objects into eFaps.
     */
    public void updateInDB()
    {
        if (hasData()) {
            this.root.dbAddChilds();
        }
    }

    /**
     * Has the root received data from the digester that must be inserted?
     *
     * @return <i>true</i> if there is Data; otherwise <i>false</i>
     */
    public boolean hasData()
    {
        return this.root != null;
    }
}
