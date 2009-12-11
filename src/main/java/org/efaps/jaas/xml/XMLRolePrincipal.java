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

package org.efaps.jaas.xml;

/**
 * The class implements the {@link java.security.Principal} interface for a
 * role. The class is used from the {@link XMLUserLoginModule} class to implement
 * a JAAS login module and set the role principals.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class XMLRolePrincipal
    extends XMLAbstractPrincipal
{
    /**
     * Constructor used to create a new role principal instance.
     *
     * @param _name name of the user
     */
    XMLRolePrincipal(final String _name)
    {
        setName(_name);
    }
}
