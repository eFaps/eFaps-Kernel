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

package org.efaps.admin.datamodel.ui;

import org.efaps.util.EFapsException;

/**
 * @author jmo
 * @version $Id$
 */
public abstract class AbstractUI implements UIInterface {

  public int compare(final FieldValue _fieldValue, final FieldValue _fieldValue2) {
    return 0;
  }

  public String getCreateHtml(final FieldValue _fieldValue)
      throws EFapsException {
    return "create";
  }

  public String getEditHtml(final FieldValue _fieldValue) throws EFapsException {
    return "edit";
  }

  public String getSearchHtml(final FieldValue _fieldValue)
      throws EFapsException {
    return "search";
  }

  public String getViewHtml(final FieldValue _fieldValue) throws EFapsException {
    return "view";
  }

}
