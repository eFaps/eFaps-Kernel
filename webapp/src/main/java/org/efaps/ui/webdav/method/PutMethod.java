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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.webdav.method;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.efaps.ui.webdav.WebDAVRequest;
import org.efaps.ui.webdav.resource.CollectionResource;
import org.efaps.ui.webdav.resource.SourceResource;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class PutMethod extends AbstractMethod  {

  /**
   *
   */
  @Override
  public void run(final WebDAVRequest _request,
                  final HttpServletResponse _response) throws IOException, ServletException  {
    Status status = null;

    final String[] uri = _request.getPathInfo().split("/");
    final String name = uri[uri.length - 1];

    final CollectionResource parentCollection
                          = getCollection4ParentPath(_request.getPathInfo());

    if (parentCollection == null)   {
// nicht immer richtig.... was ist wenn im root angelegt werden soll....
// (im moment sonderfall, da im root keine datei abgelegt werden darf)
      status = Status.CONFLICT;
    } else if (parentCollection.getCollection(name) != null)  {
      status = Status.CONFLICT;
    } else  {
      SourceResource source = parentCollection.getSource(name);

      if (source == null)  {
        if (parentCollection.createSource(name))  {
          source = parentCollection.getSource(name);
          status = Status.CREATED;
        }
      } else  {
        status = Status.NO_CONTENT;
      }

      if (source == null)  {
// was muss da gesetzt werden????? => RFC 2068
status = Status.CONFLICT;
      } else  {
        final boolean bck = source.checkin(_request.getRequest().getInputStream());
        if (!bck)  {
// was muss da gesetzt werden????? => RFC 2068
status = Status.CONFLICT;
        }
      }
    }
    _response.setStatus(status.code);
  }

     /**
     * Process a POST request for the specified resource.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
/*    protected void doPut(final String _fileName,
                         final Instance _instance,
                         final HttpServletRequest req,
                         final HttpServletResponse resp)
        throws ServletException, IOException,EFapsException {

       if (readOnly) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
    }
*/

   /**
     * Handle a partial PUT.  New content specified in request is appended to
     * existing content in oldRevisionContent (if present). This code does
     * not support simultaneous partial updates to the same resource.
     */
/*    protected java.io.File executePartialPut(HttpServletRequest req, Range range,
                                     String path)
        throws IOException {
final int BUFFER_SIZE = 4096;

        // Append data specified in ranges to existing content for this
        // resource - create a temp. file on the local filesystem to
        // perform this operation
        java.io.File tempDir = (java.io.File) getServletContext().getAttribute
            ("javax.servlet.context.tempdir");
        // Convert all '/' characters to '.' in resourcePath
        String convertedResourcePath = path.replace('/', '.');
        java.io.File contentFile = new java.io.File(tempDir, convertedResourcePath);
        if (contentFile.createNewFile()) {
            // Clean up contentFile when Tomcat is terminated
            contentFile.deleteOnExit();
        }

        java.io.RandomAccessFile randAccessContentFile =
            new java.io.RandomAccessFile(contentFile, "rw");
*/
/*
        Resource oldResource = null;
        try {
            Object obj = resources.lookup(path);
            if (obj instanceof Resource)
                oldResource = (Resource) obj;
        } catch (javax.naming.NamingException e) {
        }

        // Copy data in oldRevisionContent to contentFile
        if (oldResource != null) {
            java.io.BufferedInputStream bufOldRevStream =
                new java.io.BufferedInputStream(oldResource.streamContent(),
                                        BUFFER_SIZE);

            int numBytesRead;
            byte[] copyBuffer = new byte[BUFFER_SIZE];
            while ((numBytesRead = bufOldRevStream.read(copyBuffer)) != -1) {
                randAccessContentFile.write(copyBuffer, 0, numBytesRead);
            }

            bufOldRevStream.close();
        }
*/
/*        randAccessContentFile.setLength(range.length);

        // Append data in request input stream to contentFile
        randAccessContentFile.seek(range.start);
        int numBytesRead;
        byte[] transferBuffer = new byte[BUFFER_SIZE];
        java.io.BufferedInputStream requestBufInStream =
            new java.io.BufferedInputStream(req.getInputStream(), BUFFER_SIZE);
        while ((numBytesRead = requestBufInStream.read(transferBuffer)) != -1) {
            randAccessContentFile.write(transferBuffer, 0, numBytesRead);
        }
        randAccessContentFile.close();
        requestBufInStream.close();

        return contentFile;

    }
*/

    /**
     * Parse the content-range header.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Range
     */
/*    protected Range parseContentRange(HttpServletRequest request,
                                      HttpServletResponse response)
        throws IOException {

        // Retrieving the content-range header (if any is specified
        String rangeHeader = request.getHeader("Content-Range");

        if (rangeHeader == null)
            return null;

        // bytes is the only range unit supported
        if (!rangeHeader.startsWith("bytes")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        rangeHeader = rangeHeader.substring(6).trim();

        int dashPos = rangeHeader.indexOf('-');
        int slashPos = rangeHeader.indexOf('/');

        if (dashPos == -1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if (slashPos == -1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        Range range = new Range();

        try {
            range.start = Long.parseLong(rangeHeader.substring(0, dashPos));
            range.end =
                Long.parseLong(rangeHeader.substring(dashPos + 1, slashPos));
            range.length = Long.parseLong
                (rangeHeader.substring(slashPos + 1, rangeHeader.length()));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if (!range.validate()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        return range;

    }

    protected class Range {

        public long start;
        public long end;
        public long length;
*/
        /**
         * Validate range.
         */
/*        public boolean validate() {
            if (end >= length)
                end = length - 1;
            return ( (start >= 0) && (end >= 0) && (start <= end)
                     && (length > 0) );
        }

        public void recycle() {
            start = 0;
            end = 0;
            length = 0;
        }

    }
*/
}
