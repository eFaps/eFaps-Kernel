/*
 * Copyright 2003-2008 The eFaps Team
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
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;

import org.efaps.admin.program.bundle.EFapsPackager;
import org.efaps.admin.program.bundle.OnePackage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class AbstractEFapsPage extends WebPage {

  private static final long serialVersionUID = 1L;

  public AbstractEFapsPage() {

  }

  public AbstractEFapsPage(final IModel _model) {
    super(_model);
  }

  public AbstractEFapsPage(final IPageMap _pagemap, final IModel _model) {
    super(_pagemap, _model);
  }

  public AbstractEFapsPage(final IPageMap _pagemap) {
    super(_pagemap);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.Page#onBeforeRender()
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void onBeforeRender() {
    if (mergeStatics()) {

      final Map<StaticHeaderContributor.Type, List<StaticHeaderContributor>> resources =
          new HashMap<StaticHeaderContributor.Type, List<StaticHeaderContributor>>();

      addStaticBehaviors(resources, this.getBehaviors());
      addChildStatics(resources, this);

      for (final Entry<StaticHeaderContributor.Type, List<StaticHeaderContributor>> entry : resources
          .entrySet()) {
        if (entry.getValue().size() > 1) {
          final List namelist = merge(entry.getValue());
          final String name = EFapsPackager.getPackageKey(namelist);
          final OnePackage onepackage = EFapsPackager.getPackage(name);

          if (entry.getKey().equals(StaticHeaderContributor.Type.CSS)) {
            this.add(StaticHeaderContributor.forCss(new EFapsContentReference(
                name), true));
            onepackage.setContentType("text/css");
          } else if (entry.getKey().equals(StaticHeaderContributor.Type.JS)) {
            this.add(StaticHeaderContributor.forJavaScript(
                new EFapsContentReference(name), true));
            onepackage.setContentType("text/javascript");
          }

        }
      }
    }
    super.onBeforeRender();
  }

  protected List<String> merge(List<StaticHeaderContributor> _behaviors) {
    final List<String> ret = new ArrayList<String>();
    for (final StaticHeaderContributor behavior : _behaviors) {
      ret.add(behavior.getReference().getName());
      behavior.getComponent().remove(behavior);
    }
    return ret;
  }

  protected void addStaticBehaviors(
                                    final Map<StaticHeaderContributor.Type, List<StaticHeaderContributor>> _resources,
                                    final List<IBehavior> _behaviors) {

    for (final IBehavior oneBehavior : _behaviors) {
      if (oneBehavior instanceof StaticHeaderContributor) {
        final StaticHeaderContributor behavior =
            (StaticHeaderContributor) oneBehavior;
        if (!behavior.isMerged()) {
          List<StaticHeaderContributor> behaviors =
              _resources.get(behavior.getType());
          if (behaviors == null) {
            behaviors = new ArrayList<StaticHeaderContributor>();
            _resources.put(behavior.getType(), behaviors);
          }
          behaviors.add(behavior);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected void addChildStatics(
                                 final Map<StaticHeaderContributor.Type, List<StaticHeaderContributor>> resources,
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

  protected boolean mergeStatics() {
    return true;
  }

}
