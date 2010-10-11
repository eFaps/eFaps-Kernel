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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.update.LinkInstance;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class FormUpdate
    extends AbstractCollectionUpdate
{

    /** Link from form to type as classification form. */
    private static final Link LINK2TYPE = new Link("Admin_UI_LinkIsTypeFormFor", "From", "Admin_DataModel_Type", "To");

    /**
     * Set of all links.
     */
    private static final Set<Link> ALLLINKS = new HashSet<Link>();
    static  {
        FormUpdate.ALLLINKS.add(FormUpdate.LINK2TYPE);
    }

    /**
     *
     * @param _url URL of the file
     */
    public FormUpdate(final URL _url)
    {
        super(_url, "Admin_UI_Form", FormUpdate.ALLLINKS);
    }

    /**
     * Creates new instance of class {@link FormDefinition}.
     *
     * @return new definition instance
     * @see FormDefinition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new FormDefinition();
    }

    /**
     * Definition for a Form.
     */
    public class FormDefinition
        extends Definition
    {
        /**
         * {@inheritDoc}
         */
        @Override
        protected void readXML(final List<String> _tags, final Map<String, String> _attributes, final String _text)
        {
            final String value = _tags.get(0);
            if ("type".equals(value)) {
                // assigns a type the form for which this form instance is the
                // classification form menu
                addLink(FormUpdate.LINK2TYPE, new LinkInstance(_text));
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }
    }
}
