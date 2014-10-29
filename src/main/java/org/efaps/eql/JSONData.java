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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JSONData
{

    /**
     * @param _statement Statement the datalist will be created for
     * @return a DataList
     * @throws EFapsException on error
     */
    public static DataList getDataList(final Statement _statement)
        throws EFapsException
    {
        DataList ret = new DataList();;
        if (_statement.isEsjp()) {
            try {
                  final Class<?> clazz = Class.forName(_statement.getEsjp(), false, EFapsClassLoader.getInstance());
                  final IEsjpQuery query = (IEsjpQuery) clazz.newInstance();
                  ret = query.getDataList();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new EFapsException("Could not invoke IEsjpQuery.", e);
            }
        } else {
            final List<IEsjpSelect> esjps = new ArrayList<>();
            for (final Entry<String, String> entry : _statement.getAlias2Esjp().entrySet()) {
                try {
                    final Class<?> clazz = Class.forName(entry.getValue(), false, EFapsClassLoader.getInstance());
                    final IEsjpSelect esjp = (IEsjpSelect) clazz.newInstance();
                    esjp.setKey(entry.getKey());
                    esjps.add(esjp);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new EFapsException("Could not invoke IEsjpSelect.", e);
                }
            }

            final Map<String, String> mapping = _statement.getAlias2Selects();
            final MultiPrintQuery multi = _statement.getMultiPrint();
            for (final IEsjpSelect esjp : esjps) {
                esjp.initialize(multi.getInstanceList());
            }
            multi.execute();
            while (multi.next()) {
                final ObjectData data = new ObjectData();
                for (final Entry<String, String> entry : mapping.entrySet()) {
                    final Object obj = multi.getSelect(entry.getValue());
                    data.getValues().add(getValue(entry.getKey(), obj));
                }
                for (final IEsjpSelect esjp : esjps) {
                    final Object obj = esjp.getValue(multi.getCurrentInstance());
                    data.getValues().add(getValue(esjp.getKey(), obj));
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
    private static AbstractValue<?> getValue(final String _key,
                                             final Object _object)
    {
        AbstractValue<? extends Object> ret = null;
        if (_object instanceof String) {
            ret = new StringValue().setValue((String) _object);;
        } else if (_object instanceof BigDecimal) {
            ret = new DecimalValue().setValue((BigDecimal) _object);
        } else if (_object instanceof Long) {
            ret = new LongValue().setValue((Long) _object);
        } else if (_object instanceof DateTime) {
            ret = new DateTimeValue().setValue((DateTime) _object);
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
