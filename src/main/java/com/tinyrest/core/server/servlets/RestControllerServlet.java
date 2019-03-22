/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.servlets;

import com.google.gson.Gson;
import com.tinyrest.core.server.exceptions.AuthenticationException;
import com.tinyrest.core.server.manager.*;
import com.tinyrest.core.server.manager.RequestHandler;
import com.tinyrest.core.server.http.response.HttpStatus;
import com.tinyrest.core.server.http.response.ResponseEntity;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sam
 */
public class RestControllerServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory.getLogger(RestControllerServlet.class);

    private final Gson gson;

    private final RequestHandler requestHandler;

    private final ServerActivityMonitor serverActivityMonitor;

    public RestControllerServlet(Gson gson, RequestHandler requestHandler, ServerActivityMonitor serverActivityMonitors) {
        this.gson = gson;
        this.requestHandler = requestHandler;
        this.serverActivityMonitor = serverActivityMonitors;
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        doCall("delete", req, resp);

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doCall("put", req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doCall("post", req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doCall("get", req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doCall("options", req, resp);
    }


    private void doCall(String verb, HttpServletRequest req, HttpServletResponse resp) throws IOException {

        long startTime = System.currentTimeMillis();
        try {
//            System.out.println("Handling request for " + req.getPathInfo());

            LOGGER.debug("Handling request for " + req.getPathInfo());
            ResponseEntity responseEntity = null;
            
            if (serverActivityMonitor != null) {
                serverActivityMonitor.startCall(req);
            }

            try {

                responseEntity = this.requestHandler.processRequest(verb, req.getPathInfo(), req);

                if (responseEntity.getCrossOrigins() != null) {
                    resp.addHeader("Access-Control-Allow-Origin", responseEntity.getCrossOrigins().getOrigins());
                    resp.addHeader("Access-Control-Allow-Methods", responseEntity.getCrossOrigins().getMethods());
                    resp.addHeader("Access-Control-Allow-Headers", responseEntity.getCrossOrigins().getAllowHeaders());
                }

            } catch (AuthenticationException authException) {
                LOGGER.debug(authException.getMessage());
                responseEntity = new ResponseEntity<>(authException.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (InvocationTargetException ite) {

                Throwable t = ite.getTargetException();
                responseEntity = this.requestHandler.processException(t);

            }

            responseEntity.getResponseHeaders().forEach(
              (str, obj) -> {
                  resp.setHeader(String.valueOf(str), String.valueOf(obj));
              }
            );
            
            Object t = responseEntity.getBody();
            if (t instanceof String) {
                String response = (String) t;
                LOGGER.debug("Returning String:" + response);
                resp.setStatus(responseEntity.getStatus().value());
                resp.setContentType(responseEntity.getContentType());
                
                resp.getWriter().print(response);
                resp.getWriter().flush();

            } else {

                String response = this.gson.toJson(responseEntity.getBody());
                if (responseEntity.getBody() != null) {
                    LOGGER.debug("Returning object of type " + responseEntity.getBody().getClass().getName());
                    LOGGER.debug(response);
                } else {
                    LOGGER.debug("RESPONSE BODY WAS NULL");
                }

                resp.setStatus(responseEntity.getStatus().value());
                resp.setContentType(responseEntity.getContentType());
                resp.getWriter().print(response);
                resp.getWriter().flush();

            }

        } catch (ServerMonitorException sme) {
        
            resp.setStatus(503);
            resp.getWriter().print("Server too busy.");
            resp.getWriter().flush();
            
        } catch (HandlerException he) {

            resp.setStatus(500);
            resp.getWriter().print("Handler exception thrown " + he.getMessage());
            resp.getWriter().flush();

        } catch (NotFoundException nfe) {

            resp.setStatus(404);
            resp.getWriter().print(nfe.getMessage());
            resp.getWriter().flush();

        } catch (MissingParamException mpe) {

            resp.setStatus(422);
            resp.getWriter().print(mpe.getMessage());
            resp.getWriter().flush();

        } catch (MethodNotAllowedException mna) {

            resp.setStatus(405);
            resp.getWriter().print(mna.getMessage());
            resp.getWriter().flush();

        } catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            //Fuck knows what to do with these.
            LOGGER.debug(ex.getMessage(), ex);
            resp.setStatus(500);
            resp.getWriter().print("Unhandled Exception of type " + ex.getClass().getSimpleName() + " with message " + ex.getMessage());
            resp.getWriter().flush();

        } catch (Throwable t) {
            LOGGER.debug(t.getMessage(), t);
            resp.setStatus(500);
            resp.getWriter().print("Unhandled type " + t.getClass().getSimpleName() + " with message " + t.getMessage());
            resp.getWriter().flush();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info("[" + req.getMethod() + "]" + "  -  " + req.getPathInfo() + " --> " + resp.getStatus() + " (" + duration + " ms)");
            if (serverActivityMonitor != null) {
                serverActivityMonitor.endCall(req);
            }
        }

    }

}
