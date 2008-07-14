/*
 * Copyright 2003 - 2008 The eFaps Team
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

package org.efaps.esjp.teamwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Delete;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

@EFapsUUID("df9925c2-3afe-4282-a04a-4a938af1a53f")
public class CascadeDelete implements EventExecution {

  /**
   * Logger for this class
   */

  private static final Logger LOG =
      LoggerFactory.getLogger(CascadeDelete.class);

  public Return execute(Parameter _parameter) {
    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    final List<String> toDelete = new ArrayList<String>();

    final SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes("TeamWork_Abstract2Abstract");
      query.addSelect("AbstractLink");
      query.addWhereExprEqValue("AncestorLink", instance.getId());
      query.executeWithoutAccessCheck();
      while (query.next()) {
        toDelete.add(query.get("AbstractLink").toString());
      }
      query.close();
      final Comparator<String> comparator = Collections.<String> reverseOrder();
      Collections.sort(toDelete, comparator);
      for (final String delID : toDelete) {
        this.deleteMembers(delID);
        this.deleteAbstract2Abstract(delID);
        this.deleteSourceVersion(delID);
        this.deleteAbstract(delID);
      }
    } catch (final EFapsException e) {
      LOG.error("cascadingDelete(Parameter)", e);
    }

    return null;
  }

  private void deleteSourceVersion(final String _id) {
    try {

      final SearchQuery query = new SearchQuery();
      query.setQueryTypes("TeamWork_SourceVersion");
      query.addSelect("OID");
      query.addWhereExprEqValue("ParentSourceLink", _id);
      query.executeWithoutAccessCheck();
      while (query.next()) {
        final Delete delete = new Delete((String) query.get("OID"));
        delete.execute();
      }
      query.close();

    } catch (final EFapsException e) {
      LOG.error("deleteMembers(List<String>)", e);
    }

  }

  private void deleteMembers(final String _id) {

    try {

      final SearchQuery query = new SearchQuery();
      query.setQueryTypes("TeamWork_Member");
      query.addSelect("OID");
      query.addWhereExprEqValue("AbstractLink", _id);
      query.executeWithoutAccessCheck();
      while (query.next()) {
        final Delete delete = new Delete((String) query.get("OID"));
        delete.execute();
      }
      query.close();

    } catch (final EFapsException e) {
      LOG.error("deleteMembers(List<String>)", e);
    }

  }

  private void deleteAbstract2Abstract(final String _id) {

    try {

      final SearchQuery query = new SearchQuery();
      query.setQueryTypes("TeamWork_Abstract2Abstract");
      query.addSelect("OID");
      query.addWhereExprEqValue("AbstractLink", _id);
      query.executeWithoutAccessCheck();
      while (query.next()) {
        final Delete delete = new Delete((String) query.get("OID"));
        delete.execute();
      }
      query.close();

    } catch (final EFapsException e) {

      LOG.error("deleteAbstract2Abstract(List<String>)", e);
    }

  }

  private void deleteAbstract(final String _id) {

    try {

      final SearchQuery query = new SearchQuery();
      query.setQueryTypes("TeamWork_Abstract");
      query.addSelect("OID");
      query.addWhereExprEqValue("ID", _id);
      query.setExpandChildTypes(true);
      query.executeWithoutAccessCheck();
      while (query.next()) {
        final Delete delete = new Delete((String) query.get("OID"));
        delete.execute();
      }
      query.close();

    } catch (final EFapsException e) {
      LOG.error("deleteAbstract(List<String>)", e);
    }

  }

}
