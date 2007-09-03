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

package org.efaps.webapp.components.table;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;

import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.webapp.components.AbstractParentAjaxLink;
import org.efaps.webapp.components.listmenu.ListMenuUpdate;
import org.efaps.webapp.pages.ContentContainerPage;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class CellAjaxLinkComponent extends AbstractParentAjaxLink {

  private static final long serialVersionUID = 1L;

  private final String label;

  private int step = 1;

  private final String oid;

  public CellAjaxLinkComponent(String id, String _oid, String _label) {
    super(id);
    this.label = _label;
    this.oid = _oid;

  }

  @Override
  protected void onComponentTag(final ComponentTag tag) {
    tag.setName("a");
    super.onComponentTag(tag);
  }

  @Override
  protected void onComponentTagBody(final MarkupStream _markupStream,
                                    final ComponentTag _openTag) {
    super.replaceComponentTagBody(_markupStream, _openTag, this.label);
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    Instance instance = null;
    if (this.oid != null) {
      instance = new Instance(oid);
    }
    Menu menu;

    try {
      menu = instance.getType().getTreeMenu();

      PageParameters para = new PageParameters();
      para.add("command", menu.getName());
      para.add("oid", this.oid);

      if (isFirstStep()) {
        this.firstStep(_target, menu, para);
      } else {
        this.secondStep(menu, para);
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private boolean isFirstStep() {
    if (step == 1) {
      step = 2;
      return true;
    } else {
      return false;
    }
  }

  private void firstStep(final AjaxRequestTarget _target, final Menu _menu,
                         final PageParameters _parameters) {
    ListMenuUpdate.update(_target, ContentContainerPage.LISTMENU, _menu,
        _parameters, this.oid);
  }

  private void secondStep(final Menu _menu, final PageParameters _parameters) {
    try {

      if (_menu.getTargetTable() != null) {

        this.getRequestCycle().setResponsePage(WebTablePage.class, _parameters);
      } else {

        this.getRequestCycle().setResponsePage(WebFormPage.class, _parameters);
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
