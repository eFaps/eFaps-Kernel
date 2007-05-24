/*
 * Copyright 2003 - 2007 The eFaps Team
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

package org.efaps.admin.program.java;

/**
 * This Class extends the ClassLoader of java, to be able to load Classes on
 * demand from the eFaps Database.
 * 
 * @author jmo
 * 
 */
public class EFapsClassLoader extends ClassLoader {

  /**
   * Constructor setting the Parent of the EFapsClassLoader in ClassLoader
   * 
   * @param _parentClassLoader
   *          the Parent of the this EFapsClassLoader
   */
  public EFapsClassLoader(ClassLoader _parentClassLoader) {
    super(_parentClassLoader);

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.ClassLoader#findClass(java.lang.String)
   */
  public Class<?> findClass(String name) {
    final byte[] b = loadClassData(name);

    return defineClass(name, b, 0, b.length);
  }

  /**
   * Loads the wanted Resource with the EFapsResourceStore into a byte-Array to
   * pass it than on to findClass
   * 
   * @param _resourceName
   *          name of the Resource to load
   * @return byte[] containing the compiled javaclass
   */
  public byte[] loadClassData(final String _resourceName) {

    byte[] x = new EFapsResourceStore(new Compiler()).read(_resourceName);

    return x;

  }
}
