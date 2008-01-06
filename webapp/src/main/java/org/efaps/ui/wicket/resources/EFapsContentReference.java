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

import org.apache.wicket.IClusterable;

import org.efaps.util.RequestHandler;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class EFapsContentReference implements IClusterable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final String name;

  public EFapsContentReference(final Class<?> _scope, final String _name) {
    this(_scope.getPackage().getName() + "." + _name);
  }

  public EFapsContentReference(final String _name) {
    this.name = _name;
  }

  /**
   * This is the getter method for the instance variable {@link #name}.
   *
   * @return value of instance variable {@link #name}
   */
  public String getName() {
    return this.name;
  }

  public String getImageUrl() {
    return RequestHandler.replaceMacrosInUrl(RequestHandler.URL_IMAGE
        + this.name);
  }

  public String getCSSUrl() {
    return RequestHandler.replaceMacrosInUrl(RequestHandler.URL_STATIC
        + this.name);
  }
}
