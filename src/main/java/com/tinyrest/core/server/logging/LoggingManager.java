/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.logging;

import com.tinyrest.core.server.annotations.RequestBody;
import com.tinyrest.core.server.annotations.RequestMapping;
import com.tinyrest.core.server.annotations.RestController;
import com.tinyrest.core.server.http.request.RequestMethod;
import com.tinyrest.core.server.http.response.HttpStatus;
import com.tinyrest.core.server.http.response.ResponseEntity;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Sam
 */
@RestController
@RequestMapping(path = "/loglevel")
public class LoggingManager {

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<LogLevelDefinition> getLoggers() {
        LogLevelDefinition def = new LogLevelDefinition();
        String rootLevel = (Logger.getRootLogger().getLevel() != null) ? Logger.getRootLogger().getLevel().toString() : "";
        def.setRootLevel(rootLevel);
        Enumeration<Category> loggers = LogManager.getCurrentLoggers();
        while (loggers.hasMoreElements()) {
            Category cat = loggers.nextElement();
            String name = cat.getName();
            String level = cat.getLevel() != null ? cat.getLevel().toString() : "";
            if (!level.isEmpty()) {
                def.getLevels().put(name, level);
            }
        }
        return new ResponseEntity(def, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<LogLevelDefinition> setLogLevel(@RequestBody LogLevelDefinition definition) {

        Logger.getRootLogger().setLevel(Level.toLevel(definition.getRootLevel()));

        definition.getLevels().entrySet().stream().forEach((entry) -> {
            Logger.getLogger(entry.getKey()).setLevel(Level.toLevel(entry.getValue()));
        });

        return getLoggers();
    }

}
