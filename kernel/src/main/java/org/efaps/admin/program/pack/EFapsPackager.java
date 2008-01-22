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

package org.efaps.admin.program.pack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.efaps.db.Checkout;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public final class EFapsPackager {

  private EFapsPackager() {

  }

  public static Map<List<String>, String> PACKAGEMAPPER =
      new HashMap<List<String>, String>();

  public static Map<String, OnePackage> PACKAGES =
      new HashMap<String, OnePackage>();

  public static String getPackageKey(final List<String> _names) {
    mergeList(_names);
    String name;
    if (PACKAGEMAPPER.containsKey(_names)) {
      name = PACKAGEMAPPER.get(_names);
      if (name == "") {
        name = createName(_names);
        PACKAGEMAPPER.put(_names, name);
      }
    } else {
      name = createName(_names);
      PACKAGEMAPPER.put(_names, name);

    }
    return name;
  }

  private static void mergeList(final List<String> _names) {
    final Set<String> compare = new HashSet<String>();

    for (int i = 0; i < _names.size();) {
      if (compare.contains(_names.get(i))) {
        _names.remove(i);
      } else {
        compare.add(_names.get(i));
        i++;
      }
    }

  }

  private static String createName(final List<String> _names) {
    final StringBuilder ret = new StringBuilder();
    final List<String> oids = new ArrayList<String>();
    try {
      for (final String name : _names) {
        final SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_Program_StaticCompiled");
        query.setExpandChildTypes(true);
        query.addSelect("OID");
        query.addWhereExprEqValue("Name", name);
        query.execute();
        if (query.next()) {
          if (ret.length() > 0) {
            ret.append("-");
          }
          final String oid = query.get("OID").toString();
          ret.append(oid);
          oids.add(oid);
        }
      }
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    setFile(oids, ret.toString());

    return ret.toString();
  }

  private static void setFile(List<String> oids, String name2) {

    final File file = new File(getTempFolder(), name2);;
    try {

      final FileOutputStream out = new FileOutputStream(file);
      final GZIPOutputStream zout = new GZIPOutputStream(out);
      int bytesRead;
      final byte[] buffer = new byte[2048];
      for (final String oid : oids) {
        final Checkout checkout = new Checkout(oid);
        final InputStream bis = checkout.execute();
        while ((bytesRead = bis.read(buffer)) != -1) {
          zout.write(buffer, 0, bytesRead);
        }

      }
      zout.close();
      out.close();
      PACKAGES.put(name2, new OnePackage(file));
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    };
  }

  public static File getTempFolder() {
    try {
      return File.createTempFile("test", null).getParentFile();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public static boolean containsPackage(final String _key) {
    return PACKAGES.containsKey(_key);
  }

  public static OnePackage getPackage(final String _key) {
    return PACKAGES.get(_key);
  }

}
