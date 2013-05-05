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
 * Revision:        $Rev: 9284 $
 * Last Changed:    $Date: 2013-04-26 10:40:07 -0500 (Fri, 26 Apr 2013) $
 * Last Changed By: $Author: jan@moxter.net $
 */

package org.efaps.bpm;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.SessionConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.persistence.jta.JtaTransactionManager;
import org.drools.rule.builder.dialect.java.JavaDialectConfiguration;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItemHandler;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.bpm.identity.UserGroupCallbackImpl;
import org.efaps.bpm.listener.ProcessEventLstnr;
import org.efaps.bpm.workitem.EsjpWorkItemHandler;
import org.efaps.bpm.workitem.SignallingHandlerWrapper;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.init.INamingBinds;
import org.efaps.util.EFapsException;
import org.hibernate.cfg.AvailableSettings;
import org.jbpm.persistence.JpaProcessPersistenceContextManager;
import org.jbpm.persistence.jta.ContainerManagedTransactionManager;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.task.Content;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.identity.UserGroupCallbackManager;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.utils.ContentMarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: BPM.java 9284 2013-04-26 15:40:07Z jan@moxter.net $
 */
public final class BPM
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BPM.class);

    /**
     * SQL select statement to select a type from the database by its UUID.
     */
    private static final String SQL_SESSIONID = new SQLSelect()
                    .column("ID")
                    .from("ht_session_info", 0)
                    .addPart(SQLPart.ORDERBY).addValuePart("last_modification_date")
                    .toString();

    /**
     * Sequence used to search for the UserTransaction inside JNDI.
     */
    private static final String[] KNOWN_UT_JNDI_KEYS = new String[] {
        "java:global/" + INamingBinds.RESOURCE_USERTRANSACTION,
        "java:comp/env/" + INamingBinds.RESOURCE_USERTRANSACTION };

    /**
     * Sequence used to search for the Transactionmanager inside JNDI.
     */

    private static final String[] KNOWN_TM_JNDI_KEYS = new String[] {
        "java:global/" + INamingBinds.RESOURCE_TRANSMANAG,
        "java:comp/env/" + INamingBinds.RESOURCE_TRANSMANAG };

    /**
     * The used Bpm instance.
     */
    private static BPM BPMINSTANCE;

    /**
     * Mapping of the WorkItemhandlers used in this session.
     */
    private final Map<String, WorkItemHandler> workItemsHandlers = new HashMap<String, WorkItemHandler>();

    /**
     * Id of the KnowledgeSession.
     */
    private Integer ksessionId;

    /**
     * Instance of the used KnowledgeBase.
     */
    private KnowledgeBase kbase;

    /**
     * Environment used for hibernate persistence.
     */
    private Environment env;

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
        final SystemConfiguration config = EFapsSystemConfiguration.KERNEL.get();
        final boolean active = config != null
                        ? config.getAttributeValueAsBoolean(KernelSettings.ACTIVATE_BPM) : false;
        if (active) {

            BPM.BPMINSTANCE = new BPM();
            BPM.BPMINSTANCE.ksessionId = BPM.BPMINSTANCE.getKSessionIDFromDB();

            System.setProperty(UserGroupCallbackManager.USER_GROUP_CALLBACK_KEY, UserGroupCallbackImpl.class.getName());

            final Properties knowledgeBldrProps = new Properties();
            knowledgeBldrProps.setProperty(JavaDialectConfiguration.JAVA_COMPILER_PROPERTY, "ECLIPSE");
            knowledgeBldrProps.setProperty("drools.dialect.java.compiler.lnglevel", "1.6");

            final KnowledgeBuilderConfiguration knowledgeBldrConfig = KnowledgeBuilderFactory
                            .newKnowledgeBuilderConfiguration(
                                            knowledgeBldrProps, EFapsClassLoader.getInstance());

            final KnowledgeBuilder knowledgeBldr = KnowledgeBuilderFactory.newKnowledgeBuilder(knowledgeBldrConfig);

            BPM.add2KnowledgeBuilder(knowledgeBldr);

            final Properties kowledgeBaseProps = new Properties();

            final KnowledgeBaseConfiguration kowledgeBaseConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(
                            kowledgeBaseProps, EFapsClassLoader.getInstance());

            BPM.BPMINSTANCE.kbase = KnowledgeBaseFactory.newKnowledgeBase(kowledgeBaseConfig);

            if (knowledgeBldr.hasErrors()) {
                BPM.LOG.error("Could not build Knowledge Packages: {}", knowledgeBldr.getErrors());
            }

            BPM.BPMINSTANCE.kbase.addKnowledgePackages(knowledgeBldr.getKnowledgePackages());
            final Map<String, String> properties = new HashMap<String, String>();
            properties.put(AvailableSettings.DIALECT, Context.getDbType().getHibernateDialect());
            properties.put(AvailableSettings.SHOW_SQL, String.valueOf(BPM.LOG.isDebugEnabled()));
            properties.put(AvailableSettings.FORMAT_SQL, "true");
            properties.put("drools.processInstanceManagerFactory",
                            "org.jbpm.persistence.processinstance.JPAProcessInstanceManagerFactory");
            properties.put("drools.processSignalManagerFactory",
                            "org.jbpm.persistence.processinstance.JPASignalManagerFactory");
            // we might need that later
            // properties.put(AvailableSettings.SESSION_FACTORY_NAME,
            // "java:comp/env/test");
            properties.put(AvailableSettings.RELEASE_CONNECTIONS, "after_transaction");
            properties.put(AvailableSettings.CONNECTION_PROVIDER, ConnectionProvider.class.getName());

            properties.put(org.hibernate.ejb.AvailableSettings.NAMING_STRATEGY, NamingStrategy.class.getName());

            final EntityManagerFactory emf = Persistence
                            .createEntityManagerFactory("org.jbpm.persistence.jpa", properties);

            BPM.BPMINSTANCE.env = KnowledgeBaseFactory.newEnvironment();
            BPM.BPMINSTANCE.env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

            BPM.BPMINSTANCE.env.set(EnvironmentName.TRANSACTION_MANAGER, new ContainerManagedTransactionManager());
            BPM.BPMINSTANCE.env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER,
                            new JpaProcessPersistenceContextManager(BPM.BPMINSTANCE.env));

            UserTransaction userTrans = null;
            InitialContext context = null;
            try {
                context = new InitialContext();
                userTrans = BPM.findUserTransaction();
                BPM.BPMINSTANCE.env.set(EnvironmentName.TRANSACTION, userTrans);
                Object object = null;
                try {
                    object = context.lookup(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME);
                } catch (final NamingException ex) {
                    BPM.LOG.info("Checked for JtaTransactionManager");
                }
                if (object == null) {
                    context.bind(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, userTrans);
                    context.bind(JtaTransactionManager.FALLBACK_TRANSACTION_MANAGER_NAMES[0],
                                    BPM.findTransactionManager());
                }
            } catch (final NamingException ex) {
                BPM.LOG.error("Could not initialise JNDI InitialContext", ex);
            }

            final StatefulKnowledgeSession ksession = BPM.BPMINSTANCE.getKnowledgeSession();

            UserTaskService.getTaskService(ksession);

            final EsjpWorkItemHandler esjphandler = new EsjpWorkItemHandler();
            ksession.getWorkItemManager().registerWorkItemHandler("ESJPNode", esjphandler);
            BPM.BPMINSTANCE.workItemsHandlers.put("ESJPNode", esjphandler);
        }
    }

    /**
     * @param _kbuilder KnowledgeBuilder
     * @throws EFapsException on eror
     */
    private static void add2KnowledgeBuilder(final KnowledgeBuilder _kbuilder)
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.BPM);
        final InstanceQuery query = queryBldr.getQuery();
        query.executeWithoutAccessCheck();
        while (query.next()) {
            final Checkout checkout = new Checkout(query.getCurrentValue());
            final InputStream in = checkout.execute();
            _kbuilder.add(ResourceFactory.newInputStreamResource(in), ResourceType.BPMN2);
        }
    }

    /**
     * @param _processId    id of the process to start
     * @param _params       parameter map for the task
     */
    public static void startProcess(final String _processId,
                                    final Map<String, Object> _params)
    {
        final StatefulKnowledgeSession ksession = BPM.BPMINSTANCE.getKnowledgeSession();
        UserTaskService.getTaskService(ksession);
        ksession.startProcess(_processId, _params);
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
        final StatefulKnowledgeSession ksession = BPM.BPMINSTANCE.getKnowledgeSession();
        final org.jbpm.task.TaskService service = UserTaskService.getTaskService(ksession);
        ksession.signalEvent("", null);
        // check if must be claimed still
        if (Status.Ready.equals(_taskSummary.getStatus())) {
            service.claim(_taskSummary.getId(), Context.getThreadContext().getPerson().getName());
        }
        if (Status.InProgress.equals(_taskSummary.getStatus())) {
            service.resume(_taskSummary.getId(), Context.getThreadContext().getPerson().getName());
        } else {
            service.start(_taskSummary.getId(), Context.getThreadContext().getPerson().getName());
        }

        final Parameter parameter = new Parameter();
        parameter.put(ParameterValues.BPM_TASK, _taskSummary);
        parameter.put(ParameterValues.BPM_VALUES, _values);
        parameter.put(ParameterValues.BPM_DECISION, _decision);
        final Map<String, Object> results = new HashMap<String, Object>();

        // exec esjp
        try {
            final Class<?> transformer = Class.forName("org.efaps.esjp.bpm.TaskTransformer", true,
                            EFapsClassLoader.getInstance());
            final Method method = transformer.getMethod("execute", new Class[] { Parameter.class });
            final Return ret = (Return) method.invoke(transformer.newInstance(), parameter);
            if (ret != null) {
                results.put("resultTest", ret.get(ReturnValues.VALUES));
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
        service.completeWithResults(_taskSummary.getId(), Context.getThreadContext().getPerson().getName(), results);
    }

    /**
     * @param _taskSummary tasksummary the data is wanted for
     * @return the object data
     */
    public static Object getTaskData(final TaskSummary _taskSummary)
    {
        Object ret = null;
        final StatefulKnowledgeSession ksession = BPM.BPMINSTANCE.getKnowledgeSession();
        final org.jbpm.task.TaskService service = UserTaskService.getTaskService(ksession);
        final Task task = service.getTask(_taskSummary.getId());
        final long contentId = task.getTaskData().getDocumentContentId();
        if (contentId != -1) {
            final Content content = service.getContent(contentId);
            ret = ContentMarshallerHelper.unmarshall(content.getContent(), ksession.getEnvironment(),
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
        final StatefulKnowledgeSession ksession = BPM.BPMINSTANCE.getKnowledgeSession();
        final org.jbpm.task.TaskService service = UserTaskService.getTaskService(ksession);
        try {
            final String persname = Context.getThreadContext().getPerson().getName();
            // final String language = Context.getThreadContext().getLanguage();
            ret.addAll(service.getTasksAssignedAsPotentialOwner(persname, "en-UK"));
        } catch (final EFapsException e) {
            BPM.LOG.error("Error on retrieving List of TaskSummaries.");
        }
        return ret;
    }

    /**
     * @return the usertransaction
     */
    protected static UserTransaction findUserTransaction()
    {
        UserTransaction ret = null;
        InitialContext context = null;
        try {
            context = new InitialContext();
        } catch (final NamingException ex) {
            BPM.LOG.error("Could not initialise JNDI InitialContext", ex);
        }
        for (final String utLookup : BPM.KNOWN_UT_JNDI_KEYS) {
            if (utLookup != null) {
                try {
                    ret = (UserTransaction) context.lookup(utLookup);
                    BPM.LOG.info("User Transaction found in JNDI under '{}'", utLookup);
                } catch (final NamingException e) {
                    BPM.LOG.debug("User Transaction not found in JNDI under '{}'", utLookup);
                }
            }
        }
        if (ret == null) {
            BPM.LOG.warn("No user transaction found under known names");
        }
        return ret;
    }

    /**
     * @return the transactionmanager
     */
    protected static TransactionManager findTransactionManager()
    {
        TransactionManager ret = null;
        InitialContext context = null;
        try {
            context = new InitialContext();
        } catch (final NamingException ex) {
            BPM.LOG.error("Could not initialise JNDI InitialContext", ex);
        }
        for (final String utLookup : BPM.KNOWN_TM_JNDI_KEYS) {
            if (utLookup != null) {
                try {
                    ret = (TransactionManager) context.lookup(utLookup);
                    BPM.LOG.info("TransactionManager found in JNDI under '{}'", utLookup);
                } catch (final NamingException e) {
                    BPM.LOG.debug("TransactionManager not found in JNDI under '{}'", utLookup);
                }
            }
        }
        if (ret == null) {
            BPM.LOG.warn("No TransactionManager found under known names");
        }
        return ret;
    }

    /**
     * @param _key              the key for the WorkItemHandler
     * @param _workItemHandler   WorkItemHandler to add
     */
    protected static void registerWorkItemHandler(final String _key,
                                                  final WorkItemHandler _workItemHandler)
    {
        BPM.BPMINSTANCE.workItemsHandlers.put(_key, _workItemHandler);
    }

    /**
     * @return the id of the session from the database.
     * @throws EFapsException on error
     */
    private Integer getKSessionIDFromDB()
        throws EFapsException
    {
        Integer ret = null;
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            if (Context.getDbType().existsTable(con.getConnection(), "ht_session_info")) {
                final PreparedStatement stmt = con.getConnection().prepareStatement(BPM.SQL_SESSIONID);
                final ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    ret = rs.getInt(1);
                }
                rs.close();
                stmt.close();
            }
            con.commit();
        } catch (final EFapsException e) {
            BPM.LOG.error("initialiseCache()", e);
        } catch (final SQLException e) {
            BPM.LOG.error("initialiseCache()", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                try {
                    con.abort();
                } catch (final EFapsException e) {
                    throw new EFapsException("could not read session id", e);
                }
            }
        }
        return ret;
    }

    /**
     * @return the KnowledgeSession
     */
    private StatefulKnowledgeSession getKnowledgeSession()
    {
        StatefulKnowledgeSession ksession;
        if (this.ksessionId == null) {
            ksession = JPAKnowledgeService.newStatefulKnowledgeSession(
                            this.kbase,
                            new SessionConfiguration(EFapsClassLoader.getInstance()),
                            this.env);

            this.ksessionId = ksession.getId();
        } else {
            ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(
                            this.ksessionId,
                            this.kbase,
                            new SessionConfiguration(EFapsClassLoader.getInstance()),
                            this.env);
        }

        for (final Map.Entry<String, WorkItemHandler> entry : this.workItemsHandlers.entrySet()) {
            final SignallingHandlerWrapper sigWrapper = new SignallingHandlerWrapper(entry.getValue(), null,
                            ksession);
            ksession.getWorkItemManager().registerWorkItemHandler(entry.getKey(), sigWrapper);
        }

        ksession.addEventListener(new ProcessEventLstnr());
        final JPAWorkingMemoryDbLogger logger = new JPAWorkingMemoryDbLogger(ksession);
        ksession.addEventListener(logger);

        // Configures a logger for the session
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
        return ksession;
    }
}
