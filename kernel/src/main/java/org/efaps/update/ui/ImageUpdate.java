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

package org.efaps.update.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.db.Checkin;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class ImageUpdate extends AbstractUpdate
{
  /** Link from menu to type as type tree menu */
  private final static Link LINK2TYPE
             = new Link("Admin_UI_LinkIsTypeIconFor",
                        "From",
                        "Admin_DataModel_Type", "To");

  private final static Set <Link> ALLLINKS = new HashSet < Link > ();
  static  {
    ALLLINKS.add(LINK2TYPE);
  }

  /** Name of the root path used to initialize the path for the image. */
  private final String root;

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   * @param _url        URL of the file
   */
  public ImageUpdate(final URL _url)
  {
    super(_url, "Admin_UI_Image", ALLLINKS);
    String urlStr = _url.toString();
    final int i = urlStr.lastIndexOf("/");
    this.root = urlStr.substring(0, i + 1);
  }

  /**
   * Creates new instance of class {@link ImageDefinition}.
   *
   * @return new definition instance
   * @see ImageDefinition
   */
  @Override
  protected AbstractDefinition newDefinition()
  {
    return new ImageDefinition();
  }

  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  private class ImageDefinition extends AbstractDefinition
  {
    /** Name of the Image file (including the path) to import. */
    private String file = null;

    ///////////////////////////////////////////////////////////////////////////
    // instance methods

    @Override
    protected void readXML(final List<String> _tags,
                           final Map<String,String> _attributes,
                           final String _text)
    {
      final String value = _tags.get(0);
      if ("file".equals(value))  {
        this.file = _text;
      } else if ("type".equals(value))  {
        // Assigns a type the image for which this image instance is the type
        // icon
        addLink(LINK2TYPE, new LinkInstance(_text));
      } else  {
        super.readXML(_tags, _attributes, _text);
      }
    }

    /**
     * Updates / creates the instance in the database. If a file name is
     * given, this file is checked in the created image instance.
     *
     * @param _instance     instance to update (or null if instance is to
     *                      create)
     * @param _allLinkTypes
     * @param _insert       insert instance (if new instance is to create)
     */
    @Override
    protected void updateInDB(final Set<Link> _allLinkTypes)
        throws EFapsException
    {

      super.updateInDB(_allLinkTypes);

      if (this.file != null)  {
        try  {
          final InputStream in = new URL(ImageUpdate.this.root + this.file).openStream();
          final Checkin checkin = new Checkin(this.instance);
          checkin.executeWithoutAccessCheck(this.file,
                                            in,
                                            in.available());
          in.close();
        } catch (IOException e) {
          throw new EFapsException(getClass(),
                                   "updateInDB.IOException",
                                   e,
                                   ImageUpdate.this.root + this.file);
        }
      }
    }

    ///////////////////////////////////////////////////////////////////////////
    // instance getter / setter methods

    /**
     * Returns a string representation with values of all instance variables
     * of a field.
     *
     * @return string representation of this definition of a column
     */
    @Override
    public String toString()  {
      return new ToStringBuilder(this)
              .appendSuper(super.toString())
              .append("file", this.file).toString();
    }
  }
}
