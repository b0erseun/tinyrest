/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.servlets;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sam
 */
public class ServerActivityMonitorImpl implements ServerActivityMonitor{

    private int callCount = 0;
    
    private final int maxThreads;

    public ServerActivityMonitorImpl(int maxThreads) {
        this.maxThreads = maxThreads;
    }
    
    @Override
    public synchronized void startCall(HttpServletRequest httpServletRequest) throws ServerMonitorException {
        this.callCount++;
        if (this.maxThreads > 0) {
            if (this.callCount > this.maxThreads) {
                throw new ServerMonitorException("Too many calls made to server.");
            }
        }
    }

    @Override
    public synchronized void endCall(HttpServletRequest httpServletRequest) {
        this.callCount--;
    }
    
    
}
