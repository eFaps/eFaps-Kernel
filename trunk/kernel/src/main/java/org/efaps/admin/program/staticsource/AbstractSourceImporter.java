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

package org.efaps.admin.program.staticsource;

import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.efaps.admin.program.AbstractProgramImporter;
import org.efaps.util.EFapsException;

/**
 * Class used to import source programs into eFaps.
 *
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractSourceImporter extends AbstractProgramImporter {

  /**
   * STroed the name of the source to be extended.
   */
  private final String extendSource;

  /**
   * Constructor used to read the source code from given URL and extract the
   * class name.
   *
   * @param _url    url to the source code
   * @throws EFapsException on error
   */
  public AbstractSourceImporter(final URL _url) throws EFapsException {
    super(_url);
    this.extendSource = evalExtends();
  }

  /**
   * This Method extracts the program Name.
   *
   * @return classname of the esjp
   */
  @Override
  protected String evalProgramName() {
    final String urlPath = getUrl().getPath();
    String name = urlPath.substring(urlPath.lastIndexOf('/') + 1);

    // regular expression for the package name
    final Pattern pckPattern
                          = Pattern.compile("@eFapsPackage[\\s]*[a-z\\.]*\\b");
    final Matcher pckMatcher = pckPattern.matcher(getCode());
    if (pckMatcher.find()) {
      final String pkg = pckMatcher.group()
                                   .replaceFirst("^@eFapsPackage", "");
      name = pkg.trim() + "." + name;
    }
    return name;
  }

  /**
   * This Method extracts the Revision from the program.
   *
   * @return Revision of the program
   */
  @Override
  protected String evalRevision() {
    String ret = null;

    final Pattern revisionPattern =
                        Pattern.compile("@eFapsRevision[\\s].*");
    final Matcher revisionMatcher = revisionPattern.matcher(getCode());
    if (revisionMatcher.find()) {
      ret = revisionMatcher.group().replaceFirst("^@eFapsRevision", "");
    }
    return ret == null ? null : ret.trim();
  }

  /**
   * This Method extracts the UUID from the source.
   *
   * @return UUID of the source
   */
  @Override
  protected UUID evalUUID() {
    UUID uuid = null;

    final Pattern uuidPattern =
                 Pattern.compile("@eFapsUUID[\\s]*[0-9a-z\\-]*");
    final Matcher uuidMatcher = uuidPattern.matcher(getCode());
    if (uuidMatcher.find()) {
      final String uuidStr = uuidMatcher.group()
                                    .replaceFirst("^@eFapsUUID", "");

      uuid = UUID.fromString(uuidStr.trim());
    }

    return uuid;
  }

  /**
   * This Method extracts the expand from the source.
   *
   * @return Expand of the source
   */
  protected String evalExtends() {
    String ret = null;
    // regular expression for the package name
    final Pattern exPattern
                      = Pattern.compile("@eFapsExtends[\\s]*[a-zA-Z\\._-]*\\b");
    final Matcher exMatcher = exPattern.matcher(getCode());
    if (exMatcher.find()) {
      ret = exMatcher.group().replaceFirst("^@eFapsExtends", "");
    }
    return ret == null ? null : ret.trim();
  }

  /**
   * @return the extendSource
   */
  public String getExtendSource() {
    return this.extendSource;
  }
}
