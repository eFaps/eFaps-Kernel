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

package org.efaps.admin.datamodel.attributetype;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IJaxb;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class JaxbType
    extends AbstractType
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
        throws EFapsException
    {
        Object ret = null;
        if (_objectList.size() < 1) {
            ret = null;
        } else {
            final List<Object> list = new ArrayList<Object>();
            for (final Object object : _objectList) {
                String str = null;
                if (object instanceof String) {
                    str = (String) object;
                } else if (object != null) {
                    str = object.toString();
                }
                if (str != null) {
                    list.add(getObject4String(_attribute, str));
                }
            }
            ret = list.isEmpty() ? null : (list.size() > 1 ? list : list.get(0));
        }
        return ret;
    }

    @Override
    protected void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                           final Attribute _attribute,
                           final Object... _values)
        throws SQLException
    {
        checkSQLColumnSize(_attribute, 1);
        _insertUpdate.column(_attribute.getSqlColNames().get(0), eval(_attribute, _values));
    }

    /**
     * @param _attribute Attribute the String value must be evaluated for
     * @param _value value to be evaluated
     * @return value for database
     * @throws SQLException on error
     */
    protected String eval(final Attribute _attribute,
                          final Object[] _value)
        throws SQLException
    {
        String ret = null;
        if (_value == null) {
            ret = null;
        } else if ((_value[0] instanceof String) && (((String) _value[0]).length() > 0)) {
            ret = (String) _value[0];
        } else {
            try {
                final Object object = _value[0];
                if (object != null) {
                    final Class<?> clazz = Class.forName(_attribute.getClassName(), false,
                                    EFapsClassLoader.getInstance());
                    final IJaxb jaxb = (IJaxb) clazz.newInstance();
                    final JAXBContext jc = JAXBContext.newInstance(jaxb.getClasses());
                    final Marshaller marshaller = jc.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                    final StringWriter writer = new StringWriter();
                    marshaller.marshal(_value[0], writer);
                    ret = writer.toString();
                }
            } catch (final ClassNotFoundException e) {
                throw new SQLException("ClassNotFoundException", e);
            } catch (final InstantiationException e) {
                throw new SQLException("InstantiationException", e);
            } catch (final IllegalAccessException e) {
                throw new SQLException("IllegalAccessException", e);
            } catch (final JAXBException e) {
                throw new SQLException("JAXBException", e);
            }
        }
        return ret;
    }

    /**
     * @param _attribute Attribute the Object is wanted 4
     * @param _str string value to be parsed by jaxb
     * @return object for jaxb
     * @throws EFapsException on error
     */
    protected Object getObject4String(final Attribute _attribute,
                                      final String _str)
        throws EFapsException
    {
        Object ret = null;
        try {
            if (_str != null && !_str.isEmpty()) {
                final Class<?> clazz = Class.forName(_attribute.getClassName(), false, EFapsClassLoader.getInstance());
                final IJaxb jaxb = (IJaxb) clazz.newInstance();
                final JAXBContext jc = JAXBContext.newInstance(jaxb.getClasses());
                final Unmarshaller unmarshaller = jc.createUnmarshaller();
                final StringReader reader = new StringReader(_str);
                ret = unmarshaller.unmarshal(reader);
            }
        } catch (final ClassNotFoundException e) {
            throw new EFapsException("ClassNotFoundException", e);
        } catch (final InstantiationException e) {
            throw new EFapsException("InstantiationException", e);
        } catch (final IllegalAccessException e) {
            throw new EFapsException("IllegalAccessException", e);
        } catch (final JAXBException e) {
            throw new EFapsException("JAXBException", e);
        }
        return ret;
    }
}
