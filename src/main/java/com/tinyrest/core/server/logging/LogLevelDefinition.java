/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.logging;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sam
 */
public class LogLevelDefinition {
    
    private String rootLevel;
    
    private Map<String, String> levels = new HashMap<>();

    public String getRootLevel() {
        return rootLevel;
    }

    public void setRootLevel(String rootLevel) {
        this.rootLevel = rootLevel;
    }

    public Map<String, String> getLevels() {
        return levels;
    }

    public void setLevels(Map<String, String> levels) {
        this.levels = levels;
    }
    
    
}
