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
public class MissingParamException extends Exception{ 

    public MissingParamException() {
    }

    public MissingParamException(String message) {
        super(message);
    }

    public MissingParamException(Throwable cause) {
        super(cause);
    }
    
    
}
