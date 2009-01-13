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

import static org.efaps.admin.EFapsClassNames.ADMIN_PROGRAM_JAVA;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkin;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
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
  private static final String ENCODING = "UTF8";

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Java source code itself.
   */
  private final StringBuilder code = new StringBuilder();

  /**
   * URL of the source file in file system (or in jar, ...).
   */
  private final URL url;

  /**
   * Name of the class in eFaps.
   *
   * @see #getClassName
   */
  private final String className;

  /**
   * eFaps UUID of the class.
   */
  private final UUID eFapsUUID;

  /**
   * eFaps revision of the class.
   */
  private final String revision;

  /////////////////////////////////////////////////////////////////////////////
  // constructor / destructor

  /**
   * Constructor used to read the source code from given URL and extract the
   * class name.
   *
   * @param _url    url to the ESJP source code
   * @see #readCode
   * @see #evalClassName
   * @throws EFapsException on error
   */
  public ESJPImporter(final URL _url) throws EFapsException {
    this.url = _url;
    readCode();
    this.className = evalClassName();
    this.eFapsUUID = evalUUID();
    this.revision = evalRevision();
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
      final InputStream is
           = new ByteArrayInputStream(this.code.toString().getBytes(ENCODING));
      final Checkin checkin = new Checkin(_instance);
      checkin.executeWithoutAccessCheck(this.className,
                                        is,
                                        this.code.length());
    } catch (final UnsupportedEncodingException e) {
      throw new EFapsException(getClass(),
                               "updateDB.UnsupportedEncodingException",
                               e);
    }
  }

  /**
   * Searches for the given Java class name in eFaps. If exists, the instance
   * is returned.
   *
   * @return found instance (or null if not found)
   * @throws EFapsException if search query could not be executed
   * @see #className
   */
  public Instance searchInstance() throws EFapsException  {
    Instance instance = null;

    final Type esjpType = Type.get(ADMIN_PROGRAM_JAVA);
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(esjpType.getName());
    query.addWhereExprEqValue("Name", this.className);
    query.addSelect("OID");
    query.executeWithoutAccessCheck();
    if (query.next()) {
      instance = new Instance((String) query.get("OID"));
    }
    query.close();

    return instance;
  }

  /**
   *
   * @throws EFapsException on error
   * @see #searchInstance
   * @see #createInstance
   * @see #updateDB
   */
  public void execute() throws EFapsException  {
    Instance instance = searchInstance();

    if (instance == null)  {
      instance = createInstance();
    }

    updateDB(instance);
  }

  /**
   * Creates an instance of an ESJP in eFaps for given name.
   *
   * @return new created instance
   * @throws EFapsException on error
   */
  protected Instance createInstance() throws EFapsException  {
    final Type esjpType = Type.get(ADMIN_PROGRAM_JAVA);
    final Insert insert = new Insert(esjpType);
    insert.add("Name", this.className);
    if (this.getEFapsUUID() != null)  {
      insert.add("UUID", this.getEFapsUUID().toString());
    }
    insert.execute();

    return insert.getInstance();
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
    } catch (final IOException e)  {
      throw new EFapsException(getClass(),
                               "readCode.IOException",
                               e,
                               this.url.toString());
    }
  }

  /**
   * This Method extracts the package name and sets the name of this Java
   * definition (the name is the package name together with the name of the
   * file excluding the <code>.java</code>).
   *
   * @return classname of the esjp
   */
  protected String evalClassName() {
    final String urlPath = this.url.getPath();
    String name = urlPath.substring(urlPath.lastIndexOf('/') + 1);
    name = name.substring(0, name.lastIndexOf('.'));

    // regular expression for the package name
    final Pattern pckPattern = Pattern.compile("package +[^;]+;");
    final Matcher pckMatcher = pckPattern.matcher(this.code);
    if (pckMatcher.find()) {
      final String pkg = pckMatcher.group()
                                   .replaceFirst("^(package) +", "")
                                   .replaceFirst(";$", "");
      name = pkg + "." + name;
    }
    return name;
  }

  /**
   * This Method extracts the UUID from the esjp.
   *
   * @return UUID of the esjp
   */
  protected UUID evalUUID() {
    UUID uuid = null;

    final Pattern uuidPattern =
                 Pattern.compile("@EFapsUUID ?\\( ?\\\"[0-9a-z\\-]*\\\" ?\\)");
    final Matcher uuidMatcher = uuidPattern.matcher(this.code);
    if (uuidMatcher.find()) {
      final String uuidStr = uuidMatcher.group()
                                    .replaceFirst("^@EFapsUUID ?\\( ?\\\"", "")
                                    .replaceFirst("\\\" ?\\)", "");
      uuid = UUID.fromString(uuidStr);
    }

    return uuid;
  }

  /**
   * This Method extracts the Revision from the esjp.
   *
   * @return Revision of the esjp
   */
  protected String evalRevision() {
    String ret = null;
    final Pattern revisionPattern =
                        Pattern.compile("@EFapsRevision ?\\( ?\\\".*\\\" ?\\)");
    final Matcher revisionMatcher = revisionPattern.matcher(this.code);
    if (revisionMatcher.find()) {
      ret = revisionMatcher.group()
                                 .replaceFirst("^@EFapsRevision ?\\( ?\\\"", "")
                                 .replaceFirst("\\\" ?\\)", "");
    }
    return ret;
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
}
