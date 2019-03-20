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
public interface ServerActivityMonitor {
    
    void startCall(HttpServletRequest httpServletRequest) throws ServerMonitorException;
    
    void endCall(HttpServletRequest httpServletRequest);
    
}
