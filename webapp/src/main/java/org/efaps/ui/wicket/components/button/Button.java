/*
 * Copyright 2003-2007 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.button;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author jmox
 * @version $Id$
 */
public class Button extends Panel {

  private static final long serialVersionUID = 1L;

  public static final String LINKID = "buttonLink";

  public static final ResourceReference ICON_CANCEL =
      new ResourceReference(Button.class, "cancel.png");

  public static final ResourceReference ICON_ACCEPT =
      new ResourceReference(Button.class, "accept.png");

  public static final ResourceReference ICON_NEXT =
      new ResourceReference(Button.class, "next.png");

  private final Image image = new Image("icon");

  private boolean imageHasResource = false;

  public Button(final String _wicketId, final WebMarkupContainer _link,
                final String _label) {
    this(_wicketId, _link, _label, null);
  }

  public Button(final String _wicketId, final WebMarkupContainer _link,
                final String _label, final ResourceReference _icon) {
    super(_wicketId);
    this.add(_link);
    _link.add(new ButtonStyleBehavior());
    final Label buttonlabel = new Label("buttonLabel", _label);
    buttonlabel.add(new ButtonStyleBehavior());
    _link.add(buttonlabel);
    _link.add(this.image);
    if (_icon != null) {
      this.image.setImageResourceReference(_icon);
      this.imageHasResource = true;
    }

  }

  public String getLinkWicketId() {
    return LINKID;
  }

  public void setIconReference(final ResourceReference _icon) {
    this.image.setImageResourceReference(_icon);
    this.imageHasResource = true;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.Component#onBeforeRender()
   */
  @Override
  protected void onBeforeRender() {
    if (!this.imageHasResource) {
      this.image.setVisible(false);
    }
    super.onBeforeRender();
  }

}
