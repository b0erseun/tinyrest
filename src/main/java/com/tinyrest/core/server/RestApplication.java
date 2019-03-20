/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server;

import com.tinyrest.core.beans.BeanException;
import com.tinyrest.core.beans.BeanManager;
import com.tinyrest.core.beans.BeanProducer;
import com.tinyrest.core.server.annotations.Configuration;
import com.tinyrest.core.server.annotations.RequestMapping;
import com.tinyrest.core.server.annotations.RestController;
import com.tinyrest.core.server.annotations.RestService;
import com.tinyrest.core.server.cleanup.Cleanup;
import com.tinyrest.core.server.contexts.ApplicationContext;
import com.tinyrest.core.server.manager.RequestHandler;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.*;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;
import org.eclipse.jetty.websocket.server.pathmap.ServletPathSpec;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sam
 */
public class RestApplication {
    private static Logger LOGGER;
    private static ApplicationContext context;
    private static final String logo = "___________.__              __________                 __   \n" +
            "\\__    ___/|__| ____ ___.__.\\______   \\ ____   _______/  |_ \n" +
            "  |    |   |  |/    <   |  | |       _// __ \\ /  ___/\\   __\\\n" +
            "  |    |   |  |   |  \\___  | |    |   \\  ___/ \\___ \\  |  |  \n" +
            "  |____|   |__|___|  / ____| |____|_  /\\___  >____  > |__|  \n" +
            "                   \\/\\/             \\/     \\/     \\/        ";

    static {
        String log4jPath = "./config/log4j.properties";
        PropertyConfigurator.configure(log4jPath);
        LOGGER = LoggerFactory.getLogger(RestApplication.class);
        LOGGER.info("\n" + logo);
    }

    private static RestServer restServer;
    private static final BeanManager BEAN_MANAGER = new BeanManager();
    private static String serviceName = "";
    private static List<RequestHandler> requestHandlers = new ArrayList<>();
    private static final List<Cleanup> objectsForCleanup = new ArrayList<>();

    public static RequestHandler getRequestHandler() {
        try {
            return (RequestHandler) BEAN_MANAGER.getObjectByType(RequestHandler.class);
        } catch (BeanException be) {
            LOGGER.error("Could not get RequestHandler instance from bean manager.");
            return null;
        }
    }

