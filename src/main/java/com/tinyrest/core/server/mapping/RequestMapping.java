/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.mapping;

import com.tinyrest.core.server.http.request.HttpRequest;
import com.tinyrest.core.server.http.response.ResponseEntity;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Sam
 */
public interface RequestMapping {

    ResponseEntity executeRequest(HttpRequest request) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
    
    String getContentType();
}
