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

package org.efaps.webapp.components.dojo;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;

public class AbstractDojoBehavior extends AbstractBehavior {

  private static final long serialVersionUID = 1L;

  @Override
  public void renderHead(IHeaderResponse response) {
    super.renderHead(response);
    response.renderString(getConfigJavaScript(DojoReference.JS_DOJO));
    response.renderString(getConfigJavaScript(DojoReference.JS_EFAPSDOJO));
    response.renderCSSReference(DojoReference.CSS_TUNDRA);
  }

  private String getConfigJavaScript(ResourceReference reference) {
    StringBuilder ret = new StringBuilder();
    ret.append("<script type=\"text/javascript\" ");
    ret.append("src=\"");
    ret.append(RequestCycle.get().urlFor(reference));
    ret.append("\"");
    ret.append(" djConfig=\"parseOnLoad: true\"");
    ret.append("></script>\n");
    return ret.toString();
  }
}
