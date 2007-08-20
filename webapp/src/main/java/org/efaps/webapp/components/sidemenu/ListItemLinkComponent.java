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

package org.efaps.webapp.components.sidemenu;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;
import org.wicketstuff.dojo.markup.html.container.split.DojoSplitContainer;

import org.efaps.webapp.components.EFapsContainerComponent;
import org.efaps.webapp.models.IMenuItemModel;
import org.efaps.webapp.wicket.WebFormPage;
import org.efaps.webapp.wicket.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class ListItemLinkComponent extends AjaxLink {
  private static final long serialVersionUID = 1L;

  private final String oid;

  public ListItemLinkComponent(String _id, IModel _model, String _oid) {
    super(_id, _model);
    this.oid = _oid;
  }

  @Override
  protected void onComponentTag(final ComponentTag tag) {
    tag.setName("a");
    super.onComponentTag(tag);
  }

  @Override
  protected void onComponentTagBody(MarkupStream _markupStream,
      ComponentTag _openTag) {
    IMenuItemModel model = (IMenuItemModel) super.getModel();
    super.replaceComponentTagBody(_markupStream, _openTag, model.getLabel());
  }

  @Override
  public void onClick(AjaxRequestTarget target) {
    IMenuItemModel model = (IMenuItemModel) super.getModel();

    PageParameters u = new PageParameters();
    u.add("oid", this.oid);
    u.add("command", model.getCommand().getName());

    EFapsContainerComponent page;
    if (model.getCommand().getTargetTable() != null) {

      page =
          new EFapsContainerComponent("containerrechts", WebTablePage.class, u);
    } else {
      page =
          new EFapsContainerComponent("containerrechts", WebFormPage.class, u);
    }
    MarkupContainer x = this.findParent(DojoSplitContainer.class);
    x.replace(page);
    target.addComponent(page);
    target
        .appendJavascript("dojo.widget.getWidgetById(\"eFapsSplitContainer0\").removeChild(dojo.widget.getWidgetById(\"eFapsSplitContainer0\").children[1]);"
            + " dojo.widget.getWidgetById(\"eFapsSplitContainer0\").addChild(dojo.widget.getWidgetById(\"containerrechts2\"));");

    // dojo.widget.getWidgetById(\"eFapsSplitContainer0\").removeChild(dojo.widget.getWidgetById(\"eFapsSplitContainer0\").children[1]);
    // target
    // .appendJavascript("" +
    // "" +
    // "dojo.widget.getWidgetById(\"eFapsSplitContainer0\").removeChild(dojo.widget.getWidgetById(\"eFapsSplitContainer0\").children[1]);"
    // +
    // "dojo.widget.getWidgetById(\"eFapsSplitContainer0\").addChild(dojo.widget.getWidgetById(\"containerrechts2\"));"
    // +
    // "document.getElementById('eFapsSideMenu').style.width = 133;" +
    // "document.getElementById('containerrechts2').style.width = 766");

    //    		
    // " document.getElementById('eFapsSideMenu').style.width = jan;" +
    // " " +
    // " document.getElementById('containerrechts2').style.left=leftpos;" +
    // "
    // dojo.widget.getWidgetById(\"eFapsSplitContainer0\").sizers[0].style.left
    // = jan;" +
    // " " +
    // " ");
    // target.prependJavascript("" +
    // " jan = document.getElementById('eFapsSideMenu').style.width;" +
    // " leftpos=document.getElementById('containerrechts2').style.left");
    //    

  }
}
