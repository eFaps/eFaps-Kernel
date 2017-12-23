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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.efaps.db.Instance;

/**
 * The Class SelectionEvaluator.
 */
public final class Evaluator
{

    /** The init. */
    private boolean init;

    /** The selection. */
    private final Selection selection;

    /**
     * Instantiates a new selection evaluator.
     *
     * @param _selection the selection
     */
    private Evaluator(final Selection _selection)
    {
        this.selection = _selection;
    }

    /**
     * Initialize.
     *
     * @param _step the step
     */
    private void initialize(final boolean _step)
    {
        if (!this.init) {
            evalAccess();
            this.init = true;
            if (_step) {
                step(this.selection.getAllSelects());
            }
        }
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
        initialize(true);
        Object ret = null;
        final int idx = _idx - 1;
        if (this.selection.getSelects().size() > idx) {
            final Select select = this.selection.getSelects().get(idx);
            ret = select.getCurrent();
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
        initialize(true);
        Object ret = null;
        final Optional<Select> selectOpt = this.selection.getSelects().stream()
                        .filter(select ->_alias.equals(select.getAlias()))
                        .findFirst();
        if (selectOpt.isPresent()) {
            ret = selectOpt.get().getCurrent();
        }
        return (T) ret;
    }

    /**
     * Inst.
     *
     * @return the instance
     */
    public Instance inst()
    {
        return (Instance) this.selection.getInstSelects().get("").getCurrent();
    }

    /**
     * Next.
     *
     * @return true, if successful
     */
    public boolean next()
    {
        initialize(false);
        return step(this.selection.getAllSelects());
    }

    /**
     * Move the selects to the next value.
     *
     * @param _selects the selects
     * @return true, if successful
     */
    private boolean step(final Collection<Select> _selects) {
        boolean ret = !CollectionUtils.isEmpty(_selects);
        for (final Select select : _selects) {
            ret = ret && select.next();
        }
        return ret;
    }

    /**
     * Evaluate the access for the instances.
     */
    private void evalAccess()
    {
        final List<Instance> instances = new ArrayList<>();
        while (step(this.selection.getInstSelects().values())) {
            for (final Entry<String, Select> entry : this.selection.getInstSelects().entrySet()) {
                final Instance inst = (Instance) entry.getValue().getCurrent();
                if (inst != null) {
                    instances.add(inst);
                }
            }
        }
        System.out.println(instances);
        for (final Entry<String, Select> entry : this.selection.getInstSelects().entrySet()) {
            entry.getValue().reset();
        }
    }

    /**
     * Gets the.
     *
     * @param _selection the selection
     * @return the selection evaluator
     */
    public static Evaluator get(final Selection _selection)
    {
        return new Evaluator(_selection);
    }
}
