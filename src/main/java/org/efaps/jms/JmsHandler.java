/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class JmsHandler
{

    /**
     * Mapping of JNDI-Name of the ConnectionFactory to QueueConnection.
     */
    private static final Map<String, QueueConnection> QUEUE2QUECONN = new HashMap<String, QueueConnection>();

    /**
     * Mapping of JNDI-Name of the ConnectionFactory to TopicConnection.
     */
    private static final Map<String, TopicConnection> TOPIC2QUECONN = new HashMap<String, TopicConnection>();

    /**
     * Mapping of a name to a JmsDefinition.
     */
    private static final Map<String, JmsDefinition> NAME2DEF = new HashMap<String, JmsDefinition>();


    /**
     * Create Singelton.
     */
    private JmsHandler()
    {
    }

    /**
     * Initialize the Jms.
     * @throws EFapsException on error
     */
    public static void initialize()
        throws EFapsException
    {
        try {
            //Kernel-Configuration
            final SystemConfiguration config = SystemConfiguration.get(
                            UUID.fromString("acf2b19b-f7c4-4e4a-a724-fb2d9ed30079"));
            if (config != null) {
                final int timeout = config.getAttributeValueAsInteger(JmsSession.SESSIONTIMEOUTKEY);
                if (timeout > 0) {
                    JmsSession.setSessionTimeout(timeout);
                }
            }
            // this check is necessary for first install and update
            if (CIAdminCommon.JmsAbstract.getType() != null) {
                // remove any existing
                JmsHandler.stop();
                final Context ctx = new InitialContext();
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.JmsAbstract);
                final MultiPrintQuery multi = queryBldr.getPrint();
                multi.addAttribute(CIAdminCommon.JmsAbstract.Type,
                                CIAdminCommon.JmsAbstract.Name,
                                CIAdminCommon.JmsAbstract.ConnectionFactoryJNDI,
                                CIAdminCommon.JmsAbstract.DestinationJNDI);
                final SelectBuilder sel = new SelectBuilder().linkto(CIAdminCommon.JmsAbstract.ESJPLink)
                                .file().label();
                multi.addSelect(sel);
                multi.executeWithoutAccessCheck();
                while (multi.next()) {
                    final Type type = multi.<Type>getAttribute(CIAdminCommon.JmsAbstract.Type);
                    final String connectionFactoryJNDI = multi
                                    .<String>getAttribute(CIAdminCommon.JmsAbstract.ConnectionFactoryJNDI);
                    final String destinationJNDI = multi
                                    .<String>getAttribute(CIAdminCommon.JmsAbstract.DestinationJNDI);
                    final String name = multi.<String>getAttribute(CIAdminCommon.JmsAbstract.Name);
                    final String esjp = multi.<String>getSelect(sel);

                    Session session;
                    if (type.isKindOf(CIAdminCommon.JmsQueueAbstract.getType())) {
                        final QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) ctx
                                        .lookup(connectionFactoryJNDI);
                        final QueueConnection queueConnection;
                        if (JmsHandler.QUEUE2QUECONN.containsKey(connectionFactoryJNDI)) {
                            queueConnection = JmsHandler.QUEUE2QUECONN.get(connectionFactoryJNDI);
                        } else {
                            queueConnection = queueConnectionFactory.createQueueConnection();
                        }
                        JmsHandler.QUEUE2QUECONN.put(connectionFactoryJNDI, queueConnection);
                        session = queueConnection.createQueueSession(false,
                                        Session.AUTO_ACKNOWLEDGE);
                        queueConnection.start();
                    } else {
                        final TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) ctx
                                        .lookup(connectionFactoryJNDI);
                        final TopicConnection topicConn;
                        if (JmsHandler.TOPIC2QUECONN.containsKey(connectionFactoryJNDI)) {
                            topicConn = JmsHandler.TOPIC2QUECONN.get(connectionFactoryJNDI);
                        } else {
                            topicConn = topicConnectionFactory.createTopicConnection();
                        }
                        JmsHandler.TOPIC2QUECONN.put(connectionFactoryJNDI, topicConn);
                        if (type.isKindOf(CIAdminCommon.JmsTopicDurableConsumer.getType())) {
                            topicConn.setClientID(org.efaps.db.Context.getThreadContext().getPath() + ":" + name);
                        }
                        session = topicConn.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
                        topicConn.start();
                    }
                    final Destination dest = (Destination) ctx.lookup(destinationJNDI);

                    if (type.isKindOf(CIAdminCommon.JmsQueueProducer.getType())
                                    || type.isKindOf(CIAdminCommon.JmsTopicProducer.getType())) {
                        final MessageProducer producer;
                        if (type.isKindOf(CIAdminCommon.JmsQueueProducer.getType())) {
                            producer = session.createProducer(dest);
                        } else {
                            producer = ((TopicSession) session).createPublisher((Topic) dest);
                        }
                        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                        JmsHandler.NAME2DEF.put(name, new JmsDefinition(name, producer, session));
                    } else {
                        final MessageConsumer consumer;
                        if (type.isKindOf(CIAdminCommon.JmsQueueConsumer.getType())) {
                            consumer = session.createConsumer(dest);
                        } else if (type.isKindOf(CIAdminCommon.JmsTopicDurableConsumer.getType())) {
                            consumer = session.createDurableSubscriber((Topic) dest, name);
                        } else {
                            consumer = ((TopicSession) session).createSubscriber((Topic) dest);
                        }
                        @SuppressWarnings("unchecked")
                        final Class<? extends MessageListener> clazz = (Class<? extends MessageListener>) Class.forName(
                                        esjp.trim(), false, new EFapsClassLoader(JmsHandler.class.getClassLoader()));
                        final MessageListener myListener = clazz.newInstance();
                        consumer.setMessageListener(myListener);
                    }

                }
            }
        } catch (final NamingException e) {
            throw new EFapsException("NamingException", e);
        } catch (final JMSException e) {
            throw new EFapsException("JMSException", e);
        } catch (final ClassNotFoundException e) {
            throw new EFapsException("ClassNotFoundException", e);
        } catch (final InstantiationException e) {
            throw new EFapsException("InstantiationException", e);
        } catch (final IllegalAccessException e) {
            throw new EFapsException("IllegalAccessException", e);
        }
    }

    /**
     * @param _name anme of the definition
     * @return  JmsDefinition for the given name
     */
    public static JmsDefinition getJmsDefinition(final String _name)
    {
        return JmsHandler.NAME2DEF.get(_name);
    }

    /**
     * @param _jmsDefinition JmsDefinition to add
     */
    public static void addJmsDefintion(final JmsDefinition _jmsDefinition)
    {
        JmsHandler.NAME2DEF.put(_jmsDefinition.getName(), _jmsDefinition);
    }

    /**
     * Stop the jms.
     * @throws EFapsException
     */
    public static void stop()
        throws EFapsException
    {
        for (final QueueConnection queCon : JmsHandler.QUEUE2QUECONN.values()) {
            try {
                queCon.close();
            } catch (final JMSException e) {
                throw new EFapsException("JMSException", e);
            }
        }
        for (final TopicConnection queCon : JmsHandler.TOPIC2QUECONN.values()) {
            try {
                queCon.close();
            } catch (final JMSException e) {
                throw new EFapsException("JMSException", e);
            }
        }
        JmsHandler.TOPIC2QUECONN.clear();
        JmsHandler.QUEUE2QUECONN.clear();
        JmsHandler.NAME2DEF.clear();
    }

    /**
     * A definition of Jms for eFaps.
     */
    public static class JmsDefinition
    {

        /**
         * Name of the Definition.
         */
        private final String name;

        /**
         * the related MessageProducer.
         */
        private final MessageProducer messageProducer;

        /**
         * The related Session.
         */
        private final Session session;

        /**
         * @param _name     name
         * @param _producer MessageProducer
         * @param _session  Session
         */
        public JmsDefinition(final String _name,
                             final MessageProducer _producer,
                             final Session _session)
        {
            this.name = _name;
            this.messageProducer = _producer;
            this.session = _session;
        }

        /**
         * This is the getter method for instance variable {@link #session}.
         *
         * @return value of instance variable {@link #session}
         * @see #parameters
         */
        public Session getSession()
        {
            return this.session;
        }

        /**
         * This is the getter method for instance variable {@link #messageProducer}.
         *
         * @return value of instance variable {@link #messageProducer}
         * @see #parameters
         */
        public MessageProducer getMessageProducer()
        {
            return this.messageProducer;
        }

        /**
         * This is the getter method for instance variable {@link #name}.
         *
         * @return value of instance variable {@link #name}
         * @see #parameters
         */
        public String getName()
        {
            return this.name;
        }
    }
}
