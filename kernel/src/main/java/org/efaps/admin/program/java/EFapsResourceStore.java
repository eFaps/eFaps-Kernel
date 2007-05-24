/*
 * Copyright 2003 - 2007 The eFaps Team
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

package org.efaps.admin.program.java;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.efaps.db.Checkin;
import org.efaps.db.Checkout;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

public class EFapsResourceStore implements ResourceStore {

  // /////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG      = LogFactory
                                        .getLog(EFapsResourceStore.class);

  private Compiler         compiler = null;

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / desctructors

  public EFapsResourceStore() {

  }

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

      Long id = compiler.getclass2id().get(_resourceName);
      Instance instance;
      if (id == null) {
        String parent = _resourceName.replaceAll(".class$", "").replaceAll(
            "\\$.*", "")
            + ".java";
        Long parentId = this.compiler.getfile2id().get(parent);

        Insert insert = new Insert(this.compiler.getclassType());
        insert.add("Name", _resourceName);
        insert.add("ProgramLink", "" + parentId);
        insert.executeWithoutAccessCheck();
        instance = insert.getInstance();
        insert.close();
      } else {
        instance = new Instance(this.compiler.getclassType(), id);
        this.compiler.getclass2id().remove(_resourceName);
      }

      Checkin checkin = new Checkin(instance);
      checkin.execute(_resourceName, new ByteArrayInputStream(_resourceData),
          _resourceData.length);
    } catch (Exception e) {
      LOG.error(e);
    }
  }

  public byte[] read(final String _resourceName) {
    byte[] ret = null;
    InputStream is = null;
    if (LOG.isDebugEnabled()) {
      LOG.debug("read '" + _resourceName + "'");
    }
    SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes(this.compiler.getJavaType().getName());
      query.addSelect("ID");
      query.addWhereExprEqValue("Name", _resourceName);
      query.executeWithoutAccessCheck();
      if (query.next()) {
        Long id = (Long) query.get("ID");

        query.close();
        query = new SearchQuery();
        query.setQueryTypes(this.compiler.getclassType().getName());
        query.addWhereExprEqValue("ProgramLink", id.toString());
        query.addSelect("ID");
        query.executeWithoutAccessCheck();

        while (query.next()) {

          Long prog = (Long) query.get("ID");

          Checkout checkout = new Checkout(new Instance(this.compiler
              .getclassType(), prog));
          is = checkout.executeWithoutAccessCheck();
//TODO geht nicht wenn mehrere Programmteile
          ret = new byte[is.available()];
          is.read(ret);

        }
        is.close();

      }

    } catch (EFapsException e) {

      LOG.error("read(String)", e);
    } catch (IOException e) {

      LOG.error("read(String)", e);
    }

    return ret;
  }

  public void remove(final String _resourceName) {
  }

}
