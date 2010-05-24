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

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.spi.SchedulerPlugin;



/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QuartzSchedulerPlugin
    implements SchedulerPlugin
{

    /* (non-Javadoc)
     * @see org.quartz.spi.SchedulerPlugin#initialize(java.lang.String, org.quartz.Scheduler)
     */
    @Override
    public void initialize(final String _name,
                           final Scheduler _scheduler)
        throws SchedulerException
    {
        try {
            final QueryBuilder queryBldr = new QueryBuilder(Type.get(EFapsClassNames.ADMIN_COMMON_QUARTZABSTRACT));
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute("Type", "Name", "Parameter1" , "Parameter2", "Parameter3");
            multi.addSelect("linkto[ESJPLink].attribute[FileName]");
            multi.execute();
            while (multi.next()) {
                final Type type = multi.<Type>getAttribute("Type");
                final String name = multi.<String>getAttribute("Name");
                final Integer para1 = multi.<Integer>getAttribute("Parameter1");
                final Integer para2 = multi.<Integer>getAttribute("Parameter2");
                final Integer para3 = multi.<Integer>getAttribute("Parameter3");
                final String esjp = multi.<String>getSelect("linkto[ESJPLink].attribute[FileName]");
                Trigger trigger = null;
                if (type.isKindOf(Type.get(EFapsClassNames.ADMIN_COMMON_QUARTZSECONDLY))) {
                    trigger = TriggerUtils.makeSecondlyTrigger(name, para1, para2);
                } else if (type.isKindOf(Type.get(EFapsClassNames.ADMIN_COMMON_QUARTZMINUTELY))) {
                    trigger = TriggerUtils.makeMinutelyTrigger(name, para1, para2);
                } else if (type.isKindOf(Type.get(EFapsClassNames.ADMIN_COMMON_QUARTZHOURLY))) {
                    trigger = TriggerUtils.makeHourlyTrigger(name, para1, para2);
                } else if (type.isKindOf(Type.get(EFapsClassNames.ADMIN_COMMON_QUARTZDAILY))) {
                    trigger = TriggerUtils.makeDailyTrigger(name, para1, para2);
                } else if (type.isKindOf(Type.get(EFapsClassNames.ADMIN_COMMON_QUARTZWEEKLY))) {
                    trigger = TriggerUtils.makeWeeklyTrigger(name, para1, para2, para3);
                } else if (type.isKindOf(Type.get(EFapsClassNames.ADMIN_COMMON_QUARTZMONTHLY))) {
                    trigger = TriggerUtils.makeMonthlyTrigger(name, para1, para2, para3);
                }
                final Class<?> clazz = Class.forName(esjp, false,
                                new EFapsClassLoader(this.getClass().getClassLoader()));
                final JobDetail jobDetail = new JobDetail(name + "_" + esjp, clazz);
                if (trigger != null) {
                    _scheduler.scheduleJob(jobDetail, trigger);
                }
            }
        } catch (final ClassNotFoundException e) {
            throw new SchedulerException(e);
        } catch (final EFapsException e) {
            throw new SchedulerException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.quartz.spi.SchedulerPlugin#shutdown()
     */
    @Override
    public void shutdown()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.quartz.spi.SchedulerPlugin#start()
     */
    @Override
    public void start()
    {
        // TODO Auto-generated method stub

    }

}
