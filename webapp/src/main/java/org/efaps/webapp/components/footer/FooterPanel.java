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

package org.efaps.webapp.components.footer;

import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.PopupCloseLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.components.FormContainer;
import org.efaps.webapp.components.modalwindow.ModalWindowContainer;
import org.efaps.webapp.components.table.WebFormContainer;
import org.efaps.webapp.components.table.cell.formcell.FormCellPanel;
import org.efaps.webapp.models.AbstractModel;
import org.efaps.webapp.models.TableModel;
import org.efaps.webapp.models.FormModel.FormCellModel;
import org.efaps.webapp.pages.ContentPage;
import org.efaps.webapp.pages.MainPage;
import org.efaps.webapp.pages.WarnDialogPage;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class FooterPanel extends Panel {

  private static final long serialVersionUID = -1722339596237748160L;

  public static final ResourceReference ICON_NEXT =
      new ResourceReference(FooterPanel.class, "eFapsButtonNext.gif");

  public static final ResourceReference ICON_DONE =
      new ResourceReference(FooterPanel.class, "eFapsButtonDone.gif");

  public static final ResourceReference ICON_CANCEL =
      new ResourceReference(FooterPanel.class, "eFapsButtonCancel.gif");

  private final ModalWindowContainer modalWindow;

  public FooterPanel(final String _id, final IModel _model,
                     final ModalWindowContainer _modalWindow,
                     FormContainer _form) {
    super(_id, _model);
    this.modalWindow = _modalWindow;
    AbstractModel model = (AbstractModel) super.getModel();

    String label = null;
    if (model.isCreateMode()) {
      label = "Create";
    } else if (model.isEditMode()) {
      label = "Update";
    } else if (model.isSubmit() && model instanceof TableModel) {
      label = "Connect";
    } else if (model.isSearchMode()) {
      label = "Search";
    }

    add(new StyleSheetReference("panelcss", getClass(), "FooterPanel.css"));
    WebMarkupContainer createEditSearchLink = null;

    if ((model.isSubmit() && model instanceof TableModel)
        || !model.isSearchMode()) {
      createEditSearchLink =
          new AjaxSubmitAndCloseLink("createeditsearch", model, _form);
    } else if (model.isSearchMode() && model.getCallingCommandUUID() != null) {
      createEditSearchLink =
          new SearchSubmitLink("createeditsearch", model, _form);
    } else {
      createEditSearchLink =
          (WebMarkupContainer) new WebMarkupContainer("createeditsearch")
              .setVisible(false);
    }
    if (model.isSearchMode()) {
      createEditSearchLink.add(new Image("createeditsearchicon", ICON_NEXT));

    } else {
      createEditSearchLink.add(new Image("createeditsearchicon", ICON_DONE));
    }

    createEditSearchLink.add(new Label("createeditsearchlabel", label));
    add(createEditSearchLink);

    WebMarkupContainer cancelLink = null;
    if (_modalWindow == null) {
      cancelLink = new PopupCloseLink("cancel");
    } else {
      cancelLink = new AjaxCancelLink("cancel");
    }

    cancelLink.add(new Image("cancelicon", ICON_CANCEL));
    cancelLink.add(new Label("cancellabel", "Cancel"));
    add(cancelLink);

  }

  public class AjaxSubmitAndCloseLink extends SubmitLink {

    private static final long serialVersionUID = 1L;

    public AjaxSubmitAndCloseLink(final String _id, final IModel _model,
                                  final Form _form) {
      super(_id, _form);
      this.add(new AjaxSubmitAndCloseBehavior(_model, _form));
    }
  }

  public class AjaxSubmitAndCloseBehavior extends AjaxFormSubmitBehavior {

    private static final long serialVersionUID = 1L;

    private final IModel imodel;

    private final Form form;

    public AjaxSubmitAndCloseBehavior(final IModel _model, final Form _form) {
      super(_form, "onclick");
      this.imodel = _model;
      this.form = _form;

    }

    @Override
    protected void onSubmit(AjaxRequestTarget _target) {
      String[] other =
          this.getComponent().getRequestCycle().getRequest().getParameters(
              "selectedRow");
      if (checkForRequired(_target)) {
        ((AbstractModel) this.form.getParent().getModel()).executeEvents(other);

        AbstractModel model = (AbstractModel) this.imodel;

        if (model.getCommand().getTarget() == CommandAbstract.TARGET_MODAL) {
          FooterPanel.this.modalWindow.setReloadParent(true);
          FooterPanel.this.modalWindow.close(_target);
        } else {
          AbstractModel openermodel =
              (AbstractModel) ((EFapsSession) Session.get()).getOpenerModel();
          Class<?> clazz;
          if (openermodel instanceof TableModel) {
            clazz = WebTablePage.class;
          } else {
            clazz = WebFormPage.class;
          }
          CharSequence url =
              this.form.urlFor(PageMap.forName(MainPage.IFRAME_PAGEMAP_NAME),
                  clazz, openermodel.getPageParameters());
          _target.appendJavascript("opener.location.href = '"
              + url
              + "'; self.close();");

        }
      }
    }

    private boolean checkForRequired(final AjaxRequestTarget _target) {
      boolean ret = true;
      if (this.form.getParent().getModel() instanceof TableModel) {
        return true;
      }

      Iterator<?> it = this.getComponent().getPage().iterator();
      WebFormContainer container = null;
      while (it.hasNext()) {
        Object object = it.next();
        if (object instanceof FormContainer) {
          Iterator<?> it2 = ((FormContainer) object).iterator();
          while (it2.hasNext()) {
            Object object2 = it2.next();
            if (object2 instanceof WebFormContainer) {
              container = (WebFormContainer) object2;
              break;
            }
          }
          break;
        }
      }

      Map<?, ?> map =
          this.getComponent().getRequestCycle().getRequest().getParameterMap();
      for (FormCellPanel cellpanel : container.getRequiredComponents()) {
        String[] values =
            (String[]) map
                .get(((FormCellModel) cellpanel.getModel()).getName());
        String value = values[0];
        if (value == null || value.length() == 0) {
          WebMarkupContainer cellcontainer =
              (WebMarkupContainer) cellpanel.iterator().next();
          cellcontainer.add(new SimpleAttributeModifier("class",
              "eFapsFormLabelRequiredForce"));
          _target.addComponent(cellcontainer);
          ret = false;
        }
      }
      if (!ret) {
        final ModalWindowContainer modal =
            ((ContentPage) this.getComponent().getPage()).getModal();
        modal.setResizable(false);
        modal.setInitialWidth(20);
        modal.setInitialHeight(12);
        modal.setWidthUnit("em");
        modal.setHeightUnit("em");
        modal.setPageMapName("warn");
        modal.setPageCreator(new ModalWindow.PageCreator() {

          private static final long serialVersionUID = 1L;

          public Page createPage() {

            return new WarnDialogPage(modal);
          }
        });

        modal.show(_target);
      }
      return ret;
    }
  }

  public class AjaxCancelLink extends AjaxLink {

    public AjaxCancelLink(String id) {
      super(id);
    }

    private static final long serialVersionUID = 1L;

    @Override
    public void onClick(AjaxRequestTarget target) {
      FooterPanel.this.modalWindow.setReloadParent(false);
      FooterPanel.this.modalWindow.close(target);
    }
  }

  public class SearchSubmitLink extends SubmitLink {

    private static final long serialVersionUID = 1L;

    public SearchSubmitLink(final String _id, final AbstractModel _model,
                            final Form _form) {
      super(_id, _form);
      super.setModel(_model);
    }

    @Override
    public void onSubmit() {
     this.getSession();
      super.onSubmit();
      AbstractModel model = (AbstractModel) super.getModel();

      PageParameters parameters = new PageParameters();
      parameters.add("command", model.getCommand().getUUID().toString());
      parameters.add("oid", model.getOid());

      TableModel newmodel = new TableModel(parameters);
      if (model.isSubmit()) {
        newmodel.setSubmit(true);
        newmodel.setCallingCommandUUID(model.getCallingCommandUUID());
      }

      WebTablePage page = new WebTablePage(newmodel);

      this.getRequestCycle().setResponsePage(page);

    }
  }

}
