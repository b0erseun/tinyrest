/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.mapping;

import com.tinyrest.core.server.http.response.CrossOrigins;
import com.tinyrest.core.server.http.response.ResponseEntity;
import java.lang.reflect.Method;

/**
 *
 * @author Sam
 */
public class ExceptionMapping {
    
    private final Object exceptionHandlerInstance;
    
    private final Method method;
    
    private final Class<? extends Throwable> exceptionType;
    
    private CrossOrigins origins = null; 

    public ExceptionMapping(Object obj, Method method, Class<? extends Throwable> exceptionType) {
        this.exceptionHandlerInstance = obj;
        this.method = method;
        this.exceptionType = exceptionType;
    }
    
    
    
    public boolean canHandle(Throwable exception) {
        return exceptionType.isAssignableFrom(exception.getClass());
    }
    
    public ResponseEntity handleException(Throwable t) 
            throws Exception  {
        
        Object[] object = new Object[1];
        
        object[0] = t;
        
        Object responseObj = method.invoke(exceptionHandlerInstance, object);
        
        if (responseObj instanceof ResponseEntity) {
            ResponseEntity responseEntity = (ResponseEntity) responseObj;
            if (origins != null) {
                 responseEntity.setOrigins(origins);
            }
            return responseEntity;
        } else {
            throw new Exception ("The exception handler did not return the correct type of object.");
        }
        
        
    }

    public CrossOrigins getOrigins() {
        return origins;
    }

    public void setOrigins(CrossOrigins origins) {
        this.origins = origins;
    }
    
    
    
}
