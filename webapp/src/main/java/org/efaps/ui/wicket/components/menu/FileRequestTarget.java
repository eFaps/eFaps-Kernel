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

package org.efaps.ui.wicket.components.menu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.io.Streams;

import org.efaps.ui.wicket.util.MimeTypes;

/**
 * @author jmox
 * @version $Id$
 */
public class FileRequestTarget implements IRequestTarget {

  /**
   * Store the file.
   */
  private final File file;

  /**
   * Store the mime type.
   */
  private final MimeTypes mime;

  /**
   * Constructor.
   *
   * @param _file   File for this request
   */
  public FileRequestTarget(final File _file) {
    this.file = _file;
    this.mime = evaluateMimeType();
  }

  /**
   * Method to evaluate the mime type.
   *
   * @return  MimeType
   */
  private MimeTypes evaluateMimeType() {
    final String name = this.file.getName();
    final String end = name.substring(name.lastIndexOf(".") + 1);
    return MimeTypes.getMimeTypeByEnding(end);
  }

  /**
   * Not needed here.
   * @param _arg RequestCycle
   */
  public void detach(final RequestCycle _arg) {
    // not needed here
  }

  /**
   * Respond to the request.
   * @param _requestCycle RequestCycle
   */
  public void respond(final RequestCycle _requestCycle) {
    try {
      final InputStream input = new FileInputStream(this.file);
      final WebResponse response = (WebResponse) _requestCycle.getResponse();
      response.setAttachmentHeader(this.file.getName());
      response.setContentType(this.mime.getContentType());

      Streams.copy(input, response.getOutputStream());
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
