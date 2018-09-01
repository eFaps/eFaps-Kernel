/*
 * Copyright 2003 - 2018 The eFaps Team
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
 */
package org.efaps.db.stmt.selection.elements;

import org.efaps.util.EFapsException;

/**
 * The Interface IProxy.
 */
public interface IProxy
{

    /**
     * Gets the value.
     *
     * @param _object the object
     * @return the value
     * @throws EFapsException the eFaps exception
     */
    Object getValue(Object _object)
        throws EFapsException;

}
