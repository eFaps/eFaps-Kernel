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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.resources;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * A HeaderContributor for Content wich is stored inside the eFaps-DataBase.<br>
 * Unlike other HeaderContributor the Instance of StaticHeaderContributor migtht
 * be removed on rendering from the component to merge it with other
 * StaticHeaderContributor and added as a new merged StaticHeaderContributor to
 * a ParentComponent. To prevent a StaticHeaderContributor from merging for e.g.
 * keep the Sequence of Behaviors in a Compoment {@link #merged} must be set to
 * true.
 *
 * @author jmox
 * @version $Id$
 */
public class StaticHeaderContributor extends HeaderContributor {

  private static final long serialVersionUID = 1L;

  /**
   * this enum is used to distinguish between the different Types of the Header
   */
  public static enum HeaderType {
    CSS,
    JS;
  }

  /**
   * the Reference this Behavior is connected to
   */
  private final EFapsContentReference reference;

  /**
   * is this StaticHeaderContributor allready merged
   */
  private boolean merged = false;

  /**
   * Component this StaticHeaderContributor is bind to
   */
  private Component component;

  /**
   * The HeaderType of this StaticHeaderContributor
   */
  private HeaderType headerType;

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.behavior.AbstractBehavior#bind(org.apache.wicket.Component)
   */
  @Override
  public void bind(final Component _component) {
    this.component = _component;
  }

  /**
   * Constructor setting the IHeaderContributor in the SuperClass and the
   * Reference
   *
   * @param _headerContributor
   * @param _reference
   */
  public StaticHeaderContributor(final IHeaderContributor _headerContributor,
                                 final EFapsContentReference _reference) {
    super(_headerContributor);
    this.reference = _reference;
  }

  /**
   * Static method to get a StaticHeaderContributor for CSS that will be merged
   *
   * @param _reference
   *                Reference to the Content
   * @return
   */
  public static final StaticHeaderContributor forCss(
                                                     final EFapsContentReference _reference) {
    return StaticHeaderContributor.forCss(_reference, false);
  }

  /**
   * * Static method to get a StaticHeaderContributor for CSS
   *
   * @param _reference
   *                Reference to the Content
   * @param _merged
   *                should this StaticHeaderContributor merged
   * @return
   */
  public static final StaticHeaderContributor forCss(
                                                     final EFapsContentReference _reference,
                                                     final boolean _merged) {
    final StaticHeaderContributor ret =
        new StaticHeaderContributor(new IHeaderContributor() {

          private static final long serialVersionUID = 1L;

          public void renderHead(IHeaderResponse response) {
            response.renderCSSReference(_reference.getStaticContentUrl());
          }
        }, _reference);
    ret.setHeaderType(HeaderType.CSS);
    return ret;
  }

  /**
   * Static method to get a StaticHeaderContributor for JavaScript that will be
   * merged
   *
   * @param _reference
   *                Reference to the Content
   * @return
   */
  public static final StaticHeaderContributor forJavaScript(
                                                            final EFapsContentReference _reference) {
    return StaticHeaderContributor.forJavaScript(_reference, false);
  }

  /**
   * * Static method to get a StaticHeaderContributor for JavaScript
   *
   * @param _reference
   *                Reference to the Content
   * @param _nomerge
   *                should this StaticHeaderContributor not bemerged
   * @return
   */
  public static final StaticHeaderContributor forJavaScript(
                                                            final EFapsContentReference _reference,
                                                            boolean _nomerge) {

    final StaticHeaderContributor ret =
        new StaticHeaderContributor(new IHeaderContributor() {

          private static final long serialVersionUID = 1L;

          public void renderHead(IHeaderResponse response) {
            response
                .renderJavascriptReference(_reference.getStaticContentUrl());
          }
        }, _reference);
    ret.setHeaderType(HeaderType.JS);
    ret.merged=_nomerge;
    return ret;
  }

  /**
   * This is the getter method for the instance variable {@link #merged}.
   *
   * @return value of instance variable {@link #merged}
   */
  public boolean isMerged() {
    return this.merged;
  }

  /**
   * This is the setter method for the instance variable {@link #merged}.
   *
   * @param merged
   *                the merged to set
   */
  public void setMerged(boolean merged) {
    this.merged = merged;
  }

  /**
   * This is the getter method for the instance variable {@link #component}.
   *
   * @return value of instance variable {@link #component}
   */
  public Component getComponent() {
    return this.component;
  }

  /**
   * This is the getter method for the instance variable {@link #reference}.
   *
   * @return value of instance variable {@link #reference}
   */
  public EFapsContentReference getReference() {
    return this.reference;
  }

  /**
   * This is the getter method for the instance variable {@link #headerType}.
   *
   * @return value of instance variable {@link #headerType}
   */
  public HeaderType getHeaderType() {
    return this.headerType;
  }

  /**
   * This is the setter method for the instance variable {@link #headerType}.
   *
   * @param headerType
   *                the headerType to set
   */
  public void setHeaderType(HeaderType headerType) {
    this.headerType = headerType;
  }
}
