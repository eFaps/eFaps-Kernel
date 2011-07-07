/*
 * Copyright 2003 - 2011 The eFaps Team
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
import java.util.UUID;

import org.efaps.init.INamingBinds;
import org.efaps.message.MessageStatusHolder;
import org.efaps.util.EFapsException;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
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
     * Quartz Group Name.
     */
    public static final String QUARTZGROUP = "eFapsQuartzGroup";

    /**
     * Key for the SystemConfiguration attribute that contains the
     * properties for the Quartz Scheduler.
     */
    private static final String QUARTZPROPS = "QuartzProperties";

    /**
     * Key for the SystemConfiguration attribute that activates the
     * SystemMessage Trigger.
     */
    private static final String MSGTRIGGERACTIVE = "SystemMessageTriggerActivated";

    /**
     * Key for the SystemConfiguration attribute that sets the
     * Interval for the SystemMessage Trigger.
     */
    private static final String MSGTRIGGERINTERVAL = "SystemMessageTriggerInterval";

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
            //Kernel-Configuration
            final SystemConfiguration config = SystemConfiguration.get(
                            UUID.fromString("acf2b19b-f7c4-4e4a-a724-fb2d9ed30079"));
            final Properties props = config.getAttributeValueAsProperties(Quartz.QUARTZPROPS);

            final StdSchedulerFactory schedFact = new StdSchedulerFactory();
            props.put(StdSchedulerFactory.PROP_SCHED_USER_TX_URL, "java:comp/env/"
                                + INamingBinds.RESOURCE_USERTRANSACTION);
            props.put(StdSchedulerFactory.PROP_SCHED_WRAP_JOB_IN_USER_TX, "true");
            props.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool");
            props.put("org.quartz.plugin.jobInitializer.class", "org.efaps.admin.common.QuartzSchedulerPlugin");

            if (!props.containsKey(StdSchedulerFactory.PROP_SCHED_MAKE_SCHEDULER_THREAD_DAEMON)) {
                props.put(StdSchedulerFactory.PROP_SCHED_MAKE_SCHEDULER_THREAD_DAEMON, "true");
            }
            if (!props.containsKey(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME)) {
                props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "eFapsScheduler");
            }
            if (!props.containsKey("org.quartz.threadPool.threadCount")) {
                props.put("org.quartz.threadPool.threadCount", "2");
            }

            if (!props.containsKey("org.quartz.plugin.triggHistory.class")) {
                props.put("org.quartz.plugin.triggHistory.class",
                                "org.quartz.plugins.history.LoggingTriggerHistoryPlugin");
                props.put("org.quartz.plugin.triggHistory.triggerFiredMessage",
                            "Trigger {1}.{0} fired job {6}.{5} at: {4, date, HH:mm:ss MM/dd/yyyy}");
                props.put("org.quartz.plugin.triggHistory.triggerCompleteMessage",
                            "Trigger {1}.{0} completed firing job {6}.{5} at {4, date, HH:mm:ss MM/dd/yyyy}.");
            }

            schedFact.initialize(props);
            Scheduler sched;
            sched = schedFact.getScheduler("eFapsScheduler");
            if (sched != null) {
                sched.shutdown();
            }
            sched =  schedFact.getScheduler();

            if (config.getAttributeValueAsBoolean(Quartz.MSGTRIGGERACTIVE)) {
                final int interval = config.getAttributeValueAsInteger(Quartz.MSGTRIGGERINTERVAL);
                final Trigger trigger = TriggerBuilder.newTrigger()
                                .withIdentity("SystemMessageTrigger")
                                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(interval > 0 ? interval : 1))
                                .build();

                JobDetail jobDetail = sched.getJobDetail(new JobKey("SystemMessage", Quartz.QUARTZGROUP));
                if (jobDetail == null) {
                    jobDetail = JobBuilder.newJob(MessageStatusHolder.class)
                                    .withIdentity("SystemMessage", Quartz.QUARTZGROUP).build();
                    sched.scheduleJob(jobDetail, trigger);
                } else {
                    sched.rescheduleJob(new TriggerKey("SystemMessageTrigger", Quartz.QUARTZGROUP), trigger);
                }
            }
            sched.start();
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
