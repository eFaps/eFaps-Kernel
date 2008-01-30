/*
 * Copyright 2003-2007 The eFaps Team
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

package org.efaps.update.dbproperty;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;

import org.efaps.update.Install.ImportInterface;
import org.efaps.util.EFapsException;

/**
 * Class for importing or updating of Properties from a properties-file into the
 * Database for use as eFaps-Admin_Properties.<br>
 * The import depends on the UUID of the Bundle. That means all Keys of the
 * Properties must be unique within a Bundle. Therefore the import will update a
 * key, if it is allready existing inside this bundle. The Bundle will allways
 * be idendified by the UUID and not by the name.
 *
 * @author jmox
 * @version $Id$
 */
public class DBPropertiesUpdate implements ImportInterface {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG =
      LoggerFactory.getLogger(DBPropertiesUpdate.class);

  /**
   * name for the Type
   */
  public static final String TYPE_PROPERTIES = "Admin_DBProperties";

  /**
   * name for the Type
   */
  public static final String TYPE_PROPERTIES_BUNDLE = "Admin_DBProperties_Bundle";

  /**
   * name for the Type
   */
  public static final String TYPE_PROPERTIES_LOCAL = "Admin_DBProperties_Local";

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * the name of the Bundle
   */
  private String bundlename;

  /**
   * the UUID of the Bundle
   */
  private String bundeluuid;

  /**
   * the ID of the Bundle
   */
  private String bundleid;

  /**
   * Sequence of the Bundle
   */
  private String bundlesequence;

  /**
   * root of the XML-Filt to be imported
   */
  private String root;

  /**
   * List of all Resources in this Properties
   */
  private final List<Resource> resources = new ArrayList<Resource>();

