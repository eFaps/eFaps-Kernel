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

package org.efaps.ui.wicket.components.form.command;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.form.cell.ValueCellPanel;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.cell.UIFormCellCmd;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public class AjaxCmdBehavior extends AjaxFormSubmitBehavior {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private Component targetComponent;
  private String others;

  /**
   * @param _targetComponent
   * @param event
   */
  public AjaxCmdBehavior(final FormContainer _form,
                         final Component _targetComponent) {
    super(_form, "onclick");
    this.targetComponent = _targetComponent;
  }

  /**
   * This Method returns the JavaScript which is executed by the
   * JSCooKMenu.
   *
   * @return String with the JavaScript
   */
  public String getJavaScript() {
    final String script = super.getEventHandler().toString();
    return script;
  }

  @Override
  protected void onError(final AjaxRequestTarget _target) {
   // nothing to do
  }

  @Override
  public void onSubmit(final AjaxRequestTarget _target) {

    final UIFormCellCmd uiObject = (UIFormCellCmd) getComponent()
        .getDefaultModelObject();

    final StringBuilder snip = new StringBuilder();
    try {
      final List<Return> returns = uiObject.executeEvents(this.others);
      for (final Return oneReturn : returns) {
        if (oneReturn.contains(ReturnValues.SNIPLETT)) {
          snip.append(oneReturn.get(ReturnValues.SNIPLETT));
        }
      }
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (uiObject.isTargetField()) {
      final FormPanel formPanel = getComponent().findParent(FormPanel.class);
      this.targetComponent = getModelFromChild(formPanel,
                                               uiObject.getTargetField());
    }
    if (!uiObject.isAppend() || !this.targetComponent.isVisible()) {
      final MarkupContainer parent = this.targetComponent.getParent();
      final LabelComponent newComp
                              = new LabelComponent(this.targetComponent.getId(),
                                                   snip.toString());
      parent.addOrReplace(newComp);
      newComp.setOutputMarkupId(true);
      this.targetComponent = newComp;
      _target.addComponent(parent);
    } else {
      final StringBuilder jScript = new StringBuilder();
      jScript.append("var ele = document.getElementById('")
        .append(this.targetComponent.getMarkupId()).append("');")
        .append("var nS = document.createElement('span');")
        .append("ele.appendChild(nS);")
        .append("nS.innerHTML='").append(snip).append("'");
      _target.prependJavascript(jScript.toString());
    }
  }
  private Component getModelFromChild(final WebMarkupContainer _container,
      final String _name) {
    Component ret = null;
    final Iterator<? extends Component> iter = _container.iterator();
    while (iter.hasNext() && ret == null) {
      final Component comp = iter.next();
      if (comp.getDefaultModelObject() instanceof UIFormCell) {
        final UIFormCell cell = (UIFormCell) comp.getDefaultModelObject();
        if (_name.equals(cell.getName())) {
          if (comp instanceof ValueCellPanel) {
            final Iterator<? extends Component> celliter
                                      = ((WebMarkupContainer) comp).iterator();
            while (celliter.hasNext()) {
              final Component label = celliter.next();
              if (label instanceof LabelComponent) {
                ret = label;
              }
            }
          } else {
            ret = comp;
          }
        }
      }
      if (ret == null && comp instanceof WebMarkupContainer) {
        ret = getModelFromChild((WebMarkupContainer) comp, _name);
      }
    }
    return ret;
  }

  /**
   * Don't do anything on the tag. Must be overwritten so that the event is not
   * added to the tag.
   * @param _tag tag to modify
   */
  @Override
  protected void onComponentTag(final ComponentTag _tag) {
  }

  /**
   * @param _target
   * @param string
   */
  public void onSubmit4AutoComplete(final AjaxRequestTarget _target,
                                    final String _value) {
   this.others = _value;
   onSubmit(_target);
  }
}
