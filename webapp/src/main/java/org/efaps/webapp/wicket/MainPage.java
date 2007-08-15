package org.efaps.webapp.wicket;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

public class MainPage extends WebPage {

  private static final long serialVersionUID = -4231606613730698766L;

  private static String INLINEFRAMENAME = "ILFP";

  public MainPage() {

    Link x = new Link("eFapsMainMenu") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick() {
        PageParameters u =
            new PageParameters("command=Admin_User_PersonMyDesk");
//        Admin_User_PersonMyDesk 
//        Admin_UI_CommandMyDesk     
//        command=Admin_User_PersonTree,oid=64.1    
        
        InlineFrame c =
            new InlineFrame("eFapsContentFrame", PageMap
                .forName(INLINEFRAMENAME), WebTablePage.class, u);

        this.getPage().addOrReplace(c);
      }

    };

    add(x);

    add(new InlineFrame("eFapsContentFrame", getPageMap(), EmptyPage.class));

    add(new InlineFrame("eFapsFrameHidden", getPageMap(), EmptyPage.class));
    add(new Label("eFapsWelcomeLabel", DBProperties
        .getProperty("LogoRowInclude.Welcome.Label")));

    try {
      add(new Label("eFapsWelcomePersonFirstNameLabel", Context
          .getThreadContext().getPerson().getFirstName()));
      add(new Label("eFapsWelcomePersonLastNameLabel", Context
          .getThreadContext().getPerson().getLastName()));
      add(new Label("eFapsLogoVersionLabel", DBProperties
          .getProperty("LogoRowInclude.Version.Label")));

      add(new StyleSheetReference("eFapsMainPageCSS", getClass(),
          "mainpage/MainPage.css"));
      add(HeaderContributor.forJavaScript(super.getClass(),
          "javascript/eFapsDefault.js"));
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
