/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server;

/**
 *
 * @author Sam
 */
public class RestApplicationException extends Exception {

    /**
     * Creates a new instance of <code>RestApplicationException</code> without detail message.
     */
    public RestApplicationException() {
    }

    /**
     * Constructs an instance of <code>RestApplicationException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public RestApplicationException(String msg) {
        super(msg);
    }
}
