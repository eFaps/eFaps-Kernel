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

package org.efaps.webapp.components;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;

/**
 * @author jmo
 * @version $Id$
 * 
 */
public abstract class AbstractParentAjaxLink extends AjaxLink {

  private static final long serialVersionUID = 1L;

  public AbstractParentAjaxLink(final String id) {
    this(id, null);
  }

  public AbstractParentAjaxLink(final String id, final IModel model) {
    super(id, model);

    add(new AjaxEventBehavior("onclick") {
      private static final long serialVersionUID = 1L;

      protected void onEvent(AjaxRequestTarget target) {
        onClick(target);
      }

      protected IAjaxCallDecorator getAjaxCallDecorator() {
        return new CancelEventIfNoAjaxDecorator(AbstractParentAjaxLink.this
            .getAjaxCallDecorator());
      }

      @Override
      protected CharSequence getCallbackScript() {
        CharSequence ret = super.getCallbackUrl();
        ret = "var x = parent.parentAjaxGet('" + ret + "');" + super.getCallbackScript();
        return ret;
      }

      protected void onComponentTag(ComponentTag tag) {
        // add the onclick handler only if link is enabled
        if (isLinkEnabled()) {
          super.onComponentTag(tag);
        }
      }
    });

  }

}
