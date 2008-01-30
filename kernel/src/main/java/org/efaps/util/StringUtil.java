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

package org.efaps.util;

/**
 *
 * @author tmo
 * @version $Id$
 */
public class StringUtil  {

  /**
   * Replace in <i>_text</i> the string <i>_replace</i> with <i>_value</i>.
   *
   * @param _original Text where to replace
   * @param _what     old replace string
   * @param _replace  new value string
   */
  public static String replace(String _original, String _what, String _replace)  {
//System.out.println("StringUtil.replace("+_original+","+_what+","+_replace+")");
    if (_replace!=null && _what!=null && _original!=null)  {
      int lengthReplace = _replace.length();
      int lengthWhat = _what.length();
      int index = -lengthReplace;
      while ((index=_original.indexOf(_what, index+lengthReplace))>=0)  {
        _original = _original.substring(0, index) +
                    _replace+
                    _original.substring(index+lengthWhat);
      }
    }
    return _original;
  }
}