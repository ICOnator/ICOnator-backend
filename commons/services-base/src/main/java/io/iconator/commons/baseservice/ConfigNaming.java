package io.iconator.commons.baseservice;

public class ConfigNaming {
    public static void set(String... names){ System.setProperty("spring.config.name", String.join(",", names)); }
}
