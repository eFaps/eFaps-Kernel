/*
 * Copyright 2003 - 2007 The eFaps Team
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

package org.efaps.webapp.tags;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.myfaces.custom.dialog.ModalDialogRenderer;

public class ModalDialogsRenderer extends ModalDialogRenderer {
  public static final String RENDERER_TYPE =
      "org.efaps.webapp.tags.ModalDialogs";

  public void encodeBegin(FacesContext context, UIComponent _component)
      throws IOException {
    UIModalDialogs uimodaldialogs = (UIModalDialogs) _component;
    for (uimodaldialogs.getIterator(); uimodaldialogs.getIterator().hasNext();) {
      EFapsModalDialog modaldialog =
          (EFapsModalDialog) uimodaldialogs.getIterator().next();
      uimodaldialogs.setActiveModalDialog(modaldialog);
      super.encodeBegin(context, _component);
      StringBuffer buf = new StringBuffer();

      buf.append("<table border=\"0\" cellspacing=\"10\" >");
      buf.append("<tr><td  colspan=\"2\" align=\"center\">");
      buf.append(modaldialog.getQuestion());
      buf.append("</td></tr><tr><td align=\"center\" width=\"50%\">");
      buf.append("<button type=\"button\" name=\"cancel\" value=\"\"");
      buf.append(" onclick=\"");
      buf.append(modaldialog.getScript());
      buf.append("\">");
      buf.append(modaldialog.getSubmitText());
      buf.append("</button>");
      buf.append("</td><td align=\"center\" width=\"50%\">");
      buf.append("<button type=\"button\" id=\"");
      buf.append(uimodaldialogs.getHiderIds());
      buf.append("\" value=\"\"");
      buf.append("onclick=\"dojo.widget.byId('");
      buf.append(modaldialog.getDialogId());
      buf.append("').hide();\">");
      buf.append(modaldialog.getCancelText());
      buf.append("</button>");
      buf.append("</td></tr></table>");
      context.getResponseWriter().append(buf.toString());

      super.encodeEnd(context, _component);
    }
  }

  public void encodeEnd(FacesContext context, UIComponent component)
      throws IOException {

  }

}
