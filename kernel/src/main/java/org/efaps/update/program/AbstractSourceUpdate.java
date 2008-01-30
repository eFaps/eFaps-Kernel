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
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Set;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkin;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
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
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG =
      LoggerFactory.getLogger(AbstractSourceUpdate.class);

  private boolean setVersion = true;

  private final String localVersion =
      (new Date(System.currentTimeMillis())).toString();

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
   * This is the getter method for the instance variable {@link #setVersion}.
   *
   * @return value of instance variable {@link #setVersion}
   */
  public boolean isSetVersion() {
    return this.setVersion;
  }

  /**
   * This is the setter method for the instance variable {@link #setVersion}.
   *
   * @param setVersion
   *                the setVersion to set
   */
  public void setSetVersion(boolean setVersion) {
    this.setVersion = setVersion;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.efaps.update.AbstractUpdate#updateInDB(org.apache.commons.jexl.JexlContext)
   */
  @Override
  public void updateInDB(final JexlContext _jexlContext) throws EFapsException,
                                                        Exception {
    try {

      for (final AbstractDefinition def : getDefinitions()) {
        if (((SourceDefinition) def).getRootDir() == null) {
          ((SourceDefinition) def).setRootDir(getRootDir());
        }
        if (this.setVersion) {
          ((SourceDefinition) def).setVersion(getApplication(), "1", "eFaps:"
              + this.localVersion, "(version==" + getVersion() + ")");
        }
        final Expression jexlExpr =
            ExpressionFactory.createExpression("(version=="
                + getVersion()
                + ")");
        final boolean exec =
            Boolean.parseBoolean((jexlExpr.evaluate(_jexlContext).toString()));
        if (exec) {
          if ((getURL() != null) && LOG.isInfoEnabled()) {
            LOG.info("Checkin of: '" + getURL().toString() + "' ");
          }
          def.updateInDB(Type.get(super.getDataModelTypeName()), null,
              getAllLinkTypes(), false);
        }
      }
    } catch (final Exception e) {
      LOG.error("updateInDB", e);
      throw e;
    }
  }

  /**
   * TODO description
   *
   * @author jmox
   * @version $Id$
   */
  public static abstract class SourceDefinition extends AbstractDefinition {

    /**
     * instance vraiable holding the URL to the file to be imported
     */
    private URL url;

    /**
     * the name as the sourcefile will have in eFaps
     */
    private String name;

    /**
     * the String representation of the Directory containing the files to be
     * installed/updated
     */
    private String rootDir;

    public SourceDefinition(final URL _url) {
      this.url = _url;
    }

    /**
     * This is the getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    public String getName() {
      return this.name;
    }

    @Override
    public void setName(final String _filename) {
      this.name = _filename;
      addValue("Name", this.name);
    }

    /**
     * In case that the {@link #name} is not set, this method sets a default for
     * the Name, using the {@link #rootDir} to determine a Name
     */
    private void setDefaultName() {
      final String urlStr = this.url.toString();
      this.name =
          urlStr.substring(urlStr.lastIndexOf(this.rootDir)
              + this.rootDir.length()
              + 1);
      this.name = this.name.replace(File.separator, ".");
      addValue("Name", this.name);
    }

    @Override
    public void updateInDB(final Type _dataModelType, final String _uuid,
                           final Set<Link> _allLinkTypes,
                           final boolean _abstractType) throws EFapsException,
                                                       Exception {
      Instance instance = null;
      Insert insert = null;

      if (getName() == null) {
        setDefaultName();
      }

      // search for the instance
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes(_dataModelType.getName());
      query.addWhereExprEqValue("Name", this.name);
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (query.next()) {
        instance = new Instance((String) query.get("OID"));
      }
      query.close();

      // if no instance exists, a new insert must be done
      if (instance == null) {
        insert = new Insert(_dataModelType);
      }
      updateInDB(instance, _allLinkTypes, insert);
    }

    /**
     * Updates / creates the instance in the database. Uses
     * {@link AbstractAjaxUpdateBehavior.updateInDB} for the update. If a file
     * name is given, this file is checked in
     *
     * @param _instance
     *                instance to update (or null if instance is to create)
     * @param _allLinkTypes
     * @param _insert
     *                insert instance (if new instance is to create)
     */
    @Override
    public Instance updateInDB(final Instance _instance,
                               final Set<Link> _allLinkTypes,
                               final Insert _insert) throws EFapsException,
                                                    Exception {

      final Instance instance =
          super.updateInDB(_instance, _allLinkTypes, _insert);

      if (this.name != null) {
        final InputStream in = this.url.openStream();
        final Checkin checkin = new Checkin(instance);
        checkin.executeWithoutAccessCheck(this.name, in, in.available());
        in.close();
      }

      if (_allLinkTypes != null) {
        for (final Link linkType : _allLinkTypes) {
          setLinksInDB(instance, linkType, getLinks(linkType));
        }
      }
      return instance;
    }

    /**
     * This is the getter method for the instance variable {@link #url}.
     *
     * @return value of instance variable {@link #url}
     */
    public URL getUrl() {
      return this.url;
    }

    /**
     * This is the setter method for the instance variable {@link #url}.
     *
     * @param url
     *                the url to set
     */
    public void setUrl(URL url) {
      this.url = url;
    }

    /**
     * This is the getter method for the instance variable {@link #rootDir}.
     *
     * @return value of instance variable {@link #rootDir}
     */
    public String getRootDir() {
      return this.rootDir;
    }

    /**
     * This is the setter method for the instance variable {@link #rootDir}.
     *
     * @param rootDir
     *                the rootDir to set
     */
    public void setRootDir(String rootDir) {
      this.rootDir = rootDir;
    }

  }

}
