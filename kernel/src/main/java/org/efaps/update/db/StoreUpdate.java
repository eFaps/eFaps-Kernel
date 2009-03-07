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

package org.efaps.update.db;

import static org.efaps.admin.EFapsClassNames.DB_RESOURCE;
import static org.efaps.admin.EFapsClassNames.DB_STORE2RESOURCE;
import static org.efaps.db.store.Store.PROPERTY_COMPRESS;
import static org.efaps.db.store.Store.PROPERTY_JNDINAME;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public class StoreUpdate extends AbstractUpdate {

  /**
   * @param _url url to the xml file
   */
  public StoreUpdate(final URL _url) {
    super(_url, "DB_Store");
  }

  /**
   * Creates a new definition instance used from
   * {@link #readXML(List, Map, String)}.
   *
   * @return new definition instance
   */
  @Override
  protected AbstractDefinition newDefinition() {
    return new StoreDefinition();
  }

  /**
   * Definition for a Resource.
   *
   */
  protected final class ResourceDefinition extends AbstractDefinition {

    /**
     * Name of the class.
     */
    private final String clazz;

    /**
     * @param _class    name of the class
     * @param _compress compress
     */
    private ResourceDefinition(final String _class, final String _compress) {
      this.clazz = _class;
      getProperties().put(PROPERTY_COMPRESS, _compress);
    }
    /**
     * Read the xml.
     * @see org.efaps.update.AbstractUpdate.AbstractDefinition#readXML(
     *  java.util.List, java.util.Map, java.lang.String)
     * @param _tags       list of tags
     * @param _attributes attributes
     * @param _text       text
     */
    @Override
    protected void readXML(final List<String> _tags,
        final Map<String, String> _attributes,
        final String _text) {
      super.readXML(_tags, _attributes, _text);
    }
  }

  /**
   * Definiton for a store.
   *
   */
  protected class StoreDefinition extends AbstractDefinition {

    /**
     * Resource definition for htis store.
     */
    private ResourceDefinition resource;

    /**
     * Read the xml.
     *
     * @param _tags         List of tags
     * @param _attributes   map of attributes
     * @param _text         text
     */
    @Override
    protected void readXML(final List<String> _tags,
        final Map<String, String> _attributes, final String _text) {
      final String value = _tags.get(0);
      if ("resource".equals(value)) {
        if (_tags.size() == 1) {
          this.resource = new ResourceDefinition(_attributes.get("class"),
              _attributes.get("compress"));
        } else {
          this.resource.readXML(_tags.subList(1, _tags.size()), _attributes,
              _text);
        }
      } else if ("jndi-name".equals(value)) {
        getProperties().put(PROPERTY_JNDINAME, _text);
      } else {
        super.readXML(_tags, _attributes, _text);
      }
    }


    /**
     * Update the store in the database.
     * @see org.efaps.update.AbstractUpdate.AbstractDefinition#updateInDB(
     *  java.util.Set)
     * @param _allLinkTypes set of all links
     * @throws EFapsException on error
     */
    @Override
    public void updateInDB(final Set<Link> _allLinkTypes)
        throws EFapsException {
      super.updateInDB(_allLinkTypes);
      setSourceInDB();
    }


    /**
     * @throws EFapsException o error
     *
     */
    private void setSourceInDB() throws EFapsException {
      boolean old = false;
      final Update update;
      final SearchQuery query = new SearchQuery();
      query.setExpand(this.instance, "DB_Store2Resource\\From.To");
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (query.next()) {
        final Instance resourceInst = new Instance((String) query.get("OID"));
        update = new Update(resourceInst);
        old = true;
      } else {
        update = new Insert(Type.get(DB_RESOURCE));
      }
      query.close();
      update.add("Name", this.resource.clazz);
      update.executeWithoutAccessCheck();
      setPropertiesInDb(update.getInstance(), this.resource.getProperties());

      if (!old) {
        final Insert insert = new Insert(Type.get(DB_STORE2RESOURCE));
        insert.add("From", ((Long) this.instance.getId()).toString());
        insert.add("To", ((Long) update.getInstance().getId()).toString());
        insert.executeWithoutAccessCheck();
        insert.close();
      }
      update.close();
    }
  }
}
