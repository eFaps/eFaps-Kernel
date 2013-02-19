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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.update.schema.access;

import java.net.URL;

import org.efaps.update.AbstractUpdate;

/**
 * @author The eFaps Team
 * @version $Id$
 * TODO:  description
 */
public class AccessTypeUpdate
    extends AbstractUpdate
{
    /**
     *
     * @param _url        URL of the file
     */
    public AccessTypeUpdate(final URL _url)
    {
        super(_url, "Admin_Access_AccessType");
    }

    /**
     * Creates new instance of class {@link AccessTypeUpdate.Definition}.
     *
     * @return new definition instance
     * @see AccessTypeUpdate.Definition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new Definition();
    }

    /**
     * Defines the access type.
     */
    private class Definition
        extends AbstractDefinition
    {
    }
}
