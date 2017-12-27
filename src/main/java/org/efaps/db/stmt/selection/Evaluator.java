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
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.db.Instance;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.util.EFapsException;

/**
 * The Class SelectionEvaluator.
 */
public final class Evaluator
{

    /** The init. */
    private boolean init;

    /** The selection. */
    private final Selection selection;

    /** The Access. */
    private Access access;

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
     * @throws EFapsException on Error
     */
    private void initialize(final boolean _step)
        throws EFapsException
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
     * Count.
     *
     * @return the int
     * @throws EFapsException the e faps exception
     */
    public int count()
        throws EFapsException
    {
        initialize(false);
        final Select select = this.selection.getInstSelects().get(Selection.BASEPATH);
        return select.getObjects(this).size();
    }

    /**
     * Gets the.
     *
     * @param <T> the generic type
     * @param _idx the idx
     * @return the t
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final int _idx)
        throws EFapsException
    {
        initialize(true);
        Object ret = null;
        final int idx = _idx - 1;
        if (this.selection.getSelects().size() > idx) {
            final Select select = this.selection.getSelects().get(idx);
            if (hasAccess(select)) {
                ret = select.getCurrent();
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
     * @throws EFapsException  on error
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final String _alias)
        throws EFapsException
    {
        initialize(true);
        Object ret = null;
        final Optional<Select> selectOpt = this.selection.getSelects().stream().filter(select -> _alias.equals(select
                        .getAlias())).findFirst();
        if (selectOpt.isPresent() && hasAccess(selectOpt.get())) {
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
        return (Instance) this.selection.getInstSelects().get(Selection.BASEPATH).getCurrent();
    }

    /**
     * Move the evaluator to the next value.
     * Skips values the User does not have access to.
     * @return true, if successful
     * @throws EFapsException on Error
     */
    public boolean next()
        throws EFapsException
    {
        initialize(false);
        boolean stepForward = true;
        boolean ret = true;
        while (stepForward && ret) {
            ret = step(this.selection.getAllSelects());
            stepForward = !this.access.hasAccess(inst());
        }
        return ret;
    }

    /**
     * Checks for access.
     *
     * @param _select the select
     * @return true, if successful
     * @throws EFapsException on Error
     */
    protected boolean hasAccess(final Select _select)
        throws EFapsException
    {
        boolean ret = true;
        final int size = _select.getElements().size();
        if (size == 1) {
            ret = this.access.hasAccess(inst());
        } else {
            int idx = 0;
            while (idx < size && ret) {
                final AbstractElement<?> element = _select.getElements().get(idx);
                idx++;
                ret = ret && this.access.hasAccess((Instance) this.selection.getInstSelects().get(element.getPath())
                                .getCurrent());
            }
        }
        return ret;
    }

    /**
     * Gets the Access.
     *
     * @return the Access
     */
    protected Access getAccess()
    {
        return this.access;
    }

    /**
     * Gets the selection.
     *
     * @return the selection
     */
    protected Selection getSelection()
    {
        return this.selection;
    }

    /**
     * Move the selects to the next value.
     *
     * @param _selects the selects
     * @return true, if successful
     */
    private boolean step(final Collection<Select> _selects)
    {
        boolean ret = !CollectionUtils.isEmpty(_selects);
        for (final Select select : _selects) {
            ret = ret && select.next();
        }
        return ret;
    }

    /**
     * Evaluate the access for the instances.
     *
     * @throws EFapsException on error
     */
    private void evalAccess()
        throws EFapsException
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
        for (final Entry<String, Select> entry : this.selection.getInstSelects().entrySet()) {
            entry.getValue().reset();
        }
        this.access = Access.get(AccessTypeEnums.READ.getAccessType(), instances);
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