  /**
   * find out the Id of the language used for this properties
   *
   * @return ID of the Language
   */
  private String getLanguageId(final String _language) {
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
      return ret;
    } catch (final EFapsException e) {
      LOG.error("getLanguageId()", e);
    }
    return ret;
  }

  /**
   * inserts a new language into the Database
   *
   * @param _language
   *                language to be inserted
   * @return ID of the new language
   */
  private String insertNewLanguage(final String _language) {
    String ret = null;
    try {
      final Insert insert = new Insert("Admin_Language");
      insert.add("Language", _language);
      insert.executeWithoutAccessCheck();
      ret = insert.getId();
      insert.close();
    } catch (final EFapsException e) {
      LOG.error("insertNewLanguage()", e);
    } catch (final Exception e) {
      LOG.error("insertNewLanguage()", e);
    }
    return ret;
  }

  /**
   * set the Name of the Bundle
   *
   * @param _Name
   */
  private void setBundleName(final String _Name) {
    this.bundlename = _Name;
  }

  /**
   * get the Name of the Bundle
   *
   * @return
   */
  private String getBundleName() {
    return this.bundlename;
  }

  /**
   * set the UUID of the Bundle
   *
   * @param _UUID
   */
  public void setBundleUUID(final String _UUID) {
    this.bundeluuid = _UUID;
  }

  /**
   * get the UUID of the Bundle
   *
   * @return
   */
  private String getBundleUUID() {
    return this.bundeluuid;
  }

  /**
   * Insert a new Bundle into the Database
   *
   * @return ID of the new Bundle
   */
  private String insertNewBundle() {
    String ret = null;
    try {
      final Insert insert = new Insert(TYPE_PROPERTIES_BUNDLE);
      insert.add("Name", getBundleName());
      insert.add("UUID", getBundleUUID());
      insert.add("Sequence", getSequence());
      insert.executeWithoutAccessCheck();

      ret = insert.getId();
      insert.close();

    } catch (final EFapsException e) {
      LOG.error("insertNewBundle()", e);
    } catch (final Exception e) {
      LOG.error("insertNewBundle()", e);
    }
    return ret;
  }

  /**
   * get the Sequence of the Bundle
   *
   * @return
   */
  private String getSequence() {
    return this.bundlesequence;
  }

  /**
   * set the Sequence of the Bundel
   *
   * @param _Sequence
   */
  private void setSequence(final String _Sequence) {
    this.bundlesequence = _Sequence;
  }

  /**
   * Import Properties from a Properties-File as default, if the key is already
   * existing, the default will be replaced with the new default
   *
   * @param _url
   *                Complete Path/Name of the property file to import
   */
  private void importFromProperties(final URL _url) {

    try {
      final InputStream propInFile = _url.openStream();
      final Properties props = new Properties();
      props.load(propInFile);
      final Iterator<Entry<Object, Object>> iter = props.entrySet().iterator();

      while (iter.hasNext()) {
        final Entry<Object, Object> element = iter.next();
        final String OID = getExistingKey(element.getKey().toString());
        if (OID == null) {
          insertNewProp(element.getKey().toString(), element.getValue()
              .toString());
        } else {
          updateDefault(OID, element.getValue().toString());
        }
      }

    } catch (final IOException e) {
      LOG.error("ImportFromProperties() - I/O failed.", e);
    }
  }

  /**
   * set the ID of the Bundel
   *
   * @param _ID
   */
  private void setBundleID(final String _ID) {
    this.bundleid = _ID;

  }

  /**
   * get the ID of the Bundle
   *
   * @return ID
   */
  private String getBundleID() {
    return this.bundleid;
  }

  /**
   * Import Properties from a Properties-File as language-specific value, if the
   * key is not existing, a new default(=value) will also be created. If the
   * language is not existing it will be created also.
   *
   * @param _url
   *                Complete Path/Name of the File to import
   * @param _language
   *                Language to use for the Import
   */
  private void importFromProperties(final URL _url, final String _language) {

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
          propID =
              insertNewProp(element.getKey().toString(), element.getValue()
                  .toString());
        } else {
          propID = getId(propOID);
        }

        localOID = getExistingLocale(propID, _language);
        if (localOID == null) {
          insertNewLocal(propID, element.getValue().toString(), _language);
        } else {

          updateLocale(localOID, element.getValue().toString());
        }
      }

    } catch (final IOException e) {
      LOG.error("ImportFromProperties() - I/O failed.", e);
    }
  }

  /**
   * Is a localized value already existing
   *
   * @param _propertyid
   *                ID of the Property, the localized value is related to
   * @return OID of the value, otherwise null
   */
  private String getExistingLocale(final String _propertyid,
                                   final String _language) {
    String ret = null;
    try {
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes(TYPE_PROPERTIES_LOCAL);
      query.addSelect("OID");
      query.addWhereExprEqValue("PropertyID", _propertyid);
      query.addWhereExprEqValue("LanguageID", getLanguageId(_language));
      query.executeWithoutAccessCheck();
      if (query.next()) {
        ret = (String) query.get("OID");
      }
      query.close();
    } catch (final EFapsException e) {
      LOG.error("getExistingLocale(String)", e);
    }
    return ret;
  }

  /**
   * Insert a new localized Value
   *
   * @param _propertyid
   *                ID of the Property, the localized value is related to
   * @param _value
   *                Value of the Property
   */
  private void insertNewLocal(final String _propertyid, final String _value,
                              final String _language) {
    try {
      final Insert insert = new Insert(TYPE_PROPERTIES_LOCAL);
      insert.add("Value", _value);
      insert.add("PropertyID", _propertyid);
      insert.add("LanguageID", getLanguageId(_language));
      insert.executeWithoutAccessCheck();
      insert.close();

    } catch (final EFapsException e) {
      LOG.error("insertNewLocal(String)", e);
    } catch (final Exception e) {
      LOG.error("insertNewLocal(String)", e);
    }
  }

  /**
   * Update a localized Value
   *
   * @param _OID
   *                OID, of the localized Value
   * @param _value
   *                Value
   */
  private void updateLocale(final String _OID, final String _value) {
    try {
      final Update update = new Update(_OID);
      update.add("Value", _value);
      update.execute();

    } catch (final EFapsException e) {
      LOG.error("updateLocale(String, String)", e);
    } catch (final Exception e) {
      LOG.error("updateLocale(String, String)", e);
    }

  }

  /**
   * Is a key already existing
   *
   * @param _key
   *                Key to search for
   * @return OID of the key, otherwise null
   */
  private String getExistingKey(final String _key) {
    String ret = null;
    try {
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes(TYPE_PROPERTIES);
      query.addSelect("OID");
      query.addWhereExprEqValue("Key", _key);
      query.addWhereExprEqValue("BundleID", getBundleID());
      query.executeWithoutAccessCheck();
      if (query.next()) {
        ret = (String) query.get("OID");
      }

      query.close();
    } catch (final EFapsException e) {
      LOG.error("getExisting()", e);
    }
    return ret;
  }

  /**
   * Update a Default
   *
   * @param _OID
   *                OID of the value to update
   * @param _value
   *                value
   */
  private void updateDefault(final String _OID, final String _value) {
    try {
      final Update update = new Update(_OID);
      update.add("Default", _value);
      update.execute();

    } catch (final EFapsException e) {
      LOG.error("updateDefault(String, String)", e);
    } catch (final Exception e) {
      LOG.error("updateDefault(String, String)", e);
    }
  }

  /**
   * Insert a new Property
   *
   * @param _key
   *                Key to insert
   * @param _value
   *                value to insert
   * @return ID of the new Property
   */
  private String insertNewProp(final String _key, final String _value) {
    String ret = null;
    try {
      final Insert insert = new Insert(TYPE_PROPERTIES);
      insert.add("BundleID", getBundleID());
      insert.add("Key", _key);
      insert.add("Default", _value);
      insert.executeWithoutAccessCheck();
      ret = insert.getId();
      insert.close();

    } catch (final EFapsException e) {
      LOG.error("InsertNew(String, String)", e);
    } catch (final Exception e) {
      LOG.error("InsertNew(String, String)", e);
    }
    return ret;
  }

  /**
   * Find out the ID for the OID
   *
   * @param OID
   * @return ID
   */
  private String getId(final String OID) {
    final Long id = new Instance(OID).getId();
    return id.toString();
  }

  public static DBPropertiesUpdate readXMLFile(final URL _url) {
    DBPropertiesUpdate ret = null;
    final Digester digester = new Digester();

    digester.setValidating(false);

    digester.addObjectCreate("dbproperties", DBPropertiesUpdate.class);

    digester.addCallMethod("dbproperties/uuid", "setBundleUUID", 0);

    digester.addCallMethod("dbproperties/bundle", "setBundle", 2);
    digester.addCallParam("dbproperties/bundle", 0, "name");
    digester.addCallParam("dbproperties/bundle", 1, "sequence");

    digester.addObjectCreate("dbproperties/resource", Resource.class);

    digester.addCallMethod("dbproperties/resource", "setResource", 3);
    digester.addCallParam("dbproperties/resource/type", 0);
    digester.addCallParam("dbproperties/resource/language", 1);
    digester.addCallParam("dbproperties/resource/file", 2);

    digester.addSetNext("dbproperties/resource", "addResource");

    try {
      ret = (DBPropertiesUpdate) digester.parse(_url);

      if (ret != null) {
        final String urlStr = _url.toString();
        ret.root = urlStr.substring(0, urlStr.lastIndexOf("/") + 1);
      }
    } catch (final IOException e) {
      LOG.error("importProperties(String)", e);
    } catch (final SAXException e) {
      LOG.error("importProperties(String)", e);
    }
    return ret;
  }

  /**
   * set the Bundle
   *
   * @param _name
   *                Name of the Bundle
   * @param _sequence
   *                Sequence of the Bundle
   */
  public void setBundle(final String _name, final String _sequence) {
    setBundleName(_name);
    setSequence(_sequence);
  }

  /**
   * add a Resource to the Properties
   *
   * @param _resource
   *                Resource to be added
   */
  public void addResource(final Resource _resource) {
    this.resources.add(_resource);
  }

  /**
   * Import a Bundle of Properties into the database
   */
  public void updateInDB() {
    if (LOG.isInfoEnabled()) {
      LOG.info("Importing Properties '" + this.getBundleName() + "'");
    }

    final String BundleID = getExistingBundle(getBundleUUID());

    if (BundleID == null) {
      setBundleID(insertNewBundle());
    } else {
      setBundleID(BundleID);
    }
    try {
      for (final Resource resource : this.resources) {

        if ("Properties".equals(resource.type)) {
          if ("".equals(resource.language)) {
            importFromProperties(new URL(this.root + resource.filename));
          } else {
            importFromProperties(new URL(this.root + resource.filename),
                resource.language);
          }
        }
      }
    } catch (final MalformedURLException e) {
      LOG.error("The URL given for one File of the DBProperties is invalid", e);
    }
  }

  /**
   * Is the Bundle allready existing
   *
   * @param _UUID
   *                UUID of the Bundle
   * @return ID of the Bundle if existing, else null
   */
  private String getExistingBundle(final String _UUID) {
    String ret = null;
    try {
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes(TYPE_PROPERTIES_BUNDLE);
      query.addSelect("ID");
      query.addWhereExprEqValue("UUID", _UUID);
      query.executeWithoutAccessCheck();
      if (query.next()) {
        ret = query.get("ID").toString();
      }
      query.close();

    } catch (final EFapsException e) {
      LOG.error("getExistingBundle(String)", e);
    }
    return ret;
  }

  /**
   * Class to store the diffrent Resources witch can come with one bundle
   *
   * @author jmox
   * @version $Id$
   */
  public static class Resource {

    /**
     * type of the Properties
     */
    private String type;

    /**
     * language of the Properties
     */
    private String language;

    /**
     * Filename of the Properties
     */
    private String filename;

    /**
     * set the Resource
     *
     * @param _type
     *                type of the Properties
     * @param _language
     *                language of the Properties
     * @param _filename
     *                Filename of the Properties
     */
    public void setResource(final String _type, final String _language,
                            final String _filename) {
      this.type = _type;
      this.language = _language;
      this.filename = _filename;
    }
  }
}
