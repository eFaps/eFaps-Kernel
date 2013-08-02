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

import org.drools.runtime.KnowledgeRuntime;
import org.drools.runtime.process.WorkItem;
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
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.Group;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.Task;
import org.jbpm.task.TaskService;
import org.jbpm.task.User;
import org.jbpm.task.utils.OnErrorAction;
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

    /**
     * @param _client   client
     * @param _session  Session
     * @param _action   Action
     * @param _classLoader  Classloader
     */
    public HumanTaskWorkItemHandler(final TaskService _client,
                                    final KnowledgeRuntime _session,
                                    final OnErrorAction _action,
                                    final ClassLoader _classLoader)
    {
        super(_client, _session, _action, _classLoader);
    }

    @Override
    protected Task createTaskBasedOnWorkItemParams(final WorkItem _workItem)
    {
        final Task ret = super.createTaskBasedOnWorkItemParams(_workItem);
        try {
            final Person person = Context.getThreadContext().getPerson();
            if (person != null) {
                final User user = new User(person.getUUID().toString());
                ret.getPeopleAssignments().setTaskInitiator(user);
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
                        final Group group = new Group(((Role) user).getUUID().toString());
                        newPotOwners.add(group);
                    }
                }
                ret.getPeopleAssignments().setPotentialOwners(newPotOwners);
            }
        } catch (final EFapsException e) {
            HumanTaskWorkItemHandler.LOG.error("Catched error on creation of task", e);
        }
        return ret;
    }
}
