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
public class InvalidHandlerException extends Exception {

    /**
     * Creates a new instance of <code>InvalidHandler</code> without detail
     * message.
     */
    public InvalidHandlerException() {
    }

    /**
     * Constructs an instance of <code>InvalidHandler</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public InvalidHandlerException(String msg) {
        super(msg);
    }
}
