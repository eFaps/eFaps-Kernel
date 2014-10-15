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
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.db.MultiPrintQuery;
import org.efaps.json.data.AbstractValue;
import org.efaps.json.data.DataList;
import org.efaps.json.data.DateTimeValue;
import org.efaps.json.data.DecimalValue;
import org.efaps.json.data.LongValue;
import org.efaps.json.data.ObjectData;
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

    public static DataList getDataList(final Statement _statement)
        throws EFapsException
    {
        final DataList ret = new DataList();
        final Map<String, String> mapping = _statement.getAlias2Selects();
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
        return ret;
    }

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
        }

        if (ret != null) {
            ret.setKey(_key);
        }
        return ret;
    }

}
