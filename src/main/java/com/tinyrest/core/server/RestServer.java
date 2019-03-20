/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server;

import com.tinyrest.core.server.servlets.RestControllerServlet;
import javax.servlet.ServletException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;

/**
 *
 * @author Sam
 */
public class RestServer {

    private final String host;
    private final int port;
    private Server server;
    private final RestControllerServlet mainServlet;
    private final HandlerCollection servlets;
    private final ServletContextHandler restControllerSch;
    //private final ServletContextHandler webSocketContextHandler;

    private WebSocketUpgradeFilter wsFilter;


    public RestServer(String host, int port, RestControllerServlet mainServlet) {
        this.host = host;
        this.port = port;
        this.mainServlet = mainServlet;
        this.servlets = new HandlerCollection();
        this.restControllerSch = new ServletContextHandler();
        this.restControllerSch.addServlet(new ServletHolder(this.mainServlet), "/*");
//        this.webSocketContextHandler = new ServletContextHandler();
        try {
            wsFilter = WebSocketUpgradeFilter.configureContext(restControllerSch);
            wsFilter.getFactory().getPolicy().setIdleTimeout(600000);
        } catch (ServletException sex) {
            wsFilter = null;
        }
    }

    public ServletContextHandler getRestContextHandler() {
        return restControllerSch;
    }

    public ServletContextHandler getWebSocketContextHandler() {
        return restControllerSch;
    }

    public void startServer() throws Exception {
        server = new Server(this.port);

        restControllerSch.setContextPath("/");
//        webSocketContextHandler.setContextPath("/*");

        servlets.addHandler(restControllerSch);
//        servlets.addHandler(webSocketContextHandler);

        server.setHandler(servlets);

        server.start();

        server.join();

    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public WebSocketUpgradeFilter getWsFilter() {
        return wsFilter;
    }

    public RestControllerServlet getMainServlet() {
        return mainServlet;
    }
    


    
}
