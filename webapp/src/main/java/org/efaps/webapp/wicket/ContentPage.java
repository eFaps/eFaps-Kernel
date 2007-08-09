package org.efaps.webapp.wicket;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.resources.JavaScriptReference;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.webapp.models.IModelAbstract;

public abstract class ContentPage extends WebPage {

  private static final long serialVersionUID = -2374207555009145191L;

  public ContentPage() {

  }

  public ContentPage(PageParameters _parameters) throws Exception {
    this.addComponents();

  }

  protected void addComponents() throws Exception {
    IModelAbstract model = (IModelAbstract) super.getModel();
    add(new TitelPanel("eFapsTitel", model.getTitle()));
    add(new JavaScriptReference("eFapsDefaultScript", getClass(),
        "javascript/eFapsDefault.js"));
    add(new StyleSheetReference("eFapsDefaultCSS", getClass(),
        "css/eFapsDefault.css"));
    FooterPanel footerpanel = new FooterPanel("eFapsFooter", model);
    footerpanel.setVisible(model.isCreateMode() || model.isEditMode()
        || model.isSearchMode());
    add(footerpanel);

  }
}
