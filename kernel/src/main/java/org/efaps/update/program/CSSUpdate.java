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

package org.efaps.update.program;

import java.net.URL;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class CSSUpdate extends AbstractSourceUpdate {

  public static String TYPENAME = "Admin_Program_CSS";

  protected CSSUpdate() {
    super(TYPENAME);
  }

  public static CSSUpdate readFile(final URL _url) {
    final CSSUpdate ret = new CSSUpdate();
    ret.setURL(_url);
    final CSSDefinition definition = new CSSDefinition(_url);
    ret.addDefinition(definition);

    return ret;
  }

  public static class CSSDefinition extends SourceDefinition {

    public CSSDefinition(final URL _url) {
      super(_url);
    }

  }
}
