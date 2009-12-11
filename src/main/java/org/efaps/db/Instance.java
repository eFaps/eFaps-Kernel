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

package org.efaps.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Type;

/**
 * The class is used to store one object id of an instance (defined with type
 * and id). The string representation is the type id plus point plus id.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Instance
    implements Serializable
{
    /**
     * Serial Version unique identifier.
     */
    private static final long serialVersionUID = -5587167060980613742L;

    /**
     * The instance variable stores the type definition for which this class is
     * the instance.
     *
     * @see #getType()
     */
    private transient Type type;

    /**
     * The instance variable stores the database id of the instance in the
     * database.
     *
     * @see #getId()
     */
    private final long id;

    /**
     * Key for this instance.
     */
    private final String key;

    /**
     * Constructor used if the type and the database id is known.
     *
     * @param _type         type of the instance
     * @param _id           id in the database of the instance
     * @param _instanceKey  key to this instance
     */
    private Instance(final Type _type,
                     final long _id,
                     final String _instanceKey)
    {
        this.type = _type;
        this.id = _id;
        this.key = _instanceKey;
    }

    /**
     * The string representation which is defined by this instance is returned.
     *
     * @return string representation of the object id
     */
    public String getOid()
    {
        String ret = null;
        if ((getType() != null) && (getId() != 0)) {
            ret = getType().getId() + "." + getId();
        }
        return ret;
    }

    /**
     * @return id represented by this instance
     * @see #id
     */
    @Override()
    public int hashCode()
    {
        return (int) this.id;
    }

    /**
     * @param _obj Object to compare
     * @return <i>true</i> if the given object in _obj is an instance and holds
     *         the same type and id
     * @see #id
     * @see #type
     */
    @Override()
    public boolean equals(final Object _obj)
    {
        boolean ret = false;
        if (_obj instanceof Instance) {
            final Instance other = (Instance) _obj;
            ret = (other.getId() == getId()) && (other.getType().getId() == getType().getId());
        }
        return ret;
    }

    /**
     * First, all not transient instance variables are stored, then the UUID of
     * the type is stored.
     *
     * @param _out    object output stream
     * @throws IOException            from inside called methods
     */
    private void writeObject(final ObjectOutputStream _out)
        throws IOException
    {
        _out.defaultWriteObject();
        _out.writeObject(this.type.getUUID());
    }

    /**
     * First all not transient instance variables are read, then the UUID of
     * the type is read and the type is initialized.
     *
     * @param _in   object input stream
     * @throws IOException            from inside called methods
     * @throws ClassNotFoundException if a class not found
     * TODO: update type instance if it is final....
     */
    private void readObject(final ObjectInputStream _in)
        throws IOException, ClassNotFoundException
    {
        _in.defaultReadObject();
        this.type = Type.get((UUID) _in.readObject());
    }

    /**
     * This is the getter method for the instance variable {@link #type}.
     *
     * @return value of instance variable {@link #type}
     * @see #type
     */
    public Type getType()
    {
        return this.type;
    }

    /**
     * This is the getter method for the instance variable {@link #id}.
     *
     * @return value of instance variable {@link #id}
     * @see #id
     */
    public long getId()
    {
        return this.id;
    }

    /**
     * Getter method for instance variable {@link #key}.
     *
     * @return value of instance variable {@link #key}
     */
    public String getKey()
    {
        return this.key;
    }

    /**
     * The method returns a string representation of the instance object. It
     * does not replace method {@link #getOid}!.
     *
     * @return string representation of this instance object
     */
    @Override()
    public String toString()
    {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("oid", getOid())
            .append("type", getType())
            .append("id", getId())
            .toString();
    }

    /**
     *
     * @param _type     type of the instance
     * @param _id       id of the instance
     * @return instance
     */
    public static Instance get(final Type _type,
                               final long _id)
    {
        String keyTmp = null;
        if ((_type != null) && (_id != 0)) {
            keyTmp = _type.getId() + "." + _id;
        }
        return get(_type, _id, keyTmp);
    }

    /**
     *
     * @param _type     type of the instance
     * @param _id       id of the instance
     * @param _key      key of the instance
     * @return instance
     */
    public static Instance get(final Type _type,
                               final long _id,
                               final String _key)
    {
        return new Instance(_type, _id, _key);
    }

    /**
     *
     * @param _type     type of the instance
     * @param _id       id of the instance as string
     * @return instance
     */
    public static Instance get(final Type _type,
                               final String _id)
    {
        return get(_type, _id, null);
    }

    /**
     *
     * @param _type     type of the instance
     * @param _id       id of the instance
     * @param _key      key of the instance
     * @return instance
     */
    public static Instance get(final Type _type,
                               final String _id,
                               final String _key)
    {
        final long idTmp;
        if (_id != null && _id.length() > 0) {
            idTmp = Long.parseLong(_id);
        } else {
            idTmp = 0;
        }
        return get(_type, idTmp, _key);
    }

    /**
     *
     * @param _type     type of the instance
     * @param _id       id of the instance
     * @return instance
     */
    public static Instance get(final String _type,
                               final String _id)
    {
        return get(_type, _id, null);
    }

    /**
     *
     * @param _type     type of the instance
     * @param _id       id of the instance
     * @param _key      key of the instance
     * @return instance
     */
    public static Instance get(final String _type,
                               final String _id,
                               final String _key)
    {
        Type typeTmp = null;
        if ((_type != null) && (_type.length() > 0)) {
            typeTmp = Type.get(_type);
        }
        return get(typeTmp, _id, _key);
    }

    /**
     *
     * @param _oid  eFaps object id of the instance
     * @return instance
     */
    public static Instance get(final String _oid)
    {
        final Type typeTmp;
        final long idTmp;
        if (_oid != null) {
            final int index = _oid.indexOf(".");
            if (index >= 0) {
                typeTmp = Type.get(Long.parseLong(_oid.substring(0, index)));
                idTmp = Long.parseLong(_oid.substring(index + 1));
            } else {
                typeTmp = null;
                idTmp = 0;
            }
        } else {
            typeTmp = null;
            idTmp = 0;
        }
        return get(typeTmp, idTmp);
    }
}
