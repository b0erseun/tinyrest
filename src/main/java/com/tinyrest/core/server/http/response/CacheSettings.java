/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.http.response;

/**
 *
 * @author Sam
 */
public class CacheSettings {
    
    private final long maxAge;
    private final boolean mustCache;

    public CacheSettings(long maxAge, boolean mustCache) {
        this.maxAge = maxAge;
        this.mustCache = mustCache;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public boolean mustCache() {
        return mustCache;
    }
    
    
    
    
    
}
