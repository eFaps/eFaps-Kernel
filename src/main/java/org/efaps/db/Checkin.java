/*
 * Copyright 2003 - 2013 The eFaps Team
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
 * Author:          tmo
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.db;
import java.io.InputStream;

import org.efaps.admin.access.AccessCache;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.event.EventType;
import org.efaps.db.store.Resource;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class is used to check in a file to a given attribute of an object.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Checkin
    extends AbstractAction
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Checkin.class);

    /**
     * Constructor with a string as object id.
     *
     * @param _oid      oid of object on which the check in is made
     */
    public Checkin(final String _oid)
    {
        this(Instance.get(_oid));
    }

    /**
     * Constructor with an instance object as object id.
     *
     * @param _instance     instance on which the check in is made
     */
    public Checkin(final Instance _instance)
    {
        super.setInstance(_instance);
    }

    /**
     * @param _fileName     file name to check in (could include also the path)
     * @param _in           input stream with the binary data
     * @param _size         size of file in stream to check in (negative size
     *                      means that all from the stream must be written)
     * @see #executeWithoutAccessCheck(String, InputStream, int)
     *@throws EFapsException on error
     */
    public void execute(final String _fileName,
                        final InputStream _in,
                        final int _size)
        throws EFapsException
    {
        AccessCache.registerUpdate(getInstance());
        final boolean hasAccess = super.getInstance().getType().hasAccess(super.getInstance(),
                                                                          AccessTypeEnums.CHECKIN.getAccessType());
        if (!hasAccess) {
            throw new EFapsException(this.getClass(), "execute.NoAccess");
        }
        executeWithoutAccessCheck(_fileName, _in, _size);
    }

    /**
     * Executes the check in without checking the access rights (but with
     * triggers).
     * <ol>
     * <li>executes the pre check in trigger (if exists)</li>
     * <li>executes the check in trigger (if exists)</li>
     * <li>executes if no check in trigger exists or the check in trigger is
     *     not executed the update ({@see #executeWithoutTrigger})</li>
     * <li>executes the post check in trigger (if exists)</li>
     * </ol>
     *
     * @param _fileName     file name to check in (could include also the path)
     * @param _in           input stream with the binary data
     * @param _size         size of file in stream to check in (negative size
     *                      means that all from the stream must be written)
     * @throws EFapsException if checkout action fails
     * TODO:  history entries
     */
    public void executeWithoutAccessCheck(final String _fileName,
                                          final InputStream _in,
                                          final int _size)
        throws EFapsException
    {
        executeEvents(EventType.CHECKIN_PRE);
        if (!executeEvents(EventType.CHECKIN_OVERRIDE)) {
            executeWithoutTrigger(_fileName, _in, _size);
        }
        executeEvents(EventType.CHECKIN_POST);
    }

    /**
     * The check in is done without calling triggers and check of access
     * rights. Executes the check in:
     * <ul>
     * <li>the file is checked in</li>
     * <li>the file name and file length is stored in with
     *     {@link org.efaps.db.Update} (complete filename without path)</li>
     * </ul>
     *
     * @param _fileName file name to check in (could include also the path)
     * @param _in       input stream with the binary data
     * @param _size     size of file in stream to check in (negative size means
     *                  that all from the stream must be written)
     * @throws EFapsException if checkout action fails
     */
    public void executeWithoutTrigger(final String _fileName,
                                      final InputStream _in,
                                      final int _size)
        throws EFapsException
    {
        final Context context = Context.getThreadContext();
        Resource storeRsrc = null;
        boolean ok = false;
        try {
            getInstance().getType();
            storeRsrc = context.getStoreResource(getInstance(), Resource.StoreEvent.WRITE);
            storeRsrc.write(_in, _size, _fileName);
            storeRsrc.commit();
            storeRsrc = null;
            ok = true;
        } catch (final EFapsException e) {
            Checkin.LOG.error("could not checkin " + super.getInstance(), e);
            throw e;
        } finally {
            if (!ok) {
                context.abort();
            }
            if ((storeRsrc != null) && (storeRsrc.isOpened())) {
                storeRsrc.abort();
            }
        }
    }
}
