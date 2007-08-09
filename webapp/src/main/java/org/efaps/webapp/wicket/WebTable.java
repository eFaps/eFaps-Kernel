package org.efaps.webapp.wicket;

import org.apache.wicket.PageParameters;

import org.efaps.webapp.components.Table;
import org.efaps.webapp.models.EFapsApplicationSession;

public class WebTable extends ContentPage {

  private static final long serialVersionUID = 7564911406648729094L;

 

  public WebTable(PageParameters _parameters) throws Exception {
    EFapsApplicationSession session = (EFapsApplicationSession) getSession();
    super.setModel(session.getITableModel(null));
    this.addComponents();

  }

  @Override
  protected void addComponents() throws Exception {

    super.addComponents();
    add(new Table("eFapsWebTable", super.getModel()));
  }

}
