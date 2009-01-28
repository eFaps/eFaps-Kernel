/*
 * Copyright 2003 - 2009 The eFaps Team
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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.efapscontent;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * @author jmox * @version $Id:StaticImageComponent.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class StaticImageComponent extends WebComponent {

  private static final long serialVersionUID = 1L;

  private String url;

  public StaticImageComponent(final String _wicketid) {
    super(_wicketid);
  }

  public StaticImageComponent(final String _wicketid, final Class<?> _scope,
                              final String _name) {
    this(_wicketid, _scope.getPackage().getName() + "." + _name);
  }

  public StaticImageComponent(final String _wicketid, final String _url) {
    super(_wicketid);
    this.url = _url;
  }

  public StaticImageComponent(final String _wicketid,
                              final EFapsContentReference _reference) {
    super(_wicketid);
    this.url = _reference.getImageUrl();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
   */
  @Override
  protected void onComponentTag(ComponentTag tag) {
    super.onComponentTag(tag);
    checkComponentTag(tag, "img");
    tag.put("src", this.url);
  }

  /**
   * This is the getter method for the instance variable {@link #url}.
   *
   * @return value of instance variable {@link #url}
   */
  public String getUrl() {
    return this.url;
  }

  /**
   * This is the setter method for the instance variable {@link #url}.
   *
   * @param _url
   *                the url to set
   */
  public void setUrl(final String _url) {
    this.url = _url;
  }

  public void setReference(final EFapsContentReference _reference) {
    this.url = _reference.getImageUrl();
  }

}
