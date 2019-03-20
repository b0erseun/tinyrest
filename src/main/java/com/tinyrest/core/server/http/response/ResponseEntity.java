/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.http.response;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author Sam
 * @param <T> Object that this response netity will be returning.
 */
public class ResponseEntity<T extends Object> {
    
    private final Map<String, Object> responseHeaders = new HashMap<>();
    
    private final T t;
    
    private final HttpStatus status;
    
    private String contentType = "application/json";
    
    private CrossOrigins origins = null;
    
    public ResponseEntity(T t, HttpStatus status) {
        this.t = t;
        this.status = status;
    }

    
   

    public Map<String, Object> getResponseHeaders() {
        return responseHeaders;
    }

    public T getBody() {
        return t;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public CrossOrigins getCrossOrigins() {
        return origins;
    }

    public void setOrigins(CrossOrigins origins) {
        this.origins = origins;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    
    
    
    
    
    
    
    
}
