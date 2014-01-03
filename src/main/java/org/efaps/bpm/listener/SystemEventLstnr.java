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


import org.kie.internal.SystemEventListener;
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

    /**
     * @param _message message to log
     */
    public void debug(final String _message)
    {
        SystemEventLstnr.LOG.debug(_message);
    }

    /**
     * @param _message message to log
     * @param _obj     Object to log
     */
    public void debug(final String _message,
                      final Object _obj)
    {
        SystemEventLstnr.LOG.debug(_message, _obj);
    }

    /**
     * @param _exp throwable
     */
    public void exception(final Throwable _exp)
    {
        SystemEventLstnr.LOG.error("Error:", _exp);
    }

    /**
     * @param _message message to log
     * @param _exp throwable
     */
    public void exception(final String _message,
                          final Throwable _exp)
    {
        SystemEventLstnr.LOG.error(_message, _exp);
    }

    /**
     * @param _message message to log
     */
    public void info(final String _message)
    {
        SystemEventLstnr.LOG.info(_message);
    }

    /**
     * @param _message message to log
     * @param _obj     Object to log
     */
    public void info(final String _message,
                     final Object _obj)
    {
        SystemEventLstnr.LOG.info(_message, _obj);
    }

    /**
     * @param _message message to log
     */
    public void warning(final String _message)
    {
        SystemEventLstnr.LOG.warn(_message);
    }

    /**
     * @param _message message to log
     * @param _obj     Object to log
     */
    public void warning(final String _message,
                        final Object _obj)
    {
        SystemEventLstnr.LOG.warn(_message, _obj);
    }
}
