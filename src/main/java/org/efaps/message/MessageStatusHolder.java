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

package org.efaps.message;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.datamodel.Status;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds the status of the messages including their count.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class MessageStatusHolder
    implements Job
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MessageStatusHolder.class);

    /**
     * basis select.
     */
    private static final String SELECT = "SELECT DISTINCT userid, count(*)Ê"
        + "FROM t_msg2user WHERE status = ? GROUP BY userid";

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
        MessageStatusHolder.CACHE.update();
    }

    /**
     * has the given user read messages.
     * @param _userId   user id the status is wanted for
     * @return true if unread exist
     */
    public static boolean hasReadMsg(final Long _userId)
    {
        return MessageStatusHolder.CACHE.userID2Read.containsKey(_userId);
    }

    /**
     * Count of read messages.
     * @param _userId  user id the count is wanted for
     * @return count of read messages
     */
    public static int getReadCount(final Long _userId)
    {
        int ret = 0;
        if (MessageStatusHolder.CACHE.userID2Read.containsKey(_userId)) {
            ret = MessageStatusHolder.CACHE.userID2Read.get(_userId);
        }
        return ret;
    }

    /**
     * has the given user unread messages.
     * @param _userId   user id the status is wanted for
     * @return true if unread exist
     */
    public static boolean hasUnreadMsg(final Long _userId)
    {
        return MessageStatusHolder.CACHE.userID2UnRead.containsKey(_userId);
    }

    /**
     * Count of unread messages.
     * @param _userId  user id the count is wanted for
     * @return count of unread messages
     */
    public static int getUnReadCount(final Long _userId)
    {
        int ret = 0;
        if (MessageStatusHolder.CACHE.userID2UnRead.containsKey(_userId)) {
            ret = MessageStatusHolder.CACHE.userID2UnRead.get(_userId);
        }
        return ret;
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
         */
        private volatile Map<Long, Integer> userID2UnRead = null;

        /**
         * The map holds all cached data instances by Id. Because of the
         * double-checked locking idiom, the instance variable is defined
         * <i>volatile</i>.
         */
        private volatile Map<Long, Integer> userID2Read = null;

        /**
         * Constructor setting empty map.
         */
        private MsgCache()
        {
            this.userID2UnRead = new HashMap<Long,  Integer>();
            this.userID2Read = new HashMap<Long,  Integer>();
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

                final Map<Long, Integer> unread = new HashMap<Long, Integer>();
                final Map<Long, Integer> read = new HashMap<Long, Integer>();

                final PreparedStatement stmt = con.getConnection().prepareStatement(MessageStatusHolder.SELECT);
                stmt.setLong(1, Status.find(UUID.fromString("87b82fee-69d3-4e45-aced-0d57c6a0cd1d"), "Unread").getId());

                final ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    final long id = rs.getLong(1);
                    final Integer count = rs.getInt(1);
                    unread.put(id, count);
                }
                rs.close();

                stmt.setLong(1, Status.find(UUID.fromString("87b82fee-69d3-4e45-aced-0d57c6a0cd1d"), "Read").getId());
                final ResultSet rs2 = stmt.executeQuery();

                while (rs2.next()) {
                    final long id = rs2.getLong(1);
                    final Integer count = rs.getInt(1);
                    read.put(id, count);
                }
                rs.close();
                stmt.close();
                con.commit();
                this.userID2UnRead = unread;
                this.userID2Read = read;
                abort = false;
            } catch (final EFapsException e) {
                MessageStatusHolder.LOG.error("EFapsException");
            } catch (final IllegalStateException e) {
                MessageStatusHolder.LOG.error("IllegalStateException");
            } catch (final SQLException e) {
                MessageStatusHolder.LOG.error("SQLException");
            } finally {
                if (abort && con != null) {
                    try {
                        con.abort();
                    } catch (final EFapsException e) {
                        MessageStatusHolder.LOG.error("EFapsException");
                    }
                }
            }
        }
    }
}

