/*
 * Copyright 2003 - 2016 The eFaps Team
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.stmt.selection.elements.AttributeElement;
import org.efaps.db.stmt.selection.elements.LinktoElement;
import org.efaps.eql2.IAttributeSelectElement;
import org.efaps.eql2.ILinktoSelectElement;
import org.efaps.eql2.ISelect;
import org.efaps.eql2.ISelectElement;
import org.efaps.eql2.ISelection;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Selection.
 *
 * @author The eFaps Team
 */
public final class Selection
{
    private static final Logger LOG = LoggerFactory.getLogger(Selection.class);

    /** The elements. */
    private final List<Select> selects = new ArrayList<>();


    /**
     * Gets the elements.
     *
     * @return the elements
     */
    public List<Select> getSelects()
    {
        return this.selects;
    }

    /**
     * Analyze.
     *
     * @param _baseTypes the base types
     * @param _sel the sel
     * @return the selection
     * @throws CacheReloadException the cache reload exception
     */
    private Selection analyze(final ISelection _sel,
                              final Collection<Type> _baseTypes)

        throws CacheReloadException
    {
        for (final ISelect sel : _sel.getSelects()) {
            final Select select = Select.get();
            this.selects.add(select);
            Collection<Type> currentTypes = _baseTypes;
            for (final ISelectElement ele : sel.getElements()) {
                if (ele instanceof IAttributeSelectElement) {
                    final String attrName = ((IAttributeSelectElement) ele).getName();
                    for (final Type type : currentTypes) {
                        final Attribute attr = type.getAttribute(attrName);
                        if (attr == null) {
                            LOG.error("Could not find Attribute '{}' on Type '{}'", attrName, type.getName());
                        }
                        final AttributeElement element = new AttributeElement().setAttribute(attr);
                        select.addElement(element);
                    }
                } else if (ele instanceof ILinktoSelectElement) {
                    final String attrName = ((ILinktoSelectElement) ele).getName();
                    final List<Type> linktoTypes = new ArrayList<>();
                    for (final Type type : currentTypes) {
                        final Attribute attr = type.getAttribute(attrName);
                        linktoTypes.add(attr.getLink());
                        final LinktoElement element = new LinktoElement().setAttribute(attr);
                        select.addElement(element);
                    }
                    currentTypes = linktoTypes;
                }
            }
        }
        return this;
    }

    /**
     * Gets the.
     *
     * @param _baseTypes the base types
     * @param _sel the sel
     * @return the selection
     * @throws CacheReloadException the cache reload exception
     */
    public static Selection get(final ISelection _sel,
                                final Type... _baseTypes)
        throws CacheReloadException
    {
        return new Selection().analyze(_sel, Arrays.asList(_baseTypes));
    }
}
