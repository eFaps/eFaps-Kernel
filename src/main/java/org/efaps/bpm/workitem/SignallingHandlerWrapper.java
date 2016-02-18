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

package org.efaps.bpm.workitem;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class SignallingHandlerWrapper
    extends AbstractExceptionWorkItemHandler
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SignallingHandlerWrapper.class);

    /**
     * Event type key.
     */
    private String eventType;

    /**
     * Key to the exception returned to the Process.
     */
    private String workItemExceptionParameterName = "org.efaps.bpm.workitem.exception";

    /**
     * @param _originalTaskHandler  task handler that will be wrapped in
     */
    public SignallingHandlerWrapper(final WorkItemHandler _originalTaskHandler)
    {
        super(_originalTaskHandler);
        this.eventType = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleExecuteException(final Throwable _cause,
                                       final WorkItem _workItem,
                                       final WorkItemManager _manager)
    {
        _workItem.getParameters().put(this.workItemExceptionParameterName, _cause);
        final String eventTypeTmp = getEventType(_cause, _workItem);
        SignallingHandlerWrapper.LOG.debug("Signaling event with eventType: {}", eventTypeTmp);
        ((org.drools.core.process.instance.WorkItemManager) _manager).signalEvent(eventTypeTmp, _workItem,
                        _workItem.getProcessInstanceId());
    }

    /**
     * @param _cause    cause the ventype is wanted for
     * @param _workItem workitem the couse belongs to
     * @return eventype
     */
    public String getEventType(final Throwable _cause,
                               final WorkItem _workItem)
    {
        // check if manually set
        if (this.eventType == null) {
            final Object signal = _workItem.getParameter(EsjpWorkItemHandler.PARAMETERNAME_ERRORSIGNAL);
            if (signal != null) {
                this.eventType = "Error-" + signal;
            }
            if (_cause instanceof WorkItemException) {
                this.eventType = this.eventType + "-" + _cause.getMessage();
            }
        }
        return this.eventType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAbortException(final Throwable _cause,
                                     final WorkItem _workItem,
                                     final WorkItemManager _manager)
    {
        _workItem.getParameters().put(this.workItemExceptionParameterName, _cause);
        final String eventTypeTmp = getEventType(_cause, _workItem);
        SignallingHandlerWrapper.LOG.debug("Signaling event with eventType: {}", eventTypeTmp);
        ((org.drools.core.process.instance.WorkItemManager) _manager).signalEvent(eventTypeTmp, _workItem,
                        _workItem.getProcessInstanceId());
    }

    /**
     * Getter method for the instance variable {@link #eventType}.
     *
     * @return value of instance variable {@link #eventType}
     */
    public String getEventType()
    {
        return this.eventType;
    }

    /**
     * Setter method for instance variable {@link #eventType}.
     *
     * @param _eventType value for instance variable {@link #eventType}
     */
    public void setEventType(final String _eventType)
    {
        this.eventType = _eventType;
    }

    /**
     * Getter method for the instance variable {@link #workItemExceptionParameterName}.
     *
     * @return value of instance variable {@link #workItemExceptionParameterName}
     */
    public String getWorkItemExceptionParameterName()
    {
        return this.workItemExceptionParameterName;
    }

    /**
     * Setter method for instance variable {@link #workItemExceptionParameterName}.
     *
     * @param _workItemExceptionParameterName value for instance variable {@link #workItemExceptionParameterName}
     */
    public void setWorkItemExceptionParameterName(final String _workItemExceptionParameterName)
    {
        this.workItemExceptionParameterName = _workItemExceptionParameterName;
    }
}
