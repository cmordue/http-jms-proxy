package controllers;

import com.google.gson.Gson;

import play.*;
import play.libs.F.Promise;
import play.mvc.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;

import name.mordue.tools.jms.JmsConfig;
import name.mordue.tools.jms.JmsRequestResponseService;
import name.mordue.tools.jms.ServiceException;

import models.*;

public class HttpJmsProxy extends Controller {
    
    public static void index() {
        render();
    }
    
//    public static void jmsService(Map<String, String> hello) throws ServiceException, InterruptedException, ExecutionException, TimeoutException {
//        Logger.info("HttpJmsProxy.jmsService(Map)");
//        for (String key : hello.keySet()) {
//            Logger.info("  %s=%s ", key, params.get(key));
//        }
//        Promise<Map<String, String>> promise = jmsService.request(hello);
//        Map<String, String> response = promise.get(5000, TimeUnit.MILLISECONDS);
//        renderJSON(response);
//    }
    
    public static void jmsService(String q) throws ServiceException, InterruptedException, ExecutionException, TimeoutException {
//        Logger.info("HttpJmsProxy.jmsService(String)");
//        Logger.info("  q=%s ", q);
        Promise<String> promise = JmsRequestResponseService.getJmsService().request(q);
        String response = await(promise);
        renderText(response);
    }

}