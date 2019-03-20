/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server;

import com.tinyrest.core.server.annotations.RequestMapping;
import com.tinyrest.core.server.annotations.RestController;
import com.tinyrest.core.server.http.request.RequestMethod;
import com.tinyrest.core.server.http.response.HttpStatus;
import com.tinyrest.core.server.http.response.ResponseEntity;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Scanner;

import sun.security.pkcs11.wrapper.Constants;

/**
 *
 * @author Sam
 */
@RestController
@RequestMapping(path = "/config")
public class ConfigWriter {
    private final static String CONFIG_FILE = "application.properties";

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> getConfig() {
        Properties properties;
        String currentFolder = System.getProperty("user.dir");

        String fileName = currentFolder + "/config/" + CONFIG_FILE;

        File file = new File(fileName);
        try {
            Scanner scanner = new Scanner(file);
            StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
                builder.append(Constants.NEWLINE);
            }

            return new ResponseEntity<>(builder.toString(), HttpStatus.OK);
        } catch (FileNotFoundException fnf) {

            return new ResponseEntity<>("could not find config file " + fileName, HttpStatus.NOT_FOUND);

        }

    }

}
