/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.db.stmt.print;

import java.util.LinkedHashSet;
import java.util.Set;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.stmt.selection.ISelectionProvider;
import org.efaps.db.stmt.selection.Selection;
import org.efaps.util.EFapsException;

/**
 * The Class AbstractPrint.
 *
 * @author The eFaps Team
 */
public abstract class AbstractPrint
    implements ISelectionProvider
{

    /** The selection. */
    private Selection selection;

    /** The types. */
    private final Set<Type> types = new LinkedHashSet<>();

    /**
     * Gets the selection.
     *
     * @return the selection
     * @throws EFapsException on error
     */
    @Override
    public Selection getSelection()
        throws EFapsException
    {
        return this.selection;
    }

    /**
     * Sets the selection.
     *
     * @param _selection the new selection
     */
    protected void setSelection(final Selection _selection)
    {
        this.selection = _selection;
    }

    /**
     * Adds the type.
     *
     * @param _type the type
     */
    protected void addType(final Type _type)
    {
        this.types.add(_type);
    }

    /**
     * Gets the types.
     *
     * @return the types
     */
    public Set<Type> getTypes()
    {
        return this.types;
    }
}
