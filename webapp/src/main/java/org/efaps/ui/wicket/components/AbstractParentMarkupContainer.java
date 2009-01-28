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

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractParentMarkupContainer extends WebMarkupContainer {

  private static final long serialVersionUID = 1L;

  public AbstractParentMarkupContainer(String id) {
    super(id);
  }

  public AbstractParentMarkupContainer(String id, IModel<?> _model) {
    super(id, _model);
  }


  @Override
  public String getMarkupId() {
    return getId();
  }

  @Override
  protected void onRender(MarkupStream markupStream) {
    final int markupStart = markupStream.getCurrentIndex();

    // Get mutable copy of next tag
    final ComponentTag openTag = markupStream.getTag();
    final ComponentTag tag = openTag.mutable();

    // Call any tag handler
    onComponentTag(tag);

    // Render open tag
    if (getRenderBodyOnly() == false) {
      renderComponentTag(tag);
    }
    markupStream.next();

    // Render the body only if open-body-close. Do not render if
    // open-close.
    if (tag.isOpen()) {
      // Render the body
      onComponentTagBody(markupStream, tag);
    }
    markupStream.setCurrentIndex(markupStart);

    final Iterator<?> childs = this.iterator();
    while (childs.hasNext()) {
      markupStream.setCurrentIndex(markupStart);

      final Component child = (Component) childs.next();

      child.render(getMarkupStream());

    }

    markupStream.setCurrentIndex(markupStart);
    markupStream.next();
    // Render close tag

    if (tag.isOpen()) {
      if (openTag.isOpen()) {
        // Get the close tag from the stream
        ComponentTag closeTag = markupStream.getTag();

        // If the open tag had its id changed
        if (tag.getNameChanged()) {
          // change the id of the close tag
          closeTag = closeTag.mutable();
          closeTag.setName(tag.getName());
        }

        // Render the close tag

        renderComponentTag(closeTag);

        markupStream.next();
      }

    }
  }

}
