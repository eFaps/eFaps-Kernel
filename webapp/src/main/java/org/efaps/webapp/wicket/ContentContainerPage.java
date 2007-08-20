package org.efaps.webapp.wicket;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.wicketstuff.dojo.markup.html.container.split.DojoSplitContainer;

import org.efaps.webapp.components.EFapsContainerComponent;
import org.efaps.webapp.components.sidemenu.SideMenuPanel;
import org.efaps.webapp.models.EFapsApplicationSession;

public class ContentContainerPage extends WebPage {

  @Override
  protected void onBeforeRender() {
    
    super.onBeforeRender();
    ((EFapsApplicationSession) this.getSession()).setContentContainer( this.getNumericId(),this.getCurrentVersionNumber() );
  }

  private static final long serialVersionUID = 3169723830151134904L;

  private PageParameters parameters;

  public ContentContainerPage(PageParameters _parameters) {
    parameters = _parameters;

    DojoSplitContainer parentcontainer =
        new DojoSplitContainer("eFapsSplitContainer");
    parentcontainer.setHeight("700px");
//    parentcontainer.setActiveSizing(true);
    
    add(parentcontainer);
    parentcontainer.setOrientation(DojoSplitContainer.ORIENTATION_HORIZONTAL);

    SideMenuPanel leftcontainer =
        new SideMenuPanel("eFapsSideMenu", _parameters);

    parentcontainer.add(leftcontainer);

    
    
    EFapsContainerComponent page =
        new EFapsContainerComponent("containerrechts", WebFormPage.class,
            _parameters);
    parentcontainer.add(page);

  }

}
