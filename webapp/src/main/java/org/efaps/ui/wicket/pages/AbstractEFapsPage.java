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
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.IPageMap;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;

import org.efaps.admin.program.pack.EFapsPackager;
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

  private boolean merged = false;

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
    if (mergeStatics() && !this.merged) {
      final List<String> resources = new ArrayList<String>();
      final List<IBehavior> beh = this.getBehaviors();

      for (final IBehavior oneBehavior : beh) {
        if (oneBehavior instanceof StaticHeaderContributor) {
          this.remove(oneBehavior);
          resources.add(((StaticHeaderContributor) oneBehavior).getReference()
              .getName());
        }
      }
      addChildStatics(resources, this);
      if (resources.size() > 1) {
        this.add(StaticHeaderContributor.forCss(new EFapsContentReference(
            EFapsPackager.getPackageKey(resources))));
      } else if (!resources.isEmpty()) {
        // if it is only one we don't need a Package
        this.add(StaticHeaderContributor.forCss(new EFapsContentReference(
            resources.get(0))));
      }
      this.merged = true;
    }
    super.onBeforeRender();
  }

  @SuppressWarnings("unchecked")
  protected void addChildStatics(final List<String> _behaviors,
                                 final MarkupContainer _markupcontainer) {
    final Iterator<?> it = _markupcontainer.iterator();
    while (it.hasNext()) {
      final Component component = (Component) it.next();
      final List<IBehavior> beh = component.getBehaviors();
      for (final IBehavior oneBehavior : beh) {
        if (oneBehavior instanceof StaticHeaderContributor) {
          component.remove(oneBehavior);
          _behaviors.add(((StaticHeaderContributor) oneBehavior).getReference()
              .getName());
        }
      }

    }
  }

  protected boolean mergeStatics() {
    return true;
  }

}
