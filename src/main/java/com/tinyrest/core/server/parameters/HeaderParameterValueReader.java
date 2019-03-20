/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.parameters;


import com.tinyrest.core.server.exceptions.AuthenticationException;
import com.tinyrest.core.server.json.GsonFactory;
import com.tinyrest.core.server.http.request.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Sam
 */
public class HeaderParameterValueReader implements ParameterValueReader{

    private final HttpRequest request;
    
    private final static Logger LOGGER = LoggerFactory.getLogger(HeaderParameterValueReader.class);

    public HeaderParameterValueReader(HttpRequest request) {
        this.request = request;
    }

    
    
    
    @Override
    public Object readParameterValue(String name, String defaultValue, Class type) {
        String headerValue = this.request.getRequest().getHeader(name);
        LOGGER.debug("header[\"" + name + "\"] = \"" + headerValue + "\" (" + type.getCanonicalName() + ")");
        if (headerValue != null) {
            if(type.isPrimitive() || String.class == type) {
                return ValueReader.readValue(headerValue, type);
            }else {
                return GsonFactory.createGson().fromJson(headerValue, type);
            }
        } else {
            if("Authorization".equalsIgnoreCase(name)){
                throw new AuthenticationException("Expecting Authorization header which is not present");
            }
            return ValueReader.readValue(defaultValue, type);
        }
    }
    
    
}
