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


package org.efaps.rest;

import java.util.UUID;

import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;


/**
 * Base class for all Rest implementations inside eFaps.
 *
 * @author The eFaps Team
 *
 */
public abstract class AbstractRest
{
    /**
     * Logging instance used to give logging information of this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractRest.class);

    /**
     * Check if the logged in users has access to rest.
     * User must be assigned to the Role "Admin_Rest".
     *
     * @return true if user is assigned to Roles "Admin_Rest", else false
     * @throws EFapsException on error
     */
    protected boolean hasAccess()
        throws EFapsException
    {
        //Admin_REST
        return Context.getThreadContext().getPerson().isAssigned(Role.get(
                        UUID.fromString("2d142645-140d-46ad-af67-835161a8d732")));
    }

    /**
     * Gets the JSON reply.
     *
     * @param _jsonObject the _json object
     * @return the JSON reply
     * @throws JsonProcessingException the json processing exception
     */
    protected String getJSONReply(final Object _jsonObject)
    {
        String ret = "";
        final ObjectMapper mapper = new ObjectMapper();
        if (LOG.isDebugEnabled()) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(new JodaModule());
        try {
            ret =  mapper.writeValueAsString(_jsonObject);
        } catch (final JsonProcessingException e) {
            LOG.error("Catched JsonProcessingException", e);
        }
        return ret;
    }
}
