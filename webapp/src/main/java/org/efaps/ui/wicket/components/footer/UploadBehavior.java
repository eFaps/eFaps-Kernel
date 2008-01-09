/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.ui.wicket.components.footer;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;

import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.ui.wicket.components.MultiListener;
import org.efaps.ui.wicket.models.AbstractModel;
import org.efaps.util.EFapsException;

public class UploadBehavior extends AbstractBehavior implements MultiListener {

  private static final long serialVersionUID = 1L;

  private Component component;

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.behavior.AbstractBehavior#bind(org.apache.wicket.Component)
   */
  @Override
  public void bind(final Component _component) {
    super.bind(_component);
    this.component = _component;
  }

  public void onSubmit() {
    final MultipartServletWebRequest multi =
        (MultipartServletWebRequest) this.component.getRequestCycle()
            .getRequest();
    multi.getFiles();
    System.out.println("hier ist der Multi");

    try {
      executeEvents();
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private boolean executeEvents() throws EFapsException {


    boolean ret = true;
    final List<Return> returns =
        ((AbstractModel) this.component.getParent().getModel()).executeEvents(null);
    for (final Return oneReturn : returns) {
      if (oneReturn.get(ReturnValues.TRUE) == null && !oneReturn.isEmpty()) {
        final String key = (String) oneReturn.get(ReturnValues.VALUES);
        ret = false;
        break;
      }
    }
    return ret;
  }

}
