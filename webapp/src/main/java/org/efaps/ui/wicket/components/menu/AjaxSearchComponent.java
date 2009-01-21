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

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.heading.HeadingPanel;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.form.FormPage;

/**
 * Class is used to render a link o page containing a search. The link is used
 * to change the type and the form to search for by using ajax.
 *
 * @author jmox
 * @version $Id:SearchLink.java 1510 2007-10-18 14:35:40Z jmox $
 *
 */
public class AjaxSearchComponent extends AbstractMenuItemAjaxComponent {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   *
   * @param _wicketId   wicket id of this component
   * @param _model      model for this component
   */
  public AjaxSearchComponent(final String _wicketId,
                        final IModel<UIMenuItem> _model) {
    super(_wicketId, _model);
    this.add(new AjaxReloadSearchFormBehavior());
  }

  /**
  * This Method returns the JavaScript which is executed by the
  * JSCooKMenu.
  *
  * @return String with the JavaScript
  */
  @Override
  public String getJavaScript() {
    return ((AjaxReloadSearchFormBehavior) super.getBehaviors().get(0))
        .getJavaScript();
  }


  /**
   * Class is used to execute the reload of the page for search.
   *
   */
  public class AjaxReloadSearchFormBehavior extends AjaxEventBehavior {

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AjaxReloadSearchFormBehavior() {
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
     * Method is executed by ajax to change the content of a form for search.
     *
     * @param _target AjaxRequestTarget
     */
    @Override
    protected void onEvent(final AjaxRequestTarget _target) {

      FormContainer form = null;
      HeadingPanel heading = null;
      boolean break1 = false;
      boolean break2 = false;

      final Iterator<? extends Component> iter = getPage().iterator();
      while (iter.hasNext()) {
        final Component comp = iter.next();
        if (comp instanceof FormContainer) {
          _target.addComponent(comp);
          form = (FormContainer) comp;
          break1 = true;
        }
        if (comp instanceof HeadingPanel) {
          _target.addComponent(comp);
          heading = (HeadingPanel) comp;
          break2 = true;
        }
        if (break1 && break2) {
          break;
        }
      }
      heading.removeAll();
      form.removeAll();

      final UIMenuItem menuitem
                          = (UIMenuItem) getComponent().getDefaultModelObject();

      final UIForm uiform = (UIForm) getPage().getDefaultModelObject();
      uiform.resetModel();
      uiform.setCommandUUID(menuitem.getCommandUUID());
      uiform.setFormUUID(uiform.getCommand().getTargetForm().getUUID());
      uiform.execute();
      heading.addComponents(uiform.getTitle());
      FormPage.updateFormContainer(getPage(), form, uiform);
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
