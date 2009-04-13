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

package org.efaps.esjp.earchive.node;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.earchive.INames;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("2966a217-7bc9-4556-b1c7-11d6bf25a3d7")
@EFapsRevision("$Rev$")
public class Node2Node implements INames {

  private final String name;
  private final Long parent;
  private final Long child;
  private final Instance instance;

  public Node2Node(final Instance _instance) throws EFapsException {
    this.instance = _instance;
    final SearchQuery query = new SearchQuery();
    query.setObject(_instance);
    query.addSelect("Name");
    query.addSelect("Parent");
    query.addSelect("Child");
    query.execute();
    query.next();
    this.name = (String) query.get("Name");
    this.parent = (Long) query.get("Parent");
    this.child = (Long) query.get("Child");
  }

  /**
   * Getter method for instance variable {@link #name}.
   *
   * @return value of instance variable {@link #name}
   */
  public String getName() {
    return this.name;
  }

  /**
   * Getter method for instance variable {@link #parent}.
   *
   * @return value of instance variable {@link #parent}
   */
  public Long getParent() {
    return this.parent;
  }

  /**
   * Getter method for instance variable {@link #child}.
   *
   * @return value of instance variable {@link #child}
   */
  public Long getChild() {
    return this.child;
  }

  /**
   * Getter method for instance variable {@link #instance}.
   *
   * @return value of instance variable {@link #instance}
   */
  public Instance getInstance() {
    return this.instance;
  }


  public static void connect(final Node _parentNode, final List<Node> _children)
      throws EFapsException {
    final StringBuilder cmd = new StringBuilder();

    cmd.append("insert into ").append(TABLE_NODE2NODE)
      .append("(id, typeid, parentid, childid)")
      .append(" values (?,?,?,?)");

    final Context context = Context.getThreadContext();
    ConnectionResource con = null;

    try {
      con = context.getConnectionResource();


      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());

        for (final Node child : _children) {
          final Long idTmp = Context.getDbType().getNewId(con.getConnection(),
                                               TABLE_NODE2NODE,
                                               "ID");
          stmt.setLong(1, idTmp);
          stmt.setLong(2, Type.get("eArchive_NodeDirectory2Directory").getId());
          stmt.setLong(3, _parentNode.getId());
          stmt.setLong(4, child.getId());
          stmt.addBatch();
        }

        final int[] rows = stmt.executeBatch();
//        if (rows == 0) {
////           TODO fehler schmeissen
//        }
      } finally {
        stmt.close();
      }
    con.commit();
  } catch (final SQLException e) {
//    TODO fehler schmeissen
    e.printStackTrace();
  } finally {
    if ((con != null) && con.isOpened()) {
      con.abort();
    }
  }
  }
}
