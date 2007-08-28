package org.efaps.webapp.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.webapp.components.table.WebFormContainer;
import org.efaps.webapp.models.FormModel;

public class WebFormPage extends ContentPage {

  private static final long serialVersionUID = -3554311414948286302L;

  public WebFormPage(PageParameters _parameters) throws Exception {

    super.setModel(new FormModel(_parameters));
    this.addComponents();

  }

  @Override
  protected void addComponents() throws Exception {

    super.addComponents();
    add(new StyleSheetReference("WebFormPageCSS", getClass(),
        "webformpage/WebFormPage.css"));

    add(new WebFormContainer("eFapsFormTable", super.getModel()));

  }

}
