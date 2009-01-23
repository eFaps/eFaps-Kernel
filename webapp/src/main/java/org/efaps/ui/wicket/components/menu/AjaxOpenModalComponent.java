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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.menu;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.modalwindow.ModalWindowAjaxPageCreator;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.main.MainPage;

/**
 * Class is used as a link inside the JSCookMenu that opens a modal window.
 *
 * @author jmox
 * @version $Id:AjaxOpenModalComponent.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxOpenModalComponent extends AbstractMenuItemAjaxComponent {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   *
   * @param _wicketId   wicket id of this component
   * @param _model   model for this component
   */
  public AjaxOpenModalComponent(final String _wicketId,
                                final IModel <UIMenuItem> _model) {
    super(_wicketId, _model);
    add(new AjaxOpenModalBehavior());
  }

  /**
   * This Method returns the JavaScript which is executed by the
   * JSCooKMenu.
   *
   * @return String with the JavaScript
   */
  @Override
  public String getJavaScript() {
    return ((AjaxOpenModalBehavior) super.getBehaviors().get(0))
        .getJavaScript();
  }

  /**
   * /**
   * Class is used to execute the opening of the modal window.
   *
   *
   */
  public class AjaxOpenModalBehavior extends AjaxEventBehavior {

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AjaxOpenModalBehavior() {
      super("onclick");
    }

    /**
     * This Method returns the JavaScript which is executed by the
     * JSCooKMenu.
     *
     * @return String with the JavaScript
     */
    public String getJavaScript() {
      final String script = super.getCallbackScript().toString();
      return "javascript:" + script.replace("'", "\"");
    }

    /**
     * Show the modal window.
     *
     * @param _target AjaxRequestTarget
     */
    @Override
    protected void onEvent(final AjaxRequestTarget _target) {
      ModalWindowContainer modal;
      if (getPage() instanceof MainPage) {
        modal = ((MainPage) getPage()).getModal();
      } else {
        modal =
            ((AbstractContentPage) getPage()).getModal();
      }
      modal.reset();
      final ModalWindowAjaxPageCreator pageCreator =
          new ModalWindowAjaxPageCreator((UIMenuItem) super.getComponent()
              .getDefaultModelObject(), modal);
      modal.setPageCreator(pageCreator);
      modal.setInitialHeight(((UIMenuItem) getDefaultModelObject())
          .getWindowHeight());
      modal.setInitialWidth(((UIMenuItem) getDefaultModelObject())
          .getWindowWidth());
      modal.show(_target);
    }

    /**
     * Method must be overwritten, otherwise the default would break the
     * execution of the JavaScript.
     * @return null
     */
    @Override
    protected CharSequence getPreconditionScript() {
      return null;
    }
  }

}
