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
    add(new TitelPanel("eFapsTitel", ((IModelAbstract) super.getModel())
        .getTitle()));
    
    add(new JavaScriptReference("eFapsDefaultScript", getClass(), "eFapsDefault.js"));
    add(new StyleSheetReference("eFapsDefaultCSS", getClass(), "eFapsDefault.css"));
  }

}
