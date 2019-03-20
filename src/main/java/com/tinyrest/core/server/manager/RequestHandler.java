/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.manager;

import com.tinyrest.core.server.annotations.CacheControl;
import com.tinyrest.core.server.annotations.CrossOrigin;
import com.tinyrest.core.server.annotations.ExceptionHandler;
import com.tinyrest.core.server.annotations.RequestMapping;
import com.tinyrest.core.server.mapping.ExceptionMapping;
import com.tinyrest.core.server.mapping.PathMatch;
import com.tinyrest.core.server.mapping.RestRequestMapping;
import com.tinyrest.core.server.http.request.HttpRequest;
import com.tinyrest.core.server.http.request.RequestMethod;
import com.tinyrest.core.server.http.response.CacheSettings;
import com.tinyrest.core.server.http.response.CrossOrigins;
import com.tinyrest.core.server.http.response.HttpStatus;
import com.tinyrest.core.server.http.response.ResponseEntity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class that handles all requests.
 *
 * @author Sam
 */
public class RequestHandler {

    private List<RestRequestMapping> handlers = new ArrayList<>();

    private List<ExceptionMapping> exceptionHandlers = new ArrayList<>();

    private String baseMapping = "";

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 30, 5, TimeUnit.MINUTES, new LinkedBlockingQueue());

    private final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    public RequestHandler() {

    }

    /**
     * Add an instance of a controller to the handler.
     *
     * @param obj - The instance of the controller.
     * @throws HandlerException
     * @throws InvalidHandlerException
     */
    public void addHandler(Object obj) throws HandlerException, InvalidHandlerException {

        logger.debug("Processing controller " + obj.getClass().getName());
        processHandlers(obj);

    }

    /**
     * Process a request and send it to the handler chain.
     *
     * @param verb - The method that was used in the request
     * @param path - The path that the request came in on.
     * @param req - the HttpServletRequest object
     * @return - ResponseEntity
     * @throws HandlerException
     * @throws NotFoundException
     * @throws MissingParamException
     * @throws MethodNotAllowedException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public ResponseEntity processRequest(String verb, String path, HttpServletRequest req)
            throws HandlerException, NotFoundException, MissingParamException, MethodNotAllowedException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        RestRequestMapping mapping = null;
        HttpRequest request = null;

        boolean matchFound = false;
        ResponseEntity response = null;
        RequestMethod method = RequestMethod.fromString(verb);
        //Find the correct handler for this request
        for (RestRequestMapping m : handlers) {
            PathMatch match = m.getPathFilter().perFormMatch(path);
            if (match.isMatches()) {
                matchFound = true;
                //Once handler is found, check that it is the correct method
                if (method.equals(m.getRequestMethod())) {
                    request = new HttpRequest(match, path, RequestMethod.fromString(verb), req);
                    mapping = m;
                }
            }
        }
        try {
            //Send the request to the mapping.
            if (mapping != null) {
                if (method == RequestMethod.OPTIONS) {
                    response = new ResponseEntity("", HttpStatus.OK);
                } else {
                    response = mapping.executeRequest(request);
                }
                if (mapping.getCrossOrigins() != null) {
                    response.setOrigins(mapping.getCrossOrigins());
                }
            } else {
                if (matchFound) { //Bad method used.
                    throw new MethodNotAllowedException("Method not allowed.");
                }
                String errorMessage = "No handler found for request.";
                throw new NotFoundException(errorMessage);
            }

            if (response.getStatus().is4xxClientError() || response.getStatus().is5xxServerError()) {
            }
        } finally {
        }
        return response;

    }

    /**
     * Processes exceptions and passes them to the exception handling chain
     *
     * @param t - Throwable, the exception to be handled.
     * @return - ResponseEntity
     * @throws Throwable
     * @throws Exception
     */
    public ResponseEntity processException(Throwable t) throws Throwable, Exception {
        ExceptionMapping mapping = null;
        for (ExceptionMapping m : exceptionHandlers) {
            if (m.canHandle(t)) {
                mapping = m;
            }
        }
        if (mapping != null) {
            ResponseEntity responseEntity = mapping.handleException(t);
            CrossOrigins crossOrigins = new CrossOrigins("*");
            crossOrigins.setAllowHeaders(new String[]{"Content-Type", "Authorization", "Content-Length", "X-Requested-With"});
            crossOrigins.setMethods(new RequestMethod[]{RequestMethod.DELETE, RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS});
            responseEntity.setOrigins(crossOrigins);
            return responseEntity;
        } else {
            throw t;
        }
    }

    /**
     * Process the controller instance.
     *
     * @param object
     */
    private void processHandlers(Object object) {
        String topLevelPath = "";
        this.baseMapping = "";
        RequestMapping requestMappingOnClass = object.getClass().getAnnotation(RequestMapping.class);

        if (requestMappingOnClass != null) {
            topLevelPath = requestMappingOnClass.path();
            if (!topLevelPath.equalsIgnoreCase("/api")) {
                this.baseMapping = topLevelPath;
            }
        }

        Method[] methods = object.getClass().getDeclaredMethods();

        for (Method method : methods) {

            RequestMapping requestMappingOnMethod = method.getAnnotation(RequestMapping.class);
            //Produces produces = method.getAnnotation(Produces.class);
            CrossOrigins crossOrigins = null;
            CrossOrigin originsAnnotation = method.getAnnotation(CrossOrigin.class);
            if (originsAnnotation != null) {
                crossOrigins = new CrossOrigins(originsAnnotation.origins());
                crossOrigins.setMethods(originsAnnotation.methods());
                crossOrigins.setAllowHeaders(originsAnnotation.allowedHeaders());
            }

            CacheControl cacheControl = method.getAnnotation(CacheControl.class);

            CacheSettings cacheSettings;
            if (cacheControl != null) {

                cacheSettings = new CacheSettings(cacheControl.maxAge(), true);
            } else {
                cacheSettings = new CacheSettings(0, false);  //Do not cache
            }
            if (requestMappingOnMethod != null) {
                //Only the first RequestMapping annotation is used.

                String path = requestMappingOnMethod.path();

                String mappingPath = topLevelPath + path;

                logger.info("Method " + method.getName() + " has " + requestMappingOnMethod.method().length + " methods assigned.");

                //Create a RestRequestMapping Object for each request method defined.
                for (RequestMethod requestMethod : requestMappingOnMethod.method()) {
                    String contentType = requestMappingOnMethod.produces();

                    RestRequestMapping mapping
                            = new RestRequestMapping(mappingPath, requestMethod, object,
                                    method, this.executor, requestMappingOnMethod.description(), contentType, cacheSettings);

                    logger.info("Mapping " + mappingPath + "[" + requestMethod.toString() + "] "
                            + object.getClass().getCanonicalName() + " " + method.toGenericString());

                    if (crossOrigins != null) {
                        mapping.setCrossOrigins(crossOrigins);
                    }

                    this.handlers.add(mapping);
                }
            }

            //Process exception handlers.
            ExceptionHandler exceptionHandler = method.getAnnotation(ExceptionHandler.class);
            if (exceptionHandler != null) {
                //Only the first RequestMapping annotation is used.

                Class<? extends Throwable> throwable = exceptionHandler.exceptionClass();

                ExceptionMapping exceptionMapping = new ExceptionMapping(object, method, throwable);
                if (crossOrigins != null) {
                    exceptionMapping.setOrigins(crossOrigins);
                }

                logger.debug("Mapping exception handler " + object.getClass().getCanonicalName() + " for exception " + throwable.getCanonicalName());

                this.exceptionHandlers.add(exceptionMapping);

            }

        }
    }

    public String getBaseMapping() {
        return baseMapping;
    }

    public List<RestRequestMapping> getHandlers() {
        List<RestRequestMapping> list = new ArrayList<>();

        list.addAll(handlers);

        return handlers;
    }

}
