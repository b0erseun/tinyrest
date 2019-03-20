/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.parameters;

import java.util.HashMap;
import java.util.Map;

import com.tinyrest.core.server.json.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Sam
 */
public class MapValueReader implements ParameterValueReader {

    private final Map<String, String> parameters = new HashMap();
    private final static Logger LOGGER = LoggerFactory.getLogger(MapValueReader.class);

    public MapValueReader(Map<String, String> params) {
        params.forEach((S1, S2) -> {
            parameters.put(S1, S2);

        });
    }

    @Override
    public Object readParameterValue(String name, String defaultValue, Class type) {
        if (parameters.containsKey(name)) {
            String mapValue = parameters.get(name);
            
            LOGGER.debug("map[\"" + name + "\"] = \"" + mapValue + "\" (" + type.getCanonicalName() + ")");

            if(type.isPrimitive() || type == String.class) {
                return ValueReader.readValue(mapValue, type);
            }else {
                return GsonFactory.createGson().fromJson(mapValue, type);
            }
        } else {
            return ValueReader.readValue(defaultValue, type);
        }
    }

}
