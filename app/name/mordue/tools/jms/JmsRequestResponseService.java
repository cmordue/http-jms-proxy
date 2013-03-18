/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package name.mordue.tools.jms;

import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.spring.ActiveMQConnectionFactory;

import play.Logger;
import play.libs.F.Promise;

public class JmsRequestResponseService
{
//    private static JmsRequestResponseService instance = new JmsRequestResponseService();
//    
//    public static JmsRequestResponseService getInstance() {
//        return instance;
//    }
    
//        JmsRequestResponseService jmss = null;
//        try
//        {
//            jmss = new JmsRequestResponseService(new ActiveMQConnectionFactory("tcp://localhost:61616"), numConnections, jmsConfig);
//        }
//        catch (JMSException e)
//        {
//            Logger.error(e, "JmsService didn't initialize");
//        }
//        jmsService = jmss;
    
    private static final JmsConfig jmsConfig = new JmsConfig();
    
    private static ThreadLocal<JmsRequestResponseService> jmsServiceTL = new ThreadLocal<JmsRequestResponseService>();
    
    private static ConnectionFactory factory;
    static {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL("tcp://localhost:61616");
        factory.setUseAsyncSend(true);
        factory.setOptimizeAcknowledge(true);
        factory.setAlwaysSessionAsync(false);
        JmsRequestResponseService.factory = factory;
    }
    
    public static JmsRequestResponseService getJmsService() {
        JmsRequestResponseService jmsService = jmsServiceTL.get();
        if (jmsService == null) {
            try
            {
                jmsService = new JmsRequestResponseService(factory, jmsConfig);
                jmsServiceTL.set(jmsService);
            }
            catch (JMSException e)
            {
                Logger.error(e, "JmsService didn't initialize");
                return null;
            }
        }
        return jmsService;
    }
    
    private ConnectionFactory connectionFactory;
    private JmsConfig config;
    private JmsRequestResponseClient client;
    
    public JmsRequestResponseService(ConnectionFactory connectionFactory, JmsConfig config) throws JMSException {
        this.connectionFactory = connectionFactory;
        this.config = config;
        Connection connection = connectionFactory.createConnection();
        connection.start();
        this.client = new JmsRequestResponseClient(connection, config);
    }
    
    public Promise<Map<String, String>> request(Map<String, String> request) throws ServiceException {
        try
        {
            return client.request(request);
        }
        catch (JMSException e)
        {
            throw new ServiceException(e);
        }
    }
    
    public Promise<String> request(String request) throws ServiceException {
        try
        {
            return client.request(request);
        }
        catch (JMSException e)
        {
            throw new ServiceException(e);
        }
    }

}


