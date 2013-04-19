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

package org.efaps.bpm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.drools.KnowledgeBaseFactory;
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
import org.efaps.bpm.identity.UserGroupCallbackImpl;
import org.efaps.bpm.listener.ProcessEventLstnr;
import org.efaps.bpm.workitem.EsjpWorkItemHandler;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.init.INamingBinds;
import org.efaps.util.EFapsException;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.jbpm.persistence.JpaProcessPersistenceContextManager;
import org.jbpm.persistence.jta.ContainerManagedTransactionManager;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.task.Status;
import org.jbpm.task.admin.TasksAdmin;
import org.jbpm.task.identity.UserGroupCallbackManager;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.persistence.TaskSessionFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Bpm
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Bpm.class);

    /**
     * SQL select statement to select a type from the database by its UUID.
     */
    private static final String SQL_SESSIONID = new SQLSelect()
                    .column("ID")
                    .from("ht_session_info", 0)
                    .addPart(SQLPart.ORDERBY).addValuePart("last_modification_date")
                    .toString();


    private static Bpm bpm;
    private Integer ksessionId;

   // private TaskService taskService;

    private final Map<String, WorkItemHandler> workItemsHandlers = new HashMap<String, WorkItemHandler>();

    //private org.jbpm.task.TaskService service;


    /**
     * Getter method for the instance variable {@link #workItemsHandlers}.
     *
     * @return value of instance variable {@link #workItemsHandlers}
     */
    protected Map<String, WorkItemHandler> getWorkItemsHandlers()
    {
        return this.workItemsHandlers;
    }


    private TasksAdmin taskAdmin;

    private KnowledgeBase kbase;

    private Environment env;

    private static final String[] KNOWN_UT_JNDI_KEYS = new String[] {
                    "java:global/" + INamingBinds.RESOURCE_USERTRANSACTION,
                    "java:comp/env/" + INamingBinds.RESOURCE_USERTRANSACTION };

    private static final String[] KNOWN_TM_JNDI_KEYS = new String[] {
                    "java:global/" + INamingBinds.RESOURCE_TRANSMANAG,
                    "java:comp/env/" + INamingBinds.RESOURCE_TRANSMANAG };

    /**
     * Create Singelton.
     */
    private Bpm()
    {
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
            final PreparedStatement stmt = con.getConnection().prepareStatement(Bpm.SQL_SESSIONID);
            final ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ret = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            con.commit();
        } catch (final EFapsException e) {
            Bpm.LOG.error("initialiseCache()", e);
        } catch (final SQLException e) {
            Bpm.LOG.error("initialiseCache()", e);
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


    public static void initialize() throws EFapsException
    {
        final SystemConfiguration config = EFapsSystemConfiguration.KERNEL.get();
        final boolean active = config != null
                        ? config.getAttributeValueAsBoolean(KernelSettings.ActivateBPM) : false;
        if (active) {

            Bpm.bpm = new Bpm();
            Bpm.bpm.ksessionId = Bpm.bpm.getKSessionIDFromDB();

            System.setProperty(UserGroupCallbackManager.USER_GROUP_CALLBACK_KEY, UserGroupCallbackImpl.class.getName());

            final Properties props = new Properties();
            props.setProperty(JavaDialectConfiguration.JAVA_COMPILER_PROPERTY, "JANINO");

            final KnowledgeBuilderConfiguration bldrConfig = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(props, Bpm.class.getClassLoader());

            final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(bldrConfig);

            kbuilder.add(ResourceFactory.newClassPathResource("org/efaps/bpm/MyProcess.bpmn"), ResourceType.BPMN2);
            kbuilder.add(ResourceFactory.newClassPathResource("org/efaps/bpm/HumanTask.bpmn"), ResourceType.BPMN2);
            kbuilder.add(ResourceFactory.newClassPathResource("org/efaps/bpm/EsjpProcess.bpmn"), ResourceType.BPMN2);
            kbuilder.add(ResourceFactory.newClassPathResource("org/efaps/bpm/DocAproveTask.bpmn2"), ResourceType.BPMN2);

            Bpm.bpm.kbase = kbuilder.newKnowledgeBase();

            final Map<String, String> properties = new HashMap<String, String>();
            properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQL82Dialect");
            properties.put(AvailableSettings.SHOW_SQL, String.valueOf(Bpm.LOG.isInfoEnabled()));
            properties.put(AvailableSettings.FORMAT_SQL, "true");

           // properties.put(AvailableSettings.AUTOCOMMIT, "true");
            properties.put("drools.processInstanceManagerFactory", "org.jbpm.persistence.processinstance.JPAProcessInstanceManagerFactory");
            properties.put("drools.processSignalManagerFactory", "org.jbpm.persistence.processinstance.JPASignalManagerFactory");



//            properties.put(AvailableSettings.FLUSH_BEFORE_COMPLETION, "true");

            properties.put(AvailableSettings.SESSION_FACTORY_NAME, "java:comp/env/test");
            properties.put(AvailableSettings.RELEASE_CONNECTIONS, "after_transaction");
            properties.put(AvailableSettings.CONNECTION_PROVIDER, ConnectionProvider.class.getName());


            properties.put(org.hibernate.ejb.AvailableSettings.NAMING_STRATEGY, NamingStrategy.class.getName());

            final EntityManagerFactory emf = Persistence
                            .createEntityManagerFactory("org.jbpm.persistence.jpa", properties);

            Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa2", properties);

            Bpm.bpm.env = KnowledgeBaseFactory.newEnvironment();
            Bpm.bpm.env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);


            Bpm.bpm.env.set(EnvironmentName.TRANSACTION_MANAGER, new ContainerManagedTransactionManager());
            Bpm.bpm.env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER,
                            new JpaProcessPersistenceContextManager(Bpm.bpm.env));


            UserTransaction userTrans = null;
            InitialContext context = null;
            try {
                context = new InitialContext();
                userTrans = Bpm.findUserTransaction();
                Bpm.bpm.env.set(EnvironmentName.TRANSACTION, userTrans);
                context.bind(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, userTrans);
                context.bind(JtaTransactionManager.FALLBACK_TRANSACTION_MANAGER_NAMES[0], Bpm.findTransactionManager());
            } catch (final NamingException ex) {
                Bpm.LOG.error("Could not initialise JNDI InitialContext", ex);
            }

            final StatefulKnowledgeSession ksession = Bpm.bpm.getKnowledgeSession();

            UserTaskService.getTaskService(ksession);

            final EsjpWorkItemHandler esjphandler = new EsjpWorkItemHandler();
            ksession.getWorkItemManager().registerWorkItemHandler("ESJPNode", esjphandler);
            Bpm.bpm.workItemsHandlers.put("ESJPNode", esjphandler);

            SessionFactory sessionFactory;
            try {
                sessionFactory = (SessionFactory) context.lookup("java:comp/env/test");
                sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);

            } catch (final NamingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }




    public static class TaskSessionFactory
        extends TaskSessionFactoryImpl
    {

        /**
         * @param _taskService
         * @param _emf
         */
        public TaskSessionFactory(final TaskService _taskService,
                                  final EntityManagerFactory _emf)
        {
            super(_taskService, _emf);
        }

    }

    public static void startProcess()
    {
        InitialContext context = null;
        try {
            context = new InitialContext();
            final SessionFactory sessionFactory = (SessionFactory) context.lookup("java:comp/env/test");
            System.out.println(sessionFactory);

            sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);

            final StatefulKnowledgeSession ksession = Bpm.bpm.getKnowledgeSession();
            UserTaskService.getTaskService(ksession);

            ksession.startProcess("com.sample.hello");

            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("docOID", "1234.1234");

           //ksession.startProcess("com.sample.humantask", params);


           //ksession.startProcess("com.sample.esjp", params);


            ksession.startProcess("org.efaps.DocAproveTask", params);

            sessionFactory.getCurrentSession().flush();
        } catch (final NamingException ex) {
            Bpm.LOG.error("Could not initialise JNDI InitialContext", ex);
        }
    }

    /**
     * @param _taskSummary  TaskSummary
     * @param _decision     one of true, false, null
     * @param _values       mapping of additional values
     */
    public static void executeTask(final TaskSummary _taskSummary,
                                   final Boolean _decision,
                                   final Map<String, Object> _values)
    {
        final StatefulKnowledgeSession ksession = Bpm.bpm.getKnowledgeSession();
        final org.jbpm.task.TaskService service = UserTaskService.getTaskService(ksession);
        // check if must be claimed still
        if (Status.Ready.equals(_taskSummary.getStatus())) {
           service.claim(_taskSummary.getId(), "sales-rep");
        }
        if (Status.InProgress.equals(_taskSummary.getStatus())) {
            service.resume(_taskSummary.getId(), "sales-rep");
        } else {
            service.start(_taskSummary.getId(), "sales-rep");
        }
        final Parameter parameter = new Parameter();
        parameter.put(ParameterValues.BPM_TASK, _taskSummary);
        parameter.put(ParameterValues.BPM_VALUES, _values);
        parameter.put(ParameterValues.BPM_DECISION, _decision);
        final Map<String, Object> results = new HashMap<String, Object>();
        results.put("comment", "Agreed, existing laptop needs replacing");
        // exec esjp
        try {
            final Class<?> transformer = Class.forName("org.efaps.esjp.bpm.TaskTransformer");
            final Method method = transformer.getMethod("execute",  new Class[] { Parameter.class });
            final Return ret  = (Return) method.invoke(transformer.newInstance(), parameter);
            if (ret != null) {
                results.put("resultTest", ret.get(ReturnValues.VALUES));
            }
        } catch (final ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        service.completeWithResults(_taskSummary.getId(), "sales-rep", results);

    }


    protected static UserTransaction findUserTransaction()
    {
        UserTransaction ret = null;
        InitialContext context = null;
        try {
            context = new InitialContext();
        } catch (final NamingException ex) {
            Bpm.LOG.error("Could not initialise JNDI InitialContext", ex);
        }
        for (final String utLookup : Bpm.KNOWN_UT_JNDI_KEYS) {
            if (utLookup != null) {
                try {
                    ret = (UserTransaction) context.lookup(utLookup);
                    Bpm.LOG.info("User Transaction found in JNDI under '{}'", utLookup);
                } catch (final NamingException e) {
                    Bpm.LOG.debug("User Transaction not found in JNDI under '{}'", utLookup);
                }
            }
        }
        if (ret == null) {
            Bpm.LOG.warn("No user transaction found under known names");
        }
        return ret;
    }

    protected static TransactionManager findTransactionManager()
    {
        TransactionManager ret = null;
        InitialContext context = null;
        try {
            context = new InitialContext();
        } catch (final NamingException ex) {
            Bpm.LOG.error("Could not initialise JNDI InitialContext", ex);
        }
        for (final String utLookup : Bpm.KNOWN_TM_JNDI_KEYS) {
            if (utLookup != null) {
                try {
                    ret = (TransactionManager) context.lookup(utLookup);
                    Bpm.LOG.info("TransactionManager found in JNDI under '{}'", utLookup);
                } catch (final NamingException e) {
                    Bpm.LOG.debug("TransactionManager not found in JNDI under '{}'", utLookup);
                }
            }
        }
        if (ret == null) {
            Bpm.LOG.warn("No TransactionManager found under known names");
        }
        return ret;
    }

    public static List<TaskSummary> getTasksAssignedAsPotentialOwner()
    {
        final StatefulKnowledgeSession ksession = Bpm.bpm.getKnowledgeSession();
        final org.jbpm.task.TaskService service = UserTaskService.getTaskService(ksession);

        return service.getTasksAssignedAsPotentialOwner("sales-rep", "en-UK");
    }


    private StatefulKnowledgeSession getKnowledgeSession() {
        StatefulKnowledgeSession ksession;
        if (this.ksessionId == null) {
            ksession = JPAKnowledgeService.newStatefulKnowledgeSession(
                    this.kbase,
                    null,
                    this.env);

            this.ksessionId = ksession.getId();
        } else {
            ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(
                    this.ksessionId,
                    this.kbase,
                    null,
                    this.env);
        }

        for (final Map.Entry<String, WorkItemHandler> entry : this.workItemsHandlers.entrySet()) {
            ksession.getWorkItemManager().registerWorkItemHandler(entry.getKey(), entry.getValue());
        }

        ksession.addEventListener(new ProcessEventLstnr());
        final JPAWorkingMemoryDbLogger logger = new JPAWorkingMemoryDbLogger(ksession);
        ksession.addEventListener(logger);

        //Configures a logger for the session
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
        try {
            Context.getThreadContext().setKsession(ksession);
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ksession;
    }

    protected static void registerWorkItemHandler(final String _key, final WorkItemHandler _object)
    {
        Bpm.bpm.workItemsHandlers.put(_key, _object);
    }
}
