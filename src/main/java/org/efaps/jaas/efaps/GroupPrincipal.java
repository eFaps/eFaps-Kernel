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

package org.efaps.jaas.efaps;

/**
 * The class implements the {@link java.security.Principal} interface for a
 * user. The class is used from the {@link UserLoginModule} class to implement
 * a JAAS login module and set the group principals.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class GroupPrincipal
    extends AbstractPrincipal
{
    /**
     * Unique identifier used to serialize.
     */
    private static final long serialVersionUID = -6997130370815113314L;

    /**
     * Constructor used to create a new group principal instance.
     *
     * @param _name name of the user
     */
    protected GroupPrincipal(final String _name)
    {
        super(_name);
    }

    /**
     * Compares this principal to the specified object.
     *
     * @param _another object to compare to this principle
     * @return returns <i>true</i> if the other object is from this class and
     *         has the same name (method equals is used), otherwise <i>false</i>
     */
    //CHECKSTYLE:OFF
    @Override
    public boolean equals(final Object _another)
    {
        return _another instanceof GroupPrincipal && ((GroupPrincipal) _another).getName().equals(getName());
    }
}
