/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.beans;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sam
 */
public class BeanProducer {
    
    /*
    The type of object that is produced by this bean
    */
    private final Class<?> produces;

    /*
    The name of the bean.  If not specified, it defaults to the name of the method that creates the bean.
    */    
    private final String beanName;
    
    /*
    The bean dependencies, which are the parameters of the method that creates the bean
    */
    private final List<Class<?>> dependencies;

    /*
    The instance of the object that the method which creates the bean belongs to.
    */
    private final Object producerInstance;    

    /*
    The java reflection method of the bean
    */
    private final Method producer;
    
    private boolean initialized = false;
    
    /*
    The actual object created by the bean.
    */
    private Object product;

    /**
     * Class that represents a bean producer.   Creates an instance of an object.
     * @param produces - Type of object that this bean produces
     * @param beanName - The name of the bean.  If not specified in the annotation, takes the name of the method.
     * @param dependencies - List of types that are needed for parameters
     * @param producerInstance - The instance of the object whose methods produce beans
     * @param producer - The method that the bean is cerated from.
     */
    public BeanProducer(Class<?> produces, String beanName, List<Class<?>> dependencies, Object producerInstance, Method producer) {
        this.produces = produces;
        if (beanName == null || beanName.isEmpty()) {
            this.beanName = produces.getSimpleName();
        } else {
            this.beanName = beanName;
        }
        this.dependencies = new ArrayList<>();
        this.dependencies.addAll(dependencies);
        this.producerInstance = producerInstance;
        this.producer = producer;
    }

    /**
     * The type of object that this bean creates
     * @return 
     */
    public Class<?> getProduces() {
        return produces;
    }

    /**
     * Get the name of the bean.  This is either set via the annotation, or else it defaults to the method name
     * @return 
     */
    public String getBeanName() {
        return beanName;
    }

    /**
     * Returns a list of types that this bean needs as parameters.
     * @return 
     */
    public List<Class<?>> getDependencies() {
        return dependencies;
    }
    

    /**
     * The instance of the object that produces the beans.
     * @return 
     */
    public Object getProducerInstance() {
        return producerInstance;
    }

    public Method getProducer() {
        return producer;
    }

    public Object getProduct() {
        return product;
    }

    public void setProduct(Object product) {
        this.product = product;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }


    
}
