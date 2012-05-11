/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.admin.ui.field;

import org.efaps.util.cache.CacheReloadException;


/**
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldCommand
    extends Field
{
    /**
     * Must the button rendered? Default: true
     */
    private boolean renderButton = true;

    /**
     * Must the field appended?
     */
    private boolean append;

    /**
     * Target field.
     */
    private String targetField;

    /**
     * Icon to be displayed on the button.
     */
    private String buttonIcon;

    /**
     *
     * @param _id       id of the field
     * @param _uuid     UUID of the field
     * @param _name     name of the field
     */
    public FieldCommand(final long _id,
                        final String _uuid,
                        final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Field}.
     *
     * @param _id       id to search in the cache
     * @return instance of class {@link Field}
     */
    public static FieldCommand get(final long _id)
    {
        return (FieldCommand) Field.get(_id);
    }

    /**
     * Sets the property for this field command. This includes
     * <ul>
     * <li>{@link #renderButton}</li>
     * <li>{@link #append}</li>
     * <li>{@link #targetField}</li>
     * </ul>
     *
     * @param _name     name / key of the property
     * @param _value    value of the property
     * @throws CacheReloadException from called super property method
     */
    @Override
    protected void setProperty(final String _name,
                               final String _value)
        throws CacheReloadException
    {
        if ("CmdRenderButton".equals(_name)) {
            this.renderButton = !("false".equalsIgnoreCase(_value));
        } else if ("CmdAppend".equals(_name)) {
            this.append = "true".equalsIgnoreCase(_value);
        } else if ("CmdTargetField".equals(_name)) {
            this.targetField = _value;
        } else if ("CmdIcon".equals(_name)) {
            this.buttonIcon = _value;
        }   else {
            super.setProperty(_name, _value);
        }
    }

    /**
     * Getter method for instance variable {@link #renderButton}.
     *
     * @return value of instance variable {@link #renderButton}
     */
    public boolean isRenderButton()
    {
        return this.renderButton;
    }

    /**
     * Getter method for instance variable {@link #append}.
     *
     * @return value of instance variable {@link #append}
     */
    public boolean isAppend()
    {
        return this.append;
    }

    /**
     * Getter method for instance variable {@link #targetField}.
     *
     * @return value of instance variable {@link #targetField}
     */
    public String getTargetField()
    {
        return this.targetField;
    }

    /**
     * Getter method for the instance variable {@link #buttonIcon}.
     *
     * @return value of instance variable {@link #buttonIcon}
     */
    public String getButtonIcon()
    {
        return this.buttonIcon;
    }
}
