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

import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessEventListener;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.process.ProcessVariableChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ProcessEventLstnr
    implements ProcessEventListener
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProcessEventLstnr.class);

    public void afterNodeLeft(final ProcessNodeLeftEvent event)
    {
        ProcessEventLstnr.LOG.debug("After node left: " + event.getNodeInstance().getNodeName());

    }

    public void afterNodeTriggered(final ProcessNodeTriggeredEvent event)
    {
        ProcessEventLstnr.LOG.debug("After node triggered: " + event.getNodeInstance().getNodeName());

    }

    public void afterProcessCompleted(final ProcessCompletedEvent event)
    {
        ProcessEventLstnr.LOG.debug("After process completed");

    }

    public void afterProcessStarted(final ProcessStartedEvent event)
    {
        ProcessEventLstnr.LOG.debug("After process started");

    }

    public void beforeNodeLeft(final ProcessNodeLeftEvent event)
    {
        ProcessEventLstnr.LOG.debug("Before node left: " + event.getNodeInstance().getNodeName());

    }

    public void beforeNodeTriggered(final ProcessNodeTriggeredEvent event)
    {
        ProcessEventLstnr.LOG.debug("Before node triggered: " + event.getNodeInstance().getNodeName());

    }

    public void beforeProcessCompleted(final ProcessCompletedEvent event)
    {
        ProcessEventLstnr.LOG.debug("Before process completed");

    }

    public void beforeProcessStarted(final ProcessStartedEvent event)
    {
        ProcessEventLstnr.LOG.debug("Before process started");

    }

    public void afterVariableChanged(final ProcessVariableChangedEvent event)
    {
        ProcessEventLstnr.LOG.debug("After Variable Changed");

    }

    public void beforeVariableChanged(final ProcessVariableChangedEvent event)
    {
        ProcessEventLstnr.LOG.debug("Before Variable Changed");

    }
}
