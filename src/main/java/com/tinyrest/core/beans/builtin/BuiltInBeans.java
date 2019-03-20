/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.beans.builtin;

import com.google.gson.Gson;
import com.tinyrest.core.beans.Bean;
import com.tinyrest.core.server.ConfigWriter;
import com.tinyrest.core.server.RestServer;
import com.tinyrest.core.server.VersionController;
import com.tinyrest.core.server.json.GsonFactory;
import com.tinyrest.core.server.logging.LoggingManager;
import com.tinyrest.core.server.manager.RequestHandler;
import com.tinyrest.core.server.servlets.RestControllerServlet;
import com.tinyrest.core.server.servlets.ServerActivityMonitor;
import com.tinyrest.core.server.servlets.ServerActivityMonitorImpl;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Built in beans
 *
 * @author Sam
 */
public class BuiltInBeans {

    @Bean("environment")
    public Environment environment() {
        return new Environment();
    }

    @Bean("requestHandler")
    public RequestHandler handlerManager() {
        return new RequestHandler();
    }

    @Bean
    public Gson gson() {
        return GsonFactory.createGson();

    }


//    @Bean
//    public VersionController versionController(){
//        return new VersionController();
//    } 

    @Bean
    public VersionController versionController() {
        return new VersionController();
    }


    @Bean
    public ConfigWriter configWriter() {
        return new ConfigWriter();
    }
    
    @Bean
    public LoggingManager loggingManager() {
        return new LoggingManager();
    }




    @Bean
    public ServerActivityMonitor serverActivityMonitor(Environment env) {
        String serverMaxThreads = env.getProperty("server.maxthreads", "0");
        if (serverMaxThreads == null) {
            serverMaxThreads = "0";
        }
        int maxThreads = Integer.parseInt(serverMaxThreads);
        return new ServerActivityMonitorImpl(maxThreads);
    }

    @Bean
    public RestControllerServlet mainServlet(Gson gson, RequestHandler requestHandler, ServerActivityMonitor serverActivityMonitor) {
        return new RestControllerServlet(gson, requestHandler, serverActivityMonitor);
    }


    @Bean("restServer")
    public RestServer restServer(Environment env, RestControllerServlet mainServlet) {

        String serverPort = env.getProperty("server.port");

        String host = env.getProperty("server.host");

        if ((serverPort == null) || (serverPort.isEmpty())) {
            serverPort = "8080";
        }

        if ((host == null) || host.isEmpty()) {
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException uhe) {

                host = "0.0.0.0";

            }
        }

        int port = Integer.parseInt(serverPort);

        RestServer server = new RestServer(host, port, mainServlet);

        return server;

    }

}
