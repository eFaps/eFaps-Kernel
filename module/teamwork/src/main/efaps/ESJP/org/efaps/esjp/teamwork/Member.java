/*
 * Copyright 2003 - 2007 The eFaps Team
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.user.Person;
import org.efaps.db.Context;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * This Class is a JavaProgram for eFaps, wich takes care of the Members in
 * TeamWork.
 *
 * @author jmox
 * @version $Id$
 */
public class Member implements EventExecution {

  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(Member.class);

  /**
   * Insert a new Member for a Collection.
   *
   * @param _parameter
   * @return null
   */
  public Return insertNewMember(Parameter _parameter) {

    final Iterator<?> iter =
        ((Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES)).entrySet()
            .iterator();

    final String defaultaccessSet =
        (String) ((Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES))
            .get("DefaultAccessSet");

    final Map<String, String> newValues = new HashMap<String, String>();

    while (iter.hasNext()) {
      final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
      final Attribute attr = (Attribute) entry.getKey();
      final String attrName = attr.getName();
      final String value = entry.getValue().toString();
      newValues.put(attrName, value);
    }

    final SearchQuery query = new SearchQuery();

    try {
      // is this member allready existing?
      query.setQueryTypes("TeamWork_MemberRights");
      query.addWhereExprEqValue("UserAbstractLink", newValues
          .get("UserAbstractLink"));
      query.addWhereExprEqValue("AbstractLink", newValues.get("AbstractLink"));
      query.addSelect("OID");
      query.executeWithoutAccessCheck();

      if (!query.next()) {

        if (!isRoot(newValues.get("AbstractLink").toString())) {
          final Insert insert = new Insert("TeamWork_Member");
          insert.add("AccessSetLink", getAccessSetID(defaultaccessSet));
          insert.add("AbstractLink", getRootID(newValues.get("AbstractLink")));
          insert.add("UserAbstractLink", newValues.get("UserAbstractLink"));
          insert.executeWithoutAccessCheck();
          insert.close();

        }
        final Insert insert = new Insert("TeamWork_Member");
        insert.add("AccessSetLink", newValues.get("AccessSetLink"));
        insert.add("AbstractLink", newValues.get("AbstractLink"));
        insert.add("UserAbstractLink", newValues.get("UserAbstractLink"));
        insert.executeWithoutAccessCheck();
        insert.close();
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Member gibt es schon!");
        }
      }

    } catch (final EFapsException e) {

      LOG.error("insertNewMember(Map<TriggerKeys4Values,Object>)", e);
    } catch (final Exception e) {

      LOG.error("insertNewMember(Map<TriggerKeys4Values,Object>)", e);
    }
    return null;

  }

  /**
   * check if this TeamWork_Abstract is a Root
   *
   * @param _abstractlink
   *                Abstract to search for
   * @return true, if a Root, otherwise false
   */
  private boolean isRoot(final String _abstractlink) {
    boolean ret = false;
    final SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes("TeamWork_Abstract2Abstract");
      query.addWhereExprEqValue("AbstractLink", _abstractlink);
      query.addWhereExprEqValue("AncestorLink", _abstractlink);
      query.addWhereExprEqValue("Rank", "1");
      query.executeWithoutAccessCheck();
      if (query.next()) {
        ret = true;
      }
      query.close();
    } catch (final EFapsException e) {
      LOG.error("Can't check if TeamWork_Abstract: "
          + _abstractlink
          + " is a Root", e);
    }

    return ret;
  }

  /**
   * get the ID of the Root, for a TeamWork_Abstract
   *
   * @param _abstractlink
   *                TeamWork_Abstract the Root is searched for
   * @return the ID of the Root of the TeamWork_Abstract
   */
  private String getRootID(final String _abstractlink) {
    String ID = null;
    final SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes("TeamWork_Abstract2Abstract");
      query.addWhereExprEqValue("AbstractLink", _abstractlink);
      query.addWhereExprEqValue("Rank", "1");
      query.addSelect("AncestorLink");
      query.executeWithoutAccessCheck();
      if (query.next()) {
        ID = query.get("AncestorLink").toString();

      } else {
        LOG.error("Cant't find the ID of the Root for: " + _abstractlink);
      }
      query.close();
    } catch (final EFapsException e) {
      LOG.error("getRootID(String)", e);
    }
    return ID;
  }

  public Return editMember(Parameter _parameter) {
    System.out.print("geht doch");

    final Return ret = new Return();
    ret.put(ReturnValues.VALUES, "test");

    return ret;
  }

  public Return execute(Parameter _parameter) {

    final Iterator<?> iter =
        ((Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES)).entrySet()
            .iterator();
    final Map<String, String> newValues = new HashMap<String, String>();
    while (iter.hasNext()) {
      final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
      final Attribute attr = (Attribute) entry.getKey();
      final String attrName = attr.getName();
      final String value = entry.getValue().toString();
      newValues.put(attrName, value);
    }
    final SearchQuery query = new SearchQuery();
    Update update;
    try {
      query.setQueryTypes("TeamWork_Member");
      query.addWhereExprEqValue("UserAbstractLink", newValues
          .get("UserAbstractLink"));
      query.addWhereExprEqValue("AbstractLink", newValues.get("AbstractLink"));
      query.addSelect("OID");
      query.executeWithoutAccessCheck();

      if (query.next()) {
        update = new Update(query.get("OID").toString());
      } else {
        update = new Insert("TeamWork_Member");

      }
      update.add("AccessSetLink", newValues.get("AccessSetLink"));
      update.add("AbstractLink", newValues.get("AbstractLink"));
      update.add("UserAbstractLink", newValues.get("UserAbstractLink"));
      update.execute();
    } catch (final EFapsException e) {

      LOG.error("execute(Map<TriggerKeys4Values,Object>)", e);
    } catch (final Exception e) {

      LOG.error("execute(Map<TriggerKeys4Values,Object>)", e);
    }
    return null;

  }

  /**
   * This Method is used by a Trigger, when a new Root Collection is created, to
   * set the default Right for the Creator of the RootCollection.
   *
   * @param _parameter
   * @return null
   */
  public Return insertCollectionCreator(Parameter _parameter) {

    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    final String abstractlink = ((Long) instance.getId()).toString();

    final String accessSet =
        (String) ((Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES))
            .get("AccessSet");

    try {

      Insert insert;
      insert = new Insert("TeamWork_Member");
      insert.add("AbstractLink", abstractlink);
      insert.add("AccessSetLink", getAccessSetID(accessSet));
      insert.add("UserAbstractLink", ((Long) Context.getThreadContext()
          .getPerson().getId()).toString());
      insert.execute();

    } catch (final EFapsException e) {

      LOG.error("insertCollectionCreator(Map<TriggerKeys4Values,Object>)", e);
    }
    return null;

  }

  /**
   * Get the ID of a AccessSet
   *
   * @param _accessset
   *                AccessSet to Search for
   * @return ID of the AccessSet, Null if not found
   */
  private String getAccessSetID(final String _accessset) {
    String ID = null;
    final SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes("Admin_Access_AccessSet");
      query.addWhereExprEqValue("Name", _accessset);
      query.addSelect("ID");
      query.executeWithoutAccessCheck();
      if (query.next()) {
        ID = query.get("ID").toString();
      } else {
        LOG.error("Cant't find the ID of the AccessSet for: " + _accessset);
      }
    } catch (final EFapsException e) {
      LOG.error("getAccessSetID(String)", e);
    }
    return ID;

  }

  /**
   * This Method is used to remove a Member from a RootCollection and all of its
   * Childs. It can only be inisiated in a RootCollection.
   *
   * @param _parameter
   * @return
   */
  public Return removeMember(Parameter _parameter) {
    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    final String tempID = ((Long) instance.getId()).toString();

    Context context = null;
    try {
      context = Context.getThreadContext();

      final String abstractid =
          context.getParameter("oid").substring(
              context.getParameter("oid").indexOf(".") + 1);

      SearchQuery query = new SearchQuery();
      query.setQueryTypes("TeamWork_MemberRights");
      query.addWhereExprEqValue("ID", tempID);
      query.addWhereExprEqValue("AbstractLink", abstractid);
      query.addSelect("UserAbstractLink");
      query.executeWithoutAccessCheck();

      if (query.next()) {
        final Long Userid = ((Person) query.get("UserAbstractLink")).getId();
        query.close();

        query = new SearchQuery();
        query.setQueryTypes("TeamWork_Abstract2Abstract");
        query.addWhereExprEqValue("AncestorLink", abstractid);
        query.addSelect("AbstractLink");
        query.executeWithoutAccessCheck();

        while (query.next()) {
          final SearchQuery query2 = new SearchQuery();
          query2.setQueryTypes("TeamWork_Member");
          query2.addWhereExprEqValue("AbstractLink", query.get("AbstractLink")
              .toString());
          query2.addWhereExprEqValue("UserAbstractLink", Userid);
          query2.addSelect("OID");
          query2.executeWithoutAccessCheck();
          if (query2.next()) {
            final String delOID = (String) query2.get("OID");
            final Delete delete = new Delete(delOID);
            delete.execute();
          }
          query2.close();
        }
        query.close();
      } else {
        LOG.error("no");
      }

    } catch (final EFapsException e) {
      LOG.error("removeMember(ParameterInterface)", e);
    }

    return null;

  }

}
