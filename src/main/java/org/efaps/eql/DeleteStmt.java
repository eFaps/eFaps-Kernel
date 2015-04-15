/*
 * Copyright 2003 - 2015 The eFaps Team
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

package org.efaps.eql;

import java.util.ArrayList;
import java.util.List;

import org.efaps.db.Delete;
import org.efaps.db.Instance;
import org.efaps.eql.stmt.AbstractDeleteStmt;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: $
 */
public class DeleteStmt
    extends AbstractDeleteStmt
{

    @Override
    public void execute()
        throws Exception
    {
        final List<Instance> instances;
        if (getInstances().isEmpty()) {
            instances = QueryBldrUtil.getInstances(this);
        } else {
            instances = new ArrayList<>();
            for (final String oid : getInstances()) {
                instances.add(Instance.get(oid));
            }
        }
        for (final Instance inst : instances) {
            final Delete delete = new Delete(inst);
            delete.execute();
        }
    }
}
