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

package org.efaps.eql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.efaps.db.Instance;
import org.efaps.db.Update;
import org.efaps.eql.stmt.AbstractUpdateStmt;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: $
 */
public class UpdateStmt
    extends AbstractUpdateStmt
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
            final Update update = new Update(inst);
            for (final Entry<String, String> entry : getAttr2Value().entrySet()) {
                update.add(entry.getKey(), entry.getValue());
            }
            update.execute();
        }
    }
}
