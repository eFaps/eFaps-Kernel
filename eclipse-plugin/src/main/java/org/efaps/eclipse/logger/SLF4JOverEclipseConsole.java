/*
 * Copyright 2003 - 2009 The eFaps Team
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

import static org.slf4j.helpers.MessageFormatter.arrayFormat;
import static org.slf4j.helpers.MessageFormatter.format;

import org.slf4j.Logger;
import org.slf4j.Marker;

import org.efaps.eclipse.EfapsPlugin;

/**
 * This class is a wrapper for the simple logging facade to print the logging
 * information to the eclipse console of the eFaps eclipse plugin.
 *
 * @see Logger    interface of the simple logging facade
 * @author tmo
 * @version $Id$
 */
public class SLF4JOverEclipseConsole implements Logger {
  /////////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  /**
   * @see Logger#getName()
   * @return null
   */
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  /////////////////////////////////////////////////////////////////////////////
  // debug

  /**
   * Is debug logging currently enabled?
   * @return false
   */
  public boolean isDebugEnabled() {
    return false;
  }

  /**
   * @see Logger#isDebugEnabled(Marker)
   * @param _marker marker
   * @return false
   */
  public boolean isDebugEnabled(final Marker _marker) {
    return isDebugEnabled();
  }

  /**
   * Method to add a debug message to the logger.
   *
   * @param _text   message
   */
  public void debug(final String _text) {
    if (isDebugEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.DEBUG, _text);
    }
  }

  /**
   * Method to add a debug message to the logger.
   *
   * @param _messagePattern   message
   * @param _arg             Objects to add
   *
   */
  public void debug(final String _messagePattern,
                    final Object _arg) {
    debug(format(_messagePattern, _arg));
  }

  /**
   * Method to add a debug message to the logger.
   *
   * @param _messagePattern   message
   * @param _args             Objects to add
   *
   */
  public void debug(final String _messagePattern,
                    final Object[] _args) {
    debug(arrayFormat(_messagePattern, _args));
  }

  /**
   * Method to add a debug message to the logger.
   *
   * @param _text         message
   * @param _e            Throwable to add
   *
   */
  public void debug(final String _text, final Throwable _e) {
    if (isDebugEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.DEBUG, _text, _e);
    }
  }

  /**
   * Method to add a debug message to the logger.
   *
   * @param _marker         Marker
   * @param _text           message
   *
   */
  public void debug(final Marker _marker,
                    final String _text) {
    debug(_text);
  }

  /**
   * Method to add a debug message to the logger.
   *
   * @param _messagePattern message
   * @param _arg1            Object to add
   * @param _arg2            Object to add
   *
   */
  public void debug(final String _messagePattern,
                    final Object _arg1,
                    final Object _arg2) {
    debug(format(_messagePattern, _arg1, _arg2));
  }

  /**
   * Method to add a debug message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _arg            Object to add
   *
   */
  public void debug(final Marker _marker,
                    final String _messagePattern,
                    final Object _arg) {
    debug(format(_messagePattern, _arg));
  }

  /**
   * Method to add a debug message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _args           Objects to add
   *
   */
  public void debug(final Marker _marker,
                    final String _messagePattern,
                    final Object[] _args) {
    debug(arrayFormat(_messagePattern, _args));
  }

  /**
   * Method to add a debug message to the logger.
   *
   * @param _marker         Marker
   * @param _text           message
   * @param _e              Throwable to add
   *
   */
  public void debug(final Marker _marker,
                    final String _text,
                    final Throwable _e) {
    debug(_text, _e);
  }

  /**
   * Method to add a debug message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _arg1           object to add
   * @param _arg2           object to add
   */
  public void debug(final Marker _marker,
                    final String _messagePattern,
                    final Object _arg1,
                    final Object _arg2) {
    debug(format(_messagePattern, _arg1, _arg2));
  }

  /////////////////////////////////////////////////////////////////////////////
  // error

  /**
   * Is error logging currently enabled?
   * @return true
   */
  public boolean isErrorEnabled() {
    return true;
  }

  /**
   * @see Logger#isErrorEnabled(Marker)
   * @param _marker marker
   * @return false
   */
  public boolean isErrorEnabled(final Marker _marker) {
    return isErrorEnabled();
  }

  /**
   * Method to add a error message to the logger.
   *
   * @param _text   message
   */
  public void error(final String _text) {
    if (isErrorEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.ERROR, _text);
    }
  }

  /**
   * Method to add a error message to the logger.
   *
   * @param _messagePattern   message
   * @param _arg             Objects to add
   *
   */
  public void error(final String _messagePattern,
                    final Object _arg) {
    error(format(_messagePattern, _arg));
  }

  /**
   * Method to add a error message to the logger.
   *
   * @param _messagePattern   message
   * @param _args             Objects to add
   *
   */
  public void error(final String _messagePattern,
                    final Object[] _args) {
    error(arrayFormat(_messagePattern, _args));
  }

  /**
   * Method to add a error message to the logger.
   *
   * @param _text         message
   * @param _e            Throwable to add
   *
   */
  public void error(final String _text, final Throwable _e) {
    if (isErrorEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.ERROR, _text, _e);
    }
  }

  /**
   * Method to add a error message to the logger.
   *
   * @param _marker         Marker
   * @param _text           message
   *
   */
  public void error(final Marker _marker,
                    final String _text) {
    error(_text);
  }

  /**
   * Method to add a error message to the logger.
   *
   * @param _messagePattern message
   * @param _arg1            Object to add
   * @param _arg2            Object to add
   *
   */
  public void error(final String _messagePattern,
                    final Object _arg1,
                    final Object _arg2) {
    error(format(_messagePattern, _arg1, _arg2));
  }

  /**
   * Method to add a error message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _arg            Object to add
   *
   */
  public void error(final Marker _marker,
                    final String _messagePattern,
                    final Object _arg) {
    error(format(_messagePattern, _arg));
  }

  /**
   * Method to add a error message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _args           Objects to add
   *
   */
  public void error(final Marker _marker,
                    final String _messagePattern,
                    final Object[] _args) {
    error(arrayFormat(_messagePattern, _args));
  }

  /**
   * Method to add a error message to the logger.
   *
   * @param _marker         Marker
   * @param _text           message
   * @param _e              Throwable to add
   *
   */
  public void error(final Marker _marker,
                    final String _text,
                    final Throwable _e) {
    error(_text, _e);
  }

  /**
   * Method to add a error message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _arg1           object to add
   * @param _arg2           object to add
   */
  public void error(final Marker _marker,
                    final String _messagePattern,
                    final Object _arg1,
                    final Object _arg2) {
    error(format(_messagePattern, _arg1, _arg2));
  }

  /////////////////////////////////////////////////////////////////////////////
  // info

  /**
   * Is info logging currently enabled?
   *
   * @return true
   */
  public boolean isInfoEnabled() {
    return true;
  }

  /**
   * @see Logger#isInfoEnabled(Marker)
   * @param _marker marker
   * @return true
   */
  public boolean isInfoEnabled(final Marker _marker) {
    return isInfoEnabled();
  }

  /**
   * Method to add a info message to the logger.
   *
   * @param _text   message
   */
  public void info(final String _text) {
    if (isInfoEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.INFO, _text);
    }
  }

  /**
   * Method to add a info message to the logger.
   *
   * @param _messagePattern   message
   * @param _arg             Objects to add
   *
   */
  public void info(final String _messagePattern,
                   final Object _arg) {
    info(format(_messagePattern, _arg));
  }

  /**
   * Method to add a info message to the logger.
   *
   * @param _messagePattern   message
   * @param _args             Objects to add
   *
   */
  public void info(final String _messagePattern,
                   final Object[] _args) {
    info(arrayFormat(_messagePattern, _args));
  }

  /**
   * Method to add a info message to the logger.
   *
   * @param _text         message
   * @param _e            Throwable to add
   *
   */
  public void info(final String _text,
                   final Throwable _e) {
    if (isInfoEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.INFO, _text, _e);
    }
  }

  /**
   * Method to add a info message to the logger.
   *
   * @param _marker         Marker
   * @param _text           message
   *
   */
  public void info(final Marker _marker,
                   final String _text) {
    info(_text);
  }

  /**
   * Method to add a info message to the logger.
   *
   * @param _messagePattern message
   * @param _arg1            Object to add
   * @param _arg2            Object to add
   *
   */
  public void info(final String _messagePattern,
                   final Object _arg1,
                   final Object _arg2) {
    info(format(_messagePattern, _arg1, _arg2));
  }

  /**
   * Method to add a info message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _arg            Object to add
   *
   */
  public void info(final Marker _marker,
                   final String _messagePattern,
                   final Object _arg) {
    info(format(_messagePattern, _arg));
  }

  /**
   * Method to add a info message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _args           Objects to add
   *
   */
  public void info(final Marker _marker,
                   final String _messagePattern,
                   final Object[] _args) {
    info(arrayFormat(_messagePattern, _args));
  }

  /**
   * Method to add a info message to the logger.
   *
   * @param _marker         Marker
   * @param _text           message
   * @param _e              Throwable to add
   *
   */
  public void info(final Marker _marker,
                   final String _text,
                   final Throwable _e) {
    info(_text, _e);
  }

  /**
   * Method to add a info message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _arg1           object to add
   * @param _arg2           object to add
   */
  public void info(final Marker _marker,
                   final String _messagePattern,
                   final Object _arg1,
                   final Object _arg2) {
    info(format(_messagePattern, _arg1, _arg2));
  }

  /////////////////////////////////////////////////////////////////////////////
  // trace => debug

  /**
   * Is trace logging currently enabled?
   * @return false
   */
  public boolean isTraceEnabled() {
    return false;
  }

  /**
   * @see Logger#isTraceEnabled(Marker)
   * @param _marker marker
   * @return false
   */
  public boolean isTraceEnabled(final Marker _marker) {
    return isTraceEnabled();
  }

  /**
   * Method to add a trace message to the logger.
   *
   * @param _text   message
   */
  public void trace(final String _text) {
    if (isTraceEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.TRACE, _text);
    }
  }

  /**
   * Method to add a trace message to the logger.
   *
   * @param _messagePattern   message
   * @param _arg             Objects to add
   *
   */
  public void trace(final String _messagePattern,
                    final Object _arg) {
    trace(format(_messagePattern, _arg));
  }

  /**
   * Method to add a trace message to the logger.
   *
   * @param _messagePattern   message
   * @param _args             Objects to add
   *
   */
  public void trace(final String _messagePattern,
                    final Object[] _args) {
    trace(arrayFormat(_messagePattern, _args));
  }

  /**
   * Method to add a trace message to the logger.
   *
   * @param _text         message
   * @param _e            Throwable to add
   *
   */
  public void trace(final String _text,
                    final Throwable _e) {
    if (isTraceEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.TRACE, _text, _e);
    }
  }

  /**
   * Method to add a trace message to the logger.
   *
   * @param _marker         Marker
   * @param _text           message
   *
   */
  public void trace(final Marker _marker,
                    final String _text) {
    trace(_text);
  }

  /**
   * Method to add a trace message to the logger.
   *
   * @param _messagePattern message
   * @param _arg1            Object to add
   * @param _arg2            Object to add
   *
   */
  public void trace(final String _messagePattern,
                    final Object _arg1,
                    final Object _arg2) {
    trace(format(_messagePattern, _arg1, _arg2));
  }

  /**
   * Method to add a trace message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _arg            Object to add
   *
   */
  public void trace(final Marker _marker,
                    final String _messagePattern,
                    final Object _arg) {
    trace(format(_messagePattern, _arg));
  }

  /**
   * Method to add a trace message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _args           Objects to add
   *
   */
  public void trace(final Marker _marker,
                    final String _messagePattern,
                    final Object[] _args) {
    trace(arrayFormat(_messagePattern, _args));
  }

  /**
   * Method to add a trace message to the logger.
   *
   * @see Logger#warn(Marker, String, Throwable)
   * @param _marker         Marker
   * @param _text           message
   * @param _e              Throwable to add
   */
  public void trace(final Marker _marker,
                    final String _text,
                    final Throwable _e) {
    trace(_text, _e);
  }

  /**
   * Method to add a trace message to the logger.
   *
   * @see Logger#warn(Marker, String, Object, Object)
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _arg1           object to add
   * @param _arg2           object to add
   */
  public void trace(final Marker _marker,
                    final String _messagePattern,
                    final Object _arg1,
                    final Object _arg2) {
    trace(format(_messagePattern, _arg1, _arg2));
  }

  /////////////////////////////////////////////////////////////////////////////
  // warn

  /**
   * Is warn logging currently enabled?
   * @return false
   */
  public boolean isWarnEnabled() {
    return false;
  }

  /**
   * (non-Javadoc).
   * @see Logger#isWarnEnabled(Marker)
   * @see #isWarnEnabled()
   * @param _marker marker
   * @return false
   */
  public boolean isWarnEnabled(final Marker _marker) {
    return isWarnEnabled();
  }

  /**
   * Method to add a warn message to the logger.
   *
   * @param _text   message
   */
  public void warn(final String _text) {
    if (isWarnEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.WARN, _text);
    }
  }

  /**
   * Method to add a warn message to the logger.
   *
   * @param _messagePattern   message
   * @param _arg             Objects to add
   *
   */
  public void warn(final String _messagePattern,
                   final Object _arg) {
    warn(format(_messagePattern, _arg));
  }

  /**
   * Method to add a warn message to the logger.
   *
   * @param _messagePattern   message
   * @param _args             Objects to add
   *
   */
  public void warn(final String _messagePattern,
                   final Object[] _args) {
    warn(arrayFormat(_messagePattern, _args));
  }

  /**
   * Method to add a warn message to the logger.
   *
   * @param _text         message
   * @param _e            Throwable to add
   *
   */
  public void warn(final String _text,
                   final Throwable _e) {
    if (isWarnEnabled())  {
      EfapsPlugin.getDefault().println(EfapsPlugin.LogLevel.WARN, _text, _e);
    }
  }

  /**
   * Method to add a warn message to the logger.
   *
   * @param _marker         Marker
   * @param _text           message
   *
   */
  public void warn(final Marker _marker,
                   final String _text) {
    warn(_text);
  }

  /**
   * Method to add a warn message to the logger.
   *
   * @param _messagePattern message
   * @param _arg1            Object to add
   * @param _arg2            Object to add
   *
   */
  public void warn(final String _messagePattern,
                   final Object _arg1,
                   final Object _arg2) {
    warn(format(_messagePattern, _arg1, _arg2));
  }

  /**
   * Method to add a warn message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _arg            Object to add
   *
   */
  public void warn(final Marker _marker,
                   final String _messagePattern,
                   final Object _arg) {
    warn(format(_messagePattern, _arg));
  }

  /**
   * Method to add a warn message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _args           Objects to add
   *
   */
  public void warn(final Marker _marker,
                   final String _messagePattern,
                   final Object[] _args) {
    warn(arrayFormat(_messagePattern, _args));
  }

  /**
   * Method to add a warn message to the logger.
   *
   * @param _marker         Marker
   * @param _text           message
   * @param _e              Throwable to add
   *
   */
  public void warn(final Marker _marker,
                   final String _text,
                   final Throwable _e) {
    warn(_text, _e);
  }

  /**
   * Method to add a warn message to the logger.
   *
   * @param _marker         Marker
   * @param _messagePattern message
   * @param _arg1           object to add
   * @param _arg2           object to add
   */
  public void warn(final Marker _marker,
                   final String _messagePattern,
                   final Object _arg1,
                   final Object _arg2) {
    warn(format(_messagePattern, _arg1, _arg2));
  }
}
