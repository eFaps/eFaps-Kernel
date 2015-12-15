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
 */

package org.efaps.rest;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.efaps.db.Insert;
import org.efaps.eql.EQLInvoker;
import org.efaps.eql.InvokerUtil;
import org.efaps.eql.JSONCI;
import org.efaps.eql.JSONData;
import org.efaps.eql.stmt.ICIPrintStmt;
import org.efaps.eql.stmt.IDeleteStmt;
import org.efaps.eql.stmt.IEQLStmt;
import org.efaps.eql.stmt.IExecStmt;
import org.efaps.eql.stmt.IInsertStmt;
import org.efaps.eql.stmt.IPrintStmt;
import org.efaps.eql.stmt.IUpdateStmt;
import org.efaps.json.ci.AbstractCI;
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
 */
@Path("/eql")
public class RestEQLInvoker
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RestEQLInvoker.class);

    /**
     * Prints the.
     *
     * @param _origin the origin
     * @param _stmt the stmt
     * @return not implemented.
     */
    @Path("print")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response print(@QueryParam("origin") final String _origin,
                          @QueryParam("stmt") final String _stmt)
    {
        Response ret = null;
        // only permit print on this url
        try {
            final EQLInvoker invoker = InvokerUtil.getInvoker();
            final IEQLStmt stmt = invoker.invoke(_stmt);
            if (invoker.getSyntaxErrors().isEmpty() && stmt instanceof IPrintStmt) {
                registerEQLStmt(_origin, _stmt);
                final DataList datalist = JSONData.getDataList((IPrintStmt) stmt);
                final ObjectMapper mapper = new ObjectMapper();
                if (LOG.isDebugEnabled()) {
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                }
                mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                mapper.registerModule(new JodaModule());
                ret = Response.ok().type(MediaType.APPLICATION_JSON).entity(mapper.writeValueAsString(datalist))
                                .build();
            } else if (invoker.getSyntaxErrors().isEmpty() && stmt instanceof ICIPrintStmt) {
                registerEQLStmt(_origin, _stmt);
                final AbstractCI<?> ci = JSONCI.getCI((ICIPrintStmt) stmt);
                final ObjectMapper mapper = new ObjectMapper();
                if (LOG.isDebugEnabled()) {
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                }
                mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                mapper.registerModule(new JodaModule());
                ret = Response.ok().type(MediaType.APPLICATION_JSON).entity(mapper.writeValueAsString(ci))
                                .build();
            } else {
                final StringBuilder error = new StringBuilder();
                for (final String syntaxError : invoker.getSyntaxErrors()) {
                    LOG.warn(syntaxError);
                    error.append(syntaxError).append("\n");
                }
                ret = Response.serverError().entity(error.toString()).build();
            }
        } catch (final JsonProcessingException | EFapsException e) {
            LOG.error("Error processing data.", e);
        //CHECKSTYLE:OFF
        } catch (final Exception e) {
        //CHECKSTYLE:ON
            LOG.error("Error processing data.", e);
        }
        return ret;
    }

    /**
     * Update.
     *
     * @param _origin the origin
     * @param _stmt the stmt
     * @return not implemented.
     */
    @Path("update")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String update(@QueryParam("origin") final String _origin,
                         @QueryParam("stmt") final String _stmt)
    {
        String ret = null;
        // only permit updates on this url
        try {
            final EQLInvoker invoker = InvokerUtil.getInvoker();
            final IEQLStmt stmt = invoker.invoke(_stmt);
            if (invoker.getSyntaxErrors().isEmpty() && stmt instanceof IUpdateStmt) {
                registerEQLStmt(_origin, _stmt);
                ((IUpdateStmt) stmt).execute();
            } else {
                ret = "";
                for (final String syntaxError : invoker.getSyntaxErrors()) {
                    LOG.warn(syntaxError);
                    ret = ret + syntaxError + "\n";
                }
            }
        } catch (final JsonProcessingException | EFapsException e) {
            LOG.error("Error processing data.", e);
        //CHECKSTYLE:OFF
        } catch (final Exception e) {
        //CHECKSTYLE:ON
            LOG.error("Error processing data.", e);
        }
        return ret;
    }

    /**
     * Insert.
     *
     * @param _origin the origin
     * @param _stmt the stmt
     * @return not implemented.
     */
    @Path("insert")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String insert(@QueryParam("origin") final String _origin,
                         @QueryParam("stmt") final String _stmt)
    {
        String ret = null;
        // only permit insert on this url
        try {
            final EQLInvoker invoker = InvokerUtil.getInvoker();
            final IEQLStmt stmt = invoker.invoke(_stmt);
            if (invoker.getSyntaxErrors().isEmpty() && stmt instanceof IInsertStmt) {
                registerEQLStmt(_origin, _stmt);
                ((IInsertStmt) stmt).execute();
                ret = ((IInsertStmt) stmt).getInstance();
            } else {
                ret = "";
                for (final String syntaxError : invoker.getSyntaxErrors()) {
                    LOG.warn(syntaxError);
                    ret = ret + syntaxError + "\n";
                }
            }
        } catch (final JsonProcessingException | EFapsException e) {
            LOG.error("Error processing data.", e);
        //CHECKSTYLE:OFF
        } catch (final Exception e) {
        //CHECKSTYLE:ON
            LOG.error("Error processing data.", e);
        }
        return ret;
    }

    /**
     * Delete.
     *
     * @param _origin the origin
     * @param _stmt the stmt
     * @return not implemented.
     */
    @Path("delete")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String delete(@QueryParam("origin") final String _origin,
                         @QueryParam("stmt") final String _stmt)
    {
        String ret = null;
        // only permit delete on this url
        try {
            final EQLInvoker invoker = InvokerUtil.getInvoker();
            final IEQLStmt stmt = invoker.invoke(_stmt);
            if (invoker.getSyntaxErrors().isEmpty() && stmt instanceof IDeleteStmt) {
                registerEQLStmt(_origin, _stmt);
                ((IDeleteStmt) stmt).execute();
            } else {
                ret = "";
                for (final String syntaxError : invoker.getSyntaxErrors()) {
                    LOG.warn(syntaxError);
                    ret = ret + syntaxError + "\n";
                }
            }
        } catch (final JsonProcessingException | EFapsException e) {
            LOG.error("Error processing data.", e);
        //CHECKSTYLE:OFF
        } catch (final Exception e) {
        //CHECKSTYLE:ON
            LOG.error("Error processing data.", e);
        }
        return ret;
    }

    /**
     * Execute.
     *
     * @param _origin the origin
     * @param _stmt the stmt
     * @return not implemented.
     */
    @Path("execute")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String execute(@QueryParam("origin") final String _origin,
                          @QueryParam("stmt") final String _stmt)
    {
        String ret = null;
        // only permit execute on this url
        try {
            final EQLInvoker invoker = InvokerUtil.getInvoker();
            final IEQLStmt stmt = invoker.invoke(_stmt);
            if (stmt instanceof IExecStmt) {
                registerEQLStmt(_origin, _stmt);
                // TODO
            } else {
                ret = "";
            }
            for (final String syntaxError : invoker.getSyntaxErrors()) {
                LOG.warn(syntaxError);
                ret = ret + syntaxError + "\n";
            }
        } catch (final JsonProcessingException | EFapsException e) {
            LOG.error("Error processing data.", e);

        } catch (final Exception e) {
            LOG.error("Error processing data.", e);
        }
        return ret;
    }

    /**
     * Register eql stmt.
     *
     * @param _origin the origin
     * @param _stmt the stmt
     * @throws EFapsException on error
     */
    protected void registerEQLStmt(final String _origin,
                                   final String _stmt)
        throws EFapsException
    {
        // Common_HistoryEQL
        final Insert insert = new Insert(UUID.fromString("c96c63b5-2d4c-4bf9-9627-f335fd9c7a84"));
        insert.add("Origin", "REST: " + (_origin == null ? "" : _origin));
        insert.add("EQLStatement", _stmt);
        insert.execute();
    }
}
