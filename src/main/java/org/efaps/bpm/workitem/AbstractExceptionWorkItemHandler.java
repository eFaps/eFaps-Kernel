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

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public abstract class AbstractExceptionWorkItemHandler
    implements WorkItemHandler
{

    /**
     * The original Task Handler wrapped for Exception Handling.
     */
    private final WorkItemHandler originalTaskHandler;

    /**
     * @param _originalTaskHandler  task handler that will be wrapped in
     */
    public AbstractExceptionWorkItemHandler(final WorkItemHandler _originalTaskHandler)
    {
        this.originalTaskHandler = _originalTaskHandler;
    }

    /**
     * The given work item should be executed.
     *
     * @param _workItem the work item that should be executed
     * @param _manager the manager that requested the work item to be executed
     */
    @Override
    public void executeWorkItem(final WorkItem _workItem,
                                final WorkItemManager _manager)
    {
        try {
            this.originalTaskHandler.executeWorkItem(_workItem, _manager);
        // CHECKSTYLE:OFF
        } catch (final Throwable e) {
        // CHECKSTYLE:ON
            handleExecuteException(e, _workItem, _manager);
        }
    }

    /**
     * The given work item should be aborted.
     *
     * @param _workItem the work item that should be aborted
     * @param _manager the manager that requested the work item to be aborted
     */
    @Override
    public void abortWorkItem(final WorkItem _workItem,
                              final WorkItemManager _manager)
    {
        try {
            this.originalTaskHandler.abortWorkItem(_workItem, _manager);
        // CHECKSTYLE:OFF
        } catch (final Throwable e) {
        // CHECKSTYLE:ON
            handleAbortException(e, _workItem, _manager);
        }
    }

    /**
     * @return the original WorkItem
     */
    public WorkItemHandler getOriginalTaskHandler()
    {
        return this.originalTaskHandler;
    }

    /**
     * @param _cause        cause of the exception
     * @param _workItem     workitem the exception belongs to
     * @param _manager      manager for the workItem
     */
    public abstract void handleExecuteException(final Throwable _cause,
                                                final WorkItem _workItem,
                                                final WorkItemManager _manager);

    /**
     * @param _cause        cause of the exception
     * @param _workItem     workitem the exception belongs to
     * @param _manager      manager for the workItem
     */
    public abstract void handleAbortException(final Throwable _cause,
                                              final WorkItem _workItem,
                                              final WorkItemManager _manager);
}
