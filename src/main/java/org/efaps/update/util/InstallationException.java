/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.update.util;

/**
 * Exception which are thrown within the installation of an eFaps application.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class InstallationException
    extends Exception
{
    /**
     * Serial version ID.
     */
    private static final long serialVersionUID = 2167502421695022437L;

    /**
     * Initializes the eFaps installation exception with an error message.
     *
     * @param _text     error message
     */
    public InstallationException(final String _text)
    {
        super(_text);
    }

    /**
     * Initializes the eFaps installation exception with an error message and
     * the original exception cause.
     *
     * @param _text         error message
     * @param _throwable    embedded exception which was original thrown
     */
    public InstallationException(final String _text,
                                 final Throwable _throwable)
    {
        super(_text, _throwable);
    }
}
