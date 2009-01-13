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

package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

import org.efaps.eclipse.logger.SLF4JOverEclipseConsole;
import org.efaps.eclipse.logger.SLF4JOverEclipseConsoleFactory;

/**
 * Class is used to bind the logger to the console.
 *
 * @author tmo
 * @version $Id$
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

  /**
   * Binder.
   */
  public static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

  /**
   * Name of the Logger Factrot class.
   */
  private static final String LOGGERFACTORYCLASSSTR
                                  = (SLF4JOverEclipseConsole.class).getName();

  /**
   * Logger Factory.
   */
  private final ILoggerFactory loggerFactory
                                      = new SLF4JOverEclipseConsoleFactory();

  /**
   * Getter Method for instance variable {@link #loggerFactory}.
   *
   * @see org.slf4j.spi.LoggerFactoryBinder#getLoggerFactory()
   * @return value of instance variable {@link #loggerFactory}.
   *
   */
  public ILoggerFactory getLoggerFactory() {
    return this.loggerFactory;
  }

  /**
   * Getter Method for variable {@link #LOGGERFACTORYCLASSSTR}.
   *
   * @see org.slf4j.spi.LoggerFactoryBinder#getLoggerFactoryClassStr()
   * @return value of variable {@link #LOGGERFACTORYCLASSSTR}.
   */
  public String getLoggerFactoryClassStr() {
    return LOGGERFACTORYCLASSSTR;
  }

}
