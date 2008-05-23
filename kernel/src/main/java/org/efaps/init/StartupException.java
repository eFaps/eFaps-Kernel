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

package org.efaps.init;

/**
 * Exception which is thrown if the database connection or store binding could
 * not initialized correctly.
 *
 * @author tmo
 * @version $Id$
 */
@SuppressWarnings("serial")
public class StartupException extends Exception
{
  /**
   * Constructs a new exception with the specified detail message without
   * cause.
   *
   * @param  message the detail message
   */
  public StartupException(final String _message)
  {
    this(_message, null);
  }

  /**
   * Constructs a new exception with the specified detail message an
   * cause.
   *
   * @param  message the detail message
   * @param  cause the cause (A <code>null</code> value is permitted, and
   *         indicates that the cause is nonexistent or unknown.)
   */
  public StartupException(final String _message,
                          final Throwable _cause)
  {
    super(_message, _cause);
  }
}
