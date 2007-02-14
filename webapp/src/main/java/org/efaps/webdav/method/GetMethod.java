/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.webdav.method;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.apache.ecs.Doctype;
import org.apache.ecs.Document;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

import org.efaps.webdav.WebDAVRequest;
import org.efaps.webdav.resource.AbstractResource;
import org.efaps.webdav.resource.CollectionResource;
import org.efaps.webdav.resource.SourceResource;

/**
 *
 * @see RFC 2518 - 8.4 GET, HEAD for Collections
 * @see RFC 2068 - 9.3 GET
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class GetMethod extends AbstractMethod  {

  /**
   *
   */
  public void run(final WebDAVRequest _request, 
                  final HttpServletResponse _response) throws IOException, ServletException  {

    AbstractResource resource = getResource4Path(_request.getPathInfo());
    if (resource == null)  {
 // was fuer fehler muss hier gemacht werden???
      _response.setStatus(Status.NOT_FOUND.code);
    } else  {
      _response.setContentType(_request.getRequest().getSession().getServletContext().getMimeType(resource.getName()));
      _response.setHeader("Content-Disposition", "inline; filename=\""+resource.getName()+"\"");
      if (resource instanceof SourceResource)  {
        SourceResource source = (SourceResource) resource;
        _response.setHeader("content-length", "" + source.getLength());
        source.checkout(_response.getOutputStream());
      } else  {
        printSubs(_request.getRequestURI(), 
                  (CollectionResource) resource, _response.getWriter());
      }
      _response.setStatus(Status.NO_CONTENT.code);
    }
  }
  
  /**
   * Make a print of all collection and source resources for one collections.
   * This is not a required feature of WebDAV implementation but helps to parse
   * with a web browser through the WebDAV integration from eFaps.
   *
   * @param _col  collection resource for which the sub collections and source
   *              resources should be printed
   * @param _out  writer used to print the subs out
   * @see RFC 2518 - 8.4 GET, HEAD for Collections
   */
  protected void printSubs(final String _uri,
                           final CollectionResource _col,
                           final PrintWriter _out)  {

    List < AbstractResource > subs = _col.getSubs();
    Map < String, CollectionResource > sortedColSubs 
                              = new TreeMap < String, CollectionResource > ();
    Map < String, SourceResource > sortedSrcSubs 
                              = new TreeMap < String, SourceResource > ();

   // sort sub collections / sources
   for (AbstractResource rsrc : subs)  {
      if (rsrc instanceof CollectionResource)  {
        sortedColSubs.put(rsrc.getName(), (CollectionResource) rsrc);
      } else  {
        sortedSrcSubs.put(rsrc.getName(), (SourceResource) rsrc);
      }
    }
    
    try  {
      URI uri = new URI(_uri + "/");
  
  
      // print out
      Document doc = new Document()
                              .appendTitle(_col.getName())
                              .setDoctype(new Doctype.Html40Strict());
      Table table = new Table();
      doc.appendBody(table);
  
      // only go one up if a parent is defined
      if (_col.getParent() != null)  {
        table.addElement(new TR().addElement(new TD().addElement(
            new A()
                .setHref(uri.resolve("..").toString())
                .addElement("..")
        )));
      }
      
      // print out all collections
      for (CollectionResource col : sortedColSubs.values())  {
        table.addElement(new TR().addElement(new TD().addElement(
            new A()
                .setHref(uri.resolve(col.getName()).toString())
                .addElement(col.getName())
        )));
      }
  
      // print out all files
      for (SourceResource src : sortedSrcSubs.values())  {
        table.addElement(new TR().addElement(new TD().addElement(
            new A()
                .setHref(uri.resolve(src.getName()).toString())
                .addElement(src.getName())
        )));
      }
      
      _out.write(doc.toString());
      _out.flush();
    } catch (URISyntaxException e)  {
e.printStackTrace();
    }
  }
}
