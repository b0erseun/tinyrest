/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.mapping;

import com.tinyrest.core.server.annotations.PathVariable;
import com.tinyrest.core.server.annotations.RequestBody;
import com.tinyrest.core.server.annotations.RequestParam;
import com.tinyrest.core.server.annotations.ValueConstants;
import com.tinyrest.core.server.parameters.*;
import com.tinyrest.core.server.http.request.HttpRequest;
import com.tinyrest.core.server.http.request.RequestMethod;
import com.tinyrest.core.server.http.response.ResponseEntity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import com.tinyrest.core.server.annotations.RequestHeader;
import com.tinyrest.core.server.http.response.CacheSettings;
import com.tinyrest.core.server.http.response.CrossOrigins;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sam
 */
public class RestRequestMapping implements RequestMapping {

    private final String path;
    
    //Request method.  GET, POST, DELETE etc
    private final RequestMethod requestMethods;

    private PathFilter pathFilter;

    private CrossOrigins crossOrigins = null;

    //Class that is to handle the request, must have at least 1 method that is annotated with (GET, POST, PUT, DELETE)
    private final Object requestHandlerInstance;

    //The actual method that needs to be called.
    private final Method method;

    private final String contentType;

    private final ThreadPoolExecutor executor;

    private final static Logger LOGGER = LoggerFactory.getLogger(RestRequestMapping.class);

    private final String description;
    
    private final CacheSettings cacheSettings;

    public RestRequestMapping(String path, RequestMethod requestMethods,
                              Object requestHandlerClass, Method method, ThreadPoolExecutor executor, String description, String contentType, CacheSettings cacheSettings) {
        this.path = path;
        this.pathFilter = new PathFilter(path);
        this.requestMethods = requestMethods;
        this.requestHandlerInstance = requestHandlerClass;
        this.method = method;
        this.executor = executor;
        this.description = description;
        this.contentType = contentType;
        this.cacheSettings = cacheSettings;
    }

    public ResponseEntity executeCallable(HttpRequest request)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        //Check Parameters
        Parameter[] parameters = this.method.getParameters();
        Callable<ResponseEntity> callable = null;
        ResponseEntity response = null;
        Object[] invokeParameters = new Object[parameters.length];
        for (int c = 0; c < parameters.length; c++) {
            Parameter parameter = parameters[c];

            String paramName = "";

            String defaultValue = null;

            Class<?> paramType = parameter.getType();

            ParameterValueReader pvr = null;
            RequestReader rr = null;

            RequestParam param = parameter.getAnnotation(RequestParam.class);
            if (param != null) {
                pvr = new MapValueReader(request.getRequestParameters());
                paramName = param.name();
                if (!param.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
                    defaultValue = param.defaultValue();
                }
            }

            PathVariable pathV = parameter.getAnnotation(PathVariable.class);
            if (pathV != null) {
                pvr = new MapValueReader(request.getPathVariables());
                paramName = pathV.value();
            }

            RequestHeader header = parameter.getAnnotation(RequestHeader.class);
            if (header != null) {
                pvr = new HeaderParameterValueReader(request);
                paramName = header.name();
                if (!header.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
                    defaultValue = header.defaultValue();
                }

            }

            RequestBody body = parameter.getAnnotation(RequestBody.class);
            if (body != null) {
                rr = new RequestBodyReader(request);
                paramName = "";
            }

            if (paramName.isEmpty()) {
                paramName = parameter.getName();

            }
            if (pvr != null) {
                invokeParameters[c] = pvr.readParameterValue(paramName, defaultValue, paramType);
            } else if (rr != null) {
                Class encodingClass = parameter.getType();
                Class componentType = encodingClass;
                if (Collection.class.isAssignableFrom(encodingClass)) {
                    componentType = getClassFromName(((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0].getTypeName());
                } else if (encodingClass.isArray()) {
                    componentType = encodingClass.getComponentType();
                }
                invokeParameters[c] = rr.readParameterValue(encodingClass, componentType);
            } else {
                invokeParameters[c] = null;
            }

        }
        callable = (Callable) method.invoke(requestHandlerInstance, invokeParameters);
        try {

            response = callable.call();

//            response = responseFuture.get();
        } catch (InterruptedException | ExecutionException ex) {
        LOGGER.error("Error when trying to execute Callable " + ex.getMessage(), ex);
        throw new InvocationTargetException(ex);

    } catch (Exception ex) {
        LOGGER.error("Error while executing callable. " + ex.getMessage(), ex);
        throw new InvocationTargetException(ex);
    }
        if (this.cacheSettings.mustCache()) {
            response.getResponseHeaders().put("Cache-Control", "max-age=" + cacheSettings.getMaxAge());
        } else {
            response.getResponseHeaders().put("Cache-Control", "No-Cache");
        }
        
        return response;
    }

