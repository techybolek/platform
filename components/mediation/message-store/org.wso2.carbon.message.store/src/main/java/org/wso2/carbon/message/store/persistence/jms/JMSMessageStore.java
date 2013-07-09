/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.message.store.persistence.jms;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.message.processors.AbstractMessageProcessor;
import org.apache.synapse.message.store.AbstractMessageStore;
import org.apache.synapse.message.store.MessageStores;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.message.store.persistence.jms.message.JMSPersistentMessage;
import org.wso2.carbon.message.store.persistence.jms.util.JMSConstants;
import org.wso2.carbon.message.store.persistence.jms.util.JMSPersistentMessageHelper;
import org.wso2.carbon.message.store.persistence.jms.util.JMSUtil;

import javax.jms.*;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"UnusedDeclaration"})
public class JMSMessageStore extends AbstractMessageStore {
    public static final String JMS_SPEC_11 = "1.1";

    private static final String MESSAGE_LOG = "org.wso2.carbon.jmsms.MSMPLog";

    /**
     * Implementation level properties
     */
    private Properties properties = new Properties();

    /**
     * Look up context
     */
    private Context jndiContext;

    /**
     * JMS cachedConnection factory
     */
    private ConnectionFactory connectionFactory;

    /**
     * provider username
     */
    private String username;

    /**
     * provider password
     */
    private String password;

    /**
     * Message helper class that is used to convert Message serializable form
     */
    private JMSPersistentMessageHelper jmsPersistentMessageHelper;

    /**
     * Message Store size;
     */
    private volatile AtomicInteger size = new AtomicInteger(0);

    private int cacheLevel = JMSMessageStoreConstants.CACHE_NOTHING;

    /**
     * Cached shared Read Connection
     */
    private volatile Connection cachedReadConnection = null;

    /**
     * Cached shared Write Connection
     */
    private volatile Connection cachedWriteConnection = null;

    private String jmsMessageStoreDestination;

    private static final Log log = LogFactory.getLog(JMSMessageStore.class);

    /**
     * This log has the operations associated with each message.
     */
    private static final Log messageLog = LogFactory.getLog(MESSAGE_LOG);

    private boolean jmsSpec11 = true;

    private Semaphore removeLock = new Semaphore(1);

    private Semaphore cleanUpOfferLock = new Semaphore(1);

    private AtomicBoolean cleaning = new AtomicBoolean(false);

    private int consumerReceiveTimeOut;

    /**
     * Following fields are used within
     * @see #offer(org.apache.synapse.MessageContext)
     */

    private Connection readWriteConnection;

    private static final String versionString = "2.0";

    private AtomicInteger count = new AtomicInteger(0);

    /* */
    private boolean offerInitialized;

    private static final String OriginalMessageID = "OrigMessageID";

    private static final Map<String,MessageConsumer> consumerMap = new ConcurrentHashMap<String, MessageConsumer>();

    @Override
    public void init(SynapseEnvironment se) {
        super.init(se);
        init();
        jmsPersistentMessageHelper = new JMSPersistentMessageHelper(se);
        //syncSize();
        try {
            synchronized (JMSMessageStore.class) {
                readWriteConnection = getWriteConnection();
            }
        } catch (JMSException e) {
            log.error("Could not initialize message store. ["
                      + getName() + "]. Error while creating connection to the broker.  Error:"
                      + e.getLocalizedMessage());
            offerInitialized = false;
        }
        count.getAndSet(0);
        log.info("Message store [" + getName() + "] message count was set to 0.");
    }

