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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.eql.stmt.AbstractExecStmt;
import org.efaps.eql.stmt.parts.select.AbstractSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: $
 */
public class ExecStmt
    extends AbstractExecStmt
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ExecStmt.class);

    @Override
    public List<Map<String, Object>> getData()
        throws Exception
    {
        final Map<String, AbstractSelect> mapping = getAlias2Selects();
        final Map<String, String> map = new LinkedHashMap<>();
        for (final Entry<String, AbstractSelect> entry : mapping.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getSelect());
        }

        final Class<?> clazz = Class.forName(getESJPName(), false, EFapsClassLoader.getInstance());
        final IEsjpExecute esjp = (IEsjpExecute) clazz.newInstance();
        LOG.debug("Instantiated class: {}", esjp);
        final List<String> parameters = getParameters();
        List<Map<String, Object>> ret;
        if (parameters.isEmpty()) {
            ret = esjp.execute(map);
        } else {
            ret = esjp.execute(map, parameters.toArray(new String[parameters.size()]));
        }
        return ret;
    }
}
