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

package org.efaps.bpm.listener;

import org.drools.SystemEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SystemEventLstnr
    implements SystemEventListener
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SystemEventLstnr.class);

    public void debug(final String arg0)
    {
        SystemEventLstnr.LOG.debug(arg0);
    }

    public void debug(final String arg0,
                      final Object arg1)
    {
        SystemEventLstnr.LOG.debug(arg0, arg1);
    }

    public void exception(final Throwable arg0)
    {
        SystemEventLstnr.LOG.error("Error:", arg0);
    }

    public void exception(final String arg0,
                          final Throwable arg1)
    {
        SystemEventLstnr.LOG.error(arg0, arg1);
    }

    public void info(final String arg0)
    {
        SystemEventLstnr.LOG.info(arg0);
    }

    public void info(final String arg0,
                     final Object arg1)
    {
        SystemEventLstnr.LOG.info(arg0, arg1);
    }

    public void warning(final String arg0)
    {
        SystemEventLstnr.LOG.warn(arg0);
    }

    public void warning(final String arg0,
                        final Object arg1)
    {
        SystemEventLstnr.LOG.warn(arg0, arg1);
    }
}