    public boolean offer(MessageContext messageContext) {
        if (messageContext != null) {
            if (log.isDebugEnabled()) {
                log.debug("Storing the Message with Id :" +
                        messageContext.getMessageID() + " from the Message Store");
            }
        } else {
            return false;
        }

        JMSPersistentMessage jmsMessage = jmsPersistentMessageHelper.createPersistentMessage(
                messageContext);

        Session producerSession = null;
        MessageProducer messageProducer = null;
        boolean error = false;
        JMSException exception = null;
        if (cleaning.get()) {
            try {
                cleanUpOfferLock.acquire();
            } catch (InterruptedException e) {
                log.error("Message Cleanup lock released unexpectedly," +
                          "Message count value might show a wrong value ," +
                          "Restart the system to re sync the message count", e);
            }
        }
        try {
            if (readWriteConnection == null) {
                synchronized (JMSMessageStore.class) {
                    if (readWriteConnection == null) {
                        readWriteConnection = getWriteConnection();
                    }
                }
            }
            synchronized (JMSMessageStore.class) {
                // even if the other end of the write connection is open, the connection wont be null.
                // Such a connection will throw an error if we try to create a session out of it.
                // Therefore, we need to handle that situation. This synch block does that.
                try {
                    producerSession = JMSUtil.createSession(readWriteConnection, jmsSpec11);
                } catch (JMSException e) {
                    log.warn("Existing connection is faulty. Creating a new connection to the broker. Error:"
                             + e.getLocalizedMessage());
                    readWriteConnection = getWriteConnection();
                    producerSession = JMSUtil.createSession(readWriteConnection, jmsSpec11);
                    log.info("Message store[" + getName() + "] established a connection to the broker.");
                }
            }
            Destination destination;
            destination = getDestination(producerSession);
            messageProducer = JMSUtil.createProducer(producerSession, destination, jmsSpec11);
            ObjectMessage objectMessage = producerSession.createObjectMessage(jmsMessage);
            objectMessage.setStringProperty(OriginalMessageID, messageContext.getMessageID());
            messageProducer.send(objectMessage);
            count.getAndIncrement();
            int count = size.incrementAndGet();
            error = false;
            if (messageLog.isDebugEnabled()) {
                messageLog.debug("["+getName()+"] Storing["+count+"] MessageID:" + messageContext.getMessageID());
            }
        } catch (JMSException e) {
            exception = e;
            error = true;
        } finally {
            cleanUpOfferLock.release();
            synchronized (JMSMessageStore.class) {
                if (error) {
                    String errorMsg = "Ignoring Message. Could not store message to store ["
                                      + getName() + "]. Error:" + exception.getLocalizedMessage();
                    log.error(errorMsg, exception);
                    cleanupJMSResources(readWriteConnection, producerSession, messageProducer,
                                        true, ConnectionType.WRITE_CONNECTION);
                    readWriteConnection = null;
                    throw new SynapseException(errorMsg, exception);
                } else {
                    cleanupJMSResources(null, producerSession, messageProducer,
                                        false, ConnectionType.WRITE_CONNECTION);
                }
            }
        }
        return true;
    }

    public MessageContext poll() {
        throw new UnsupportedOperationException("Poll operation is not supported in " +
                                                "version " + versionString  + " of JMSMessageStore.");
    }

