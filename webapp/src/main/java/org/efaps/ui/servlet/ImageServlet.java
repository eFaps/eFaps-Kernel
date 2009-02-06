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

package org.efaps.ui.servlet;

import static org.efaps.admin.EFapsClassNames.IMAGE;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkout;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.AutomaticCache;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;


/**
 * The servlet checks out user interface images depending on the
 * administrational name (not file name).<br/> E.g.:<br/>
 * <code>/efaps/servlet/image/Admin_UI_Image</code>.
 *
 * @author tmo
 * @version $Id:ImageServlet.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class ImageServlet extends HttpServlet {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = -2469349574113406199L;

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ImageServlet.class);

  /**
   * The cache stores all instance of class {@link #ImageMappe}.
   */
  private static ImageCache CACHE = new ImageCache();


  /**
   * The method checks the image from the user interface image object out and
   * returns them in a output stream to the web client. The name of the user
   * interface image object must given as name at the end of the path.
   *
   * @param _req  request variable
   * @param _res  response variable
   * @see #PARAM_ATTRNAME
   * @see #PARAM_OID
   * @throws ServletException on error
   * @throws IOException on error
   */
  @Override
  protected void doGet(final HttpServletRequest _req,
                       final HttpServletResponse _res)
      throws ServletException, IOException {
    String imgName = _req.getRequestURI();

    imgName = imgName.substring(imgName.lastIndexOf('/') + 1);

    try {
      if (!CACHE.hasEntries()) {
        loadCache();
      }

      final ImageMapper imageMapper = CACHE.get(imgName);

      if (imageMapper != null) {
        final Checkout checkout = new Checkout(imageMapper.oid);

        _res.setContentType(getServletContext().getMimeType(imageMapper.file));
        _res.setContentLength((int) imageMapper.filelength);
        _res.setDateHeader("Last-Modified", imageMapper.time);

        _res.setDateHeader("Expires", System.currentTimeMillis()
            + (3600 * 1000));
        _res.setHeader("Cache-Control", "max-age=3600");

        checkout.execute(_res.getOutputStream());

        checkout.close();
      }
    } catch (final IOException e) {
      LOG.error("while reading history data", e);
      throw e;
    } catch (final CacheReloadException e) {
      LOG.error("while reading history data", e);
      throw new ServletException(e);
    } catch (final Exception e) {
      LOG.error("while reading history data", e);
      throw new ServletException(e);
    }
  }

  /**
   * A query is made for all user interface images and caches the name, file
   * name and object id. The cache is needed to reference from an image name to
   * the object id and the original file name.
   *
   * @throws CacheReloadException if search query fails
   * @see #CACHE
   * @see #ImageMapper
   */
  private static void loadCache() throws CacheReloadException {

  }

  /**
   * The class is used to map from the administrational image name to the image
   * file name and image object id.
   */
  private static final class ImageMapper implements CacheObjectInterface {

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

    /**
     * Lenght of the image in long.
     */
    private final long filelength;

    /**
     * Time the image was last retrieved.
     */
    private final Long time;

    /**
     * @param _name       administrational name of the image
     * @param _file       file name of the image
     * @param _oid        object id of the image
     * @param _filelength lenght of the file
     * @param _time       time
     */
    private ImageMapper(final String _name,
                        final String _file,
                        final String _oid,
                        final Long _filelength,
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
     * The method is not needed in this cache implementation, but to implement
     * interface {@link CacheInterface} the method is required.
     *
     * @return always <code>null</code>
     */
    public UUID getUUID() {
      return null;
    }

    /**
     * The method is not needed in this cache implementation, but to implement
     * interface {@link CacheInterface} the method is required.
     *
     * @return always <code>0</code>
     */
    public long getId() {
      return 0;
    }
  }

  private static class ImageCache extends AutomaticCache<ImageMapper> {


    /* (non-Javadoc)
     * @see org.efaps.util.cache.Cache#readCache(java.util.Map, java.util.Map, java.util.Map)
     */
    @Override
    protected void readCache(final Map<Long, ImageMapper> cache4Id,
        final Map<String, ImageMapper> cache4Name, final Map<UUID, ImageMapper> cache4UUID)
        throws CacheReloadException {
      try {
        synchronized (CACHE) {
          final SearchQuery query = new SearchQuery();
          query.setQueryTypes(Type.get(IMAGE.getUuid()).getName());
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
            final DateTime time = (DateTime) query.get("Modified");
            final ImageMapper mapper = new ImageMapper(name, file, oid, filelength,
                                      time.getMillis());

            cache4Name.put(mapper.getName(), mapper);
          }
          query.close();
        }
      } catch (final EFapsException e) {
        throw new CacheReloadException("could not initialise "
            + "image servlet cache");
      }

    }

  }
}
