/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.http.response;

import com.tinyrest.core.server.http.request.RequestMethod;

import java.util.StringJoiner;

/**
 *
 * @author Sam
 */
public class CrossOrigins {
    
    private String origins;
    
    private RequestMethod[] methods = new RequestMethod[]{
        RequestMethod.DELETE, 
        RequestMethod.GET, 
        RequestMethod.HEAD, 
        RequestMethod.POST, 
        RequestMethod.PUT};

    public CrossOrigins(String origins) {
        this.origins = origins;
    }

    
    
    
    private String[] allowHeaders = new String[]{"Content-Type","Authorization","Content-Length","X-Requested-With"};

    public String getOrigins() {
        return origins;
    }

    public void setOrigins(String origins) {
        this.origins = origins;
    }

    public String getMethods() {
        StringJoiner joiner = new StringJoiner(",");
        for (RequestMethod method : methods) {
            joiner = joiner.add(method.name());
        }
        
        return joiner.toString();
    }

    public void setMethods(RequestMethod[] methods) {
        this.methods = methods;
    }

    public String getAllowHeaders() {
        StringJoiner joiner = new StringJoiner(",");
        for (String string : this.allowHeaders) {
            joiner = joiner.add(string);
        }
        return joiner.toString();
    }
    
    public void setAllowHeaders(String[] allowHeaders) {
        this.allowHeaders = allowHeaders;
    }
    
    
    
}
