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

package org.efaps.admin.program.jasperreport;

import groovy.lang.GroovyClassLoader;
import groovyjarjarasm.asm.ClassVisitor;
import groovyjarjarasm.asm.ClassWriter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.compilers.JRGroovyCompiler;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JRCompilationUnit;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;

/**
 * Calculator compiler that uses groovy to compile expressions. It is used due
 * to the reason that the Classloader inside groovy must be set to use the
 * classpath from the maven targets.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JasperGroovyCompiler extends JRGroovyCompiler
{

    /**
     * @see net.sf.jasperreports.compilers.JRGroovyCompiler#compileUnits(net.sf.jasperreports.engine.design.JRCompilationUnit[],
     *      java.lang.String, java.io.File)
     * @param _units        compilation units for Jasper
     * @param _classpath    classpath
     * @param _tempDirFile  directory for the temporary files
     * @return null
     * @throws JRException on error during compilation
     */
    @Override
    protected String compileUnits(final JRCompilationUnit[] _units,
                                  final String _classpath,
                                  final File _tempDirFile)
        throws JRException
    {
        final CompilerConfiguration config = new CompilerConfiguration();
        config.setClasspath(_classpath);
        config.setVerbose(true);
        final GroovyClassLoader loader = new GroovyClassLoader(JasperGroovyCompiler.class.getClassLoader(), config,
                        true);
        final CompilationUnit unit = new CompilationUnit(loader);

        for (int i = 0; i < _units.length; i++) {
            unit.addSource("calculator_" + _units[i].getName(), new ByteArrayInputStream(_units[i].getSourceCode()
                            .getBytes()));
        }

        final ClassCollector collector = new ClassCollector();
        unit.setClassgenCallback(collector);
        try {
            unit.compile(Phases.CLASS_GENERATION);
        } catch (final CompilationFailedException e) {
            throw new JRException("Errors were encountered when compiling report expressions class file:\n"
                            + e.toString());
        }

        if (collector.classes.size() < _units.length) {
            throw new JRException("Too few groovy class were generated.");
        } else if (collector.classCount > _units.length) {
            throw new JRException("Too many groovy classes were generated.\n"
                      + "Please make sure that you don't use Groovy features such as closures "
                      + "that are not supported by this report compiler.\n");
        }

        for (int i = 0; i < _units.length; i++) {
            _units[i].setCompileData((Serializable) collector.classes.get(_units[i].getName()));
        }

        return null;
    }

    /**
     * Class is a exact copy of the inner class of this parent class. This is
     * done due to private definition.
     */
    private static class ClassCollector extends CompilationUnit.ClassgenCallback
    {
        /**
         * Name to classes.
         */
        private final Map<String, Object> classes = new HashMap<String, Object>();

        /**
         * Count of classes.
         */
        private int classCount;

        /**
         * @see org.codehaus.groovy.control.CompilationUnit.ClassgenCallback#call(groovyjarjarasm.asm.ClassVisitor, org.codehaus.groovy.ast.ClassNode)
         * @param _writer   writer
         * @param _node     node
         * @throws CompilationFailedException on error
         */
        @Override
        public void call(final ClassVisitor _writer,
                         final ClassNode _node)
        {
            this.classCount++;
            final String name = _node.getName();
            if (!this.classes.containsKey(name)) {
                final byte[] bytes = ((ClassWriter) _writer).toByteArray();
                this.classes.put(name, bytes);
            }
        }
    }
}
