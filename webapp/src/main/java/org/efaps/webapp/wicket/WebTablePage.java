package org.efaps.webapp.wicket;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.webapp.components.table.WebTableContainer;
import org.efaps.webapp.components.table.header.TableHeaderPanel;
import org.efaps.webapp.models.EFapsApplicationSession;
import org.efaps.webapp.models.ITableModel;

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
    ITableModel model = (ITableModel) super.getModel();
    model.execute();
    add(new TableHeaderPanel("eFapsTableHeader", model));
    add(new WebTableContainer("eFapsTable", model));
  }

}
