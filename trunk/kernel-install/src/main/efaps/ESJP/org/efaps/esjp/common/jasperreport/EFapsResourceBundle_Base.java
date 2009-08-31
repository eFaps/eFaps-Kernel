/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.esjp.common.jasperreport;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * This Class is used as an accesspoint to the eFaps DBProperties.This is
 * necessary because the JasperReport does need a
 * <code>java.util.ResourceBundle</code>.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("a7fd126d-05c5-4974-a7d8-f3fd19736764")
@EFapsRevision("$Rev$")
abstract class EFapsResourceBundle_Base extends ResourceBundle
{
    /**
     * @see java.util.ResourceBundle#getKeys()
     * @return null
     */
    @Override
    public Enumeration<String> getKeys()
    {
        return null;
    }

    /**
     * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
     * @param _key key used or the value
     * @return String from the DBProperties
     */
    @Override
    protected Object handleGetObject(final String _key)
    {
        return DBProperties.getProperty(_key);
    }
}
