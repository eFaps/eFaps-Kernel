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

package org.efaps.ui.wicket.components.menu;

import java.io.File;
import java.util.List;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.model.IModel;

import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;

/**
 * Class renderes a standart link for the menu.
 *
 * @author jmox
 * @version $Id$
 */
public class StandardLink extends AbstractMenuItemLink {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   *
   * @param _wicketId     wicket id of this component
   * @param _model  model for this component
   */
  public StandardLink(final String _wicketId, final IModel<UIMenuItem> _model) {
    super(_wicketId, _model);
  }

  /**
   * On click it is evaluated what must be responded.
   */
  @Override
  public void onClick() {
    final UIMenuItem model = super.getModelObject();

    final AbstractCommand command = model.getCommand();

    // in case of popup store the opener model in the session
    if (command.getTarget() == Target.POPUP) {
      ((EFapsSession) getSession()).setOpenerModel(getPage().getDefaultModel());
    }

    final PageParameters para = new PageParameters();
    para.add("command", command.getUUID().toString());
    para.add("oid", model.getOid());

    if (command.getTargetTable() != null) {
      if (command.getProperty("TargetStructurBrowserField") != null) {
        final InlineFrame iframe
            = new InlineFrame(MainPage.IFRAME_WICKETID,
                              PageMap.forName(MainPage.IFRAME_PAGEMAP_NAME),
                              StructurBrowserPage.class,
                              para);
        getPage().addOrReplace(iframe);
      } else {
        if (getPage() instanceof MainPage) {
          final InlineFrame iframe
              = new InlineFrame(MainPage.IFRAME_WICKETID,
                                PageMap.forName(MainPage.IFRAME_PAGEMAP_NAME),
                                TablePage.class,
                                para);

          getPage().addOrReplace(iframe);
        } else {
          final TablePage table = new TablePage(para,
                                           getPopupSettings().getPageMap(null));
          setResponsePage(table);
        }
      }
    } else if (command.getTargetForm() != null
        || command.getTargetSearch() != null) {
      if (getPage() instanceof MainPage && command.getTargetSearch() == null) {
        final InlineFrame iframe
              = new InlineFrame(MainPage.IFRAME_WICKETID,
                                PageMap.forName(MainPage.IFRAME_PAGEMAP_NAME),
                                FormPage.class,
                                para);
        this.getPage().addOrReplace(iframe);
      } else {
        final FormPage formpage
                = new FormPage(para, null, getPopupSettings().getPageMap(null));
        setResponsePage(formpage);
      }
    } else {
      try {
        final List<Return> rets = model.executeEvents(this);
        if ("true".equals(command.getProperty("TargetShowFile"))) {
          final Object object = rets.get(0).get(ReturnValues.VALUES);
          if (object instanceof File) {
            getRequestCycle().setRequestTarget(
                new FileRequestTarget((File) object));
          }
        }
      } catch (final EFapsException e) {
        throw new RestartResponseException(new ErrorPage(e));
      }
      if ("true".equals(command.getProperty("NoUpdateAfterCOMMAND"))) {
        getRequestCycle().setRequestTarget(null);
      }
    }
  }
}
