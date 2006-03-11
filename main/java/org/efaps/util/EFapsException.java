/*
 * Copyright 2005 The eFaps Team
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
 */

package org.efaps.util;

import org.efaps.db.Context;

/**
 * The class is the exception class used to throw exception. This exceptions
 * are shown in the user interface (web browser). The exception text stands
 * in the properties. The key value in the property is the name of the class
 * ({@link #className}) plus the id ({@link #id}) plus the strings <i>.Id</i>,
 * <i>.Error</i> and <i>.Action</i> to show the user a internationlised
 * description of the exception.
 */
public class EFapsException extends Exception  {

  public EFapsException(Class _className, String _id, Object... _args)  {
    super("error in "+_className.getName()+"("+_id+","+_args+")");
    setId(_id);
    setClassName(_className);
    if (_args.length>0 && _args[0] instanceof Throwable)  {
      setThrowable((Throwable)_args[0]);
    }
    setArgs(_args);
  }


  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the class name where the exception occurs.
   *
   * @see #getClassName
   * @see #setClassName
   */
  private Class className = null;

  /**
   * The instance variable stores the id (key) of the exception.
   *
   * @see #getId
   * @see #setId
   */
  private String id = null;

  /**
   * The instance variable stores the thrown exception
   *
   * @see #getThrowable
   * @see #setThrowable
   */
  private Throwable throwable = null;

  /**
   * The instance variable stores the arguments replaced in the error text.
   *
   * @see #getArgs
   * @see #setArgs
   */
  private Object[] args = null;

  /////////////////////////////////////////////////////////////////////////////

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
   * This is the setter method for instance variable {@link #className}.
   *
   * @param _className new value for instance variable {@link #className}
   * @see #className
   * @see #getClassName
   */
  private void setClassName(Class _className)  {
    this.className = _className;
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
   * This is the setter method for instance variable {@link #id}.
   *
   * @param _id new value for instance variable {@link #id}
   * @see #id
   * @see #getId
   */
  private void setId(String _id)  {
    this.id = _id;
  }

  /**
   * This is the getter method for instance variable {@link #throwable}.
   *
   * @return value of instance variable {@link #throwable}
   * @see #throwable
   * @see #setThrowable
   */
  public Throwable getThrowable()  {
    return this.throwable;
  }

  /**
   * This is the setter method for instance variable {@link #throwable}.
   *
   * @param _throwable new value for instance variable {@link #throwable}
   * @see #throwable
   * @see #getThrowable
   */
  private void setThrowable(Throwable _throwable)  {
    this.throwable = _throwable;
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

  /**
   * This is the setter method for instance variable {@link #args}.
   *
   * @param _args new value for instance variable {@link #args}
   * @see #args
   * @see #getArgs
   */
  private void setArgs(Object[] _args)  {
    this.args = _args;
  }
}