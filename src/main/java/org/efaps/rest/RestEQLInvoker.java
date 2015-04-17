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

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.efaps.db.Insert;
import org.efaps.eql.InvokerUtil;
import org.efaps.eql.JSONData;
import org.efaps.eql.stmt.IDeleteStmt;
import org.efaps.eql.stmt.IEQLStmt;
import org.efaps.eql.stmt.IExecStmt;
import org.efaps.eql.stmt.IInsertStmt;
import org.efaps.eql.stmt.IPrintStmt;
import org.efaps.eql.stmt.IUpdateStmt;
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
    @Path("print")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String print(@QueryParam("origin") final String _origin,
                        @QueryParam("stmt") final String _stmt)
    {
        String ret = null;
        // only permit queries on this url
        try {
            final IEQLStmt stmt = InvokerUtil.getInvoker().invoke(_stmt);
            if (stmt instanceof IPrintStmt) {
                registerEQLStmt(_origin, _stmt);
                final DataList datalist = JSONData.getDataList((IPrintStmt) stmt);
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

    /**
     * @return not implemented.
     */
    @Path("update")
    @GET
    public String update(@QueryParam("origin") final String _origin,
                         @QueryParam("stmt") final String _stmt)
    {
        final String ret = null;
        // only permit queries on this url
        try {
            final IEQLStmt stmt = InvokerUtil.getInvoker().invoke(_stmt);
            if (stmt instanceof IUpdateStmt) {
                registerEQLStmt(_origin, _stmt);
                ((IUpdateStmt) stmt).execute();
            }
            LOG.debug("JSON: '{}'", ret);
        } catch (final JsonProcessingException | EFapsException e) {
            LOG.error("Error processing data.", e);
        } catch (final Exception e) {
            LOG.error("Error processing data.", e);
        }
        return ret;
    }

    /**
     * @return not implemented.
     */
    @Path("insert")
    @GET
    public String insert(@QueryParam("origin") final String _origin,
                         @QueryParam("stmt") final String _stmt)
    {
        final String ret = null;
        // only permit queries on this url
        try {
            final IEQLStmt stmt = InvokerUtil.getInvoker().invoke(_stmt);
            if (stmt instanceof IInsertStmt) {
                registerEQLStmt(_origin, _stmt);
                ((IInsertStmt) stmt).execute();
            }
            LOG.debug("JSON: '{}'", ret);
        } catch (final JsonProcessingException | EFapsException e) {
            LOG.error("Error processing data.", e);
        } catch (final Exception e) {
            LOG.error("Error processing data.", e);
        }
        return ret;
    }

    /**
     * @return not implemented.
     */
    @Path("delete")
    @GET
    public String delete(@QueryParam("origin") final String _origin,
                         @QueryParam("stmt") final String _stmt)
    {
        final String ret = null;
        // only permit queries on this url
        try {
            final IEQLStmt stmt = InvokerUtil.getInvoker().invoke(_stmt);
            if (stmt instanceof IDeleteStmt) {
                registerEQLStmt(_origin, _stmt);
                ((IDeleteStmt) stmt).execute();
            }
            LOG.debug("JSON: '{}'", ret);
        } catch (final JsonProcessingException | EFapsException e) {
            LOG.error("Error processing data.", e);
        } catch (final Exception e) {
            LOG.error("Error processing data.", e);
        }
        return ret;
    }

    /**
     * @return not implemented.
     */
    @Path("execute")
    @GET
    public String execute(@QueryParam("origin") final String _origin,
                         @QueryParam("stmt") final String _stmt)
    {
        final String ret = null;
        // only permit queries on this url
        try {
            final IEQLStmt stmt = InvokerUtil.getInvoker().invoke(_stmt);
            if (stmt instanceof IExecStmt) {
                registerEQLStmt(_origin, _stmt);
               //TODO
            }
            LOG.debug("JSON: '{}'", ret);
        } catch (final JsonProcessingException | EFapsException e) {
            LOG.error("Error processing data.", e);
        } catch (final Exception e) {
            LOG.error("Error processing data.", e);
        }
        return ret;
    }

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
