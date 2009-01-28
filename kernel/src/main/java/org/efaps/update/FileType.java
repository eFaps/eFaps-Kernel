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

package org.efaps.update;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.efaps.update.program.CSSUpdate;
import org.efaps.update.program.JavaScriptUpdate;
import org.efaps.update.program.JavaUpdate;
import org.efaps.update.program.XSLUpdate;

/**
 * @author tmo
 * @version $Id$
 */
public enum FileType
{
  JAVA("source-java", "java"),
  JS("source-js",     "js"),
  CSS("source-css",   "css"),
  XML("install-xml",  "xml"),
  XSL("source-xsl",   "xsl", "xslt");

  private final static class Mapper
  {
    /**
     * Mapping between an extensions and the related file type.
     */
    private final static Map<String, FileType> EXT2FILETYPE = new HashMap<String, FileType>();

    /**
     * Mapping between a type and the related file type.
     */
    private final static Map<String, FileType> TYPE2FILETYPE = new HashMap<String, FileType>();

    static
    {
//      FileType.CSS.clazzes.add(CSSUpdate.class);
//      FileType.JAVA.clazzes.add(JavaUpdate.class);
//      FileType.JS.clazzes.add(JavaScriptUpdate.class);
/*      FileType.XML.clazzes.add(AccessSetUpdate.class);
      FileType.XML.clazzes.add(AccessTypeUpdate.class);
      FileType.XML.clazzes.add(CommandUpdate.class);
      FileType.XML.clazzes.add(FormUpdate.class);
      FileType.XML.clazzes.add(ImageUpdate.class);
      FileType.XML.clazzes.add(JAASSystemUpdate.class);
      FileType.XML.clazzes.add(MenuUpdate.class);
      FileType.XML.clazzes.add(RoleUpdate.class);
      FileType.XML.clazzes.add(SQLTableUpdate.class);
      FileType.XML.clazzes.add(SearchUpdate.class);
      FileType.XML.clazzes.add(SystemAttributeUpdate.class);
      FileType.XML.clazzes.add(TableUpdate.class);
      FileType.XML.clazzes.add(TypeUpdate.class);
      FileType.XML.clazzes.add(WebDAVUpdate.class);
      FileType.XSL.clazzes.add(XSLUpdate.class);
*/    }
  }

  /**
   * Internal type of the file (used e.g. from the eFaps Maven installer).
   */
  public final String type;
  /**
   * All extensions of this file type.
   */
  public final Set<String> extensions = new HashSet<String>();

  /**
   * Set of all update classes.
   */
  public final Set<Class<? extends AbstractUpdate>> clazzes = new HashSet<Class<? extends AbstractUpdate>>();

  /**
   * File type enum constructor.
   *
   * @param _type         file type
   * @param _extensions   extensions of the file type
   */
  private FileType(final String _type,
                   final String... _extensions)
  {
    this.type = _type;
    for (final String extension : _extensions)  {
      this.extensions.add(extension);
      Mapper.EXT2FILETYPE.put(extension, this);
    }
    Mapper.TYPE2FILETYPE.put(_type, this);
    if ("source-java".equals(_type))  {
      this.clazzes.add(JavaUpdate.class);
    } else if ("source-js".equals(_type))  {
      this.clazzes.add(JavaScriptUpdate.class);
    } else if ("source-css".equals(_type))  {
      this.clazzes.add(CSSUpdate.class);
    } else if ("source-xsl".equals(_type))  {
      this.clazzes.add(XSLUpdate.class);
    }
  }

  /**
   * Depending on the extension the file type is returned.
   *
   * @param _extension  extension for which the file type is searched
   * @return file type instance for given extension
   */
  public static FileType getFileTypeByExensione(final String _extension)
  {
    return Mapper.EXT2FILETYPE.get(_extension);
  }

  /**
   * Depending on the type the file type is returned.
   *
   * @param _type   type for which the file type is searched
   * @return file type instance for given type
   */
  public static FileType getFileTypeByType(final String _type)
  {
    return Mapper.TYPE2FILETYPE.get(_type);
  }
}
