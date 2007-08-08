package org.efaps.webapp.wicket;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

import org.efaps.webapp.components.FormTable;
import org.efaps.webapp.models.EFapsApplicationSession;
import org.efaps.webapp.models.IFormModel;

public class WebForm extends WebPage {

  private static final long serialVersionUID = -3554311414948286302L;

  private IFormModel model;

 

  public WebForm() {

    add(new Label("message", "kein Parameter"));
  }

  public WebForm(PageParameters _parameters) throws Exception {
    EFapsApplicationSession session = (EFapsApplicationSession) getSession();
    model= session.getIFormModel(null);
    
    
    add(new TitelPanel("eFapsTitel", model.getTitle()));
    add(new FormTable("eFapsFormTable", model));
  }

}
