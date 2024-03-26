/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.admin.datamodel.ui;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.api.ui.IOption;
import org.efaps.util.EFapsException;

/**
 * This Class is the representation of
 * {@link org.efaps.admin.datamodel.attributetype.LinkWithRanges} for the user
 * interface.<br>
 * Depending on the access mode (e.g. edit) the Value of a Field is presented in
 * an editable or non editable mode.
 *
 * @author The eFaps Team
 *
 */
public class LinkWithRangesUI
    extends AbstractProvider
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getValue(final UIValue _uiValue)
        throws EFapsException
    {
        final List<IOption> ret = new ArrayList<>();
        final Attribute attribute = _uiValue.getAttribute();
        if (attribute != null && attribute.hasEvents(EventType.RANGE_VALUE)) {
            for (final Return values : attribute.executeEvents(EventType.RANGE_VALUE, ParameterValues.UIOBJECT,
                            _uiValue, ParameterValues.ACCESSMODE, _uiValue.getTargetMode())) {
                ret.addAll((List<IOption>) values.get(ReturnValues.VALUES));
            }
        }
        return ret;
    }

    @Override
    public String validateValue(final UIValue _uiValue)
        throws EFapsException
    {
        return null;
    }
}
