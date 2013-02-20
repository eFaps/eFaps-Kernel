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

package org.efaps.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Type;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class is used to store one object id of an instance (defined with type and id). The string representation is the
 * type id plus point plus id.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Instance
    implements Serializable
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Instance.class);

    /**
     * Serial Version unique identifier.
     */
    private static final long serialVersionUID = -5587167060980613742L;

    /**
     * The instance variable stores the type definition for which this class is the instance.
     *
     * @see #getType()
     */
    private transient Type type;

    /**
     * The instance variable stores the database id of the instance in the database.
     *
     * @see #getId()
     */
    private final long id;

    /**
     * Key for this instance.
     */
    private final String key;

    /**
     * Is the information of GeneralId retrieved from the eFaps DataBase.
     */
    private boolean generalised;

    /**
     * The generalId of this Instance.
     */
    private long generalId = 0;

    /**
     * The ExhangeID of this Instance.
     */
    private long exchangeId = 0;

    /**
     * The ExchangeSystemID of this Instance.
     */
    private long exchangeSystemId = 0;

    /**
     * Constructor used if the type and the database id is known.
     *
     * @param _type type of the instance
     * @param _id id in the database of the instance
     * @param _instanceKey key to this instance
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
     * @return id represented by this instance
     * @see #id
     */
    @Override
    public int hashCode()
    {
        return (int) this.id;
    }

    /**
     * @param _obj Object to compare
     * @return <i>true</i> if the given object in _obj is an instance and holds the same type and id
     * @see #id
     * @see #type
     */
    @Override
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
     * First, all not transient instance variables are stored, then the UUID of the type is stored.
     *
     * @param _out object output stream
     * @throws IOException from inside called methods
     */
    private void writeObject(final ObjectOutputStream _out)
        throws IOException
    {
        _out.defaultWriteObject();
        _out.writeObject(this.type.getUUID());
    }

    /**
     * First all not transient instance variables are read, then the UUID of the type is read and the type is
     * initialized.
     *
     * @param _in object input stream
     * @throws IOException from inside called methods
     * @throws CacheReloadException if a class not found
     * @throws ClassNotFoundException on error
     *
     */
    private void readObject(final ObjectInputStream _in)
        throws IOException, ClassNotFoundException, CacheReloadException
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
     * Getter method for instance variable {@link #key}.
     *
     * @return value of instance variable {@link #key}
     */
    public String getKey()
    {
        return this.key;
    }

    /**
     * Method to evaluate if this instance is an valid instance. Meaning that it has a valid type and a valid id.
     *
     * @return true if valid, else false
     */
    public boolean isValid()
    {
        return this.type != null && this.id > 0;
    }

    /**
     * @throws EFapsException on error
     */
    private void check4Generalised()
        throws EFapsException
    {
        if (this.type.isGeneralInstance() && !this.generalised) {
            GeneralInstance.generaliseInstance(this);
            this.generalised = true;
        }
    }

    /**
     * Setter method for instance variable {@link #generalised}.
     *
     * @param _generalised value for instance variable {@link #generalised}
     */

    protected void setGeneralised(final boolean _generalised)
    {
        this.generalised = _generalised;
    }

    /**
     * Get the id of the general instance for this instance.<br/>
     * <b>Attention this method is actually executing a Query against the eFaps Database the first time this method or
     * {@link #getExchangeSystemId()} or {@link #getExchangeId()}is called!</b>
     *
     * @return 0 if no general instance exits
     * @throws EFapsException on error
     */
    public long getGeneralId()
        throws EFapsException
    {
        check4Generalised();
        return this.generalId;
    }

    /**
     * Setter method for instance variable {@link #generalId}.
     *
     * @param _generalId value for instance variable {@link #generalId}
     */
    protected void setGeneralId(final long _generalId)
    {
        this.generalId = _generalId;
    }

    /**
     * Getter method for the instance variable {@link #exchangeId}.<br/>
     * <b>Attention this method is actually executing a Query against the eFaps Database the first time this method or
     * {@link #getExchangeSystemId()} or {@link #getGeneralId()}is called!</b>
     *
     * @return value of instance variable {@link #exchangeId}
     * @throws EFapsException on error
     */
    public long getExchangeId()
        throws EFapsException
    {
        return this.getExchangeId(true);
    }

    /**
     * Getter method for the instance variable {@link #exchangeId}.<br/>
     * <b>Attention this method is actually executing a Query against
     * the eFaps Database the first time this method or
     * {@link #getExchangeSystemId()} or {@link #getGeneralId()}is called!</b>
     *
     * @param _request must the eFaps Database be requested
     * @return value of instance variable {@link #exchangeId}
     * @throws EFapsException on error
     */
    protected long getExchangeId(final boolean _request)
        throws EFapsException
    {
        if (_request) {
            check4Generalised();
        }
        return this.exchangeId;
    }

    /**
     * Setter method for instance variable {@link #exchangeId}.
     *
     * @param _exchangeId value for instance variable {@link #exchangeId}
     */
    protected void setExchangeId(final long _exchangeId)
    {
        this.exchangeId = _exchangeId;
    }

    /**
     * Getter method for the instance variable {@link #exchangeSystemId}.<br/>
     * <b>Attention this method is actually executing a Query against the
     * eFaps Database the first time this method or
     * {@link #getExchangeId()} or {@link #getGeneralId()}is called!</b>
     *
     * @return value of instance variable {@link #exchangeSystemId}
     * @throws EFapsException on error
     */
    public long getExchangeSystemId()
        throws EFapsException
    {
        return this.getExchangeSystemId(true);
    }

    /**
     * Getter method for the instance variable {@link #exchangeSystemId}.<br/>
     * <b>Attention this method is actually executing a Query against the
     * eFaps Database the first time this method or
     * {@link #getExchangeId()} or {@link #getGeneralId()}is called!</b>
     *
     * @param _request must the eFaps Database be requested
     * @return value of instance variable {@link #exchangeSystemId}
     * @throws EFapsException on error
     */
    public long getExchangeSystemId(final boolean _request)
        throws EFapsException
    {
        if (_request) {
            check4Generalised();
        }
        return this.exchangeSystemId;
    }

    /**
     * Setter method for instance variable {@link #exchangeSystemId}.
     *
     * @param _exchangeSystemId value for instance variable {@link #exchangeSystemId}
     */
    protected void setExchangeSystemId(final long _exchangeSystemId)
    {
        this.exchangeSystemId = _exchangeSystemId;
    }

    /**
     * The method returns a string representation of the instance object. It does not replace method {@link #getOid}!.
     *
     * @return string representation of this instance object
     */
    @Override
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
     * @param _type type of the instance
     * @param _id id of the instance
     * @return instance
     */
    public static Instance get(final Type _type,
                               final long _id)
    {
        String keyTmp = null;
        if ((_type != null) && (_id != 0)) {
            keyTmp = _type.getId() + "." + _id;
        }
        return Instance.get(_type, _id, keyTmp);
    }

    /**
     *
     * @param _type type of the instance
     * @param _id id of the instance
     * @param _key key of the instance
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
     * @param _type type of the instance
     * @param _id id of the instance as string
     * @return instance
     */
    public static Instance get(final Type _type,
                               final String _id)
    {
        return Instance.get(_type, _id, null);
    }

    /**
     *
     * @param _type type of the instance
     * @param _id id of the instance
     * @param _key key of the instance
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
        return Instance.get(_type, idTmp, _key);
    }

    /**
     *
     * @param _type type of the instance
     * @param _id id of the instance
     * @return instance
     */
    public static Instance get(final String _type,
                               final String _id)
    {
        return Instance.get(_type, _id, null);
    }

    /**
     *
     * @param _type type of the instance
     * @param _id id of the instance
     * @param _key key of the instance
     * @return instance
     */
    public static Instance get(final String _type,
                               final String _id,
                               final String _key)
    {
        Type typeTmp = null;
        if ((_type != null) && (_type.length() > 0)) {
            try {
                typeTmp = Type.get(_type);
            } catch (final CacheReloadException e) {
                Instance.LOG.error("Instance get error with Type: '{}', id: '{}' , key: '{}'",  _type, _id, _key);
            }
        }
        return Instance.get(typeTmp, _id, _key);
    }

    /**
     *
     * @param _oid eFaps object id of the instance
     * @return instance
     */
    public static Instance get(final String _oid)
    {
        Type typeTmp = null;
        final long idTmp;
        if (_oid != null) {
            final int index = _oid.indexOf(".");
            if (index >= 0) {
                try {
                    typeTmp = Type.get(Long.parseLong(_oid.substring(0, index)));
                } catch (final NumberFormatException e) {
                    Instance.LOG.error("Instance get error with OID: '{}'", _oid);
                } catch (final CacheReloadException e) {
                    Instance.LOG.error("Instance get error with OID: '{}'", _oid);
                }
                idTmp = Long.parseLong(_oid.substring(index + 1));
            } else {
                typeTmp = null;
                idTmp = 0;
            }
        } else {
            typeTmp = null;
            idTmp = 0;
        }
        return Instance.get(typeTmp, idTmp);
    }
}
