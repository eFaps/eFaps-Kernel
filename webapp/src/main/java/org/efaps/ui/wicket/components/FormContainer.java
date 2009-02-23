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

package org.efaps.ui.wicket.components;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmitListener;
import org.apache.wicket.util.string.AppendingStringBuffer;

import org.efaps.ui.wicket.models.objects.AbstractUIObject;


/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 *
 */
public class FormContainer extends Form<Object> {

  private static final long serialVersionUID = 1L;

  private Component defaultSubmit;

  private String actionUrl;

  public FormContainer(final String id) {
    super(id);
    // super.setMultiPart(true);
  }

  private boolean fileUpload = false;

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.markup.html.form.Form#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
   *      org.apache.wicket.markup.ComponentTag)
   */
  @Override
  protected void onComponentTagBody(final MarkupStream _markupstream,
                                    final ComponentTag _tag) {
    super.onComponentTagBody(_markupstream, _tag);
    if (getDefaultButton() == null && this.defaultSubmit != null) {
      appendDefaultSubmit(_markupstream, _tag);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.markup.html.form.Form#onComponentTag(org.apache.wicket.markup.ComponentTag)
   */
  @Override
  protected void onComponentTag(final ComponentTag _tag) {
    super.onComponentTag(_tag);
    this.actionUrl = urlFor(IFormSubmitListener.INTERFACE).toString();
    if (getPage().getDefaultModelObject() != null) {
      if ((((AbstractUIObject) getPage().getDefaultModelObject()).isCreateMode()
          || ((AbstractUIObject) getPage().getDefaultModelObject()).isEditMode())) {
        _tag.put("enctype", "multipart/form-data");
      }
      // only on SearchMode we want normal submit, in any other case we use
      // AjaxSubmit
      if (!((AbstractUIObject) getPage().getDefaultModelObject()).isSearchMode()) {
        _tag.put("onSubmit", "return false;");
        _tag.put("action", "");
      }
    }
  }

  public void setDefaultSubmit(final Component _component) {
    this.defaultSubmit = _component;
  }

  protected void appendDefaultSubmit(final MarkupStream markupStream,
                                     final ComponentTag openTag) {

    final AppendingStringBuffer buffer = new AppendingStringBuffer();

    // div that is not visible (but not display:none either)
    buffer.append("<div style=\"width:0px;height:0px;position:absolute;"
        + "left:-100px;top:-100px;overflow:hidden\">");

    // add an empty textfield (otherwise IE doesn't work)
    buffer.append("<input type=\"text\" autocomplete=\"false\"/>");

    buffer.append("<input type=\"submit\" onclick=\" var b=Wicket.$('");
    buffer.append(this.defaultSubmit.getMarkupId());
    buffer.append("'); if (typeof(b.onclick) != 'undefined') "
        + "{ b.onclick();  }"
        + " \" ");
    buffer.append(" /></div>");
    getResponse().write(buffer);
  }

  /**
   * This is the getter method for the instance variable {@link #actionUrl}.
   *
   * @return value of instance variable {@link #actionUrl}
   */
  public String getActionUrl() {
    return this.actionUrl;
  }


  @Override
  protected void onSubmit() {
    super.onSubmit();
    if (this.fileUpload) {
      final List<IBehavior> uploadListeners =
          this.getBehaviors(FileUploadListener.class);
      for (final IBehavior listener : uploadListeners) {
        ((FileUploadListener)listener).onSubmit();
      }
    }
  }

  /**
   * This is the getter method for the instance variable {@link #fileUpload}.
   *
   * @return value of instance variable {@link #fileUpload}
   */
  public boolean isFileUpload() {
    return this.fileUpload;
  }

  /**
   * This is the setter method for the instance variable {@link #fileUpload}.
   *
   * @param _fileUpload
   *                the fileUpload to set
   */
  public void setFileUpload(final boolean _fileUpload) {
    this.fileUpload = _fileUpload;
  }

}
