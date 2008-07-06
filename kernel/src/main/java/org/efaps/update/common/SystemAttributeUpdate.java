/*
 * Copyright 2003-2008 The eFaps Team
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
import java.util.List;
import java.util.Map;

import org.efaps.db.Insert;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id$
 */
public class SystemAttributeUpdate extends AbstractUpdate
{
  /**
   *
   * @param _url        URL of the file
   */
  public SystemAttributeUpdate(final URL _url)
  {
    super(_url, "Admin_Common_SystemAttribute");
  }

  /**
   * Creates new instance of class {@link Definition}.
   *
   * @return new definition instance
   * @see Definition
   */
  @Override
  protected AbstractDefinition newDefinition()
  {
    return new Definition();
  }

  public class Definition extends AbstractDefinition
  {
    @Override
    protected void readXML(final List<String> _tags,
                           final Map<String,String> _attributes,
                           final String _text)
    {
      final String value = _tags.get(0);
      if ("value".equals(value))  {
        addValue("Value", _text);
      } else  {
        super.readXML(_tags, _attributes, _text);
      }
    }

    /**
     * Because the attribute 'Value' of the system attribute is a required
     * attribute, the attribute value is also set for the create.
     *
     * @param _insert  insert instance
     */
    @Override
    protected void createInDB(final Insert _insert) throws EFapsException
    {
      _insert.add("Value", getValue("Value"));
      super.createInDB(_insert);
    }
  }
}
