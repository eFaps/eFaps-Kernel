package org.efaps.webapp.wicket;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.webapp.components.Table;
import org.efaps.webapp.models.EFapsApplicationSession;

public class WebTablePage extends ContentPage {

  private static final long serialVersionUID = 7564911406648729094L;

  public WebTablePage(PageParameters _parameters) throws Exception {
    EFapsApplicationSession session = (EFapsApplicationSession) getSession();
    super.setModel(session.getITableModel(_parameters, null));
    this.addComponents();
  }

  @Override
  protected void addComponents() throws Exception {

    super.addComponents();
    add(new StyleSheetReference("WebTablePageCSS", getClass(),
    "webtablepage/WebTablePage.css"));
    add(new Table("eFapsTable", super.getModel()));
  }

}
