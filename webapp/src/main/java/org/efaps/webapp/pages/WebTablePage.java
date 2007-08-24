package org.efaps.webapp.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.components.table.WebTableContainer;
import org.efaps.webapp.components.table.header.TableHeaderPanel;
import org.efaps.webapp.models.TableModel;

public class WebTablePage extends ContentPage {

  private static final long serialVersionUID = 7564911406648729094L;

  public WebTablePage(PageParameters _parameters) throws Exception {
    EFapsSession session = (EFapsSession) getSession();
    super.setModel(session.getITableModel(_parameters, null));
    this.addComponents();
  }

  @Override
  protected void addComponents() throws Exception {

    super.addComponents();
    add(new StyleSheetReference("WebTablePageCSS", getClass(),
        "webtablepage/WebTablePage.css"));
    TableModel model = (TableModel) super.getModel();
    model.execute();
    add(new TableHeaderPanel("eFapsTableHeader", model));
    add(new WebTableContainer("eFapsTable", model));
  }

}
