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

package org.efaps.ci;

import org.efaps.admin.common.MsgPhrase;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
//CHECKSTYLE:OFF
public abstract class CIMsgPhrase
    extends CIObject
{
//CHECKSTYLE:ON
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CIMsgPhrase.class);

    /**
     * Instantiates a new CI msg phrase.
     *
     * @param _uuid the _uuid
     */
    protected CIMsgPhrase(final String _uuid)
    {
        super(_uuid);
    }

    /**
     * Get the type this Configuration item represents.
     *
     * @return Type
     */
    public MsgPhrase getMsgPhrase()
    {
        MsgPhrase ret = null;
        try {
            ret =  MsgPhrase.get(this.uuid);
        } catch (final EFapsException e) {
            LOG.error("Error on retrieving MsgPhrase for CIMsgPhrase with uuid: {}", this.uuid);
        }
        return ret;
    }
}
