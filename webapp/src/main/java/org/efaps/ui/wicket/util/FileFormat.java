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

package org.efaps.ui.wicket.util;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class FileFormat {

  public enum MimeTypes {
    PDF("pdf", "application/pdf");

    public final String application;

    public final String end;

    private MimeTypes(final String _end, final String _appTyp) {
      this.end = _end;
      this.application = _appTyp;
      MIMETYPEMAPPER.put(_end, this);
    }

    public static MimeTypes getMime(final String _end) {
      return MIMETYPEMAPPER.get(_end);

    }

  }

  public static Map<String, MimeTypes> MIMETYPEMAPPER =
      new HashMap<String, MimeTypes>();
}
