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

import org.drools.core.command.CommandService;
import org.drools.core.time.InternalSchedulerService;
import org.drools.core.time.Job;
import org.drools.core.time.JobContext;
import org.drools.core.time.JobHandle;
import org.drools.core.time.SelfRemovalJob;
import org.drools.core.time.SelfRemovalJobContext;
import org.drools.core.time.Trigger;
import org.drools.core.time.impl.TimerJobFactoryManager;
import org.drools.core.time.impl.TimerJobInstance;



/**
 * A Timer factory that opens a Context.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ContextTimerJobFactoryManager
    implements TimerJobFactoryManager
{
    /**
     * The CommandSerice that will be executed on time event.
     */
    private CommandService commandService;

    /**
     * Timer instances.
     */
    private final Map<Long, TimerJobInstance> timerInstances;

    /**
     * Default Constructor.
     */
    public ContextTimerJobFactoryManager()
    {
        this.timerInstances = new ConcurrentHashMap<Long, TimerJobInstance>();
    }

    /**
     * Get the Command Service.
     * @return value of instance variable {@link #commandService}
     * @see org.drools.time.impl.TimerJobFactoryManager#getCommandService()
     */
    public CommandService getCommandService()
    {
        return this.commandService;
    }

    /**
     * Set the Command Service.
     * @param _commandService   Command Service to set
     * @see org.drools.time.impl.TimerJobFactoryManager#setCommandService(org.drools.command.CommandService)
     */
    public void setCommandService(final CommandService _commandService)
    {
        this.commandService = _commandService;
    }

    /**
     * @param _job          job to be used for timer
     * @param _ctx          jobContext
     * @param _trigger      trigger to use
     * @param _handle       handle to the job
     * @param _scheduler    service
     * @return new ContextTimerJobInstance
     */
    public TimerJobInstance createTimerJobInstance(final Job _job,
                                                   final JobContext _ctx,
                                                   final Trigger _trigger,
                                                   final JobHandle _handle,
                                                   final InternalSchedulerService _scheduler)
    {
        _ctx.setJobHandle(_handle);
        final ContextTimerJobInstance jobInstance = new ContextTimerJobInstance(new SelfRemovalJob(_job),
                        new SelfRemovalJobContext(_ctx, this.timerInstances),
                        _trigger,
                        _handle,
                        _scheduler);
        return jobInstance;
    }

    /**
     * @param _instance  instance of a timer job to add
     * @see org.drools.time.impl.TimerJobFactoryManager#addTimerJobInstance(org.drools.time.impl.TimerJobInstance)
     */
    public void addTimerJobInstance(final TimerJobInstance _instance)
    {
        this.timerInstances.put(_instance.getJobHandle().getId(),
                        _instance);
    }

    /**
     * @param _instance  instance of a timer job to remove
     * @see org.drools.time.impl.TimerJobFactoryManager#removeTimerJobInstance(org.drools.time.impl.TimerJobInstance)
     */
    public void removeTimerJobInstance(final TimerJobInstance _instance)
    {
        this.timerInstances.remove(_instance.getJobHandle().getId());
    }

    /**
     * @return a collection of TimerJobInstance
     * @see org.drools.time.impl.TimerJobFactoryManager#getTimerJobInstances()
     */
    public Collection<TimerJobInstance> getTimerJobInstances()
    {
        return this.timerInstances.values();
    }
}
