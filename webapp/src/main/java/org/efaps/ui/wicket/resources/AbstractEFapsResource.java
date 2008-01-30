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

package org.efaps.ui.wicket.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Application;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Checkout;
import org.efaps.db.SearchQuery;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractEFapsResource extends WebResource {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(EFapsSession.class);

  private final String name;

  private final String type;

  protected EFapsResourceStream stream;

  public AbstractEFapsResource(final String _name, final String _type) {
    super();
    this.name = _name;
    this.type = _type;
    this.stream = setNewResourceStream();
  }

  protected abstract EFapsResourceStream setNewResourceStream();

  @Override
  public IResourceStream getResourceStream() {
    if (this.stream == null) {
      this.stream = setNewResourceStream();
    }
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

    private transient InputStream inputStream;

    private Time time;

    private byte[] data;

    public EFapsResourceStream() {
      this.time = Time.now();
    }

    protected void setData() {
      try {
        final SearchQuery query = new SearchQuery();
        query.setQueryTypes(AbstractEFapsResource.this.type);
        query.addSelect("OID");
        query.addWhereExprMatchValue("Name", AbstractEFapsResource.this.name);
        query.execute();
        if (query.next()) {
          final Checkout checkout = new Checkout(query.get("OID").toString());
          final InputStream tmp = checkout.execute();
          this.data = IOUtils.toByteArray(tmp);
          tmp.close();
          checkout.close();
          if (LOG.isInfoEnabled()) {
            LOG.info("loaded: " + AbstractEFapsResource.this.name);
          }
        }
      } catch (final EFapsException e) {
        throw new RestartResponseException(new ErrorPage(e));
      } catch (final IOException e) {
        throw new RestartResponseException(new ErrorPage(e));
      }
    }

    public void close() throws IOException {
      if (this.inputStream != null) {
        this.inputStream.close();
        this.inputStream = null;
      }
    }

    public InputStream getInputStream() throws ResourceStreamNotFoundException {
      if (this.inputStream == null) {
        checkData(false);
        this.inputStream = new ByteArrayInputStream(this.data);
      }
      return this.inputStream;
    }

    public Locale getLocale() {
      return null;
    }

    public long length() {
      checkData(true);
      return this.data != null ? this.data.length : 0;
    }

    public void setLocale(Locale locale) {
      // not used here
    }

    public Time lastModifiedTime() {
      return this.time;
    }

    private void checkData(final boolean _checkDuration) {
      if ((Application.DEVELOPMENT.equals(Application.get()
          .getConfigurationType()) || ((Time.now())
          .subtract(lastModifiedTime()).getMilliseconds() / 1000 > getCacheDuration()))
          && _checkDuration) {
        this.time = Time.now();
        setData();
      }
      if (this.data == null) {
        setData();
      }
    }
  }
}
