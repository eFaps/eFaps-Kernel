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

package org.efaps.ui.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.program.bundle.BundleInterface;
import org.efaps.admin.program.bundle.BundleMaker;
import org.efaps.db.Checkout;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.AutomaticCache;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class StaticContentServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  /**
   * Logging instance used in this class.
   */
  private final static Logger LOG =
      LoggerFactory.getLogger(StaticContentServlet.class);

  private final static StaticContentCache CACHE = new StaticContentCache();

  private int cacheDuration = 3600;

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The method checks the image from the user interface image object out and
   * returns them in a output stream to the web client. The name of the user
   * interface image object must given as name at the end of the path.
   *
   * @param _req
   *                request variable
   * @param _res
   *                response variable
   * @see #PARAM_ATTRNAME
   * @see #PARAM_OID
   */
  @Override
  protected void doGet(final HttpServletRequest _req, final HttpServletResponse _res)
                                                                         throws ServletException,
                                                                         IOException {
    String contentName = _req.getRequestURI();

    contentName = contentName.substring(contentName.lastIndexOf('/') + 1);

    try {
      if (!CACHE.hasEntries()) {
        this.cacheDuration = SystemConfiguration.get(
              UUID.fromString("50a65460-2d08-4ea8-b801-37594e93dad5"))
              .getAttributeValueAsInteger("CacheDuration");
      }

      final ContentMapper imageMapper = CACHE.get(contentName);

      if (imageMapper != null) {
        final Checkout checkout = new Checkout(imageMapper.oid);

        _res.setContentType(getServletContext().getMimeType(imageMapper.file));
        _res.setDateHeader("Last-Modified", imageMapper.time);
        _res.setDateHeader("Expires", System.currentTimeMillis()
            + (this.cacheDuration * 1000));
        _res.setHeader("Cache-Control", "max-age=" + this.cacheDuration);

        if (supportsCompression(_req)) {
          _res.setHeader("Content-Encoding", "gzip");

          final ByteArrayOutputStream bytearray = new ByteArrayOutputStream();
          final GZIPOutputStream zout = new GZIPOutputStream(bytearray);
          checkout.execute(zout);
          zout.close();
          final byte[] b = bytearray.toByteArray();
          bytearray.close();
          _res.getOutputStream().write(b);
          checkout.close();

        } else {
          _res.setContentLength((int) imageMapper.filelength);
          checkout.execute(_res.getOutputStream());
        }

      } else if (BundleMaker.containsKey(contentName)) {
        final BundleInterface bundle = BundleMaker.getBundle(contentName);

        _res.setContentType(bundle.getContentType());
        _res.setDateHeader("Last-Modified", bundle.getCreationTime());
        _res.setDateHeader("Expires", System.currentTimeMillis()
            + (this.cacheDuration * 1000));
        _res.setHeader("Cache-Control", "max-age=" + this.cacheDuration);
        _res.setHeader("Content-Encoding", "gzip");

        int bytesRead;
        final byte[] buffer = new byte[2048];

        final InputStream in =
            bundle.getInputStream(supportsCompression(_req));
        while ((bytesRead = in.read(buffer)) != -1) {
          _res.getOutputStream().write(buffer, 0, bytesRead);
        }

      }
    } catch (final IOException e) {
      LOG.error("while reading Static Content", e);
      throw e;
    } catch (final CacheReloadException e) {
      LOG.error("while reading Static Content", e);
      throw new ServletException(e);
    } catch (final Exception e) {
      LOG.error("while reading Static Content", e);
      throw new ServletException(e);
    }
  }

  private boolean supportsCompression(final HttpServletRequest _req) {
    boolean ret = false;
    final String accencoding = _req.getHeader("Accept-Encoding");
    if (accencoding != null) {
      ret = accencoding.indexOf("gzip") >= 0;
    }
    return ret;
  }



  /**
   * The class is used to map from the administrational image name to the image
   * file name and image object id.
   */
  private static class ContentMapper implements CacheObjectInterface {

    /**
     * The instance variable stores the administational name of the image.
     */
    private final String name;

    /**
     * The instance variable stores the file name of the image.
     */
    private final String file;

    /**
     * The instance variable stores the object id of the image.
     */
    private final String oid;

    private final long filelength;

    private final Long time;

    /**
     * @param _name
     *                administrational name of the image
     * @param _file
     *                file name of the image
     * @param _oid
     *                object id of the image
     */
    private ContentMapper(final String _name, final String _file,
                          final String _oid, final Long _filelength,
                          final Long _time) {
      this.name = _name;
      this.oid = _oid;
      this.file = _file;
      this.filelength = _filelength;
      this.time = _time;
    }

    /**
     * This is the getter method for instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     * @see #name
     */
    public String getName() {
      return this.name;
    }

    /**
     * The method is not needed in this cache implementation, but to implemente
     * interface {@link CacheInterface} the method is required.
     *
     * @return always <code>null</code>
     */
    public UUID getUUID() {
      return null;
    }

    /**
     * The method is not needed in this cache implementation, but to implemente
     * interface {@link CacheInterface} the method is required.
     *
     * @return always <code>0</code>
     */
    public long getId() {
      return 0;
    }
  }
  private static class StaticContentCache extends AutomaticCache<ContentMapper> {


    /* (non-Javadoc)
     * @see org.efaps.util.cache.Cache#readCache(java.util.Map, java.util.Map, java.util.Map)
     */
    @Override
    protected void readCache(final Map<Long, ContentMapper> cache4Id,
        final Map<String, ContentMapper> cache4Name,
        final Map<UUID, ContentMapper> cache4UUID) throws CacheReloadException {
      try {

          final SearchQuery query = new SearchQuery();
          query.setQueryTypes("Admin_Program_StaticCompiled");
          query.setExpandChildTypes(true);
          query.addSelect("Name");
          query.addSelect("FileName");
          query.addSelect("OID");
          query.addSelect("FileLength");
          query.addSelect("Modified");

          query.executeWithoutAccessCheck();

          while (query.next()) {
            final String name = (String) query.get("Name");
            final String file = (String) query.get("FileName");
            final String oid = (String) query.get("OID");
            final Long filelength = (Long) query.get("FileLength");
            final DateTime datetime = (DateTime) query.get("Modified");

            final ContentMapper mapper = new ContentMapper(name, file, oid, filelength,
                datetime.getMillis());

                cache4Name.put(mapper.getName(), mapper);
          }
          query.close();

      } catch (final EFapsException e) {
        throw new CacheReloadException("could not initialise "
            + "image servlet cache");
      }

    }

  }
}
