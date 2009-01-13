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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.AbstractProgramImporter;
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
public class ESJPImporter extends AbstractProgramImporter {
  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Defines the encoding of the ESJP source code within eFaps.
   */
  private static final String ENCODING = "UTF8";

  /////////////////////////////////////////////////////////////////////////////
  // instance variables



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
    super(_url);
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
           = new ByteArrayInputStream(getCode().toString().getBytes(ENCODING));
      final Checkin checkin = new Checkin(_instance);
      checkin.executeWithoutAccessCheck(getProgramName(),
                                        is,
                                        getCode().length());
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
   * @see #programName
   */
  public Instance searchInstance() throws EFapsException  {
    Instance instance = null;

    final Type esjpType = Type.get(ADMIN_PROGRAM_JAVA);
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(esjpType.getName());
    query.addWhereExprEqValue("Name", getProgramName());
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
    insert.add("Name", getProgramName());
    if (this.getEFapsUUID() != null)  {
      insert.add("UUID", this.getEFapsUUID().toString());
    }
    insert.execute();

    return insert.getInstance();
  }

  /**
   * This Method extracts the package name and sets the name of this Java
   * definition (the name is the package name together with the name of the
   * file excluding the <code>.java</code>).
   *
   * @return classname of the esjp
   */
  @Override
  protected String evalProgramName() {
    final String urlPath = getUrl().getPath();
    String name = urlPath.substring(urlPath.lastIndexOf('/') + 1);
    name = name.substring(0, name.lastIndexOf('.'));

    // regular expression for the package name
    final Pattern pckPattern = Pattern.compile("package +[^;]+;");
    final Matcher pckMatcher = pckPattern.matcher(this.getCode());
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
  @Override
  protected UUID evalUUID() {
    UUID uuid = null;

    final Pattern uuidPattern =
                 Pattern.compile("@EFapsUUID ?\\( ?\\\"[0-9a-z\\-]*\\\" ?\\)");
    final Matcher uuidMatcher = uuidPattern.matcher(this.getCode());
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
  @Override
  protected String evalRevision() {
    String ret = null;
    final Pattern revisionPattern =
                        Pattern.compile("@EFapsRevision ?\\( ?\\\".*\\\" ?\\)");
    final Matcher revisionMatcher = revisionPattern.matcher(this.getCode());
    if (revisionMatcher.find()) {
      ret = revisionMatcher.group()
                                 .replaceFirst("^@EFapsRevision ?\\( ?\\\"", "")
                                 .replaceFirst("\\\" ?\\)", "");
    }
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter / setter methods


}
