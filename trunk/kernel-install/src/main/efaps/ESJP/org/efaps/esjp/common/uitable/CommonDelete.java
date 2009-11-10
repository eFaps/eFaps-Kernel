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

package org.efaps.esjp.common.uitable;

import java.util.Map;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Delete;
import org.efaps.util.EFapsException;

/**
 * The ESJP is used to delete selected objects in a row of a web table.<br/>
 * <b>Example:</b><br/> <code>
 *   &lt;execute program="org.efaps.esjp.common.uitable.CommonDelete"&gt;
 *     &lt;property name="DeleteIndex"&gt;1&lt;/property&gt;
 *   &lt;/execute&gt;
 * </code>
 * Because an expand is made to show the web table, the oid with the index 1 in
 * the list of oids is used to delete.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("b8d99ead-ceb1-4ee8-bf6f-d1022cc4485f")
@EFapsRevision("$Rev$")
public class CommonDelete implements EventExecution
{
    /**
     * All selected oids are split by the pipe (<code>|</code> - meaning the
     * oids on the way to the row oid, like an expand) and deletes - depending
     * on the delete index - this oid.
     *
     * @param _parameter parameters from the submitted web table
     * @throws EFapsException if a delete of the selected oids is not possible
     * @return new Return()
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final String[] allOids = (String[]) _parameter.get(ParameterValues.OTHERS);
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        if (allOids != null) {

            int delIdx = 0;
            final String delIdxStr = (String) properties.get("DeleteIndex");
            if ((delIdxStr != null) && (delIdxStr.length() > 0)) {
                delIdx = Integer.parseInt(delIdxStr);
            }

            for (final String rowOids : allOids) {
                final String[] colOids = rowOids.split("\\|");
                (new Delete(colOids[delIdx])).execute();
            }
        }
        return new Return();
    }
}