    @Override
    public String getContentType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseEntity executeRequest(HttpRequest request)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ResponseEntity response;
        if (Callable.class.isAssignableFrom(this.method.getReturnType())) {

            response = executeCallable(request);
        } else {
            response = executeResponseEntity(request);
        }

        response.setContentType(contentType);

        if (this.cacheSettings.mustCache()) {
            response.getResponseHeaders().put("Cache-Control", "max-age=" + cacheSettings.getMaxAge());
        } else {
            response.getResponseHeaders().put("Cache-Control", "No-Cache");
        }

        return response;
    }

    public ResponseEntity executeResponseEntity(HttpRequest request)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        //Check Parameters
        Parameter[] parameters = this.method.getParameters();
        ResponseEntity response = null;
        Object[] invokeParameters = new Object[parameters.length];
        for (int c = 0; c < parameters.length; c++) {
            Parameter p = parameters[c];

            String paramName = "";

            String defaultValue = null;

            Class<?> paramType = p.getType();

            ParameterValueReader pvr = null;
            RequestReader rr = null;

            RequestParam param = p.getAnnotation(RequestParam.class);
            if (param != null) {
                pvr = new MapValueReader(request.getRequestParameters());
                paramName = param.name();
                if (!param.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
                    defaultValue = param.defaultValue();
                }
            }

            PathVariable pathV = p.getAnnotation(PathVariable.class);
            if (pathV != null) {
                pvr = new MapValueReader(request.getPathVariables());
                paramName = pathV.value();
            }

            RequestHeader header = p.getAnnotation(RequestHeader.class);
            if (header != null) {
                pvr = new HeaderParameterValueReader(request);
                paramName = header.name();
                if (!header.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
                    defaultValue = header.defaultValue();
                }

            }

            RequestBody body = p.getAnnotation(RequestBody.class);
            if (body != null) {
                rr = new RequestBodyReader(request);
                paramName = "";
            }

            if (paramName.isEmpty()) {
                paramName = p.getName();

            }
            if (pvr != null) {
                invokeParameters[c] = pvr.readParameterValue(paramName, defaultValue, paramType);
            } else if (rr != null) {
                Class encodingClass = p.getType();
                Class componentType = encodingClass;
                if (Collection.class.isAssignableFrom(encodingClass)) {
                    componentType = getClassFromName(((ParameterizedType) p.getParameterizedType()).getActualTypeArguments()[0].getTypeName());
                } else if (encodingClass.isArray()) {
                    componentType = encodingClass.getComponentType();
                }
                invokeParameters[c] = rr.readParameterValue(encodingClass, componentType);
            } else {
                invokeParameters[c] = null;
            }
        }

        response = (ResponseEntity) method.invoke(requestHandlerInstance, invokeParameters);

        if (this.cacheSettings.mustCache()) {
            response.getResponseHeaders().put("Cache-Control", "max-age=" + cacheSettings.getMaxAge());
        } else {
            response.getResponseHeaders().put("Cache-Control", "No-Cache");
        }
        

        return response;

    }

    public Class getClassFromName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException cne) {
            return null;
        }
    }

    
    public String getPath() {
        return path;
    }

    public RequestMethod getRequestMethod() {
        return requestMethods;
    }

    public PathFilter getPathFilter() {
        return pathFilter;
    }

    public Object getRequestHandlerClass() {
        return requestHandlerInstance;
    }

    public Method getMethod() {
        return method;
    }

    public CrossOrigins getCrossOrigins() {
        return crossOrigins;
    }

    public void setCrossOrigins(CrossOrigins crossOrigins) {
        this.crossOrigins = crossOrigins;
    }

    public String getDescription() {
        return description;
    }

    public CacheSettings getCacheSettings() {
        return cacheSettings;
    }
    
    

}
