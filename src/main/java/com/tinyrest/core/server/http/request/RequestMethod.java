/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.http.request;

/**
 *
 * @author Sam
 */
public enum RequestMethod {
    GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE"), HEAD("HEAD"), OPTIONS("OPTIONS");

    private final String value;

    private RequestMethod(String value) {
        this.value = value;
    }

    public static RequestMethod fromString(String v) {
        if ("GET".equalsIgnoreCase(v)) {
            return GET;
        }
        if ("POST".equalsIgnoreCase(v)) {
            return POST;
        }
        if ("PUT".equalsIgnoreCase(v)) {
            return PUT;
        }
        if ("DELETE".equalsIgnoreCase(v)) {
            return DELETE;
        }
        if ("HEAD".equalsIgnoreCase(v)) {
            return HEAD;
        }
        if ("OPTIONS".equalsIgnoreCase(v)) {
            return OPTIONS;
        }
        return GET;
    }

}