    /**
     * Retrieves the next message from the store and submit it to the message handler for processing.
     * Even though this method is synchronized, it will not reliably work in a scenario where
     * multiple message processors are attached to a same message store. For instance, in a multiple
     * message processor scenario, if one processor fails to send a message, it is very likely that
     * all other message processors end up retrying the same message.
     * @param messageHandler
     * @return TRUE if message handler received and processed the message successfully.<br/>
     * FALSE if the message processing is unsuccessful.
     */
    public synchronized boolean fetchInto(MessageProcessingHandler messageHandler) {
        if (messageHandler == null) {
            log.error("No Message Handler found. Cannot retrieve message from store.");
            return false;
        }
        try {
            removeLock.acquire();
        } catch (InterruptedException e) {
            log.error("Message Removal lock released unexpectedly," +
                    "Message count value might show a wrong value ," +
                    "Restart the system to re sync the message count" ,e);
            return false;
        }
        ClassLoader originalCl = null;
    	if (properties.get(JMSMessageStoreConstants.VENDER_CLASS_LOADER_ENABLE) == null
				|| Boolean.parseBoolean(
                (String) properties.get(JMSMessageStoreConstants.VENDER_CLASS_LOADER_ENABLE))) {
            originalCl = getContextClassLoader();
			setContextClassLoader(this.getClass().getClassLoader());
		}
        //// First get a valid message consumer that has a connection to the message store.
        org.apache.synapse.message.MessageConsumer messageConsumer;
        synchronized (AbstractMessageProcessor.class) {
            if (!messageHandler.isConnected()) { // message handler is not connected to the store.
                org.apache.synapse.message.MessageConsumer newConsumer;
                newConsumer = newConsumer0(!messageHandler.isRetrying());
                if (newConsumer.isConnectionError()) {
                    messageHandler.setRetrying();
                    removeLock.release();
                    return false;
                } // else: the new message consumer is connected to the store.
                messageHandler.registerMessageConsumer(newConsumer);
            }
        }
        messageConsumer = messageHandler.getMessageConsumer();
        MessageConsumer consumer = (MessageConsumer) messageConsumer.getConsumer();
        if (messageHandler.isRetrying()) {
            log.info("Message Processor ["
                     + messageHandler.messageProcessorName()
                     + "] Reconnected to store ["
                     + getName() + "]. ");
        }
        messageHandler.unsetRetrying();
        boolean error = false;
        //// Fetch the message from the store.
        Message currentMessage = null;
        try {
            if (consumer != null) {
                if (messageHandler.getMessage() == null) {
                    currentMessage = consumer.receive(3000);
                } else {
                    currentMessage = messageHandler.getMessage();
                }
                if (currentMessage == null) {
                    removeLock.release();
                    return false;
                }
                messageHandler.setCurrentMessage(currentMessage);
                error = false;
                messageHandler.unsetRetrying();
            } else {
                // XXX: Unlikely
                log.error("JMS Message consumer is null. Cannot pull messages from message store.");
            }
        } catch (JMSException e) {
            error = true;
            log.info("Message Processor ["
                     + messageHandler.messageProcessorName()
                     + "] dropped the connection to store [" + getName() + "]. ");
        } catch (Throwable t) {
            error = true;
            log.error("Error occurred while connecting to store. " + t.getLocalizedMessage(), t);
        } finally {
            if (error) {
                if (messageHandler.getMessageConsumer() != null) {
                    messageHandler.cleanupConsumer();
                }
                messageHandler.clearCurrentMessage();
                messageHandler.setRetrying();
                removeLock.release();
                return false;
            }
        }
        ////:~
        //// Process the message.
        error = false;
        boolean successful = false;
        boolean ackFault = false;
        JMSException exception = null;
        try {
            String messageId = "-1";
            if (currentMessage instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) currentMessage;
                messageId = objectMessage.getStringProperty(OriginalMessageID);
                if (messageHandler.getMessageContext() == null) {
                    JMSPersistentMessage jmsMeg = (JMSPersistentMessage) objectMessage.getObject();
                    MessageContext currentMessageContext;
                    currentMessageContext = jmsPersistentMessageHelper.createMessageContext(jmsMeg);
                    messageHandler.setCurrentMessageContext(currentMessageContext);
                }
            }
            successful = messageHandler.receive(messageHandler.getMessageContext());
            if (successful) {
                try {
                    messageHandler.getMessage().acknowledge();
                    count.getAndDecrement();
                } catch (JMSException e) {
                    log.error("Cannot ACK message[" + messageId + "]. Error:" + e.getLocalizedMessage(), e);
                    ackFault = true;
                }
                messageHandler.clearCurrentMessage();
                messageHandler.incrementProcessedCount();
                if (messageLog.isDebugEnabled()) {
                    messageLog.debug("[" + messageHandler.messageProcessorName() + "] Fetched["
                                     + messageHandler.getProcessedCount() + "] MessageID:" + messageId);
                }
            } else {
                if (messageLog.isDebugEnabled()) {
                    messageLog.debug("[" + messageHandler.messageProcessorName() + "] Retrying MessageID:" + messageId);
                }
            }
        } catch (JMSException e) {
            error = true;
            successful = false;
            exception = e;
        } catch (RuntimeException e) {
            removeLock.release();
            error = true;
            successful = false;
            String errorMsg = "Ignoring message. Message processor ["
                              + messageHandler.messageProcessorName() + "] "
                              + "Cannot process message. Error:" + e.getLocalizedMessage();
            log.error(errorMsg, e);
            throw new SynapseException(errorMsg, e);
        } finally {
            removeLock.release();
            if (error) {
                String errorMsg = "Ignoring message. Message processor ["
                                  + messageHandler.messageProcessorName() + "] "
                                  + "Cannot process message. Error:" + exception.getLocalizedMessage();
                log.error(errorMsg, exception);
                if (ackFault) {
                    messageHandler.cleanupConsumer();
                }
                messageHandler.clearCurrentMessage();
                throw new SynapseException(errorMsg, exception);
            }
        }
        size.decrementAndGet();
        if (originalCl != null) {
        	setContextClassLoader(originalCl);
        }
        return successful;
    }

    public MessageContext peek() {
        throw new UnsupportedOperationException("Peek operation is not supported in " +
                                                "version " + versionString  + " of JMSMessageStore.");
    }

    public MessageContext remove() throws NoSuchElementException {
        if (size.get() == 0) {
            throw new NoSuchElementException("Message Store " + name + " Empty");
        }
        log.info("Removing messages is not allowed in JMS Message store.");
        return null;
    }

    public void clear() {
        Connection con = null;
        Session session = null;
        MessageConsumer consumer = null;
        boolean error = false;
        int count = 0;
        try {
            removeLock.acquire();
            cleaning.set(true);
            cleanUpOfferLock.acquire();
        } catch (InterruptedException e) {
             log.error("Message Removal lock released unexpectedly," +
                    "Message count value might show a wrong value ," +
                    "Restart the system to re sync the message count" ,e);
        }
        ClassLoader originalCl = null;
    	
    	if (properties.get(JMSMessageStoreConstants.VENDER_CLASS_LOADER_ENABLE) == null
				|| Boolean.parseBoolean((String)properties.get(JMSMessageStoreConstants.VENDER_CLASS_LOADER_ENABLE))) {	originalCl = getContextClassLoader();
			setContextClassLoader(this.getClass().getClassLoader());
		}
        try {
            con = getWriteConnection();
            session = JMSUtil.createSession(con,jmsSpec11);
            Destination destination = getDestination(session);
            consumer = JMSUtil.createConsumer(session,destination,jmsSpec11);

            count = size();

            for (int i = 0; i < count;) {
                Message message = consumer.receive(1000);
                if (message != null) {
                    i++;
                } else {
                    break;
                }
            }
        } catch (JMSException e) {
            log.error("JMS error while deleting messages", e);
            error = true;
            throw new SynapseException("JMS Message Store Exception " + e);
        } finally {
            removeLock.release();
            cleaning.set(false);
            cleanUpOfferLock.release();
            size.set(size.get()-count);
            cleanupJMSResources(con, session, consumer, error, ConnectionType.WRITE_CONNECTION);
        }
        if(originalCl != null) {
        	setContextClassLoader(originalCl);
        }
    }

    public MessageContext remove(String messageId) {
        // Removing a Random Message is not supported in JMS Message store;
        throw new UnsupportedOperationException("Removing a Random Message is not supported in JMS Message store");
    }

    public MessageContext get(int i) {
        if (i < 0 || i > (size() - 1)) {
            return null;
        }
        int pointer = 0;
        Connection con = null;
        Session session = null;
        QueueBrowser browser = null;
        boolean error = false;
        ClassLoader originalCl = null;
    	
    	if (properties.get(JMSMessageStoreConstants.VENDER_CLASS_LOADER_ENABLE) == null
				|| Boolean.parseBoolean((String)properties.get(JMSMessageStoreConstants.VENDER_CLASS_LOADER_ENABLE))) {	originalCl = getContextClassLoader();
			setContextClassLoader(this.getClass().getClassLoader());

		}
        MessageContext messageContext = null;
        try {
            con = getReadConnection();
            session = JMSUtil.createSession(con,jmsSpec11);
            Destination destination = getDestination(session);
            browser = session.createBrowser((Queue) destination);
            Enumeration enumeration = browser.getEnumeration();
            while (enumeration.hasMoreElements()) {
                Object msg = enumeration.nextElement();
                if (pointer == i) {
                    if (msg != null) {
                        JMSPersistentMessage jmsMeg = (JMSPersistentMessage) ((ObjectMessage) msg).getObject();
                        messageContext = jmsPersistentMessageHelper.createMessageContext(jmsMeg);
                        /**Prevents message loss against the polling of the queue(issue occurs at ActiveMQ V5.4.2)**/
						while (enumeration.hasMoreElements()) {
							 enumeration.nextElement();
						}
                        return messageContext;
                    } else {
                        return null;
                    }
                } else {
                    pointer++;
                }
            }
        } catch (JMSException e) {
            log.error("JMS error while retrieving messages from the store: " + name, e);
            error = true;
            throw new SynapseException("JMS Message Store Exception " + e);
        } finally {
            cleanupJMSResources(con, session, browser, error, ConnectionType.READ_CONNECTION);
        }
        if(originalCl !=null){
        	setContextClassLoader(originalCl);
        }
        return messageContext;
    }

    public List<MessageContext> getAll() {
        List<MessageContext> list = new ArrayList<MessageContext>();
        Connection con = null;
        Session session = null;
        QueueBrowser browser = null;
        boolean error = false;
        ClassLoader originalCl = null;
    	
    	if (properties.get(JMSMessageStoreConstants.VENDER_CLASS_LOADER_ENABLE) == null
				|| Boolean.parseBoolean((String)properties.get(JMSMessageStoreConstants.VENDER_CLASS_LOADER_ENABLE))) {	originalCl = getContextClassLoader();
			setContextClassLoader(this.getClass().getClassLoader());
		}
        try {
            con = getReadConnection();
            session = JMSUtil.createSession(con,jmsSpec11);
            Destination destination = getDestination(session);
            browser = session.createBrowser((Queue) destination);
            Enumeration enumeration = browser.getEnumeration();
            while (enumeration.hasMoreElements()) {
                Object msg = enumeration.nextElement();
                if (msg instanceof ObjectMessage) {
                	/**Prevents message loss against the polling of the queue(issue occurs at ActiveMQ V5.4.2)**/
					while (enumeration.hasMoreElements()) {
						 enumeration.nextElement();
					}
                    JMSPersistentMessage jmsMeg = (JMSPersistentMessage) ((ObjectMessage) msg).getObject();
                    list.add(jmsPersistentMessageHelper.createMessageContext(jmsMeg));
                }
            }
        } catch (JMSException e) {
            log.error("JMS error while retrieving messages from the store: " + name, e);
            error = true;
            throw new SynapseException("JMS Message Store Exception " + e);
        } finally {
            cleanupJMSResources(con, session, browser, error, ConnectionType.READ_CONNECTION);
        }
        if(originalCl != null) {
        	setContextClassLoader(originalCl);
        }
        return list;
    }

    public MessageContext get(String s) {
        if (s == null) {
            return null;
        }
        Connection con = null;
        Session session = null;
        QueueBrowser browser = null;
        boolean error = false;
        ClassLoader originalCl = null;
        	
    	if (properties.get(JMSMessageStoreConstants.VENDER_CLASS_LOADER_ENABLE) == null
				|| Boolean.parseBoolean((String)properties.get(JMSMessageStoreConstants.VENDER_CLASS_LOADER_ENABLE))) {	originalCl = getContextClassLoader();
			setContextClassLoader(this.getClass().getClassLoader());

		}
        MessageContext messageContext = null;
        try {
            con = getReadConnection();
            session = JMSUtil.createSession(con,jmsSpec11);
            Destination destination = getDestination(session);
            browser = session.createBrowser((Queue) destination);
            Enumeration enumeration = browser.getEnumeration();
            while (enumeration.hasMoreElements()) {
				Object msg = enumeration.nextElement();
				JMSPersistentMessage jmsMeg =
				    (JMSPersistentMessage) ((ObjectMessage) msg).getObject();
				if (s.equals(jmsMeg.getJmsPersistentAxis2Message().getMessageID())) {
					messageContext = jmsPersistentMessageHelper.createMessageContext(jmsMeg);
					  /**Prevents message loss against the polling of the queue(issue occurs at ActiveMQ V5.4.2)**/
					while (enumeration.hasMoreElements()) {
						enumeration.nextElement();
					}
					break;
				}
			}
        } catch (JMSException e) {
            log.error("JMS error while retrieving messages from the store: " + name, e);
            error = true;
            throw new SynapseException("JMS Message Store Exception " + e);
        } finally {
            cleanupJMSResources(con, session, browser, error, ConnectionType.READ_CONNECTION);
        }
        if(originalCl !=null){
        	setContextClassLoader(originalCl);
        }
        return messageContext;
    }


    private Connection getReadConnection() throws JMSException {
        if (cacheLevel > JMSMessageStoreConstants.CACHE_NOTHING) {
            return getCachedReadConnection();
        }
        return createConnection();
    }

    private Connection getWriteConnection() throws JMSException {
        if (cacheLevel > JMSMessageStoreConstants.CACHE_NOTHING) {
            return getCachedWriteConnection();
        }
        return createConnection();
    }

    private Destination getDestination(Session session) throws JMSException {
        Destination destination = null;
        if (jmsMessageStoreDestination == null) {
            synchronized (JMSMessageStore.class) {
                if (jmsMessageStoreDestination == null) {
                    jmsMessageStoreDestination = name + "_Queue";
                }
            }
        }
        try {
            destination = lookup(jndiContext, Destination.class, jmsMessageStoreDestination);
        } catch (NamingException e) {
            try {
                Properties initialContextProperties = new Properties();
                if (jndiContext.getEnvironment() != null) {
                    if (jndiContext.getEnvironment().get(JMSConstants.NAMING_FACTORY_INITIAL) != null) {
                        initialContextProperties.put(JMSConstants.NAMING_FACTORY_INITIAL,
                                                     jndiContext.getEnvironment().get(JMSConstants.NAMING_FACTORY_INITIAL));
                    }
                    if (jndiContext.getEnvironment().get(JMSConstants.CONNECTION_STRING) != null) {
                        initialContextProperties.put(JMSConstants.CONNECTION_STRING,
                                                     jndiContext.getEnvironment().get(JMSConstants.CONNECTION_STRING));
                    }
                    if (jndiContext.getEnvironment().get(JMSConstants.PROVIDER_URL) != null) {
                        initialContextProperties.put(JMSConstants.PROVIDER_URL,
                                                     jndiContext.getEnvironment().get(JMSConstants.PROVIDER_URL));
                    }
                }
                initialContextProperties.put(JMSConstants.QUEUE_PREFIX + jmsMessageStoreDestination,
                                             jmsMessageStoreDestination);
                InitialContext initialContext = new InitialContext(initialContextProperties);
                destination = lookup(initialContext, Destination.class, jmsMessageStoreDestination);
            } catch (NamingException e1) {
                log.debug("Error creating Destination  " + jmsMessageStoreDestination + " : " + e1 +
                          " Destination is not defined in the JNDI context");
            }
        } finally {
            if (destination == null) {
                destination = session.createQueue(jmsMessageStoreDestination);
            }
            return destination;
        }
    }

    private Connection getCachedReadConnection() throws JMSException {
        if (cachedReadConnection == null) {
            synchronized (this) {
                if (cachedReadConnection == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating a cached JMS connection for the store: " + name);
                    }
                    cachedReadConnection = createConnection();
                }
            }
        }
        return cachedReadConnection;
    }

    private Connection getCachedWriteConnection() throws JMSException {
        if (cachedWriteConnection == null) {
            synchronized (this) {
                if (cachedWriteConnection == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating a cached JMS connection for the store: " + name);
                    }
                    cachedWriteConnection = createConnection();
                }
            }
        }
        return cachedWriteConnection;
    }

    private synchronized Connection createConnection() throws JMSException {
        Connection con = JMSUtil.createConnection(connectionFactory,username,password,jmsSpec11);
        con.start();
        return con;
    }

    private void init() {
        log.debug("Initializing the JMS Message Store");
        try {
            jndiContext = CarbonContext.getCurrentContext().getJNDIContext(properties);
            String connectionFac = (String) parameters.get(JMSMessageStoreConstants.CONNECTION_FACTORY);
            if (connectionFac == null) {
                connectionFac = "QueueConnectionFactory";
            }
            connectionFactory = lookup(jndiContext, ConnectionFactory.class, connectionFac);
            if (connectionFactory == null) {
                throw new SynapseException("Could not initialize message store[" + getName()
                                           + "]Connection factory not found :" + "QueueConnectionFactory");
            }
        } catch (NamingException e) {
            log.error("Could not initialize message store[" + getName()
                      + "]. Naming Exception. Error:" + e.getLocalizedMessage(), e);
        } catch (Exception e) {
            log.error("Could not initialize message store[" + getName()
                      + "]. Error:" + e.getMessage(), e);
        }
    }

    private static synchronized <T> T lookup(Context context, Class<T> clazz, String name)
            throws NamingException {
        Object object = context.lookup(name);
        try {
            return clazz.cast(object);
        } catch (ClassCastException ex) {
            log.error("Error while performing the JNDI lookup for the name: " + name, ex);
            return null;
        }
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        super.setParameters(parameters);
        if (parameters != null && !parameters.isEmpty()) {
            Set<Map.Entry<String, Object>> mapSet = parameters.entrySet();
            for (Map.Entry<String, Object> e : mapSet) {
                if (e.getValue() instanceof String) {
                    properties.put(e.getKey(), e.getValue());
                }
            }
            username = (String) parameters.get(JMSMessageStoreConstants.JMS_USERNAME);
            password = (String) parameters.get(JMSMessageStoreConstants.JMS_PASSWORD);
            String conCaching = (String) parameters.get(JMSMessageStoreConstants.ENABLE_CONNECTION_CACHING);
            if ("true".equals(conCaching)) {
                if (log.isDebugEnabled()) {
                    log.debug("Enabling the connection Caching");
                }
                cacheLevel = JMSMessageStoreConstants.CACHE_CONNECTION;
            }
            String jmsDest = (String) parameters.get(JMSMessageStoreConstants.JMS_DESTINATION);
            if(jmsDest != null) {
                jmsMessageStoreDestination = jmsDest;
            }
            String jmsSpecVersion = (String) parameters.
                                        get(JMSMessageStoreConstants.JMS_SPEC_VERSION);
            if(jmsSpecVersion != null) {
                if(!JMS_SPEC_11.equals(jmsSpecVersion)) {
                    jmsSpec11 = false;
                }
            }
            String consumerReceiveTimeOut = (String) parameters.get(JMSMessageStoreConstants.CONSUMER_RECEIVE_TIMEOUT);
            if(null != consumerReceiveTimeOut){
                try {
                    this.consumerReceiveTimeOut =  Integer.parseInt(consumerReceiveTimeOut);
                } catch (NumberFormatException e) {
                    log.error("Error in parsing the consumer receive time out value and , it is set to 60 secs");
                    this.consumerReceiveTimeOut = 60000;
                }
            }else {
                log.warn("Consumer Receiving time out is not passed in, Setting to default value of 60 secs");
                this.consumerReceiveTimeOut = 60000;
            }

        } else {
            throw new SynapseException("Required Parameters missing. Can't initialize " +
                    "JMS Message Store");
        }
    }

    private void cleanupJMSResources(Connection connection, Session session, Object jmsObject,
                                     boolean error, ConnectionType connectionType) {
        if (connection == null && error) {
            return;
        }
        try {
            if (jmsObject != null) {
                if (jmsObject instanceof MessageProducer) {
                    ((MessageProducer) jmsObject).close();
                } else if (jmsObject instanceof MessageConsumer) {
                    ((MessageConsumer) jmsObject).close();
                } else if (jmsObject instanceof QueueBrowser) {
                    ((QueueBrowser) jmsObject).close();
                }
            }
        } catch (JMSException e) {
            log.error("Error while cleaning up connection object to message store [" + name + "]. ", e);
        }
        try {
            if (session != null) {
                session.close();
            }
        } catch (JMSException e) {
            log.error("Error while cleaning up connection session to message store [" + name + "]. ", e);
        }
        try {
            if (connection != null && cacheLevel == JMSMessageStoreConstants.CACHE_NOTHING) {
                if (connection != null) {
                    synchronized (connection) {
                        if (connection != null) {
                            connection.close();
                        }
                    }
                }
            } else if (error) {
                // if we are using a cached JMS connection, then we should only close it in case
                // of errors
                switch (connectionType) {
                    case READ_CONNECTION: {
                        cleanupCachedReadConnection();
                        break;
                    }
                    case WRITE_CONNECTION: {
                        cleanupCachedWriteConnection();
                        break;
                    }
                }
            }
        } catch (JMSException e) {
            log.error("Error while cleaning up connection to message store [" + name + "]. ", e);
        }
    }

    private synchronized void ack(Message message, Session session, boolean successful) throws JMSException {
        if (successful && session.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE) {
            if (messageLog.isDebugEnabled()) {
                messageLog.debug("Dequeue SUCCESS MessageID: " + message.getJMSMessageID());
            }
        }
        if (successful && session.getTransacted()) {
            session.commit();
        } else if (!successful && session.getTransacted()) {
            //session.rollback();
        }
    }

    private synchronized void cleanupCachedReadConnection() {
        if (cachedReadConnection != null) {
            if (log.isDebugEnabled()) {
                log.debug("Closing the cached JMS connection in: " + name);
            }
            try {
                cachedReadConnection.close();
            } catch (JMSException e) {
                log.warn("Error closing the JMS connection", e);
            }
            cachedReadConnection = null;
        }
    }

    private synchronized void cleanupCachedWriteConnection() {
        if (cachedReadConnection != null) {
            if (log.isDebugEnabled()) {
                log.debug("Closing the cached JMS connection in: " + name);
            }
            try {
                cachedReadConnection.close();
            } catch (JMSException e) {
                log.warn("Error closing the JMS connection", e);
            }
            cachedReadConnection = null;
        }
    }

    private void syncSize() {
        log.debug("Synchronizing Message Store size with the Queue Size");
        int count = 0;
        Connection con = null;
        Session session = null;
        QueueBrowser browser = null;
        boolean error = false;

        ClassLoader originalCl = null;

        if (properties.get(JMSMessageStoreConstants.VENDER_CLASS_LOADER_ENABLE) == null
                || Boolean.parseBoolean((String)properties.get(JMSMessageStoreConstants.VENDER_CLASS_LOADER_ENABLE))) {
            originalCl = getContextClassLoader();
            setContextClassLoader(this.getClass().getClassLoader());
        }

        this.setContextClassLoader(this.getClass().getClassLoader());

        try {
            con = getReadConnection();
            session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = getDestination(session);
            browser = session.createBrowser((Queue) destination);
            Enumeration enumeration = browser.getEnumeration();
            while (enumeration.hasMoreElements()) {
                enumeration.nextElement();
                count++;
            }
        } catch (JMSException e) {
            log.error("JMS error while updating size of the store: " + name, e);
            error = true;
        } catch (Exception e) {
            log.error("Exception occurred while initializing the JMS message store: " + name, e);
            error = true;
        } finally {
            cleanupJMSResources(con, session, browser, error, ConnectionType.READ_CONNECTION);
        }
        this.size.set(count);
        log.debug("Updated JMS Message Store Size :" + size);
        if(originalCl !=null){
        	setContextClassLoader(originalCl);
        }
    }

   private void setContextClassLoader(final ClassLoader cl) {
        AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        Thread.currentThread().setContextClassLoader(cl);
                        return null;
                    }
                }
        );
   }

    private ClassLoader getContextClassLoader(){
        return (ClassLoader) AccessController.doPrivileged(
                 new PrivilegedAction() {
                     public Object run() {
                         return Thread.currentThread().getContextClassLoader();
                     }
                 }
         );
    }

    @Override
    /**
     * Do not rely on the value returned by this method. It returns only the difference between
     * enqueued and dequeued messages to the message store for the current session.
     * Hence it does not retrieve the actual message count from the store when starting up and manage
     * the count on the fly.
     */
    public int size() {
        return count.get();
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            cleanupCachedReadConnection();
            cleanupCachedWriteConnection();
            if (jndiContext != null) {
                jndiContext.close();
            }
        } catch (NamingException e) {
            log.error("Error closing the JNDI Context" ,e);
        }
    }

    private enum ConnectionType {
        READ_CONNECTION, WRITE_CONNECTION
    }

    public OMElement serialize() {
        OMElement store = fac.createOMElement("JMSMessageStore", synNS);
        if (getName() != null) {
            store.addAttribute(fac.createOMAttribute("name", nullNS, getName()));
        }
        if (getParameters() != null) {
            Iterator i = getParameters().keySet().iterator();
            while (i.hasNext()) {
                String name = (String) i.next();
                String value = (String) getParameters().get(name);
                OMElement property = fac.createOMElement("parameter", synNS);
                property.addAttribute(fac.createOMAttribute(
                        "name", nullNS, name));
                property.setText(value.trim());
                store.addChild(property);
            }
        }
        if (getDescription() != null) {
            OMElement descriptionElem = fac.createOMElement(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "description"));
            descriptionElem.setText(getDescription());
            return descriptionElem;
        }
        return store;
    }

    public int getType() {
        return MessageStores.JMS_MS;
    }

    public org.apache.synapse.message.MessageConsumer newConsumer() throws Exception {
        try {
            return newConsumer0(true);
        } catch (Throwable t) {
            log.error("Could not create a new message consumer. " + t.getLocalizedMessage());
            throw new Exception(t);
        }
    }

    private org.apache.synapse.message.MessageConsumer newConsumer0(boolean repeatMessage) throws SynapseException {
        Connection connection = null;
        Session consumerSession = null;
        Destination consumerDestination = null;
        MessageConsumer consumer = null;
        JMSMessageConsumer consumer1 = new JMSMessageConsumer();
        consumer1.setStoreName(getName());
        boolean connectionError = false;
        try {
            connection = getReadConnection();
            consumerSession = JMSUtil.createClientAckSession(connection, jmsSpec11);
            consumerDestination = getDestination(consumerSession);
            synchronized (consumerMap){
                if(consumerMap.containsKey(consumerDestination.toString())){
                    consumerMap.remove(consumerDestination.toString()).close();
                    consumer = JMSUtil.createConsumer(consumerSession, consumerDestination, jmsSpec11);
                    consumerMap.put(consumerDestination.toString(),consumer);
                }else {
                    consumer = JMSUtil.createConsumer(consumerSession, consumerDestination, jmsSpec11);
                    consumerMap.put(consumerDestination.toString(),consumer);
                }
            }

        } catch (JMSException e) {
            connectionError = true;
            if (repeatMessage) {
                log.error("Could not create a consumer to message store [" + getName()
                          + "]. Message consumer has a connection error. Error:" + e.getLocalizedMessage());
            }
        } catch (Throwable t) {
            connectionError = true;
            if (repeatMessage) {
                log.error("An Exception occurred while creating a consumer to message store [ "
                          + getName() + "]. Message consumer has a connection error. Error:"
                          + t.getLocalizedMessage(), t);
            }
        } finally {
            if (connectionError) {
                cleanupJMSResources(connection, consumerSession, consumer, true, ConnectionType.READ_CONNECTION);
                consumer1.connectionError();
                return consumer1;
            }
            consumer1.setConsumer(consumer);
            consumer1.setConnection(connection);
            consumer1.setSession(consumerSession);
            consumer1.setDestination(consumerDestination);
            consumer1.clearConnectionError();
        }
        return consumer1;
    }

    private MessageProducer newProducer() throws JMSException {
        Connection connection;
        synchronized (JMSMessageStore.class) {
            connection = getReadConnection();
        }
        Session producerSession;
        Destination producerDestination;
        MessageProducer producer;
        synchronized (JMSMessageStore.class) {
            producerSession = JMSUtil.createSession(connection, jmsSpec11);
        }
        if (producerSession != null) {
            producerDestination = getDestination(producerSession);
        } else {
            log.warn("Cannot create a new Message Producer. Producer session is null.");
            return null;
        }
        if (producerDestination != null) {
            producer = JMSUtil.createProducer(producerSession, producerDestination, jmsSpec11);
        } else {
            log.warn("Cannot create a new Message Producer. Producer destination is null.");
            return null;
        }
        return producer;
    }
}
