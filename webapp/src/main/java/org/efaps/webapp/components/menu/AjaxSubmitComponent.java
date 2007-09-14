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

package org.efaps.webapp.components.menu;

import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.form.Form;

import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.util.EFapsException;
import org.efaps.webapp.models.AbstractModel;
import org.efaps.webapp.models.FormModel;
import org.efaps.webapp.models.MenuItemModel;
import org.efaps.webapp.models.TableModel;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class AjaxSubmitComponent extends AbstractMenuItemAjaxComponent {

  private static final long serialVersionUID = 1L;

  public AjaxSubmitComponent(final String id, final MenuItemModel _menuItem,
                             final Form _form) {
    super(id, _menuItem);
    this.add(new SubmitAndUpdateBehavior(_form));
  }

  @Override
  public String getJavaScript() {
    return ((SubmitAndUpdateBehavior) super.getBehaviors().get(0))
        .getJavaScript();
  }

  public class SubmitAndUpdateBehavior extends AjaxFormSubmitBehavior {

    private static final long serialVersionUID = 1L;

    private final Form form;

    public String getJavaScript() {
      String script = super.getEventHandler().toString();
      return "javascript:" + script.replace("'", "\"");
    }

    public SubmitAndUpdateBehavior(final Form _form) {
      super(_form, "onClick");
      this.form = _form;
    }

    @Override
    protected void onSubmit(final AjaxRequestTarget _target) {
      CommandAbstract command =
          ((MenuItemModel) super.getComponent().getModel()).getCommand();
      Map<?, ?> para = this.form.getRequest().getParameterMap();

      if (command.hasEvents(EventType.UI_COMMAND_EXECUTE)) {
        try {
          String[] oids = (String[]) para.get("selectedRow");
          if (oids != null) {
            command.executeEvents(EventType.UI_COMMAND_EXECUTE,
                ParameterValues.OTHERS, oids);
          } else {
            command.executeEvents(EventType.UI_COMMAND_EXECUTE);
          }
        } catch (EFapsException e) {
          e.printStackTrace();
        }
      }
      ((AbstractModel) this.form.getPage().getModel()).resetModel();
      Page page = null;
      if (this.form.getPage().getModel() instanceof TableModel) {

        page = new WebTablePage(this.form.getPage().getModel());

      } else if (this.form.getPage().getModel() instanceof FormModel) {
        page = new WebFormPage(this.form.getPage().getModel());
      }
      this.form.setResponsePage(page);
    }
  }

}
