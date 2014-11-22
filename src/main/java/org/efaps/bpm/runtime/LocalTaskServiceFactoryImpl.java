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

package org.efaps.bpm.runtime;

import javax.persistence.EntityManagerFactory;

import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.audit.JPATaskLifeCycleEventListener;
import org.jbpm.services.task.lifecycle.listeners.BAMTaskEventListener;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.task.TaskService;
import org.kie.internal.runtime.manager.TaskServiceFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LocalTaskServiceFactoryImpl
    implements TaskServiceFactory
{

    /**
     * RuntimeEnvironment.
     */
    private final RuntimeEnvironment runtimeEnvironment;

    /**
     * @param _runtimeEnvironment RuntimeEnvironment to be used
     */
    public LocalTaskServiceFactoryImpl(final RuntimeEnvironment _runtimeEnvironment)
    {
        this.runtimeEnvironment = _runtimeEnvironment;
    }

    @Override
    public TaskService newTaskService()
    {
        final EntityManagerFactory emf = ((SimpleRuntimeEnvironment) this.runtimeEnvironment).getEmf();
        TaskService ret = null;
        if (emf != null) {
            ret = HumanTaskServiceFactory.newTaskServiceConfigurator()
                        .environment(this.runtimeEnvironment.getEnvironment())
                        .entityManagerFactory(emf)
                        .listener(new JPATaskLifeCycleEventListener(true))
                        .listener(new BAMTaskEventListener(true))
                        .userGroupCallback(this.runtimeEnvironment.getUserGroupCallback())
                        .getTaskService();
        }
        return ret;
    }

    @Override
    public void close()
    {
        // no action required
    }
}
