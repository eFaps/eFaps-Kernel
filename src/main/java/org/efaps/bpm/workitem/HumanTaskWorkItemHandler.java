/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.bpm.workitem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.user.AbstractUserObject;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.services.task.wih.LocalHTWorkItemHandler;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalOrganizationalEntity;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class HumanTaskWorkItemHandler
    extends LocalHTWorkItemHandler
{

    /**
     * Classname of the class to be executed.
     */
    public static final String POTOWNERCLASS = "org.efaps.esjp.bpm.listener.PotentialOwnerListener";

    /**
     * Logger for this Instance.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HumanTaskWorkItemHandler.class);

    @Override
    protected Task createTaskBasedOnWorkItemParams(final KieSession _ksession,
                                                   final WorkItem _workItem)
    {
        final Task ret = super.createTaskBasedOnWorkItemParams(_ksession, _workItem);
        try {
            final Person person = Context.getThreadContext().getPerson();
            if (person != null) {
                final User user = new UserImpl(person.getUUID().toString());
                ((InternalPeopleAssignments) ret.getPeopleAssignments()).setTaskInitiator(user);
            }
            // the original implementation does not allow to add groups as bussinessadministrator
            final String baIds = (String)_workItem.getParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID);
            if (baIds != null && !baIds.isEmpty()) {
                final List<OrganizationalEntity> baList = ret.getPeopleAssignments().getBusinessAdministrators();
                final String separator = System.getProperty("org.jbpm.ht.user.separator", ",");
                for (final String baId : baIds.split(separator)) {
                    final Role role = Role.get(UUID.fromString(baId));
                    if (role != null) {
                        final Group group = TaskModelProvider.getFactory().newGroup();
                        ((InternalOrganizationalEntity) group).setId(baId);
                        // this works only due to the reason that the Group "equal" implementation uses only the id
                        if (baList.contains(group)) {
                            baList.remove(group);
                        }
                        baList.add(group);
                    }
                }
                ((InternalPeopleAssignments) ret.getPeopleAssignments()).setBusinessAdministrators(baList);
            }

            final List<OrganizationalEntity> potOwners = ret.getPeopleAssignments().getPotentialOwners();

            final List<AbstractUserObject> users = new ArrayList<AbstractUserObject>();
            for (final OrganizationalEntity potOwner : potOwners) {
                users.add(AbstractUserObject.getUserObject(UUID.fromString(potOwner.getId())));
            }
            List<?> newUser = null;
            try {
                final Parameter parameter = new Parameter();
                parameter.put(ParameterValues.BPM_TASK, _workItem.getName());
                parameter.put(ParameterValues.BPM_VALUES, users);
                parameter.put(ParameterValues.PARAMETERS, _workItem.getParameters());

                final Class<?> transformer = Class.forName(HumanTaskWorkItemHandler.POTOWNERCLASS, true,
                                EFapsClassLoader.getInstance());
                final Method method = transformer.getMethod("execute", new Class[] { Parameter.class });
                final Return ret2 = (Return) method.invoke(transformer.newInstance(), parameter);
                if (ret2 != null) {
                    newUser = (List<?>) ret2.get(ReturnValues.VALUES);
                }
            } catch (final ClassNotFoundException e) {
                HumanTaskWorkItemHandler.LOG.error("Class could not be found.", e);
            } catch (final InstantiationException e) {
                HumanTaskWorkItemHandler.LOG.error("Class could not be instantiation.", e);
            } catch (final IllegalAccessException e) {
                HumanTaskWorkItemHandler.LOG.error("Class could not be accessed.", e);
            } catch (final IllegalArgumentException e) {
                HumanTaskWorkItemHandler.LOG.error("Illegal Argument.", e);
            } catch (final InvocationTargetException e) {
                HumanTaskWorkItemHandler.LOG.error("Invocation Target.", e);
            } catch (final SecurityException e) {
                HumanTaskWorkItemHandler.LOG.error("Class could not be found.", e);
            } catch (final NoSuchMethodException e) {
                HumanTaskWorkItemHandler.LOG.error("Class could not be found.", e);
            }

            if (newUser != null) {
                final List<OrganizationalEntity> newPotOwners = new ArrayList<OrganizationalEntity>();
                for (final Object user : newUser) {
                    if (user instanceof Role) {
                        final Group group = new GroupImpl(((Role) user).getUUID().toString());
                        newPotOwners.add(group);
                    }
                }
                ((InternalPeopleAssignments) ret.getPeopleAssignments()).setPotentialOwners(newPotOwners);
            }
        } catch (final EFapsException e) {
            HumanTaskWorkItemHandler.LOG.error("Catched error on creation of task", e);
        }
        return ret;
    }
}
