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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.models.cell.UIFormCell;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public class Value4Picker extends WebComponent {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Key for the value.
   */
  private String key;

  /**
   * Value.
   */
  private String value;

  /**
   * @param _wicketId wicket id for this component
   * @param _model    model for this component
   */
  public Value4Picker(final String _wicketId, final IModel<?> _model) {
    super(_wicketId, _model);
    setOutputMarkupId(true);
  }

  /**
   * @see org.apache.wicket.Component#onComponentTagBody(
   * org.apache.wicket.markup.MarkupStream,
   * org.apache.wicket.markup.ComponentTag)
   * @param _markupStream markup stream
   * @param _tag          tag
   */
  @Override
  protected void onComponentTagBody(final MarkupStream _markupStream,
                                    final ComponentTag _tag) {
    super.onComponentTagBody(_markupStream, _tag);
    final UIFormCell uiObject = (UIFormCell) getDefaultModelObject();

    final StringBuilder html = new StringBuilder();
    html.append("<input type=\"text\" readonly=\"readonly\" ")
      .append(" value=\"").append(this.value).append("\"")
      .append("<input type=\"hidden\" value=\"")
      .append(this.key).append("\" ")
      .append("name=\"").append(uiObject.getName()).append("\"/>");
    replaceComponentTagBody(_markupStream, _tag, html);
  }

  /**
   * @param _key    key
   * @param _value  value
   */
  public void setValue(final String _key, final String _value) {
   this.key = _key;
   this.value = _value;
  }
}
