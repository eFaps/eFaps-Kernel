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

package org.efaps.admin.program.esjp;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class extends the ClassLoader of java, to be able to load Classes on
 * demand from the eFaps Database.
 *
 * @author jmox
 *
 */
public class EFapsClassLoader extends ClassLoader {
  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(EFapsClassLoader.class);

  /**
   * should the Class be kept in a local Cache
   */
  private static boolean                   HOLDCLASSESINCACHE = false;

  /**
   * holds all allready loaded Classes
   */
  private static final Map<String, byte[]> LOADEDCLASSES      = new HashMap<String, byte[]>();

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
  @Override
  public Class<?> findClass(String name) {

    byte[] b = getLoadedClasse(name);
    if (b == null) {
      b = loadClassData(name);
    }
    return defineClass(name, b, 0, b.length);
  }

  /**
   * Loads the wanted Resource with the EFapsResourceStore into a byte-Array to
   * pass it on to findClass
   *
   * @param _resourceName
   *          name of the Resource to load
   * @return byte[] containing the compiled javaclass
   */
  public byte[] loadClassData(final String _resourceName) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Loading Class '" + _resourceName + "' from Database");
    }
    final byte[] x = new EFapsResourceStore(new Compiler()).read(_resourceName);

    if (x != null && HOLDCLASSESINCACHE) {
      LOADEDCLASSES.put(_resourceName, x);
    }
    return x;

  }

  /**
   * get the Binary Class sored inthe lokal Cache
   *
   * @param _resourceName
   *          Name of the Class
   * @return binary Class, null if not in Cache
   */
  public byte[] getLoadedClasse(String _resourceName) {

    return LOADEDCLASSES.get(_resourceName);
  }
}
