/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.http.request;

import com.tinyrest.core.server.mapping.PathMatch;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sam
 */
public class HttpRequest {

    private final String requestPath;

    private final Map<String, String> requestParameters = new HashMap<>();

    private final Map<String, String> requestHeaders = new HashMap<>();

    private final Map<String, String> pathVariables = new HashMap<>();

    private final RequestMethod requestMethod;

    private final PathMatch pathMatch;

    private final HttpServletRequest request;

    public HttpRequest(PathMatch pathMatch, String requestPath, RequestMethod requestMethod, HttpServletRequest request) {
        this.requestPath = requestPath;
        this.requestMethod = requestMethod;
        this.request = request;
        this.pathMatch = pathMatch;

        request.getParameterMap().forEach((S1, S2) -> {
            this.requestParameters.put(S1, S2[0]);
        });
        
        this.pathVariables.putAll(pathMatch.getPathVariables());
    }

    public String getRequestPath() {
        return requestPath;
    }

    public Map<String, String> getRequestParameters() {
        return requestParameters;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

}
