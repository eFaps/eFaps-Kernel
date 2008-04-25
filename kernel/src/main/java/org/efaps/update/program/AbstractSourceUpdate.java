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

package org.efaps.update.program;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.db.Checkin;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractSourceUpdate extends AbstractUpdate {

  /**
   * Constructor setting the Name iof the Type to be imported/updated
   *
   * @param _modelTypeName
   */
  protected AbstractSourceUpdate(final String _modelTypeName) {
    this(_modelTypeName, null);
  }

  protected AbstractSourceUpdate(final String modelTypeName,
                                 final Set<Link> linkTypes) {
    super(modelTypeName, linkTypes);
  }

  /**
   * get the Version of this Update. Override id to use other than 1. To set the
   * Version to the last Version return {@link #getMaxVersion()}
   *
   * @return
   */
  protected Long getVersion() {
    return new Long(1);
  }

  /**
   * TODO description
   */
  public static abstract class SourceDefinition extends AbstractDefinition {

    /**
     * instance variable holding the URL to the file to be imported
     */
    private URL fileUrl;

    /**
     * Constructor to defined the URL in {@link #fileUrl} to the file and
     * calculating the name of the source object (file url minus root url).
     * The path separators are replaces by points.
     *
     * @param _rootUrl  URL to the root
     * @param _fileUrl  URL to the file (incl. root).
     */
    protected SourceDefinition(final URL _rootUrl, final URL _fileUrl)
    {
      // searched by attribute Name
      super("Name");
      // calculating name of file in eFaps
      this.fileUrl = _fileUrl;
      final String rootStr = _rootUrl.toString();
      final String urlStr = this.fileUrl.toString();
      final String name = urlStr.substring(urlStr.lastIndexOf(rootStr)
                                           + rootStr.length())
                                .replace(File.separator, ".");
      setName(name);
    }

    /**
     * Updates / creates the instance in the database. If a file
     * name is given, this file is checked in
     *
     * @param _instance     instance to update (or null if instance is to create)
     * @param _allLinkTypes
     */
    @Override
    public void updateInDB(final Set<Link> _allLinkTypes)
        throws EFapsException
    {
      super.updateInDB(_allLinkTypes);

      if (getValue("Name") != null) {
        final Checkin checkin = new Checkin(this.instance);
        try {
          final InputStream in = this.fileUrl.openStream();
          checkin.executeWithoutAccessCheck(getValue("Name"), in, in.available());
          in.close();
        } catch (IOException e) {
          throw new EFapsException(getClass(), "updateInDB.IOException", e, getValue("Name"));
        }
      }
    }

    ///////////////////////////////////////////////////////////////////////////
    // instance getter / setter methods

    /**
     * This is the getter method for the instance variable {@link #fileUrl}.
     *
     * @return value of instance variable {@link #fileUrl}
     */
    public URL getUrl()
    {
      return this.fileUrl;
    }

    /**
     * This is the setter method for the instance variable {@link #fileUrl}.
     *
     * @param url
     *                the url to set
     */
    public void setUrl(URL url)
    {
      this.fileUrl = url;
    }

    @Override
    public String toString()
    {
      return new ToStringBuilder(this)
              .appendSuper(super.toString())
              .append("url", this.fileUrl)
              .toString();
    }
  }

}
