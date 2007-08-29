package org.efaps.webapp.pages;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.webapp.components.footer.FooterPanel;
import org.efaps.webapp.components.menu.MenuPanel;
import org.efaps.webapp.components.titel.TitelPanel;
import org.efaps.webapp.models.ModelAbstract;

public abstract class ContentPage extends WebPage {

  private static final long serialVersionUID = -2374207555009145191L;

  protected void addComponents() throws Exception {
    ModelAbstract model = (ModelAbstract) super.getModel();
    add(new TitelPanel("eFapsTitel", model.getTitle()));
    add(new StyleSheetReference("ContentPageCSS", getClass(),
        "contentpage/ContentPage.css"));

    add(new MenuPanel("eFapsMenu", model));
    FooterPanel footerpanel = new FooterPanel("eFapsFooter", model);
    footerpanel.setVisible(model.isCreateMode() || model.isEditMode()
        || model.isSearchMode());
    add(footerpanel);

  }
}
