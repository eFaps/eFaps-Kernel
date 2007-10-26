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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.footer;

import java.util.Iterator;
import java.util.List;
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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.PopupCloseLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.model.IModel;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.table.WebFormContainer;
import org.efaps.ui.wicket.components.table.cell.formcell.FormCellPanel;
import org.efaps.ui.wicket.models.AbstractModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.FormModel.FormCellModel;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;

/**
 * This class renders the Footer under a WebForm or WebTable.<br>
 * It provides also the necesarry links to initialise the necesarry actions of
 * the Footer like submit, cancel and so on.
 *
 * @author jmox
 * @version $Id:FooterPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class FooterPanel extends Panel {

  private static final long serialVersionUID = -1722339596237748160L;

  /**
   * static Reference to the Icon for "next'
   */
  public static final ResourceReference ICON_NEXT =
      new ResourceReference(FooterPanel.class, "eFapsButtonNext.gif");

  /**
   * static Reference to the Icon for "done'
   */
  public static final ResourceReference ICON_DONE =
      new ResourceReference(FooterPanel.class, "eFapsButtonDone.gif");

  /**
   * static Reference to the Icon for "cancel'
   */
  public static final ResourceReference ICON_CANCEL =
      new ResourceReference(FooterPanel.class, "eFapsButtonCancel.gif");

  /**
   * This instance variable stores the ModalWindowContainer the Page and with it
   * this footer was opened in, to have acess to it, for actions like closing
   * the ModalWindow.
   */
  private final ModalWindowContainer modalWindow;

  private boolean success;

  /**
   * Constructor for the FooterPanel.
   *
   * @param _id
   *                wicket:id of the Component
   * @param _model
   *                Model of the Comoponent
   * @param _modalWindow
   *                ModalWindowContainer containing this FooterPanel
   * @param _form
   *                FormContainer of the Page (needed to submit the Form)
   */
  public FooterPanel(final String _id, final IModel _model,
                     final ModalWindowContainer _modalWindow,
                     FormContainer _form) {
    super(_id, _model);
    this.modalWindow = _modalWindow;
    final AbstractModel model = (AbstractModel) super.getModel();

    if ("true".equals(model.getCommand().getProperty("SuccessDialog"))) {
      FooterPanel.this.modalWindow
          .setWindowClosedCallback(new WindowClosedCallback() {

            private static final long serialVersionUID = 1L;

            public void onClose(final AjaxRequestTarget _target) {
              if (FooterPanel.this.success) {
                FooterPanel.this.modalWindow.setResizable(false);
                FooterPanel.this.modalWindow.setInitialWidth(20);
                FooterPanel.this.modalWindow.setInitialHeight(12);
                FooterPanel.this.modalWindow.setWidthUnit("em");
                FooterPanel.this.modalWindow.setHeightUnit("em");

                FooterPanel.this.modalWindow
                    .setPageCreator(new ModalWindow.PageCreator() {

                      private static final long serialVersionUID = 1L;

                      public Page createPage() {
                        return new DialogPage(FooterPanel.this.modalWindow,
                            model.getCommand().getName() + ".Success");
                      }
                    });

                FooterPanel.this.modalWindow.show(_target);
                FooterPanel.this.success = false;
              }
            }
          });

    }

    String label = null;

    if (model.isCreateMode()) {
      label = getLabel(model.getCommand().getName(), "Create");
    } else if (model.isEditMode()) {
      label = getLabel(model.getCommand().getName(), "Edit");
    } else if (model.isSubmit() && model instanceof TableModel) {
      label = getLabel(model.getCommand().getName(), "Connect");
    } else if (model.isSearchMode()) {
      label = getLabel(model.getCommand().getName(), "Search");
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
    cancelLink.add(new Label("cancellabel", getLabel(model.getCommand()
        .getName(), "Cancel")));
    add(cancelLink);

  }

  /**
   * method that searches a DBProperty for the Label
   *
   * @param _cmdName
   *                Name of the CommandAbstract the Label should be searched for
   * @param _keytype
   *                what Label should be searched
   * @return if found DBProperty of the CommandAbstract, else a Default
   */
  private String getLabel(final String _cmdName, final String _keytype) {
    String ret;
    if (DBProperties.hasProperty(_cmdName + ".Button." + _keytype)) {
      ret = DBProperties.getProperty(_cmdName + ".Button." + _keytype);
    } else {
      ret = DBProperties.getProperty("default.Button." + _keytype);
    }
    return ret;
  }

  /**
   * Link using Ajax to submit the Form and close the ModalWindow or the PopUp
   * this FooterPanel is imbeded.
   */
  public class AjaxSubmitAndCloseLink extends SubmitLink {

    private static final long serialVersionUID = 1L;

    public AjaxSubmitAndCloseLink(final String _id, final IModel _model,
                                  final FormContainer _form) {
      super(_id, _form);
      this.add(new AjaxSubmitAndCloseBehavior(_model, _form));
      _form.setDefaultSubmit(this);
    }
  }

  /**
   * Behavior providing the functionality for {@link #AjaxSubmitAndCloseLink}
   */
  public class AjaxSubmitAndCloseBehavior extends AjaxFormSubmitBehavior {

    private static final long serialVersionUID = 1L;

    /**
     * Instance variable storing the model, because the superclasses of a
     * behavior, don't store the model.
     */
    private final IModel imodel;

    /** Instance variable storing the form to be submited. */
    private final FormContainer form;

    /**
     * Constructor
     *
     * @param _model
     * @param _form
     */
    public AjaxSubmitAndCloseBehavior(final IModel _model,
                                      final FormContainer _form) {
      super(_form, "onclick");
      this.imodel = _model;
      this.form = _form;
    }

    @Override
    protected void onSubmit(final AjaxRequestTarget _target) {
      final String[] other =
          this.getComponent().getRequestCycle().getRequest().getParameters(
              "selectedRow");
      if (checkForRequired(_target) && (validateForm(_target))) {
        try {
          if (!executeEvents(_target, other)) {
            return;
          }

        } catch (final EFapsException e) {
          final ModalWindowContainer modal =
              ((AbstractContentPage) this.getComponent().getPage()).getModal();
          modal.setPageCreator(new ModalWindow.PageCreator() {

            private static final long serialVersionUID = 1L;

            public Page createPage() {
              return new ErrorPage(e);
            }
          });
          modal.show(_target);
          return;
        }

        final AbstractModel model = (AbstractModel) this.imodel;

        if (model.getCommand().getTarget() == CommandAbstract.TARGET_MODAL) {
          FooterPanel.this.modalWindow.setReloadChild(true);
          FooterPanel.this.modalWindow.close(_target);
        } else {
          final AbstractModel openermodel =
              (AbstractModel) ((EFapsSession) Session.get()).getOpenerModel();
          Class<?> clazz;
          if (openermodel instanceof TableModel) {
            clazz = TablePage.class;
          } else {
            clazz = FormPage.class;
          }
          final CharSequence url =
              this.form.urlFor(PageMap.forName(MainPage.IFRAME_PAGEMAP_NAME),
                  clazz, openermodel.getPageParameters());
          _target.appendJavascript("opener.location.href = '"
              + url
              + "'; self.close();");

        }
        FooterPanel.this.success = true;

      }
    }

    @Override
    protected void onError(final AjaxRequestTarget _target) {
      // not useful here
    }

    /**
     * execute the events wich are related to CommandAbstract calling the Form
     *
     * @param _target
     *                AjaxRequestTarget to be used in the case a ModalPage
     *                should be called
     * @param _other
     *                Parameters to be passed on to the Event
     * @return true if the events where executed successfully, otherwise false
     * @throws EFapsException
     */
    private boolean executeEvents(final AjaxRequestTarget _target,
                                  final String[] _other) throws EFapsException {
      boolean ret = true;
      final List<Return> returns =
          ((AbstractModel) this.form.getParent().getModel())
              .executeEvents(_other);
      for (Return oneReturn : returns) {
        if (oneReturn.get(ReturnValues.TRUE) == null && !oneReturn.isEmpty()) {
          final String key = (String) oneReturn.get(ReturnValues.VALUES);
          showDialog(_target, key);
          ret = false;
          break;
        }
      }
      return ret;
    }

    /**
     * executes the Validation-Events related to the CommandAbstract wich called
     * this Form
     *
     * @param _target
     *                AjaxRequestTarget to be used in the case a ModalPage
     *                should be called
     * @return true if the Validation was valid, otherwise false
     */
    private boolean validateForm(final AjaxRequestTarget _target) {
      boolean ret = true;

      final List<Return> validation =
          ((AbstractModel) this.form.getParent().getModel()).validate();

      for (Return oneReturn : validation) {
        if (oneReturn.get(ReturnValues.TRUE) == null) {
          final String key = (String) oneReturn.get(ReturnValues.VALUES);
          showDialog(_target, key);

          ret = false;
          break;
        }
      }
      return ret;
    }

    /**
     * Method checking if the mandatory field of the Form are filled in, and if
     * not opens a WarnDialog and marks the fields in the Form via Ajax.
     *
     * @param _target
     *                RequestTarget used for this Request
     * @return true if all mandatory fields are filled, else false
     */
    private boolean checkForRequired(final AjaxRequestTarget _target) {
      boolean ret = true;
      if (this.form.getParent().getModel() instanceof TableModel) {
        return true;
      }

      final Iterator<?> iterator = this.getComponent().getPage().iterator();
      WebFormContainer container = null;
      while (iterator.hasNext()) {
        final Object object = iterator.next();
        if (object instanceof FormContainer) {
          final Iterator<?> iterator2 = ((FormContainer) object).iterator();
          while (iterator2.hasNext()) {
            final Object object2 = iterator2.next();
            if (object2 instanceof WebFormContainer) {
              container = (WebFormContainer) object2;
              break;
            }
          }
          break;
        }
      }

      final Map<?, ?> map =
          this.getComponent().getRequestCycle().getRequest().getParameterMap();
      for (FormCellPanel cellpanel : container.getRequiredComponents()) {
        final String[] values =
            (String[]) map
                .get(((FormCellModel) cellpanel.getModel()).getName());
        final String value = values[0];
        if (value == null || value.length() == 0) {
          final WebMarkupContainer cellcontainer =
              (WebMarkupContainer) cellpanel.iterator().next();
          cellcontainer.add(new SimpleAttributeModifier("class",
              "eFapsFormLabelRequiredForce"));
          _target.addComponent(cellcontainer);
          ret = false;
        }
      }
      if (!ret) {
        showDialog(_target, "MandatoryDialog");
      }
      return ret;
    }

  }

  /**
   * shows a modal DialogPage
   *
   * @param _target
   *                AjaxRequestTarget to be used for opening the modal
   *                DialogPage
   * @param _key
   *                the Key to get the DBProperties from the eFapsDataBaase
   */
  private void showDialog(final AjaxRequestTarget _target, final String _key) {
    final ModalWindowContainer modal =
        ((AbstractContentPage) this.getPage()).getModal();

    modal.setResizable(false);
    modal.setInitialWidth(20);
    modal.setInitialHeight(12);
    modal.setWidthUnit("em");
    modal.setHeightUnit("em");
    modal.setPageMapName("warn");

    modal.setPageCreator(new ModalWindow.PageCreator() {

      private static final long serialVersionUID = 1L;

      public Page createPage() {
        return new DialogPage(modal, _key);
      }
    });

    modal.show(_target);

  }

  /**
   * Link using Ajax to close the ModalWindow the FooterPanel was opened in.
   */
  public class AjaxCancelLink extends AjaxLink {

    public AjaxCancelLink(final String _id) {
      super(_id);
    }

    private static final long serialVersionUID = 1L;

    @Override
    public void onClick(final AjaxRequestTarget _target) {
      FooterPanel.this.modalWindow.setReloadChild(false);
      FooterPanel.this.modalWindow.close(_target);
    }
  }

  /**
   * Link used to submit a Search
   */
  public class SearchSubmitLink extends SubmitLink {

    private static final long serialVersionUID = 1L;

    public SearchSubmitLink(final String _id, final AbstractModel _model,
                            final Form _form) {
      super(_id, _form);
      super.setModel(_model);
    }

    @Override
    public void onSubmit() {
      super.onSubmit();
      final AbstractModel model = (AbstractModel) super.getModel();

      final PageParameters parameters = new PageParameters();
      parameters.add("command", model.getCommand().getUUID().toString());
      parameters.add("oid", model.getOid());

      final TableModel newmodel = new TableModel(parameters);
      if (model.isSubmit()) {
        newmodel.setSubmit(true);
        newmodel.setCallingCommandUUID(model.getCallingCommandUUID());
      }

      final TablePage page = new TablePage(newmodel);

      this.getRequestCycle().setResponsePage(page);

    }
  }

}
