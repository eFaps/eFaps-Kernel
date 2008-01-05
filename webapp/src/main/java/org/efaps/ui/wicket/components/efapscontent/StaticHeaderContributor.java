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

package org.efaps.ui.wicket.components.efapscontent;

import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;

public class StaticHeaderContributor extends HeaderContributor {

  private static final long serialVersionUID = 1L;

  public StaticHeaderContributor(IHeaderContributor headerContributor) {
    super(headerContributor);
  }

  public static final StaticHeaderContributor forCss(
                                                     final EFapsContentReference _reference) {
    return new StaticHeaderContributor(new IHeaderContributor() {

      private static final long serialVersionUID = 1L;

      public void renderHead(IHeaderResponse response) {
        response.renderCSSReference(_reference.getCSSUrl());
      }
    });
  }
}
