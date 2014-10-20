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

package org.efaps.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.efaps.eql.JSONData;
import org.efaps.eql.Statement;
import org.efaps.json.data.DataList;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@Path("/eql")
public class EQLInvoker
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EQLInvoker.class);

    /**
     * @return not implemented.
     */
    @Path("query")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@FormParam("stmt") final String _stmt)
    {
        String ret = null;
        final Statement stmt = Statement.getStatement(_stmt);
        // only permit queries on this url
        if (stmt.isQuery()) {
            try {
                final DataList datalist = JSONData.getDataList(stmt);
                final ObjectMapper mapper = new ObjectMapper();
                if (LOG.isDebugEnabled()) {
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                }
                mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

                mapper.registerModule(new JodaModule());

                ret = mapper.writeValueAsString(datalist);
            } catch (final JsonProcessingException | EFapsException e) {
                LOG.error("Error processing data.", e);
            }
        }
        return ret;
    }
}
