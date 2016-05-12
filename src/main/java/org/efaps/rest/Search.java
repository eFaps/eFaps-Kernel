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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.efaps.json.index.SearchResult;
import org.efaps.json.reply.ErrorReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@Path("/search")
public class Search
    extends AbstractRest
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Search.class);

    /**
     * Search.
     *
     * @param _query the _query
     * @return the response
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    @SuppressWarnings("checkstyle:illegalcatch")
    public Response search(@QueryParam("query") final String _query)
    {
        Response ret;
        try {
            final SearchResult result = org.efaps.admin.index.Searcher.search(_query);
            ret = Response.ok().type(MediaType.APPLICATION_JSON).entity(getJSONReply(result)).build();
        } catch (final Exception e) {
            LOG.error("Error processing data.", e);
            final ErrorReply reply = new ErrorReply()
                            .setError(e.getClass().getName())
                            .setMessage(e.getMessage())
                            .setStacktrace(ExceptionUtils.getStackTrace(e));
            ret = Response.serverError().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
        }
        return ret;
    }
}
