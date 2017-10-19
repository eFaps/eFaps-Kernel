/*
 * Copyright 2003 - 2017 The eFaps Team
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
 */

package org.efaps.admin.access.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.user.Person;
import org.efaps.ci.CIAdminAccess;
import org.efaps.db.CachedMultiPrintQuery;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Evaluation.
 *
 * @author The eFaps Team
 */
public final class Evaluation
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Evaluation.class);

    /**
     * Instantiates a new evaluation.
     */
    private Evaluation()
    {
    }

    /**
     * Gets the permissions.
     *
     * @param _instance the instance
     * @return the permissions
     * @throws EFapsException on error
     */
    public static PermissionSet getPermissionSet(final Instance _instance)
        throws EFapsException
    {
        Evaluation.LOG.debug("Evaluation PermissionSet for {}", _instance);
        final Key accessKey = Key.get4Instance(_instance);
        final PermissionSet ret = AccessCache.getPermissionCache().get(accessKey);
        Evaluation.LOG.debug("Evaluated PermissionSet {}", ret);
        return ret;
    }

    /**
     * Gets the PermissionSet.
     *
     * @param _instance the instance
     * @param _evaluate the evaluate
     * @return the PermissionSet
     * @throws EFapsException on error
     */
    public static PermissionSet getPermissionSet(final Instance _instance,
                                                 final boolean _evaluate)
        throws EFapsException
    {
        Evaluation.LOG.debug("Retrieving PermissionSet for {}", _instance);
        final Key accessKey = Key.get4Instance(_instance);
        final PermissionSet ret;
        if (AccessCache.getPermissionCache().containsKey(accessKey)) {
            ret = AccessCache.getPermissionCache().get(accessKey);
        } else if (_evaluate) {
            ret = new PermissionSet().setPersonId(accessKey.getPersonId()).setCompanyId(accessKey.getCompanyId())
                            .setTypeId(accessKey.getTypeId());
            Evaluation.eval(ret);
            AccessCache.getPermissionCache().put(accessKey, ret);
        } else {
            ret = null;
        }
        Evaluation.LOG.debug("Retrieved PermissionSet {}", ret);
        return ret;
    }

    /**
     * Eval.
     *
     * @param _permissionSet the PermissionSet
     * @throws EFapsException on error
     */
    public static void eval(final PermissionSet _permissionSet)
        throws EFapsException
    {
        Evaluation.LOG.debug("Evaluating PermissionSet {}", _permissionSet);
        final Person person = Person.get(_permissionSet.getPersonId());
        final Set<Long> ids = new HashSet<>(person.getRoles());
        ids.addAll(person.getGroups());
        ids.add(person.getId());

        final QueryBuilder userAttrQueryBldr = new QueryBuilder(CIAdminAccess.AccessSet2UserAbstract);
        userAttrQueryBldr.addWhereAttrEqValue(CIAdminAccess.AccessSet2UserAbstract.UserAbstractLink, ids.toArray());

        final QueryBuilder typeAttrQueryBldr = new QueryBuilder(CIAdminAccess.AccessSet2DataModelType);
        typeAttrQueryBldr.addWhereAttrEqValue(CIAdminAccess.AccessSet2DataModelType.DataModelTypeLink, _permissionSet
                        .getTypeId());

        final QueryBuilder queryBldr = new QueryBuilder(CIAdminAccess.AccessSet2Type);
        queryBldr.addWhereAttrInQuery(CIAdminAccess.AccessSet2Type.AccessSetLink, typeAttrQueryBldr.getAttributeQuery(
                        CIAdminAccess.AccessSet2DataModelType.AccessSetLink));
        queryBldr.addWhereAttrInQuery(CIAdminAccess.AccessSet2Type.AccessSetLink, userAttrQueryBldr.getAttributeQuery(
                        CIAdminAccess.AccessSet2UserAbstract.AccessSetLink));
        final CachedMultiPrintQuery multi = queryBldr.getCachedPrint4Request();
        multi.addAttribute(CIAdminAccess.AccessSet2Type.AccessTypeLink);
        multi.executeWithoutAccessCheck();
        while (multi.next()) {
            _permissionSet.addAccessTypeId(multi.<Long>getAttribute(CIAdminAccess.AccessSet2Type.AccessTypeLink));
        }

        final Type type = Type.get(_permissionSet.getTypeId());
        if (type.isCheckStatus()) {
            final QueryBuilder statusQueryBldr = new QueryBuilder(CIAdminAccess.AccessSet2Status);
            statusQueryBldr.addWhereAttrInQuery(CIAdminAccess.AccessSet2Status.AccessSetLink, typeAttrQueryBldr
                            .getAttributeQuery(CIAdminAccess.AccessSet2DataModelType.AccessSetLink));
            statusQueryBldr.addWhereAttrInQuery(CIAdminAccess.AccessSet2Status.AccessSetLink, userAttrQueryBldr
                            .getAttributeQuery(CIAdminAccess.AccessSet2UserAbstract.AccessSetLink));

            final CachedMultiPrintQuery statusMulti = statusQueryBldr.getCachedPrint4Request();
            statusMulti.addAttribute(CIAdminAccess.AccessSet2Status.SatusLink);
            statusMulti.executeWithoutAccessCheck();
            while (statusMulti.next()) {
                final Long statusId = statusMulti.getAttribute(CIAdminAccess.AccessSet2Status.SatusLink);
                final Status status = Status.get(statusId);
                if (status.getStatusGroup().getId() == type.getStatusAttribute().getLink().getId()) {
                    _permissionSet.addStatusId(statusId);
                }
            }
        }
        Evaluation.LOG.debug("Evaluated PermissionSet {}", _permissionSet);
    }

    /**
     * Gets the status.
     *
     * @param _instance the instance
     * @return the status
     * @throws EFapsException on error
     */
    public static Status getStatus(final Instance _instance)
        throws EFapsException
    {
        Evaluation.LOG.debug("Retrieving Status for {}", _instance);
        long statusId = 0;
        if (_instance.getType().isCheckStatus()) {
            final Cache<String, Long> cache = AccessCache.getStatusCache();
            if (cache.containsKey(_instance.getKey())) {
                statusId = cache.get(_instance.getKey());
            } else {
                final PrintQuery print = new PrintQuery(_instance);
                print.addAttribute(_instance.getType().getStatusAttribute());
                print.executeWithoutAccessCheck();
                final Long statusIdTmp = print.getAttribute(_instance.getType().getStatusAttribute());
                statusId = statusIdTmp == null ? 0 : statusIdTmp;
                cache.put(_instance.getKey(), statusId);
            }
        }
        final Status ret = statusId > 0 ? Status.get(statusId) : null;
        Evaluation.LOG.debug("Retrieve Status: {}", ret);
        return ret;
    }

    /**
     * Eval status.
     *
     * @param _instances the instances
     * @throws EFapsException on error
     */
    public static void evalStatus(final Collection<Instance> _instances)
        throws EFapsException
    {
        Evaluation.LOG.debug("Evaluating Status for {}", _instances);
        if (CollectionUtils.isNotEmpty(_instances)) {
            final Cache<String, Long> cache = AccessCache.getStatusCache();
            final List<Instance> instances = _instances.stream().filter(inst -> !cache.containsKey(inst.getKey()))
                            .collect(Collectors.toList());
            if (!instances.isEmpty()) {
                final Attribute attr = instances.get(0).getType().getStatusAttribute();
                final MultiPrintQuery multi = new MultiPrintQuery(instances);
                multi.addAttribute(attr);
                multi.executeWithoutAccessCheck();
                while (multi.next()) {
                    final Long statusId = multi.getAttribute(attr);
                    cache.put(multi.getCurrentInstance().getKey(), statusId == null ? 0 : statusId);
                }
            }
        }
    }
}
