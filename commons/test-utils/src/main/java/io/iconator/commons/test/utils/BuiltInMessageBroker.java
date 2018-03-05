package io.iconator.commons.test.utils;


import org.apache.qpid.server.SystemLauncher;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BuiltInMessageBroker {

    private static final String INITIAL_CONFIGURATION = "qpid-test-initial-config.json";

    private static SystemLauncher broker;

    public static void start() throws Exception {
        broker = new SystemLauncher();
        broker.startup(createSystemConfig());
    }

    private static Map<String, Object> createSystemConfig() {
        Map<String, Object> attributes = new HashMap<>();
        URL initialConfig = BuiltInMessageBroker.class.getClassLoader().getResource(INITIAL_CONFIGURATION);
        attributes.put("type", "Memory");
        attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
        attributes.put("startupLoggedToSystemOut", true);
        return attributes;
    }

    public static void stop() {
        broker.shutdown();
    }

}
