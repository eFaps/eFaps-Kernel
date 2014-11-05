/*
 * Copyright 2003 - 2014 The eFaps Team
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.MultiPrintQuery;
import org.efaps.json.data.AbstractValue;
import org.efaps.json.data.DataList;
import org.efaps.json.data.DateTimeValue;
import org.efaps.json.data.DecimalValue;
import org.efaps.json.data.LongValue;
import org.efaps.json.data.ObjectData;
import org.efaps.json.data.StringListValue;
import org.efaps.json.data.StringValue;
import org.efaps.json.data.UUIDValue;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JSONData
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JSONData.class);

    /**
     * @param _statement Statement the datalist will be created for
     * @return a DataList
     * @throws EFapsException on error
     */
    public static DataList getDataList(final Statement _statement)
        throws EFapsException
    {
        DataList ret = new DataList();
        final Map<String, String> mapping = _statement.getAlias2Selects();
        if (_statement.isEsjp()) {
            try {
                  final Class<?> clazz = Class.forName(_statement.getEsjp(), false, EFapsClassLoader.getInstance());
                  final IEsjpExecute esjp = (IEsjpExecute) clazz.newInstance();
                  LOG.debug("Instantiated class: {}", esjp);
                  final List<String> parameters = _statement.getParameters();
                  if (parameters.isEmpty()) {
                      ret = esjp.execute(mapping);
                  } else {
                      ret = esjp.execute(mapping, parameters.toArray(new String[parameters.size()]));
                  }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOG.error("Could not invoke IEsjpQuery.", e);
                throw new EFapsException("Could not invoke IEsjpQuery.", e);
            }
        } else {
            final MultiPrintQuery multi = _statement.getMultiPrint();
            multi.execute();
            while (multi.next()) {
                final ObjectData data = new ObjectData();
                for (final Entry<String, String> entry : mapping.entrySet()) {
                    final Object obj = multi.getSelect(entry.getValue());
                    data.getValues().add(getValue(entry.getKey(), obj));
                }
                ret.add(data);
            }
        }
        return ret;
    }

    /**
     * @param _key  key the value is wanted for
     * @param _object oebjct to be converted
     * @return AbstractValue for the key
     */
    @SuppressWarnings("unchecked")
    public static AbstractValue<?> getValue(final String _key,
                                            final Object _object)
    {
        AbstractValue<? extends Object> ret = null;
        if (_object instanceof String) {
            ret = new StringValue().setValue((String) _object);
            ;
        } else if (_object instanceof BigDecimal) {
            ret = new DecimalValue().setValue((BigDecimal) _object);
        } else if (_object instanceof Long) {
            ret = new LongValue().setValue((Long) _object);
        } else if (_object instanceof DateTime) {
            ret = new DateTimeValue().setValue((DateTime) _object);
        } else if (_object instanceof UUID) {
            ret = new UUIDValue().setValue((UUID) _object);
        } else if (_object instanceof List) {
            final List<?> list = (List<?>) _object;
            if (!list.isEmpty()) {
                final Object inner = list.get(0);
                if (inner instanceof String) {
                    ret = new StringListValue().setValue((List<String>) list);
                }
            }
        }
        if (ret != null) {
            ret.setKey(_key);
        }
        return ret;
    }
}
