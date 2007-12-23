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

package org.efaps.ui.wicket.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;

import org.efaps.db.Checkout;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractEFapsResource extends WebResource {

  private static final long serialVersionUID = 1L;

  private final String name;

  private final String type;

  protected EFapsResourceStream stream;

  public AbstractEFapsResource(final String _name, final String _type) {
    super();
    this.name = _name;
    this.type = _type;
    this.stream = setResourceStream();
    this.stream.setInputStream(getFromDB());
  }

  protected abstract EFapsResourceStream setResourceStream();





  protected InputStream getFromDB() {
    InputStream ret = null;
    try {
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes(this.type);
      query.addSelect("OID");
      query.addWhereExprMatchValue("Name", this.name);
      query.execute();
      if (query.next()) {
        final Checkout checkout = new Checkout(query.get("OID").toString());
        final InputStream tmp = checkout.execute();
        ret = new ByteArrayInputStream(IOUtils.toByteArray(tmp));
        tmp.close();
        checkout.close();
      }

    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return ret;
  }

  @Override
  public IResourceStream getResourceStream() {
    return this.stream;
  }

  /**
   * TODO description
   *
   * @author jmox
   * @version $Id$
   */
  public abstract class EFapsResourceStream implements IResourceStream {

    private static final long serialVersionUID = 1L;

    private InputStream inputstream;

    private final Time time;

    public EFapsResourceStream() {
      this.time = Time.now();
    }

    public void setInputStream(final InputStream _inputstream) {
      this.inputstream = _inputstream;
    }

    public void close() throws IOException {
    }

    public InputStream getInputStream() throws ResourceStreamNotFoundException {
      return this.inputstream;
    }

    public Locale getLocale() {
      // TODO Auto-generated method stub
      return null;
    }

    public long length() {

      return -1;
    }

    public void setLocale(Locale locale) {
      // TODO Auto-generated method stub

    }

    public Time lastModifiedTime() {
      return this.time;
    }

  }
}
