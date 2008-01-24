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

import org.efaps.admin.program.pack.EFapsPackager;
import org.efaps.admin.program.pack.OnePackage;
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

      final Map<StaticHeaderContributor.Type, List<String>> resources =
          new HashMap<StaticHeaderContributor.Type, List<String>>();

      final List<IBehavior> beh = this.getBehaviors();

      for (final IBehavior oneBehavior : beh) {
        if (oneBehavior instanceof StaticHeaderContributor) {
          this.remove(oneBehavior);
          List<String> resourcelist =
              resources.get(((StaticHeaderContributor) oneBehavior).getType());

          if (resourcelist == null) {
            resourcelist = new ArrayList<String>();
            resources.put(((StaticHeaderContributor) oneBehavior).getType(),
                resourcelist);
          }

          resourcelist.add(((StaticHeaderContributor) oneBehavior)
              .getReference().getName());
        }
      }
      addChildStatics(resources, this);

      for (final Entry<StaticHeaderContributor.Type, List<String>> entry : resources
          .entrySet()) {
        if (entry.getValue().size() > 1) {

          final String name = EFapsPackager.getPackageKey(entry.getValue());
          final OnePackage onepackage = EFapsPackager.getPackage(name);

          if (entry.getKey().equals(StaticHeaderContributor.Type.CSS)) {
            this.add(StaticHeaderContributor.forCss(new EFapsContentReference(
                name)));
            onepackage.setContentType("text/css");
          } else if (entry.getKey().equals(StaticHeaderContributor.Type.JS)) {
            this.add(StaticHeaderContributor
                .forJavaScript(new EFapsContentReference(name)));
            onepackage.setContentType("text/javascript");
          }
        } else if (!entry.getValue().isEmpty()) {
          // if it is only one we don't need a Package
          if (entry.getKey().equals(StaticHeaderContributor.Type.CSS)) {
            this.add(StaticHeaderContributor.forCss(new EFapsContentReference(
                entry.getValue().get(0))));
          } else if (entry.getKey().equals(StaticHeaderContributor.Type.JS)) {
            this.add(StaticHeaderContributor
                .forJavaScript(new EFapsContentReference(entry.getValue()
                    .get(0))));

          }

        }
      }

      this.merged = true;
    }
    super.onBeforeRender();
  }

  @SuppressWarnings("unchecked")
  protected void addChildStatics(
                                 final Map<StaticHeaderContributor.Type, List<String>> resources,
                                 final MarkupContainer _markupcontainer) {
    final Iterator<?> it = _markupcontainer.iterator();
    while (it.hasNext()) {
      final Component component = (Component) it.next();
      final List<IBehavior> beh = component.getBehaviors();
      for (final IBehavior oneBehavior : beh) {
        if (oneBehavior instanceof StaticHeaderContributor) {
          component.remove(oneBehavior);
          List<String> resourcelist =
              resources.get(((StaticHeaderContributor) oneBehavior).getType());

          if (resourcelist == null) {
            resourcelist = new ArrayList<String>();
            resources.put(((StaticHeaderContributor) oneBehavior).getType(),
                resourcelist);
          }

          resourcelist.add(((StaticHeaderContributor) oneBehavior)
              .getReference().getName());
        }
      }

    }
  }

  protected boolean mergeStatics() {
    return true;
  }

}
