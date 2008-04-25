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

package org.efaps.update.program;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.efaps.update.LinkInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class CSSUpdate extends AbstractSourceUpdate {

  public static String TYPENAME = "Admin_Program_CSS";

  public static String ANNOTATION_VERSION = "@version";

  public static String ANNOTATION_EXTENDS = "@extends";

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(CSSUpdate.class);

  private int version = 0;

  /** Link from CSS extending CSS */
  private final static Link LINK2SUPER =
      new Link("Admin_Program_CSS2CSS", "From", TYPENAME, "To");

  protected final static Set<Link> ALLLINKS = new HashSet<Link>();
  {
    ALLLINKS.add(LINK2SUPER);
  }

  protected CSSUpdate() {
    super(TYPENAME, ALLLINKS);
  }

  public static CSSUpdate readFile(final URL _root, final URL _url)
  {
    final CSSUpdate ret = new CSSUpdate();
    ret.setURL(_url);
    final CSSDefinition definition = new CSSDefinition(_root, _url);
    ret.addDefinition(definition);

    String thisLine;
    try {
      final BufferedReader in =
          new BufferedReader(new FileReader(_url.getPath()));
      while ((thisLine = in.readLine()) != null) {
        if (thisLine.contains(ANNOTATION_VERSION)) {
          String versionstr =
              thisLine.substring(thisLine.indexOf(ANNOTATION_VERSION)
                  + ANNOTATION_VERSION.length());
          versionstr = versionstr.trim();
          if (Character.isDigit(versionstr.charAt(0))) {
            ret.version = Integer.parseInt(versionstr.substring(0, 1));
          }
          for (int i = 0; i < versionstr.length(); i++) {
            if (!Character.isDigit(versionstr.charAt(i))) {
              ret.version = Integer.parseInt(versionstr.substring(0, i));
              break;
            }
          }

        }
        if (thisLine.contains(ANNOTATION_EXTENDS)) {
          String parent =
              thisLine.substring(thisLine.indexOf(ANNOTATION_EXTENDS)
                  + ANNOTATION_EXTENDS.length());
          parent = parent.trim();
          if (parent.indexOf(" ") > 0) {
            parent = parent.substring(0, parent.indexOf(" "));
          }
          definition.assignSuper(parent.trim());
        }
      }
      in.close();

    } catch (final FileNotFoundException e) {
      LOG.error(_url.toString() + " could not be found", e);
    } catch (final IOException e) {
      LOG.error(_url.toString() + " is not readable", e);
    }

    return ret;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.efaps.update.program.AbstractSourceUpdate#getVersion()
   */
  @Override
  protected Long getVersion() {
    long ret;
    if (this.version > 0) {
      ret = this.version;
    } else {
      ret = super.getVersion();
    }
    return ret;
  }

  /**
   * TODO description
   *
   * @author jmox
   * @version $Id$
   */
  public static class CSSDefinition extends SourceDefinition {

    public CSSDefinition(final URL _rootUrl,
                         final URL _url)
    {
      super(_rootUrl, _url);
    }

    public void assignSuper(final String _super) {
      addLink(LINK2SUPER, new LinkInstance(_super));
    }
  }
}
