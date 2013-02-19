/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.admin.datamodel.ui;

import org.efaps.admin.ui.field.Field;
import org.efaps.util.EFapsException;

/**
 * This class returns the different Html-Snipplets wich are needed for a
 * PasswordType.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PasswordUI
    extends AbstractUI
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEditHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        final StringBuffer ret = new StringBuffer();
        final Field field = _fieldValue.getField();
        ret.append("<input type=\"password\" size=\"").append(field.getCols()).append("\" name=\"").append(
                        field.getName()).append("\" " + "value=\"\">");
        return ret.toString();
    }
}
