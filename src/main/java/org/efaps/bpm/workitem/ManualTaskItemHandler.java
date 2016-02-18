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

import java.util.HashMap;
import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

/**
 * An Item Handler for Manual task that actual does nothing at all.
 *
 * @author The eFaps Team
 *
 */
public class ManualTaskItemHandler
    implements WorkItemHandler
{

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
        final Map<String, Object> results = new HashMap<String, Object>();
        _manager.completeWorkItem(_workItem.getId(), results);
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
        _manager.abortWorkItem(_workItem.getId());
    }
}
