/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.contexts;

import com.google.gson.Gson;
import com.tinyrest.core.server.manager.HandlerException;
import com.tinyrest.core.server.manager.RequestHandler;
import com.tinyrest.core.server.manager.InvalidHandlerException;
import com.tinyrest.core.server.servlets.RestControllerServlet;
import com.tinyrest.core.server.servlets.ServerActivityMonitor;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 *
 * @author Sam
 */
public class RestControllerContext {
    
    private final Object restControllerInstance;
    
    private final RequestHandler requestHandler;
    
    private final RestControllerServlet servlet;
    
    private final ServletContextHandler sch;
    
    private final Gson gson;
    
    public RestControllerContext(Object restControllerInstance, String basePath, Gson gson,
                                 RequestHandler requestHandler, RestControllerServlet servlet, ServerActivityMonitor serverActivityMonitor)
            throws HandlerException, InvalidHandlerException {
        
        this.restControllerInstance = restControllerInstance;
        this.requestHandler = requestHandler;
        this.gson = gson;
        this.requestHandler.addHandler(restControllerInstance);
        
        this.servlet = new RestControllerServlet(gson, this.requestHandler, serverActivityMonitor);
        
        this.sch = new ServletContextHandler();
        this.sch.setContextPath(basePath);
        this.sch.addServlet(new ServletHolder(servlet), "/*");
    }

    public Object getRestControllerInstance() {
        return restControllerInstance;
    }

    public RequestHandler getRequestHandler() {
        return requestHandler;
    }

    public RestControllerServlet getServlet() {
        return servlet;
    }

    public Gson getGson() {
        return gson;
    }

    public ServletContextHandler getServletContextHandler() {
        return sch;
    }
    
    
    
    
    
    
    
}
