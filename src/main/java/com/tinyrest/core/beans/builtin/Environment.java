/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.beans.builtin;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author Sam
 */
public class Environment {

    private final static Logger LOGGER = Logger.getLogger(Environment.class);

    private final static String CONFIG_FILE = "application.properties";

    private Properties properties;

    public Environment() {

        String currentFolder = System.getProperty("user.dir");
        
        String fileName = currentFolder + "/config/" + CONFIG_FILE;

        File file = new File(fileName);
        
        this.properties = new Properties();
        try {

            try (FileInputStream fis = new FileInputStream(file)) {
                this.properties.load(fis);
            }

        } catch (IOException ioex) {

            LOGGER.warn("Could not read environment properties fro:m " + fileName);
        }

    }

    public String getProperty(String propertyName) {
        return this.properties.getProperty(propertyName);
    }

    public String getProperty(String propertyName, String defaultValue) {
        return this.properties.getProperty(propertyName, defaultValue);
    }

    public Properties getProperties() {
        return properties;
    }

}
