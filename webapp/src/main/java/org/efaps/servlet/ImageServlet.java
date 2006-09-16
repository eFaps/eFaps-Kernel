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

package org.efaps.servlet;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.AdminObject;
import org.efaps.db.Cache;
import org.efaps.db.CacheInterface;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;

/**
 * The servlet checks out user interface images depending on the
 * administrational name (not file name).<br/>
 * E.g.:<br/>
 * <code>/efaps/servlet/image/Admin_UI_Image</code>.
 *
 * @author tmo
 * @version $Id$
 */
public class ImageServlet extends HttpServlet  {

  /**
   * Logging instance used in this class.
   */
  private final static Log LOG = LogFactory.getLog(ImageServlet.class);

  /**
   * The method checks the image from the user interface image object out and
   * returns them in a output stream to the web client. The name of the user
   * interface image object must given as name at the end of the path.
   *
   * @param _req request variable
   * @param _res response variable
   * @see #PARAM_ATTRNAME
   * @see #PARAM_OID
   */
  protected void doGet(HttpServletRequest _req, HttpServletResponse _res) throws ServletException, IOException  {
    String imgName = _req.getRequestURI();

    imgName = imgName.substring(imgName.lastIndexOf('/') + 1);

    try  {
      Context context = Context.getThreadContext();

      if (!cache.hasEntries())  {
        loadCache(context);
      }

      ImageMapper imageMapper = cache.get(imgName);

      if (imageMapper != null)  {
        Checkout checkout = new Checkout(imageMapper.oid);

        _res.setContentType(getServletContext().getMimeType(imageMapper.file));
        _res.addHeader("Content-Disposition", "inline; filename=\"" + imageMapper.file + "\"");

        checkout.executeWithoutAccessCheck(_res.getOutputStream());

        checkout.close();
      }
    } catch (IOException e)  {
      LOG.error("while reading history data", e);
      throw e;
    } catch (ServletException e)  {
      LOG.error("while reading history data", e);
      throw e;
    } catch (Exception e)  {
      LOG.error("while reading history data", e);
      throw new ServletException(e);
    }
  }

  /**
   * A query is made for all user interface images and caches the name, file
   * name and object id. The cache is needed to reference from an image name
   * to the object id and the original file name.
   *
   * @param _context  context for this request
   * @throws Exception if searchquery fails
   * @see #cache
   * @see #ImageMapper
   */
  private static void loadCache(final Context _context) throws Exception  {
    synchronized(cache)  {
      SearchQuery query = new SearchQuery();
      query.setQueryTypes(_context, AdminObject.EFapsClassName.IMAGE.name);
      query.addSelect(_context, "Name");
      query.addSelect(_context, "FileName");
      query.addSelect(_context, "OID");
      query.executeWithoutAccessCheck();

      while (query.next())  {
        String name = (String) query.get(_context, "Name");
        String file = (String) query.get(_context, "FileName");
        String oid  = (String) query.get(_context, "OID");
        cache.add(new ImageMapper(name, file, oid));
      }
      query.close();
    }
  }

  /**
   * The cache stores all instance of class {@link #ImageMappe}.
   */
  private static Cache < ImageMapper > cache = new Cache < ImageMapper > ();

  /**
   * The class is used to map from the administrational image name to the image
   * file name and image object id.
   */
  private static class ImageMapper implements CacheInterface  {

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
     * @param _name   administrational name of the image
     * @param _file   file name of the image
     * @param _oid    object id of the image
     */
    private ImageMapper(final String _name, final String _file, final String _oid)  {
      this.name = _name;
      this.oid = _oid;
      this.file = _file;
    }

    /**
     * This is the getter method for instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     * @see #name
     */
    public String getName()  {
      return this.name;
    }

    /**
     * The method is not needed in this cache implementation, but to implemente
     * interface {@link CacheInterface} the method is required.
     *
     * @return always <code>null</code>
     */
    public UUID getUUID()  {
      return null;
    }

    /**
     * The method is not needed in this cache implementation, but to implemente
     * interface {@link CacheInterface} the method is required.
     *
     * @return always <code>0</code>
     */
    public long getId()  {
      return 0;
    }
  }

}