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

/**
 *
 * @author Sam
 */
@RestController
@RequestMapping(path = "/version")
public class VersionController {

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> getVersion() {

        return new ResponseEntity(RestApplication.getProjectVersionFromManifest(), HttpStatus.OK);
        
    }

}
