package org.efaps.webapp.wicket;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.wicketstuff.dojo.markup.html.container.DojoSimpleContainer;
import org.wicketstuff.dojo.markup.html.container.page.DojoPageContainer;
import org.wicketstuff.dojo.markup.html.container.split.DojoSplitContainer;

public class ContentContainerPage extends WebPage {

  private static final long serialVersionUID = 3169723830151134904L;

  private PageParameters parameters;

  public ContentContainerPage(PageParameters _parameters) {
    parameters = _parameters;

    DojoSplitContainer parentcontainer =
        new DojoSplitContainer("splitContainer");
    parentcontainer.setHeight("700px");
    add(parentcontainer);
    parentcontainer.setOrientation(DojoSplitContainer.ORIENTATION_HORIZONTAL);
    DojoSimpleContainer leftcontainer =
        new DojoSimpleContainer("containerlinks", "title1");
//    leftcontainer.setVisible(false);
    parentcontainer.add(leftcontainer);

    EFapsContainer page =
        new EFapsContainer("containerrechts", WebTablePage.class, _parameters);
    parentcontainer.add(page);

  }

  public class EFapsContainer extends DojoPageContainer {
    private PageParameters parameters;

    public EFapsContainer(String id, Class<?> pageClass) {
      super(id, pageClass);
    }

    public EFapsContainer(String id, Class<?> pageClass,
                          PageParameters _parameters) {
      super(id, pageClass);
      parameters = _parameters;
    }

    protected void onComponentTag(ComponentTag tag) {
      super.onComponentTag(tag);
      if (parameters != null) {
        tag.put("href", urlFor(getPageClass(), parameters));
      }
    }

    private static final long serialVersionUID = 1L;

  }
}
