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


package org.efaps.bpm.listener;

import org.drools.audit.WorkingMemoryLogger;
import org.drools.audit.event.LogEvent;
import org.drools.event.KnowledgeRuntimeEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class WorkingMemoryLogListener
    extends WorkingMemoryLogger
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(WorkingMemoryLogListener.class);

    /**
     * @param _ksession session the logger will log
     */
    private WorkingMemoryLogListener(final KnowledgeRuntimeEventManager _ksession)
    {
        super(_ksession);
    }

    /**
     * log the event.
     * @param _logEvent evnt to log
     */
    @Override
    public void logEventCreated(final LogEvent _logEvent)
    {
        WorkingMemoryLogListener.LOG.debug("{}", _logEvent);
    }

    /**
     * @param _ksession sessio to attach to
     */
    public static void attach(final KnowledgeRuntimeEventManager _ksession)
    {
        new WorkingMemoryLogListener(_ksession);
    }
}
