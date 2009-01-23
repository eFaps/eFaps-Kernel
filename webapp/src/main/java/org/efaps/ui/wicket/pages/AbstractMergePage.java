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

package org.efaps.ui.wicket.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.Component;
import org.apache.wicket.IPageMap;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;

import org.efaps.admin.program.bundle.BundleMaker;
import org.efaps.admin.program.bundle.TempFileBundle;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

/**
 * This abstract Page extends WebPage to deliver the functionality of merging
 * specific behaviors to one behavior to reduce the amount of reqeusts per page.<br>
 * Before the page is rendered
 * {@link #org.efaps.ui.wicket.resources.StaticHeaderContributor} will be
 * bundled using {@link #org.efaps.admin.program.bundle.BundleMaker}.
 *
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractMergePage extends WebPage {

  private static final long serialVersionUID = 1L;

  /**
   * this instance variable is used to define if the merging is done or not
   */
  private boolean mergeStatics = true;

  /**
   * Constructor that passes to the SuperConstructor
   */
  public AbstractMergePage() {
    super();
  }

  /**
   * Constructor that passes to the SuperConstructor
   */
  public AbstractMergePage(final IModel<?> _model) {
    super(_model);
  }

  /**
   * Constructor that passes to the SuperConstructor
   */
  public AbstractMergePage(final IPageMap _pagemap, final IModel<?> _model) {
    super(_pagemap, _model);
  }

  /**
   * Constructor that passes to the SuperConstructor
   */
  public AbstractMergePage(final IPageMap _pagemap) {
    super(_pagemap);
  }

  /**
   * Constructor that passes to the SuperConstructor
   */
  public AbstractMergePage(final IPageMap _pagemap,
                           final PageParameters _parameters) {
    super(_pagemap, _parameters);
  }

  /**
   * Constructor that passes to the SuperConstructor
   */
  public AbstractMergePage(final PageParameters _parameters) {
    super(_parameters);
  }


  /**
   * in this method the actual merging is done depending on the value of
   * {@link #mergeStatics()}
   *
   * @see #mergeStatics()
   * @see org.apache.wicket.Page#onBeforeRender()
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void onBeforeRender() {
    if (mergeStatics()) {

      final Map<StaticHeaderContributor.HeaderType, List<StaticHeaderContributor>> resources =
          new HashMap<StaticHeaderContributor.HeaderType, List<StaticHeaderContributor>>();

      // get all StaticHeaderContributor from all childs
      addStaticBehaviors(resources, this.getBehaviors());
      addChildStatics(resources, this);

      for (final Entry<StaticHeaderContributor.HeaderType, List<StaticHeaderContributor>> entry : resources
          .entrySet()) {
        if (entry.getValue().size() > 1) {
          final List namelist = getReferenceNameList(entry.getValue());
          // get a new Bundle
          String name = "";
          try {
            name = BundleMaker.getBundleKey(namelist, TempFileBundle.class);
          } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
          }
          // add the new Bundle to the Page
          final TempFileBundle bundle =
              (TempFileBundle) BundleMaker.getBundle(name);
          if (entry.getKey().equals(StaticHeaderContributor.HeaderType.CSS)) {
            this.add(StaticHeaderContributor.forCss(new EFapsContentReference(
                name), true));
            bundle.setContentType("text/css");
          } else if (entry.getKey().equals(
              StaticHeaderContributor.HeaderType.JS)) {
            this.add(StaticHeaderContributor.forJavaScript(
                new EFapsContentReference(name), true));
            bundle.setContentType("text/javascript");
          }
        }
      }
    }
    super.onBeforeRender();
  }

  /**
   * this method removes the given Behaviors from the Components and ads the
   * Names of the References to a List
   *
   * @param _behaviors
   *                List of Behaviors that will be removed and the Names added
   *                to a List
   * @return a List with the Names of the Reference
   */
  protected List<String> getReferenceNameList(
                                              final List<StaticHeaderContributor> _behaviors) {
    final List<String> ret = new ArrayList<String>();
    for (final StaticHeaderContributor behavior : _behaviors) {
      ret.add(behavior.getReference().getName());
      behavior.getComponent().remove(behavior);
    }
    return ret;
  }

  /**
   * this method checks for behaviors in the given List wich are instances of
   * StaticHeaderContributor and puts them in the map
   *
   * @param _resources
   *                the map the List of SaticHeaderContributors will be put
   * @param _behaviors
   *                a List a Behaviors that will be searched for instances of
   *                StaticHeaderContributor
   * @see #addChildStatics(Map, MarkupContainer)
   */
  protected void addStaticBehaviors(
                                    final Map<StaticHeaderContributor.HeaderType, List<StaticHeaderContributor>> _resources,
                                    final List<IBehavior> _behaviors) {

    for (final IBehavior oneBehavior : _behaviors) {
      if (oneBehavior instanceof StaticHeaderContributor) {
        final StaticHeaderContributor behavior =
            (StaticHeaderContributor) oneBehavior;
        if (!behavior.isMerged()) {
          List<StaticHeaderContributor> behaviors =
              _resources.get(behavior.getHeaderType());
          if (behaviors == null) {
            behaviors = new ArrayList<StaticHeaderContributor>();
            _resources.put(behavior.getHeaderType(), behaviors);
          }
          behaviors.add(behavior);
        }
      }
    }
  }

  /**
   * recursive method to step through all ChildComponents and calls
   * {@link #addStaticBehaviors(Map, List)} for the Behaviors of the Component
   *
   * @param resources
   * @param _markupcontainer
   * @see #addStaticBehaviors(Map, List)
   */
  protected void addChildStatics(
                                 final Map<StaticHeaderContributor.HeaderType, List<StaticHeaderContributor>> resources,
                                 final MarkupContainer _markupcontainer) {
    final Iterator<?> it = _markupcontainer.iterator();
    while (it.hasNext()) {
      final Component component = (Component) it.next();
      if (component instanceof MarkupContainer) {
        addChildStatics(resources, (MarkupContainer) component);
      }
      addStaticBehaviors(resources, component.getBehaviors());
    }
  }

  /**
   * should the merging be done?
   *
   * @see #onBeforeRender()
   * @return
   */
  protected boolean mergeStatics() {
    return this.mergeStatics;
  }

  /**
   * This is the getter method for the instance variable {@link #mergeStatics}.
   *
   * @return value of instance variable {@link #mergeStatics}
   */
  public boolean isMergeStatics() {
    return this.mergeStatics;
  }

  /**
   * This is the setter method for the instance variable {@link #mergeStatics}.
   *
   * @param mergeStatics
   *                the mergeStatics to set
   */
  public void setMergeStatics(final boolean mergeStatics) {
    this.mergeStatics = mergeStatics;
  }

}
