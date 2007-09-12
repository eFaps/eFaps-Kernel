package org.efaps.webapp.pages;

import org.apache.wicket.Session;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.JavascriptUtils;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.webapp.EFapsSession;

public class LoginPage extends WebPage {

  private static final long serialVersionUID = 524408099967362477L;

  public LoginPage() {

    this.add(new StringHeaderContributor(""
        + JavascriptUtils.SCRIPT_OPEN_TAG
        + "function test4top() {\n"
        + "  if(top!=self) {\n"
        + "    top.location = self.location;\n"
        + "  }\n"
        + "}\n"
        + JavascriptUtils.SCRIPT_CLOSE_TAG));
    Form form = new Form("form") {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onComponentTag(final ComponentTag _tag) {
        super.onComponentTag(_tag);
      }

      @Override
      protected void onSubmit() {
        super.onSubmit();
        ((EFapsSession) this.getSession()).checkin();
        this.getRequestCycle().setResponsePage(MainPage.class);
      }

    };
    this.add(form);
    form.add(new Label("formname", new Model(DBProperties.getProperty(
        "Login.Name.Label", Session.get().getLocale().getLanguage()))));

    form.add(new Label("formpwd", new Model(DBProperties.getProperty(
        "Login.Password.Label", Session.get().getLocale().getLanguage()))));

    Button button = new Button("formbutton");
    form.add(button);

    button.add(new Label("formbuttonlabel", new Model(DBProperties.getProperty(
        "Login.Button.Label", Session.get().getLocale().getLanguage()))));

  }
}
