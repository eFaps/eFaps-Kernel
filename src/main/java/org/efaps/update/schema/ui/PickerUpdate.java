/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.update.schema.ui;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.efaps.admin.event.EventType;
import org.efaps.update.event.Event;

/**
 * @author The eFaps Update
 * @version $Id$
 */
public class PickerUpdate
    extends AbstractCollectionUpdate
{
    /**
     *
     * @param _url        URL of the file
     */
    public PickerUpdate(final URL _url)
    {
        super(_url, "Admin_UI_Picker");
    }

    /**
     * Creates new instance of class {@link PickerUpdate.PickerDefinition}.
     *
     * @return new definition instance
     * @see PickerUpdate.PickerDefinition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new PickerDefinition();
    }

    private class PickerDefinition
        extends Definition
    {
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("execute".equals(value))  {
                this.events.add(new Event(getValue("Name") + ".PickerEvent",
                                          EventType.UI_PICKER,
                                          _attributes.get("program"),
                                          _attributes.get("method"),
                                          _attributes.get("index")));
            } else  {
                super.readXML(_tags, _attributes, _text);
            }
        }
    }
}

