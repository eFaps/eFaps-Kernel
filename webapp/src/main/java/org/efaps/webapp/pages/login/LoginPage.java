package org.efaps.webapp.pages.login;

import org.apache.wicket.Session;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.JavascriptUtils;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.webapp.EFapsNoAuthorizationNeededInterface;
import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.pages.main.MainPage;

/**
 * This class renders the LoginPage for the eFpas-WebApplication.<br>
 * It is called from the #
 * {@link #onRuntimeException(org.efaps.webapp.EFapsWebRequestCycle)} method, in
 * the case that noone is logged in. In case of a wrong login try, an additional
 * Message is shwon to the User.
 *
 * @author jmox
 * @version $Id$
 */
public class LoginPage extends WebPage implements
    EFapsNoAuthorizationNeededInterface {

  private static final long serialVersionUID = 524408099967362477L;

  /**
   * standart Constructor showing no Message
   */
  public LoginPage() {
    this(false);
  }

  /**
   * Constructor showing a "wrong Login Message" depending on the Parameter
   *
   * @param _msg
   *                true, if "wrong Login Message: should be shown, else false
   */
  public LoginPage(final boolean _msg) {

    this.add(new StringHeaderContributor(""
        + JavascriptUtils.SCRIPT_OPEN_TAG
        + "function test4top() {\n"
        + "  if(top!=self) {\n"
        + "    top.location = self.location;\n"
        + "  }\n"
        + "}\n"
        + JavascriptUtils.SCRIPT_CLOSE_TAG));

    this.add(new StyleSheetReference("css", getClass(), "LoginPage.css"));

    Form form = new Form("form") {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onSubmit() {
        super.onSubmit();
        EFapsSession session = ((EFapsSession) this.getSession());
        session.checkin();
        if (session.isLogedIn()) {
          this.getRequestCycle().setResponsePage(MainPage.class);
        } else {
          LoginPage page = new LoginPage(true);
          this.getRequestCycle().setResponsePage(page);
        }
      }

    };
    this.add(form);
    form.add(new Label("formname", new Model(DBProperties.getProperty(
        "Login.Name.Label", Session.get().getLocale().getLanguage()))));

    form.add(new Label("formpwd", new Model(DBProperties.getProperty(
        "Login.Password.Label", Session.get().getLocale().getLanguage()))));

    Button button = new Button("formbutton") {

      private static final long serialVersionUID = 1L;

      /*
       * (non-Javadoc)
       *
       * @see org.apache.wicket.markup.html.form.Button#onComponentTag(org.apache.wicket.markup.ComponentTag)
       */
      @Override
      protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.put("type", "submit");
      }
    };

    form.add(button);

    button.add(new Label("formbuttonlabel", new Model(DBProperties.getProperty(
        "Login.Button.Label", Session.get().getLocale().getLanguage()))));

    if (_msg) {
      this.add(new Label("msg", new Model(DBProperties.getProperty(
          "Login.Wrong.Label", Session.get().getLocale().getLanguage()))));
    } else {
      this.add(new WebMarkupContainer("msg").setVisible(false));
    }
  }
}
