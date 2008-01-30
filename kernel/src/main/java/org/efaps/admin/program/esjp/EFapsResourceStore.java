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

package org.efaps.admin.program.esjp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.jci.stores.ResourceStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Checkin;
import org.efaps.db.Checkout;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * This Class implements the
 * <code>org.apache.commons.jci.stores.ResourceStore</code>. Through this it
 * is posible to access the dateabase with the normal Checkin or Checkin
 * methods.
 *
 * @author jmox
 * @version $Id$
 *
 */
public class EFapsResourceStore implements ResourceStore {

  // /////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EFapsResourceStore.class);

  /**
   * local Compiler used to get access to the different programm.java partsÄ
   */
  private Compiler         compiler = null;

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / desctructors

  public EFapsResourceStore(Compiler _compiler) {
    this.compiler = _compiler;
  }

  /**
   * The compiled class in <i>_resourceData</i> is stored with the name
   * <i>_resourceName</i> in the eFaps database (checked in). If the class
   * instance already exists in eFaps, the class data is updated. Otherwise, the
   * compiled class is new inserted in eFaps (related to the original Java
   * program).
   *
   * @param _resourceName
   *          name of the resource to stored (Java class name as file name)
   * @param _resourceData
   *          binary data of the compiled Java class
   * @todo exception handling
   */
  public void write(final String _resourceName, final byte[] _resourceData) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("write '" + _resourceName + "'");
    }
    try {
      final String resourceName = getInternalClassName(new File(_resourceName).getAbsoluteFile()).toString();
      final String javaClassName = resourceName.replaceAll("\\.class", "")
                                         .replaceFirst(".", "");

      final Long id = this.compiler.getclass2id().get(javaClassName);
      Instance instance;
      if (id == null) {
        final String parent = _resourceName.replaceAll(".class$", "").replaceAll(
            "\\$.*", "")
            + ".java";


        final Long parentId = this.compiler.getfile2id().get(new File(parent).getAbsolutePath());

        final Insert insert = new Insert(this.compiler.getClassType());
        insert.add("Name", javaClassName);
        insert.add("ProgramLink", "" + parentId);
        insert.executeWithoutAccessCheck();
        instance = insert.getInstance();
        insert.close();
      } else {
        instance = new Instance(this.compiler.getClassType(), id);
        this.compiler.getclass2id().remove(javaClassName);
      }

      final Checkin checkin = new Checkin(instance);
      checkin.executeWithoutAccessCheck(_resourceName, new ByteArrayInputStream(_resourceData),
          _resourceData.length);
    } catch (final Exception e) {
      LOG.error("unable to write to Database '" + _resourceName + "'", e);
    }
  }

  /**
   * Convert the file of the class name in a name without slashes / backslashes
   * (instead points are used for the internal representation of the class
   * name).
   *
   * @param _file file with name to convert
   */
  protected StringBuilder getInternalClassName(final File _file)  {
    StringBuilder ret = null;
    if (_file.getParentFile() == null)  {
      ret = new StringBuilder(_file.getName());
    } else  {
      ret = getInternalClassName(_file.getParentFile())
                  .append(".").append(_file.getName());
    }
    return ret;
  }

  /**
   * The compiled class is recieved from the eFaps database (checked out) using
   * the name <i>_resourceName</i>
   *
   * @param _resourceName
   *          name of the resource to be recieved (Java class name)
   *
   * @return Byte-Array containing the compiled Java class
   */
  public byte[] read(final String _resourceName) {
    byte[] ret = null;

    if (LOG.isDebugEnabled()) {
      LOG.debug("read '" + _resourceName + "'");
    }
    final SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes(this.compiler.getClassType().getName());
      query.addSelect("ID");
      query.addWhereExprEqValue("Name", _resourceName);
      query.executeWithoutAccessCheck();
      if (query.next()) {
        final Long id = (Long) query.get("ID");
        final Checkout checkout = new Checkout(new Instance(
                                        this.compiler.getClassType(), id));
        final InputStream is = checkout.executeWithoutAccessCheck();

        ret = new byte[is.available()];
        is.read(ret);
        is.close();

      }
    } catch (final EFapsException e) {
      LOG.error("could not access the Database for reading '" + _resourceName
          + "'", e);
    } catch (final IOException e) {
      LOG.error("could not read the Javaclass '" + _resourceName + "'", e);
    }
    return ret;
  }

  /**
   * no need here
   */
  public void remove(String arg0) {

  }

}
