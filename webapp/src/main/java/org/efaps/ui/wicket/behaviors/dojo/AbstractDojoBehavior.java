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

package org.efaps.ui.wicket.behaviors.dojo;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;


/**
 * This class renders the Links for the JavaScripts in the Head for Behaviors
 * using Dojo.
 *
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractDojoBehavior extends AbstractBehavior {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;


  /**
   * Render the links for the head.
   * @see org.apache.wicket.behavior.AbstractBehavior#renderHead(
   *  org.apache.wicket.markup.html.IHeaderResponse)
   * @param _response resonse to add
   */
  @Override
  public void renderHead(final IHeaderResponse _response) {
    super.renderHead(_response);
    _response.renderString(DojoReference
        .getConfigJavaScript(DojoReference.JS_DOJO));
    _response.renderString(DojoReference
        .getConfigJavaScript(DojoReference.JS_EFAPSDOJO));
    _response.renderCSSReference(DojoReference.CSS_TUNDRA);
  }

  /**
   * All components using dojo must render the id of the component.
   * @see org.apache.wicket.behavior.AbstractBehavior#beforeRender(
   * org.apache.wicket.Component)
   * @param _component component
   */
  @Override
  public void beforeRender(final Component _component) {
    super.beforeRender(_component);
    _component.setOutputMarkupId(true);
  }
}
