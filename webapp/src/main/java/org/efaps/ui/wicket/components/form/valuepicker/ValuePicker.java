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

package org.efaps.ui.wicket.components.form.valuepicker;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public class ValuePicker extends WebComponent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final Value4Picker valueComponent;
  public static EFapsContentReference CSS =
    new EFapsContentReference(ValuePicker.class, "ValuePicker.css");

  public static final EFapsContentReference ICON =
    new EFapsContentReference(ValuePicker.class, "valuepicker.png");

  /**
   * @param string
   * @param _model
   * @param value
   */
  public ValuePicker(final String _wicketId, final IModel<?> _model,
      final Value4Picker _value) {
    super(_wicketId, _model);
    add(new AjaxOpenPickerBehavior());
    add(StaticHeaderContributor.forCss(CSS));


    this.valueComponent = _value;
  }




  /**
   * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
   * @param _tag
   */
  @Override
  protected void onComponentTag(final ComponentTag _tag) {
   super.onComponentTag(_tag);
   final UIFormCell uiObject = (UIFormCell) getDefaultModelObject();
   _tag.put("title", uiObject.getPicker().getLabel());
   _tag.put("class", "eFapsPickerLink");
  }

  /**
   * @see org.apache.wicket.Component#onComponentTagBody(
   * org.apache.wicket.markup.MarkupStream,
   * org.apache.wicket.markup.ComponentTag)
   * @param _markupStream
   * @param _openTag
   */
  @Override
  protected void onComponentTagBody(final MarkupStream _markupStream,
                                    final ComponentTag _openTag) {
    super.onComponentTagBody(_markupStream, _openTag);
    final StringBuilder html = new StringBuilder();
    html.append("<img alt=\"\" src=\"")
      .append(ICON.getImageUrl()).append("\"/>");
    replaceComponentTagBody(_markupStream, _openTag, html);
  }




  public class AjaxOpenPickerBehavior extends AjaxEventBehavior {

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AjaxOpenPickerBehavior() {
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
      modal.setTitle(DBProperties.getProperty("Logo.Version.Label"));
      final ValuePickerPanel panel = new ValuePickerPanel(modal.getContentId(),
                                              getDefaultModel(),
                                              ValuePicker.this.valueComponent);

      modal.setContent(panel);
      modal.show(_target);
      final StringBuilder script = new StringBuilder();
      script.append("document.getElementById('")
        .append(panel.getSearchField().getMarkupId())
        .append("').focus()");
      _target.appendJavascript(script.toString());
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
