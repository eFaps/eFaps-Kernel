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

package org.efaps.webapp.components.modalwindow;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;

import org.efaps.webapp.models.AbstractModel;
import org.efaps.webapp.models.FormModel;
import org.efaps.webapp.models.StructurBrowserModel;
import org.efaps.webapp.models.TableModel;
import org.efaps.webapp.pages.ContentPage;
import org.efaps.webapp.pages.MainPage;
import org.efaps.webapp.pages.StructurBrowserTablePage;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class ModalWindowContainer extends ModalWindow {

  private static final long serialVersionUID = 1L;

  private boolean reloadChild = false;

  private boolean updateParent = false;

  public ModalWindowContainer(String id) {
    super(id);
    super.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
  }

  /**
   * This is the getter method for the instance variable {@link #reloadChild}.
   *
   * @return value of instance variable {@link #reloadChild}
   */

  public boolean isReloadChild() {
    return this.reloadChild;
  }

  /**
   * This is the setter method for the instance variable {@link #reloadChild}.
   *
   * @param _reloadchild
   *                the reloadParent to set
   */
  public void setReloadChild(boolean _reloadchild) {
    this.reloadChild = _reloadchild;
  }

  @Override
  public void close(final AjaxRequestTarget _target) {
    super.close(_target);
    if (this.reloadChild) {
      AbstractModel model = (AbstractModel) this.getPage().getModel();
      Class<?> clazz = null;
      if (model instanceof TableModel) {
        clazz = WebTablePage.class;
      } else if (model instanceof FormModel) {
        clazz = WebFormPage.class;
      } else if (model instanceof StructurBrowserModel) {
        clazz = StructurBrowserTablePage.class;
      }
      PageParameters parameters = model.getPageParameters();
      parameters.put("listMenuKey", ((ContentPage) this.getPage())
          .getListMenuKey());
      CharSequence url =
          this.urlFor(PageMap.forName(this.getPage().getPageMapName()), clazz,
              parameters);
      String javascript = null;
      if (this.getPage().getPageMapName().equals(MainPage.IFRAME_PAGEMAP_NAME)) {
        javascript = "top.frames[0].location.href = '";
      } else {
        javascript = "top.frames[0].frames[0].location.href = '";
      }
      javascript += url + "';";
      _target.appendJavascript(javascript);
    }

  }

  /**
   * This is the getter method for the instance variable {@link #updateParent}.
   *
   * @return value of instance variable {@link #updateParent}
   */

  public boolean isUpdateParent() {
    return this.updateParent;
  }

  /**
   * This is the setter method for the instance variable {@link #updateParent}.
   *
   * @param updateParent
   *                the updateParent to set
   */
  public void setUpdateParent(final boolean updateParent) {
    this.updateParent = updateParent;
  }

}
