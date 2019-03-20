/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.beans;

/**
 *
 * @author Sam
 */
public class BeanException extends Exception {

    /**
     * Creates a new instance of <code>BeanException</code> without detail message.
     */
    public BeanException() {
    }

    /**
     * Constructs an instance of <code>BeanException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public BeanException(String msg) {
        super(msg);
    }
}
