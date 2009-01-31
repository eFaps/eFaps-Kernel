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

package org.efaps.ui.wicket.components.tree;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.ui.wicket.models.StructurBrowserModel;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;

/**
 * Class renders a Link used to sort a Structur Browser.
 *
 * @author jmox
 * @version $Id$
 */
public class SortHeaderColumnLink extends Link<UIStructurBrowser> {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The value for the header.
   */
  private final String header;

  /**
   * Constructor.
   *
   * @param _wicketId   wicket id for this component
   * @param _header     header to set
   * @param _model      model for this component
   */
  public SortHeaderColumnLink(final String _wicketId,
                              final String _header,
                              final IModel<UIStructurBrowser> _model) {
    super(_wicketId, _model);
    this.header = _header;
  }

  /**
   * On the tag the class is set.
   * @param _tag tag to be set
   */
  @Override
  protected void onComponentTag(final ComponentTag _tag) {
    super.onComponentTag(_tag);
    final UIStructurBrowser structurBrowser
                                  = (UIStructurBrowser) getDefaultModelObject();
    if (structurBrowser.getSortDirection().equals(SortDirection.ASCENDING)) {
      _tag.put("class", "sortLabelAsc");
    } else {
      _tag.put("class", "sortLabelDsc");
    }
  }

  /**
   * The body of the component is filled with the header.
   * @param _markupStream   MarkupStream
   * @param _openTag        ComponentTag
   */
  @Override
  protected void onComponentTagBody(final MarkupStream _markupStream,
                                    final ComponentTag _openTag) {
    replaceComponentTagBody(_markupStream, _openTag, this.header);
  }

  /**
   * On click the model is sorted and a new page with this model as response
   * rendered.
   */
  @Override
  public void onClick() {

    final UIStructurBrowser structurBrowser
                                  = (UIStructurBrowser) getDefaultModelObject();
    //toggle sort direction
    if (structurBrowser.getSortDirection() == SortDirection.NONE
        || structurBrowser.getSortDirection() == SortDirection.DESCENDING) {
      structurBrowser.setSortDirection(SortDirection.ASCENDING);
    } else {
      structurBrowser.setSortDirection(SortDirection.DESCENDING);
    }
    structurBrowser.sort();
    getRequestCycle().setResponsePage(
        new StructurBrowserPage(new StructurBrowserModel(structurBrowser)));
  }
}
