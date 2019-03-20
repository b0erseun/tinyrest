/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.servlets;

/**
 *
 * @author Sam
 */
public class ServerMonitorException extends Exception {

    /**
     * Creates a new instance of <code>ServerMonitorException</code> without detail message.
     */
    public ServerMonitorException() {
    }

    /**
     * Constructs an instance of <code>ServerMonitorException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ServerMonitorException(String msg) {
        super(msg);
    }
}
