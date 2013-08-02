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

import java.util.HashMap;
import java.util.List;

import org.jbpm.task.Status;
import org.jbpm.task.admin.TasksAdminImpl;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.persistence.TaskPersistenceManager;


/**
 * Overridden to have a query for ready tasks.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TaskAdminstration
    extends TasksAdminImpl
{

    /**
     * @param _tpm taskPersitencemanager
     */
    public TaskAdminstration(final TaskPersistenceManager _tpm)
    {
        super(_tpm);
    }

    /**
     * @return the task in status ready
     */
    @SuppressWarnings("unchecked")
    public List<TaskSummary> getReadyTasks()
    {
        final HashMap<String, Object> params = TaskPersistenceManager.addParametersToMap(
                        "status", Status.Ready,
                        "language", "en-UK");
        return (List<TaskSummary>) this.tpm.queryWithParametersInTransaction("TasksByStatus", params);
    }

}
