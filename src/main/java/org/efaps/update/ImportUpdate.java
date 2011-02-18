/*
 * Copyright 2003 - 2011 The eFaps Team
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


package org.efaps.update;

import java.util.List;
import java.util.Map;

import org.apache.commons.jexl.JexlContext;
import org.efaps.update.util.InstallationException;
import org.xml.sax.SAXException;


/**
 * Default implementation for import of objects by using xml.
 * Not supported yet.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ImportUpdate
    implements IUpdate
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateInDB(final JexlContext _jexlContext,
                           final UpdateLifecycle _step)
        throws InstallationException
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileApplication()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readXML(final List<String> _tags,
                        final Map<String, String> _attributes,
                        final String _text)
        throws SAXException
    {
    }
}
