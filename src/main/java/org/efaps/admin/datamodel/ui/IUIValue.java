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

import org.apache.commons.lang3.ObjectUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.Field.Display;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public interface IUIValue
{
    /** The null replacement. */
    Object NULL = ObjectUtils.NULL;

    /**
     * Gets the display.
     *
     * @return the display
     */
    Display getDisplay();

    /**
     * Gets the field.
     *
     * @return the field
     */
    Field getField();

    /**
     * Gets the single instance of IUIValue.
     *
     * @return single instance of IUIValue
     */
    Instance getInstance();

    /**
     * Gets the call instance.
     *
     * @return the call instance
     */
    Instance getCallInstance();

    /**
     * Gets the object.
     *
     * @return the object
     */
    Object getObject();

    /**
     * Gets the attribute.
     *
     * @return the attribute
     * @throws EFapsException on error
     */
    Attribute getAttribute()
        throws EFapsException;
}
