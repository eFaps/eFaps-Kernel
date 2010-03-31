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

package org.efaps.util.cache;

import org.efaps.util.EFapsException;

/**
 * The <code>CacheReloadException</code> class is an exception is thrown, if
 * a cache implementing {@link Cache} could not be reloaded.
 *
 * @author The eFaps Team
 * @version $Id$
 * @see Cache
 */
public class CacheReloadException
    extends EFapsException
{
    /**
     * Unique identifier used to serialize this class.
     */
    private static final long serialVersionUID = -2388991706315797381L;

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized, and may subsequently be initialized by a call to
     * {@link Throwable#initCause(Throwable)}.
     *
     * @param _message  detailed message (which is saved for later retrieval by
     *                  the {@link Throwable#getMessage()} method).
     */
    public CacheReloadException(final String _message)
    {
        this(_message, null);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * <br/>
     * Note that the detail message associated with cause is not automatically
     * incorporated in this exception's detail message.
     *
     * @param _message  detailed message (which is saved for later retrieval by
     *                  the {@link Throwable#getMessage()} method).
     * @param _cause    cause (which is saved for later retrieval by the
     *                  {@link Throwable#getCause()} method). (A null value is
     *                  permitted, and indicates that the cause is nonexistent
     *                  or unknown.)
     */
    public CacheReloadException(final String _message,
                                final Throwable _cause)
    {
        super(_message, _cause);
    }
}
