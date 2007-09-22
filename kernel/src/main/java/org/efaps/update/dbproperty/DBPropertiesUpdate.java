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
import org.efaps.util.EFapsException;

/**
 * Class for importing or updating of Properties from a properties-file into the
 * Database for use as eFaps-Admin_Properties.<br>
 * The import depends on the UUID of the Bundle. That means all Keys of the
 * Properties must be unique within a Bundle. Therefore the import will update a
 * key, if it is allready existing inside this bundle. The Bundle will allways
 * be idendified by the UUID and not by the name.
 * 
 * @author jmo
 * @version $Id$
 */
public class DBPropertiesUpdate {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(DBPropertiesUpdate.class);

  /////////////////////////////////////////////////////////////////////////////
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
    String ID = null;
    SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes("Admin_Language");
      query.addSelect("ID");
      query.addWhereExprEqValue("Language", _language);
      query.executeWithoutAccessCheck();
      if (query.next()) {
        ID = query.get("ID").toString();
      } else {
        ID = insertNewLanguage(_language);
      }
      query.close();
      return ID;
    } catch (EFapsException e) {

      LOG.error("getLanguageId()", e);
    }
    return ID;

  }

  /**
   * inserts a new language into the Database
   * 
   * @param _language
   *          language to be inserted
   * @return ID of the new language
   */
  private String insertNewLanguage(final String _language) {
    String ID = null;
    try {
      Insert insert = new Insert("Admin_Language");
      insert.add("Language", _language);
      insert.executeWithoutAccessCheck();
      ID = insert.getId();
      insert.close();
    } catch (EFapsException e) {

      LOG.error("insertNewLanguage()", e);
    } catch (Exception e) {

      LOG.error("insertNewLanguage()", e);
    }

    return ID;

  }

  /**
   * set the Name of the Bundle
   * 
   * @param _Name
   */
  private void setBundleName(String _Name) {
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
  public void setBundleUUID(String _UUID) {
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

    try {
      Insert insert = new Insert("Admin_DBProperties_Bundle");
      insert.add("Name", getBundleName());
      insert.add("UUID", getBundleUUID());
      insert.add("Sequence", getSequence());
      insert.executeWithoutAccessCheck();

      String Id = insert.getId();
      insert.close();
      return Id;
    } catch (EFapsException e) {

      LOG.error("insertNewBundle()", e);
    } catch (Exception e) {

      LOG.error("insertNewBundle()", e);
    }

    return null;
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
  private void setSequence(String _Sequence) {
    this.bundlesequence = _Sequence;
  }

  /**
   * Import Properties from a Properties-File as default, if the key is already
   * existing, the default will be replaced with the new default
   * 
   * @param _url  Complete Path/Name of the property file to import
   */
  private void importFromProperties(final URL _url) {

    try {
      InputStream propInFile = _url.openStream();
      Properties p2 = new Properties();
      p2.load(propInFile);
      Iterator<Entry<Object, Object>> x = p2.entrySet().iterator();

      while (x.hasNext()) {
        Entry<Object, Object> element = x.next();
        String OID = getExistingKey(element.getKey().toString());
        if (OID == null) {
          insertNewProp(element.getKey().toString(), element.getValue()
              .toString());
        } else {
          updateDefault(OID, element.getValue().toString());
        }
      }

    } catch (IOException e) {
      LOG.error("ImportFromProperties() - I/O failed.", e);
    }
  }

  /**
   * set the ID of the Bundel
   * 
   * @param _ID
   */
  private void setBundleID(String _ID) {
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
   * @param _url      Complete Path/Name of the File to import
   * @param _language Language to use for the Import
   */
  private void importFromProperties(final URL _url,
                                    final String _language) {

    String propOID;
    String propID;
    String localOID;
    try {

      InputStream propInFile = _url.openStream();
      Properties p2 = new Properties();
      p2.load(propInFile);
      Iterator<Entry<Object, Object>> x = p2.entrySet().iterator();

      while (x.hasNext()) {
        Entry<Object, Object> element = x.next();
        propOID = getExistingKey(element.getKey().toString());

        if (propOID == null) {
          propID = insertNewProp(element.getKey().toString(), element
              .getValue().toString());
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

    } catch (IOException e) {
      LOG.error("ImportFromProperties() - I/O failed.", e);
    }
  }

  /**
   * Is a localized value already existing
   * 
   * @param _propertyid
   *          ID of the Property, the localized value is related to
   * @return OID of the value, otherwise null
   */
  private String getExistingLocale(final String _propertyid,
                                   final String _language) {
    SearchQuery query = new SearchQuery();
    String OID = null;
    try {
      query.setQueryTypes("Admin_DBProperties_Local");
      query.addSelect("OID");
      query.addWhereExprEqValue("PropertyID", _propertyid);
      query.addWhereExprEqValue("LanguageID", getLanguageId(_language));
      query.executeWithoutAccessCheck();
      if (query.next()) {
        OID = (String) query.get("OID");
      }
      query.close();

      return OID;
    } catch (EFapsException e) {

      LOG.error("getExistingLocale(String)", e);
    }

    return null;
  }

  /**
   * Insert a new localized Value
   * 
   * @param _propertyid
   *          ID of the Property, the localized value is related to
   * @param _value
   *          Value of the Property
   */
  private void insertNewLocal(final String _propertyid, final String _value,
                              final String _language) {
    try {
      Insert insert = new Insert("Admin_DBProperties_Local");
      insert.add("Value", _value);
      insert.add("PropertyID", _propertyid);
      insert.add("LanguageID", getLanguageId(_language));
      insert.executeWithoutAccessCheck();
      insert.close();

    } catch (EFapsException e) {

      LOG.error("insertNewLocal(String)", e);
    } catch (Exception e) {

      LOG.error("insertNewLocal(String)", e);
    }

  }

  /**
   * Update a localized Value
   * 
   * @param _OID
   *          OID, of the localized Value
   * @param _value
   *          Value
   */
  private void updateLocale(String _OID, String _value) {
    try {
      Update update = new Update(_OID);
      update.add("Value", _value);
      update.execute();

    } catch (EFapsException e) {

      LOG.error("updateLocale(String, String)", e);
    } catch (Exception e) {

      LOG.error("updateLocale(String, String)", e);
    }

  }

  /**
   * Is a key already existing
   * 
   * 
   * @param _key
   *          Key to search for
   * @return OID of the key, otherwise null
   */
  private String getExistingKey(String _key) {
    String OID = null;
    SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes("Admin_DBProperties");
      query.addSelect("OID");
      query.addWhereExprEqValue("Key", _key);
      query.addWhereExprEqValue("BundleID", getBundleID());
      query.executeWithoutAccessCheck();
      if (query.next()) {
        OID = (String) query.get("OID");
      }

      query.close();
      return OID;
    } catch (EFapsException e) {
      LOG.error("getExisting()", e);
    }

    return null;
  }

  /**
   * Update a Default
   * 
   * @param _OID
   *          OID of the value to update
   * @param _value
   *          value
   */
  private void updateDefault(String _OID, String _value) {
    try {
      Update update = new Update(_OID);
      update.add("Default", _value);
      update.execute();

    } catch (EFapsException e) {

      LOG.error("updateDefault(String, String)", e);
    } catch (Exception e) {

      LOG.error("updateDefault(String, String)", e);
    }

  }

  /**
   * Insert a new Property
   * 
   * @param _key
   *          Key to insert
   * @param _value
   *          value to insert
   * @return ID of the new Property
   */
  private String insertNewProp(String _key, String _value) {
    try {
      Insert insert = new Insert("Admin_DBProperties");
      insert.add("BundleID", getBundleID());
      insert.add("Key", _key);
      insert.add("Default", _value);
      insert.executeWithoutAccessCheck();
      String Id = insert.getId();
      insert.close();
      return Id;
    } catch (EFapsException e) {

      LOG.error("InsertNew(String, String)", e);
    } catch (Exception e) {

      LOG.error("InsertNew(String, String)", e);
    }

    return null;

  }

  /**
   * Find out the ID for the OID
   * 
   * @param OID
   * @return ID
   */
  private String getId(String OID) {
    Long id = new Instance(OID).getId();
    return id.toString();

  }


  public static DBPropertiesUpdate readXMLFile(final URL _url) {
    DBPropertiesUpdate propimport = null;
    Digester digester = new Digester();

    digester.setValidating(false);

    digester.addObjectCreate("eFaps-DBProperties", DBPropertiesUpdate.class);

    digester.addCallMethod("eFaps-DBProperties/uuid", "setBundleUUID", 0);

    digester.addCallMethod("eFaps-DBProperties/bundle", "setBundle", 2);
    digester.addCallParam("eFaps-DBProperties/bundle", 0, "name");
    digester.addCallParam("eFaps-DBProperties/bundle", 1, "sequence");

    digester.addObjectCreate("eFaps-DBProperties/resource", Resource.class);

    digester.addCallMethod("eFaps-DBProperties/resource", "setResource", 3);
    digester.addCallParam("eFaps-DBProperties/resource/type", 0);
    digester.addCallParam("eFaps-DBProperties/resource/language", 1);
    digester.addCallParam("eFaps-DBProperties/resource/file", 2);

    digester.addSetNext("eFaps-DBProperties/resource", "addResource");

    try {
      propimport = (DBPropertiesUpdate) digester.parse(_url);

      if (propimport != null) {
        String urlStr = _url.toString();
        int i = urlStr.lastIndexOf("/");
        propimport.root = urlStr.substring(0, i + 1);
      }
    } catch (IOException e) {
      LOG.error("importProperties(String)", e);
    } catch (SAXException e) {
      LOG.error("importProperties(String)", e);
    }
    return propimport;
  }

  /**
   * set the Bundle
   * 
   * @param _Name
   *          Name of the Bundle
   * @param _Sequence
   *          Sequence of the Bundle
   */
  public void setBundle(String _Name, String _Sequence) {
    setBundleName(_Name);
    setSequence(_Sequence);
  }

  /**
   * add a Resource to the Properties
   * 
   * @param _resource
   *          Resource to be added
   */
  public void addResource(Resource _resource) {
    this.resources.add(_resource);
  }

  /**
   * Import a Bundle of Properties into the database
   * 
   */
  public void updateInDB() throws MalformedURLException  {
    if (LOG.isInfoEnabled()) {
      LOG.info("Importing Properties '" + this.getBundleName() + "'");
    }

    String BundleID = getExistingBundle(getBundleUUID());

    if (BundleID != null) {
      setBundleID(BundleID);
    } else {
      setBundleID(insertNewBundle());
    }
    for (Resource resource : this.resources) {

      if (resource.type.equals("Properties")) {
        if (resource.language.equals("")) {
          importFromProperties(new URL(this.root + resource.filename));
        } else {
          importFromProperties(new URL(this.root + resource.filename),
                               resource.language);
        }
      }
    }
  }

  /**
   * Is the Bundle allready existing
   * 
   * @param _UUID
   *          UUID of the Bundle
   * @return ID of the Bundle if existing, else null
   */
  private String getExistingBundle(String _UUID) {
    SearchQuery query = new SearchQuery();

    String BundleID = null;
    try {
      query.setQueryTypes("Admin_DBProperties_Bundle");
      query.addSelect("ID");
      query.addWhereExprEqValue("UUID", _UUID);
      query.executeWithoutAccessCheck();
      if (query.next()) {
        BundleID = (String) query.get("ID").toString();
      }
      query.close();
      return BundleID;
    } catch (EFapsException e) {

      LOG.error("getExistingBundle(String)", e);
    }
    return null;

  }

  /**
   * Class to store the diffrent Resources witch can come with one bundle
   * 
   * @author jmo
   * @version $Id$
   * 
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
     *          type of the Properties
     * @param _language
     *          language of the Properties
     * @param _filename
     *          Filename of the Properties
     */
    public void setResource(final String _type, final String _language,
                            final String _filename) {
      this.type = _type;
      this.language = _language;
      this.filename = _filename;
    }
  }
}
