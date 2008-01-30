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

package org.efaps.util.cache;

/**
 * The <code>CacheReloadException</code> class is an exception is thrown, if 
 * a cache implementing {@link CacheReloadInterface} could not be reloaded.
 *
 * @author tmo
 * @version $Id$
 * @see CacheReloadInterface
 */
public class CacheReloadException extends Exception  {

  /**
   * 
   */
  private static final long serialVersionUID = -2388991706315797381L;

  /**
   * Constructs a new exception with the specified detail message. The cause is 
   * not initialized, and may subsequently be initialized by a call to 
   * {@link Throwable.initCause(Throwable)}.
   *
   * @param _message  the detail message (which is saved for later retrieval by
   *                  the {@link Throwable.getMessage} method).
   */
  public CacheReloadException(final String _message) {
    this(_message, null);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   * <br/>
   * Note that the detail message associated with cause is not automatically 
   * incorporated in this exception's detail message.
   *
   * @param _message  the detail message (which is saved for later retrieval by
   *                  the {@link Throwable.getMessage} method).
   * @param _cause    the cause (which is saved for later retrieval by the 
   *                  {@link Throwable.getCause} method). (A null value is 
   *                  permitted, and indicates that the cause is nonexistent or 
   *                  unknown.)
   */
  public CacheReloadException(final String _message, final Throwable _cause) {
    super(_message, _cause);
  }
}
