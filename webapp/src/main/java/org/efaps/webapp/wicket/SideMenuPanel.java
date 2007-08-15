package org.efaps.webapp.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.efaps.webapp.components.MenuItem;
import org.efaps.webapp.models.EFapsApplicationSession;
import org.efaps.webapp.models.IMenuItemModel;

public class SideMenuPanel extends Panel {

  private static final String REPEATER_WICKETID = "eFapsMenuItems";

  private static final String ITEM_WICKETID = "eFapsMenuItemValue";

  private static final String CHILD_WICKETID = "eFapsMenuItemChild";

  private static final long serialVersionUID = -3982115652276054328L;

  private final RepeatingView repeater;

  public SideMenuPanel(final String id, final String _command) {
    super(id);
    this.repeater = new RepeatingView(REPEATER_WICKETID);

    super.add(this.repeater);

    add(HeaderContributor.forCss(super.getClass(), "css/SideMenuPanel.css"));
    try {
      super.setModel(new IMenuItemModel(_command));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    WebMarkupContainer item = getNewNestedItem();
    item.setOutputMarkupId(true);
    MenuItem x = new MenuItem(ITEM_WICKETID, getIMenuItemModel());
    x.add(new CloseEvent());
    x.setOutputMarkupId(true);

    item.add(x);

    if (getIMenuItemModel().hasChilds()) {
      SideMenuPanel parent = new SideMenuPanel(CHILD_WICKETID);
      parent.setOutputMarkupId(true);
      item.add(parent);
      parent.expandChilds(getIMenuItemModel());
    } else {
      item.add(new Empty(CHILD_WICKETID));
    }
    cache(item);
  }

  public SideMenuPanel(final String _id) {
    super(_id);
    this.repeater = new RepeatingView(REPEATER_WICKETID);
    super.add(this.repeater);
  }

  public void expandChilds(IMenuItemModel _parent) {
    setIMenuItemModel(_parent);
    for (IMenuItemModel child : getIMenuItemModel().getChilds()) {
      WebMarkupContainer item = getNewNestedItem();
      MenuItem menuitem = new MenuItem(ITEM_WICKETID, child);
      menuitem.add(new OpenTargetEvent());
      item.add(menuitem);

      item.add(new Empty(CHILD_WICKETID));
      cache(child, item);
    }
  }

  private void cache(WebMarkupContainer _container) {
    cache(getIMenuItemModel(), _container);
  }

  private void cache(IMenuItemModel _model, WebMarkupContainer _container) {
    EFapsApplicationSession session =
        (EFapsApplicationSession) super.getSession();
    session.cacheIMenuItem2Path(_model, _container);
  }

  private WebMarkupContainer getNewNestedItem() {
    WebMarkupContainer ret = new WebMarkupContainer(this.repeater.newChildId());
    ret.setOutputMarkupPlaceholderTag(true);
    this.repeater.add(ret);
    return ret;

  }

  public IMenuItemModel getIMenuItemModel() {
    IMenuItemModel ret = (IMenuItemModel) super.getModel();
    return ret;
  }

  public void setIMenuItemModel(IMenuItemModel _imenuitemmodel) {
    super.setModel(_imenuitemmodel);
  }

  public class OpenEvent extends AjaxEventBehavior {

    private static final long serialVersionUID = 4122563305593542324L;

    public OpenEvent() {
      super("onclick");
    }

    @Override
    protected void onEvent(AjaxRequestTarget _target) {

      IMenuItemModel model = (IMenuItemModel) this.getComponent().getModel();
      EFapsApplicationSession session =
          (EFapsApplicationSession) this.getComponent().getSession();

      for (IMenuItemModel child : model.getChilds()) {
        Component comp = session.getIMenuItem2Path(child);
        comp.setVisible(true);

      }

      // String newid = getRowsRepeater().newChildId();
      // ChildRow child = new ChildRow(newid);
      // child.setOutputMarkupId(true);
      // child.add(new SubMenu(ITEM_WICKETID));
      //
      // parentContainer.add(child);
      // _target.addComponent(child);
      //
      //      

      // javascript +=
      // getScript(this.getComponent().getMarkupId(), comp.getMarkupId());
      // _target.prependJavascript(javascript);

      this.getComponent().remove(this);
      this.getComponent().add(new CloseEvent());
      _target.addComponent(this.getComponent().getParent());

    }

    private String getScript(final String _markupid, final String compmarkupid) {
      StringBuilder ret = new StringBuilder();
      String pknot = "pk" + _markupid;
      String nknot = "nk" + compmarkupid;
      ret.append("var ").append(pknot).append(" = document.getElementById('")
          .append(_markupid).append("'); var ").append(nknot).append(
              " = document.createElement('li'); ").append(nknot).append(
              ".setAttribute('id','").append(compmarkupid).append(
              "'); document.getElementById('eFapsSideMenu').insertBefore(")
          .append(nknot).append(",").append(pknot).append(".nextSibling);");
      return ret.toString();

    }
  }

  public class CloseEvent extends AjaxEventBehavior {

    private static final long serialVersionUID = 4122563305593542324L;

    public CloseEvent() {
      super("onclick");
    }

    @Override
    protected void onEvent(AjaxRequestTarget _target) {

      IMenuItemModel model = (IMenuItemModel) this.getComponent().getModel();
      EFapsApplicationSession session =
          (EFapsApplicationSession) this.getComponent().getSession();
      for (IMenuItemModel child : model.getChilds()) {
        Component comp = session.getIMenuItem2Path(child);

        comp.setVisible(false);
        _target.addComponent(comp);
      }

      this.getComponent().remove(this);
      this.getComponent().add(new OpenEvent());
      _target.addComponent(this.getComponent());

    }
  }

  public class Empty extends WebComponent {
    private static final long serialVersionUID = -4467180974082507943L;

    public Empty(String id) {
      super(id);
      super.setVisible(false);
    }

  }

  public class OpenTargetEvent extends AjaxEventBehavior{

    public OpenTargetEvent() {
      super("onclick");
      
    }

    private static final long serialVersionUID = 1L;

    @Override
    protected void onEvent(AjaxRequestTarget target) {
      // TODO Auto-generated method stub
    System.out.print("test")  ;
    }
    
  }
}
