/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.parameters;

import com.tinyrest.core.server.mapping.PathMatch;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

/**
 *
 * @author Sam
 */
public class PathVariableParameterValueReader implements ParameterValueReader {

    private final static Logger LOGGER = LoggerFactory.getLogger(PathVariableParameterValueReader.class);

    private final PathMatch match;

    public PathVariableParameterValueReader(PathMatch match) {
        this.match = match;
    }

    @Override
    public Object readParameterValue(String name, String defaultValue, Class type) {
        if (match.getPathVariables().containsKey(name)) {
            String pathVariableValue = match.getPathVariables().get(name);
            LOGGER.debug("pathVariable[\"" + name + "\"] = \"" + pathVariableValue + "\" (" + type.getCanonicalName() + ")");
            return ValueReader.readValue(match.getPathVariables().get(name), type);
        } else {
            //We should actuall throw an exception here.
            return null;
        }
    }

}
