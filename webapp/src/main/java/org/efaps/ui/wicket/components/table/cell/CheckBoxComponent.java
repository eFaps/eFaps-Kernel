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
import org.apache.wicket.markup.html.WebComponent;

/**
 * @author jmox
 * @version $Id$
 *
 */
public class CheckBoxComponent extends WebComponent {
  private static final long serialVersionUID = 1L;

  private final String oid;

  public CheckBoxComponent(String id, String _oid) {
    super(id);
    this.oid = _oid;
  }

  @Override
  protected void onComponentTag(ComponentTag tag) {
    tag.getAttributes().put("type", "checkbox");
    tag.getAttributes().put("name", "selectedRow");
    tag.getAttributes().put("value", this.oid);
    tag.setName("input");
  }

}