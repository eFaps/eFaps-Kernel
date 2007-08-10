package org.efaps.webapp.wicket;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class HomePage extends WebPage {

  private static final long serialVersionUID = 524408099967362477L;

  public HomePage() {
    BookmarkablePageLink x =
        new BookmarkablePageLink("webformlink", WebFormPage.class);
    x.setParameter("oid", "64.1");
    x.setParameter("command", "Admin_User_PersonTree");
    add(x);
    x = new BookmarkablePageLink("webtablelink", WebTablePage.class);
    x.setParameter("command", "Admin_DBProperties_MyDesk");
    add(x);
    x = new BookmarkablePageLink("create", WebFormPage.class);
    x.setParameter("command", "Admin_User_PersonMyDesk_Menu_Action_Create");
    add(x);
    
    
    x = new BookmarkablePageLink("main", MainPage.class);
   
    add(x);
    
    
  }
}
