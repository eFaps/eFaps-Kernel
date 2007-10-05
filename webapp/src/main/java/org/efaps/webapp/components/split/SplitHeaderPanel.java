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

package org.efaps.webapp.components.split;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import org.efaps.webapp.pages.ContentContainerPage;

public class SplitHeaderPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public SplitHeaderPanel(String id) {
    super(id);
    this.setOutputMarkupId(true);
    this.add(HeaderContributor.forCss(SplitHeaderPanel.class,
        "SplitHeaderPanel.css"));

    AjaxLink link = new AjaxLink("expandcontract") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(final AjaxRequestTarget _target) {
        String ret;
        ret =
            "togglePane(\""
                + getPage().get(
                    ((ContentContainerPage) getPage()).getSplitPath())
                    .getMarkupId()
                + "\",\""
                + findParent(ListOnlyPanel.class).getMarkupId()
                + "\",\""
                + this.getParent().getMarkupId()
                + "\")";

        _target.appendJavascript(ret);
        _target.focusComponent(this.getParent());
      }
    };
    this.add(link);

    link.add(new ImageDiv("image"));
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.markup.html.panel.Panel#onComponentTag(org.apache.wicket.markup.ComponentTag)
   */
  @Override
  protected void onComponentTag(final ComponentTag _tag) {
    super.onComponentTag(_tag);
    _tag.put("class", "eFapsSplitHeader");
  }

  public class ImageDiv extends WebMarkupContainer {

    private static final long serialVersionUID = 1L;

    public ImageDiv(String id) {
      super(id);
      setOutputMarkupId(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag) {
      super.onComponentTag(_tag);
      _tag.put("class", "eFapsSplitImageContract");
    }

  }

}
