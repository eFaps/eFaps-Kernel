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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.behaviors.dojo;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;

/**
 * This class provides static access to the ResourceReferences needed to use the
 * DojoToolKit.
 *
 * @author jmox
 * @version $Id$
 */
public final class DojoReference {

  /**
   * Reference to the stylesheet.
   */
  public static final ResourceReference CSS_TUNDRA
    = new CompressedResourceReference(DojoReference.class,
          "dijit/themes/tundra/tundra.css");

  /**
   * Reference to the JavaScript.
   */
  public static final JavascriptResourceReference JS_DOJO =
      new JavascriptResourceReference(DojoReference.class, "dojo/dojo.js");

  /**
   * Reference to the JavaScript.
   */
  public static final JavascriptResourceReference JS_EFAPSDOJO =
      new JavascriptResourceReference(DojoReference.class, "dojo/eFapsDojo.js");

  /**
   *
   */
  private DojoReference() {
  }

  /**
   * Method to get a HeaderContibuter for addinf djo to a webpage.
   * @return HeaderContributor
   */
  public static HeaderContributor getHeaderContributerforDojo() {
    return new HeaderContributor(new IHeaderContributor() {

      private static final long serialVersionUID = 1L;

      public void renderHead(final IHeaderResponse _response) {
        _response.renderString(getConfigJavaScript(JS_DOJO));
      }
    });
  }

  /**
   * method to create the tag for linking JavaScript.
   *
   * @param _reference ResourceReference to be linked
   * @return scriptLink width extension djConfig="parseOnLoad:true"
   */
  public static String getConfigJavaScript(final ResourceReference _reference) {
    final StringBuilder ret = new StringBuilder();
    ret.append("<script type=\"text/javascript\" ");
    ret.append("src=\"");
    ret.append(RequestCycle.get().urlFor(_reference));
    ret.append("\"");
    ret.append(" djConfig=\"parseOnLoad: true\"");
    ret.append("></script>\n");
    return ret.toString();
  }
}
