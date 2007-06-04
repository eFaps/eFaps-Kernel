/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.maven;

import org.apache.commons.logging.Log;

/**
 * This Class converts the Maven internal Logger to an apache Commmons logger.<br/>
 * Therefor the standart logger definition used in eFaps can be used for the
 * Maven Mojo also.
 * 
 * @author tmo
 * @version $Id$
 * 
 */
public class Maven2CommonsLog implements Log {

  static org.apache.maven.plugin.logging.Log logger;

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  /**
   * 
   */
  // public Maven2CommonsLog(final org.apache.maven.plugin.logging.Log _logger)
  // {
  // this.logger = _logger;
  // }
  public Maven2CommonsLog(final String _logClass) {
  }

  // ///////////////////////////////////////////////////////////////////////////
  // debug

  // Is debug logging currently enabled?
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  // Log a message with debug log level.
  public void debug(final Object _message) {
    logger.debug(_message.toString());
  }

  // Log an error with debug log level.
  public void debug(final Object _message, final Throwable _error) {
    logger.debug(_message.toString(), _error);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // error

  // Is error logging currently enabled?
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  // Log a message with error log level.
  public void error(final Object _message) {
    logger.error(_message.toString());
  }

  // Log an error with error log level.
  public void error(final Object _message, final Throwable _error) {
    logger.error(_message.toString(), _error);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // fatal => error

  // Is fatal logging currently enabled?
  public boolean isFatalEnabled() {
    return logger.isErrorEnabled();
  }

  // Log a message with fatal log level.
  public void fatal(final Object _message) {
    logger.error(_message.toString());
  }

  // Log an error with fatal log level.
  public void fatal(final Object _message, final Throwable _error) {
    logger.error(_message.toString(), _error);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // info

  // Is info logging currently enabled?
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  // Log a message with info log level.
  public void info(final Object _message) {
    logger.info(_message.toString());
  }

  // Log an error with info log level.
  public void info(final Object _message, final Throwable _error) {
    logger.info(_message.toString(), _error);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // trace => debug

  // Is trace logging currently enabled?
  public boolean isTraceEnabled() {
    return logger.isDebugEnabled();
  }

  // Log a message with trace log level.
  public void trace(final Object _message) {
    logger.debug(_message.toString());
  }

  // Log an error with trace log level.
  public void trace(final Object _message, final Throwable _error) {
    logger.debug(_message.toString(), _error);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // warn

  // Is warn logging currently enabled?
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  // Log a message with warn log level.
  public void warn(final Object _message) {
    logger.warn(_message.toString());
  }

  // Log an error with warn log level.
  public void warn(final Object _message, final Throwable _error) {
    logger.warn(_message.toString(), _error);
  }
}
