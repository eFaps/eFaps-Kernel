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

package org.efaps.ui.wicket.components.modalwindow;

import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.util.string.AppendingStringBuffer;

import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.Opener;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.main.MainPage;

/**
 * This is a wrapper class for a modal window.
 * @author jmox
 * @version $Id:ModalWindowContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class ModalWindowContainer extends ModalWindow {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Stores if the child mut be reloaded.
   */
  private boolean reloadChild = false;

  /**
   * Stores if the parent must be updated on close.
   */
  private boolean updateParent = false;

  /**
   * Constructor.
   *
   * @param _wicketId wicket id of this component
   */
  public ModalWindowContainer(final String _wicketId) {
    super(_wicketId);
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
  public void setReloadChild(final boolean _reloadchild) {
    this.reloadChild = _reloadchild;
  }

  /**
   * Method is called when the modal window is closed.
   *
   * @param _target AjaxRequestTarget
   */
  @Override
  public void close(final AjaxRequestTarget _target) {
    super.close(_target);
    if (this.reloadChild) {
      _target.prependJavascript(getReloadJavaScript());
    }
  }

  /**
   * Method creates a JavaScript top reload the parent page.
   * @return JavaScript
   */
  public String getReloadJavaScript() {
    final AbstractUIObject model
                        = (AbstractUIObject) getPage().getDefaultModelObject();
    final StringBuilder javascript = new StringBuilder();
    if (model != null) {
      Class<? extends Page> clazz = null;
      if (model instanceof UITable) {
        clazz = TablePage.class;
      } else if (model instanceof UIForm) {
        clazz = FormPage.class;
      } else if (model instanceof UIStructurBrowser) {
        clazz = StructurBrowserPage.class;
      }
      final Opener opener = new Opener(getPage().getDefaultModel(),
                                       getPage().getPageMapName());
      opener.setMenuTreeKey(((AbstractContentPage) getPage()).getMenuTreeKey());
      final PageParameters parameters = new PageParameters();
      parameters.add(Opener.OPENER_PARAKEY, opener.getId());
      ((EFapsSession) getSession()).storeOpener(opener);
      opener.setMarked4Remove(true);
      final CharSequence url =
          urlFor(PageMap.forName(getPage().getPageMapName()), clazz,
              parameters);

      if (getPage().getPageMapName().equals(MainPage.IFRAME_PAGEMAP_NAME)) {
        javascript.append("top.frames[0].location.href = '");
      } else {
        javascript.append("top.frames[0].frames[0].location.href = '");
      }
      javascript.append(url).append("';");
    }
    return javascript.toString();
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
   * @param _updateParent the updateParent to set
   */
  public void setUpdateParent(final boolean _updateParent) {
    this.updateParent = _updateParent;
  }

  /**
   * This method sets this ModalWindowContainer into the state like it was just
   * created. It uses the default values as they are defined in
   * <code>org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow
   * </code>
   */
  public void reset() {
    super.setMinimalWidth(200);
    super.setMinimalHeight(200);
    super.setInitialHeight(400);
    super.setInitialWidth(600);
    super.setUseInitialHeight(true);
    super.setResizable(true);
    super.setHeightUnit("px");
    super.setWidthUnit("px");
    super.setPageCreator(null);
    super.setCloseButtonCallback(null);
    super.setWindowClosedCallback(null);
    super.setPageMapName("modal-dialog-pagemap");
  }

  /**
   * This method is a exact copy of the private method getCloseJavacript() in
   * {@link #org.efaps.ui.wicket.components.modalwindow.ModalWindow}, but it has
   * to be public.
   *
   * @return JavaScript
   */
  public static String getCloseJavacript() {
    final StringBuilder ret = new StringBuilder();
    ret.append("var win;\n")
       .append("try {\n")
       .append("     win = window.parent.Wicket.Window;\n")
       .append("} catch (ignore) {\n")
       .append("}\n")
       .append("if (typeof(win) == \"undefined\" ")
         .append("|| typeof(win.current) == \"undefined\") {\n")
       .append("  try {\n")
       .append("     win = window.Wicket.Window;\n")
       .append("  } catch (ignore) {\n")
       .append("  }\n")
       .append("}\n")
       .append("if (typeof(win) != \"undefined\" ")
         .append("&& typeof(win.current) != \"undefined\") {\n")
       .append("     window.parent.setTimeout(function() {\n")
       .append("             win.current.close();\n")
       .append("     }, 0);\n")
       .append("}");
    return ret.toString();
  }

  /**
   *
   * @see org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow#
   * postProcessSettings(org.apache.wicket.util.string.AppendingStringBuffer)
   * @param _settings AppendingStringBuffer
   * @return AppendingStringBuffer
   */
  @Override
  protected AppendingStringBuffer postProcessSettings(
      final AppendingStringBuffer _settings) {
    // cut out that stupid PreconditionScript, because it will not work in case
    // of frames
    final int start = _settings.lastIndexOf("function()");
    _settings.replace(start, _settings.capacity(), "null );};");
    return _settings;
  }

}
