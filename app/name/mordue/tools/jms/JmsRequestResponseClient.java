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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import play.Logger;
import play.libs.F.Action;
import play.libs.F.Promise;

public class JmsRequestResponseClient implements MessageListener
{
    private JmsConfig config;
    private Session session;
    private Destination requestQueue;
    private MessageProducer producer;
    
    private Destination responseQueue;
    private MessageConsumer consumer;
    
    private static final ConcurrentHashMap<String, Promise> promises = new ConcurrentHashMap<String, Promise>();
    
    public JmsRequestResponseClient(Connection connection, JmsConfig config) throws JMSException
    {
        this.config = config;
        session = connection.createSession(config.isTransacted(), config.getAckMode());
        requestQueue = session.createQueue(config.getQueueName());

        //Setup a message producer to send message to the queue the server is consuming from
        producer = session.createProducer(requestQueue);
        producer.setDeliveryMode(config.getDeliveryMode());

        //Create a temporary queue that this client will listen for responses on then create a consumer
        //that consumes message from this temporary queue...for a real application a client should reuse
        //the same temp queue for each message to the server...one temp queue per client
        responseQueue = session.createTemporaryQueue();
        consumer = session.createConsumer(responseQueue);

        System.out.println("Connection: " + connection + " session: " + session + " requestQueue: " + requestQueue + " responseQueue: " + responseQueue + " consumer: " + consumer);

        //This class will handle the messages to the temp queue as well
        consumer.setMessageListener(this);
    }

    @Override
    public void onMessage(Message response)
    {
//        Logger.info("Received message");
        try
        {
            String correlationId = response.getJMSCorrelationID();
//            try
//            {
//                Thread.sleep(50);
//            }
//            catch (InterruptedException e)
//            {
//                e.printStackTrace();
//            }
//            String correlationId = response.getStringProperty("muleID");
//            Logger.info("pending tasks: " + promises.size());
            Promise p = promises.remove(correlationId);
//            System.out.println("Processing thread: " + Thread.currentThread().getId() + " " + correlationId);
//            Logger.info("Received message with correlationId: " + correlationId);
            p.invoke(response);
        }
        catch (JMSException e)
        {
            Logger.warn("Couldn't get the correlationId for a message. Dropping message on the floor");
        }
    }

    public Promise<Map<String, String>> request(Map<String, String> request) throws JMSException {
        MapMessage message = session.createMapMessage();
        Promise<MapMessage> mapMessagePromise = request(message);
        final Promise<Map<String, String>> mapPromise = new Promise<Map<String, String>>();
        mapMessagePromise.onRedeem(new Action<Promise<MapMessage>>() {
            @Override
            public void invoke(Promise<MapMessage> result) {
                if (result != null && result.getOrNull() != null) {
                    try
                    {
                        MapMessage mapMessage = result.getOrNull();
                        Map<String, String> map = new HashMap<String, String>();
                        Enumeration<?> names = mapMessage.getMapNames();
                        while (names.hasMoreElements()) {
                            String name = names.nextElement().toString();
                            Object obj = mapMessage.getObject(name);
                            map.put(name, obj != null ? obj.toString() : "null");
                        }
//                        Logger.info("about to invoke map promise with result");
                        mapPromise.invoke(map);
                    }
                    catch (JMSException e)
                    {
                        mapPromise.invokeWithException(e);
                    }
                } else {
                    mapPromise.invoke(null);
                }
            }
        });
        return mapPromise;
    }

    public Promise<String> request(String request) throws JMSException {
        TextMessage message = session.createTextMessage(request);
        Promise<TextMessage> textMessagePromise = request(message);
        final Promise<String> stringPromise = new Promise<String>();
        textMessagePromise.onRedeem(new Action<Promise<TextMessage>>() {
            @Override
            public void invoke(Promise<TextMessage> result) {
                if (result != null && result.getOrNull() != null) {
                    try
                    {
//                        Logger.info("about to invoke string promise with result: " + result.getOrNull().getText());
                        stringPromise.invoke(result.getOrNull().getText());
                    }
                    catch (JMSException e)
                    {
                        stringPromise.invokeWithException(e);
                    }
                } else {
                    stringPromise.invoke(null);
                }
            }
        });
        return stringPromise;
    }
    
    protected <T> Promise<T> request(Message message) throws JMSException {
        //Set the reply to field to the temp queue you created above, this is the queue the server
        //will respond to
        message.setJMSReplyTo(responseQueue);

        //Set a correlation ID so when you get a response you know which sent message the response is for
        //If there is never more than one outstanding message to the server then the
        //same correlation ID can be used for all the messages...if there is more than one outstanding
        //message to the server you would presumably want to associate the correlation ID with this
        //message somehow...a Map works good
        String correlationId = this.createRandomString();
//        message.setStringProperty("muleID", correlationId);
        message.setJMSCorrelationID(correlationId);
        
        // create a promise for the response
        Promise<T> p = new Promise<T>();
        promises.put(correlationId, p);
//        Logger.info("Sending message with correlationId: %s and message: " , correlationId, message);
        producer.send(message);
        return p;
    }

    private String createRandomString() {
        return UUID.randomUUID().toString();
    }
}


