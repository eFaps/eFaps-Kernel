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


package org.efaps.bpm.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.TaskQueryService;


/**
 * Overridden to have a query for ready tasks.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TaskAdminstration
{

    private final InternalTaskService taskService;

    /**
     * @param _taskService
     */
    public TaskAdminstration(final InternalTaskService _taskService)
    {
        this.taskService =_taskService;
    }

    /**
     * @return the task in status ready
     */
    public List<TaskSummary> getReadyTasks()
    {
        final Map<String, List<?>> params = new HashMap<String, List<?>>();
        params.put(TaskQueryService.STATUS_LIST, Arrays.asList(Status.Ready));
        return this.taskService.getTasksByVariousFields(params, true);
    }

    /**
     * @return the task in status Reserved
     */
    public List<TaskSummary> getReservedTasks()
    {
        final Map<String, List<?>> params = new HashMap<String, List<?>>();
        params.put(TaskQueryService.STATUS_LIST, Arrays.asList(Status.Reserved));
        return this.taskService.getTasksByVariousFields(params, true);
    }

    /**
     * @return the task in status Error
     */
    public List<TaskSummary> getErrorTasks()
    {
        final Map<String, List<?>> params = new HashMap<String, List<?>>();
        params.put(TaskQueryService.STATUS_LIST, Arrays.asList(Status.Error));
        return this.taskService.getTasksByVariousFields(params, true);
    }


    /**
     * @return
     */
    public List<TaskSummary> getActiveTasks()
    {
        return this.taskService.getActiveTasks();
    }

    /**
     * @return
     */
    public List<TaskSummary> getCompletedTasks()
    {
        return this.taskService.getCompletedTasks();
    }

}
