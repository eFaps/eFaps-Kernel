package org.efaps.webapp.pages;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.wicketstuff.dojo.markup.html.container.DojoSimpleContainer;
import org.wicketstuff.dojo.markup.html.container.split.DojoSplitContainer;

import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.components.listmenu.ListMenuPanel;

public class ContentContainerPage extends WebPage {

  private static final long serialVersionUID = 3169723830151134904L;

  public ContentContainerPage(PageParameters _parameters) {
    super(PageMap.forName(MainPage.INLINEFRAMENAME));
    final ClientProperties properties =
        ((WebClientInfo) getRequestCycle().getClientInfo()).getProperties();

    add(new StyleSheetReference("ContentContainerPageCSS", getClass(),
        "contentcontainerpage/ContentContainerPage.css"));

    DojoSplitContainer parentcontainer =
        new DojoSplitContainer("eFapsSplitContainer");
    parentcontainer.setHeight(properties.getBrowserHeight() + "px");

    add(parentcontainer);
    parentcontainer.setOrientation(DojoSplitContainer.ORIENTATION_HORIZONTAL);

    parentcontainer.add(new ListMenuPanel("eFapsListMenu", _parameters));

    DojoSimpleContainer container =
        new DojoSimpleContainer("containerrechts", "Content");
    parentcontainer.add(container);
    //
    WebMarkupContainer parent = new WebMarkupContainer("aktParent");
    parent.setOutputMarkupId(true);
    container.add(parent);
    InlineFrame inline =
        new InlineFrame("eFapsContentContainerFrame", PageMap
            .forName("content"), WebFormPage.class, _parameters);
    

    parent.add(inline);

    // EFapsContainerComponent content =
    // new EFapsContainerComponent("eFapsContentContainer", WebFormPage.class,
    // _parameters);
    // container.add(content);
  }

  @Override
  protected void onBeforeRender() {

    super.onBeforeRender();
    ((EFapsSession) this.getSession()).setContentContainer(this.getNumericId(),
        this.getCurrentVersionNumber());
  }
}
