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

package org.efaps.update.common;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id$
 */
public class SystemConfigurationUpdate extends AbstractUpdate {
  /**
   *
   * @param _url        URL of the file
   */
  public SystemConfigurationUpdate(final URL _url) {
    super(_url, "Admin_Common_SystemConfiguration");
  }

  /**
   * Creates new instance of class {@link Definition}.
   *
   * @return new definition instance
   * @see Definition
   */
  @Override
  protected AbstractDefinition newDefinition() {
    return new Definition();
  }
  public class AttributeDefinition extends AbstractDefinition {

    private String key;
    private String value;
    private String description;

    @Override
    protected void readXML(final List<String> _tags,
                           final Map<String, String> _attributes,
                           final String _text) {
      final String value = _tags.get(0);
      if ("key".equals(value)) {
        this.key = _text;
      } else if ("value".equals(value)) {
          this.value = _text;
      } else if ("description".equals(value)) {
          this.description = _text;
      } else {
        super.readXML(_tags, _attributes, _text);
      }
    }

    /**
     * @param instance
     * @throws EFapsException
     */
    public void updateInDB(final Instance _instance) throws EFapsException {
      //create/update the attributSet
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_Common_SystemConfigurationAttribute");
      query.addWhereExprEqValue("Key", this.key);
      query.addWhereExprEqValue("AbstractLink", _instance.getId());
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      Update update = null;
      if (query.next()) {
        update = new Update((String) query.get("OID"));
      } else {
        update = new Insert("Admin_Common_SystemConfigurationAttribute");
        update.add("AbstractLink", "" + _instance.getId());
        update.add("Key", this.key);
      }
      query.close();

      update.add("Value", this.value);
      update.add("Description", this.description);

      update.executeWithoutAccessCheck();

    }
  }


  public class Definition extends AbstractDefinition {

    private AttributeDefinition curAttr;
    private final List<AttributeDefinition> attributes
                                        = new ArrayList<AttributeDefinition>();
    @Override
    protected void readXML(final List<String> _tags,
                           final Map<String, String> _attributes,
                           final String _text) {
      final String value = _tags.get(0);
      if ("attribute".equals(value)) {
        if (_tags.size() == 1)  {
          this.curAttr = new AttributeDefinition();
          this.attributes.add(this.curAttr);
        } else  {
          this.curAttr.readXML(_tags.subList(1, _tags.size()), _attributes, _text);
        }
      } else  {
        super.readXML(_tags, _attributes, _text);
      }
    }

    @Override
    public void updateInDB(final Set<Link> _allLinkTypes)
        throws EFapsException {

      super.updateInDB(_allLinkTypes);

      for (final AttributeDefinition attr : this.attributes) {
        attr.updateInDB(this.instance);
      }
    }

  }
}
