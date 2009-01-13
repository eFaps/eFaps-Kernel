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

package org.efaps.admin.program;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.UUID;

import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * Class used to import programs into eFaps.
 *
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractProgramImporter {

  /**
   * URL of the source file in file system (or in jar, ...).
   */
  private final URL url;

  /**
   * Source code itself.
   */
  private final StringBuilder code = new StringBuilder();

  /**
   * eFaps UUID of the program.
   */
  private final UUID eFapsUUID;

  /**
   * eFaps revision of the program.
   */
  private final String revision;

  /**
   * Name of the program in eFaps.
   *
   * @see #getClassName
   */
  private final String programName;

  /**
   * Constructor used to read the source code from given URL and extract the
   * class name.
   *
   * @param _url    url to the ESJP source code
   * @see #readCode
   * @see #evalClassName
   * @throws EFapsException on error
   */
  public AbstractProgramImporter(final URL _url) throws EFapsException {
    this.url = _url;
    readCode();
    this.programName = evalProgramName();
    this.eFapsUUID = evalUUID();
    this.revision = evalRevision();
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

      final InputStream input = this.getUrl().openStream();

      final Reader reader = new InputStreamReader(input);
      int length;
      while ((length = reader.read(buf)) > 0) {
        this.getCode().append(buf, 0, length);
      }
      reader.close();
    } catch (final IOException e)  {
      throw new EFapsException(getClass(),
                               "readCode.IOException",
                               e,
                               this.getUrl().toString());
    }
  }

  /**
   * This Method extracts the Name from the program.
   *
   * @return Name of the program
   */
  protected abstract String evalProgramName();


  /**
   * This Method extracts the UUID from the program.
   *
   * @return UUID of the program
   */
  protected abstract UUID evalUUID();


  /**
   * This Method extracts the Revision from the program.
   *
   * @return Revision of the program
   */
  protected abstract String evalRevision();

  /**
   * Method to search the Instance which is imported.
   * @return Instance of the imported program
   * @throws EFapsException on error
   */
  public abstract Instance searchInstance() throws EFapsException;

  /**
   * Getter method for instance variable {@link #url}.
   *
   * @return value for instance variable {@link #url}
   */
  public URL getUrl() {
    return this.url;
  }


  /**
   * Getter method for instance variable {@link #code}.
   *
   * @return value for instance variable {@link #code}
   */
  public StringBuilder getCode() {
    return this.code;
  }

  /**
   * Getter Method for instance variable {@link #eFapsUUID}.
   *
   * @return value for instance variable {@link #eFapsUUID}
   */
  public UUID getEFapsUUID() {
    return this.eFapsUUID;
  }

  /**
   * Getter Method for instance variable {@link #revision}.
   *
   * @return value for instance variable {@link #revision}
   */
  public String getRevision() {
    return this.revision;
  }

  /**
   * Getter method for instance variable {@link #programName}.
   *
   * @return value of instance variable className
   * @see #programName
   */
  public String getProgramName() {
    return this.programName;
  }
}