    private static void registerServiceStartup() throws BeanException, Exception {
        String port = String.valueOf(restServer.getPort());
        String host = String.valueOf(restServer.getHost());
        RequestHandler requestHandler = getRequestHandler();
        String servicePath = requestHandler.getBaseMapping();
        String version = getProjectVersionFromManifest();

        String serviceBaseUrl = "http://" + host + ":" + port + servicePath;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdown();
            } catch (Exception ex) {

            }
        }));
    }

    public static String getProjectVersionFromManifest() {
        try {
            InputStream propertiesIs = RestApplication.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
            Properties prop = new Properties();
            prop.load(propertiesIs);
            return prop.getProperty("Implementation-Version");
        } catch (Exception e) {
            e.printStackTrace();
            return "UNKNOWN";
        }
    }

    private static void processRestController(RestServer restServer, Class<?> controller) throws Exception {

//        RestControllerContext context = createRestControllerContext(controller);
//        if (context != null) {
//            restServer.addServletContextHandler(context.getServletContextHandler());
//        }
        RequestHandler requestHandler = getRequestHandler();
        Object restControllerInstance = BEAN_MANAGER.getObjectByType(controller);
        if (restControllerInstance == null) {
            LOGGER.warn("The rest controller " + controller + " is not produced by any beans, it will not be initialized.");
        }
        requestHandler.addHandler(restControllerInstance);

    }

    private static Set<Class<?>> scanClassPathForAnnotatedClass(String classPath, Class<? extends Annotation> annotation) {
        Reflections reflect = new Reflections(classPath);
        Set<Class<?>> classes = reflect.getTypesAnnotatedWith(annotation);
        return classes;
    }

    private static void processRestControllers(RestServer restServer, String classPath) throws Exception {
        Set<Class<?>> coreRestControllers = scanClassPathForAnnotatedClass(classPath, RestController.class);
        /*
        We instantiate Rest Controllers 1 by 1 by using the BEAN manager.   This means that all restControllers must
        have a bean defined that creates them.
         */
        for (Class<?> aClass : coreRestControllers) {
            processRestController(restServer, aClass);
        }

    }

    private static void processWebSockets(RestServer restServer, Set<Object> webSockets) {
        if (!webSockets.isEmpty()) {
            ServletContextHandler webSocketSch = restServer.getWebSocketContextHandler();

            try {
                for (Object webSocket : webSockets) {

                    WebSocketUpgradeFilter wsFilter = restServer.getWsFilter();
                    if (webSocket != null) {
                        RequestMapping mapping = webSocket.getClass().getAnnotation(RequestMapping.class);
                        if (mapping != null) {
                            String path = mapping.path();

                            wsFilter.addMapping(new ServletPathSpec(path), (sur, sur1) -> {
                                return webSocket;
                            });
                        }
                    }
                }
            } catch (Exception e) {

                LOGGER.error("There was an exception processing websockets: " + e.getMessage());

            }
        }
    }

    public static Set<Object> getWebSocketInstances(Set<Class<?>> webSocketClasses) throws Exception {
        Set<Object> objs = new HashSet<>();
        for (Class<?> webSocket : webSocketClasses) {
            BeanProducer bp = BEAN_MANAGER.getBeanByType(webSocket);
            if (bp == null) {
                LOGGER.warn("There is no bean that produces an instance of WebSocket " + webSocket.getName() + ".");
            } else {
                Object obj = bp.getProduct();
                objs.add(obj);
            }
        }
        return objs;
    }

    public static void run(String[] args, Class<?> appClass) throws Exception {

        //First determine the classpath of the app we are running.
        String className = appClass.getSimpleName();
        String classPath = appClass.getCanonicalName().replace("." + className, "");

        RestService restService = appClass.getAnnotation(RestService.class);
        if (restService == null) {
            LOGGER.error("The Main Application class needs to be annotated with @RestService");
            throw new Exception("Main app class not annotated with @RestService");
        }

        serviceName = restService.name();

        try {
            if ((serviceName == null) || (serviceName.isEmpty())) {
                LOGGER.error("The @RestService annotation requires a name to be specified.");
                throw new Exception("You need to specify a name for the service in @RestService annotation.");
            }

            Set<Class<?>> configs = scanClassPathForAnnotatedClass(classPath, Configuration.class);

            if (configs.isEmpty()) {
                LOGGER.warn("No classes annotated with @Configuration were found in the classpath " + classPath);
            }

            for (Class<?> aClass : configs) {

                LOGGER.info("Extracting beans from class " + aClass.getName());
                try {
                    Object obj = aClass.newInstance();
                    BEAN_MANAGER.extractBeans(obj);
                } catch (IllegalAccessException iaex) {
                    LOGGER.error("The configuration class " + aClass.getName()
                            + " has an inaccessable constructor.");
                } catch (InstantiationException iex) {
                    LOGGER.error("The configuration class " + aClass.getName()
                            + " threw an exception during initialization. " + iex.getMessage());
                } catch (BeanException be) {
                    LOGGER.error("Could not extract beans from class " + aClass.getName() + " " + be.getMessage());
                }

            }

            BEAN_MANAGER.initBeans();

            restServer = (RestServer) BEAN_MANAGER.getObject("restServer");

            processRestControllers(restServer, "com.syzygy.core");
            processRestControllers(restServer, classPath);

            Set<Class<?>> webSockets = scanClassPathForAnnotatedClass(classPath, WebSocket.class);

            processWebSockets(restServer, getWebSocketInstances(webSockets));

            if (!serviceName.equalsIgnoreCase("phonebook")) {
                registerServiceStartup();
            }

        } finally {
        }

        restServer.startServer();

    }

    private static void cleanupObject(Cleanup cleanup) {

        try {
            LOGGER.info("Cleaning up object " + cleanup.getClass().getSimpleName());
            cleanup.cleanup();

        } catch (Exception ex) {

            LOGGER.error("Exception during cleanup " + ex.getMessage());

        }

    }

    private static void doCleanup() {
        objectsForCleanup.forEach(cleanup -> cleanupObject(cleanup));
    }

    /**
     * If an object implements the cleanup interface, it needs to be regsitered to be cleaned at shutdown. I may later
     * add the @Cleanup annotation to indicate that a bean needs to be cleaned up.
     *
     * @param cleanup
     */
    public static void registerObjectForCleanup(Cleanup cleanup) {
        objectsForCleanup.add(cleanup);
    }

    public static <T> T getBean(Class<T> type) {
        return (T) BEAN_MANAGER.getBeanByType(type).getProduct();
    }

    public static String getServiceName() {
        return serviceName;
    }

    /**
     * Unregisters the application with the phonebook.
     *
     * @throws Exception
     */
    public static void shutdown() throws Exception {

        doCleanup();


    }

}
