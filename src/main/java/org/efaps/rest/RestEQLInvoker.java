/*
 * Copyright 2003 - 2015 The eFaps Team
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

import org.efaps.eql.IEQLStmt;
import org.efaps.eql.ISelectStmt;
import org.efaps.eql.InvokerUtil;
import org.efaps.eql.JSONData;
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
public class RestEQLInvoker
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RestEQLInvoker.class);

    /**
     * @return not implemented.
     */
    @Path("query")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@FormParam("stmt") final String _stmt)
    {
        String ret = null;
        // only permit queries on this url
        try {
            final IEQLStmt stmt = InvokerUtil.getInvoker().invoke(_stmt);
            if (stmt instanceof ISelectStmt) {
                final DataList datalist = JSONData.getDataList((ISelectStmt) stmt);
                final ObjectMapper mapper = new ObjectMapper();
                if (LOG.isDebugEnabled()) {
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                }
                mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

                mapper.registerModule(new JodaModule());

                ret = mapper.writeValueAsString(datalist);
            }
            LOG.debug("JSON: '{}'", ret);
        } catch (final JsonProcessingException | EFapsException e) {
            LOG.error("Error processing data.", e);
        } catch (final Exception e) {
            LOG.error("Error processing data.", e);
        }
        return ret;
    }
}
