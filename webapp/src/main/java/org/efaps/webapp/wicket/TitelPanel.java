package org.efaps.webapp.wicket;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;


public class TitelPanel extends Panel {

  public TitelPanel(String _id, String _titel) {
    super(_id);
    add(new Label("eFapsTitel", _titel));
   
  }

  /**
   * 
   */
  private static final long serialVersionUID = -1282979592409784911L;

}

