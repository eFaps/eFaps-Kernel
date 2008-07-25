/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.eclipse.logger;

import org.efaps.eclipse.EfapsPlugin;
import org.slf4j.Logger;
import org.slf4j.Marker;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;
import static org.slf4j.helpers.MessageFormatter.format;

/**
 * This class is a wrapper for the simple logging facade to print the logging
 * information to the eclipse console of the eFaps eclipse plugin.
 * 
 * @see Logger    interface of the simple logging facade
 * @author tmo
 * @version $Id$
 */
public class SLF4JOverEclipseConsole implements Logger
{
  /////////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  /**
   * @see Logger#getName()
   */
  public String getName()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /////////////////////////////////////////////////////////////////////////////
  // debug

  /**
   * Is debug logging currently enabled?
   */
  public boolean isDebugEnabled()
  {
    return false;
  }

  /**
   * @see Logger#isDebugEnabled(Marker)
   */
  public boolean isDebugEnabled(final Marker _marker)
  {
    return isDebugEnabled();
  }

  /**
   * @see Logger#debug(String)
   */
  public void debug(final String _text)
  {
    if (isDebugEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.DEBUG, _text);
    }
  }

  /**
   * @see Logger#debug(String, Object)
   */
  public void debug(final String _messagePattern,
                    final Object _arg)
  {
    debug(format(_messagePattern, _arg));
  }

  /**
   * @see Logger#debug(String, Object[])
   */
  public void debug(final String _messagePattern,
                    final Object[] _args)
  {
    debug(arrayFormat(_messagePattern, _args));
  }

  /**
   * @see Logger#debug(String, Throwable)
   */
  public void debug(final String _text, final Throwable _e) {
    if (isDebugEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.DEBUG, _text, _e);
    }
  }

  /**
   * @see Logger#debug(Marker, String)
   */
  public void debug(final Marker _marker,
                    final String _text)
  {
    debug(_text);
  }

  /**
   * @see Logger#debug(String, Object, Object)
   */
  public void debug(final String _messagePattern,
                    final Object _arg1,
                    final Object _arg2)
  {
    debug(format(_messagePattern, _arg1, _arg2));
  }

  /**
   * @see Logger#debug(Marker, String, Object)
   */
  public void debug(final Marker _marker,
                    final String _messagePattern,
                    final Object _arg)
  {
    debug(format(_messagePattern, _arg));
  }

  /**
   * @see Logger#debug(Marker, String, Object[])
   */
  public void debug(final Marker _marker,
                    final String _messagePattern,
                    final Object[] _args)
  {
    debug(arrayFormat(_messagePattern, _args));
  }

  /**
   * @see Logger#debug(Marker, String, Throwable)
   */
  public void debug(final Marker _marker,
                    final String _text,
                    final Throwable _e)
  {
    debug(_text, _e);
  }

  /**
   * @see Logger#debug(Marker, String, Object, Object)
   */
  public void debug(final Marker _marker,
                    final String _messagePattern,
                    final Object _arg1,
                    final Object _arg2)
  {
    debug(format(_messagePattern, _arg1, _arg2));
  }

  /////////////////////////////////////////////////////////////////////////////
  // error

  /**
   * Is error logging currently enabled?
   */
  public boolean isErrorEnabled()
  {
    return true;
  }

  /**
   * @see Logger#isErrorEnabled(Marker)
   */
  public boolean isErrorEnabled(final Marker _marker)
  {
    return isErrorEnabled();
  }

  /**
   * @see Logger#error(String)
   */
  public void error(final String _text)
  {
    if (isErrorEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.ERROR, _text);
    }
  }

  /**
   * @see Logger#error(String, Object)
   */
  public void error(final String _messagePattern,
                    final Object _arg)
  {
    error(format(_messagePattern, _arg));
  }

  /**
   * @see Logger#error(String, Object[])
   */
  public void error(final String _messagePattern,
                    final Object[] _args)
  {
    error(arrayFormat(_messagePattern, _args));
  }

  /**
   * @see Logger#error(String, Throwable)
   */
  public void error(final String _text, final Throwable _e) {
    if (isErrorEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.ERROR, _text, _e);
    }
  }

  /**
   * @see Logger#error(Marker, String)
   */
  public void error(final Marker _marker,
                    final String _text)
  {
    error(_text);
  }

  /**
   * @see Logger#error(String, Object, Object)
   */
  public void error(final String _messagePattern,
                    final Object _arg1,
                    final Object _arg2)
  {
    error(format(_messagePattern, _arg1, _arg2));
  }

  /**
   * @see Logger#error(Marker, String, Object)
   */
  public void error(final Marker _marker,
                    final String _messagePattern,
                    final Object _arg)
  {
    error(format(_messagePattern, _arg));
  }

  /**
   * @see Logger#error(Marker, String, Object[])
   */
  public void error(final Marker _marker,
                    final String _messagePattern,
                    final Object[] _args)
  {
    error(arrayFormat(_messagePattern, _args));
  }

  /**
   * @see Logger#error(Marker, String, Throwable)
   */
  public void error(final Marker _marker,
                    final String _text,
                    final Throwable _e)
  {
    error(_text, _e);
  }

  /**
   * @see Logger#error(Marker, String, Object, Object)
   */
  public void error(final Marker _marker,
                    final String _messagePattern,
                    final Object _arg1,
                    final Object _arg2)
  {
    error(format(_messagePattern, _arg1, _arg2));
  }

  /////////////////////////////////////////////////////////////////////////////
  // info

  /**
   * Is info logging currently enabled?
   */
  public boolean isInfoEnabled()
  {
    return true;
  }

  /**
   * @see Logger#isInfoEnabled(Marker)
   */
  public boolean isInfoEnabled(final Marker _marker)
  {
    return isInfoEnabled();
  }

  /**
   * @see Logger#info(String)
   */
  public void info(final String _text) 
  {
    if (isInfoEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.INFO, _text);
    }
  }

  /**
   * @see Logger#info(String, Object)
   */
  public void info(final String _messagePattern,
                   final Object _arg)
  {
    info(format(_messagePattern, _arg));
  }

  /**
   * @see Logger#info(String, Object[])
   */
  public void info(final String _messagePattern,
                   final Object[] _args)
  {
    info(arrayFormat(_messagePattern, _args));
  }

  /**
   * @see Logger#info(String, Throwable)
   */
  public void info(final String _text,
                   final Throwable _e)
  {
    if (isInfoEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.INFO, _text, _e);
    }
  }

  /**
   * @see Logger#info(Marker, String)
   */
  public void info(final Marker _marker,
                   final String _text)
  {
    info(_text);
  }

  /**
   * @see Logger#info(String, Object, Object)
   */
  public void info(final String _messagePattern,
                   final Object _arg1,
                   final Object _arg2)
  {
    info(format(_messagePattern, _arg1, _arg2));
  }

  /**
   * @see Logger#info(Marker, String, Object)
   */
  public void info(final Marker _marker,
                   final String _messagePattern,
                   final Object _arg)
  {
    info(format(_messagePattern, _arg));
  }

  /**
   * @see Logger#info(Marker, String, Object[])
   */
  public void info(final Marker _marker,
                   final String _messagePattern,
                   final Object[] _args)
  {
    info(arrayFormat(_messagePattern, _args));
  }

  /**
   * @see Logger#info(Marker, String, Throwable)
   */
  public void info(final Marker _marker,
                   final String _text,
                   final Throwable _e)
  {
    info(_text, _e);
  }

  /**
   * @see Logger#info(Marker, String, Object, Object)
   */
  public void info(final Marker _marker,
                   final String _messagePattern,
                   final Object _arg1,
                   final Object _arg2)
  {
    info(format(_messagePattern, _arg1, _arg2));
  }

  /////////////////////////////////////////////////////////////////////////////
  // trace => debug

  /**
   * Is trace logging currently enabled?
   */
  public boolean isTraceEnabled() {
    return false;
  }

  /**
   * @see Logger#isTraceEnabled(Marker)
   */
  public boolean isTraceEnabled(final Marker _marker)
  {
    return isTraceEnabled();
  }

  /**
   * @see Logger#trace(String)
   */
  public void trace(final String _text)
  {
    if (isTraceEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.TRACE, _text);
    }
  }

  /**
   * @see Logger#trace(String, Object)
   */
  public void trace(final String _messagePattern,
                    final Object _arg)
  {
    trace(format(_messagePattern, _arg));
  }

  /**
   * @see Logger#trace(String, Object[])
   */
  public void trace(final String _messagePattern,
                    final Object[] _args)
  {
    trace(arrayFormat(_messagePattern, _args));
  }

  /**
   * @see Logger#trace(String, Throwable)
   */
  public void trace(final String _text,
                    final Throwable _e)
  {
    if (isTraceEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.TRACE, _text, _e);
    }
  }

  /**
   * @see Logger#trace(Marker, String)
   */
  public void trace(final Marker _marker,
                    final String _text)
  {
    trace(_text);
  }

  /**
   * @see Logger#trace(String, Object, Object)
   */
  public void trace(final String _messagePattern,
                    final Object _arg1,
                    final Object _arg2)
  {
    trace(format(_messagePattern, _arg1, _arg2));
  }

  /**
   * @see Logger#trace(Marker, String, Object)
   */
  public void trace(final Marker _marker,
                    final String _messagePattern,
                    final Object _arg)
  {
    trace(format(_messagePattern, _arg));
  }

  /**
   * @see Logger#trace(Marker, String, Object[])
   */
  public void trace(final Marker _marker,
                    final String _messagePattern,
                    final Object[] _args)
  {
    trace(arrayFormat(_messagePattern, _args));
  }

  /**
   * @see Logger#trace(Marker, String, Throwable)
   */
  public void trace(final Marker _marker,
                    final String _text,
                    final Throwable _e)
  {
    trace(_text, _e);
  }

  /**
   * @see Logger#trace(Marker, String, Object, Object)
   */
  public void trace(final Marker _marker,
                    final String _messagePattern,
                    final Object _arg1,
                    final Object _arg2)
  {
    trace(format(_messagePattern, _arg1, _arg2));
  }

  /////////////////////////////////////////////////////////////////////////////
  // warn

  /**
   * Is warn logging currently enabled?
   */
  public boolean isWarnEnabled()
  {
    return false;
  }

  /**
   * (non-Javadoc)
   * @see Logger#isWarnEnabled(Marker)
   * @see #isWarnEnabled()
   */
  public boolean isWarnEnabled(final Marker _marker)
  {
    return isWarnEnabled();
  }

  /**
   * @see Logger#warn(String)
   */
  public void warn(final String _text)
  {
    if (isWarnEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.WARN, _text);
    }
  }

  /**
   * @see Logger#warn(String, Object)
   */
  public void warn(final String _messagePattern,
                   final Object _arg)
  {
    warn(format(_messagePattern, _arg));
  }

  /**
   * @see Logger#warn(String, Object[])
   */
  public void warn(final String _messagePattern,
                   final Object[] _args)
  {
    warn(arrayFormat(_messagePattern, _args));
  }

  /**
   * @see Logger#warn(String, Throwable)
   */
  public void warn(final String _text,
                   final Throwable _e)
  {
    if (isWarnEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.WARN, _text, _e);
    }
  }

  /**
   * @see Logger#warn(Marker, String)
   */
  public void warn(final Marker _marker,
                   final String _text)
  {
    warn(_text);
  }

  /**
   * @see Logger#warn(String, Object, Object)
   */
  public void warn(final String _messagePattern,
                   final Object _arg1,
                   final Object _arg2)
  {
    warn(format(_messagePattern, _arg1, _arg2));
  }

  /**
   * @see Logger#warn(Marker, String, Object)
   */
  public void warn(final Marker _marker,
                   final String _messagePattern,
                   final Object _arg)
  {
    warn(format(_messagePattern, _arg));
  }

  /**
   * @see Logger#warn(Marker, String, Object[])
   */
  public void warn(final Marker _marker,
                   final String _messagePattern,
                   final Object[] _args)
  {
    warn(arrayFormat(_messagePattern, _args));
  }

  /**
   * @see Logger#warn(Marker, String, Throwable)
   */
  public void warn(final Marker _marker,
                   final String _text,
                   final Throwable _e)
  {
    warn(_text, _e);
  }

  /**
   * @see Logger#warn(Marker, String, Object, Object)
   */
  public void warn(final Marker _marker,
                   final String _messagePattern,
                   final Object _arg1,
                   final Object _arg2)
  {
    warn(format(_messagePattern, _arg1, _arg2));
  }
}
