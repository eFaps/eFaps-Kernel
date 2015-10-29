/*
 * Copyright 2003 - 2015 The eFaps Team
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

package org.efaps.bpm;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.drools.persistence.jta.JtaTransactionManager;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.user.AbstractUserObject;
import org.efaps.admin.user.Role;
import org.efaps.bpm.compiler.KnowledgeBuilderFactoryServiceImpl;
import org.efaps.bpm.identity.EntityMapper;
import org.efaps.bpm.identity.UserGroupCallbackImpl;
import org.efaps.bpm.listener.AsyncCalls;
import org.efaps.bpm.listener.IAsyncListener;
import org.efaps.bpm.listener.WorkingMemoryLogListener;
import org.efaps.bpm.process.ProcessAdmin;
import org.efaps.bpm.runtime.ManagerFactoryImpl;
import org.efaps.bpm.runtime.RegisterableItemsFactoryImpl;
import org.efaps.bpm.task.TaskAdminstration;
import org.efaps.bpm.transaction.ConnectionProvider;
import org.efaps.bpm.transaction.TransactionHelper;
import org.efaps.bpm.workitem.ManualTaskItemHandler;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.hibernate.cfg.AvailableSettings;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.runtime.manager.impl.PerProcessInstanceRuntimeManager;
import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.jbpm.services.task.impl.model.I18NTextImpl;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.Mapper;
import org.kie.internal.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.context.CorrelationKeyContext;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.utils.ServiceRegistryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class BPM
{
    /**
     * Parameter key to be used to pass to the process.
     */
    public static final String OUTPARAMETER4TASKDECISION = "eFapsDecision";

    /**
     * Parameter key to be used to pass delegates to an Human Task.
     */
    public static final String INPUTPARAMETER4DELEGATE = "DelegateIds";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BPM.class);

    /**
     * SingletonRuntimeManager.
     */
    private static RuntimeManager SMANAGER;

    /**
     * PerProcessInstanceRuntimeManager.
     */
    private static RuntimeManager PMANAGER;

    /**
     * Create Singelton.
     */
    private BPM()
    {
    }

    /**
     * Initialize BPM.
     *
     * @throws EFapsException on error
     */
    public static void initialize()
        throws EFapsException
    {
        final SystemConfiguration config = EFapsSystemConfiguration.get();
        final boolean active = config != null
                        ? config.getAttributeValueAsBoolean(KernelSettings.ACTIVATE_BPM) : false;
        if (active) {

            if (BPM.PMANAGER != null) {
                BPM.PMANAGER.close();
                BPM.PMANAGER = null;
            }
            if (BPM.SMANAGER != null) {
                BPM.SMANAGER.close();
                BPM.SMANAGER = null;
            }

            UserTransaction userTrans = null;
            InitialContext context = null;
            try {
                context = new InitialContext();
                userTrans = TransactionHelper.findUserTransaction();
                Object object = null;
                try {
                    object = context.lookup(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME);
                } catch (final NamingException ex) {
                    BPM.LOG.info("Checked for JtaTransactionManager");
                }
                if (object == null) {
                    context.bind(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, userTrans);
                    context.bind(JtaTransactionManager.FALLBACK_TRANSACTION_MANAGER_NAMES[0],
                                    TransactionHelper.findTransactionManager());
                }
            } catch (final NamingException ex) {
                BPM.LOG.error("Could not initialise JNDI InitialContext", ex);
            }

            // register our own KnowledgeBuilderFactoryService
            ServiceRegistryImpl.getInstance().addDefault(KnowledgeBuilderFactoryService.class,
                            KnowledgeBuilderFactoryServiceImpl.class.getName());

            final RegisterableItemsFactoryImpl itemsFactory = new RegisterableItemsFactoryImpl();
            itemsFactory.addWorkItemHandler("Manual Task", ManualTaskItemHandler.class);
            itemsFactory.addProcessListener(WorkingMemoryLogListener.class);

            final Map<String, String> properties = new HashMap<String, String>();
            properties.put(AvailableSettings.DIALECT, Context.getDbType().getHibernateDialect());
            properties.put(AvailableSettings.SHOW_SQL, String.valueOf(BPM.LOG.isDebugEnabled()));
            properties.put(AvailableSettings.FORMAT_SQL, "true");
            properties.put(AvailableSettings.RELEASE_CONNECTIONS, "after_transaction");
            properties.put(AvailableSettings.CONNECTION_PROVIDER, ConnectionProvider.class.getName());
            properties.put(org.hibernate.jpa.AvailableSettings.NAMING_STRATEGY, NamingStrategy.class.getName());

            final EntityManagerFactory emf = Persistence
                            .createEntityManagerFactory("org.jbpm.persistence.jpa", properties);

            final RuntimeEnvironmentBuilder builder = RuntimeEnvironmentBuilder.getDefault()
                            .classLoader(EFapsClassLoader.getInstance())
                            .userGroupCallback(new UserGroupCallbackImpl())
                            .entityManagerFactory(emf)
                            .registerableItemsFactory(itemsFactory)
                            .persistence(true)
                            .addEnvironmentEntry("TRANSACTION_LOCK_ENABLED", "false");

            BPM.add2EnvironmentBuilder(builder);

            final RuntimeEnvironment environment = builder.get();
            final ManagerFactoryImpl factory = new ManagerFactoryImpl();

            BPM.PMANAGER = factory.newPerProcessInstanceRuntimeManager(environment);
            BPM.SMANAGER = factory.newSingletonRuntimeManager(environment);
        }
    }

    /**
     * @param _envBuilder RuntimeEnvironmentBuilder
     * @throws EFapsException on error
     */
    private static void add2EnvironmentBuilder(final RuntimeEnvironmentBuilder _envBuilder)
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.BPM);
        final InstanceQuery query = queryBldr.getQuery();
        query.executeWithoutAccessCheck();
        while (query.next()) {
            final Checkout checkout = new Checkout(query.getCurrentValue());
            final InputStream in = checkout.execute();
            _envBuilder.addAsset(ResourceFactory.newInputStreamResource(in), ResourceType.BPMN2);
        }
    }

    /**
     * @param _processId    id of the process to start
     * @param _params       parameter map for the task
     * @return the <code>ProcessInstance</code> that represents the instance of the process that was started
     */
    public static ProcessInstance startProcess(final String _processId,
                                               final Map<String, Object> _params)
    {
        final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(CorrelationKeyContext.get());
        final KieSession ksession = runtimeEngine.getKieSession();
        return ksession.startProcess(_processId, _params);
    }

    /**
     * Returns all node instances that are currently active within this container.
     * @param _processInstanceId if of a process Instance
     * @return the list of node instances currently active, empty list if none found
     */
    public static Collection<NodeInstance> getActiveNodes4ProcessId(final long _processInstanceId)
    {
        ProcessInstance processInstance = null;
        if (BPM.processIsActiv(_processInstanceId)) {
            final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext
                            .get(_processInstanceId));
            final KieSession ksession = runtimeEngine.getKieSession();
            processInstance = ksession.getProcessInstance(_processInstanceId);
        }
        return processInstance == null ? Collections.<NodeInstance>emptyList()
                        : ((WorkflowProcessInstance) processInstance).getNodeInstances();
    }

    /**
     * Check for the PerProcessInstanceRuntimeManager if the process for the
     * given processInstanceId is still active.
     *
     * @param _processInstanceId processInstanceId to be checked if still activ
     * @return true if activ, else fasle
     */
    public static boolean processIsActiv(final long _processInstanceId)
    {
        final ProcessInstanceIdContext context = ProcessInstanceIdContext.get(_processInstanceId);
        final Mapper mapper = ((PerProcessInstanceRuntimeManager) BPM.PMANAGER).getMapper();
        return mapper.findMapping(context, null) != null;
    }

    /**
     * @param _processInstanceId id of the processinstanc to be aborted
     */
    public static void abortProcessInstance(final long _processInstanceId)
    {
        final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext
                        .get(_processInstanceId));
        runtimeEngine.getKieSession().abortProcessInstance(_processInstanceId);
    }

    /**
     * When the task has a single potential owner, it transitions into the
     * Reserved state, indicating that it is assigned to a single actual owner.
     * Otherwise (i.e., when it has multiple potential owners or is assigned to
     * a work queue), it transitions into the Ready state, indicating that it
     * can be claimed by one of its potential owners. Once a potential owner
     * claims the task, it transitions into the Reserved state, making that
     * potential owner the actual owner.
     *
     * @param _taskSummary task to be claimed
     * @throws EFapsException on error
     */
    public static void claimTask(final TaskSummary _taskSummary)
        throws EFapsException
    {
        final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext.get(_taskSummary
                        .getProcessInstanceId()));
        final TaskService taskService = runtimeEngine.getTaskService();
        // check if must be claimed still
        if (Status.Ready.equals(_taskSummary.getStatus())) {
            taskService.claim(_taskSummary.getId(),
                            EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID()));
        }
    }

    /**
     * Task’s potential owners, actual owner or business administrator can
     * forward an active task to another person or a set of people, replacing
     * himself by those people in the list of potential owners. Potential owners
     * can only forward tasks that are in the Ready state. Forwarding is
     * possible if the task has a set of individually assigned potential owners,
     * not if its potential owners are assigned using one or many groups. If the
     * task is in the Reserved or InProgress state then the task is implicitly
     * released first, that is, the task is transitioned into the Ready state.
     * Business data associated with the task is kept. The user performing the
     * forward is removed from the set of potential owners of the task, and the
     * forwardee is added to the set of potential owners.
     *
     * @param _taskSummary task to be claimed
     * @param _targetEntityId target entity
     * @throws EFapsException on error
     */
    public static void forwardTask(final TaskSummary _taskSummary,
                                   final String _targetEntityId)
        throws EFapsException
    {
        final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext.get(_taskSummary
                        .getProcessInstanceId()));
        final TaskService taskService = runtimeEngine.getTaskService();
        taskService.forward(_taskSummary.getId(),
                        EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID()),
                        EntityMapper.getEntityId(_targetEntityId));
    }

    /**
     * Task’s potential owners, actual owner or business administrator can
     * delegate a task to another user, making that user the actual owner of the
     * task, and also adding her to the list of potential owners in case she is
     * not, yet. A task can be delegated when it is in an active state (Ready,
     * Reserved, InProgress), and transitions the task into the Reserved state.
     * Business data associated with the task is kept.
     *
     * @param _taskSummary  TaskSummary
     * @param _targetEntityId   target entity
     * @throws EFapsException on error
     */
    public static void delegateTask(final TaskSummary _taskSummary,
                                    final String _targetEntityId)
        throws EFapsException
    {
        final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext.get(_taskSummary
                        .getProcessInstanceId()));
        final TaskService taskService = runtimeEngine.getTaskService();
        taskService.delegate(_taskSummary.getId(),
                        EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID()),
                        EntityMapper.getEntityId(_targetEntityId));
    }


    /**
     * @param _taskSummary taskSummary the Delegate Roles are wanted for.
     * @return List of Roles
     * @throws CacheReloadException on error
     */
    public static List<Role> getDelegates4Task(final TaskSummary _taskSummary)
        throws CacheReloadException
    {
        final List<Role> ret = new ArrayList<Role>();
        final Object values = BPM.getTaskData(_taskSummary);
        if (values instanceof Map) {
            final String delegatesStr = (String) ((Map<?, ?>) values).get(BPM.INPUTPARAMETER4DELEGATE);
            if (delegatesStr != null && !delegatesStr.isEmpty()) {
                final String[] delegates = delegatesStr.split(";");
                for (final String delegate : delegates) {
                    final Role role = Role.get(UUID.fromString(delegate));
                    if (role != null) {
                        ret.add(role);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * @param _taskSummary task to be delgate
     * @param _targetUserId userid of the user the task will be delegated to
     * @throws EFapsException on error
     */
    public static void delegateTask4Role(final TaskSummary _taskSummary,
                                         final String _targetUserId)
        throws EFapsException
    {
        final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(CorrelationKeyContext.get());
        final TaskService taskService = runtimeEngine.getTaskService();
       // runtimeEngine.getKieSession().execute(new GenericCommand(){})
        // check if must be claimed still
        if (Status.Ready.equals(_taskSummary.getStatus())) {
            boolean add = true;
            final Task task = taskService.getTaskById(_taskSummary.getId());
            final List<OrganizationalEntity> owners = task.getPeopleAssignments().getPotentialOwners();
            for (final OrganizationalEntity org : owners) {
                if (_targetUserId.equals(org.getId())) {
                    add = false;
                }
            }
            if (add) {
                final boolean isRole = Role.get(UUID.fromString(_targetUserId)) != null;
                if (isRole) {
//  final List<OperationCommand> commands = taskService.getCommandsForOperation(Operation.Delegate);
//                    for (final OperationCommand cmd : commands) {
//                        cmd.setExec(Operation.Delegate);
//                    }
                }

                taskService.delegate(_taskSummary.getId(),
                                EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID()),
                                _targetUserId);

                final List<I18NText> descr = task.getDescriptions();
                final String txt = DBProperties.getFormatedDBProperty(
                                "org.efaps.bpm.BPM.delegateText",
                                new Object[] { Context.getThreadContext().getPerson().getName(),
                                          AbstractUserObject.getUserObject(UUID.fromString(_targetUserId)).getName() });
                if (descr.isEmpty()) {
                    final I18NTextImpl i18n = new I18NTextImpl();
                    i18n.setText(txt);
                    i18n.setLanguage("en-UK");
                    descr.add(i18n);
                } else {
                    final String oldTxt = descr.get(0).getText();
                    if (!oldTxt.contains(txt)) {
                        ((I18NTextImpl) descr.get(0)).setText(descr.get(0).getText() + " - " + txt);
                    }
                }

                if (isRole) {
//                    final List<OperationCommand> commands = taskService.getCommandsForOperation(Operation.Delegate);
//                    for (final OperationCommand cmd : commands) {
//                        cmd.setExec(Operation.Claim);
//                    }
                }
            }
        }
    }

    /**
     * The current actual owner of a human task may release a task to again make
     * it available for all potential owners. A task can be released from active
     * states that have an actual owner (Reserved, InProgress), transitioning it
     * into the Ready state. Business data associated with the task
     * (intermediate result data, ad-hoc attachments and comments) is kept. A
     * task that is currently InProgress can be stopped by the actual owner,
     * transitioning it into state Reserved. Business data associated with the
     * task as well as its actual owner is kept.
     *
     * @param _taskSummary task to be claimed
     * @throws EFapsException on error
     */
    public static void releaseTask(final TaskSummary _taskSummary)
        throws EFapsException
    {
        final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext.get(_taskSummary
                        .getProcessInstanceId()));
        final TaskService taskService = runtimeEngine.getTaskService();
        // check if must be claimed still
        if (Status.Reserved.equals(_taskSummary.getStatus())) {
            taskService.release(_taskSummary.getId(),
                            EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID()));
        }
    }

    /**
     * Stop a task that is in process.
     *
     * @param _taskSummary task to be claimed
     * @throws EFapsException on error
     */
    public static void stopTask(final TaskSummary _taskSummary)
        throws EFapsException
    {
        final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext.get(_taskSummary
                        .getProcessInstanceId()));
        final TaskService taskService = runtimeEngine.getTaskService();
        taskService.stop(_taskSummary.getId(),
                        EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID()));
    }

    /**
     * Stop a task that is in process.
     *
     * @param _taskSummary task to be claimed
     * @throws EFapsException on error
     */
    public static void exitTask(final TaskSummary _taskSummary)
        throws EFapsException
    {
        final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext.get(_taskSummary
                        .getProcessInstanceId()));
        final TaskService taskService = runtimeEngine.getTaskService();
        taskService.exit(_taskSummary.getId(),
                        EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID()));
    }

    /**
     * @param _taskSummary TaskSummary
     * @param _decision one of true, false, null
     * @param _values mapping of additional values
     * @throws EFapsException on error
     */
    public static void executeTask(final TaskSummary _taskSummary,
                                   final Boolean _decision,
                                   final Map<String, Object> _values)
        throws EFapsException
    {
        final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext.get(_taskSummary
                        .getProcessInstanceId()));
        final TaskService taskService = runtimeEngine.getTaskService();

        // check if must be claimed still
        if (Status.Ready.equals(_taskSummary.getStatus())) {
            taskService.claim(_taskSummary.getId(),
                            EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID()));
        }
        if (Status.InProgress.equals(_taskSummary.getStatus())) {
            taskService.resume(_taskSummary.getId(),
                            EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID()));
        } else {
            taskService.start(_taskSummary.getId(),
                            EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID()));
        }

        final Parameter parameter = new Parameter();
        parameter.put(ParameterValues.BPM_TASK, _taskSummary);
        parameter.put(ParameterValues.BPM_VALUES, _values);
        parameter.put(ParameterValues.BPM_DECISION, _decision);
        final Map<String, Object> results = new HashMap<String, Object>();
        if (_values != null) {
            results.putAll(_values);
        }
        // exec esjp
        try {
            final Class<?> transformer = Class.forName("org.efaps.esjp.bpm.TaskTransformer", true,
                            EFapsClassLoader.getInstance());
            final Method method = transformer.getMethod("execute", new Class[] { Parameter.class });
            final Return ret = (Return) method.invoke(transformer.newInstance(), parameter);
            if (ret != null) {
                results.put(BPM.OUTPARAMETER4TASKDECISION, ret.get(ReturnValues.VALUES));
            }
        } catch (final ClassNotFoundException e) {
            BPM.LOG.error("Class could not be found.", e);
        } catch (final InstantiationException e) {
            BPM.LOG.error("Class could not be instantiation.", e);
        } catch (final IllegalAccessException e) {
            BPM.LOG.error("Class could not be accessed.", e);
        } catch (final IllegalArgumentException e) {
            BPM.LOG.error("Illegal Argument.", e);
        } catch (final InvocationTargetException e) {
            BPM.LOG.error("Invocation Target.", e);
        } catch (final SecurityException e) {
            BPM.LOG.error("Class could not be found.", e);
        } catch (final NoSuchMethodException e) {
            BPM.LOG.error("Class could not be found.", e);
        }
        taskService.complete(_taskSummary.getId(),
                             EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID()), results);

        BPM.invokeAsyncListeners(AsyncCalls.AFTER_EXECUTE, _taskSummary, results);
    }

    /**
     * @param _taskSummary tasksummary the data is wanted for
     * @return the object data
     */
    public static Object getTaskData(final TaskSummary _taskSummary)
    {
        Object ret = null;
        final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext.get(_taskSummary
                        .getProcessInstanceId()));
        final TaskService taskService = runtimeEngine.getTaskService();
        final Task task = taskService.getTaskById(_taskSummary.getId());
        final long contentId = task.getTaskData().getDocumentContentId();
        if (contentId != -1) {
            final Content content = taskService.getContentById(contentId);
            ret = ContentMarshallerHelper.unmarshall(content.getContent(),
                            runtimeEngine.getKieSession().getEnvironment(),
                            BPM.class.getClassLoader());
        }
        return ret;
    }

    /**
     * @return list of assigned tasks
     */
    public static List<TaskSummary> getTasksAssignedAsPotentialOwner()
    {
        final List<TaskSummary> ret = new ArrayList<TaskSummary>();
        final RuntimeEngine runtimeEngine = BPM.SMANAGER.getRuntimeEngine(EmptyContext.get());
        final TaskService taskService = runtimeEngine.getTaskService();
        try {
            if (Context.getThreadContext().getPerson().getUUID() == null) {
                BPM.LOG.error("User '{}' has no UUID assigned.", Context.getThreadContext().getPerson().getName());
            } else {
                final String persId = EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID());
                // final String language = Context.getThreadContext().getLanguage();
                ret.addAll(taskService.getTasksAssignedAsPotentialOwner(persId, "en-UK"));
            }
        } catch (final EFapsException e) {
            BPM.LOG.error("Error on retrieving List of TaskSummaries.");
        }
        return ret;
    }

    /**
     * @return list of assigned tasks
     */
    public static List<TaskSummary> getTasksOwned()
    {
        final List<TaskSummary> ret = new ArrayList<TaskSummary>();
        final RuntimeEngine runtimeEngine = BPM.SMANAGER.getRuntimeEngine(EmptyContext.get());
        final TaskService taskService = runtimeEngine.getTaskService();
        try {
            if (Context.getThreadContext().getPerson().getUUID() == null) {
                BPM.LOG.error("User '{}' has no UUID assigned.", Context.getThreadContext().getPerson().getName());
            } else {
                final String persId = EntityMapper.getUserId(Context.getThreadContext().getPerson().getUUID());
                // final String language = Context.getThreadContext().getLanguage();
                final List<Status> status = new ArrayList<Status>();
                status.add(Status.InProgress);
                status.add(Status.Reserved);
                status.add(Status.Suspended);
                ret.addAll(taskService.getTasksOwnedByStatus(persId, status, "en-UK"));
            }
        } catch (final EFapsException e) {
            BPM.LOG.error("Error on retrieving List of TaskSummaries.");
        }
        return ret;
    }


    /**
     * @param _processInstanceId ProcessInstance the task are wanted for
     * @param _status status
     * @return list of tasks
     */
    public static List<TaskSummary> getTasksByStatusByProcessId(final long _processInstanceId,
                                                                final List<Status> _status)
    {
        final List<TaskSummary> ret = new ArrayList<TaskSummary>();
        if (BPM.processIsActiv(_processInstanceId)) {
            final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext
                            .get(_processInstanceId));
            final TaskService taskService = runtimeEngine.getTaskService();

            ret.addAll(taskService.getTasksByStatusByProcessInstanceId(_processInstanceId, _status, "en-UK"));
        }
        return ret;
    }

    /**
     * @param _processInstanceId ProcessInstance the task are wanted for
     * @param _status status
     * @param _taskName name of the task wanted
     * @return list of tasks
     */
    public static List<TaskSummary> getTasksByStatusByProcessInstanceIdByTaskName(final long _processInstanceId,
                                                                                  final List<Status> _status,
                                                                                  final String _taskName)
    {
        final List<TaskSummary> ret = new ArrayList<TaskSummary>();
        if (BPM.processIsActiv(_processInstanceId)) {
            final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext
                            .get(_processInstanceId));
            final InternalTaskService taskService = (InternalTaskService) runtimeEngine.getTaskService();
            ret.addAll(taskService.getTasksByStatusByProcessInstanceIdByTaskName(_processInstanceId, _status,
                            _taskName));
        }
        return ret;
    }

    /**
     * @param _taskSummary summary the task is wanted for
     * @return internal task object
     */
    public static InternalTask getTaskById(final TaskSummary _taskSummary)
    {
        final RuntimeEngine runtimeEngine = BPM.PMANAGER.getRuntimeEngine(ProcessInstanceIdContext.get(_taskSummary
                        .getProcessInstanceId()));
        final TaskService taskService = runtimeEngine.getTaskService();
        return (InternalTask) taskService.getTaskById(_taskSummary.getId());
    }

    /**
     * @return the TaskAdmin
     */
    public static TaskAdminstration getTaskAdmin()
    {
        final RuntimeEngine runtimeEngine = BPM.SMANAGER.getRuntimeEngine(EmptyContext.get());
        final TaskService taskService = runtimeEngine.getTaskService();
        return new TaskAdminstration((InternalTaskService) taskService);
    }

    /**
     * @return the TaskAdmin
     */
    public static ProcessAdmin getProcessAdmin()
    {
        final RuntimeEngine runtimeEngine = BPM.SMANAGER.getRuntimeEngine(EmptyContext.get());
        return new ProcessAdmin(new JPAAuditLogService(runtimeEngine.getKieSession().getEnvironment()));
    }

    /**
     * Invoke the listener.
     * @param _call call to be executed
     * @param _taskSummary  tasksummary of the task involved
     * @param _values values form eFaps
     */
    private static void invokeAsyncListeners(final AsyncCalls _call,
                                             final TaskSummary _taskSummary,
                                             final Map<String, Object> _values)
    {
        // exec esjp
        try {
            final Class<?> listenerClass = Class.forName("org.efaps.esjp.bpm.listener.ReviewListener", true,
                            EFapsClassLoader.getInstance());
            final IAsyncListener listener = (IAsyncListener) listenerClass.newInstance();

            final Object taskData = BPM.getTaskData(_taskSummary);

            final String userName = Context.getThreadContext().getPerson().getName();
            final ListenerThread threat = new ListenerThread(userName,  listener, _call, _values, taskData);

            final BasicThreadFactory factory = new BasicThreadFactory.Builder()
                            .namingPattern("eFapsBPMListenerThreat-%d")
                            .priority(Thread.MIN_PRIORITY)
                            .build();
            final ExecutorService exec = Executors.newSingleThreadExecutor(factory);
            exec.submit(threat);
            exec.shutdown();

        } catch (final ClassNotFoundException e) {
            BPM.LOG.error("Class could not be found.", e);
        } catch (final InstantiationException e) {
            BPM.LOG.error("Class could not be instantiation.", e);
        } catch (final IllegalAccessException e) {
            BPM.LOG.error("Class could not be accessed.", e);
        } catch (final IllegalArgumentException e) {
            BPM.LOG.error("Illegal Argument.", e);
        } catch (final SecurityException e) {
            BPM.LOG.error("Class could not be found.", e);
        } catch (final EFapsException e) {
            BPM.LOG.error("EFapsException catched", e);
        }
    }

    /**
     * Threat to execute the listeners.
     */
    private static class ListenerThread
        implements Runnable
    {

        /**
         * Listener to be executed.
         */
        private final IAsyncListener listener;

        /**
         * Call to be executed on the listener.
         */
        private final AsyncCalls call;

        /**
         * value mapping.
         */
        private final Map<String, Object> values;

        /**
         * Data from the task.
         */
        private final Object taskData;
        /**
         * UserName the Context will be spanned for.
         */
        private final String userName;

        /**
         * @param _userName userName
         * @param _listener listener
         * @param _call     call
         * @param _values   values
         * @param _taskData taskdata
         */
        public ListenerThread(final String _userName,
                              final IAsyncListener _listener,
                              final AsyncCalls _call,
                              final Map<String, Object> _values,
                              final Object _taskData)
        {
            this.userName = _userName;
            this.listener = _listener;
            this.call = _call;
            this.values = _values;
            this.taskData = _taskData;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see     java.lang.Thread#run()
         */
        @Override
        public void run()
        {
            try {
                if (!Context.isTMActive()) {
                    Context.begin(this.userName, Context.Inheritance.Local);
                }
            } catch (final EFapsException e) {
                BPM.LOG.error("Context problem", e);
            }
            try {
                switch (this.call) {
                    case AFTER_EXECUTE:
                        this.listener.onAfterExecute(this.values, this.taskData);
                        break;
                    default:
                        break;
                }
            } catch (final EFapsException e) {
                BPM.LOG.error("execution problem", e);
            } finally {
                try {
                    if (!Context.isTMNoTransaction()) {
                        if (Context.isTMActive()) {
                            Context.commit();
                        } else {
                            Context.rollback();
                        }
                    }
                } catch (final EFapsException e) {
                    BPM.LOG.error("Context problem", e);
                }
            }
        }
    }
}
