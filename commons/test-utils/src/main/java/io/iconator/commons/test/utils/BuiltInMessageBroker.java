package io.iconator.commons.test.utils;


import org.apache.qpid.server.SystemLauncher;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BuiltInMessageBroker {

    private static final String INITIAL_CONFIGURATION = "qpid-test-initial-config.json";
    private static final String SYSTEM_PROPERTIES = "system.properties";

    private static SystemLauncher broker;

    public static void start() throws Exception {
        broker = new SystemLauncher();
        broker.startup(createSystemConfig());
    }

    private static Map<String, Object> createSystemConfig() {
        Map<String, Object> attributes = new HashMap<>();
        URL initialConfig = BuiltInMessageBroker.class.getClassLoader().getResource(INITIAL_CONFIGURATION);

        //if this property is not found, the default classpath:system.properties will be used, that is causing
        //a java.net.MalformedURLException: unknown protocol: classpath at
        //org.apache.qpid.server.SystemLauncher.populateSystemPropertiesFromDefaults(SystemLauncher.java:118)
        //Thus, overwrite the default, but the file can be empty
        URL systemProperties = BuiltInMessageBroker.class.getClassLoader().getResource(SYSTEM_PROPERTIES);
        attributes.put("type", "Memory");
        attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
        attributes.put("initialSystemPropertiesLocation", systemProperties.toExternalForm());
        attributes.put("startupLoggedToSystemOut", true);
        return attributes;
    }

    public static void stop() {
        broker.shutdown();
    }

}
