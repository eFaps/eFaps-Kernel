package org.efaps.webapp.wicket;

import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.WebPage;

import org.efaps.webapp.models.IModelAbstract;

public  abstract class ContentPage extends WebPage {

  private static final long serialVersionUID = -2374207555009145191L;

   

  protected void addComponents() throws Exception {
    IModelAbstract model = (IModelAbstract) super.getModel();
    add(new TitelPanel("eFapsTitel", model.getTitle()));
    add(HeaderContributor.forCss(super.getClass(), "css/eFapsDefault.css"));
    add(HeaderContributor.forJavaScript(super.getClass(),
        "javascript/eFapsDefault.js"));
    
    FooterPanel footerpanel = new FooterPanel("eFapsFooter", model);
    footerpanel.setVisible(model.isCreateMode() || model.isEditMode()
        || model.isSearchMode());
    add(footerpanel);

  }
}
