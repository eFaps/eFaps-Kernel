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

package org.efaps.util;

/**
 * The class is the exception class used to throw exception. This exceptions
 * are shown in the user interface (web browser). The exception text stands
 * in the properties. The key value in the property is the name of the class
 * ({@link #className}) plus the id ({@link #id}) plus the strings <i>.Id</i>,
 * <i>.Error</i> and <i>.Action</i> to show the user a internationlised
 * description of the exception.
 *
 * @author tmo
 * @version $Id$
 */
public class EFapsException extends Exception  {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The instance variable stores the class name where the exception occurs.
   *
   * @see #getClassName
   * @see #setClassName
   */
  private final Class className;

  /**
   * The instance variable stores the id (key) of the exception.
   *
   * @see #getId
   * @see #setId
   */
  private final String id;

  /**
   * The instance variable stores the arguments replaced in the error text.
   *
   * @see #getArgs
   * @see #setArgs
   */
  private final Object[] args;

  /////////////////////////////////////////////////////////////////////////////
  // constructor / destructor

  public EFapsException(Class _className, String _id, Object... _args)  {
    super("error in "+_className.getName()+"("+_id+","+_args+")");
    this.id = _id;
    this.className = _className;
    if ((_args.length > 0) && (_args[0] instanceof Throwable))  {
      initCause((Throwable) _args[0]);
    }
    this.args = _args;
  }

  /**
   * The cause is returned. The method only exists to support old interface
   * (in pre Java 1.4 it was not possible to store a cause in class
   * {@link java.lang.Throwable}).
   *
   * @see java.lang.Throwable#getCause()
   * @deprecated
   */
  public Throwable getThrowable()  {
    return getCause();
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the getter method for instance variable {@link #className}.
   *
   * @return value of instance variable {@link #className}
   * @see #className
   * @see #setClassName
   */
  public Class getClassName()  {
    return this.className;
  }


  /**
   * This is the getter method for instance variable {@link #id}.
   *
   * @return value of instance variable {@link #id}
   * @see #id
   * @see #setId
   */
  public String getId()  {
    return this.id;
  }

  /**
   * This is the getter method for instance variable {@link #args}.
   *
   * @return value of instance variable {@link #args}
   * @see #args
   * @see #setArgs
   */
  public Object[] getArgs()  {
    return this.args;
  }
}