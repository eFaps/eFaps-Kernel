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

package org.efaps.ui.wicket.components.menu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.io.Streams;

import org.efaps.ui.wicket.util.FileFormat.MimeTypes;

/**
 * @author jmox
 * @version $Id$
 */
public class FileRequestTarget implements IRequestTarget {

  private final File file;

  private final MimeTypes mime;

  public FileRequestTarget(final File _file) {
    this.file = _file;
    this.mime = evaluateMimeType();
  }

  private MimeTypes evaluateMimeType() {
    String name = this.file.getName();
    String end = name.substring(name.lastIndexOf(".") + 1);
    return MimeTypes.getMime(end);
  }

  public void detach(RequestCycle arg0) {
    // not needed here
  }

  public void respond(final RequestCycle _requestCycle) {
    try {
      InputStream input = new FileInputStream(this.file);
      WebResponse response = (WebResponse) _requestCycle.getResponse();
      response.setAttachmentHeader("print." + this.mime.end);
      response.setContentType(this.mime.application);

      Streams.copy(input, response.getOutputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

}
