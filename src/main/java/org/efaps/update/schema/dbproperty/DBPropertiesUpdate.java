/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.update.schema.dbproperty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.jexl.JexlContext;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.update.IUpdate;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for importing or updating of Properties from a properties-file into the
 * Database for use as eFaps-Admin_Properties.<br>
 * The import depends on the UUID of the Bundle. That means all Keys of the
 * Properties must be unique within a Bundle. Therefore the import will update a
 * key, if it is already existing inside this bundle. The Bundle will always be
 * identified by the UUID and not by the name.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DBPropertiesUpdate implements IUpdate
{
    /**
     * name for the Type.
     */
    private static final String TYPE_PROPERTIES = "Admin_Common_DBProperties";

    /**
     * name for the Type.
     */
    private static final String TYPE_PROPERTIES_BUNDLE = "Admin_Common_DBPropertiesBundle";

    /**
     * name for the Type.
     */
    private static final String TYPE_PROPERTIES_LOCAL = "Admin_Common_DBPropertiesLocal";

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DBPropertiesUpdate.class);

    /**
     * the name of the Bundle.
     */
    private String bundlename;

    /**
     * the UUID of the Bundle.
     */
    private String bundeluuid;

    /**
     * the ID of the Bundle.
     */
    private String bundleid;

    /**
     * Sequence of the Bundle.
     */
    private String bundlesequence;

    /**
     * root of the XML-file to be imported.
     */
    private final String root;

    /**
     * List of all Resources in this Properties.
     */
    private final List<Resource> resources = new ArrayList<Resource>();

    /**
     * Name of the application.
     */
    private String fileApplication;

    /**
     * Current read source.
     * @see #readXML(List, Map, String)
     */
    private Resource curResource;

    /**
     * @param _url url for the file
     */
    public DBPropertiesUpdate(final URL _url)
    {
        final String urlStr = _url.toString();
        this.root = urlStr.substring(0, urlStr.lastIndexOf(File.separator) + 1);
    }

    /**
     * find out the Id of the language used for this properties.
     * @param _language Language
     * @return ID of the Language
     */
    private String getLanguageId(final String _language)
    {
        String ret = null;
        final SearchQuery query = new SearchQuery();
        try {
            query.setQueryTypes("Admin_Language");
            query.addSelect("ID");
            query.addWhereExprEqValue("Language", _language);
            query.executeWithoutAccessCheck();
            if (query.next()) {
                ret = query.get("ID").toString();
            } else {
                ret = insertNewLanguage(_language);
            }
            query.close();

        } catch (final EFapsException e) {
            DBPropertiesUpdate.LOG.error("getLanguageId()", e);
        }
        return ret;
    }

    /**
     * inserts a new language into the Database.
     *
     * @param _language language to be inserted
     * @return ID of the new language
     */
    private String insertNewLanguage(final String _language)
    {
        String ret = null;
        try {
            final Insert insert = new Insert("Admin_Language");
            insert.add("Language", _language);
            insert.executeWithoutAccessCheck();
            ret = insert.getId();
            insert.close();
        } catch (final EFapsException e) {
            DBPropertiesUpdate.LOG.error("insertNewLanguage()", e);
        } catch (final Exception e) {
            DBPropertiesUpdate.LOG.error("insertNewLanguage()", e);
        }
        return ret;
    }

    /**
     * Insert a new Bundle into the Database.
     *
     * @return ID of the new Bundle
     */
    private String insertNewBundle()
    {
        String ret = null;
        try {
            final Insert insert = new Insert(DBPropertiesUpdate.TYPE_PROPERTIES_BUNDLE);
            insert.add("Name", this.bundlename);
            insert.add("UUID", this.bundeluuid);
            insert.add("Sequence", this.bundlesequence);
            insert.executeWithoutAccessCheck();

            ret = insert.getId();
            insert.close();

        } catch (final EFapsException e) {
            DBPropertiesUpdate.LOG.error("insertNewBundle()", e);
        } catch (final Exception e) {
            DBPropertiesUpdate.LOG.error("insertNewBundle()", e);
        }
        return ret;
    }


    /**
     * Import Properties from a Properties-File as default, if the key is
     * already existing, the default will be replaced with the new default.
     *
     * @param _url Complete Path/Name of the property file to import
     */
    private void importFromProperties(final URL _url)
    {
        try {
            final InputStream propInFile = _url.openStream();
            final Properties props = new Properties();
            props.load(propInFile);
            final Iterator<Entry<Object, Object>> iter = props.entrySet().iterator();

            while (iter.hasNext()) {
                final Entry<Object, Object> element = iter.next();
                final String oid = getExistingKey(element.getKey().toString());
                if (oid == null) {
                    insertNewProp(element.getKey().toString(), element.getValue().toString());
                } else {
                    updateDefault(oid, element.getValue().toString());
                }
            }

        } catch (final IOException e) {
            DBPropertiesUpdate.LOG.error("ImportFromProperties() - I/O failed.", e);
        }
    }

    /**
     * Import Properties from a Properties-File as language-specific value, if
     * the key is not existing, a new default(=value) will also be created. If
     * the language is not existing it will be created also.
     *
     * @param _url Complete Path/Name of the File to import
     * @param _language Language to use for the Import
     */
    private void importFromProperties(final URL _url,
                                      final String _language)
    {
        String propOID;
        String propID;
        String localOID;
        try {

            final InputStream propInFile = _url.openStream();
            final Properties props = new Properties();
            props.load(propInFile);
            final Iterator<Entry<Object, Object>> iter = props.entrySet().iterator();

            while (iter.hasNext()) {
                final Entry<Object, Object> element = iter.next();
                propOID = getExistingKey(element.getKey().toString());

                if (propOID == null) {
                    propID = insertNewProp(element.getKey().toString(), element.getValue().toString());
                } else {
                    propID = ((Long) Instance.get(propOID).getId()).toString();
                }

                localOID = getExistingLocale(propID, _language);
                if (localOID == null) {
                    insertNewLocal(propID, element.getValue().toString(), _language);
                } else {
                    updateLocale(localOID, element.getValue().toString());
                }
            }

        } catch (final IOException e) {
            DBPropertiesUpdate.LOG.error("ImportFromProperties() - I/O failed.", e);
        }
    }

    /**
     * Is a localized value already existing.
     *
     * @param _propertyid   ID of the Property, the localized value is related to
     * @param _language     Language of the property
     * @return OID of the value, otherwise null
     */
    private String getExistingLocale(final String _propertyid,
                                     final String _language)
    {
        String ret = null;
        try {
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes(DBPropertiesUpdate.TYPE_PROPERTIES_LOCAL);
            query.addSelect("OID");
            query.addWhereExprEqValue("PropertyID", _propertyid);
            query.addWhereExprEqValue("LanguageID", getLanguageId(_language));
            query.executeWithoutAccessCheck();
            if (query.next()) {
                ret = (String) query.get("OID");
            }
            query.close();
        } catch (final EFapsException e) {
            DBPropertiesUpdate.LOG.error("getExistingLocale(String)", e);
        }
        return ret;
    }

    /**
     * Insert a new localized Value.
     *
     * @param _propertyid   ID of the Property, the localized value is related to
     * @param _value        Value of the Property
     * @param _language     Language of the property
     */
    private void insertNewLocal(final String _propertyid,
                                final String _value,
                                final String _language)
    {
        try {
            final Insert insert = new Insert(DBPropertiesUpdate.TYPE_PROPERTIES_LOCAL);
            insert.add("Value", _value);
            insert.add("PropertyID", _propertyid);
            insert.add("LanguageID", getLanguageId(_language));
            insert.executeWithoutAccessCheck();
            insert.close();

        } catch (final EFapsException e) {
            DBPropertiesUpdate.LOG.error("insertNewLocal(String)", e);
        } catch (final Exception e) {
            DBPropertiesUpdate.LOG.error("insertNewLocal(String)", e);
        }
    }

    /**
     * Update a localized Value.
     *
     * @param _oid OID, of the localized Value
     * @param _value Value
     */
    private void updateLocale(final String _oid,
                              final String _value)
    {
        try {
            final Update update = new Update(_oid);
            update.add("Value", _value);
            update.execute();

        } catch (final EFapsException e) {
            DBPropertiesUpdate.LOG.error("updateLocale(String, String)", e);
        } catch (final Exception e) {
            DBPropertiesUpdate.LOG.error("updateLocale(String, String)", e);
        }

    }

    /**
     * Is a key already existing.
     *
     * @param _key Key to search for
     * @return OID of the key, otherwise null
     */
    private String getExistingKey(final String _key)
    {
        String ret = null;
        try {
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes(DBPropertiesUpdate.TYPE_PROPERTIES);
            query.addSelect("OID");
            query.addWhereExprEqValue("Key", _key);
            query.addWhereExprEqValue("BundleID", this.bundleid);
            query.executeWithoutAccessCheck();
            if (query.next()) {
                ret = (String) query.get("OID");
            }

            query.close();
        } catch (final EFapsException e) {
            DBPropertiesUpdate.LOG.error("getExisting()", e);
        }
        return ret;
    }

    /**
     * Update a Default.
     *
     * @param _oid      OID of the value to update
     * @param _value    value
     */
    private void updateDefault(final String _oid,
                               final String _value)
    {
        try {
            final Update update = new Update(_oid);
            update.add("Default", _value);
            update.execute();

        } catch (final EFapsException e) {
            DBPropertiesUpdate.LOG.error("updateDefault(String, String)", e);
        } catch (final Exception e) {
            DBPropertiesUpdate.LOG.error("updateDefault(String, String)", e);
        }
    }

    /**
     * Insert a new Property.
     *
     * @param _key      Key to insert
     * @param _value    value to insert
     * @return ID of the new Property
     */
    private String insertNewProp(final String _key,
                                 final String _value)
    {
        String ret = null;
        try {
            final Insert insert = new Insert(DBPropertiesUpdate.TYPE_PROPERTIES);
            insert.add("BundleID", this.bundleid);
            insert.add("Key", _key);
            insert.add("Default", _value);
            insert.executeWithoutAccessCheck();
            ret = insert.getId();
            insert.close();

        } catch (final EFapsException e) {
            DBPropertiesUpdate.LOG.error("InsertNew(String, String)", e);
        } catch (final Exception e) {
            DBPropertiesUpdate.LOG.error("InsertNew(String, String)", e);
        }
        return ret;
    }

    /**
     * Is the Bundle allready existing.
     *
     * @param _uuid UUID of the Bundle
     * @return ID of the Bundle if existing, else null
     */
    private String getExistingBundle(final String _uuid)
    {
        String ret = null;
        try {
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes(DBPropertiesUpdate.TYPE_PROPERTIES_BUNDLE);
            query.addSelect("ID");
            query.addWhereExprEqValue("UUID", _uuid);
            query.executeWithoutAccessCheck();
            if (query.next()) {
                ret = query.get("ID").toString();
            }
            query.close();

        } catch (final EFapsException e) {
            DBPropertiesUpdate.LOG.error("getExistingBundle(String)", e);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public String getFileApplication()
    {
        return this.fileApplication;
    }

    /**
     * {@inheritDoc}
     */
    public void updateInDB(final JexlContext _jexlContext,
                           final UpdateLifecycle _step)
        throws InstallationException
    {
        if (_step == UpdateLifecycle.DBPROPERTIES_UPDATE) {

            if (DBPropertiesUpdate.LOG.isInfoEnabled()) {
                DBPropertiesUpdate.LOG.info("Importing Properties '" + this.bundlename + "'");
            }

            final String bundleID = getExistingBundle(this.bundeluuid);

            if (bundleID == null) {
                this.bundleid = insertNewBundle();
            } else {
                this.bundleid = bundleID;
            }
            try {
                for (final Resource resource : this.resources) {
                    if ("Properties".equals(resource.type)) {
                        if (resource.language == null || resource.language.length() < 1) {
                            importFromProperties(new URL(this.root + resource.filename));
                        } else {
                            importFromProperties(new URL(this.root + resource.filename), resource.language);
                        }
                    }
                }
            } catch (final MalformedURLException e) {
                DBPropertiesUpdate.LOG.error("The URL given for one File of the DBProperties is invalid", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void readXML(final List<String> _tags,
                        final Map<String, String> _attributes,
                        final String _text)
    {
        final String value = _tags.get(0);

        if ("uuid".equals(value)) {
            this.bundeluuid = _text;
        } else if ("file-application".equals(value)) {
            this.fileApplication = _text;
        } else if ("bundle".equals(value)) {
            this.bundlename = _attributes.get("name");
            this.bundlesequence = _attributes.get("sequence");
        } else if ("resource".equals(value)) {
            if (_tags.size() == 1) {
                this.curResource = new Resource();
                this.resources.add(this.curResource);
            } else {
                this.curResource.readXML(_tags.subList(1, _tags.size()), _attributes, _text);
            }
        }
    }

    /**
     * Class to store the different Resources witch can come with one bundle.
     */
    public static class Resource
    {

        /**
         * type of the Properties.
         */
        private String type;

        /**
         * language of the Properties.
         */
        private String language;

        /**
         * Filename of the Properties.
         */
        private String filename;

        /**
         * Read event for given tags path with attributes and text.
         *
         * @param _tags         tags path as list
         * @param _attributes   map of attributes for current tag
         * @param _text         content text of this tags path
         */
        public void readXML(final List<String> _tags,
                            final Map<String, String> _attributes,
                            final String _text)
        {
            final String value = _tags.get(0);
            if ("type".equals(value)) {
                this.type = _text;
            } else if ("language".equals(value)) {
                this.language = _text;
            } else if ("file".equals(value)) {
                this.filename = _text;
            }
        }
    }
}
