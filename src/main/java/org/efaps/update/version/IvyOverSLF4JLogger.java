/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.update.version;

import org.apache.ivy.util.AbstractMessageLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper between the Ivy logger and the SLF$J logger.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class IvyOverSLF4JLogger
    extends AbstractMessageLogger
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationVersion.class);

    /**
     * Method is only required to implement the IVY logging interface.
     *
     * @param _text     not used
     */
    @Override
    protected void doEndProgress(final String _text)
    {
    }

    /**
     * Method is only required to implement the IVY logging interface.
     */
    @Override
    protected void doProgress()
    {
    }

    /**
     * Logs depending on the <code>_level</code> given <code>_message</code>.
     *
     * @param _message      message to log
     * @param _level        level to log
     */
    public void log(final String _message,
                    final int _level)
    {
        switch (_level)  {
            case 4:
                if (IvyOverSLF4JLogger.LOG.isDebugEnabled())  {
                    IvyOverSLF4JLogger.LOG.debug(_message);
                }
                break;
            case 3:
                if (IvyOverSLF4JLogger.LOG.isWarnEnabled())  {
                    IvyOverSLF4JLogger.LOG.warn(_message);
                }
                break;
            case 2:
                if (IvyOverSLF4JLogger.LOG.isInfoEnabled())  {
                    IvyOverSLF4JLogger.LOG.info(_message);
                }
                break;
            case 1:
                if (IvyOverSLF4JLogger.LOG.isErrorEnabled())  {
                    IvyOverSLF4JLogger.LOG.error(_message);
                }
                break;
            default:
                IvyOverSLF4JLogger.LOG.error("unknown log level " + _level);
                IvyOverSLF4JLogger.LOG.error(_message);
        }
    }

    /**
     * Calls {@link #log(String, int)}.
     *
     * @param _message      message to log
     * @param _level        level to log
     */
    public void rawlog(final String _message,
                       final int _level)
    {
        this.log(_message, _level);
    }
}
