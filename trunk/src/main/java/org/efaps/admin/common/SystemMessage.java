/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.admin.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.datamodel.Status;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SystemMessage
    implements Job
{
    /**
     * Enum used for the status of message.
     */
    public enum MsgStatus {
        /**
         * unread messages exist.
         */
        UNREAD,

        /**
         * read messages exist.
         */
        READ;
    }

    /**
     * basis select.
     */
    private static final String SELECT = new SQLSelect().distinct(true).column("USERID")
        .from("T_CMSYSMSG2USER").getSQL();

    /**
     * Cache used for the MessageStatus.
     */
    private static final MsgCache CACHE = new MsgCache();

    /**
     * Execute the job.
     * @param _context JobExecutionContext
     * @throws JobExecutionException on error
     */
    @Override
    public void execute(final JobExecutionContext _context)
        throws JobExecutionException
    {
        SystemMessage.CACHE.update();
    }

    /**
     * @param _userId user id the status is wanted for
     * @return MsgStatus
     */
    public static MsgStatus getStatus(final Long _userId)
    {
        return SystemMessage.CACHE.userID2Status.get(_userId);
    }

    /**
     * has the given user read messages.
     * @param _userId   user id the status is wanted for
     * @return true if unread exist
     */
    public static boolean hasReadMsg(final Long _userId)
    {
        final MsgStatus status = SystemMessage.CACHE.userID2Status.get(_userId);
        return SystemMessage.MsgStatus.READ.equals(status);
    }

    /**
     * has the given user unread messages.
     * @param _userId   user id the status is wanted for
     * @return true if unread exist
     */
    public static boolean hasUnreadMsg(final Long _userId)
    {
        final MsgStatus status = SystemMessage.CACHE.userID2Status.get(_userId);
        return SystemMessage.MsgStatus.UNREAD.equals(status);
    }

    /**
     * Thread save cache.
     */
    private static final class MsgCache
    {
        /**
         * The map holds all cached data instances by Id. Because of the
         * double-checked locking idiom, the instance variable is defined
         * <i>volatile</i>.
         *
         * @see #get(Long)
         */
        private volatile Map<Long, SystemMessage.MsgStatus> userID2Status = null;

        /**
         * Constructor setting empty map.
         */
        private MsgCache()
        {
            this.userID2Status = new HashMap<Long,  SystemMessage.MsgStatus>();
        }

        /**
         * Update the MessageStatus.
         */
        public void update()
        {
            ConnectionResource con = null;
            boolean abort = true;
            try {
                con = Context.getThreadContext().getConnectionResource();

                final Map<Long, SystemMessage.MsgStatus> map = new HashMap<Long, SystemMessage.MsgStatus>();

                final String select = SystemMessage.SELECT + " where STATUS = "
                    + Status.find(UUID.fromString("87b82fee-69d3-4e45-aced-0d57c6a0cd1d"), "Unread").getId();
                final Statement stmt = con.getConnection().createStatement();

                final ResultSet rs = stmt.executeQuery(select);

                while (rs.next()) {
                    final long id = rs.getLong(1);
                    map.put(id, SystemMessage.MsgStatus.UNREAD);
                }
                rs.close();

                final StringBuilder bldr = new StringBuilder().append(SystemMessage.SELECT)
                    .append(" where STATUS = ")
                    .append(Status.find(UUID.fromString("87b82fee-69d3-4e45-aced-0d57c6a0cd1d"), "Read").getId());
                if (!map.isEmpty()) {
                    bldr.append(" and USERID not in (");
                    boolean first = true;
                    for (final long id : map.keySet()) {
                        if (first) {
                            first = false;
                        } else {
                            bldr.append(",");
                        }
                        bldr.append(id);
                    }
                    bldr.append(")");
                }

                final ResultSet rs2 = stmt.executeQuery(bldr.toString());

                while (rs2.next()) {
                    final long id = rs2.getLong(1);
                    map.put(id, SystemMessage.MsgStatus.READ);
                }
                rs.close();
                stmt.close();
                con.commit();
                this.userID2Status = map;
                abort = false;
            } catch (final EFapsException e) {
             // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (abort && con != null) {
                    try {
                        con.abort();
                    } catch (final EFapsException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

