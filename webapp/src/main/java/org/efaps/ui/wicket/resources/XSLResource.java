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

package org.efaps.ui.wicket.resources;

import org.apache.wicket.Application;
import org.apache.wicket.SharedResources;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Type;

/**
 * The Resource for a XSL.
 *
 * @author jmox
 * @version $Id$
 */
public class XSLResource extends AbstractEFapsResource {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor settingt the Name of the Type in the SuperClass
   *
   * @param _name
   *                Name of the Resource
   */
  public XSLResource(final String _name) {
    super(_name, Type.get(EFapsClassNames.ADMIN_PROGRAM_XSL).getName());
  }

  /**
   * method to retrieve a XSLResource. It will check if it allready exist and
   * return the existung one. If no Resource exists a new one will be created.
   *
   * @param _name
   *                Name of the Resource
   * @return the XSLResource related to the Name
   */
  public static XSLResource get(final String _name) {
    final SharedResources sharedResources =
        Application.get().getSharedResources();

    XSLResource resource = (XSLResource) sharedResources.get(_name);

    if (resource == null) {
      resource = new XSLResource(_name);
      sharedResources.add(_name, null, resource);
    }
    return resource;
  }

  @Override
  protected AbstractEFapsResourceStream setNewResourceStream() {
    return new XSLResourceStream();
  }

  /**
   * Class for a ResourceStream for XSL
   */
  protected class XSLResourceStream extends AbstractEFapsResourceStream {

    private static final long serialVersionUID = 1L;

    public XSLResourceStream() {
      super();
    }

    public String getContentType() {
      return null;
    }

  }
}
