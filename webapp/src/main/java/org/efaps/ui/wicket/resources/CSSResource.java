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

package org.efaps.ui.wicket.resources;

import java.util.UUID;

import org.apache.wicket.Application;
import org.apache.wicket.SharedResources;

import org.efaps.admin.common.SystemAttribute;
import org.efaps.update.program.CSSUpdate;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class CSSResource extends AbstractEFapsResource {

  private static final long serialVersionUID = 1L;

  public CSSResource(final String _name) {
    super(_name, CSSUpdate.TYPENAME);
  }

  public static CSSResource get(final String _name) {
    final SharedResources sharedResources =
        Application.get().getSharedResources();

    CSSResource resource = (CSSResource) sharedResources.get(_name);

    if (resource == null) {
      resource = new CSSResource(_name);
      sharedResources.add(_name, null, resource);
    }
    return resource;

  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.markup.html.WebResource#getCacheDuration()
   */
  @Override
  protected int getCacheDuration() {
    return SystemAttribute.get(
        UUID.fromString("50a65460-2d08-4ea8-b801-37594e93dad5"))
        .getIntegerValue();
  }

  @Override
  protected EFapsResourceStream setNewResourceStream() {
    return new CSSResourceStream();
  }

  /**
   * TODO description
   *
   * @author jmox
   * @version $Id$
   */
  public class CSSResourceStream extends EFapsResourceStream {

    private static final long serialVersionUID = 1L;

    public CSSResourceStream() {
      super();
    }

    public String getContentType() {
      return "text/css";
    }

  }

}
