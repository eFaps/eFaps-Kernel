/*
 * Copyright 2003 - 2018 The eFaps Team
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

import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestApplicationEventListener
    implements ApplicationEventListener
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RestApplicationEventListener.class);

    @Override
    public void onEvent(final ApplicationEvent _event)
    {
        switch (_event.getType()) {
            case INITIALIZATION_FINISHED:
                try {
                    if (Context.isThreadActive() && Context.getThreadContext().getRequestAttribute(
                                    EFapsResourceConfig.CONTEXTHINT) != null) {
                        Context.rollback();
                    }
                } catch (final EFapsException e) {
                    LOG.error("Catched", e);
                }
                break;
            default:
        }
    }

    @Override
    public RequestEventListener onRequest(final RequestEvent _requestEvent)
    {
        return null;
    }
}
