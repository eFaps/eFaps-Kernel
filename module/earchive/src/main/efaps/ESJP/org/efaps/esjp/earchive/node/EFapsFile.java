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

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.earchive.INames;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author Jan Moxter
 * @version $Id$
 */
@EFapsUUID("30d42d98-6c4d-445e-bf89-815ca3ec12ae")
@EFapsRevision("$Rev$")
public class EFapsFile implements INames {

  /**
   * Id of this EFapsFile;
   */
  private long id;

  /**
   * Name of this EFapsFile;
   */
  private String name;

  /**
   * Size of this EFapsFile;
   */
  private Long size;

  private String MD5;

  public static EFapsFile createFile(final InputStream _inputStream,
                                     final String _name, final Long _size,
                                     final String _md5)
      throws EFapsException {
    final EFapsFile ret = new EFapsFile();
    ret.setName(_name);
    ret.setSize(_size);
    ret.setMD5(_md5);
    final StringBuilder cmd = new StringBuilder();
    cmd.append("insert into ").append(TABLE_FILE)
      .append("(")
      .append(TABLE_FILE_C_ID).append(",")
      .append(TABLE_FILE_C_TYPEID).append(",")
      .append(TABLE_FILE_C_FILELENGTH).append(",")
      .append(TABLE_FILE_C_FILENAME).append(",")
      .append(TABLE_FILE_C_MD5FILE).append(",")
      .append(TABLE_FILE_C_MD5DELTA).append(")")
      .append(" values (?,?,?,?,?,?)");
    final Context context = Context.getThreadContext();
    ConnectionResource con = null;

    try {
      con = context.getConnectionResource();
      final long id = Context.getDbType().getNewId(con.getConnection(), TABLE_FILE, "ID");
      final long typeid = Type.get(TYPE_FILE).getId();

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, id);
        stmt.setLong(2, typeid);
        stmt.setLong(3, _size);
        stmt.setString(4, _name);
        stmt.setString(5, _md5);
        stmt.setString(6, "empty");
        ret.setId(id);
        final int rows = stmt.executeUpdate();
        if (rows == 0) {
          // TODO fehler schmeissen
        }
      } finally {
        stmt.close();
      }
      con.commit();

      final Instance fileInstance = Instance.get(Type.get(TYPE_FILE), ret.id);

      final Checkin checkin = new Checkin(fileInstance);
      checkin.execute(_name, _inputStream, _size.intValue());

    } catch (final SQLException e) {
      // TODO fehler schmeissen
      e.printStackTrace();
    } finally {
      if ((con != null) && con.isOpened()) {
        con.abort();
      }
    }
    return ret;
  }

  public static EFapsFile createFile() throws EFapsException {
    final EFapsFile ret = new EFapsFile();

    final StringBuilder cmd = new StringBuilder();
    cmd.append("insert into ").append(TABLE_FILE)
      .append("(")
      .append(TABLE_FILE_C_ID).append(",")
      .append(TABLE_FILE_C_TYPEID).append(",")
      .append(TABLE_FILE_C_FILELENGTH).append(",")
      .append(TABLE_FILE_C_FILENAME).append(",")
      .append(TABLE_FILE_C_MD5FILE).append(",")
      .append(TABLE_FILE_C_MD5DELTA).append(")")
      .append(" values (?,?,?,?,?,?)");
    final Context context = Context.getThreadContext();
    ConnectionResource con = null;

    try {
      con = context.getConnectionResource();
      final long idTmp = Context.getDbType().getNewId(con.getConnection(), TABLE_FILE, "ID");
      final long typeid = Type.get(TYPE_FILE).getId();

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, idTmp);
        stmt.setLong(2, typeid);
        stmt.setLong(3, new Long(0));
        stmt.setString(4, "empty");
        stmt.setString(5, "empty");
        stmt.setString(6, "empty");
        ret.setId(idTmp);
        final int rows = stmt.executeUpdate();
        if (rows == 0) {
          // TODO fehler schmeissen
        }
      } finally {
        stmt.close();
      }
      con.commit();
    } catch (final SQLException e) {
      // TODO fehler schmeissen
      e.printStackTrace();
    } finally {
      if ((con != null) && con.isOpened()) {
        con.abort();
      }
    }
    return ret;
  }

  public static EFapsFile getFile(final Long _fileId) throws EFapsException {

    final StringBuilder cmd = new StringBuilder();
    //statement for the rootnode
    cmd.append(" select ")
      .append(TABLE_FILE_C_ID).append(",")
      .append(TABLE_FILE_C_FILENAME).append(",")
      .append(TABLE_FILE_C_FILELENGTH).append(",")
      .append(TABLE_FILE_C_MD5FILE).append(",")
      .append(TABLE_FILE_C_MD5DELTA)
      .append(" from ").append(TABLE_FILE)
      .append(" where ").append(TABLE_FILE_C_ID).append(" = ?");

    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    final EFapsFile ret = new EFapsFile();
    try {

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, _fileId);
        final ResultSet resultset = stmt.executeQuery();

        if (resultset.next()) {
          ret.setId(_fileId);
          ret.setName(resultset.getString(2));
          ret.setSize(resultset.getLong(3));
          ret.setMD5(resultset.getString(4));
        }
        resultset.close();
      } finally {
        stmt.close();
      }
      con.commit();
    } catch (final SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if ((con != null) && con.isOpened()) {
        con.abort();
      }
    }
    return ret;
  }

  /**
   * Getter method for instance variable {@link #id}.
   *
   * @return value of instance variable {@link #id}
   */
  public long getId() {
    return this.id;
  }

  /**
   * @param id
   */
  private void setId(final long _id) {
   this.id = _id;
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
   * Setter method for instance variable {@link #name}.
   *
   * @param name value for instance variable {@link #name}
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Getter method for instance variable {@link #size}.
   *
   * @return value of instance variable {@link #size}
   */
  public int getSize() {
    return this.size.intValue();
  }

  /**
   * Setter method for instance variable {@link #size}.
   *
   * @param size value for instance variable {@link #size}
   */
  public void setSize(final Long size) {
    this.size = size;
  }

  /**
   * Getter method for instance variable {@link #mD5}.
   *
   * @return value of instance variable {@link #mD5}
   */
  public String getMD5() {
    return this.MD5;
  }

  /**
   * Setter method for instance variable {@link #mD5}.
   *
   * @param md5 value for instance variable {@link #mD5}
   */
  public void setMD5(final String md5) {
    this.MD5 = md5;
  }
}
