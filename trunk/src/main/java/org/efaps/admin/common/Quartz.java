/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.admin.common;

import java.util.Properties;

import org.efaps.init.INamingBinds;
import org.efaps.util.EFapsException;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Quartz for eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Quartz
{
    /**
     * Contains the instance of this singelton.
     */
    private static Quartz QUARTZ;

    /**
     * Private Constructor.
     */
    private Quartz()
    {
    }

    /**
     * Method to initialize the Quartz.
     * @throws EFapsException on error
     */
    public static void initialize()
        throws EFapsException
    {
        Quartz.QUARTZ = new Quartz();
        try {
            final StdSchedulerFactory schedFact = new StdSchedulerFactory();
            final Properties props = new Properties();
            props.put("org.quartz.scheduler.userTransactionURL", "java:comp/env/"
                                + INamingBinds.RESOURCE_USERTRANSACTION);
            props.put("org.quartz.scheduler.wrapJobExecutionInUserTransaction", "true");
            props.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
            props.put("org.quartz.threadPool.threadCount", "1");
            schedFact.initialize(props);
            final Scheduler sched =  schedFact.getScheduler();

            sched.start();

            final JobDetail jobDetail = new JobDetail("SystemMessage",
                                            null,
                                            SystemMessage.class);

            final Trigger trigger = TriggerUtils.makeMinutelyTrigger(1);
            trigger.setName("SystemMessageTrigger");

            sched.scheduleJob(jobDetail, trigger);
        } catch (final SchedulerException e) {
            throw new EFapsException(Quartz.class, "Quartz.SchedulerException", e);
        }
    }

    /**
     * Getter method for the instance variable {@link #QUARTZ}.
     *
     * @return value of instance variable {@link #QUARTZ}
     * @throws EFapsException on error
     */
    public static Quartz getQuartz()
        throws EFapsException
    {
        if (Quartz.QUARTZ == null) {
            Quartz.initialize();
        }
        return Quartz.QUARTZ;
    }
}
