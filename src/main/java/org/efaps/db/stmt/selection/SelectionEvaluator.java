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

package org.efaps.db.stmt.selection;

import java.util.List;
import java.util.Optional;

/**
 * The Class SelectionEvaluator.
 */
public final class SelectionEvaluator
{

    /** The selection. */
    private final Selection selection;

    /**
     * Instantiates a new selection evaluator.
     *
     * @param _selection the selection
     */
    private SelectionEvaluator(final Selection _selection)
    {
        this.selection = _selection;
    }

    /**
     * Gets the.
     *
     * @param _selection the selection
     * @return the selection evaluator
     */
    public static SelectionEvaluator get(final Selection _selection)
    {
        return new SelectionEvaluator(_selection);
    }

    /**
     * Gets the.
     *
     * @param <T> the generic type
     * @param _idx the idx
     * @return the t
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final int _idx)
    {
        Object ret = null;
        final int idx = _idx - 1;
        if (this.selection.getSelects().size() > idx) {
            final Select select = this.selection.getSelects().get(idx);
            final List<Object> objects = select.getObjects();
            if (objects.size() == 1) {
                ret = objects.get(0);
            }
        }
        return (T) ret;
    }

    /**
     * Gets the.
     *
     * @param <T> the generic type
     * @param _alias the alias
     * @return the t
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final String _alias)
    {
        Object ret = null;
        final Optional<Select> selectOpt = this.selection.getSelects().stream()
                        .filter(select ->_alias.equals(select.getAlias()))
                        .findFirst();
        if (selectOpt.isPresent()) {
            final List<Object> objects = selectOpt.get().getObjects();
            if (objects.size() == 1) {
                ret = objects.get(0);
            }
        }
        return (T) ret;
    }
}
