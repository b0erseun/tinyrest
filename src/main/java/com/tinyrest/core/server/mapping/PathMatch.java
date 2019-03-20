/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sam
 */
public class PathMatch {
    
    private boolean matches;
    
    private final Map<String, String> pathVariables = new HashMap<>();

    
    public boolean isMatches() {
        return matches;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }

    public void setMatches(boolean matches) {
        this.matches = matches;
    }

    @Override
    public String toString() {
        return "PathMatch{" + "matches=" + matches + ", pathVariables=" + pathVariables + '}';
    }

    
    
    
}
