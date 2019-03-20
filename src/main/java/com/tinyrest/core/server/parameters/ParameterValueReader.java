/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.parameters;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sam
 */
public interface ParameterValueReader {
    
    Object readParameterValue(String name, String defaultValue, Class type);
    
}
