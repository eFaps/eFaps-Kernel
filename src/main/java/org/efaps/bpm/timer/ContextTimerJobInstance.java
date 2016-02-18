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

package org.efaps.bpm.timer;

import org.drools.core.time.InternalSchedulerService;
import org.drools.core.time.Job;
import org.drools.core.time.JobContext;
import org.drools.core.time.JobHandle;
import org.drools.core.time.Trigger;
import org.drools.persistence.jpa.JpaTimerJobInstance;
import org.efaps.db.Context;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class ContextTimerJobInstance
    extends JpaTimerJobInstance
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * @param _job          job to be used for timer
     * @param _ctx          jobContext
     * @param _trigger      trigger to use
     * @param _handle       handle to the job
     * @param _scheduler    service
     */
    public ContextTimerJobInstance(final Job _job,
                                   final JobContext _ctx,
                                   final Trigger _trigger,
                                   final JobHandle _handle,
                                   final InternalSchedulerService _scheduler)
    {
        super(_job, _ctx, _trigger, _handle, _scheduler);
    }

    @Override
    public Void call()
        throws Exception
    {
        Context.begin("Administrator", Context.Inheritance.Local);
        super.call();
        Context.commit();
        return null;
    }
}
