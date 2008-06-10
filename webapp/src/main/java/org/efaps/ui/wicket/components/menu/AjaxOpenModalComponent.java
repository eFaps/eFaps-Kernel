/*
 * Copyright 2003-2008 The eFaps Team
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
/*
 * Copyright 2003-2008 The eFaps Team
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
 * @author jmox
 * @version $Id:AjaxOpenModalComponent.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxOpenModalComponent extends AbstractMenuItemAjaxComponent {

  private static final long serialVersionUID = 1L;

  public AjaxOpenModalComponent(final String _id, final IModel <UIMenuItem> _menuItem) {
    super(_id, _menuItem);
    this.add(new AjaxOpenModalBehavior());
  }

  @Override
  public String getJavaScript() {
    return ((AjaxOpenModalBehavior) super.getBehaviors().get(0))
        .getJavaScript();
  }

  public class AjaxOpenModalBehavior extends AjaxEventBehavior {

    private static final long serialVersionUID = 1L;

    public AjaxOpenModalBehavior() {
      super("onclick");
    }

    public String getJavaScript() {
      final String script = super.getCallbackScript().toString();
      return "javascript:" + script.replace("'", "\"");
    }

    @Override
    protected void onEvent(final AjaxRequestTarget _target) {
      ModalWindowContainer modal;
      if (super.getComponent().getPage() instanceof MainPage) {
        modal = ((MainPage) super.getComponent().getPage()).getModal();
      } else {
        modal =
            ((AbstractContentPage) super.getComponent().getPage()).getModal();
      }
      modal.reset();
      final ModalWindowAjaxPageCreator pageCreator =
          new ModalWindowAjaxPageCreator((UIMenuItem) super.getComponent()
              .getModelObject(), modal);
      modal.setPageCreator(pageCreator);
      modal.setInitialHeight(((UIMenuItem) getModelObject()).getWindowHeight());
      modal.setInitialWidth(((UIMenuItem) getModelObject()).getWindowWidth());
      modal.show(_target);

    }

    @Override
    protected CharSequence getPreconditionScript() {
      return null;
    }
  }

}
