/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.manager;

/**
 *
 * @author Sam
 */
public class MethodNotAllowedException extends Exception{

    public MethodNotAllowedException() {
    }

    public MethodNotAllowedException(String message) {
        super(message);
    }

    public MethodNotAllowedException(Throwable cause) {
        super(cause);
    }
    
    
}
