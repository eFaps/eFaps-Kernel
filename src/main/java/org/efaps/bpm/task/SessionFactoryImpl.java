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

import javax.persistence.EntityManagerFactory;

import org.jbpm.task.admin.TasksAdmin;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.persistence.TaskSessionFactoryImpl;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SessionFactoryImpl
    extends TaskSessionFactoryImpl
{

    /**
     * @param _taskService  TaskServcie
     * @param _emf          EntityManagerFactory
     */
    public SessionFactoryImpl(final TaskService _taskService,
                              final EntityManagerFactory _emf)
    {
        super(_taskService, _emf);
    }

    @Override
    public TasksAdmin createTaskAdmin()
    {
        return new TaskAdminstration(super.createTaskServiceSession().getTaskPersistenceManager());
    }

}
