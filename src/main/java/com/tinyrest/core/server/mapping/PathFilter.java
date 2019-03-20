/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Sam
 */
public class PathFilter {
    
    private final String pathTemplate;
    
    private final List<String> pathItems = new ArrayList<>();
    
    private final List<String> pathVariables = new ArrayList<>();

    public PathFilter(String pathPattern) {
        this.pathTemplate = pathPattern;
        decodePath();
    }
    
    private void decodePath() {
        //Break up the path into parts
        this.pathItems.clear();
        this.pathItems.addAll(breakPath(pathTemplate));
    }
    
    public PathMatch perFormMatch(String testPathPattern) {
        PathMatch match = new PathMatch();
        match.setMatches(false);

        List<String> items = breakPath(testPathPattern);

        if (items.size() != pathItems.size()) {
            return match;
        }
        
        match.setMatches(true); //start with true, test as we go
        for (int c = 0; c < pathItems.size(); c++) {
            String patternItem = pathItems.get(c);
            String testItem = items.get(c);
            if (isVariable(patternItem)) {
                match.getPathVariables().put(getVariableName(patternItem), testItem);
            } else {
                if (!patternItem.equalsIgnoreCase(testItem)) {
                    match.getPathVariables().clear();
                    match.setMatches(false);
                    return match;
                }
            }
        }
        return match;
        
        
    }
    
    private List<String> breakPath(String path) {
        String temp = path;
        if (temp.startsWith("/")) {
            temp = temp.substring(1);
        }
        if (temp.endsWith("/")) {
            temp = temp.substring(0, temp.length() - 1);
        }
        
        String[] strings = temp.split("/");
        
        return Arrays.asList(strings);
        
    }

    private boolean isVariable(String item) {
         return item.startsWith("{") && item.endsWith("}");
    }
    
    
    private String getVariableName(String item) {
        if (isVariable(item)) {
            String temp = item.substring(1, item.length() - 1);
            return temp;
        } else {
            return "";
        }
        
    }
    
    
}
