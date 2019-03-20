/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.beans;

import com.tinyrest.core.beans.builtin.BuiltInBeans;
import com.tinyrest.core.server.annotations.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sam
 */
public class BeanManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanManager.class);


    /*
    Hashmap that holds all beans by type that they produce.
    */
    private HashMap<Class<?>, BeanProducer> beans = new HashMap<>();

    /*
    Beans by name
    */
    private HashMap<String, BeanProducer> beansByName = new HashMap<>();

    /*
    List of beans
    */
    private List<BeanProducer> beanList = new ArrayList<>();

    /*
    All objects annotated with @RestController gets added to this list.
    */
    private List<Object> restControllers = new ArrayList<>();

    /**
     * Constructor
     */
    public BeanManager() {
        processBuiltInBeans();
    }


    private void addBean(BeanProducer bean) throws BeanException {
        beans.put(bean.getProduces(), bean);
        beanList.add(bean);
        beansByName.put(bean.getBeanName(), bean);
        LOGGER.debug("Adding bean " + bean.getBeanName());


        if (bean.getDependencies().isEmpty()) {
            //Needs no dependencies, so lets start it up
            try {
                LOGGER.debug("Bean has no dependencies, starting it up....");
                bean.setProduct(
                        bean.getProducer().invoke(bean.getProducerInstance(), new Object[]{}));   //On empty parameter list we pass empty object array

                bean.setInitialized(true);

            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException beanException) {

                LOGGER.error("Bean start error: " + beanException.getMessage());
                throw new BeanException("Could not add bean: " + beanException.getMessage());

            }
        }
    }

    /**
     * Get a bean by a specific type.
     *
     * @param type
     * @return
     */
    public BeanProducer getBeanByType(Class<?> type) {
        if (beans.containsKey(type)) {
            return beans.get(type);
        } else {
            return null;
        }
    }

    public BeanProducer getBeanByName(String beanName) throws BeanException {
        if (beansByName.containsKey(beanName)) {
            return beansByName.get(beanName);
        } else {
            throw new BeanException("No Bean reference found for name [" + beanName + "]");
        }
    }

    public Object getObjectByTypeAndName(Class<?> type, String name) throws BeanException {
        BeanProducer bean = getBeanByName(name);
        if (bean.getProduces() != type) {
            throw new BeanException("Bean referenced by name [" + name + "] does not produce the correct type.");
        }
        return bean.getProduct();
    }

    public Object getObjectByType(Class<?> type) throws BeanException {
        BeanProducer bean = getBeanByType(type);
        if (bean == null) {
            throw new BeanException("There is no bean of type " + type.getName());
        } else {
            return bean.getProduct();
        }
    }

    private BeanProducer processSpecialClasses(Object object) throws BeanException {

        if (object == null) {
            return null;
        }
        LOGGER.debug("Checking for special class in " + object.getClass().getSimpleName());

        RestController restController = object.getClass().getAnnotation(RestController.class);
        if (restController != null) {
            LOGGER.debug("Processing RestController " + object.getClass().getSimpleName());
            BeanProducer bean = new BeanProducer(object.getClass(), "RestController", new ArrayList<Class<?>>(), null, null);

            bean.setProduct(object);

            bean.setInitialized(true);

            return bean;
        } else {
            return null;
        }
    }

    public List<Object> getRestControllers() {

        return restControllers;
    }


    public void extractBeans(Object object) throws BeanException {

        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            Bean bean = method.getAnnotation(Bean.class);
            if (bean != null) {  //This method is a bean!

                //Extract the bean name  
                String beanName = bean.value();
                if (beanName.isEmpty()) {
                    beanName = method.getName();
                }

                //Extract Parameters (Dependencies)
                List<Class<?>> parameters = new ArrayList<>();
                Parameter[] params = method.getParameters();
                for (Parameter parameter : params) {
                    parameters.add(parameter.getType());
                }

                BeanProducer beanProducer
                        = new BeanProducer(method.getReturnType(), beanName, parameters, object, method);
                addBean(beanProducer);
            }
        }

        //All done, lets try and initialize.
    }

    public void initBeans() throws BeanException {
        this.init();
    }

    public Object getObject(String beanName) {
        if (beansByName.containsKey(beanName)) {
            BeanProducer bean = beansByName.get(beanName);
            return bean.getProduct();
        } else {
            return null;
        }
    }

    List<BeanProducer> listRestControllers() {
        List<BeanProducer> list = new ArrayList<>();
        beanList.stream().forEach((bean) -> {
            try {
                BeanProducer specialBean = processSpecialClasses(bean.getProduct());
                if (specialBean != null) {
                    list.add(specialBean);
                }
            } catch (BeanException be) {

            }
        });
        return list;
    }

    /**
     * INitializes a bean (Create the object that it produces and stores that object in the bean Object.)
     *
     * @param bean
     * @throws BeanException
     */
    private void initializeBean(BeanProducer bean) throws BeanException {
        Object[] parameters = new Object[bean.getDependencies().size()];
        LOGGER.debug("Initializing bean " + bean.getBeanName());

        //Set all the parameters (Dependencies)
        for (int c = 0; c < parameters.length; c++) {
            Class<?> type = bean.getDependencies().get(c);
            BeanProducer dependency = getBeanByType(type);
            parameters[c] = dependency.getProduct();
        }

        try {

            LOGGER.debug("Invoking method to create bean " + bean.getBeanName());
            Object obj = bean.getProducer().invoke(bean.getProducerInstance(), parameters);
            bean.setProduct(obj);
            bean.setInitialized(true);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException beanException) {
            LOGGER.error("Bean initialization error :" + beanException.getMessage());
            beanException.printStackTrace();
            throw new BeanException("Error initializing bean: " + beanException.getMessage());

        } catch (RuntimeException rte) {
            LOGGER.error("Bean initialization error :" + rte.getMessage());
            throw new BeanException("Unable to initialize beans: " + rte.getMessage());
        }
    }

    private void processRestControllers() {
        List<BeanProducer> specialBeans = listRestControllers();
        specialBeans.forEach(bean -> restControllers.add(bean.getProduct()));

    }

    private void processBuiltInBeans() {
        LOGGER.debug("Processing built-in beans...");
        BuiltInBeans bit = new BuiltInBeans();
        try {
            this.extractBeans(bit);
        } catch (BeanException be) {

            LOGGER.warn("Unable to add built-in beans : " + be.getMessage());

        }
    }

    private boolean dependencyExists(Class<?> dependency) {
        BeanProducer bean = getBeanByType(dependency);
        return (bean != null);
    }

    /**
     * Checks whether the beans dependencies have been initialized. Thows BeanException when a dependency does not
     * exist.
     *
     * @param bean - The bean to test
     * @return - Boolean
     * @throws BeanException
     */
    private boolean dependanciesAreInitialized(BeanProducer bean) throws BeanException {
        boolean initialized = true;

        for (Class<?> type : bean.getDependencies()) {
            if (!dependencyExists(type)) {  //a Dependency for this bean has not been provided.

                throw new BeanException("There is no bean that creates the dependency "
                        + type.getName() + " for bean " + bean.getBeanName());

            }
            BeanProducer dependency = beans.get(type);
            initialized = initialized && dependency.isInitialized();
        }
        return initialized;
    }

    /**
     * Initializes the defined beans by going through list and initializing all beans whose dependencies have been
     * initialized.
     *
     * @throws BeanException
     */
    private void init() throws BeanException {
        boolean listChanged = true;
        boolean allBeansInitialized = true;

        while (listChanged) {
            listChanged = false;
            allBeansInitialized = true;

            int c = 0;

            for (BeanProducer bean : beanList) {
                if (!bean.isInitialized()) {
                    if (dependanciesAreInitialized(bean)) {
                        initializeBean(bean);
                        listChanged = true;
                    }
                }
                c++;
            }

            for (BeanProducer bean : beanList) {
                allBeansInitialized = allBeansInitialized && bean.isInitialized();
            }

        }
        if (!allBeansInitialized) {
            StringBuilder sb = new StringBuilder();
            for (BeanProducer bean : beanList) {
                if (!bean.isInitialized()) {
                    sb.append(bean.getBeanName());
                    sb.append(" ");
                }
            }
            throw new BeanException("The following beans are uninitialized: " + sb.toString());
        } else {
            processRestControllers();
        }


    }

}
