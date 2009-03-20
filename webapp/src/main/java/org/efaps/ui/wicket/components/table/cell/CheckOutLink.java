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

package org.efaps.ui.wicket.components.table.cell;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id$
 */
public class CheckOutLink extends WebMarkupContainer {

  private static final long serialVersionUID = 1L;

  public CheckOutLink(final String _wicketId, final IModel<?>  _model) {
    super(_wicketId, _model);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
   */
  @Override
  protected void onComponentTag(final ComponentTag _tag) {
    super.onComponentTag(_tag);
    final UITableCell model =  (UITableCell) super.getDefaultModelObject();
    final StringBuilder href = new StringBuilder();

    try {
      href.append(model.getReference()).append("oid=").append(model.getInstance().getOid());
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    _tag.put("href", href);

    if (model.getTarget() == Target.POPUP) {
      _tag.put("target", "_blank");
    }
  }
}
