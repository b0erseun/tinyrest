/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.annotations;

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

public @interface CacheControl {
    long maxAge() default 300000;   //default cache timeout of 5 minutes.
}
