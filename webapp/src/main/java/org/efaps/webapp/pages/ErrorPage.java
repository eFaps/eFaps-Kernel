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

package org.efaps.webapp.pages;

import java.text.MessageFormat;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.util.EFapsException;

public class ErrorPage extends WebPage {

  private static final long serialVersionUID = 1L;

  public ErrorPage(final Exception _exception) {
    String errorMessage = _exception.getMessage();
    String errorAction = "";
    String errorKey = "";
    String errorId = "";
    String errorAdvanced = "";

    if (_exception instanceof EFapsException) {

      EFapsException eFapsException = (EFapsException) _exception;
      errorKey =
          eFapsException.getClassName().getName()
              + "."
              + eFapsException.getId();
      errorId = DBProperties.getProperty(errorKey + ".Id");
      errorMessage = DBProperties.getProperty(errorKey + ".Message");
      errorAction = DBProperties.getProperty(errorKey + ".Action");
      if (eFapsException.getArgs() != null) {
        errorMessage =
            MessageFormat.format(errorMessage, eFapsException.getArgs());
      }

    } else {
      if (errorMessage == null) {
        errorMessage = _exception.toString();
      }
    }

    StackTraceElement[] traceElements = _exception.getStackTrace();
    for (int i = 0; i < traceElements.length; i++) {
      errorAdvanced += traceElements[i].toString() + "\n";
    }
    this.add(new StringHeaderContributor("<title>"
        + DBProperties.getProperty("JSPPage.Exception.TextTitle")
        + "</title>"));

    add(new StyleSheetReference("css", getClass(), "errorpage/ErrorPage.css"));

    add(new Label("errorIDLabel", DBProperties
        .getProperty("JSPPage.Exception.TextId")));
    add(new Label("errorID", errorId));

    add(new Label("errorMsgLabel", DBProperties
        .getProperty("JSPPage.Exception.TextMessage")));
    add(new MultiLineLabel("errorMsg", errorMessage));

    final WebMarkupContainer advanced = new WebMarkupContainer("advanced");

    AjaxLink ajaxlink = new AjaxLink("openclose") {

      private boolean expanded = false;

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(final AjaxRequestTarget _target) {
        this.expanded = !this.expanded;
        String text;
        if (this.expanded) {
          text = "less";
        } else {
          text = "more";
        }
        advanced.setVisible(this.expanded);

        Label label = new Label("opencloseLabel", text);

        label.setOutputMarkupId(true);

        this.replace(label);

        _target.addComponent(label);
        _target.addComponent(advanced);

      }
    };
    this.add(ajaxlink);

    ajaxlink.add(new Label("opencloseLabel", "more").setOutputMarkupId(true));

    if (!(errorAdvanced.length() > 0)) {
      ajaxlink.setVisible(false);
    }

    this.add(advanced);
    advanced.setVisible(false);
    advanced.setOutputMarkupPlaceholderTag(true);

    advanced.add(new MultiLineLabel("advancedMsg", errorAdvanced));

    add(new Label("errorActLabel", DBProperties
        .getProperty("JSPPage.Exception.TextAction")));
    add(new Label("errorAct", errorAction));

  }

  @Override
  public boolean isErrorPage() {
    return true;
  }

  @Override
  public boolean isVersioned() {
    return false;
  }

}
