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

package org.efaps.bpm.timer;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.command.CommandService;
import org.drools.time.InternalSchedulerService;
import org.drools.time.Job;
import org.drools.time.JobContext;
import org.drools.time.JobHandle;
import org.drools.time.SelfRemovalJob;
import org.drools.time.SelfRemovalJobContext;
import org.drools.time.Trigger;
import org.drools.time.impl.TimerJobFactoryManager;
import org.drools.time.impl.TimerJobInstance;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ContextTimerJobFactoryManager
    implements
    TimerJobFactoryManager
{

    private CommandService commandService;

    private final Map<Long, TimerJobInstance> timerInstances;

    public void setCommandService(final CommandService commandService)
    {
        this.commandService = commandService;
    }

    public ContextTimerJobFactoryManager()
    {
        this.timerInstances = new ConcurrentHashMap<Long, TimerJobInstance>();
    }

    public TimerJobInstance createTimerJobInstance(final Job job,
                                                   final JobContext ctx,
                                                   final Trigger trigger,
                                                   final JobHandle handle,
                                                   final InternalSchedulerService scheduler)
    {
        ctx.setJobHandle(handle);
        final ContextTimerJobInstance jobInstance = new ContextTimerJobInstance(new SelfRemovalJob(job),
                        new SelfRemovalJobContext(ctx, this.timerInstances),
                        trigger,
                        handle,
                        scheduler);

        return jobInstance;
    }

    public void addTimerJobInstance(final TimerJobInstance instance)
    {

        this.timerInstances.put(instance.getJobHandle().getId(),
                        instance);
    }

    public void removeTimerJobInstance(final TimerJobInstance instance)
    {

        this.timerInstances.remove(instance.getJobHandle().getId());
    }

    public Collection<TimerJobInstance> getTimerJobInstances()
    {
        return this.timerInstances.values();
    }

    public CommandService getCommandService()
    {
        return this.commandService;

    }
}
