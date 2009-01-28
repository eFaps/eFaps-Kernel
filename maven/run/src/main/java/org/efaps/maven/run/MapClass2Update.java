/*
 * * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.maven.run;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.maven.plugin.Mojo;

/**
 * @author tmo
 * @version $Id$
 */
abstract class MapClass2Update {

  private final static Map<String, MapClass2Update> MAPPING
      = new HashMap<String, MapClass2Update>();

  /**
   * Returns the mapping for given class name.
   *
   * @param _className  name of searched class
   * @return instance of this class or <code>null</code> if not found
   */
  public static MapClass2Update get(final String _className)  {
    return MAPPING.get(_className);
  }

  public abstract void setObjectParameter(final Mojo _mojo,
                                          final Field _field,
                                          final XMLConfiguration _config,
                                          final Plugin.Parameter _param) 
                  throws IllegalArgumentException, IllegalAccessException;

  static {
    MAPPING.put("int", new MapClass2Update() {
      public void setObjectParameter(final Mojo _mojo,
                                     final Field _field,
                                     final XMLConfiguration _config,
                                     final Plugin.Parameter _param) 
      throws IllegalArgumentException, IllegalAccessException  {
        _field.setInt(_mojo, _config.getInt(_param.getParamName()));
      }
    });
    MAPPING.put("java.lang.String", new MapClass2Update() {
      public void setObjectParameter(final Mojo _mojo,
                                     final Field _field,
                                     final XMLConfiguration _config,
                                     final Plugin.Parameter _param) 
      throws IllegalArgumentException, IllegalAccessException  {
        _field.set(_mojo, _config.getString(_param.getParamName()));
      }
    });
    MAPPING.put("java.util.Properties", new MapClass2Update() {
      public void setObjectParameter(final Mojo _mojo,
                                     final Field _field,
                                     final XMLConfiguration _config,
                                     final Plugin.Parameter _param) 
      throws IllegalArgumentException, IllegalAccessException  {
        final BaseConfiguration baseConf = new BaseConfiguration();
        baseConf.setProperty(_param.getParamName(),
                             _config.getString(_param.getParamName()));
        _field.set(_mojo, baseConf.getProperties(_param.getParamName()));
      }
    });
    MAPPING.put("java.util.List<java.lang.String>", new MapClass2Update() {
      public void setObjectParameter(final Mojo _mojo,
                                     final Field _field,
                                     final XMLConfiguration _config,
                                     final Plugin.Parameter _param) 
      throws IllegalArgumentException, IllegalAccessException  {
        final BaseConfiguration baseConf = new BaseConfiguration();
        baseConf.setProperty(_param.getParamName(),
                             _config.getString(_param.getParamName()));
        _field.set(_mojo, baseConf.getList(_param.getParamName()));
      }
    });
  }
}
