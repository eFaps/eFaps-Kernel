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

package org.efaps.bpm;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.SystemEventListenerFactory;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.local.LocalTaskService;
import org.jbpm.task.utils.OnErrorAction;

/**
 * Responsible for executing the TaskServie implementation for eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class UserTaskService
{

    /**
     * TaskServie instance.
     */
    private static TaskService INSTANCE;

    /**
     * LocalTaskService instance.
     */
    private static LocalTaskService LOCAL;

    /**
     * Helper Class private constructor.
     */
    private UserTaskService()
    {
    }


    /**
     * @param _environment environment the service is wanted for
     * @return TaskService
     */
    public static TaskService getService(final Environment _environment)
    {
        if (UserTaskService.INSTANCE == null) {
            EntityManagerFactory emf = (EntityManagerFactory) _environment.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
            if (emf == null) {
                emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
            }
            final TaskService taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
            UserTaskService.INSTANCE = taskService;
        }
        return UserTaskService.INSTANCE;
    }

    /**
     * @param _ksession session the service is wanted for
     * @return taskservice
     */
    public static org.jbpm.task.TaskService getTaskService(final StatefulKnowledgeSession _ksession)
    {
        if (UserTaskService.LOCAL == null) {
            final TaskService taskService = UserTaskService.getService(_ksession.getEnvironment());
            UserTaskService.LOCAL = new LocalTaskService(taskService);
            final LocalHTWorkItemHandler humanTaskHandler = new LocalHTWorkItemHandler(UserTaskService.LOCAL, _ksession,
                            OnErrorAction.LOG, EFapsClassLoader.getInstance());
            _ksession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);
            BPM.registerWorkItemHandler("Human Task", humanTaskHandler);
        }
        return UserTaskService.LOCAL;
    }

    /**
     * Dispose the Service.
     */
    protected static void dispose()
    {
        UserTaskService.INSTANCE = null;
        if (UserTaskService.LOCAL != null) {
            UserTaskService.LOCAL.dispose();
            UserTaskService.LOCAL = null;
        }
    }
}
