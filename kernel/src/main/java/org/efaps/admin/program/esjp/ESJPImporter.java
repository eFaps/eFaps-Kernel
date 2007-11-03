/*
 * Copyright 2003-2007 The eFaps Team
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.efaps.db.Checkin;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * Java source code could be imported into eFaps as ESJP with this class. The
 * class does not need any XML update files and could be called directly.
 *
 * @author tmo
 * @version $Id$
 * @todo encoding from java files!
 */
public class ESJPImporter {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Defines the encoding of the ESJP source code within eFaps.
   */
  private final static String ENCODING = "UTF8";

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Java source code itself.
   */
  final StringBuilder code = new StringBuilder();

  /**
   * URL of the source file in file system (or in jar, ...).
   */
  final URL url;

  /**
   * Name of the class in eFaps.
   *
   * @see #getClassName
   */
  final String className;

  /////////////////////////////////////////////////////////////////////////////
  // constructor / destructor

  public ESJPImporter(final URL _url) throws EFapsException  {
    this.url = _url;
    readCode();
    this.className = evalClassName();
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Stores the read source code in eFaps. This is done with a checkin.
   *
   * @param _instance   instance (object id) of Java program
   * @throws EFapsException   if Java code in eFaps could not updated or
   *                          the source code could not encoded
   */
  public void updateDB(final Instance _instance) throws EFapsException  {
    try {
      final InputStream is = new ByteArrayInputStream(this.code.toString().getBytes(ENCODING));
      final Checkin checkin = new Checkin(_instance);
      checkin.executeWithoutAccessCheck(this.className, 
                                        is,
                                        this.code.length());
    } catch (UnsupportedEncodingException e) {
      throw new EFapsException(getClass(),
                               "updateDB.UnsupportedEncodingException",
                               e);
    }
  }

  /**
   * Read the code from the file defined through {@link #url}.
   *
   * @throws EFapsException if the source code could not read from url
   * @see #url
   */
  protected void readCode() throws EFapsException  {
    try  {
      final char[] buf = new char[1024];
  
      final InputStream input = this.url.openStream();
  
      final Reader reader = new InputStreamReader(input);
      int length;
      while ((length = reader.read(buf)) > 0) {
        this.code.append(buf, 0, length);
      }
      reader.close();
    } catch (IOException e)  {
      throw new EFapsException(getClass(),
                               "readCode.IOException",
                               e,
                               this.url.toString());
    }
  }

  /**
   * This Method extracts the package name and sets the name of this Java
   * definition (the name is the package name together with the name of the
   * file exluding the <code>.java</code>).
   */
  protected String evalClassName() {

    final String urlPath = this.url.getPath();
    String name = urlPath.substring(urlPath.lastIndexOf('/') + 1);
    name = name.substring(0, name.lastIndexOf('.'));

    // regular expression for the package name
    final Pattern pattern = Pattern.compile("package +[^;]+;");
    final Matcher matcher = pattern.matcher(this.code);
    if (matcher.find()) {
      String pkg = matcher.group();
      pkg = pkg.replaceFirst("^(package) +", "");
      pkg = pkg.replaceFirst(";$", "");
      name = pkg + "." + name;
    }
    return name;
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter / setter methods

  /**
   * Getter method for instance variable {@link #className}.
   *
   * @return value of instance variable className
   * @see #className
   */
  public String getClassName() {
    return this.className;
  }
}
