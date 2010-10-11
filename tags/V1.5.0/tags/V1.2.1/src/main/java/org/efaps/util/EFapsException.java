/*
 * Copyright 2003 - 2010 The eFaps Team
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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * The class is the exception class used to throw exception. This exceptions
 * are shown in the user interface (web browser). The exception text stands
 * in the properties. The key value in the property is the name of the class
 * ({@link #className}) plus the id ({@link #id}) plus the strings <i>.Id</i>,
 * <i>.Error</i> and <i>.Action</i> to show the user a internationalized
 * description of the exception.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EFapsException
    extends Exception
{
    /**
     * Unique identifier used to serialize this class.
     */
    private static final long serialVersionUID = 1906998311318776048L;

    /**
     * The instance variable stores the class name where the exception occurs.
     *
     * @see #getClassName()
     */
    private final Class<?> className;

    /**
     * The instance variable stores the id (key) of the exception.
     *
     * @see #getId()
     */
    private final String id;

    /**
     * The instance variable stores the arguments replaced in the error text.
     *
     * @see #getArgs()
     */
    private final Object[] args;

    /**
     * @param _className  name of class in which the exception is thrown
     * @param _id         id of the exception which is thrown
     * @param _args       argument arrays
     */
    public EFapsException(final Class<?> _className,
                          final String _id,
                          final Object... _args)
    {
        super("error in " + _className.getName() + "(" + _id + "," + _args + ")");
        this.id = _id;
        this.className = _className;
        if ((_args.length > 0) && (_args[0] instanceof Throwable))  {
            initCause((Throwable) _args[0]);
        }
        this.args = _args;
    }

    /**
     * @param _message      message of the exception
     * @param _cause        cause
     */
    public EFapsException(final String _message,
                          final Throwable _cause)
    {
        super(_message, _cause);
        if (_cause instanceof EFapsException) {
             final EFapsException cause = ((EFapsException) _cause);
             this.id = cause.getId();
             this.className = cause.getClassName();
             this.args = cause.getArgs();
        } else {
            this.id = null;
            this.className = null;
            this.args = null;
        }
    }

    /**
     * If a caused exception is a {@link SQLException}, also all next
     * exceptions of the {@link SQLException}'s are printed into the stack
     * trace.
     *
     * @param _stream <code>PrintStream</code> to use for output
     * @see #makeInfo() to get all information about this EFapsException
     */
    @Override()
    public void printStackTrace(final PrintStream _stream)
    {
        _stream.append(makeInfo());
        super.printStackTrace(_stream);
        if ((getCause() != null) && (getCause() instanceof SQLException))  {
            SQLException ex = (SQLException) getCause();
            ex = ex.getNextException();
            while (ex != null)  {
                _stream.append("Next SQL Exception is: ");
                ex.printStackTrace(_stream);
                ex = ex.getNextException();
            }
        }
    }

    /**
     * If a caused exception is a {@link SQLException}, also all next
     * exceptions of the {@link SQLException}'s are printed into the stack
     * trace.
     *
     * @param _writer <code>PrintWriter</code> to use for output
     * @see #makeInfo() to get all information about this EFapsException
     */
    @Override()
    public void printStackTrace(final PrintWriter _writer)
    {
        _writer.append(makeInfo());
        if (this.className != null)  {
            _writer.append("Thrown within class ").append(this.className.getName()).append('\n');
        }
        super.printStackTrace(_writer);
        if ((getCause() != null) && (getCause() instanceof SQLException))  {
            SQLException ex = (SQLException) getCause();
            ex = ex.getNextException();
            while (ex != null)  {
                _writer.append("Next SQL Exception is: ");
                ex.printStackTrace(_writer);
                ex = ex.getNextException();
            }
        }
    }

    /**
     * Prepares a string of all information of this EFapsException. The
     * returned string includes information about the class which throws this
     * exception, the exception id and all arguments.
     *
     * @return string representation about the EFapsException
     */
    protected String makeInfo()
    {
        final StringBuilder str = new StringBuilder();
        if (this.className != null)  {
            str.append("Thrown within class ").append(this.className.getName()).append('\n');
        }
        if (this.id != null)  {
            str.append("Id of Exception is ").append(this.id).append('\n');
        }
        if ((this.args != null) && (this.args.length > 0))  {
            str.append("Arguments are:\n");
            for (Integer index = 0; index < this.args.length; index++)  {
                final String arg = (this.args[index] == null)
                                   ? "null"
                                   : this.args[index].toString();
                str.append("\targs[").append(index.toString()).append("] = '").append(arg).append("'\n");
            }
        }
        return str.toString();
    }

    /**
     * This is the getter method for instance variable {@link #className}.
     *
     * @return value of instance variable {@link #className}
     * @see #className
     */
    public Class<?> getClassName()
    {
        return this.className;
    }

    /**
     * This is the getter method for instance variable {@link #id}.
     *
     * @return value of instance variable {@link #id}
     * @see #id
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * This is the getter method for instance variable {@link #args}.
     *
     * @return value of instance variable {@link #args}
     * @see #args
     */
    public Object[] getArgs()
    {
        return this.args;
    }
}
