/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.annotations;

import com.tinyrest.core.server.http.request.RequestMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Sam
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CrossOrigin {

    String origins() default "*";
    RequestMethod[] methods() default {RequestMethod.DELETE, RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS};
    String[] allowedHeaders() default {"Content-Type","Authorization","Content-Length","X-Requested-With"};
    
}
