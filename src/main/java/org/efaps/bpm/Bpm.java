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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.persistence.jta.JtaTransactionManager;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.efaps.bpm.identity.UserGroupCallbackImpl;
import org.efaps.bpm.listener.ProcessEventLstnr;
import org.efaps.bpm.listener.SystemEventLstnr;
import org.efaps.init.INamingBinds;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.Status;
import org.jbpm.task.admin.TasksAdmin;
import org.jbpm.task.identity.UserGroupCallbackManager;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.DefaultEscalatedDeadlineHandler;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.local.LocalTaskService;
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

    private static Bpm bpm;

    private StatefulKnowledgeSession ksession;
    private TaskService taskService;


    private org.jbpm.task.TaskService service;

    private TasksAdmin taskAdmin;

    private KnowledgeBase kbase;

    private Environment env;
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Bpm.class);

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

    public static void initialize()
    {
        if (Bpm.bpm != null) {
            Bpm.bpm.ksession.dispose();
        }

        Bpm.bpm = new Bpm();

        System.setProperty(UserGroupCallbackManager.USER_GROUP_CALLBACK_KEY, UserGroupCallbackImpl.class.getName());

        final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(ResourceFactory.newClassPathResource("org/efaps/bpm/MyProcess.bpmn"), ResourceType.BPMN2);
        kbuilder.add(ResourceFactory.newClassPathResource("org/efaps/bpm/HumanTask.bpmn"), ResourceType.BPMN2);

        Bpm.bpm.kbase = kbuilder.newKnowledgeBase();

        final Map<String, String> properties = new HashMap<String, String>();
        properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        properties.put(AvailableSettings.SHOW_SQL, String.valueOf(Bpm.LOG.isInfoEnabled()));
        properties.put(AvailableSettings.FORMAT_SQL, "true");
        properties.put(AvailableSettings.AUTOCOMMIT, "false");
        //properties.put(org.hibernate.cfg.Environment.AUTO_CLOSE_SESSION, "true");
        properties.put(AvailableSettings.FLUSH_BEFORE_COMPLETION, "true");
        properties.put(AvailableSettings.SESSION_FACTORY_NAME, "java:comp/env/test");


        properties.put(org.hibernate.ejb.AvailableSettings.NAMING_STRATEGY, NamingStrategy.class.getName());

        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa", properties);

        Bpm.bpm.env = KnowledgeBaseFactory.newEnvironment();
        Bpm.bpm.env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        Bpm.bpm.env.set(EnvironmentName.TRANSACTION_MANAGER, Bpm.findTransactionManager());

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

        Bpm.bpm.ksession = JPAKnowledgeService.newStatefulKnowledgeSession(Bpm.bpm.kbase, null,  Bpm.bpm.env);
        Bpm.bpm.ksession.addEventListener(new ProcessEventLstnr());
        final JPAWorkingMemoryDbLogger logger = new JPAWorkingMemoryDbLogger(Bpm.bpm.ksession);
        Bpm.bpm.ksession.addEventListener(logger);

        Bpm.bpm.taskService = new TaskService();

        Bpm.bpm.taskService.setTaskSessionFactory(new TaskSessionFactory(Bpm.bpm.taskService, emf));
        Bpm.bpm.taskService.setSystemEventListener(new SystemEventLstnr());
        Bpm.bpm.taskService.setEscalatedDeadlineHandler( new DefaultEscalatedDeadlineHandler());
        Bpm.bpm.taskService.initialize();

       //  Bpm.bpm.service = LocalHumanTaskService.getTaskService(Bpm.bpm.ksession);

        final LocalHTWorkItemHandler humanTaskHandler = new LocalHTWorkItemHandler(
                        new LocalTaskService(Bpm.bpm.taskService), Bpm.bpm.ksession);
        Bpm.bpm.ksession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);

        Bpm.bpm.service = new LocalTaskService( Bpm.bpm.taskService);

        Bpm.bpm.taskAdmin =  Bpm.bpm.taskService.createTaskAdmin();
        humanTaskHandler.connect();
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

        } catch (final NamingException ex) {
            Bpm.LOG.error("Could not initialise JNDI InitialContext", ex);
        }
        Bpm.bpm.ksession.getId();
        //Bpm.bpm.ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(Bpm.bpm.ksession.getId(),Bpm.bpm.kbase, null,  Bpm.bpm.env);
        Bpm.bpm.ksession.startProcess("com.sample.hello");

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", "krisv");
        params.put("description", "Need a new laptop computer");

        Bpm.bpm.ksession.startProcess("com.sample.humantask", params);


        final List<TaskSummary> summar = Bpm.bpm.taskAdmin.getActiveTasks();
        System.out.println(summar);


        // "sales-rep" reviews request
        final List<TaskSummary> summaries = Bpm.bpm.service.getTasksAssignedAsPotentialOwner("sales-rep", "en-UK");
        System.out.println(summaries);
        if (!summaries.isEmpty()) {
            final TaskSummary taskSumary = summaries.get(0);
            if (Status.Ready.equals(taskSumary.getStatus())) {
                Bpm.bpm.service.claim(taskSumary.getId(), "sales-rep");
                Bpm.LOG.debug("Sales-rep claimed task '{}' ({} : )", taskSumary.getName(), taskSumary.getId(),
                                taskSumary.getDescription());
            }
        }
        final List<TaskSummary> summaries2 =  Bpm.bpm.service.getTasksOwned("sales-rep", "en-UK");
        if (!summaries2.isEmpty()) {
            final TaskSummary taskSumary = summaries2.get(0);
            Bpm.bpm.service.start(taskSumary.getId(), "sales-rep");
            Bpm.bpm.service.completeWithResults(taskSumary.getId(), "sales-rep", "No hay result");
        }

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
}

