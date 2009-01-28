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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.error;

import java.text.MessageFormat;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

/**
 * This Page is the ErrorPage for the eFaps-Webapplication.<br>
 * It renders a Page that shows the EFapsException in a userfriendly way.
 *
 * @author jmox
 * @version $Id:ErrorPage.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class ErrorPage extends WebPage {

  private static final long serialVersionUID = 1L;

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ErrorPage.class);

  /**
   * reference to the StyleSheet of this Page stored in the eFaps-DataBase
   */
  private static final EFapsContentReference CSS =
      new EFapsContentReference(ErrorPage.class, "ErrorPage.css");

  /**
   * Constructor adding all Components
   *
   * @param _exception
   */
  public ErrorPage(final Exception _exception) {
    super();
    this.add(StaticHeaderContributor.forCss(CSS));

    LOG.error("ErrorPage was called", _exception);

    String errorMessage = _exception.getMessage();
    String errorAction = "";
    String errorKey = "";
    String errorId = "";
    String errorAdvanced = "";

    if (_exception instanceof EFapsException) {
      this.add(StaticHeaderContributor.forCss(CSS));

      final EFapsException eFapsException = (EFapsException) _exception;
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

    final StackTraceElement[] traceElements = _exception.getStackTrace();
    for (int i = 0; i < traceElements.length; i++) {
      errorAdvanced += traceElements[i].toString() + "\n";
    }
    this.add(new StringHeaderContributor("<title>"
        + DBProperties.getProperty("ErrorPage.Titel")
        + "</title>"));

    add(new Label("errorIDLabel", DBProperties
        .getProperty("ErrorPage.Id.Label")));
    add(new Label("errorID", errorId));

    add(new Label("errorMsgLabel", DBProperties
        .getProperty("ErrorPage.Message.Label")));
    add(new MultiLineLabel("errorMsg", errorMessage));

    final WebMarkupContainer advanced = new WebMarkupContainer("advanced");

    final AjaxLink<Object> ajaxlink = new AjaxLink<Object>("openclose") {

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
        .getProperty("ErrorPage.Action.Label")));
    add(new Label("errorAct", errorAction));

  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.Page#isErrorPage()
   */
  @Override
  public boolean isErrorPage() {
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.Component#isVersioned()
   */
  @Override
  public boolean isVersioned() {
    return false;
  }

}
