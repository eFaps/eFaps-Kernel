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

import java.util.List;

import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.ParametrizedQuery;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.query.TaskQueryBuilder;


/**
 * Overridden to have a query for ready tasks.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TaskAdminstration
{

    /**
     * TaskService used to execute the commands.
     */
    private final InternalTaskService taskService;

    /**
     * @param _taskService  TaskService used to execute the commands
     */
    public TaskAdminstration(final InternalTaskService _taskService)
    {
        this.taskService = _taskService;
    }

    /**
     * @return the task in status ready
     */
    public List<TaskSummary> getReadyTasks()
    {
        final TaskQueryBuilder queryBldr = this.taskService.taskQuery("Administrator");
        queryBldr.status(Status.Ready);
        final ParametrizedQuery<TaskSummary> paraQuery = queryBldr.buildQuery();
        return paraQuery.getResultList() ;
    }

    /**
     * @return the task in status Reserved
     */
    public List<TaskSummary> getReservedTasks()
    {
        final TaskQueryBuilder queryBldr = this.taskService.taskQuery("Administrator");
        queryBldr.status(Status.Reserved);
        final ParametrizedQuery<TaskSummary> paraQuery = queryBldr.buildQuery();
        return paraQuery.getResultList() ;
    }

    /**
     * @return the task in status Error
     */
    public List<TaskSummary> getErrorTasks()
    {
        final TaskQueryBuilder queryBldr = this.taskService.taskQuery("Administrator");
        queryBldr.status(Status.Error);
        final ParametrizedQuery<TaskSummary> paraQuery = queryBldr.buildQuery();
        return paraQuery.getResultList() ;
    }

    /**
     * @return the task in status Error
     */
    public List<TaskSummary> getExitedTasks()
    {
        final TaskQueryBuilder queryBldr = this.taskService.taskQuery("Administrator");
        queryBldr.status(Status.Exited);
        final ParametrizedQuery<TaskSummary> paraQuery = queryBldr.buildQuery();
        return paraQuery.getResultList() ;
    }

    /**
     * @return list of active Tasks
     */
    public List<TaskSummary> getActiveTasks()
    {
        final TaskQueryBuilder queryBldr = this.taskService.taskQuery("Administrator");
        queryBldr.status(Status.InProgress);
        final ParametrizedQuery<TaskSummary> paraQuery = queryBldr.buildQuery();
        return paraQuery.getResultList() ;
    }

    /**
     * @return list of completed Tasks
     */
    public List<TaskSummary> getCompletedTasks()
    {
        final TaskQueryBuilder queryBldr = this.taskService.taskQuery("Administrator");
        queryBldr.status(Status.Completed);
        final ParametrizedQuery<TaskSummary> paraQuery = queryBldr.buildQuery();
        return paraQuery.getResultList() ;
    }
}
