/*
 * Copyright 2003 - 2013 The eFaps Team
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


package org.efaps.admin.datamodel.attributetype;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IBitEnum;
import org.efaps.admin.program.esjp.EFapsClassLoader;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class BitEnumType
    extends EnumType
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _attribute Attribute the enum class is defined for
     * @param _num       number from eFapsDatabase thta defines the enum
     * @return enum
     */
    @Override
    protected Object getEnum4Int(final Attribute _attribute,
                                 final Integer _num)
    {
        final List<IBitEnum> ret = new ArrayList<IBitEnum>();
        try {
            final Class<?> clazz = Class.forName(_attribute.getClassName(), false, EFapsClassLoader.getInstance());
            final Object[] consts = clazz.getEnumConstants();
            if (consts != null) {
                for (final Object cons : consts) {
                    if (BitEnumType.isSelected(_num, (IBitEnum) cons)) {
                        ret.add((IBitEnum) cons);
                    }
                }
            }
        } catch (final ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    protected Integer eval(final Attribute _attribute,
                           final Object[] _value)
    {
        final Integer ret;
        if (_value == null) {
            ret = null;
        } else {
            Integer tmp = null;
            for (final Object obj : _value) {
                if ((obj instanceof String) && (((String) obj).length() > 0)) {
                    tmp = tmp == null ? Integer.parseInt((String) obj) : tmp + Integer.parseInt((String) obj);
                } else if (obj instanceof Integer) {
                    tmp = tmp == null ? (Integer) obj : tmp + (Integer) obj;
                } else if (obj instanceof Long) {
                    tmp = tmp == null ? ((Long) obj).intValue() : tmp + ((Long) obj).intValue();
                } else  {
                    final Integer tmp2 = eval4Enum(_attribute, obj);
                    if (tmp2 != null) {
                        tmp = tmp == null ? tmp2 : tmp + tmp2;
                    }
                }
            }
            ret = tmp;
        }
        return ret;
    }


    /**
     * @param _int  the integer value
     * @param _enum enum to be check if it is selected
     * @return true if selected else false
     */
    public static boolean isSelected(final Integer _int,
                                     final IBitEnum _enum)
    {
        boolean ret = false;
        final int idx = _enum.getBitIndex();
        final BitSet bitset =  BitEnumType.getBitSet(_int);
        if (bitset.length() > idx) {
            ret = bitset.get(idx);
        }
        return ret;
    }

    /**
     * @param _int integer the BitSet is wanted for
     * @return BitSet representing the given integer
     */
    public static BitSet getBitSet(final int _int)
    {
        final char[] bits = Integer.toBinaryString(_int).toCharArray();
        ArrayUtils.reverse(bits);
        final BitSet bitSet = new BitSet(bits.length);
        for (int i = 0; i < bits.length; i++) {
            if (bits[i] == '1') {
                bitSet.set(i, true);
            } else {
                bitSet.set(i, false);
            }
        }
        return bitSet;
    }

    /**
     * @param _bitIndex bitindex the integer value is wanted for
     * @return integer value representing the bitindex
     */
    public static int getInt4Index(final int _bitIndex)
    {
        final BitSet bitSet = new BitSet(_bitIndex + 1);
        bitSet.set(_bitIndex);
        int ret = 0;
        for (int i = 0; i < bitSet.length(); ++i) {
            ret += bitSet.get(i) ? (1 << i) : 0;
        }
        return ret;
    }
}
