/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.cluster;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.jgroups.util.ByteArray;
import org.jgroups.util.SizeStreamable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("deceedf1-9dc9-4f8c-9178-0c276a926969")
@EFapsApplication("eFaps-Kernel")
public class StreamableWrapper
    implements SizeStreamable
{
    private static final Logger LOG = LoggerFactory.getLogger(StreamableWrapper.class);

    public StreamableWrapper()
    {
    }

    public StreamableWrapper(final Serializable obj)
    {
        LOG.warn("ClusterCommunication is not activated for kernel usage");
    }

    public <T> T getObject()
    {
        return null;
    }

    public synchronized StreamableWrapper setObject(final Serializable obj)
    {
        LOG.warn("ClusterCommunication is not activated for kernel usage");
        return this;
    }

    public synchronized ByteArray getSerialized()
    {
        LOG.warn("ClusterCommunication is not activated for kernel usage");
        return null;
    }

    public int getLength()
    {
        return getSerialized().getLength();
    }


    @Override
    public int serializedSize()
    {
        LOG.warn("ClusterCommunication is not activated for kernel usage");
        return 0;
    }

    @Override
    public void writeTo(DataOutput out)
        throws IOException
    {
        LOG.warn("ClusterCommunication is not activated for kernel usage");
    }

    @Override
    public void readFrom(DataInput in)
        throws IOException, ClassNotFoundException
    {
        LOG.warn("ClusterCommunication is not activated for kernel usage");
    }
}
