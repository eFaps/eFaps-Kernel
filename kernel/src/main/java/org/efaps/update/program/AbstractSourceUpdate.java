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

package org.efaps.update.program;

import java.io.InputStream;
import java.net.URL;
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
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractSourceUpdate extends AbstractUpdate {

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG =
      LoggerFactory.getLogger(AbstractSourceUpdate.class);

  protected AbstractSourceUpdate(final String _modelTypeName) {
    super(_modelTypeName);
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

  /* (non-Javadoc)
   * @see org.efaps.update.AbstractUpdate#updateInDB(org.apache.commons.jexl.JexlContext)
   */
  @Override
  public void updateInDB(final JexlContext _jexlContext) throws EFapsException,
                                                        Exception {
    try {

      for (final AbstractDefinition def : getDefinitions()) {

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
          def.updateInDB(Type.get(super.getDataModelTypeName()), null, null,
              false);
        }
      }
    } catch (final Exception e) {
      LOG.error("updateInDB", e);
      throw e;
    }
  }

  public static abstract class SourceDefinition extends AbstractDefinition {

    private URL url;

    private String fileName;

    public SourceDefinition(final URL _url) {
      setURL(_url);
    }

    public void setURL(URL _url) {
      this.url = _url;
      final String urlStr = _url.toString();
      this.fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1);
    }

    @Override
    public void updateInDB(final Type _dataModelType, final String _uuid,
                           final Set<Link> _allLinkTypes,
                           final boolean _abstractType) throws EFapsException,
                                                       Exception {
      Instance instance = null;
      Insert insert = null;
      addValue("Name", this.fileName);
      // search for the instance
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes(_dataModelType.getName());
      query.addWhereExprEqValue("Name", this.fileName);
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (query.next()) {
        instance = new Instance((String) query.get("OID"));
      }
      query.close();

      // if no instance exists, a new insert must be done
      if (instance == null) {
        insert = new Insert(_dataModelType);
        if (insert.getInstance().getType().getAttribute("Abstract") != null) {
          insert.add("Abstract", ((Boolean) _abstractType).toString());
        }
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

      if (this.fileName != null) {
        final InputStream in = this.url.openStream();
        final Checkin checkin = new Checkin(instance);
        checkin.executeWithoutAccessCheck(this.fileName, in, in.available());
        in.close();
      }
      return instance;
    }
  }

}
